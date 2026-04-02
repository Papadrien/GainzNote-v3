package com.gainznote.android

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import com.gainznote.ui.App
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var pendingExportJson: String? = null
    private var onJsonReadCallback: ((String) -> Unit)? = null

    // ── Export ────────────────────────────────────────────────────────────────
    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        try {
            contentResolver.openOutputStream(uri)?.use {
                it.write((pendingExportJson ?: "[]").toByteArray())
            }
            Toast.makeText(this, "Données sauvegardées ✓", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Erreur lors de la sauvegarde", Toast.LENGTH_LONG).show()
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
                Toast.makeText(this, "Format de fichier invalide", Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }
            onJsonReadCallback?.invoke(json)
        } catch (e: Exception) {
            Toast.makeText(this, "Erreur : fichier mal formaté", Toast.LENGTH_LONG).show()
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
                "Autorisation refusée — la notification chrono ne s'affichera pas",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // ── Service chrono ────────────────────────────────────────────────────────

    private fun startChronoService(startTimeMs: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val perm = android.Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                // Demander la permission puis relancer
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
        // Splash screen — doit être appelé AVANT super.onCreate
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                                Toast.makeText(this@MainActivity, "Données restaurées ✓", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                },
                onChronoStart = { startTimeMs -> startChronoService(startTimeMs) },
                onChronoStop  = { stopChronoService() }
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
            .setTitle("Restaurer les données")
            .setMessage("Des entraînements existent déjà.\n\nQue souhaitez-vous faire ?")
            .setPositiveButton("Ajouter aux existants") { _, _ ->
                composableCallback(json)
                Toast.makeText(this, "Données ajoutées ✓", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Écraser tout") { _, _ ->
                lifecycleScope.launch {
                    try {
                        repo.deleteAllWorkouts()
                        composableCallback(json)
                        Toast.makeText(this@MainActivity, "Données remplacées ✓", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Erreur lors de l'import", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
}
