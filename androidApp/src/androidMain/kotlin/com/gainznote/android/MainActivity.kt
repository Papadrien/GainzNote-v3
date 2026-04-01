package com.gainznote.android

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.gainznote.db.DatabaseDriverFactory
import com.gainznote.repository.WorkoutRepository
import com.gainznote.ui.App
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var pendingExportJson: String? = null

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

    // Callback appelé une fois que le JSON a été lu depuis le fichier
    private var onJsonReadCallback: ((String) -> Unit)? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
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
                    // Étape 1 : lire le fichier
                    onJsonReadCallback = { json ->
                        // Étape 2 : vérifier s'il y a des données existantes
                        lifecycleScope.launch {
                            val hasData = repo.hasWorkouts()
                            if (hasData) {
                                // Étape 3 : dialogue merge / écraser
                                showImportChoiceDialog(json, repo, composableCallback)
                            } else {
                                // Pas de données → importer directement
                                composableCallback(json)
                                Toast.makeText(this@MainActivity, "Données restaurées ✓", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                }
            )
        }
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
