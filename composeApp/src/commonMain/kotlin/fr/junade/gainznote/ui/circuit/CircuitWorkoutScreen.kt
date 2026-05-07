package fr.junade.gainznote.ui.circuit

import androidx.compose.animation.*
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.junade.gainznote.i18n.S
import fr.junade.gainznote.model.CircuitExercise
import fr.junade.gainznote.model.CircuitInputType
import fr.junade.gainznote.model.CircuitPerformance
import fr.junade.gainznote.model.WorkoutType
import fr.junade.gainznote.repository.WorkoutRepository
import fr.junade.gainznote.ui.BackHandler
import fr.junade.gainznote.ui.components.DurationWheelPicker
import fr.junade.gainznote.ui.theme.GainzThemeColors

import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import fr.junade.gainznote.ui.ads.AdBanner
import fr.junade.gainznote.ui.components.FloatingTimer


@Composable
fun CircuitWorkoutScreen(
    repo: WorkoutRepository,
    darkTheme: Boolean,
    
    workoutId: String,
    adFree: Boolean = false,
    chronoNotifEnabled: Boolean = false,
    onChronoStart: (Long) -> Unit = {},
    onChronoStop: () -> Unit = {},
    onBack: () -> Unit,
    onFinished: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val vm = remember(workoutId) {
        CircuitViewModel(repo, scope, templateId = null, resumeId = workoutId)
    }
    val workout by vm.state.collectAsState()
    val c = GainzThemeColors(dark = darkTheme, type = WorkoutType.CIRCUIT)

    val exercises = workout.circuitExercises
    val totalRounds = workout.circuitConfig?.totalRounds ?: 1
    val restExoSec = workout.circuitConfig?.restBetweenExercisesSeconds ?: 0L
    val restRoundSec = workout.circuitConfig?.restBetweenRoundsSeconds ?: 0L

    var currentRound by remember { mutableStateOf(1) }
    var currentExIdx by remember { mutableStateOf(0) }
    var showFinishDialog by remember { mutableStateOf(false) }
    
    BackHandler(enabled = true) { onBack() }

    var editing by remember { mutableStateOf<Pair<String, Int>?>(null) } // exId, round (null = pas d'edit)

    // Saisie courante
    
    val currentExo = exercises.getOrNull(currentExIdx)
    val pastPerf = currentExo?.performances?.find { it.roundNumber == 1 }

    var reps by remember(currentRound, currentExIdx) {
        mutableStateOf(
            if (currentRound > 1 && currentExo?.inputType == CircuitInputType.REPS) {
                pastPerf?.reps?.toString() ?: ""
            } else ""
        )
    }
    var weight by remember(currentRound, currentExIdx) {
        mutableStateOf(
            if (currentRound > 1) {
                pastPerf?.weightKg?.let { it.toString().replace(".0", "") } ?: ""
            } else {
                currentExo?.referenceWeightKg?.let { it.toString().replace(".0", "") } ?: ""
            }
        )
    }
    var duration by remember(currentRound, currentExIdx) {
        mutableStateOf(
            if (currentRound > 1) {
                pastPerf?.durationSeconds ?: 0L
            } else {
                currentExo?.referenceDurationSeconds ?: 0L
            }
        )
    }
    var notes by remember(currentRound, currentExIdx) { mutableStateOf("") }

    // Countdown de repos (local display seulement — chrono notif déléguée)
    var restEndMs by remember { mutableStateOf<Long?>(null) }
    var restDisplay by remember { mutableStateOf("00:00") }
    LaunchedEffect(restEndMs) {
        while (restEndMs != null) {
            val remain = restEndMs!! - Clock.System.now().toEpochMilliseconds()
            if (remain <= 0) {
                restEndMs = null
                restDisplay = "00:00"
                if (chronoNotifEnabled) onChronoStop()
                break
            }
            val sec = (remain + 999) / 1000
            restDisplay = "${(sec / 60).toString().padStart(2,'0')}:${(sec % 60).toString().padStart(2,'0')}"
            delay(1000)
        }
    }

    

        Box(Modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize().background(c.background)) {
        val isLandscape = maxWidth > maxHeight
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
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
                    Text(S.workoutTypeCircuit, color = c.accent, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text(S.roundProgress(currentRound, totalRounds), color = c.textMuted, fontSize = 11.sp)
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

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(10.dp))
            if (currentExo != null) {
                AnimatedContent(
                    targetState = currentRound to currentExIdx,
                    transitionSpec = {
                        (fadeIn() + slideInHorizontally { it / 4 }) togetherWith
                            (fadeOut() + slideOutHorizontally { -it / 4 })
                    },
                    label = "activeExerciseTransition"
                ) { _ ->
                ActiveExerciseCard(
                    exercise = currentExo,
                    exerciseIdx = currentExIdx,
                    totalExercises = exercises.size,
                    c = c,
                    reps = reps, onRepsChange = { reps = it },
                    weight = weight, onWeightChange = { weight = it },
                    duration = duration, onDurationChange = { duration = it },
                    notes = notes, onNotesChange = { notes = it },
                    onStartTimer = { durationSec ->
                        val end = Clock.System.now().toEpochMilliseconds() + durationSec * 1000
                        restEndMs = end
                        if (chronoNotifEnabled) onChronoStart(end)
                    },
                    onValidateNext = {
                        // Enregistrer la perf
                        vm.upsertPerformance(
                            exId = currentExo.id,
                            roundNumber = currentRound,
                            reps = reps.toIntOrNull(),
                            weightKg = weight.replace(',', '.').toDoubleOrNull(),
                            durationSeconds = if (currentExo.inputType == CircuitInputType.DURATION) duration else null,
                            notes = notes
                        )
                        // Reset saisie
                        reps = ""; weight = ""; duration = 0L; notes = ""
                        // Avancer
                        val lastExo = currentExIdx == exercises.size - 1
                        val lastRound = currentRound == totalRounds
                        if (lastExo && lastRound) {
                            showFinishDialog = true
                        } else if (lastExo) {
                            currentExIdx = 0
                            currentRound += 1
                            if (restRoundSec > 0) {
                                val end = Clock.System.now().toEpochMilliseconds() + restRoundSec * 1000
                                restEndMs = end
                                if (chronoNotifEnabled) onChronoStart(end)
                            }
                        } else {
                            currentExIdx += 1
                            if (restExoSec > 0) {
                                val end = Clock.System.now().toEpochMilliseconds() + restExoSec * 1000
                                restEndMs = end
                                if (chronoNotifEnabled) onChronoStart(end)
                            }
                        }
                    }
                )
                }
            }
            Spacer(Modifier.height(16.dp))

            // Tableau récap (exos x tours)
            if (exercises.isNotEmpty()) {
                RecapTable(
                    exercises = exercises,
                    totalRounds = totalRounds,
                    currentRound = currentRound,
                    currentExIdx = currentExIdx,
                    c = c,
                    onTapCell = { exId, round -> editing = exId to round }
                )
            }

                            Spacer(Modifier.height(32.dp))
            }
        }

        // FloatingTimer en overlay (au-dessus du contenu)
        FloatingTimer(
            visible = restEndMs != null,
            timerDisplay = restDisplay,
            onClose = {
                restEndMs = null
                if (chronoNotifEnabled) onChronoStop()
            },
            c = c,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .safeDrawingPadding()
                .padding(top = 70.dp)
        )

        // Bannière pub en bas (overlay, hors scroll)
        if (!adFree) {
            AdBanner(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )
        }
    }
    }

    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            containerColor = c.surface,
            title = { Text(S.finishWorkoutTitle, color = c.text) },
            text = {
                val totalPerfs = exercises.sumOf { it.performances.size }
                Text(S.finishCircuitBody(exercises.size, totalPerfs), color = c.textSec)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFinishDialog = false
                        if (chronoNotifEnabled) onChronoStop()
                        vm.finish(onFinished)
                    },
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

    editing?.let { (exId, round) ->
        val ex = exercises.firstOrNull { it.id == exId } ?: return@let
        val perf = ex.performances.firstOrNull { it.roundNumber == round }
        EditPerfDialog(
            exerciseName = ex.name,
            roundNumber = round,
            inputType = ex.inputType,
            initial = perf,
            c = c,
            darkTheme = darkTheme,
            onSave = { newReps, newWeight, newDuration, newNotes ->
                vm.upsertPerformance(exId, round, newReps, newWeight, newDuration, newNotes)
                editing = null
            },
            onDismiss = { editing = null }
        )
    }


}

