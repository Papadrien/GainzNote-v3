package fr.junade.gainznote

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import fr.junade.gainznote.db.DatabaseDriverFactory
import fr.junade.gainznote.repository.WorkoutRepository
import fr.junade.gainznote.i18n.S
import fr.junade.gainznote.BuildConfig
import fr.junade.gainznote.ui.App
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {

    private var pendingExportJson: String? = null
    private var onJsonReadCallback: ((String) -> Unit)? = null

    // ── Billing (achat suppression pubs) ─────────────────────────────────
    private lateinit var billingManager: BillingManager
    /** Prix formaté récupéré depuis le Play Store (ex: "1,99 €", "$1.99"). Null tant que non chargé. */
    private var removeAdsPrice by mutableStateOf<String?>(null)
    /** État d'achat réactif — mis à jour par BillingManager et lu par le composable App. */
    private var isAdFreePurchased by mutableStateOf(false)

    // ── Interstitiel AdMob ────────────────────────────────────────────────────
    private var interstitialAd: InterstitialAd? = null

    /** Pré-charge un interstitiel pour la prochaine utilisation. */
    private fun loadInterstitial() {
        val adUnitId = if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/1033173712" // Interstitiel de test Google
        } else {
            "ca-app-pub-7203301690798915/5097105544" // Interstitiel de production
        }
        InterstitialAd.load(
            this,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d("GainzAds", "Interstitiel chargé")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    Log.d("GainzAds", "Échec chargement interstitiel: ${error.message}")
                }
            }
        )
    }

    /**
     * Tente d'afficher l'interstitiel puis exécute [onDismissed].
     * Affiché une fois tous les 3 entraînements terminés pour rester non-intrusif.
     * Si la pub n'est pas prête, on passe directement au callback.
     */
    private fun showInterstitialThen(onDismissed: () -> Unit) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitial()   // recharger pour la prochaine fois
                    onDismissed()
                }
                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    loadInterstitial()
                    onDismissed()        // fallback : ne pas bloquer l'utilisateur
                }
            }
            ad.show(this)
        } else {
            onDismissed()
        }
    }

    // ── Export ────────────────────────────────────────────────────────────────
    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        try {
            contentResolver.openOutputStream(uri)?.use {
                it.write((pendingExportJson ?: "[]").toByteArray())
            }
            Toast.makeText(this, S.dataSaved, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, S.saveError, Toast.LENGTH_LONG).show()
        }
        pendingExportJson = null
    }

    // ── Import ────────────────────────────────────────────────────────────────
    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        try {
            val json = contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
            if (json.isNullOrBlank() || !json.trim().startsWith("[")) {
                Toast.makeText(this, S.invalidFileFormat, Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }
            onJsonReadCallback?.invoke(json)
        } catch (e: Exception) {
            Toast.makeText(this, S.fileMalformatted, Toast.LENGTH_LONG).show()
        }
        onJsonReadCallback = null
    }

    // ── Permission notifications ──────────────────────────────────────────────
    private var pendingNotifCallback: ((Boolean) -> Unit)? = null

    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(this, S.notifPermDenied, Toast.LENGTH_LONG).show()
        }
        pendingNotifCallback?.invoke(granted)
        pendingNotifCallback = null
    }

    /**
     * Vérifie et demande la permission POST_NOTIFICATIONS si nécessaire.
     * Appelle [onResult] avec true si accordée, false sinon.
     * Sur Android < 13 : toujours true (pas de permission runtime).
     */
    private fun requestNotifPermission(onResult: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val perm = android.Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED) {
                onResult(true)
            } else {
                pendingNotifCallback = onResult
                notifPermLauncher.launch(perm)
            }
        } else {
            onResult(true) // Pas de permission runtime avant Android 13
        }
    }

    // ── Service chrono ────────────────────────────────────────────────────────

    private fun startChronoService(startTimeMs: Long) {
        // La permission est déjà vérifiée au toggle du paramètre — on démarre directement
        val intent = Intent(this, ChronoForegroundService::class.java).apply {
            action = ChronoForegroundService.ACTION_START
            putExtra(ChronoForegroundService.EXTRA_START_TIME, startTimeMs)
        }
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopChronoService() {
        val intent = Intent(this, ChronoForegroundService::class.java).apply {
            action = ChronoForegroundService.ACTION_STOP
        }
        startService(intent)
    }

    // ── onCreate ──────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialiser le SDK AdMob
        MobileAds.initialize(this) {}
        loadInterstitial()

        val repo = WorkoutRepository(DatabaseDriverFactory(this))

        // Initialiser Google Play Billing
        billingManager = BillingManager(
            context = this,
            onAdFreeChanged = { purchased ->
                // Met à jour le state Compose (UI immédiate) ET persiste en DB
                isAdFreePurchased = purchased
                lifecycleScope.launch {
                    val settings = repo.getAppSettings()
                    if (settings.adFree != purchased) {
                        repo.saveAppSettings(settings.copy(adFree = purchased))
                    }
                }
            },
            onPriceChanged = { price ->
                removeAdsPrice = price
            }
        )
        billingManager.startConnection()

        setContent {
            App(
                driverFactory = DatabaseDriverFactory(this),
                onExit = { finish() },
                onExportReady = { json ->
                    pendingExportJson = json
                    exportLauncher.launch("gainznote_sauvegarde.json")
                },
                onImportRequest = { composableCallback ->
                    onJsonReadCallback = { json ->
                        lifecycleScope.launch {
                            val hasData = repo.hasWorkouts()
                            if (hasData) {
                                showImportChoiceDialog(json, repo, composableCallback)
                            } else {
                                composableCallback(json)
                                Toast.makeText(this@MainActivity, S.dataRestored, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                },
                onRequestNotifPermission = { onResult -> requestNotifPermission(onResult) },
                onChronoStart = { startTimeMs -> startChronoService(startTimeMs) },
                onChronoStop  = { stopChronoService() },
                onShowInterstitial = { onDismissed -> showInterstitialThen(onDismissed) },
                isDebug = BuildConfig.DEBUG,
                removeAdsPrice = removeAdsPrice,
                purchasedAdFree = isAdFreePurchased,
                onPurchaseRemoveAds = {
                    if (!billingManager.launchPurchase(this@MainActivity)) {
                        Toast.makeText(this@MainActivity, S.purchaseError, Toast.LENGTH_SHORT).show()
                    }
                },
                onRestorePurchases = {
                    billingManager.restorePurchases { found ->
                        runOnUiThread {
                            val msg = if (found) S.restorePurchasesSuccess else S.restorePurchasesNone
                            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }

    override fun onDestroy() {
        stopChronoService()
        billingManager.destroy()
        super.onDestroy()
    }

    private fun showImportChoiceDialog(
        json: String,
        repo: WorkoutRepository,
        composableCallback: (String) -> Unit
    ) {
        android.app.AlertDialog.Builder(this)
            .setTitle(S.restoreDialogTitle)
            .setMessage(S.restoreDialogBody)
            .setPositiveButton(S.addToExisting) { _, _ ->
                composableCallback(json)
                Toast.makeText(this, S.dataAdded, Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton(S.overwriteAll) { _, _ ->
                lifecycleScope.launch {
                    try {
                        repo.deleteAllWorkouts()
                        composableCallback(json)
                        Toast.makeText(this@MainActivity, S.dataReplaced, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, S.importError, Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton(S.cancel, null)
            .show()
    }
}
