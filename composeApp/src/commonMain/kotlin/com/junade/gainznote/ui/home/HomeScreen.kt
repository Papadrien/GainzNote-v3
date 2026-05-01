package com.junade.gainznote.ui.home

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
import com.junade.gainznote.model.Workout
import com.junade.gainznote.model.WorkoutType
import com.junade.gainznote.repository.WorkoutRepository
import com.junade.gainznote.ui.theme.GainzThemeColors
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.junade.gainznote.i18n.S
import com.junade.gainznote.ui.ads.AdBanner

@Composable
fun HomeScreen(
    repo: WorkoutRepository,
    darkTheme: Boolean,
    chronoNotifEnabled: Boolean = false,
    onToggleTheme: () -> Unit,
    onToggleChronoNotif: () -> Unit = {},
    selectedWorkoutType: WorkoutType = WorkoutType.MUSCULATION,
    onSelectedWorkoutTypeChange: (WorkoutType) -> Unit = {},
    onStartWorkoutOfType: (WorkoutType) -> Unit = {},
    onHistory: () -> Unit,
    onOpenWorkout: (String) -> Unit,
    onResumeWorkout: (String) -> Unit = {},
    onDeleteInProgressWorkout: (String) -> Unit = {},
    onExport: () -> Unit = {},
    onImport: () -> Unit = {},
    adFree: Boolean = false,
    isDebug: Boolean = false,
    onPurchaseRemoveAds: () -> Unit = {},
    onToggleAdFree: () -> Unit = {},
    language: String = "auto",
    onChangeLang: (String) -> Unit = {},
    refreshKey: Int = 0
) {
    val c = GainzThemeColors(darkTheme, type = selectedWorkoutType)
    var recentWorkouts by remember { mutableStateOf<List<Workout>>(emptyList()) }
    var inProgressWorkouts by remember { mutableStateOf<List<Workout>>(emptyList()) }

    LaunchedEffect(refreshKey) {
        recentWorkouts = repo.getFinishedWorkouts().take(3)
        inProgressWorkouts = repo.getInProgressWorkouts()
    }

    Column(
        Modifier.fillMaxSize().background(c.background).safeDrawingPadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            "GainzNote", color = c.accent, fontSize = 34.sp,
            fontWeight = FontWeight.Black, letterSpacing = (-1).sp
        )
        Text(S.subtitle, color = c.textMuted, fontSize = 13.sp)
        Spacer(Modifier.height(28.dp))

        // State local pour le dialog "écraser entraînement en cours"
        var pendingTypeForStart by remember { mutableStateOf<WorkoutType?>(null) }

                

        fun tryStart(type: WorkoutType) {
            if (inProgressWorkouts.isNotEmpty()) {
                pendingTypeForStart = type
            } else {
                onStartWorkoutOfType(type)
            }
        }

        
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WorkoutTypeDropdown(
                selected = selectedWorkoutType,
                onSelected = { onSelectedWorkoutTypeChange(it) },
                darkTheme = darkTheme,
                modifier = Modifier.weight(1f)
            )
            val (accent, _) = Pair(c.accent, c.accentDim)
            Button(
                onClick = { tryStart(selectedWorkoutType) },
                modifier = Modifier.weight(1.2f).height(58.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) {
                Text(
                    S.newWorkout,
                    color = if (darkTheme) Color.Black else Color.White,
                    fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
        Spacer(Modifier.height(32.dp))

        if (pendingTypeForStart != null) {
            val t = pendingTypeForStart!!
            AlertDialog(
                onDismissRequest = { pendingTypeForStart = null },
                containerColor = c.surface,
                title = { Text(S.overwriteInProgressTitle, color = c.text) },
                text = { Text(S.overwriteInProgressBody, color = c.textSec) },
                confirmButton = {
                    Button(
                        onClick = {
                            pendingTypeForStart = null
                            // Supprimer les entraînements en cours avant de démarrer le nouveau
                            inProgressWorkouts.forEach { onDeleteInProgressWorkout(it.id) }
                            onStartWorkoutOfType(t)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = c.danger)
                    ) {
                        Text(S.overwriteConfirm, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingTypeForStart = null }) {
                        Text(S.cancel, color = c.textMuted)
                    }
                }
            )
        }


        // ── En cours ──────────────────────────────────────────────────────────
        if (inProgressWorkouts.isNotEmpty()) {
            SectionLabel(S.inProgress, c)
            Spacer(Modifier.height(8.dp))
            inProgressWorkouts.forEach { w ->
                InProgressCard(
                    workout = w,
                    c = c,
                    onClick = { onResumeWorkout(w.id) },
                    onDelete = { onDeleteInProgressWorkout(w.id) }
                )
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(20.dp))
        }

        // ── Récents ───────────────────────────────────────────────────────────
        if (recentWorkouts.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel(S.recent, c)
                TextButton(onClick = onHistory) {
                    Text(S.seeAll, color = c.accent, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            recentWorkouts.forEach { w ->
                RecentCard(w, c) { onOpenWorkout(w.id) }
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(24.dp))
        }

        // ── Paramètres ────────────────────────────────────────────────────────
        SectionLabel(S.settings, c)
        Spacer(Modifier.height(12.dp))
        // ─ Supprimer les pubs (vrai achat) ───────────────────────────────────
        if (!adFree) {
            Surface(
                onClick = onPurchaseRemoveAds,
                shape = RoundedCornerShape(12.dp), color = c.surface,
                border = BorderStroke(1.dp, c.accent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(S.removeAdsPrice, color = c.accent, fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold)
                        Text(S.removeAdsPriceDesc, color = c.textMuted, fontSize = 11.sp)
                    }
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(12.dp), color = c.surface,
                border = BorderStroke(1.dp, c.accent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(S.adsRemoved, color = c.accent, fontSize = 15.sp)
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        // ─ Bloc Thème ─────────────────────────────────────────────────────────
        Surface(
            shape = RoundedCornerShape(12.dp), color = c.surface,
            border = BorderStroke(1.dp, c.border), modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Mode sombre
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(S.darkMode, color = c.text, fontSize = 15.sp)
                    Switch(
                        checked = darkTheme, onCheckedChange = { onToggleTheme() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = c.accent, checkedTrackColor = c.accentDim,
                            uncheckedThumbColor = c.textMuted, uncheckedTrackColor = if (c.dark,
 uncheckedBorderColor = if (c.dark) androidx.compose.ui.graphics.Color(0xFF3A3A3A) else androidx.compose.ui.graphics.Color(0xFFCCCCCC)
) androidx.compose.ui.graphics.Color(0xFF3A3A3A) else androidx.compose.ui.graphics.Color(0xFFCCCCCC)
                        )
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        // ─ Bloc Notification (séparé du thème) ───────────────────────────────
        Surface(
            shape = RoundedCornerShape(12.dp), color = c.surface,
            border = BorderStroke(1.dp, c.border), modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(S.chronoNotif, color = c.text, fontSize = 15.sp)
                    Text(
                        S.chronoNotifDesc,
                        color = c.textMuted, fontSize = 11.sp
                    )
                }
                Switch(
                    checked = chronoNotifEnabled, onCheckedChange = { onToggleChronoNotif() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = c.accent, checkedTrackColor = c.accentDim,
                        uncheckedThumbColor = c.textMuted, uncheckedTrackColor = if (c.dark,
 uncheckedBorderColor = if (c.dark) androidx.compose.ui.graphics.Color(0xFF3A3A3A) else androidx.compose.ui.graphics.Color(0xFFCCCCCC)
) androidx.compose.ui.graphics.Color(0xFF3A3A3A) else androidx.compose.ui.graphics.Color(0xFFCCCCCC),
                        uncheckedBorderColor = if (c.dark) androidx.compose.ui.graphics.Color(0xFF3A3A3A) else androidx.compose.ui.graphics.Color(0xFFCCCCCC)
                    )
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        // ─ Export / Import ────────────────────────────────────────────────────
        SettingButton("↑", S.backupData, c, onExport)
        Spacer(Modifier.height(8.dp))
        SettingButton("↓", S.restoreData, c, onImport)
        Spacer(Modifier.height(8.dp))

        // ─ Langue ─────────────────────────────────────────────────────────────
        var showLangDialog by remember { mutableStateOf(false) }
        val langLabel = when (language) {
            "fr" -> "Français"
            "en" -> "English"
            else -> S.languageAuto
        }
        SettingButton("🌐", "${S.language} · $langLabel", c) { showLangDialog = true }
        if (showLangDialog) {
            LanguagePickerDialog(
                current = language,
                c = c,
                onPick = { code ->
                    showLangDialog = false
                    onChangeLang(code)
                },
                onDismiss = { showLangDialog = false }
            )
        }

        // ─ Bouton test debug-only (toggle adFree sans achat) ─────────────────
        if (isDebug) {
            Spacer(Modifier.height(8.dp))
            Surface(
                onClick = onToggleAdFree,
                shape = RoundedCornerShape(12.dp), color = c.surface,
                border = BorderStroke(1.dp, c.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            if (adFree) S.adsRemoved else S.removeAds,
                            color = c.textMuted, fontSize = 14.sp
                        )
                        Text(
                            if (adFree) S.adsRemovedTestHint else S.removeAdsTestHint,
                            color = c.textMuted, fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = adFree, onCheckedChange = { onToggleAdFree() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = c.accent, checkedTrackColor = c.accentDim,
                            uncheckedThumbColor = c.textMuted, uncheckedTrackColor = if (c.dark,
 uncheckedBorderColor = if (c.dark) androidx.compose.ui.graphics.Color(0xFF3A3A3A) else androidx.compose.ui.graphics.Color(0xFFCCCCCC)
) androidx.compose.ui.graphics.Color(0xFF3A3A3A) else androidx.compose.ui.graphics.Color(0xFFCCCCCC)
                        )
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        // ── Publicité ─────────────────────────────────────────────────────────
        if (!adFree) {
            AdBanner(Modifier.fillMaxWidth())
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ─── Composants ───────────────────────────────────────────────────────────────

@Composable
fun SectionLabel(text: String, c: GainzThemeColors) {
    Text(
        text.uppercase(), color = c.textMuted, fontSize = 11.sp,
        fontWeight = FontWeight.Bold, letterSpacing = 1.sp
    )
}

@Composable
fun InProgressCard(
    workout: Workout,
    c: GainzThemeColors,
    onClick: () -> Unit,
    onDelete: () -> Unit = {}
) {
    Surface(
        onClick = onClick, shape = RoundedCornerShape(12.dp), color = c.accentDim,
        border = BorderStroke(1.5.dp, c.accent), modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val typeColors = GainzThemeColors(c.dark, type = workout.type)
                    val typeAccent = typeColors.accent
                    Box(Modifier.size(8.dp).background(typeAccent, RoundedCornerShape(4.dp)))
                    Text(
                        workout.title.ifBlank { S.untitledWorkout },
                        color = typeAccent, fontSize = 15.sp, fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(formatDisplayDate(workout.startedAt), color = c.textSec, fontSize = 12.sp)
                val inProgressCount = when (workout.type) {
                    WorkoutType.MUSCULATION -> workout.exercises.size
                    WorkoutType.CARDIO -> workout.cardioExercises.size
                    WorkoutType.CIRCUIT -> workout.circuitExercises.size
                }
                Text(
                    S.exerciseCountInProgress(inProgressCount),
                    color = c.textSec, fontSize = 12.sp
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = S.deleteInProgressDesc,
                        tint = c.danger
                    )
                }
                Text("▶", color = c.accent, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun SettingButton(icon: String, label: String, c: GainzThemeColors, onClick: () -> Unit) {
    Surface(
        onClick = onClick, shape = RoundedCornerShape(12.dp), color = c.surface,
        border = BorderStroke(1.dp, c.border), modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = c.text, fontSize = 15.sp)
        }
    }
}

@Composable
fun RecentCard(workout: Workout, c: GainzThemeColors, onClick: () -> Unit) {
    Surface(
        onClick = onClick, shape = RoundedCornerShape(12.dp), color = c.surface,
        border = BorderStroke(1.dp, c.border), modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val typeAccent = GainzThemeColors(c.dark, type = workout.type).accent
            Box(Modifier.size(8.dp).background(typeAccent, RoundedCornerShape(4.dp)))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    workout.title.ifBlank { S.untitled }, color = c.text,
                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(3.dp))
                Text(formatDisplayDate(workout.startedAt), color = c.textMuted, fontSize = 12.sp)
                Text(workoutTypeAndCount(workout), color = c.textMuted, fontSize = 12.sp)
            }
            Text("›", color = if(c.dark) Color(0xFF666666) else Color(0xFFAAAAAA), fontSize = 22.sp)
        }
    }
}


@Composable
fun LanguagePickerDialog(
    current: String,
    c: GainzThemeColors,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Liste extensible : ajouter simplement une paire (code, label) pour une nouvelle langue.
    val options: List<Pair<String, String>> = listOf(
        "auto" to S.languageAuto,
        "fr"   to "Français",
        "en"   to "English"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        title = { Text(S.chooseLanguage, color = c.text) },
        text = {
            Column {
                options.forEach { (code, label) ->
                    Row(
                        Modifier.fillMaxWidth()
                            .clickable { onPick(code) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = code == current,
                            onClick = { onPick(code) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = c.accent,
                                unselectedColor = c.textMuted
                            )
                        )
                        Text(label, color = c.text, fontSize = 15.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(S.cancel, color = c.accent)
            }
        }
    )
}


@Composable
fun WorkoutTypeDropdown(
    selected: WorkoutType,
    onSelected: (WorkoutType) -> Unit,
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val c = GainzThemeColors(darkTheme)
    var expanded by remember { mutableStateOf(false) }

    val label = when (selected) {
        WorkoutType.MUSCULATION -> S.workoutTypeMusculation
        WorkoutType.CARDIO -> S.workoutTypeCardio
        WorkoutType.CIRCUIT -> S.workoutTypeCircuit
    }
    val accent = c.copy(type = selected).accent

    Box(modifier) {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(14.dp),
            color = c.surface,
            border = BorderStroke(1.5.dp, accent),
            modifier = Modifier.fillMaxWidth().height(58.dp)
        ) {
            Row(
                Modifier.fillMaxSize().padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(S.selectWorkoutType, color = accent, fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text(label, color = accent, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                Text("\u25BC", color = accent, fontSize = 11.sp)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = c.surface
        ) {
            WorkoutType.entries.forEach { type ->
                val typeLabel = when (type) {
                    WorkoutType.MUSCULATION -> S.workoutTypeMusculation
                    WorkoutType.CARDIO -> S.workoutTypeCardio
                    WorkoutType.CIRCUIT -> S.workoutTypeCircuit
                }
                val typeAccent = c.accent
                DropdownMenuItem(
                    text = {
                        Text(
                            typeLabel,
                            color = if (type == selected) typeAccent else c.text,
                            fontWeight = if (type == selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelected(type)
                    }
                )
            }
        }
    }
}



/** Affiche le type + le décompte adapté (exos pour muscu, exos cardio pour cardio, exos×tours pour circuit). */
private fun workoutTypeAndCount(workout: Workout): String {
    val typeLabel = when (workout.type) {
        WorkoutType.MUSCULATION -> S.workoutTypeMusculation
        WorkoutType.CARDIO -> S.workoutTypeCardio
        WorkoutType.CIRCUIT -> S.workoutTypeCircuit
    }
    val count = when (workout.type) {
        WorkoutType.MUSCULATION -> S.exerciseCount(workout.exercises.size)
        WorkoutType.CARDIO -> S.exerciseCount(workout.cardioExercises.size)
        WorkoutType.CIRCUIT -> S.exerciseCount(workout.circuitExercises.size)
    }
    return "$typeLabel · $count"
}

fun formatDisplayDate(iso: String): String = try {
    val instant = Instant.parse(iso)
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val days = S.daysShort
    val months = S.monthsShort
    S.dateShort(days[local.dayOfWeek.ordinal], local.dayOfMonth, months[local.monthNumber - 1],
        local.hour.toString().padStart(2, '0'), local.minute.toString().padStart(2, '0'))
} catch (e: Exception) { iso }

fun formatDisplayDateFull(iso: String): String = try {
    val instant = Instant.parse(iso)
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val days = S.daysLong
    val months = S.monthsShort
    S.dateAtTime(days[local.dayOfWeek.ordinal], local.dayOfMonth, months[local.monthNumber - 1],
        local.hour.toString().padStart(2, '0'), local.minute.toString().padStart(2, '0'))
} catch (e: Exception) { iso }
