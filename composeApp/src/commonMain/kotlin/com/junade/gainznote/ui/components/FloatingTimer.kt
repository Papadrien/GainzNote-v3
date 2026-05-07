package com.junade.gainznote.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junade.gainznote.ui.theme.GainzThemeColors

@Composable
fun FloatingTimer(
    visible: Boolean,
    timerDisplay: String,
    onClose: () -> Unit,
    c: GainzThemeColors,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
    ) {
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
                Text(
                    timerDisplay,
                    color = c.text,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
