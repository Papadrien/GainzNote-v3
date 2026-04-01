package com.gainznote.ui

import androidx.compose.runtime.*
import com.gainznote.db.DatabaseDriverFactory
import com.gainznote.repository.WorkoutRepository
import com.gainznote.ui.detail.DetailScreen
import com.gainznote.ui.history.HistoryScreen
import com.gainznote.ui.home.HomeScreen
import com.gainznote.ui.theme.GainzTheme
import com.gainznote.ui.workout.WorkoutScreen
import kotlinx.coroutines.launch

sealed class Screen {
    data object Home : Screen()
    data class Workout(val templateId: String? = null, val resumeId: String? = null) : Screen()
    data object History : Screen()
    data class Detail(val workoutId: String) : Screen()
}

@Composable
fun App(
    driverFactory: DatabaseDriverFactory,
    onExit: () -> Unit = {},
    onExportReady: (String) -> Unit = {},
    onImportRequest: ((String) -> Unit) -> Unit = {},
    // Callbacks notification chrono (implémentés côté Android)
    onChronoStart: (Long) -> Unit = {},
    onChronoStop: () -> Unit = {}
) {
    val repo = remember { WorkoutRepository(driverFactory) }
    val backStack = remember { mutableStateListOf<Screen>(Screen.Home) }
    val currentScreen = backStack.last()
    var darkTheme by remember { mutableStateOf(true) }
    var blackBg by remember { mutableStateOf(false) }
    var chronoNotifEnabled by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Clé qui s'incrémente après chaque import pour forcer le rechargement de HomeScreen
    var refreshKey by remember { mutableStateOf(0) }

    SystemBarsEffect(darkTheme)

    fun navigateTo(screen: Screen) = backStack.add(screen)
    fun navigateBack() { if (backStack.size > 1) backStack.removeLast() else onExit() }

    BackHandler(enabled = backStack.size > 1) { navigateBack() }

    GainzTheme(dark = darkTheme, blackBg = blackBg) {
        when (val s = currentScreen) {
            Screen.Home -> HomeScreen(
                repo = repo,
                darkTheme = darkTheme,
                blackBg = blackBg,
                chronoNotifEnabled = chronoNotifEnabled,
                onToggleTheme = { darkTheme = !darkTheme; if (!darkTheme) blackBg = false },
                onToggleBlackBg = { blackBg = !blackBg },
                onToggleChronoNotif = { chronoNotifEnabled = !chronoNotifEnabled },
                onNewWorkout = { navigateTo(Screen.Workout()) },
                onHistory = { navigateTo(Screen.History) },
                onOpenWorkout = { id -> navigateTo(Screen.Detail(id)) },
                onResumeWorkout = { id -> navigateTo(Screen.Workout(resumeId = id)) },
                onExport = { scope.launch { val json = repo.exportJson(); onExportReady(json) } },
                onImport = {
                    onImportRequest { json ->
                        scope.launch {
                            repo.importJson(json)
                            // Incrémenter refreshKey pour recharger HomeScreen après import
                            refreshKey++
                        }
                    }
                },
                refreshKey = refreshKey
            )
            is Screen.Workout -> WorkoutScreen(
                repo = repo,
                darkTheme = darkTheme,
                blackBg = blackBg,
                templateId = s.templateId,
                resumeId = s.resumeId,
                onBack = { navigateBack() },
                onFinished = { backStack.clear(); backStack.add(Screen.Home) },
                chronoNotifEnabled = chronoNotifEnabled,
                onChronoStart = onChronoStart,
                onChronoStop = onChronoStop
            )
            Screen.History -> HistoryScreen(
                repo = repo,
                darkTheme = darkTheme,
                onBack = { navigateBack() },
                onOpenDetail = { id -> navigateTo(Screen.Detail(id)) },
                onUseAsTemplate = { id ->
                    backStack.clear(); backStack.add(Screen.Home)
                    navigateTo(Screen.Workout(templateId = id))
                }
            )
            is Screen.Detail -> DetailScreen(
                repo = repo,
                darkTheme = darkTheme,
                blackBg = blackBg,
                workoutId = s.workoutId,
                onBack = { navigateBack() },
                onUseAsTemplate = { id ->
                    backStack.clear(); backStack.add(Screen.Home)
                    navigateTo(Screen.Workout(templateId = id))
                },
                onDeleted = { navigateBack() }
            )
        }
    }
}

@Composable
expect fun BackHandler(enabled: Boolean, onBack: () -> Unit)
