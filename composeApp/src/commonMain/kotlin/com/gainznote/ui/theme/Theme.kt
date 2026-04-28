package com.gainznote.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.gainznote.model.WorkoutType

object GainzColors {
    // ── Dark (Muscu - bleu) ──────────────────────────────────────────────────
    val AccentDark        = Color(0xFF4DB8FF)
    val AccentDimDark     = Color(0xFF0E3975)
    val BackgroundDark    = Color(0xFF0A1628)
    val BackgroundBlack   = Color(0xFF000000)
    val SurfaceDark       = Color(0xFF112240)
    val SurfaceAltDark    = Color(0xFF1A3050)
    val BorderDark        = Color(0xFF1E3D63)
    val TextDark          = Color(0xFFE8F0FF)
    val TextSecDark       = Color(0xFF88AACC)
    val TextMutedDark     = Color(0xFF3D6080)
    val DangerDark        = Color(0xFFEF5350)
    val SupersetDark      = Color(0xFF9F8FEF)
    val SupersetDimDark   = Color(0xFF1C1840)

    // ── Light (Muscu - bleu) ─────────────────────────────────────────────────
    val AccentLight       = Color(0xFF1565C0)
    val AccentDimLight    = Color(0xFFC8DEFF)
    val BackgroundLight   = Color(0xFFE8F0FE)
    val SurfaceLight      = Color(0xFFFFFFFF)
    val SurfaceAltLight   = Color(0xFFD8E8F8)
    val BorderLight       = Color(0xFFB0CAE8)
    val TextLight         = Color(0xFF0A1628)
    val TextSecLight      = Color(0xFF1E3D63)
    val TextMutedLight    = Color(0xFF5580A8)
    val DangerLight       = Color(0xFFC62828)
    val SupersetLight     = Color(0xFF5C50D0)
    val SupersetDimLight  = Color(0xFFE0DAFF)

    // ── Cardio (vert) ────────────────────────────────────────────────────────
    val CardioAccentDark      = Color(0xFF4DD98C)
    val CardioAccentDimDark   = Color(0xFF0E4A2A)
    val CardioBackgroundDark  = Color(0xFF0A2816)
    val CardioSurfaceDark     = Color(0xFF114022)
    val CardioSurfaceAltDark  = Color(0xFF1A5030)
    val CardioBorderDark      = Color(0xFF1E633D)
    val CardioTextDark        = Color(0xFFE8FFF0)
    val CardioTextSecDark     = Color(0xFF88CCAA)
    val CardioTextMutedDark   = Color(0xFF3D8060)

    val CardioAccentLight     = Color(0xFF1B8A4A)
    val CardioAccentDimLight  = Color(0xFFC8F0D8)
    val CardioBackgroundLight = Color(0xFFE8FEF0)
    val CardioSurfaceLight    = Color(0xFFFFFFFF)
    val CardioSurfaceAltLight = Color(0xFFD8F8E8)
    val CardioBorderLight     = Color(0xFFB0E8CA)
    val CardioTextLight       = Color(0xFF0A2816)
    val CardioTextSecLight    = Color(0xFF1E633D)
    val CardioTextMutedLight  = Color(0xFF55A880)

    // ── Circuit (rouge) ──────────────────────────────────────────────────────
    val CircuitAccentDark     = Color(0xFFFF6B6B)
    val CircuitAccentDimDark  = Color(0xFF5A1F1F)
    val CircuitBackgroundDark = Color(0xFF280A0A)
    val CircuitSurfaceDark    = Color(0xFF401111)
    val CircuitSurfaceAltDark = Color(0xFF501A1A)
    val CircuitBorderDark     = Color(0xFF631E1E)
    val CircuitTextDark       = Color(0xFFFFE8E8)
    val CircuitTextSecDark    = Color(0xFFCC8888)
    val CircuitTextMutedDark  = Color(0xFF803D3D)

    val CircuitAccentLight    = Color(0xFFC62828)
    val CircuitAccentDimLight = Color(0xFFFFD0D0)
    val CircuitBackgroundLight= Color(0xFFFEE8E8)
    val CircuitSurfaceLight   = Color(0xFFFFFFFF)
    val CircuitSurfaceAltLight= Color(0xFFF8D8D8)
    val CircuitBorderLight    = Color(0xFFE8B0B0)
    val CircuitTextLight      = Color(0xFF280A0A)
    val CircuitTextSecLight   = Color(0xFF631E1E)
    val CircuitTextMutedLight = Color(0xFFA85555)
}

