package com.gainznote.ui.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.gainznote.model.Workout
import com.gainznote.repository.WorkoutRepository
import com.gainznote.ui.theme.GainzThemeColors
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun HomeScreen(
    repo: WorkoutRepository,
    darkTheme: Boolean,
    blackBg: Boolean = false,
    chronoNotifEnabled: Boolean = false,
    onToggleTheme: () -> Unit,
    onToggleBlackBg: () -> Unit = {},
    onToggleChronoNotif: () -> Unit = {},
    onNewWorkout: () -> Unit,
    onHistory: () -> Unit,
    onOpenWorkout: (String) -> Unit,
    onResumeWorkout: (String) -> Unit = {},
    onExport: () -> Unit = {},
    onImport: () -> Unit = {},
    refreshKey: Int = 0
) {
    val c = GainzThemeColors(darkTheme, blackBg)
    var recentWorkouts by remember { mutableStateOf<List<Workout>>(emptyList()) }
    var inProgressWorkouts by remember { mutableStateOf<List<Workout>>(emptyList()) }

    // Rechargement à chaque fois que refreshKey change (ex: après import)
    LaunchedEffect(refreshKey) {
        recentWorkouts = repo.getFinishedWorkouts().take(3)
        inProgressWorkouts = repo.getInProgressWorkouts()
    }

    Column(
        Modifier.fillMaxSize().background(c.background).safeDrawingPadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text("GainzNote", color = c.accent, fontSize = 34.sp,
            fontWeight = FontWeight.Black, letterSpacing = (-1).sp)
        Text("Ton carnet de musculation", color = c.textMuted, fontSize = 13.sp)
        Spacer(Modifier.height(28.dp))

        Button(
            onClick = onNewWorkout,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = c.accent)
        ) {
            Text(
                "+ Nouvel entraînement",
                color = if (darkTheme) Color.Black else Color.White,
                fontSize = 17.sp, fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(32.dp))

        // ── En cours ──────────────────────────────────────────────────────────
        if (inProgressWorkouts.isNotEmpty()) {
            SectionLabel("En cours", c)
            Spacer(Modifier.height(8.dp))
            inProgressWorkouts.forEach { w ->
                InProgressCard(workout = w, c = c, onClick = { onResumeWorkout(w.id) })
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
                SectionLabel("Récents", c)
                TextButton(onClick = onHistory) {
                    Text("Voir tout →", color = c.accent, fontSize = 13.sp)
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
        SectionLabel("Paramètres", c)
        Spacer(Modifier.height(12.dp))

        Surface(
            shape = RoundedCornerShape(12.dp), color = c.surface,
            border = BorderStroke(1.dp, c.border), modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Mode sombre — switch ancré à droite avec fillMaxWidth sur la Row
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mode sombre", color = c.text, fontSize = 15.sp)
                    Switch(
                        checked = darkTheme, onCheckedChange = { onToggleTheme() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = c.accent,
                            checkedTrackColor = c.accentDim
                        )
                    )
                }

                // Fond noir — visible seulement en mode sombre, sans sous-titre
                if (darkTheme) {
                    HorizontalDivider(
                        color = c.border, thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Row(
                        Modifier.fillMaxWidth().padding(start = 32.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Fond noir", color = c.textSec, fontSize = 14.sp)
                        Switch(
                            checked = blackBg, onCheckedChange = { onToggleBlackBg() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = c.accent,
                                checkedTrackColor = c.accentDim
                            )
                        )
                    }
                }

                // Notification chronomètre
                HorizontalDivider(
                    color = c.border, thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f).padding(end = 8.dp)) {
                        Text("Notification chronomètre", color = c.text, fontSize = 15.sp)
                        Text(
                            "Affiche le temps de repos dans les notifications",
                            color = c.textMuted, fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = chronoNotifEnabled, onCheckedChange = { onToggleChronoNotif() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = c.accent,
                            checkedTrackColor = c.accentDim
                        )
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        SettingButton("↑", "Sauvegarder les données", c, onExport)
        Spacer(Modifier.height(8.dp))
        SettingButton("↓", "Restaurer les données", c, onImport)
        Spacer(Modifier.height(40.dp))
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
fun InProgressCard(workout: Workout, c: GainzThemeColors, onClick: () -> Unit) {
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
                    Box(Modifier.size(8.dp).background(c.accent, RoundedCornerShape(4.dp)))
                    Text(
                        workout.title.ifBlank { "Entraînement sans titre" },
                        color = c.accent, fontSize = 15.sp, fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(formatDisplayDate(workout.startedAt), color = c.textSec, fontSize = 12.sp)
                Text(
                    "${workout.exercises.size} exercice(s) · En cours…",
                    color = c.textSec, fontSize = 12.sp
                )
            }
            Text("▶", color = c.accent, fontSize = 18.sp)
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(icon, color = c.accent, fontSize = 18.sp)
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
            Column(Modifier.weight(1f)) {
                Text(
                    workout.title.ifBlank { "Sans titre" }, color = c.text,
                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(3.dp))
                Text(formatDisplayDate(workout.startedAt), color = c.textMuted, fontSize = 12.sp)
                Text("${workout.exercises.size} exercice(s)", color = c.textMuted, fontSize = 12.sp)
            }
            Text("›", color = c.textMuted, fontSize = 22.sp)
        }
    }
}

// ─── Formatage des dates ──────────────────────────────────────────────────────

fun formatDisplayDate(iso: String): String = try {
    val instant = Instant.parse(iso)
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val days = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
    val months = listOf("jan", "fév", "mar", "avr", "mai", "jun", "jul", "aoû", "sep", "oct", "nov", "déc")
    "${days[local.dayOfWeek.ordinal]} ${local.dayOfMonth} ${months[local.monthNumber - 1]} · ${
        local.hour.toString().padStart(2, '0')
    }:${local.minute.toString().padStart(2, '0')}"
} catch (e: Exception) { iso }

fun formatDisplayDateFull(iso: String): String = try {
    val instant = Instant.parse(iso)
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val days = listOf("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche")
    val months = listOf("jan", "fév", "mar", "avr", "mai", "jun", "jul", "aoû", "sep", "oct", "nov", "déc")
    "${days[local.dayOfWeek.ordinal]} ${local.dayOfMonth} ${months[local.monthNumber - 1]} à ${
        local.hour.toString().padStart(2, '0')
    }:${local.minute.toString().padStart(2, '0')}"
} catch (e: Exception) { iso }
