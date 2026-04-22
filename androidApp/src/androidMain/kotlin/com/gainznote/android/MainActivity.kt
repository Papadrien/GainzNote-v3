package com.gainznote.android

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
import com.gainznote.db.DatabaseDriverFactory
import com.gainznote.repository.WorkoutRepository
import com.gainznote.i18n.S
import com.gainznote.ui.App
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var pendingExportJson: String? = null
    private var onJsonReadCallback: ((String) -> Unit)? = null

    // ── Interstitiel AdMob ────────────────────────────────────────────────────
    private var interstitialAd: InterstitialAd? = null

    /** Pré-charge un interstitiel pour la prochaine utilisation. */
    private fun loadInterstitial() {
        InterstitialAd.load(
            this,
            // ID de test Google — remplacer par le vrai avant publication
            "ca-app-pub-3940256099942544/1033173712",
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
    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(
                this,
                S.notifPermDenied,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // ── Service chrono ────────────────────────────────────────────────────────

    private fun startChronoService(startTimeMs: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val perm = android.Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                notifPermLauncher.launch(perm)
                return
            }
        }
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
                onChronoStart = { startTimeMs -> startChronoService(startTimeMs) },
                onChronoStop  = { stopChronoService() },
                onShowInterstitial = { onDismissed -> showInterstitialThen(onDismissed) }
            )
        }
    }

    override fun onDestroy() {
        stopChronoService()
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