@Composable
private fun ActiveExerciseCard(
    exercise: CircuitExercise,
    exerciseIdx: Int,
    totalExercises: Int,
    c: GainzThemeColors,
    reps: String, onRepsChange: (String) -> Unit,
    weight: String, onWeightChange: (String) -> Unit,
    duration: Long, onDurationChange: (Long) -> Unit,
    notes: String, onNotesChange: (String) -> Unit,
    onStartTimer: (Long) -> Unit = {},
    onValidateNext: () -> Unit
) {
    Column(Modifier.fillMaxWidth()
        .border(2.dp, c.accent, RoundedCornerShape(14.dp))
        .background(c.surface, RoundedCornerShape(14.dp))
        .padding(14.dp)) {
        Text(
            S.exerciseProgress(exerciseIdx + 1, totalExercises),
            color = c.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            exercise.name.ifBlank { S.unnamedExercise },
            color = c.text, fontSize = 22.sp, fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(12.dp))

        when (exercise.inputType) {
            CircuitInputType.REPS -> {
                NumberField(S.reps, reps, onRepsChange, c, placeholder = exercise.referenceReps?.toString())
            }
            CircuitInputType.REPS_WEIGHT -> {
                val repsFocusRequester = remember { FocusRequester() }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        NumberField(S.weight, weight, onWeightChange, c, decimal = true,
                            onImeAction = { repsFocusRequester.requestFocus() })
                    }
                    Box(Modifier.weight(1f).focusRequester(repsFocusRequester)) {
                        NumberField(S.reps, reps, onRepsChange, c, placeholder = exercise.referenceReps?.toString())
                    }
                }
            }
            CircuitInputType.DURATION -> {
                DurationWheelPicker(
                    totalSeconds = duration,
                    onChange = onDurationChange,
                    c = c, showHours = false
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { if (duration > 0L) onStartTimer(duration) },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = c.accentDim,
                        contentColor = c.accent
                    )
                ) {
                    Text(
                        "Démarrer",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))

        // Notes (optionnel)
        BasicTextField(
            value = notes, onValueChange = onNotesChange,
            textStyle = TextStyle(color = c.textSec, fontSize = 13.sp),
            cursorBrush = SolidColor(c.accent),
            decorationBox = { inner ->
                Box(Modifier.fillMaxWidth().background(c.surfaceAlt, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)) {
                    if (notes.isEmpty()) Text(S.notesPlaceholder, color = c.textMuted, fontSize = 13.sp)
                    inner()
                }
            }
        )
        Spacer(Modifier.height(14.dp))

        Button(
            onClick = onValidateNext,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = c.accent)
        ) {
            Text(
                S.validateAndNext,
                color = if (c.dark) Color.Black else Color.White,
                fontWeight = FontWeight.Bold, fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun NumberField(
    label: String, value: String, onChange: (String) -> Unit,
    c: GainzThemeColors, decimal: Boolean = false, placeholder: String? = null,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: (() -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    Column {
        Text(label, color = c.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        BasicTextField(
            value = value, onValueChange = onChange,
            textStyle = TextStyle(color = c.text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
            cursorBrush = SolidColor(c.accent),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(onNext = {
                if (onImeAction != null) onImeAction() else focusManager.moveFocus(FocusDirection.Down)
            }),
            decorationBox = { inner ->
                Box(Modifier.fillMaxWidth()
                    .background(c.surfaceAlt, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)) {
                    if (value.isEmpty()) {
                        Text(placeholder ?: "—", color = c.textMuted, fontSize = 20.sp)
                    }
                    inner()
                }
            }
        )
    }
}

@Composable
private fun RecapTable(
    exercises: List<CircuitExercise>,
    totalRounds: Int,
    currentRound: Int,
    currentExIdx: Int,
    c: GainzThemeColors,
    onTapCell: (String, Int) -> Unit
) {
    Column(Modifier.fillMaxWidth()
        .border(1.dp, c.border, RoundedCornerShape(12.dp))
        .background(c.surface, RoundedCornerShape(12.dp))
        .padding(10.dp)) {
        Text(S.recap, color = c.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        // Header: Exo | T1 | T2 | ...
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
            Column {
                // Header row
                Row {
                    Box(Modifier.width(110.dp).padding(4.dp)) {
                        Text(S.exercise, color = c.textMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    (1..totalRounds).forEach { r ->
                        Box(Modifier.width(56.dp).padding(4.dp), contentAlignment = Alignment.Center) {
                            Text(
                                "T$r",
                                color = if (r == currentRound) c.accent else c.textMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                HorizontalDivider(color = c.border, thickness = 0.5.dp)

                exercises.forEachIndexed { idx, ex ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(110.dp).padding(horizontal = 4.dp, vertical = 6.dp)) {
                            Text(
                                ex.name.ifBlank { "?" },
                                color = if (idx == currentExIdx) c.accent else c.text,
                                fontSize = 12.sp,
                                fontWeight = if (idx == currentExIdx) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 2
                            )
                        }
                        (1..totalRounds).forEach { r ->
                            val perf = ex.performances.firstOrNull { it.roundNumber == r }
                            val isCurrent = r == currentRound && idx == currentExIdx
                            val cell: String = when {
                                isCurrent && perf == null -> "●"
                                perf == null -> "–"
                                ex.inputType == CircuitInputType.DURATION -> formatShortSec(perf.durationSeconds ?: 0L)
                                ex.inputType == CircuitInputType.REPS_WEIGHT -> {
                                    val r2 = perf.reps?.toString() ?: "?"
                                    val w = perf.weightKg?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: "?"
                                    "$r2×${w}kg"
                                }
                                else -> perf.reps?.toString() ?: "–"
                            }
                            Box(
                                Modifier.width(56.dp).height(34.dp)
                                    .padding(horizontal = 2.dp, vertical = 2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(when {
                                        isCurrent -> c.accentDim
                                        perf != null -> c.surfaceAlt
                                        else -> Color.Transparent
                                    })
                                    .clickable(enabled = perf != null) { onTapCell(ex.id, r) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    cell,
                                    color = when {
                                        isCurrent -> c.accent
                                        perf != null -> c.text
                                        else -> c.textMuted
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = if (perf != null) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    if (idx < exercises.size - 1) {
                        HorizontalDivider(color = c.border.copy(alpha = 0.5f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

private fun formatShortSec(sec: Long): String {
    val m = sec / 60; val s = sec % 60
    return if (m > 0) "${m}m${s.toString().padStart(2,'0')}" else "${s}s"
}

@Composable
private fun EditPerfDialog(
    exerciseName: String,
    roundNumber: Int,
    inputType: CircuitInputType,
    initial: CircuitPerformance?,
    c: GainzThemeColors,
    darkTheme: Boolean,
    onSave: (Int?, Double?, Long?, String) -> Unit,
    onDismiss: () -> Unit
) {
    var reps by remember { mutableStateOf(initial?.reps?.toString() ?: "") }
    var weight by remember { mutableStateOf(initial?.weightKg?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: "") }
    var duration by remember { mutableStateOf(initial?.durationSeconds ?: 0L) }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        title = { Text("${exerciseName.ifBlank { S.unnamedExercise }} · T$roundNumber", color = c.text) },
        text = {
            Column {
                when (inputType) {
                    CircuitInputType.REPS -> NumberField(S.reps, reps, { reps = it }, c)
                    CircuitInputType.REPS_WEIGHT -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f)) { NumberField(S.weight, weight, { weight = it }, c, decimal = true) }
                            Box(Modifier.weight(1f)) { NumberField(S.reps, reps, { reps = it }, c) }
                        }
                    }
                    CircuitInputType.DURATION -> {
                        DurationWheelPicker(totalSeconds = duration, onChange = { duration = it }, c = c, showHours = false)
                    }
                }
                Spacer(Modifier.height(10.dp))
                BasicTextField(
                    value = notes, onValueChange = { notes = it },
                    textStyle = TextStyle(color = c.textSec, fontSize = 13.sp),
                    cursorBrush = SolidColor(c.accent),
                    decorationBox = { inner ->
                        Box(Modifier.fillMaxWidth().background(c.surfaceAlt, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)) {
                            if (notes.isEmpty()) Text(S.notesPlaceholder, color = c.textMuted, fontSize = 13.sp)
                            inner()
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        reps.toIntOrNull(),
                        weight.replace(',', '.').toDoubleOrNull(),
                        if (inputType == CircuitInputType.DURATION) duration else null,
                        notes
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = c.accent)
            ) {
                Text(S.save, color = if (darkTheme) Color.Black else Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(S.cancel, color = c.textMuted) } }
    )
}
