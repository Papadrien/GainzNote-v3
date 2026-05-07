package fr.junade.gainznote.ui

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS gère le retour arrière via le swipe natif
}
