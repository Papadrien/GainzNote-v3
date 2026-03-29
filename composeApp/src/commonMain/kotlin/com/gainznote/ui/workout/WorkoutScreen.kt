package com.gainznote.ui.workout

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
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@Composable
fun WorkoutScreen(repo: WorkoutRepository, templateId: String?, onFinished: () -> Unit) {
    val scope = rememberCoroutineScope()
    val vm = remember { WorkoutViewModel(repo, scope, templateId) }
    val workout by vm.state.collectAsState()
    val c = GainzThemeColors(true) // thème récupéré via paramètre idéalement
    var showFinishDialog by remember { mutableStateOf(false) }
    var showSupersetPicker by remember { mutableStateOf<String?>(null) }
    var showAddSetsFor by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().background(c.background).safeDrawingPadding()) {
        // TopBar
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("GainzNote", color = c.accent, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text("Démarré à ${formatDisplayDate(workout.startedAt)}", color = c.textMuted, fontSize = 11.sp)
            }
            Button(onClick = { showFinishDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = c.accent),
                shape = RoundedCornerShape(10.dp)) {
                Text("Terminer", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
        HorizontalDivider(color = c.border, thickness = 0.5.dp)

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            // Titre
            GainzTextField(value = workout.title, placeholder = "Titre de l'entraînement",
                textSize = 22.sp, bold = true, c = c, onValueChange = vm::updateTitle)
            Spacer(Modifier.height(8.dp))
            GainzTextField(value = workout.notes, placeholder = "Notes générales…",
                c = c, onValueChange = vm::updateNotes, minLines = 2)
            Spacer(Modifier.height(20.dp))

            // Exercices
            workout.exercises.forEachIndexed { idx, ex ->
                val isSecond = idx > 0 && workout.exercises[idx-1].supersetWith == ex.id
                if (!isSecond) {
                    val partner = ex.supersetWith?.let { pid -> workout.exercises.firstOrNull { it.id == pid } }
                    ExerciseCard(exercise = ex, partner = partner, c = c, vm = vm,
                        allExercises = workout.exercises,
                        onPickSuperset = { showSupersetPicker = ex.id },
                        onAddMultiple = { showAddSetsFor = ex.id })
                    Spacer(Modifier.height(12.dp))
                }
            }

            OutlinedButton(onClick = vm::addExercise, modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, c.border),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = c.accent)) {
                Text("+ Ajouter un exercice", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    // Dialogue terminer
    if (showFinishDialog) {
        AlertDialog(onDismissRequest = { showFinishDialog = false },
            containerColor = c.surface,
            title = { Text("Terminer l'entraînement ?", color = c.text) },
            text = { Text("${workout.exercises.size} exercice(s) · ${workout.exercises.sumOf { it.sets.size }} série(s)", color = c.textSec) },
            confirmButton = {
                Button(onClick = { showFinishDialog = false; vm.finish(onFinished) },
                    colors = ButtonDefaults.buttonColors(containerColor = c.accent)) {
                    Text("Terminer ✓", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showFinishDialog = false }) {
                Text("Continuer", color = c.textMuted) } })
    }

    // Dialogue superset
    showSupersetPicker?.let { srcId ->
        val candidates = workout.exercises.filter { it.id != srcId && it.supersetWith == null }
        AlertDialog(onDismissRequest = { showSupersetPicker = null },
            containerColor = c.surface,
            title = { Text("Associer en superset", color = c.text) },
            text = {
                if (candidates.isEmpty()) Text("Aucun exercice disponible.", color = c.textSec)
                else Column { candidates.forEach { ex ->
                    TextButton(onClick = { vm.linkSuperset(srcId, ex.id); showSupersetPicker = null },
                        modifier = Modifier.fillMaxWidth()) {
                        Text(ex.name.ifBlank { "Exercice sans nom" }, color = c.superset)
                    }
                }}
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showSupersetPicker = null }) {
                Text("Annuler", color = c.textMuted) } })
    }

    // Dialogue multi-séries
    showAddSetsFor?.let { exId ->
        var count by remember { mutableStateOf(3) }
        AlertDialog(onDismissRequest = { showAddSetsFor = null },
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
                Button(onClick = { vm.addSets(exId, count); showAddSetsFor = null },
                    colors = ButtonDefaults.buttonColors(containerColor = c.accent)) {
                    Text("Ajouter", color = Color.Black)
                }
            },
            dismissButton = { TextButton(onClick = { showAddSetsFor = null }) {
                Text("Annuler", color = c.textMuted) } })
    }
}

@Composable
fun GainzTextField(value: String, placeholder: String, c: GainzThemeColors,
                   onValueChange: (String) -> Unit, textSize: TextUnit = 15.sp,
                   bold: Boolean = false, minLines: Int = 1) {
    BasicTextField(value = value, onValueChange = onValueChange, minLines = minLines,
        textStyle = TextStyle(color = c.text, fontSize = textSize,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal),
        cursorBrush = SolidColor(c.accent),
        decorationBox = { inner ->
            Box(Modifier.fillMaxWidth().border(1.dp, c.border, RoundedCornerShape(12.dp))
                .padding(12.dp)) {
                if (value.isEmpty()) Text(placeholder, color = c.textMuted, fontSize = textSize)
                inner()
            }
        })
}

