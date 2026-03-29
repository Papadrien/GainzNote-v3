package com.gainznote.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object GainzColors {
    // ── Dark ──────────────────────────────────────────────────────────────────
    val AccentDark        = Color(0xFFE8C547) // jaune doré — lisible sur fond sombre
    val AccentDimDark     = Color(0xFF3A3010) // fond du badge actif — assez clair pour voir l'icône
    val BackgroundDark    = Color(0xFF0E0E0F)
    val SurfaceDark       = Color(0xFF1A1A1C)
    val SurfaceAltDark    = Color(0xFF252527) // champs de saisie — légèrement plus clair
    val BorderDark        = Color(0xFF363639)
    val TextDark          = Color(0xFFF0EEE8)
    val TextSecDark       = Color(0xFFB0ACA4) // secondaire lisible (ratio ~4.5:1 sur surface dark)
    val TextMutedDark     = Color(0xFF6E6A66) // indices, labels
    val DangerDark        = Color(0xFFE85447)
    val SupersetDark      = Color(0xFF9B8FF0) // violet plus clair = meilleur contraste sur dark
    val SupersetDimDark   = Color(0xFF201C3A)

    // ── Light ─────────────────────────────────────────────────────────────────
    val AccentLight       = Color(0xFF9A6F00) // doré foncé — lisible sur fond clair (ratio > 4.5:1)
    val AccentDimLight    = Color(0xFFFFF3C4)
    val BackgroundLight   = Color(0xFFF4F2ED)
    val SurfaceLight      = Color(0xFFFFFFFF)
    val SurfaceAltLight   = Color(0xFFEAE8E2)
    val BorderLight       = Color(0xFFD4D0C8)
    val TextLight         = Color(0xFF1A1816)
    val TextSecLight      = Color(0xFF4A4742) // lisible sur fond clair
    val TextMutedLight    = Color(0xFF8A8680)
    val DangerLight       = Color(0xFFC0392B) // rouge foncé lisible sur blanc
    val SupersetLight     = Color(0xFF4A40C0) // violet foncé lisible sur fond clair
    val SupersetDimLight  = Color(0xFFEAE7FC)
}

data class GainzThemeColors(val dark: Boolean) {
    val accent      get() = if (dark) GainzColors.AccentDark      else GainzColors.AccentLight
    val accentDim   get() = if (dark) GainzColors.AccentDimDark   else GainzColors.AccentDimLight
    val background  get() = if (dark) GainzColors.BackgroundDark  else GainzColors.BackgroundLight
    val surface     get() = if (dark) GainzColors.SurfaceDark     else GainzColors.SurfaceLight
    val surfaceAlt  get() = if (dark) GainzColors.SurfaceAltDark  else GainzColors.SurfaceAltLight
    val border      get() = if (dark) GainzColors.BorderDark      else GainzColors.BorderLight
    val text        get() = if (dark) GainzColors.TextDark        else GainzColors.TextLight
    val textSec     get() = if (dark) GainzColors.TextSecDark     else GainzColors.TextSecLight
    val textMuted   get() = if (dark) GainzColors.TextMutedDark   else GainzColors.TextMutedLight
    val danger      get() = if (dark) GainzColors.DangerDark      else GainzColors.DangerLight
    val superset    get() = if (dark) GainzColors.SupersetDark    else GainzColors.SupersetLight
    val supersetDim get() = if (dark) GainzColors.SupersetDimDark else GainzColors.SupersetDimLight
}

@Composable
fun GainzTheme(dark: Boolean, content: @Composable () -> Unit) {
    val c = GainzThemeColors(dark)
    val scheme = if (dark) darkColorScheme(
        primary = c.accent,
        onPrimary = Color.Black,
        background = c.background,
        surface = c.surface,
        onBackground = c.text,
        onSurface = c.text,
        error = c.danger,
        onError = Color.White,
        surfaceVariant = c.surfaceAlt,
        outline = c.border
    ) else lightColorScheme(
        primary = c.accent,
        onPrimary = Color.White,
        background = c.background,
        surface = c.surface,
        onBackground = c.text,
        onSurface = c.text,
        error = c.danger,
        onError = Color.White,
        surfaceVariant = c.surfaceAlt,
        outline = c.border
    )
    MaterialTheme(colorScheme = scheme, content = content)
}