data class GainzThemeColors(
    val dark: Boolean,
    val type: WorkoutType = WorkoutType.MUSCULATION
) {
    val accent: Color get() = when(type) {
        WorkoutType.MUSCULATION -> if (dark) GainzColors.AccentDark else GainzColors.AccentLight
        WorkoutType.CARDIO -> if (dark) GainzColors.CardioAccentDark else GainzColors.CardioAccentLight
        WorkoutType.CIRCUIT -> if (dark) GainzColors.CircuitAccentDark else GainzColors.CircuitAccentLight
    }
    val accentDim: Color get() = when(type) {
        WorkoutType.MUSCULATION -> if (dark) GainzColors.AccentDimDark else GainzColors.AccentDimLight
        WorkoutType.CARDIO -> if (dark) GainzColors.CardioAccentDimDark else GainzColors.CardioAccentDimLight
        WorkoutType.CIRCUIT -> if (dark) GainzColors.CircuitAccentDimDark else GainzColors.CircuitAccentDimLight
    }
    val background: Color get() = if (dark) GainzColors.BackgroundBlack else Color.White
    val surface: Color get() = when(type) {
        WorkoutType.MUSCULATION -> if (dark) GainzColors.SurfaceDark else GainzColors.SurfaceLight
        WorkoutType.CARDIO -> if (dark) GainzColors.CardioSurfaceDark else GainzColors.CardioSurfaceLight
        WorkoutType.CIRCUIT -> if (dark) GainzColors.CircuitSurfaceDark else GainzColors.CircuitSurfaceLight
    }
    val surfaceAlt: Color get() = when(type) {
        WorkoutType.MUSCULATION -> if (dark) GainzColors.SurfaceAltDark else GainzColors.SurfaceAltLight
        WorkoutType.CARDIO -> if (dark) GainzColors.CardioSurfaceAltDark else GainzColors.CardioSurfaceAltLight
        WorkoutType.CIRCUIT -> if (dark) GainzColors.CircuitSurfaceAltDark else GainzColors.CircuitSurfaceAltLight
    }
    val border: Color get() = when(type) {
        WorkoutType.MUSCULATION -> if (dark) GainzColors.BorderDark else GainzColors.BorderLight
        WorkoutType.CARDIO -> if (dark) GainzColors.CardioBorderDark else GainzColors.CardioBorderLight
        WorkoutType.CIRCUIT -> if (dark) GainzColors.CircuitBorderDark else GainzColors.CircuitBorderLight
    }
    val text: Color get() = when(type) {
        WorkoutType.MUSCULATION -> if (dark) GainzColors.TextDark else GainzColors.TextLight
        WorkoutType.CARDIO -> if (dark) GainzColors.CardioTextDark else GainzColors.CardioTextLight
        WorkoutType.CIRCUIT -> if (dark) GainzColors.CircuitTextDark else GainzColors.CircuitTextLight
    }
    val textSec: Color get() = when(type) {
        WorkoutType.MUSCULATION -> if (dark) GainzColors.TextSecDark else GainzColors.TextSecLight
        WorkoutType.CARDIO -> if (dark) GainzColors.CardioTextSecDark else GainzColors.CardioTextSecLight
        WorkoutType.CIRCUIT -> if (dark) GainzColors.CircuitTextSecDark else GainzColors.CircuitTextSecLight
    }
    val textMuted: Color get() = when(type) {
        WorkoutType.MUSCULATION -> if (dark) GainzColors.TextMutedDark else GainzColors.TextMutedLight
        WorkoutType.CARDIO -> if (dark) GainzColors.CardioTextMutedDark else GainzColors.CardioTextMutedLight
        WorkoutType.CIRCUIT -> if (dark) GainzColors.CircuitTextMutedDark else GainzColors.CircuitTextMutedLight
    }
    val danger: Color get() = if (dark) GainzColors.DangerDark else GainzColors.DangerLight
    val superset: Color get() = if (dark) GainzColors.SupersetDark else GainzColors.SupersetLight
    val supersetDim: Color get() = if (dark) GainzColors.SupersetDimDark else GainzColors.SupersetDimLight
}

@Composable
fun GainzTheme(dark: Boolean, content: @Composable () -> Unit) {
    val c = GainzThemeColors(dark)
    val scheme = if (dark) darkColorScheme(
        primary = c.accent, onPrimary = Color.Black,
        background = c.background, surface = c.surface,
        onBackground = c.text, onSurface = c.text,
        error = c.danger, onError = Color.White,
        surfaceVariant = c.surfaceAlt, outline = c.border
    ) else lightColorScheme(
        primary = c.accent, onPrimary = Color.White,
        background = c.background, surface = c.surface,
        onBackground = c.text, onSurface = c.text,
        error = c.danger, onError = Color.White,
        surfaceVariant = c.surfaceAlt, outline = c.border
    )
    MaterialTheme(colorScheme = scheme, content = content)
}

/** Wrapper qui applique l'accent du type d'entraînement. */
@Composable
fun WorkoutTypeTheme(
    type: WorkoutType,
    dark: Boolean,
    content: @Composable (GainzThemeColors) -> Unit
) {
    val c = GainzThemeColors(dark = dark, type = type)
    val scheme = if (dark) darkColorScheme(
        primary = c.accent, onPrimary = Color.Black,
        background = c.background, surface = c.surface,
        onBackground = c.text, onSurface = c.text,
        error = c.danger, onError = Color.White,
        surfaceVariant = c.surfaceAlt, outline = c.border
    ) else lightColorScheme(
        primary = c.accent, onPrimary = Color.White,
        background = c.background, surface = c.surface,
        onBackground = c.text, onSurface = c.text,
        error = c.danger, onError = Color.White,
        surfaceVariant = c.surfaceAlt, outline = c.border
    )
    MaterialTheme(colorScheme = scheme) { content(c) }
}
