package com.gainznote.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Palette bleue GainzNote ──────────────────────────────────────────────────
// Inspirée de l'image fournie : bleu foncé navy + bleu clair vif

object GainzColors {
    // ── Dark (navy profond + bleu accent vif) ─────────────────────────────────
    val AccentDark        = Color(0xFF4DB8FF) // bleu ciel vif — lisible sur fond sombre
    val AccentDimDark     = Color(0xFF0A2040) // fond badge actif
    val BackgroundDark    = Color(0xFF0A1628) // navy très sombre
    val SurfaceDark       = Color(0xFF112240) // navy sombre
    val SurfaceAltDark    = Color(0xFF1A3050) // navy moyen — champs de saisie
    val BorderDark        = Color(0xFF1E3D63) // bordures discrètes
    val TextDark          = Color(0xFFE8F0FF) // blanc bleuté
    val TextSecDark       = Color(0xFF88AACC) // bleu-gris clair
    val TextMutedDark     = Color(0xFF3D6080) // discret
    val DangerDark        = Color(0xFFEF5350) // rouge
    val SupersetDark      = Color(0xFF9F8FEF) // violet clair
    val SupersetDimDark   = Color(0xFF1C1840)

    // ── Light (bleu très clair + bleu foncé accent) ───────────────────────────
    val AccentLight       = Color(0xFF1565C0) // bleu foncé — excellent contraste sur blanc
    val AccentDimLight    = Color(0xFFC8DEFF) // bleu très pâle
    val BackgroundLight   = Color(0xFFE8F0FE) // bleu glacé très clair
    val SurfaceLight      = Color(0xFFFFFFFF)
    val SurfaceAltLight   = Color(0xFFD8E8F8) // champs de saisie bleutés
    val BorderLight       = Color(0xFFB0CAE8)
    val TextLight         = Color(0xFF0A1628) // navy sur fond clair
    val TextSecLight      = Color(0xFF1E3D63)
    val TextMutedLight    = Color(0xFF5580A8)
    val DangerLight       = Color(0xFFC62828)
    val SupersetLight     = Color(0xFF5C50D0)
    val SupersetDimLight  = Color(0xFFE0DAFF)
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
        primary         = c.accent,
        onPrimary       = Color.Black,
        background      = c.background,
        surface         = c.surface,
        onBackground    = c.text,
        onSurface       = c.text,
        error           = c.danger,
        onError         = Color.White,
        surfaceVariant  = c.surfaceAlt,
        outline         = c.border
    ) else lightColorScheme(
        primary         = c.accent,
        onPrimary       = Color.White,
        background      = c.background,
        surface         = c.surface,
        onBackground    = c.text,
        onSurface       = c.text,
        error           = c.danger,
        onError         = Color.White,
        surfaceVariant  = c.surfaceAlt,
        outline         = c.border
    )
    MaterialTheme(colorScheme = scheme, content = content)
}
