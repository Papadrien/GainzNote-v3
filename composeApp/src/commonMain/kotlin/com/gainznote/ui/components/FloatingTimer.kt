package com.gainznote.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gainznote.i18n.S
import com.gainznote.ui.theme.GainzThemeColors

@Composable
fun FloatingTimer(
    visible: Boolean,
    timerDisplay: String,
    onClose: () -> Unit,
    c: GainzThemeColors
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = c.surface)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    timerDisplay, 
                    color = c.accent, 
                    fontSize = 20.sp, 
                    fontWeight = FontWeight.Bold
                )
                Box(Modifier.size(32.dp).clickable(onClick = onClose)) {
                    Text(
                        "✕", 
                        color = c.textMuted, 
                        fontSize = 18.sp, 
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}
