package com.gainznote.ui.workout

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import com.gainznote.model.Exercise
import com.gainznote.model.TrainingSet
import com.gainznote.repository.WorkoutRepository
import com.gainznote.ui.home.formatDisplayDateFull
import com.gainznote.ui.theme.GainzThemeColors
import com.gainznote.i18n.S
import kotlinx.coroutines.delay

@Composable
fun WorkoutScreen(
    repo: WorkoutRepository,
    darkTheme: Boolean,
    
    templateId: String?,
    resumeId: String? = null,
    onBack: () -> Unit,
    onFinished: () -> Unit,
    chronoNotifEnabled: Boolean = false,
    onChronoStart: (Long) -> Unit = {},
    onChronoStop: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    // Key sur resumeId+templateId pour recréer le VM si on change d'entraînement
    val vm = remember(resumeId, templateId) { WorkoutViewModel(repo, scope, templateId, resumeId) }
    val workout by vm.state.collectAsState()
    val c = GainzThemeColors(darkTheme)
    var showFinishDialog by remember { mutableStateOf(false) }
    var showSupersetPicker by remember { mutableStateOf<String?>(null) }
    var showAddSetsFor by remember { mutableStateOf<String?>(null) }

    // ── Chronomètre ───────────────────────────────────────────────────────────
    var chronoStart by remember { mutableStateOf<Long?>(null) }
    var chronoDisplay by remember { mutableStateOf("00:00") }
    LaunchedEffect(chronoStart) {
        while (chronoStart != null) {
            val elapsed = (kotlinx.datetime.Clock.System.now().toEpochMilliseconds() - chronoStart!!) / 1000L
            chronoDisplay = "${(elapsed / 60).toString().padStart(2, '0')}:${(elapsed % 60).toString().padStart(2, '0')}"
            delay(1000)
        }
    }

    Column(Modifier.fillMaxSize().background(c.background).safeDrawingPadding()) {

        // ── TopBar ────────────────────────────────────────────────────────────
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("GainzNote", color = c.accent, fontSize = 20.sp, fontWeight = FontWeight.Black)
                // Jour en toutes lettres
                Text(S.startedAt(formatDisplayDateFull(workout.startedAt)), color = c.textMuted, fontSize = 11.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                val chronoActive = chronoStart != null
                Box(
                    Modifier
                        .background(if (chronoActive) c.accentDim else c.surfaceAlt, RoundedCornerShape(8.dp))
                        .border(1.dp, if (chronoActive) c.accent else c.border, RoundedCornerShape(8.dp))
                        .clickable {
                            if (chronoActive) {
                                // Arrêter le chrono
                                chronoStart = null
                                chronoDisplay = "00:00"
                                if (chronoNotifEnabled) onChronoStop()
                            } else {
                                // Démarrer le chrono
                                val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                                chronoStart = now
                                if (chronoNotifEnabled) onChronoStart(now)
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⏱", fontSize = 16.sp, color = if (chronoActive) c.accent else c.textSec)
                }
                Button(onClick = { showFinishDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = c.accent),
                    shape = RoundedCornerShape(10.dp)) {
                    Text(S.finish, color = if (darkTheme) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ── Chrono inline — juste sous la topbar, toujours visible même avec le clavier ──
        // Positionné dans le flux normal (pas flottant) donc reste visible même si le contenu
        // est décalé par le clavier. S'affiche/masque avec animation.
        AnimatedVisibility(
            visible = chronoStart != null,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(c.accentDim)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "⏱  $chronoDisplay",
                    color = c.accent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }

        HorizontalDivider(color = c.border, thickness = 0.5.dp)

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            GainzTextField(value = workout.title, placeholder = S.workoutTitlePlaceholder,
                textSize = 22.sp, bold = true, c = c, onValueChange = vm::updateTitle,
                capitalization = KeyboardCapitalization.Sentences)
            Spacer(Modifier.height(8.dp))
            GainzTextField(value = workout.notes, placeholder = S.generalNotesPlaceholder,
                c = c, onValueChange = vm::updateNotes, minLines = 2,
                capitalization = KeyboardCapitalization.Sentences)
            Spacer(Modifier.height(20.dp))

            workout.exercises.forEachIndexed { idx, ex ->
                ExerciseCard(
                    exercise = ex, isFirst = idx == 0,
                    isSupersetMember = ex.supersetWith != null,
                    c = c, vm = vm, allExercises = workout.exercises,
                    onPickSuperset = { showSupersetPicker = ex.id },
                    onAddMultiple = { showAddSetsFor = ex.id }
                )
                Spacer(Modifier.height(12.dp))
            }

            OutlinedButton(onClick = vm::addExercise,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, c.border),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = c.accent)) {
                Text(S.addExercise, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    // Dialogue terminer
    if (showFinishDialog) {
        AlertDialog(onDismissRequest = { showFinishDialog = false },
            containerColor = c.surface,
            title = { Text(S.finishWorkoutTitle, color = c.text) },
            text = { Text(S.finishWorkoutBody(workout.exercises.size, workout.exercises.sumOf { it.sets.size }), color = c.textSec) },
            confirmButton = {
                Button(onClick = { showFinishDialog = false; if (chronoNotifEnabled) onChronoStop(); vm.finish(onFinished) },
                    colors = ButtonDefaults.buttonColors(containerColor = c.accent)) {
                    Text(S.finishConfirm, color = if (darkTheme) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showFinishDialog = false }) { Text(S.continueWorkout, color = c.textSec) } })
    }

    // Dialogue superset
    showSupersetPicker?.let { srcId ->
        val candidates = workout.exercises.filter { it.id != srcId && it.supersetWith == null }
        AlertDialog(onDismissRequest = { showSupersetPicker = null },
            containerColor = c.surface,
            title = { Text(S.supersetPickerTitle, color = c.text) },
            text = {
                if (candidates.isEmpty()) Text(S.noExerciseAvailable, color = c.textSec)
                else Column { candidates.forEach { ex ->
                    TextButton(onClick = { vm.linkSuperset(srcId, ex.id); showSupersetPicker = null },
                        modifier = Modifier.fillMaxWidth()) {
                        Text(ex.name.ifBlank { S.unnamedExercise }, color = c.superset)
                    }
                }}
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showSupersetPicker = null }) { Text(S.cancel, color = c.textSec) } })
    }

    // Dialogue multi-séries
    showAddSetsFor?.let { exId ->
        var count by remember { mutableStateOf(3) }
        AlertDialog(onDismissRequest = { showAddSetsFor = null },
            containerColor = c.surface,
            title = { Text(S.addSetsTitle, color = c.text) },
            text = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (count > 1) count-- }) { Text("−", color = c.textSec, fontSize = 20.sp) }
                    Text("$count", color = c.text, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp))
                    IconButton(onClick = { if (count < 20) count++ }) { Text("+", color = c.textSec, fontSize = 20.sp) }
                }
            },
            confirmButton = {
                Button(onClick = { vm.addSets(exId, count); showAddSetsFor = null },
                    colors = ButtonDefaults.buttonColors(containerColor = c.accent)) {
                    Text(S.add, color = if (darkTheme) Color.Black else Color.White)
                }
            },
            dismissButton = { TextButton(onClick = { showAddSetsFor = null }) { Text(S.cancel, color = c.textSec) } })
    }
}

