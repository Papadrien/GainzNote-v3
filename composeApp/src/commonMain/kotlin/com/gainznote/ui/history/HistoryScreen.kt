package com.gainznote.ui.history

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.gainznote.model.Workout
import com.gainznote.repository.WorkoutRepository
import com.gainznote.ui.home.formatDisplayDate
import com.gainznote.ui.theme.GainzThemeColors

@Composable
fun HistoryScreen(
    repo: WorkoutRepository,
    onBack: () -> Unit,
    onOpenDetail: (String) -> Unit,
    onUseAsTemplate: (String) -> Unit
) {
    val c = GainzThemeColors(true)
    var workouts by remember { mutableStateOf<List<Workout>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        workouts = repo.getAllWorkouts()
        loading = false
    }

    Column(Modifier.fillMaxSize().background(c.background).safeDrawingPadding()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clickable { onBack() }, contentAlignment = Alignment.Center) {
                Text("←", color = c.accent, fontSize = 22.sp)
            }
            Spacer(Modifier.width(8.dp))
            Text("Historique", color = c.text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider(color = c.border, thickness = 0.5.dp)

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = c.accent)
            }
        } else if (workouts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏋️", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Aucun entraînement enregistré", color = c.textMuted, fontSize = 15.sp)
                }
            }
        } else {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                workouts.forEach { w ->
                    HistoryCard(workout = w, c = c,
                        onClick = { onOpenDetail(w.id) },
                        onUseAsTemplate = { onUseAsTemplate(w.id) })
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun HistoryCard(workout: Workout, c: GainzThemeColors, onClick: () -> Unit, onUseAsTemplate: () -> Unit) {
    Column(Modifier.fillMaxWidth()
        .border(1.dp, c.border, RoundedCornerShape(14.dp))
        .background(c.surface, RoundedCornerShape(14.dp))) {
        Row(Modifier.fillMaxWidth().clickable { onClick() }.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(workout.title.ifBlank { "Sans titre" }, color = c.text,
                    fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(formatDisplayDate(workout.startedAt), color = c.textMuted, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatChip("${workout.exercises.size} exercices", c)
                    StatChip("${workout.exercises.sumOf { it.sets.size }} séries", c)
                }
                if (workout.exercises.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(workout.exercises.take(3).joinToString(" · ") { it.name.ifBlank { "?" } }
                            + if (workout.exercises.size > 3) " +${workout.exercises.size - 3}" else "",
                        color = c.textSec, fontSize = 12.sp)
                }
            }
            Text("›", color = c.textMuted, fontSize = 22.sp)
        }
        HorizontalDivider(color = c.border, thickness = 0.5.dp)
        TextButton(onClick = onUseAsTemplate, modifier = Modifier.fillMaxWidth()) {
            Text("↻  Utiliser comme base", color = c.accent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun StatChip(text: String, c: GainzThemeColors) {
    Box(Modifier.background(c.surfaceAlt, RoundedCornerShape(6.dp))
        .padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(text, color = c.textSec, fontSize = 11.sp)
    }
}
