package com.gainznote.ui.cardio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gainznote.i18n.S
import com.gainznote.model.WorkoutType
import com.gainznote.repository.WorkoutRepository
import com.gainznote.ui.theme.themeColorsFor

@Composable
fun CardioSetupScreen(
    repo: WorkoutRepository,
    darkTheme: Boolean,
    blackBg: Boolean = false,
    templateId: String? = null,
    resumeId: String? = null,
    onBack: () -> Unit,
    onFinished: () -> Unit,
    chronoNotifEnabled: Boolean = false,
    onChronoStart: (Long) -> Unit = {},
    onChronoStop: () -> Unit = {}
) {
    val c = themeColorsFor(WorkoutType.CARDIO, darkTheme, blackBg)
    Column(
        Modifier.fillMaxSize().background(c.background).safeDrawingPadding().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("\uD83C\uDFC3", fontSize = 56.sp)
        Spacer(Modifier.height(16.dp))
        Text(S.cardioComingSoon, color = c.accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(S.cardioComingSoonDesc, color = c.textSec, fontSize = 14.sp)
        Spacer(Modifier.height(24.dp))
        androidx.compose.material3.TextButton(onClick = onBack) {
            Text(S.back, color = c.accent)
        }
    }
}
