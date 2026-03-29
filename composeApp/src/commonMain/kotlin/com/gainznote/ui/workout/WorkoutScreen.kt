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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import com.gainznote.model.Exercise
import com.gainznote.model.TrainingSet
import com.gainznote.repository.WorkoutRepository
import com.gainznote.ui.home.formatDisplayDate
import com.gainznote.ui.theme.GainzThemeColors
import kotlinx.coroutines.delay

@Composable
fun WorkoutScreen(
    repo: WorkoutRepository,
    templateId: String?,
    onBack: () -> Unit,
    onFinished: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val vm = remember { WorkoutViewModel(repo, scope, templateId) }
    val workout by vm.state.collectAsState()
    val c = GainzThemeColors(true)
    var showFinishDialog by remember { mutableStateOf(false) }
    var showSupersetPicker by remember { mutableStateOf<String?>(null) }
    var showAddSetsFor by remember { mutableStateOf<String?>(null) }

    // Chronomètre
    var chronoStart by remember { mutableStateOf<Long?>(null) }
    var chronoDisplay by remember { mutableStateOf("00:00") }

    LaunchedEffect(chronoStart) {
        while (chronoStart != null) {
            val elapsed = (kotlinx.datetime.Clock.System.now().toEpochMilliseconds() - chronoStart!!) / 1000L
            val m = elapsed / 60
            val s = elapsed % 60
            chronoDisplay = "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
            delay(1000)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(c.background).safeDrawingPadding()) {

            // ── TopBar ────────────────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("GainzNote", color = c.accent, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text("Démarré à ${formatDisplayDate(workout.startedAt)}", color = c.textMuted, fontSize = 11.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {

                    // ── Bouton chronomètre ────────────────────────────────────
                    // Fond : surfaceAlt au repos, surface+bordure accent actif
                    // Icône toujours en accent pour rester lisible sur fond sombre
                    val chronoActive = chronoStart != null
                    Box(
                        Modifier
                            .background(
                                if (chronoActive) c.surface else c.surfaceAlt,
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (chronoActive) c.accent else c.border,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (chronoActive) {
                                    chronoStart = null
                                    chronoDisplay = "00:00"
                                } else {
                                    chronoStart = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "⏱",
                            fontSize = 16.sp,
                            color = if (chronoActive) c.accent else c.textSec
                        )
                    }

                    Button(
                        onClick = { showFinishDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = c.accent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Terminer", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            HorizontalDivider(color = c.border, thickness = 0.5.dp)

            // ── Contenu scrollable ────────────────────────────────────────────
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                GainzTextField(
                    value = workout.title,
                    placeholder = "Titre de l'entraînement",
                    textSize = 22.sp, bold = true, c = c,
                    onValueChange = vm::updateTitle
                )
                Spacer(Modifier.height(8.dp))
                GainzTextField(
                    value = workout.notes,
                    placeholder = "Notes générales…",
                    c = c, onValueChange = vm::updateNotes, minLines = 2
                )
                Spacer(Modifier.height(20.dp))

                workout.exercises.forEach { ex ->
                    ExerciseCard(
                        exercise = ex,
                        isSupersetMember = ex.supersetWith != null,
                        c = c, vm = vm,
                        allExercises = workout.exercises,
                        onPickSuperset = { showSupersetPicker = ex.id },
                        onAddMultiple = { showAddSetsFor = ex.id }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                OutlinedButton(
                    onClick = vm::addExercise,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, c.border),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = c.accent)
                ) {
                    Text("+ Ajouter un exercice", fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(40.dp))
            }
        }

        // ── Chronomètre flottant ──────────────────────────────────────────────
        AnimatedVisibility(
            visible = chronoStart != null,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            Box(
                Modifier
                    .background(c.surface, RoundedCornerShape(12.dp))
                    .border(1.5.dp, c.accent, RoundedCornerShape(12.dp))
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Text(chronoDisplay, color = c.accent, fontSize = 24.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
        }
    }

    // ── Dialogue terminer ─────────────────────────────────────────────────────
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            containerColor = c.surface,
            title = { Text("Terminer l'entraînement ?", color = c.text) },
            text = { Text("${workout.exercises.size} exercice(s) · ${workout.exercises.sumOf { it.sets.size }} série(s)", color = c.textSec) },
            confirmButton = {
                Button(
                    onClick = { showFinishDialog = false; vm.finish(onFinished) },
                    colors = ButtonDefaults.buttonColors(containerColor = c.accent)
                ) { Text("Terminer ✓", color = Color.Black, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text("Continuer", color = c.textSec)
                }
            }
        )
    }

    // ── Dialogue superset ─────────────────────────────────────────────────────
    showSupersetPicker?.let { srcId ->
        val candidates = workout.exercises.filter { it.id != srcId && it.supersetWith == null }
        AlertDialog(
            onDismissRequest = { showSupersetPicker = null },
            containerColor = c.surface,
            title = { Text("Associer en superset avec…", color = c.text) },
            text = {
                if (candidates.isEmpty()) Text("Aucun exercice disponible.", color = c.textSec)
                else Column {
                    candidates.forEach { ex ->
                        TextButton(
                            onClick = { vm.linkSuperset(srcId, ex.id); showSupersetPicker = null },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(ex.name.ifBlank { "Exercice sans nom" }, color = c.superset) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSupersetPicker = null }) {
                    Text("Annuler", color = c.textSec)
                }
            }
        )
    }

    // ── Dialogue multi-séries ─────────────────────────────────────────────────
    showAddSetsFor?.let { exId ->
        var count by remember { mutableStateOf(3) }
        AlertDialog(
            onDismissRequest = { showAddSetsFor = null },
            containerColor = c.surface,
            title = { Text("Ajouter des séries", color = c.text) },
            text = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (count > 1) count-- }) {
                        Text("−", color = c.textSec, fontSize = 20.sp)
                    }
                    Text("$count", color = c.text, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp))
                    IconButton(onClick = { if (count < 20) count++ }) {
                        Text("+", color = c.textSec, fontSize = 20.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { vm.addSets(exId, count); showAddSetsFor = null },
                    colors = ButtonDefaults.buttonColors(containerColor = c.accent)
                ) { Text("Ajouter", color = Color.Black) }
            },
            dismissButton = {
                TextButton(onClick = { showAddSetsFor = null }) { Text("Annuler", color = c.textSec) }
            }
        )
    }
}

// ─── GainzTextField ───────────────────────────────────────────────────────────

@Composable
fun GainzTextField(
    value: String, placeholder: String, c: GainzThemeColors,
    onValueChange: (String) -> Unit, textSize: TextUnit = 15.sp,
    bold: Boolean = false, minLines: Int = 1
) {
    BasicTextField(
        value = value, onValueChange = onValueChange, minLines = minLines,
        textStyle = TextStyle(color = c.text, fontSize = textSize,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal),
        cursorBrush = SolidColor(c.accent),
        decorationBox = { inner ->
            Box(Modifier.fillMaxWidth().border(1.dp, c.border, RoundedCornerShape(12.dp)).padding(12.dp)) {
                if (value.isEmpty()) Text(placeholder, color = c.textMuted, fontSize = textSize)
                inner()
            }
        }
    )
}

// ─── ExerciseCard ─────────────────────────────────────────────────────────────

@Composable
fun ExerciseCard(
    exercise: Exercise,
    isSupersetMember: Boolean,
    c: GainzThemeColors,
    vm: WorkoutViewModel,
    allExercises: List<Exercise>,
    onPickSuperset: () -> Unit,
    onAddMultiple: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth()
            .border(if (isSupersetMember) 2.dp else 1.dp,
                if (isSupersetMember) c.superset else c.border, RoundedCornerShape(14.dp))
            .background(c.surface, RoundedCornerShape(14.dp))
    ) {
        Row(Modifier.padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            if (isSupersetMember) {
                Box(Modifier.background(c.supersetDim, RoundedCornerShape(5.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text("SS", color = c.superset, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
            }
            BasicTextField(
                value = exercise.name,
                onValueChange = { vm.updateExerciseName(exercise.id, it) },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(color = c.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                cursorBrush = SolidColor(c.accent),
                singleLine = true,
                decorationBox = { inner ->
                    if (exercise.name.isEmpty()) Text("Nom de l'exercice", color = c.textMuted, fontSize = 15.sp)
                    inner()
                }
            )
            var menuOpen by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Text("⋮", color = c.textSec, fontSize = 20.sp)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false },
                    containerColor = c.surface) {
                    if (isSupersetMember)
                        DropdownMenuItem(text = { Text("Retirer du superset", color = c.superset) },
                            onClick = { vm.unlinkSuperset(exercise.id); menuOpen = false })
                    else
                        DropdownMenuItem(text = { Text("Associer en superset", color = c.text) },
                            onClick = { onPickSuperset(); menuOpen = false })
                    DropdownMenuItem(text = { Text("Supprimer l'exercice", color = c.danger) },
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
            Spacer(Modifier.width(56.dp))
        }

        exercise.sets.forEachIndexed { i, set ->
            SetRow(index = i, set = set, c = c,
                onUpdate = { w, r, n -> vm.updateSet(exercise.id, set.id, weight = w, reps = r, notes = n) },
                onPropagate = { vm.propagateWeight(exercise.id, set.id) },
                onRemove = { vm.removeSet(exercise.id, set.id) })
        }

        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            TextButton(onClick = { vm.addSets(exercise.id) }, modifier = Modifier.weight(1f)) {
                Text("+ Série", color = c.accent, fontSize = 13.sp)
            }
            TextButton(onClick = onAddMultiple, modifier = Modifier.weight(1f)) {
                Text("+ Plusieurs", color = c.accent, fontSize = 13.sp)
            }
        }
    }
}

// ─── SetRow ───────────────────────────────────────────────────────────────────

@Composable
fun SetRow(
    index: Int, set: TrainingSet, c: GainzThemeColors,
    onUpdate: (Double?, Int?, String?) -> Unit,
    onPropagate: () -> Unit, onRemove: () -> Unit
) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)) {

        Text("${index + 1}", color = c.textMuted, fontSize = 12.sp, modifier = Modifier.width(24.dp))

        // ── Poids ─────────────────────────────────────────────────────────────
        // On utilise un state local pour éviter le bug de suppression du dernier caractère.
        // Le state local absorbe la frappe en temps réel ; on ne notifie le VM
        // que quand la valeur parsée change, ce qui évite le re-render intempestif.
        var weightText by remember(set.id) {
            mutableStateOf(set.weightKg?.let {
                if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
            } ?: "")
        }
        CompactField(
            localValue = weightText,
            hint = "0",
            keyboardType = KeyboardType.Decimal,
            c = c,
            modifier = Modifier.weight(1f).height(34.dp),
            onLocalChange = { raw ->
                weightText = raw
                onUpdate(raw.toDoubleOrNull(), null, null)
            }
        )

        // ── Reps ──────────────────────────────────────────────────────────────
        var repsText by remember(set.id) { mutableStateOf(set.reps?.toString() ?: "") }
        CompactField(
            localValue = repsText,
            hint = set.repsPlaceholder?.toString() ?: "0",
            hintIsAccent = set.repsPlaceholder != null,
            keyboardType = KeyboardType.Number,
            c = c,
            modifier = Modifier.weight(1f).height(34.dp),
            onLocalChange = { raw ->
                repsText = raw
                onUpdate(null, raw.toIntOrNull(), null)
            }
        )

        // ── Notes ─────────────────────────────────────────────────────────────
        var notesText by remember(set.id) { mutableStateOf(set.notes) }
        CompactField(
            localValue = notesText,
            hint = "…",
            c = c,
            modifier = Modifier.weight(1.5f).height(34.dp),
            onLocalChange = { raw ->
                notesText = raw
                onUpdate(null, null, raw)
            }
        )

        // ── Bouton propager (visible seulement si poids renseigné) ────────────
        Box(Modifier.width(32.dp).height(34.dp), contentAlignment = Alignment.Center) {
            if (set.weightKg != null) {
                Box(Modifier.size(28.dp).clickable { onPropagate() },
                    contentAlignment = Alignment.Center) {
                    Text("⬇", color = c.textSec, fontSize = 14.sp)
                }
            }
        }

        // ── Bouton supprimer ──────────────────────────────────────────────────
        Box(Modifier.size(32.dp).clickable { onRemove() }, contentAlignment = Alignment.Center) {
            Text("✕", color = c.danger, fontSize = 12.sp)
        }
    }
}

// ─── CompactField ─────────────────────────────────────────────────────────────
// Prend un `localValue` géré par le parent (state local dans SetRow).
// Cela évite le problème où BasicTextField lie sa valeur au modèle distant
// et empêche la suppression du dernier caractère.

@Composable
fun CompactField(
    localValue: String,
    hint: String,
    c: GainzThemeColors,
    modifier: Modifier,
    hintIsAccent: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    onLocalChange: (String) -> Unit
) {
    BasicTextField(
        value = localValue,
        onValueChange = onLocalChange,
        modifier = modifier
            .background(c.surfaceAlt, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 0.dp),
        textStyle = TextStyle(color = c.text, fontSize = 14.sp, fontWeight = FontWeight.Medium),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        cursorBrush = SolidColor(c.accent),
        singleLine = true,
        decorationBox = { inner ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                if (localValue.isEmpty())
                    Text(hint,
                        color = if (hintIsAccent) c.accent.copy(alpha = 0.6f) else c.textMuted,
                        fontSize = 14.sp)
                inner()
            }
        }
    )
}
