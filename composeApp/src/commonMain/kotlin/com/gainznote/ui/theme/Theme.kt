package com.gainznote.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object GainzColors {
    val AccentDark       = Color(0xFFE8C547)
    val AccentLight      = Color(0xFFC4960A)
    val BackgroundDark   = Color(0xFF0E0E0F)
    val BackgroundLight  = Color(0xFFF5F3EE)
    val SurfaceDark      = Color(0xFF1A1A1C)
    val SurfaceLight     = Color(0xFFFFFFFF)
    val SurfaceAltDark   = Color(0xFF242426)
    val SurfaceAltLight  = Color(0xFFEEECE7)
    val BorderDark       = Color(0xFF2E2E31)
    val BorderLight      = Color(0xFFDDD9D0)
    val TextDark         = Color(0xFFF0EEE8)
    val TextLight        = Color(0xFF1A1816)
    val TextSecDark      = Color(0xFFA8A49C)
    val TextSecLight     = Color(0xFF5C5750)
    val TextMutedDark    = Color(0xFF5A5855)
    val TextMutedLight   = Color(0xFFA8A49C)
    val DangerDark       = Color(0xFFE85447)
    val DangerLight      = Color(0xFFD63B2F)
    val SupersetDark     = Color(0xFF7B6FE8)
    val SupersetLight    = Color(0xFF5A4FD4)
    val SupersetDimDark  = Color(0xFF1A1730)
    val SupersetDimLight = Color(0xFFECEAFC)
    val AccentDimDark    = Color(0xFF2A250D)
    val AccentDimLight   = Color(0xFFFDF5DC)
}

data class GainzThemeColors(val dark: Boolean) {
    val accent       get() = if (dark) GainzColors.AccentDark       else GainzColors.AccentLight
    val background   get() = if (dark) GainzColors.BackgroundDark   else GainzColors.BackgroundLight
    val surface      get() = if (dark) GainzColors.SurfaceDark      else GainzColors.SurfaceLight
    val surfaceAlt   get() = if (dark) GainzColors.SurfaceAltDark   else GainzColors.SurfaceAltLight
    val border       get() = if (dark) GainzColors.BorderDark       else GainzColors.BorderLight
    val text         get() = if (dark) GainzColors.TextDark         else GainzColors.TextLight
    val textSec      get() = if (dark) GainzColors.TextSecDark      else GainzColors.TextSecLight
    val textMuted    get() = if (dark) GainzColors.TextMutedDark    else GainzColors.TextMutedLight
    val danger       get() = if (dark) GainzColors.DangerDark       else GainzColors.DangerLight
    val superset     get() = if (dark) GainzColors.SupersetDark     else GainzColors.SupersetLight
    val supersetDim  get() = if (dark) GainzColors.SupersetDimDark  else GainzColors.SupersetDimLight
    val accentDim    get() = if (dark) GainzColors.AccentDimDark    else GainzColors.AccentDimLight
}

@Composable
fun GainzTheme(dark: Boolean, content: @Composable () -> Unit) {
    val c = GainzThemeColors(dark)
    val scheme = if (dark) darkColorScheme(
        primary = c.accent, background = c.background,
        surface = c.surface, onBackground = c.text, onSurface = c.text,
        error = c.danger
    ) else lightColorScheme(
        primary = c.accent, background = c.background,
        surface = c.surface, onBackground = c.text, onSurface = c.text,
        error = c.danger
    )
    MaterialTheme(colorScheme = scheme, content = content)
}
