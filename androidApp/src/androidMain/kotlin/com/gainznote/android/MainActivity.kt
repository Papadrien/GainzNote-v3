package com.gainznote.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gainznote.db.DatabaseDriverFactory
import com.gainznote.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App(
                driverFactory = DatabaseDriverFactory(this),
                onExit = { finish() }  // ferme l'app si on est à Home et qu'on appuie retour
            )
        }
    }
}
