package fr.junade.gainznote.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * AdMob iOS — IDs de production
 * App ID      : ca-app-pub-7203301690798915~1591217211
 * Bannière    : ca-app-pub-7203301690798915/8844778861
 * Interstitiel: ca-app-pub-7203301690798915/6787703571
 *
 * TODO: Implémenter avec UIKitView + GADBannerView
 * Exemple d'intégration :
 *   val bannerView = GADBannerView(UIScreen.mainScreen.bounds)
 *   bannerView.adUnitID = "ca-app-pub-7203301690798915/8844778861"
 */

// ID production iOS
const val IOS_ADMOB_APP_ID          = "ca-app-pub-7203301690798915~1591217211"
const val IOS_ADMOB_BANNER_ID       = "ca-app-pub-7203301690798915/8844778861"
const val IOS_ADMOB_INTERSTITIAL_ID = "ca-app-pub-7203301690798915/6787703571"

@Composable
actual fun AdBanner(modifier: Modifier) {
    // TODO: Implémenter avec UIKitView + GADBannerView pour iOS
    // Utiliser IOS_ADMOB_BANNER_ID comme adUnitID
}
