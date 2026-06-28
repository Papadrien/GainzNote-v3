package fr.junade.gainznote.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import gainznote.composeapp.generated.resources.Res
import gainznote.composeapp.generated.resources.junade_logo
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

/**
 * Splash screen Junadé affiché au démarrage.
 *
 * Affiche le logo centré sur fond noir avec un fade-in/out,
 * puis appelle [onFinished] pour céder la place à [HomeScreen].
 *
 * Durée totale : ~1 400 ms (300 fade-in · 800 hold · 300 fade-out).
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val logoAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, animationSpec = tween(durationMillis = 300))
        delay(800)
        logoAlpha.animateTo(0f, animationSpec = tween(durationMillis = 300))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.junade_logo),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.60f)
                .alpha(logoAlpha.value)
        )
    }
}
