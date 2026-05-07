package com.junade.gainznote.ui.history

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.junade.gainznote.model.Workout
import com.junade.gainznote.model.WorkoutType
import com.junade.gainznote.repository.WorkoutRepository
import com.junade.gainznote.ui.home.formatDisplayDate
import com.junade.gainznote.i18n.S
import com.junade.gainznote.ui.theme.GainzThemeColors

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
    val typeC = GainzThemeColors(c.dark, type = workout.type)
    val typeAccent = typeC.accent
    val typeAccentDim = typeC.accentDim
    val histBorder = if (c.dark) Color(0xFF333333) else Color(0xFFDDDDDD)
    Column(Modifier.fillMaxWidth()
        .border(1.dp, histBorder, RoundedCornerShape(14.dp))
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
                WorkoutStatsRow(workout, typeC)
                WorkoutPreview(workout, typeC)
            }
            Text("›", color = if (c.dark) Color(0xFF666666) else Color(0xFFAAAAAA), fontSize = 22.sp)
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
    val chipBg = if (c.dark) Color(0xFF2C2C2C) else Color(0xFFF0F0F0)
    val chipText = if (c.dark) Color(0xFFA0A0A0) else Color(0xFF666666)
    Box(Modifier.background(chipBg, RoundedCornerShape(6.dp))
        .padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(text, color = chipText, fontSize = 11.sp)
    }
}
