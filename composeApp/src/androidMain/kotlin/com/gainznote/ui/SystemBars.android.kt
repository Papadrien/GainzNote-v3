package com.gainznote.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat

@Composable
actual fun SystemBarsEffect(darkTheme: Boolean) {
    val activity = LocalContext.current as? Activity ?: return
    SideEffect {
        // isAppearanceLightStatusBars = true → icônes SOMBRES (pour fond clair)
        // isAppearanceLightStatusBars = false → icônes CLAIRES (pour fond sombre)
        WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            .isAppearanceLightStatusBars = !darkTheme
    }
}
