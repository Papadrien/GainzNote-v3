package com.junade.gainznote.ui.circuit

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junade.gainznote.i18n.S
import com.junade.gainznote.model.CircuitExercise
import com.junade.gainznote.model.CircuitInputType
import com.junade.gainznote.model.WorkoutType
import com.junade.gainznote.repository.WorkoutRepository
import com.junade.gainznote.ui.BackHandler
import com.junade.gainznote.ui.ads.AdBanner
import com.junade.gainznote.ui.components.DurationWheelPicker
import com.junade.gainznote.ui.home.formatDisplayDateFull
import com.junade.gainznote.ui.theme.GainzThemeColors

@Composable
fun CircuitSetupScreen(
    repo: WorkoutRepository,
    darkTheme: Boolean,
    
    templateId: String? = null,
    resumeId: String? = null,
    skipSetup: Boolean = false,
    adFree: Boolean = false,
    onBack: () -> Unit,
    onStartWorkout: (String) -> Unit,
    onFinished: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val vm = remember(resumeId, templateId) {
        CircuitViewModel(repo, scope, templateId, resumeId)
    }
    val workout by vm.state.collectAsState()
    val c = GainzThemeColors(dark = darkTheme, type = WorkoutType.CIRCUIT)
    val cfg = workout.circuitConfig
    
    BackHandler(enabled = true) { onBack() }


    // Mode "skipSetup" : on route directement vers l'écran de séance après chargement template
    LaunchedEffect(workout.id, skipSetup, templateId) {
        if (skipSetup && templateId != null && workout.circuitExercises.isNotEmpty()) {
            onStartWorkout(workout.id)
        }
    }

    Column(Modifier.fillMaxSize().background(c.background).safeDrawingPadding()) {
        // TopBar
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
                    Text(S.workoutTypeCircuit, color = c.accent, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text(S.startedAt(formatDisplayDateFull(workout.startedAt)), color = c.textMuted, fontSize = 11.sp)
                }
            }
            Button(
                onClick = { onStartWorkout(workout.id) },
                colors = ButtonDefaults.buttonColors(containerColor = c.accent),
                shape = RoundedCornerShape(10.dp),
                enabled = workout.circuitExercises.isNotEmpty()
            ) {
                Text(
                    S.startCircuit,
                    color = if (darkTheme) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold
                )
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
            Spacer(Modifier.height(12.dp))

            // ── Config (tours + repos) ────────────────────────────────────
            Column(Modifier.fillMaxWidth()
                .border(1.dp, c.border, RoundedCornerShape(12.dp))
                .background(c.surface, RoundedCornerShape(12.dp))
                .padding(14.dp)) {
                Text(S.circuitConfig, color = c.accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))

                // Nb tours
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(S.totalRounds, color = c.text, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { vm.updateTotalRounds((cfg?.totalRounds ?: 3) - 1) },
                        modifier = Modifier.semantics { contentDescription = S.decreaseRoundsDesc }
                    ) {
                        Text("−", color = c.accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "${cfg?.totalRounds ?: 3}",
                        color = c.text, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.widthIn(min = 32.dp)
                    )
                    IconButton(
                        onClick = { vm.updateTotalRounds((cfg?.totalRounds ?: 3) + 1) },
                        modifier = Modifier.semantics { contentDescription = S.increaseRoundsDesc }
                    ) {
                        Text("+", color = c.accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = c.border, thickness = 0.5.dp)
                Spacer(Modifier.height(8.dp))

                // Repos entre exos
                Text(S.restBetweenExercises, color = c.textSec, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                DurationWheelPicker(
                    totalSeconds = cfg?.restBetweenExercisesSeconds ?: 0L,
                    onChange = { vm.updateRestBetweenExercises(it) },
                    c = c, showHours = false
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = c.border, thickness = 0.5.dp)
                Spacer(Modifier.height(8.dp))

                // Repos entre tours
                Text(S.restBetweenRounds, color = c.textSec, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                DurationWheelPicker(
                    totalSeconds = cfg?.restBetweenRoundsSeconds ?: 0L,
                    onChange = { vm.updateRestBetweenRounds(it) },
                    c = c, showHours = false
                )
            }
            Spacer(Modifier.height(16.dp))

            // Liste des exercices circuit
            workout.circuitExercises.forEach { ex ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        CircuitExerciseSetupCard(
                            exercise = ex,
                            c = c,
                            onNameChange = { vm.updateExerciseName(ex.id, it) },
                            onInputTypeChange = { vm.updateInputType(ex.id, it) },
                            onRemove = { vm.removeExercise(ex.id) },
                            onMoveUp = { vm.moveExerciseUp(ex.id) }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            Surface(
                onClick = { vm.addExercise() },
                shape = RoundedCornerShape(12.dp),
                color = c.accentDim,
                border = BorderStroke(1.dp, c.accent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(S.addCircuitExercise, color = c.accent, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
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


}

@Composable
private fun CircuitExerciseSetupCard(
    exercise: CircuitExercise,
    c: GainzThemeColors,
    onNameChange: (String) -> Unit,
    onInputTypeChange: (CircuitInputType) -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit
) {
    Column(Modifier.fillMaxWidth()
        .border(1.dp, c.border, RoundedCornerShape(12.dp))
        .background(c.surface, RoundedCornerShape(12.dp))
        .padding(12.dp)) {
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
                        if (exercise.name.isEmpty()) Text(S.circuitExerciseNamePlaceholder, color = c.textMuted, fontSize = 16.sp)
                        inner()
                    }
                }
            )
            IconButton(
                onClick = onMoveUp,
                modifier = Modifier.size(32.dp).semantics { contentDescription = S.moveUpDesc }
            ) {
                Text("↑", color = c.accent, fontSize = 16.sp)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = S.removeExerciseDesc, tint = c.danger)
            }
        }
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = c.border, thickness = 0.5.dp)
        Spacer(Modifier.height(8.dp))
        Text(S.inputType, color = c.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            InputTypeChip(CircuitInputType.REPS, S.inputTypeReps, exercise.inputType, c, onInputTypeChange)
            InputTypeChip(CircuitInputType.REPS_WEIGHT, S.inputTypeRepsWeight, exercise.inputType, c, onInputTypeChange)
            InputTypeChip(CircuitInputType.DURATION, S.inputTypeDuration, exercise.inputType, c, onInputTypeChange)
        }
    }
}

@Composable
private fun InputTypeChip(
    value: CircuitInputType,
    label: String,
    current: CircuitInputType,
    c: GainzThemeColors,
    onPick: (CircuitInputType) -> Unit
) {
    val selected = value == current
    val bg = if (selected) c.accent else Color.Transparent
    val tc = if (selected) {
        if (c.dark) Color.Black else Color.White
    } else c.textMuted
    val fw = if (selected) FontWeight.Bold else FontWeight.Medium
    val modifier = if (selected) {
        Modifier.background(bg, RoundedCornerShape(8.dp))
    } else {
        Modifier.background(Color.Transparent, RoundedCornerShape(8.dp))
    }

    Box(
        modifier = modifier
            .clickable { onPick(value) }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = tc, fontSize = 12.sp, fontWeight = fw)
    }
}