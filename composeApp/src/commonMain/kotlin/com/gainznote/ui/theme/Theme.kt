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

    // ── Cardio (vert) - Palette étendue ─────────────────────────────────────
    val CardioAccentDark      = Color(0xFF4DD98C)
    val CardioAccentDimDark   = Color(0xFF0E4A2A)
    val CardioBackgroundDark  = Color(0xFF0A2818)
    val CardioSurfaceDark     = Color(0xFF114030)
    val CardioSurfaceAltDark  = Color(0xFF1A5040)
    val CardioBorderDark      = Color(0xFF1E6348)

    val CardioAccentLight     = Color(0xFF1B8A4A)
    val CardioAccentDimLight  = Color(0xFFC8F0D8)
    val CardioBackgroundLight = Color(0xFFE8F5EE)
    val CardioSurfaceLight    = Color(0xFFFFFFFF)
    val CardioSurfaceAltLight = Color(0xFFD8F0E8)
    val CardioBorderLight     = Color(0xFFB0E8C8)

    // ── Circuit (rouge) - Palette étendue ───────────────────────────────────
    val CircuitAccentDark     = Color(0xFFFF6B6B)
    val CircuitAccentDimDark  = Color(0xFF5A1F1F)
    val CircuitBackgroundDark = Color(0xFF281010)
    val CircuitSurfaceDark    = Color(0xFF402020)
    val CircuitSurfaceAltDark = Color(0xFF502A2A)
    val CircuitBorderDark     = Color(0xFF634040)

    val CircuitAccentLight    = Color(0xFFC62828)
    val CircuitAccentDimLight = Color(0xFFFFD0D0)
    val CircuitBackgroundLight= Color(0xFFFFF0F0)
    val CircuitSurfaceLight   = Color(0xFFFFFFFF)
    val CircuitSurfaceAltLight= Color(0xFFF8E8E8)
    val CircuitBorderLight    = Color(0xFFE8C0C0)
}

data class GainzThemeColors(
    val dark: Boolean,
    val blackBg: Boolean = false,
    val workoutType: WorkoutType = WorkoutType.MUSCULATION
) {
    val accent get() = when (workoutType) {
        WorkoutType.CARDIO -> if (dark) GainzColors.CardioAccentDark else GainzColors.CardioAccentLight
        WorkoutType.CIRCUIT -> if (dark) GainzColors.CircuitAccentDark else GainzColors.CircuitAccentLight
        else -> if (dark) GainzColors.AccentDark else GainzColors.AccentLight
    }

    val accentDim get() = when (workoutType) {
        WorkoutType.CARDIO -> if (dark) GainzColors.CardioAccentDimDark else GainzColors.CardioAccentDimLight
        WorkoutType.CIRCUIT -> if (dark) GainzColors.CircuitAccentDimDark else GainzColors.CircuitAccentDimLight
        else -> if (dark) GainzColors.AccentDimDark else GainzColors.AccentDimLight
    }

    val background get() = when {
        dark && blackBg -> GainzColors.BackgroundBlack
        dark -> when (workoutType) {
            WorkoutType.CARDIO -> GainzColors.CardioBackgroundDark
            WorkoutType.CIRCUIT -> GainzColors.CircuitBackgroundDark
            else -> GainzColors.BackgroundDark
        }
        else -> when (workoutType) {
            WorkoutType.CARDIO -> GainzColors.CardioBackgroundLight
            WorkoutType.CIRCUIT -> GainzColors.CircuitBackgroundLight
            else -> GainzColors.BackgroundLight
        }
    }

    val surface get() = when (workoutType) {
        WorkoutType.CARDIO -> if (dark) GainzColors.CardioSurfaceDark else GainzColors.CardioSurfaceLight
        WorkoutType.CIRCUIT -> if (dark) GainzColors.CircuitSurfaceDark else GainzColors.CircuitSurfaceLight
        else -> if (dark) GainzColors.SurfaceDark else GainzColors.SurfaceLight
    }

    val surfaceAlt get() = when (workoutType) {
        WorkoutType.CARDIO -> if (dark) GainzColors.CardioSurfaceAltDark else GainzColors.CardioSurfaceAltLight
        WorkoutType.CIRCUIT -> if (dark) GainzColors.CircuitSurfaceAltDark else GainzColors.CircuitSurfaceAltLight
        else -> if (dark) GainzColors.SurfaceAltDark else GainzColors.SurfaceAltLight
    }

    val border get() = when (workoutType) {
        WorkoutType.CARDIO -> if (dark) GainzColors.CardioBorderDark else GainzColors.CardioBorderLight
        WorkoutType.CIRCUIT -> if (dark) GainzColors.CircuitBorderDark else GainzColors.CircuitBorderLight
        else -> if (dark) GainzColors.BorderDark else GainzColors.BorderLight
    }

    val text        get() = if (dark) GainzColors.TextDark        else GainzColors.TextLight
    val textSec     get() = if (dark) GainzColors.TextSecDark     else GainzColors.TextSecLight
    val textMuted   get() = if (dark) GainzColors.TextMutedDark   else GainzColors.TextMutedLight
    val danger      get() = if (dark) GainzColors.DangerDark      else GainzColors.DangerLight
    val superset    get() = if (dark) GainzColors.SupersetDark    else GainzColors.SupersetLight
    val supersetDim get() = if (dark) GainzColors.SupersetDimDark else GainzColors.SupersetDimLight
}

/** Palette d'accent (accent, accentDim) correspondant à un type d'entraînement. */
fun accentPairFor(type: WorkoutType, dark: Boolean): Pair<Color, Color> = when (type) {
    WorkoutType.MUSCULATION -> if (dark)
        GainzColors.AccentDark to GainzColors.AccentDimDark
    else
        GainzColors.AccentLight to GainzColors.AccentDimLight
    WorkoutType.CARDIO -> if (dark)
        GainzColors.CardioAccentDark to GainzColors.CardioAccentDimDark
    else
        GainzColors.CardioAccentLight to GainzColors.CardioAccentDimLight
    WorkoutType.CIRCUIT -> if (dark)
        GainzColors.CircuitAccentDark to GainzColors.CircuitAccentDimDark
    else
        GainzColors.CircuitAccentLight to GainzColors.CircuitAccentDimLight
}

/** Construit un GainzThemeColors avec l'accent du type d'entraînement. */
fun themeColorsFor(type: WorkoutType, dark: Boolean, blackBg: Boolean = false): GainzThemeColors {
    return GainzThemeColors(dark = dark, blackBg = blackBg, workoutType = type)
}

@Composable
fun GainzTheme(dark: Boolean, blackBg: Boolean = false, content: @Composable () -> Unit) {
    val c = GainzThemeColors(dark, blackBg)
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
    blackBg: Boolean = false,
    content: @Composable (GainzThemeColors) -> Unit
) {
    val c = themeColorsFor(type, dark, blackBg)
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
