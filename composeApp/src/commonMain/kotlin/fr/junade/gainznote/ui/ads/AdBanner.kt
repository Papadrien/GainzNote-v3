package fr.junade.gainznote.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Bannière publicitaire AdMob.
 * Implémentée côté Android avec AndroidView + AdView.
 * Côté iOS : stub vide (à implémenter plus tard avec GADBannerView).
 */
@Composable
expect fun AdBanner(modifier: Modifier = Modifier)
