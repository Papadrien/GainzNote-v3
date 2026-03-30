package com.gainznote.ui

import androidx.compose.runtime.Composable

// Contrôle la couleur des icônes de la status bar selon le thème
@Composable
expect fun SystemBarsEffect(darkTheme: Boolean)
