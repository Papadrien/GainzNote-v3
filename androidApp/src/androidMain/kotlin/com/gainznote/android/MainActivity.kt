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
import com.gainznote.db.DatabaseDriverFactory
import com.gainznote.ui.App

class MainActivity : ComponentActivity() {

    private var pendingExportJson: String? = null
    private var pendingImportCallback: ((String) -> Unit)? = null

    // Launcher pour créer le fichier d'export
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

    // Launcher pour sélectionner le fichier à importer
    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        try {
            val json = contentResolver.openInputStream(uri)
                ?.bufferedReader()?.readText()
            if (json.isNullOrBlank()) {
                Toast.makeText(this, "Fichier vide ou illisible", Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }
            // Validation basique : doit commencer par [
            if (!json.trim().startsWith("[")) {
                Toast.makeText(this, "Format de fichier invalide", Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }
            pendingImportCallback?.invoke(json)
            Toast.makeText(this, "Données restaurées ✓", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Erreur : fichier mal formaté", Toast.LENGTH_LONG).show()
        }
        pendingImportCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App(
                driverFactory = DatabaseDriverFactory(this),
                onExit = { finish() },
                onExportReady = { json ->
                    pendingExportJson = json
                    exportLauncher.launch("gainznote_sauvegarde.json")
                },
                onImportRequest = { callback ->
                    pendingImportCallback = callback
                    importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                }
            )
        }
    }
}
