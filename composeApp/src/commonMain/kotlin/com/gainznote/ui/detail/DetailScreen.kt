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
import com.gainznote.model.CardioExercise
import com.gainznote.model.CircuitExercise
import com.gainznote.model.CircuitInputType
import com.gainznote.model.Exercise
import com.gainznote.model.Workout
import com.gainznote.model.WorkoutType
import com.gainznote.repository.WorkoutRepository
import com.gainznote.ui.history.StatChip
import com.gainznote.ui.history.WorkoutStatsRow
import com.gainznote.ui.history.WorkoutTypeBadge
import com.gainznote.ui.home.formatDisplayDate
import com.gainznote.i18n.S
import com.gainznote.ui.theme.GainzThemeColors
import kotlinx.coroutines.launch

@Composable
fun DetailScreen(
    repo: WorkoutRepository,
    darkTheme: Boolean,
    
    workoutId: String,
    onBack: () -> Unit,
    onUseAsTemplate: (String) -> Unit, // Add this line

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
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = S.delete, tint = c.danger)
                }
            }
        }

        val w = workout
        if (w == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = c.accent)
            }
        } else {
            val typeC = GainzThemeColors(darkTheme, type = w.type)
            val typeAccent = typeC.accent
            val typeAccentDim = typeC.accentDim
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WorkoutTypeBadge(w.type, typeAccent, typeAccentDim)
                }
                Spacer(Modifier.height(8.dp))
                Text(w.title.ifBlank { S.untitled }, color = c.text,
                    fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp)
                Spacer(Modifier.height(6.dp))
                Text(formatDisplayDate(w.startedAt), color = c.textMuted, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                WorkoutStatsRow(w, c)
                if (w.notes.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Box(Modifier.fillMaxWidth().background(c.surfaceAlt, RoundedCornerShape(10.dp)).padding(12.dp)) {
                        Text(w.notes, color = c.textSec, fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
                when (w.type) {
                    WorkoutType.MUSCULATION -> {
                        w.exercises.forEach { ex ->
                            ExerciseDetailCard(ex, c)
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                    WorkoutType.CARDIO -> {
                        w.cardioExercises.forEach { ex ->
                            CardioExerciseDetailCard(ex, c, typeAccent, typeAccentDim)
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                    WorkoutType.CIRCUIT -> {
                        val cfg = w.circuitConfig
                        if (cfg != null) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatChip(S.roundsCount(cfg.totalRounds), c)
                                if (cfg.restBetweenExercisesSeconds > 0) {
                                    StatChip("⏱ ${formatShortSec(cfg.restBetweenExercisesSeconds)}", c)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }
                        w.circuitExercises.forEach { ex ->
                            CircuitExerciseDetailCard(ex, cfg?.totalRounds ?: 0, c, typeAccent, typeAccentDim)
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
                Button(onClick = { onUseAsTemplate(w.id) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = typeAccent)) {
                    Text(S.useAsTemplate, color = if (darkTheme) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                if (w.type == WorkoutType.CIRCUIT) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { onReplayCircuit(w.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(S.replayCircuit, color = typeAccent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showDeleteDialog && workout != null) {
        AlertDialog(onDismissRequest = { showDeleteDialog = false },
            containerColor = c.surface,
            title = { Text(S.deleteConfirmTitle, color = c.text) },
            text = { Text(S.deleteConfirmBody, color = c.textSec) },
            confirmButton = {
                Button(onClick = { scope.launch { repo.deleteWorkout(workoutId); onDeleted() } },
                    colors = ButtonDefaults.buttonColors(containerColor = c.danger)) {
                    Text(S.delete, color = Color.White)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text(S.cancel, color = c.textMuted) } })
    }
}

@Composable
fun ExerciseDetailCard(exercise: Exercise, c: GainzThemeColors) {
    val isSuperset = exercise.supersetWith != null
    Column(Modifier.fillMaxWidth()
        .border(if (isSuperset) 2.dp else 1.dp,
            if (isSuperset) c.superset else c.border, RoundedCornerShape(12.dp))
        .background(c.surface, RoundedCornerShape(12.dp))) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (isSuperset) {
                Box(Modifier.background(c.supersetDim, RoundedCornerShape(5.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)) {
                    Text("SS", color = c.superset, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
            }
            Text(exercise.name.ifBlank { S.unnamedExercise }, color = c.text,
                fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        HorizontalDivider(color = if (isSuperset) c.superset.copy(alpha = 0.3f) else c.border, thickness = 0.5.dp)
        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text("#", color = c.textMuted, fontSize = 11.sp, modifier = Modifier.width(28.dp))
            Text(S.weight, color = c.textMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
            Text(S.reps, color = c.textMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
            Text(S.notes, color = c.textMuted, fontSize = 11.sp, modifier = Modifier.weight(2f))
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

@Composable
fun CardioExerciseDetailCard(
    exercise: CardioExercise,
    c: GainzThemeColors,
    typeAccent: Color,
    typeAccentDim: Color
) {
    Column(Modifier.fillMaxWidth()
        .border(1.dp, c.border, RoundedCornerShape(12.dp))
        .background(c.surface, RoundedCornerShape(12.dp))) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(exercise.name.ifBlank { S.unnamedExercise }, color = c.text,
                fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        HorizontalDivider(color = c.border, thickness = 0.5.dp)
        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text("#", color = c.textMuted, fontSize = 11.sp, modifier = Modifier.width(28.dp))
            Text(S.intensity, color = c.textMuted, fontSize = 11.sp, modifier = Modifier.weight(2f))
            Text(S.duration, color = c.textMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
        }
        exercise.segments.forEachIndexed { i, seg ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 5.dp)) {
                Text("${i+1}", color = c.textMuted, fontSize = 12.sp, modifier = Modifier.width(28.dp))
                Text(seg.intensity.ifBlank { "—" }, color = c.text, fontSize = 14.sp,
                    fontWeight = FontWeight.Medium, modifier = Modifier.weight(2f))
                Text(formatSegmentDuration(seg.durationSeconds), color = c.text, fontSize = 14.sp,
                    fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

fun formatSegmentDuration(totalSec: Long): String {
    val h = totalSec / 3600L
    val m = (totalSec % 3600L) / 60L
    val s = totalSec % 60L
    return when {
        h > 0 -> "${h}h ${m.toString().padStart(2,'0')}m ${s.toString().padStart(2,'0')}s"
        m > 0 -> "${m}m ${s.toString().padStart(2,'0')}s"
        else  -> "${s}s"
    }
}


@Composable
fun CircuitExerciseDetailCard(
    exercise: CircuitExercise,
    totalRounds: Int,
    c: GainzThemeColors,
    typeAccent: Color,
    typeAccentDim: Color
) {
    Column(Modifier.fillMaxWidth()
        .border(1.dp, c.border, RoundedCornerShape(12.dp))
        .background(c.surface, RoundedCornerShape(12.dp))) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(exercise.name.ifBlank { S.unnamedExercise }, color = c.text,
                fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Box(Modifier.background(typeAccentDim, RoundedCornerShape(5.dp))
                .padding(horizontal = 5.dp, vertical = 2.dp)) {
                val label = when (exercise.inputType) {
                    CircuitInputType.REPS -> S.inputTypeReps
                    CircuitInputType.REPS_WEIGHT -> S.inputTypeRepsWeight
                    CircuitInputType.DURATION -> S.inputTypeDuration
                }
                Text(label, color = typeAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
        HorizontalDivider(color = c.border, thickness = 0.5.dp)
        // Tableau perfs par tour
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            (1..totalRounds).forEach { round ->
                val perf = exercise.performances.firstOrNull { it.roundNumber == round }
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("T$round", color = c.textMuted, fontSize = 11.sp, modifier = Modifier.width(32.dp))
                    if (perf == null) {
                        Text("—", color = c.textMuted, fontSize = 13.sp)
                    } else {
                        val desc: String = when (exercise.inputType) {
                            CircuitInputType.REPS -> "${perf.reps ?: "?"} ${S.repsShort}"
                            CircuitInputType.REPS_WEIGHT -> {
                                val w = perf.weightKg?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: "?"
                                "${perf.reps ?: "?"} ${S.repsShort} × ${w} ${S.kgShort}"
                            }
                            CircuitInputType.DURATION -> formatShortSec(perf.durationSeconds ?: 0L)
                        }
                        Text(desc, color = c.text, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp))
                        if (perf.notes.isNotBlank()) {
                            Text(perf.notes, color = c.textSec, fontSize = 12.sp, maxLines = 1,
                                modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

fun formatShortSec(sec: Long): String {
    val h = sec / 3600
    val m = (sec % 3600) / 60
    val s = sec % 60
    return when {
        h > 0 -> "${h}h${m.toString().padStart(2,'0')}"
        m > 0 -> "${m}m${s.toString().padStart(2,'0')}"
        else -> "${s}s"
    }
}
