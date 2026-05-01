package com.gainznote.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gainznote.ui.theme.GainzThemeColors

@Composable
fun FloatingTimer(
    visible: Boolean,
    timerDisplay: String,
    onClose: () -> Unit,
    c: GainzThemeColors,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        modifier = modifier,          // ← applique le modifier (align, padding) ici
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
    ) {
        // Aligne le chip à droite, pleine largeur pour le placement
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .border(2.dp, c.accent, RoundedCornerShape(12.dp))
                    .background(c.surface, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        timerDisplay,
                        color = c.text,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Box(
                        Modifier
                            .size(28.dp)
                            .clickable(onClick = onClose),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "\u2715",
                            color = c.textMuted,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
