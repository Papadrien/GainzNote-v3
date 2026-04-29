package com.gainznote.ui.cardio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gainznote.i18n.S
import com.gainznote.model.CardioExercise
import com.gainznote.model.CardioSegment
import com.gainznote.model.WorkoutType
import com.gainznote.repository.WorkoutRepository
import com.gainznote.ui.BackHandler
import com.gainznote.ui.ads.AdBanner
import com.gainznote.ui.components.DurationWheelPicker
import com.gainznote.ui.home.formatDisplayDateFull
import com.gainznote.ui.theme.GainzThemeColors

@Composable
fun CardioSetupScreen(
    repo: WorkoutRepository,
    darkTheme: Boolean,
    
    templateId: String? = null,
    resumeId: String? = null,
    adFree: Boolean = false,
    onBack: () -> Unit,
    onFinished: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val vm = remember(resumeId, templateId) {
        CardioViewModel(repo, scope, templateId, resumeId)
    }
    val workout by vm.state.collectAsState()
    val c = GainzThemeColors(dark = darkTheme, type = WorkoutType.CARDIO)
    var showFinishDialog by remember { mutableStateOf(false) }
    
    BackHandler(enabled = true) { onBack() }


    Column(Modifier.fillMaxSize().background(c.background).safeDrawingPadding()) {
        // ── TopBar ─────────────────────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp)
                        .clickable(onClickLabel = S.backDesc) { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("←", color = c.accent, fontSize = 22.sp)
                }
                Spacer(Modifier.width(4.dp))
                Column {
                    Text(S.workoutTypeCardio, color = c.accent, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text(S.startedAt(formatDisplayDateFull(workout.startedAt)), color = c.textMuted, fontSize = 11.sp)
                }
            }
            Button(
                onClick = { showFinishDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = c.accent),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(S.finish, color = if (darkTheme) Color.Black else Color.White, fontWeight = FontWeight.Bold)
            }
        }
        HorizontalDivider(color = c.border, thickness = 0.5.dp)

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            // Titre
            BasicTextField(
                value = workout.title,
                onValueChange = { vm.updateTitle(it) },
                textStyle = TextStyle(color = c.text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                cursorBrush = SolidColor(c.accent),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                decorationBox = { inner ->
                    Box(Modifier.fillMaxWidth().background(c.surface, RoundedCornerShape(10.dp))
                        .border(1.dp, c.border, RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 14.dp)) {
                        if (workout.title.isEmpty()) Text(S.workoutTitlePlaceholder, color = c.textMuted, fontSize = 16.sp)
                        inner()
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
            // Notes
            BasicTextField(
                value = workout.notes,
                onValueChange = { vm.updateNotes(it) },
                textStyle = TextStyle(color = c.textSec, fontSize = 14.sp),
                cursorBrush = SolidColor(c.accent),
                decorationBox = { inner ->
                    Box(Modifier.fillMaxWidth().background(c.surface, RoundedCornerShape(10.dp))
                        .border(1.dp, c.border, RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)) {
                        if (workout.notes.isEmpty()) Text(S.generalNotesPlaceholder, color = c.textMuted, fontSize = 14.sp)
                        inner()
                    }
                }
            )
            Spacer(Modifier.height(16.dp))

            // Liste des exercices cardio
            workout.cardioExercises.forEach { ex ->
                CardioExerciseCard(
                    exercise = ex,
                    c = c,
                    onNameChange = { vm.updateExerciseName(ex.id, it) },
                    onRemoveExercise = { vm.removeExercise(ex.id) },
                    onAddSegment = { vm.addSegment(ex.id) },
                    onRemoveSegment = { segId -> vm.removeSegment(ex.id, segId) },
                    onIntensityChange = { segId, v -> vm.updateSegmentIntensity(ex.id, segId, v) },
                    onDurationChange = { segId, v -> vm.updateSegmentDuration(ex.id, segId, v) }
                )
                Spacer(Modifier.height(12.dp))
            }

            // Bouton ajouter exercice
            Surface(
                onClick = { vm.addExercise() },
                shape = RoundedCornerShape(12.dp),
                color = c.accentDim,
                border = BorderStroke(1.dp, c.accent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(S.addCardioExercise, color = c.accent, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(24.dp))

            if (!adFree) {
                AdBanner(Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showFinishDialog) {
        val segCount = workout.cardioExercises.sumOf { it.segments.size }
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            containerColor = c.surface,
            title = { Text(S.finishWorkoutTitle, color = c.text) },
            text = { Text(S.finishCardioBody(workout.cardioExercises.size, segCount), color = c.textSec) },
            confirmButton = {
                Button(
                    onClick = { showFinishDialog = false; vm.finish(onFinished) },
                    colors = ButtonDefaults.buttonColors(containerColor = c.accent)
                ) {
                    Text(S.finishConfirm, color = if (darkTheme) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text(S.continueWorkout, color = c.textMuted)
                }
            }
        )
    }


}

@Composable
private fun CardioExerciseCard(
    exercise: CardioExercise,
    c: GainzThemeColors,
    onNameChange: (String) -> Unit,
    onRemoveExercise: () -> Unit,
    onAddSegment: () -> Unit,
    onRemoveSegment: (String) -> Unit,
    onIntensityChange: (String, String) -> Unit,
    onDurationChange: (String, Long) -> Unit
) {
    Column(
        Modifier.fillMaxWidth()
            .border(1.dp, c.border, RoundedCornerShape(14.dp))
            .background(c.surface, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = exercise.name,
                onValueChange = onNameChange,
                textStyle = TextStyle(color = c.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                cursorBrush = SolidColor(c.accent),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box(Modifier.padding(vertical = 4.dp)) {
                        if (exercise.name.isEmpty()) Text(S.cardioExerciseNamePlaceholder, color = c.textMuted, fontSize = 16.sp)
                        inner()
                    }
                }
            )
            IconButton(onClick = onRemoveExercise) {
                Icon(Icons.Default.Delete, contentDescription = S.removeExerciseDesc, tint = c.danger)
            }
        }
        HorizontalDivider(color = c.border, thickness = 0.5.dp)
        Spacer(Modifier.height(8.dp))
        exercise.segments.forEachIndexed { idx, seg ->
            androidx.compose.animation.AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    SegmentRow(
                        index = idx + 1,
                        segment = seg,
                        canRemove = exercise.segments.size > 1,
                        c = c,
                        onIntensityChange = { onIntensityChange(seg.id, it) },
                        onDurationChange = { onDurationChange(seg.id, it) },
                        onRemove = { onRemoveSegment(seg.id) }
                    )
                    if (idx < exercise.segments.size - 1) {
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = c.border, thickness = 0.5.dp)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        TextButton(onClick = onAddSegment, modifier = Modifier.fillMaxWidth()) {
            Text(S.addSegment, color = c.accent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SegmentRow(
    index: Int,
    segment: CardioSegment,
    canRemove: Boolean,
    c: GainzThemeColors,
    onIntensityChange: (String) -> Unit,
    onDurationChange: (Long) -> Unit,
    onRemove: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(S.segmentLabel(index), color = c.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            if (canRemove) {
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = S.removeSegmentDesc, tint = c.danger)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        BasicTextField(
            value = segment.intensity,
            onValueChange = onIntensityChange,
            textStyle = TextStyle(color = c.text, fontSize = 14.sp),
            cursorBrush = SolidColor(c.accent),
            decorationBox = { inner ->
                Box(Modifier.fillMaxWidth()
                    .background(c.surfaceAlt, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)) {
                    if (segment.intensity.isEmpty()) Text(S.intensityPlaceholder, color = c.textMuted, fontSize = 14.sp)
                    inner()
                }
            }
        )
        Spacer(Modifier.height(8.dp))
        DurationWheelPicker(
            totalSeconds = segment.durationSeconds,
            onChange = onDurationChange,
            c = c,
            showHours = true
        )
    }
}
