package com.gainznote.ui.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.gainznote.model.Workout
import com.gainznote.repository.WorkoutRepository
import com.gainznote.ui.theme.GainzThemeColors
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    repo: WorkoutRepository,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onNewWorkout: () -> Unit,
    onHistory: () -> Unit,
    onOpenWorkout: (String) -> Unit
) {
    val c = GainzThemeColors(darkTheme)
    val scope = rememberCoroutineScope()
    var recentWorkouts by remember { mutableStateOf<List<Workout>>(emptyList()) }

    LaunchedEffect(Unit) {
        recentWorkouts = repo.getAllWorkouts().take(3)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text("GainzNote", color = c.accent, fontSize = 34.sp,
            fontWeight = FontWeight.Black, letterSpacing = (-1).sp)
        Text("Ton carnet de musculation", color = c.textMuted, fontSize = 13.sp)
        Spacer(Modifier.height(28.dp))

        // Bouton principal
        Button(
            onClick = onNewWorkout,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = c.accent)
        ) {
            Text("+ Nouvel entraînement", color = Color.Black,
                fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(32.dp))

        // Récents
        if (recentWorkouts.isNotEmpty()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                SectionLabel("Récents", c)
                TextButton(onClick = onHistory) {
                    Text("Voir tout →", color = c.accent, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            recentWorkouts.forEach { w ->
                RecentCard(w, c) { onOpenWorkout(w.id) }
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(24.dp))
        }

        // Paramètres
        SectionLabel("Paramètres", c)
        Spacer(Modifier.height(12.dp))

        // Switch thème
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = c.surface,
            border = BorderStroke(1.dp, c.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Mode sombre", color = c.text, fontSize = 15.sp)
                Switch(checked = darkTheme, onCheckedChange = { onToggleTheme() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = c.accent,
                        checkedTrackColor = c.accentDim))
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun SectionLabel(text: String, c: GainzThemeColors) {
    Text(text.uppercase(), color = c.textMuted, fontSize = 11.sp,
        fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
}

@Composable
fun RecentCard(workout: Workout, c: GainzThemeColors, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = c.surface,
        border = BorderStroke(1.dp, c.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(workout.title.ifBlank { "Sans titre" }, color = c.text,
                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(3.dp))
                Text(formatDisplayDate(workout.startedAt), color = c.textMuted, fontSize = 12.sp)
                Text("${workout.exercises.size} exercice(s)", color = c.textMuted, fontSize = 12.sp)
            }
            Text("›", color = c.textMuted, fontSize = 22.sp)
        }
    }
}

fun formatDisplayDate(iso: String): String = try {
    val instant = kotlinx.datetime.Instant.parse(iso)
    val local = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    val days = listOf("Lun","Mar","Mer","Jeu","Ven","Sam","Dim")
    val months = listOf("jan","fév","mar","avr","mai","jun","jul","aoû","sep","oct","nov","déc")
    "${days[local.dayOfWeek.ordinal]} ${local.dayOfMonth} ${months[local.monthNumber-1]} · ${local.hour.toString().padStart(2,'0')}:${local.minute.toString().padStart(2,'0')}"
} catch (e: Exception) { iso }
