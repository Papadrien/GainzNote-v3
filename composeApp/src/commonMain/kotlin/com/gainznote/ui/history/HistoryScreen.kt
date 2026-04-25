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
import com.gainznote.model.WorkoutType
import com.gainznote.repository.WorkoutRepository
import com.gainznote.ui.home.formatDisplayDate
import com.gainznote.i18n.S
import com.gainznote.ui.theme.GainzThemeColors
import com.gainznote.ui.theme.accentPairFor

@Composable
fun HistoryScreen(
    repo: WorkoutRepository,
    darkTheme: Boolean,
    onBack: () -> Unit,
    onOpenDetail: (String) -> Unit,
    onUseAsTemplate: (String) -> Unit
) {
    val c = GainzThemeColors(darkTheme)
    var workouts by remember { mutableStateOf<List<Workout>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        workouts = repo.getFinishedWorkouts()
        loading = false
    }

    Column(Modifier.fillMaxSize().background(c.background).safeDrawingPadding()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clickable { onBack() }, contentAlignment = Alignment.Center) {
                Text("←", color = c.accent, fontSize = 22.sp)
            }
            Spacer(Modifier.width(8.dp))
            Text(S.history, color = c.text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider(color = c.border, thickness = 0.5.dp)

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = c.accent)
            }
        } else if (workouts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏋", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(S.noWorkoutRecorded, color = c.textMuted, fontSize = 15.sp)
                }
            }
        } else {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                workouts.forEach { w ->
                    HistoryCard(workout = w, c = c, darkTheme = darkTheme,
                        onClick = { onOpenDetail(w.id) },
                        onUseAsTemplate = { onUseAsTemplate(w.id) })
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun HistoryCard(workout: Workout, c: GainzThemeColors, darkTheme: Boolean, onClick: () -> Unit, onUseAsTemplate: () -> Unit) {
    val (typeAccent, typeAccentDim) = accentPairFor(workout.type, darkTheme)
    Column(Modifier.fillMaxWidth()
        .border(1.dp, c.border, RoundedCornerShape(14.dp))
        .background(c.surface, RoundedCornerShape(14.dp))) {
        Row(Modifier.fillMaxWidth().clickable { onClick() }.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WorkoutTypeBadge(workout.type, typeAccent, typeAccentDim)
                    Text(workout.title.ifBlank { S.untitled }, color = c.text,
                        fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(4.dp))
                Text(formatDisplayDate(workout.startedAt), color = c.textMuted, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                WorkoutStatsRow(workout, c)
                WorkoutPreview(workout, c)
            }
            Text("›", color = c.textMuted, fontSize = 22.sp)
        }
        HorizontalDivider(color = c.border, thickness = 0.5.dp)
        TextButton(onClick = onUseAsTemplate, modifier = Modifier.fillMaxWidth()) {
            Text(S.useAsTemplate, color = typeAccent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun WorkoutTypeBadge(type: WorkoutType, accent: androidx.compose.ui.graphics.Color, accentDim: androidx.compose.ui.graphics.Color) {
    val label = when (type) {
        WorkoutType.MUSCULATION -> S.workoutTypeMusculation
        WorkoutType.CARDIO      -> S.workoutTypeCardio
        WorkoutType.CIRCUIT     -> S.workoutTypeCircuit
    }
    Box(Modifier.background(accentDim, RoundedCornerShape(6.dp))
        .padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(label, color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun WorkoutStatsRow(workout: Workout, c: GainzThemeColors) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        when (workout.type) {
            WorkoutType.MUSCULATION -> {
                StatChip(S.exercisesCount(workout.exercises.size), c)
                StatChip(S.setsCount(workout.exercises.sumOf { it.sets.size }), c)
            }
            WorkoutType.CARDIO -> {
                StatChip(S.cardioExercisesCount(workout.cardioExercises.size), c)
                val totalSegs = workout.cardioExercises.sumOf { it.segments.size }
                StatChip(S.segmentsCount(totalSegs), c)
            }
            WorkoutType.CIRCUIT -> {
                val rounds = workout.circuitConfig?.totalRounds ?: 0
                StatChip(S.exercisesCount(workout.circuitExercises.size), c)
                StatChip(S.roundsCount(rounds), c)
            }
        }
    }
}

@Composable
fun WorkoutPreview(workout: Workout, c: GainzThemeColors) {
    val names: List<String> = when (workout.type) {
        WorkoutType.MUSCULATION -> workout.exercises.map { it.name.ifBlank { "?" } }
        WorkoutType.CARDIO      -> workout.cardioExercises.map { it.name.ifBlank { "?" } }
        WorkoutType.CIRCUIT     -> workout.circuitExercises.map { it.name.ifBlank { "?" } }
    }
    if (names.isNotEmpty()) {
        Spacer(Modifier.height(6.dp))
        Text(names.take(3).joinToString(" · ")
                + if (names.size > 3) " +${names.size - 3}" else "",
            color = c.textSec, fontSize = 12.sp)
    }
}

@Composable
fun StatChip(text: String, c: GainzThemeColors) {
    Box(Modifier.background(c.surfaceAlt, RoundedCornerShape(6.dp))
        .padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(text, color = c.textSec, fontSize = 11.sp)
    }
}