// ─── GainzTextField ───────────────────────────────────────────────────────────

@Composable
fun GainzTextField(
    value: String, placeholder: String, c: GainzThemeColors,
    onValueChange: (String) -> Unit, textSize: TextUnit = 15.sp,
    bold: Boolean = false, minLines: Int = 1,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences
) {
    BasicTextField(value = value, onValueChange = onValueChange, minLines = minLines,
        textStyle = TextStyle(color = c.text, fontSize = textSize,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal),
        keyboardOptions = KeyboardOptions(capitalization = capitalization),
        cursorBrush = SolidColor(c.accent),
        decorationBox = { inner ->
            Box(Modifier.fillMaxWidth().border(1.dp, c.border, RoundedCornerShape(12.dp)).padding(12.dp)) {
                if (value.isEmpty()) Text(placeholder, color = c.textMuted, fontSize = textSize)
                inner()
            }
        })
}

// ─── ExerciseCard ─────────────────────────────────────────────────────────────

@Composable
fun ExerciseCard(
    exercise: Exercise, isFirst: Boolean, isSupersetMember: Boolean,
    c: GainzThemeColors, vm: WorkoutViewModel, allExercises: List<Exercise>,
    onPickSuperset: () -> Unit, onAddMultiple: () -> Unit
) {
    Column(Modifier.fillMaxWidth()
        .border(if (isSupersetMember) 2.dp else 1.dp,
            if (isSupersetMember) c.superset else c.border, RoundedCornerShape(14.dp))
        .background(c.surface, RoundedCornerShape(14.dp))) {

        Row(Modifier.padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            if (isSupersetMember) {
                Box(Modifier.background(c.supersetDim, RoundedCornerShape(5.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text("SS", color = c.superset, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
            }
            // Majuscule Words pour les noms d'exercices
            BasicTextField(value = exercise.name,
                onValueChange = { vm.updateExerciseName(exercise.id, it) },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(color = c.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                cursorBrush = SolidColor(c.accent), singleLine = true,
                decorationBox = { inner ->
                    if (exercise.name.isEmpty()) Text(S.exerciseNamePlaceholder, color = c.textMuted, fontSize = 15.sp)
                    inner()
                })
            var menuOpen by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { menuOpen = true }) { Text("⋮", color = c.textSec, fontSize = 20.sp) }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }, containerColor = c.surface) {
                    if (!isFirst)
                        DropdownMenuItem(text = { Text(S.moveUp, color = c.text) },
                            onClick = { vm.moveExerciseUp(exercise.id); menuOpen = false })
                    if (isSupersetMember)
                        DropdownMenuItem(text = { Text(S.unlinkSuperset, color = c.superset) },
                            onClick = { vm.unlinkSuperset(exercise.id); menuOpen = false })
                    else
                        DropdownMenuItem(text = { Text(S.linkSuperset, color = c.text) },
                            onClick = { onPickSuperset(); menuOpen = false })
                    DropdownMenuItem(text = { Text(S.deleteExercise, color = c.danger) },
                        onClick = { vm.removeExercise(exercise.id); menuOpen = false })
                }
            }
        }

        HorizontalDivider(color = if (isSupersetMember) c.superset.copy(alpha = 0.3f) else c.border, thickness = 0.5.dp)

        Row(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
            Text("#", color = c.textMuted, fontSize = 11.sp, modifier = Modifier.width(24.dp))
            Text("kg", color = c.textMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
            Text("reps", color = c.textMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
            Text("note", color = c.textMuted, fontSize = 11.sp, modifier = Modifier.weight(1.5f))
            Spacer(Modifier.width(64.dp))
        }

        exercise.sets.forEachIndexed { i, set ->
            SetRow(index = i, set = set, c = c,
                onWeightChange = { w -> vm.updateSetWeight(exercise.id, set.id, w) },
                onRepsChange = { r -> vm.updateSetReps(exercise.id, set.id, r) },
                onNotesChange = { n -> vm.updateSetNotes(exercise.id, set.id, n) },
                onPropagate = { vm.propagateWeight(exercise.id, set.id) },
                onRemove = { vm.removeSet(exercise.id, set.id) })
        }

        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            TextButton(onClick = { vm.addSets(exercise.id) }, modifier = Modifier.weight(1f)) {
                Text(S.addSet, color = c.accent, fontSize = 13.sp)
            }
            TextButton(onClick = onAddMultiple, modifier = Modifier.weight(1f)) {
                Text(S.addMultiple, color = c.accent, fontSize = 13.sp)
            }
        }
    }
}

// ─── SetRow ───────────────────────────────────────────────────────────────────

@Composable
fun SetRow(index: Int, set: TrainingSet, c: GainzThemeColors,
           onWeightChange: (Double?) -> Unit, onRepsChange: (Int?) -> Unit,
           onNotesChange: (String) -> Unit, onPropagate: () -> Unit, onRemove: () -> Unit) {

    var weightText by remember(set.id) {
        mutableStateOf(set.weightKg?.let {
            if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
        } ?: "")
    }
    var repsText by remember(set.id) { mutableStateOf(set.reps?.toString() ?: "") }
    var notesText by remember(set.id) { mutableStateOf(set.notes) }

    LaunchedEffect(set.weightKg) {
        if (weightText.toDoubleOrNull() != set.weightKg) {
            weightText = set.weightKg?.let {
                if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
            } ?: ""
        }
    }

    val showPropagate = weightText.isNotEmpty()

    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)) {

        Text("${index + 1}", color = c.textMuted, fontSize = 12.sp, modifier = Modifier.width(24.dp))

        CompactField(localValue = weightText, hint = "0", keyboardType = KeyboardType.Decimal,
            c = c, modifier = Modifier.weight(1f).height(34.dp),
            onLocalChange = { raw -> weightText = raw; onWeightChange(raw.toDoubleOrNull()) })

        CompactField(localValue = repsText,
            hint = set.repsPlaceholder?.toString() ?: "0",
            hintIsAccent = set.repsPlaceholder != null,
            keyboardType = KeyboardType.Number,
            c = c, modifier = Modifier.weight(1f).height(34.dp),
            onLocalChange = { raw -> repsText = raw; onRepsChange(raw.toIntOrNull()) })

        CompactField(localValue = notesText, hint = "…", c = c,
            modifier = Modifier.weight(1.5f).height(34.dp),
            onLocalChange = { raw -> notesText = raw; onNotesChange(raw) })

        // ⬇ Propager — fond accentDim, même style que le bouton supprimer
        Box(Modifier.width(30.dp).height(34.dp), contentAlignment = Alignment.Center) {
            if (showPropagate) {
                Box(Modifier.size(28.dp)
                    .background(c.accentDim, RoundedCornerShape(6.dp))
                    .clickable {
                        onWeightChange(weightText.toDoubleOrNull())
                        onPropagate()
                    }, contentAlignment = Alignment.Center) {
                    Text("⬇", color = c.accent, fontSize = 13.sp)
                }
            } else {
                Spacer(Modifier.size(28.dp))
            }
        }

        // ✕ Supprimer — fond danger, croix blanche
        Box(Modifier.size(28.dp)
            .background(c.danger, RoundedCornerShape(6.dp))
            .clickable { onRemove() },
            contentAlignment = Alignment.Center) {
            Text("✕", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ─── CompactField ─────────────────────────────────────────────────────────────

@Composable
fun CompactField(localValue: String, hint: String, c: GainzThemeColors, modifier: Modifier,
                 hintIsAccent: Boolean = false, keyboardType: KeyboardType = KeyboardType.Text,
                 onLocalChange: (String) -> Unit) {
    BasicTextField(value = localValue, onValueChange = onLocalChange,
        modifier = modifier.background(c.surfaceAlt, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 0.dp),
        textStyle = TextStyle(color = c.text, fontSize = 14.sp, fontWeight = FontWeight.Medium),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        cursorBrush = SolidColor(c.accent), singleLine = true,
        decorationBox = { inner ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                if (localValue.isEmpty())
                    Text(hint, color = if (hintIsAccent) c.accent.copy(alpha = 0.6f) else c.textMuted, fontSize = 14.sp)
                inner()
            }
        })
}
