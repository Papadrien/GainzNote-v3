package com.gainznote.ui.detail

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.gainznote.model.Exercise
import com.gainznote.model.Workout
import com.gainznote.repository.WorkoutRepository
import com.gainznote.ui.history.StatChip
import com.gainznote.ui.home.formatDisplayDate
import com.gainznote.ui.theme.GainzThemeColors
import kotlinx.coroutines.launch

@Composable
fun DetailScreen(
    repo: WorkoutRepository,
    darkTheme: Boolean,
    workoutId: String,
    onBack: () -> Unit,
    onUseAsTemplate: (String) -> Unit,
    onDeleted: () -> Unit
) {
    val c = GainzThemeColors(darkTheme)
    val scope = rememberCoroutineScope()
    var workout by remember { mutableStateOf<Workout?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(workoutId) { workout = repo.getWorkoutById(workoutId) }

    Column(Modifier.fillMaxSize().background(c.background).safeDrawingPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clickable { onBack() }, contentAlignment = Alignment.Center) {
                Text("←", color = c.accent, fontSize = 22.sp)
            }
            if (workout != null) {
                // Icône poubelle Material
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = c.danger)
                }
            }
        }

        val w = workout
        if (w == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = c.accent)
            }
        } else {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                Text(w.title.ifBlank { "Sans titre" }, color = c.text,
                    fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp)
                Spacer(Modifier.height(6.dp))
                Text(formatDisplayDate(w.startedAt), color = c.textMuted, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatChip("${w.exercises.size} exercices", c)
                    StatChip("${w.exercises.sumOf { it.sets.size }} séries", c)
                }
                if (w.notes.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Box(Modifier.fillMaxWidth().background(c.surfaceAlt, RoundedCornerShape(10.dp)).padding(12.dp)) {
                        Text(w.notes, color = c.textSec, fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
                w.exercises.forEach { ex ->
                    ExerciseDetailCard(ex, c)
                    Spacer(Modifier.height(12.dp))
                }
                Button(onClick = { onUseAsTemplate(w.id) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = c.accent)) {
                    Text("↻  Utiliser comme base", color = if (darkTheme) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showDeleteDialog && workout != null) {
        AlertDialog(onDismissRequest = { showDeleteDialog = false },
            containerColor = c.surface,
            title = { Text("Supprimer ?", color = c.text) },
            text = { Text("Action irréversible.", color = c.textSec) },
            confirmButton = {
                Button(onClick = { scope.launch { repo.deleteWorkout(workoutId); onDeleted() } },
                    colors = ButtonDefaults.buttonColors(containerColor = c.danger)) {
                    Text("Supprimer", color = Color.White)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Annuler", color = c.textMuted) } })
    }
}

@Composable
fun ExerciseDetailCard(exercise: Exercise, c: GainzThemeColors) {
    Column(Modifier.fillMaxWidth()
        .border(1.dp, c.border, RoundedCornerShape(12.dp))
        .background(c.surface, RoundedCornerShape(12.dp))) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (exercise.supersetWith != null) {
                Box(Modifier.background(c.supersetDim, RoundedCornerShape(5.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)) {
                    Text("SS", color = c.superset, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
            }
            Text(exercise.name.ifBlank { "Exercice sans nom" }, color = c.text,
                fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        HorizontalDivider(color = c.border, thickness = 0.5.dp)
        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text("#", color = c.textMuted, fontSize = 11.sp, modifier = Modifier.width(28.dp))
            Text("Poids", color = c.textMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
            Text("Reps", color = c.textMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
            Text("Notes", color = c.textMuted, fontSize = 11.sp, modifier = Modifier.weight(2f))
        }
        exercise.sets.forEachIndexed { i, s ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 5.dp)) {
                Text("${i+1}", color = c.textMuted, fontSize = 12.sp, modifier = Modifier.width(28.dp))
                Text(s.weightKg?.let { "${if (it == it.toLong().toDouble()) it.toLong() else it} kg" } ?: "—",
                    color = c.text, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Text(s.reps?.toString() ?: "—", color = c.text, fontSize = 14.sp,
                    fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Text(s.notes, color = c.textSec, fontSize = 13.sp, modifier = Modifier.weight(2f))
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