@Composable
fun ExerciseCard(exercise: Exercise, partner: Exercise?, c: GainzThemeColors,
                 vm: WorkoutViewModel, allExercises: List<Exercise>,
                 onPickSuperset: () -> Unit, onAddMultiple: () -> Unit) {
    val isSuperset = partner != null
    Column(Modifier.fillMaxWidth()
        .border(1.dp, if (isSuperset) c.superset else c.border, RoundedCornerShape(14.dp))
        .background(c.surface, RoundedCornerShape(14.dp))) {

        // Header
        Row(Modifier.padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            if (isSuperset) {
                Box(Modifier.background(c.supersetDim, RoundedCornerShape(5.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text("SS", color = c.superset, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
            }
            BasicTextField(value = exercise.name,
                onValueChange = { vm.updateExerciseName(exercise.id, it) },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(color = c.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                cursorBrush = SolidColor(c.accent),
                singleLine = true,
                decorationBox = { inner ->
                    if (exercise.name.isEmpty()) Text("Nom de l'exercice", color = c.textMuted, fontSize = 15.sp)
                    inner()
                })
            // Menu
            var menuOpen by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Text("⋮", color = c.textMuted, fontSize = 20.sp)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false },
                    containerColor = c.surface) {
                    if (isSuperset) DropdownMenuItem(
                        text = { Text("Retirer le superset", color = c.superset) },
                        onClick = { vm.unlinkSuperset(exercise.id); menuOpen = false })
                    else DropdownMenuItem(
                        text = { Text("Associer en superset", color = c.text) },
                        onClick = { onPickSuperset(); menuOpen = false })
                    DropdownMenuItem(
                        text = { Text("Supprimer l'exercice", color = c.danger) },
                        onClick = { vm.removeExercise(exercise.id); menuOpen = false })
                }
            }
        }

        HorizontalDivider(color = c.border, thickness = 0.5.dp)

        // En-têtes colonnes
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

        // Boutons ajout séries
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            TextButton(onClick = { vm.addSets(exercise.id) }, modifier = Modifier.weight(1f)) {
                Text("+ Série", color = c.accent, fontSize = 13.sp)
            }
            TextButton(onClick = onAddMultiple, modifier = Modifier.weight(1f)) {
                Text("+ Plusieurs", color = c.accent, fontSize = 13.sp)
            }
        }

        // Partenaire superset
        if (isSuperset && partner != null) {
            HorizontalDivider(color = c.superset.copy(alpha = 0.3f))
            Box(Modifier.fillMaxWidth().background(c.supersetDim.copy(alpha = 0.4f))
                .padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text("↕ ${partner.name.ifBlank { "Exercice partenaire" }}",
                    color = c.superset, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun SetRow(index: Int, set: TrainingSet, c: GainzThemeColors,
           onUpdate: (Double?, Int?, String?) -> Unit,
           onPropagate: () -> Unit, onRemove: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("${index+1}", color = c.textMuted, fontSize = 12.sp, modifier = Modifier.width(24.dp))
        CompactField(value = set.weightKg?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: "",
            hint = "0", keyboardType = KeyboardType.Decimal, c = c, modifier = Modifier.weight(1f),
            onValueChange = { onUpdate(it.toDoubleOrNull(), null, null) })
        CompactField(value = set.reps?.toString() ?: "",
            hint = set.repsPlaceholder?.toString() ?: "0",
            hintIsAccent = set.repsPlaceholder != null,
            keyboardType = KeyboardType.Number, c = c, modifier = Modifier.weight(1f),
            onValueChange = { onUpdate(null, it.toIntOrNull(), null) })
        CompactField(value = set.notes, hint = "…", c = c, modifier = Modifier.weight(1.5f),
            onValueChange = { onUpdate(null, null, it) })
        Box(Modifier.size(32.dp).clickable { onPropagate() }, contentAlignment = Alignment.Center) {
            Text("⬇", color = c.textMuted, fontSize = 14.sp)
        }
        Box(Modifier.size(32.dp).clickable { onRemove() }, contentAlignment = Alignment.Center) {
            Text("✕", color = c.danger, fontSize = 12.sp)
        }
    }
}

@Composable
fun CompactField(value: String, hint: String, c: GainzThemeColors, modifier: Modifier,
                 hintIsAccent: Boolean = false, keyboardType: KeyboardType = KeyboardType.Text,
                 onValueChange: (String) -> Unit) {
    BasicTextField(value = value, onValueChange = onValueChange,
        modifier = modifier.background(c.surfaceAlt, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        textStyle = TextStyle(color = c.text, fontSize = 14.sp, fontWeight = FontWeight.Medium),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        cursorBrush = SolidColor(c.accent), singleLine = true,
        decorationBox = { inner ->
            if (value.isEmpty()) Text(hint, color = if (hintIsAccent) c.accent.copy(alpha = 0.5f) else c.textMuted, fontSize = 14.sp)
            inner()
        })
}
