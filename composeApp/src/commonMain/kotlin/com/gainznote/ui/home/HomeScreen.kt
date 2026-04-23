package com.gainznote.ui.home

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
import com.gainznote.model.Workout
import com.gainznote.repository.WorkoutRepository
import com.gainznote.ui.theme.GainzThemeColors
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.gainznote.i18n.S
import com.gainznote.ui.ads.AdBanner

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
    onDeleteInProgressWorkout: (String) -> Unit = {},
    onExport: () -> Unit = {},
    onImport: () -> Unit = {},
    adFree: Boolean = false,
    isDebug: Boolean = false,
    onPurchaseRemoveAds: () -> Unit = {},
    onToggleAdFree: () -> Unit = {},
    language: String = "auto",
    onCycleLang: () -> Unit = {},
    refreshKey: Int = 0
) {
    val c = GainzThemeColors(darkTheme, blackBg)
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

        Button(
            onClick = onNewWorkout, modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = c.accent)
        ) {
            Text(
                S.newWorkout,
                color = if (darkTheme) Color.Black else Color.White,
                fontSize = 17.sp, fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(32.dp))

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
                            checkedThumbColor = c.accent, checkedTrackColor = c.accentDim
                        )
                    )
                }
                // Fond noir — visible seulement en mode sombre
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
                        Text(S.blackBg, color = c.textSec, fontSize = 14.sp)
                        Switch(
                            checked = blackBg, onCheckedChange = { onToggleBlackBg() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = c.accent, checkedTrackColor = c.accentDim
                            )
                        )
                    }
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
                        checkedThumbColor = c.accent, checkedTrackColor = c.accentDim
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
        val langLabel = when (language) { "fr" -> "Français"; "en" -> "English"; else -> "Auto" }
        SettingButton("🌐", "${S.language} · $langLabel", c, onCycleLang)
        Spacer(Modifier.height(8.dp))

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
                    Text("\uD83D\uDEAB", fontSize = 18.sp)
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
                    Text("\u2705", fontSize = 18.sp)
                    Text(S.adsRemoved, color = c.accent, fontSize = 15.sp)
                }
            }
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
                            checkedThumbColor = c.accent, checkedTrackColor = c.accentDim
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
                    Box(Modifier.size(8.dp).background(c.accent, RoundedCornerShape(4.dp)))
                    Text(
                        workout.title.ifBlank { S.untitledWorkout },
                        color = c.accent, fontSize = 15.sp, fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(formatDisplayDate(workout.startedAt), color = c.textSec, fontSize = 12.sp)
                Text(
                    S.exerciseCountInProgress(workout.exercises.size),
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
                    workout.title.ifBlank { S.untitled }, color = c.text,
                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(3.dp))
                Text(formatDisplayDate(workout.startedAt), color = c.textMuted, fontSize = 12.sp)
                Text(S.exerciseCount(workout.exercises.size), color = c.textMuted, fontSize = 12.sp)
            }
            Text("›", color = c.textMuted, fontSize = 22.sp)
        }
    }
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
