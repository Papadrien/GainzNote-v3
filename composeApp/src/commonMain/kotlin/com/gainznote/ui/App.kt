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
    data class Workout(val templateId: String? = null) : Screen()
    data object History : Screen()
    data class Detail(val workoutId: String) : Screen()
}

@Composable
fun App(
    driverFactory: DatabaseDriverFactory,
    onExit: () -> Unit = {},
    onExportReady: (String) -> Unit = {},
    onImportRequest: ((String) -> Unit) -> Unit = {}
) {
    val repo = remember { WorkoutRepository(driverFactory) }
    val backStack = remember { mutableStateListOf<Screen>(Screen.Home) }
    val currentScreen = backStack.last()
    var darkTheme by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Contrôle couleur status bar selon le thème
    SystemBarsEffect(darkTheme)

    fun navigateTo(screen: Screen) = backStack.add(screen)
    fun navigateBack() { if (backStack.size > 1) backStack.removeLast() else onExit() }

    BackHandler(enabled = backStack.size > 1) { navigateBack() }

    GainzTheme(dark = darkTheme) {
        when (val s = currentScreen) {
            Screen.Home -> HomeScreen(
                repo = repo,
                darkTheme = darkTheme,
                onToggleTheme = { darkTheme = !darkTheme },
                onNewWorkout = { navigateTo(Screen.Workout()) },
                onHistory = { navigateTo(Screen.History) },
                onOpenWorkout = { id -> navigateTo(Screen.Detail(id)) },
                onExport = {
                    scope.launch {
                        val json = repo.exportJson()
                        onExportReady(json)
                    }
                },
                onImport = {
                    onImportRequest { json ->
                        scope.launch { repo.importJson(json) }
                    }
                }
            )
            is Screen.Workout -> WorkoutScreen(
                repo = repo,
                darkTheme = darkTheme,
                templateId = s.templateId,
                onBack = { navigateBack() },
                onFinished = { backStack.clear(); backStack.add(Screen.Home) }
            )
            Screen.History -> HistoryScreen(
                repo = repo,
                darkTheme = darkTheme,
                onBack = { navigateBack() },
                onOpenDetail = { id -> navigateTo(Screen.Detail(id)) },
                onUseAsTemplate = { id ->
                    backStack.clear(); backStack.add(Screen.Home)
                    navigateTo(Screen.Workout(id))
                }
            )
            is Screen.Detail -> DetailScreen(
                repo = repo,
                darkTheme = darkTheme,
                workoutId = s.workoutId,
                onBack = { navigateBack() },
                onUseAsTemplate = { id ->
                    backStack.clear(); backStack.add(Screen.Home)
                    navigateTo(Screen.Workout(id))
                },
                onDeleted = { navigateBack() }
            )
        }
    }
}

@Composable
expect fun BackHandler(enabled: Boolean, onBack: () -> Unit)
