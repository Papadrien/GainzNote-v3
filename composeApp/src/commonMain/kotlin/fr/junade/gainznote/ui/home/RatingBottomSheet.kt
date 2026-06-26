package fr.junade.gainznote.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.junade.gainznote.i18n.S
import fr.junade.gainznote.ui.theme.GainzThemeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingBottomSheet(
    c: GainzThemeColors,
    darkTheme: Boolean,
    onDismiss: () -> Unit,
    onRate: () -> Unit,
    onLater: () -> Unit,
    onNoThanks: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                S.ratingTitle,
                color = c.text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                S.ratingBody,
                color = c.textSec,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onRate,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = c.accent)
            ) {
                Text(
                    S.ratingConfirm,
                    color = if (darkTheme) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onLater,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, c.border)
            ) {
                Text(S.ratingLater, color = c.text, fontSize = 15.sp)
            }
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onNoThanks) {
                Text(S.ratingNo, color = c.textMuted, fontSize = 14.sp)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
