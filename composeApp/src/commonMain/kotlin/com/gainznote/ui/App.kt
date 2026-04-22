package com.gainznote.ui

import androidx.compose.runtime.*
import com.gainznote.db.DatabaseDriverFactory
import com.gainznote.i18n.Lang
import com.gainznote.i18n.S
import com.gainznote.i18n.getSystemLanguage
import com.gainznote.model.AppSettings
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
    onChronoStop: () -> Unit = {},
    // Callback interstitiel pub : appelé quand un entraînement se termine.
    // Le paramètre est un callback à invoquer quand la pub est fermée (ou si pas de pub).
    onShowInterstitial: (onDismissed: () -> Unit) -> Unit = { it() }
) {
    val repo = remember { WorkoutRepository(driverFactory) }
    val backStack = remember { mutableStateListOf<Screen>(Screen.Home) }
    val currentScreen = backStack.last()
    var darkTheme by remember { mutableStateOf(true) }
    var blackBg by remember { mutableStateOf(false) }
    var chronoNotifEnabled by remember { mutableStateOf(false) }
    var adFree by remember { mutableStateOf(false) }
    var language by remember { mutableStateOf("auto") }
    var settingsLoaded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val settings = repo.getAppSettings()
        darkTheme = settings.darkTheme
        blackBg = settings.blackBg
        chronoNotifEnabled = settings.chronoNotifEnabled
        adFree = settings.adFree
        language = settings.language
        // Initialiser la langue
        if (language == "auto") S.initFromSystem(getSystemLanguage()) else S.setLang(if (language == "fr") Lang.FR else Lang.EN)
        settingsLoaded = true
    }

    fun persistSettings() {
        if (!settingsLoaded) return
        scope.launch {
            repo.saveAppSettings(
                AppSettings(
                    darkTheme = darkTheme,
                    blackBg = blackBg,
                    chronoNotifEnabled = chronoNotifEnabled,
                    adFree = adFree,
                    language = language
                )
            )
        }
    }

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
                onToggleTheme = {
                    darkTheme = !darkTheme
                    if (!darkTheme) blackBg = false
                    persistSettings()
                },
                onToggleBlackBg = {
                    blackBg = !blackBg
                    persistSettings()
                },
                onToggleChronoNotif = {
                    chronoNotifEnabled = !chronoNotifEnabled
                    persistSettings()
                },
                onNewWorkout = { navigateTo(Screen.Workout()) },
                onHistory = { navigateTo(Screen.History) },
                onOpenWorkout = { id -> navigateTo(Screen.Detail(id)) },
                onResumeWorkout = { id -> navigateTo(Screen.Workout(resumeId = id)) },
                onDeleteInProgressWorkout = { id ->
                    scope.launch {
                        repo.deleteWorkout(id)
                        refreshKey++
                    }
                },
                onExport = { scope.launch { val json = repo.exportJson(); onExportReady(json) } },
                onImport = {
                    onImportRequest { json ->
                        scope.launch {
                            repo.importJson(json)
                            // Forcer un retour à l'accueil pour afficher immédiatement les données restaurées
                            backStack.clear()
                            backStack.add(Screen.Home)
                            // Incrémenter refreshKey pour recharger HomeScreen après import
                            refreshKey++
                        }
                    }
                },
                adFree = adFree,
                onToggleAdFree = {
                    adFree = !adFree
                    persistSettings()
                },
                language = language,
                onCycleLang = {
                    language = when (language) { "auto" -> "fr"; "fr" -> "en"; else -> "auto" }
                    if (language == "auto") S.initFromSystem(getSystemLanguage()) else S.setLang(if (language == "fr") Lang.FR else Lang.EN)
                    persistSettings()
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
                onFinished = {
                    if (adFree) {
                        backStack.clear()
                        backStack.add(Screen.Home)
                    } else {
                        onShowInterstitial {
                            backStack.clear()
                            backStack.add(Screen.Home)
                        }
                    }
                },
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
