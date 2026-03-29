package com.gainznote.ui

import androidx.compose.runtime.*
import com.gainznote.db.DatabaseDriverFactory
import com.gainznote.repository.WorkoutRepository
import com.gainznote.ui.detail.DetailScreen
import com.gainznote.ui.history.HistoryScreen
import com.gainznote.ui.home.HomeScreen
import com.gainznote.ui.theme.GainzTheme
import com.gainznote.ui.workout.WorkoutScreen

sealed class Screen {
    data object Home : Screen()
    data class Workout(val templateId: String? = null) : Screen()
    data object History : Screen()
    data class Detail(val workoutId: String) : Screen()
}

@Composable
fun App(driverFactory: DatabaseDriverFactory) {
    val repo = remember { WorkoutRepository(driverFactory) }
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    var darkTheme by remember { mutableStateOf(true) }

    GainzTheme(dark = darkTheme) {
        when (val s = screen) {
            Screen.Home -> HomeScreen(
                repo = repo,
                darkTheme = darkTheme,
                onToggleTheme = { darkTheme = !darkTheme },
                onNewWorkout = { screen = Screen.Workout() },
                onHistory = { screen = Screen.History },
                onOpenWorkout = { id -> screen = Screen.Detail(id) }
            )
            is Screen.Workout -> WorkoutScreen(
                repo = repo,
                templateId = s.templateId,
                onFinished = { screen = Screen.Home }
            )
            Screen.History -> HistoryScreen(
                repo = repo,
                onBack = { screen = Screen.Home },
                onOpenDetail = { id -> screen = Screen.Detail(id) },
                onUseAsTemplate = { id -> screen = Screen.Workout(id) }
            )
            is Screen.Detail -> DetailScreen(
                repo = repo,
                workoutId = s.workoutId,
                onBack = { screen = Screen.History },
                onUseAsTemplate = { id -> screen = Screen.Workout(id) },
                onDeleted = { screen = Screen.History }
            )
        }
    }
}
