package com.junade.gainznote.ui

import androidx.compose.runtime.*
import com.junade.gainznote.db.DatabaseDriverFactory
import com.junade.gainznote.i18n.Lang
import com.junade.gainznote.i18n.S
import com.junade.gainznote.i18n.getSystemLanguage
import com.junade.gainznote.model.AppSettings
import com.junade.gainznote.model.WorkoutType
import com.junade.gainznote.repository.WorkoutRepository
import com.junade.gainznote.ui.cardio.CardioSetupScreen
import com.junade.gainznote.ui.circuit.CircuitSetupScreen
import com.junade.gainznote.ui.circuit.CircuitWorkoutScreen
import com.junade.gainznote.ui.detail.DetailScreen
import com.junade.gainznote.ui.history.HistoryScreen
import com.junade.gainznote.ui.home.HomeScreen
import com.junade.gainznote.ui.home.PrivacyPolicyScreen
import com.junade.gainznote.ui.theme.GainzTheme
import com.junade.gainznote.ui.workout.WorkoutScreen
import kotlinx.coroutines.launch

sealed class Screen {
    data object Home : Screen()
    data class Workout(val templateId: String? = null, val resumeId: String? = null) : Screen()
    data class CardioSetup(val templateId: String? = null, val resumeId: String? = null) : Screen()
    data class CircuitSetup(
        val templateId: String? = null,
        val resumeId: String? = null,
        val skipSetup: Boolean = false
    ) : Screen()
    data class CircuitWorkout(val workoutId: String) : Screen()
    data object History : Screen()
    data object PrivacyPolicy : Screen()
    data class Detail(val workoutId: String) : Screen()
}

@Composable
fun App(
    driverFactory: DatabaseDriverFactory,
    onExit: () -> Unit = {},
    onExportReady: (String) -> Unit = {},
    onImportRequest: ((String) -> Unit) -> Unit = {},
    // Callbacks notification chrono (implémentés côté Android)
    onRequestNotifPermission: (onResult: (Boolean) -> Unit) -> Unit = { it(true) },
    onChronoStart: (Long) -> Unit = {},
    onChronoStop: () -> Unit = {},
    // Callback interstitiel pub : appelé quand un entraînement se termine.
    // Le paramètre est un callback à invoquer quand la pub est fermée (ou si pas de pub).
    onShowInterstitial: (onDismissed: () -> Unit) -> Unit = { it() },
    isDebug: Boolean = false,
    onPurchaseRemoveAds: () -> Unit = {}
) {
    val repo = remember { WorkoutRepository(driverFactory) }
    val backStack = remember { mutableStateListOf<Screen>(Screen.Home) }
    val currentScreen = backStack.last()
    var darkTheme by remember { mutableStateOf(true) }
    
    var chronoNotifEnabled by remember { mutableStateOf(false) }
    var adFree by remember { mutableStateOf(false) }
    var language by remember { mutableStateOf("auto") }
    var lastWorkoutType by remember { mutableStateOf(WorkoutType.MUSCULATION) }
    var settingsLoaded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val settings = repo.getAppSettings()
        darkTheme = settings.darkTheme
        chronoNotifEnabled = settings.chronoNotifEnabled
        adFree = settings.adFree
        language = settings.language
        lastWorkoutType = settings.lastWorkoutType
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
                    chronoNotifEnabled = chronoNotifEnabled,
                    adFree = adFree,
                    language = language,
                    lastWorkoutType = lastWorkoutType
                )
            )
        }
    }

    // Clé qui s'incrémente après chaque import pour forcer le rechargement de HomeScreen
    var refreshKey by remember { mutableStateOf(0) }

    SystemBarsEffect(darkTheme)

    fun navigateTo(screen: Screen) = backStack.add(screen)
    fun navigateBack() { if (backStack.size > 1) backStack.removeLast() else onExit() }

    fun onWorkoutFinished() {
        if (adFree) {
            backStack.clear()
            backStack.add(Screen.Home)
        } else {
            onShowInterstitial {
                backStack.clear()
                backStack.add(Screen.Home)
            }
        }
    }

    BackHandler(enabled = backStack.size > 1) { navigateBack() }

    GainzTheme(dark = darkTheme) {
        when (val s = currentScreen) {
            Screen.Home -> HomeScreen(
                repo = repo,
                darkTheme = darkTheme,
                chronoNotifEnabled = chronoNotifEnabled,
                onToggleTheme = {
                    darkTheme = !darkTheme
                    persistSettings()
                },
                onToggleChronoNotif = {
                    if (chronoNotifEnabled) {
                        // Désactivation — pas besoin de permission
                        chronoNotifEnabled = false
                        persistSettings()
                    } else {
                        // Activation — vérifier la permission d'abord
                        onRequestNotifPermission { granted ->
                            if (granted) {
                                chronoNotifEnabled = true
                                persistSettings()
                            }
                        }
                    }
                },
                selectedWorkoutType = lastWorkoutType,
                onSelectedWorkoutTypeChange = { t ->
                    lastWorkoutType = t
                    persistSettings()
                },
                onStartWorkoutOfType = { type ->
                    lastWorkoutType = type
                    persistSettings()
                    val dest: Screen = when (type) {
                        WorkoutType.MUSCULATION -> Screen.Workout()
                        WorkoutType.CARDIO -> Screen.CardioSetup()
                        WorkoutType.CIRCUIT -> Screen.CircuitSetup()
                    }
                    navigateTo(dest)
                },
                onHistory = { navigateTo(Screen.History) },
                onPrivacyPolicy = { navigateTo(Screen.PrivacyPolicy) },
                onOpenWorkout = { id -> navigateTo(Screen.Detail(id)) },
                onResumeWorkout = { id ->
                    scope.launch {
                        val w = repo.getWorkoutById(id)
                        val type = w?.type ?: com.junade.gainznote.model.WorkoutType.MUSCULATION
                        navigateTo(when (type) {
                            com.junade.gainznote.model.WorkoutType.MUSCULATION -> Screen.Workout(resumeId = id)
                            com.junade.gainznote.model.WorkoutType.CARDIO -> Screen.CardioSetup(resumeId = id)
                            com.junade.gainznote.model.WorkoutType.CIRCUIT -> Screen.CircuitWorkout(workoutId = id)
                        })
                    }
                },
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
                isDebug = isDebug,
                onPurchaseRemoveAds = onPurchaseRemoveAds,
                onToggleAdFree = {
                    adFree = !adFree
                    persistSettings()
                },
                language = language,
                onChangeLang = { newLang ->
                    language = newLang
                    if (newLang == "auto") S.initFromSystem(getSystemLanguage())
                    else S.setLang(if (newLang == "fr") Lang.FR else Lang.EN)
                    persistSettings()
                },
                refreshKey = refreshKey
            )
            is Screen.Workout -> WorkoutScreen(
                repo = repo,
                darkTheme = darkTheme,
                adFree = adFree,
                templateId = s.templateId,
                resumeId = s.resumeId,
                onBack = { navigateBack() },
                onFinished = { onWorkoutFinished() },
                chronoNotifEnabled = chronoNotifEnabled,
                onChronoStart = onChronoStart,
                onChronoStop = onChronoStop
            )
            Screen.PrivacyPolicy -> PrivacyPolicyScreen(
                darkTheme = darkTheme,
                onBack = { navigateBack() }
            )
            Screen.History -> HistoryScreen(
                repo = repo,
                darkTheme = darkTheme,
                onBack = { navigateBack() },
                onOpenDetail = { id -> navigateTo(Screen.Detail(id)) },
                onUseAsTemplate = { id ->
                    scope.launch {
                        val w = repo.getWorkoutById(id)
                        val type = w?.type ?: com.junade.gainznote.model.WorkoutType.MUSCULATION
                        backStack.clear(); backStack.add(Screen.Home)
                        navigateTo(when (type) {
                            com.junade.gainznote.model.WorkoutType.MUSCULATION -> Screen.Workout(templateId = id)
                            com.junade.gainznote.model.WorkoutType.CARDIO -> Screen.CardioSetup(templateId = id)
                            com.junade.gainznote.model.WorkoutType.CIRCUIT -> Screen.CircuitSetup(templateId = id)
                        })
                    }
                }
            )
            is Screen.CardioSetup -> CardioSetupScreen(
                repo = repo,
                darkTheme = darkTheme,
                templateId = s.templateId,
                resumeId = s.resumeId,
                adFree = adFree,
                onBack = { navigateBack() },
                onFinished = { onWorkoutFinished() }
            )
            is Screen.CircuitSetup -> CircuitSetupScreen(
                repo = repo,
                darkTheme = darkTheme,
                templateId = s.templateId,
                resumeId = s.resumeId,
                skipSetup = s.skipSetup,
                adFree = adFree,
                onBack = { navigateBack() },
                onStartWorkout = { workoutId ->
                    // Remplace l'écran de setup par l'écran de séance active
                    backStack.removeLast()
                    backStack.add(Screen.CircuitWorkout(workoutId = workoutId))
                },
                onFinished = { onWorkoutFinished() }
            )
            is Screen.CircuitWorkout -> CircuitWorkoutScreen(
                repo = repo,
                darkTheme = darkTheme,
                workoutId = s.workoutId,
                adFree = adFree,
                chronoNotifEnabled = chronoNotifEnabled,
                onChronoStart = onChronoStart,
                onChronoStop = onChronoStop,
                onBack = { navigateBack() },
                onFinished = { onWorkoutFinished() }
            )
            is Screen.Detail -> DetailScreen(
                repo = repo,
                darkTheme = darkTheme,
                workoutId = s.workoutId,
                onBack = { navigateBack() },
                onUseAsTemplate = { id ->
                    scope.launch {
                        val w = repo.getWorkoutById(id)
                        val type = w?.type ?: com.junade.gainznote.model.WorkoutType.MUSCULATION
                        backStack.clear(); backStack.add(Screen.Home)
                        navigateTo(when (type) {
                            com.junade.gainznote.model.WorkoutType.MUSCULATION -> Screen.Workout(templateId = id)
                            com.junade.gainznote.model.WorkoutType.CARDIO -> Screen.CardioSetup(templateId = id)
                            com.junade.gainznote.model.WorkoutType.CIRCUIT -> Screen.CircuitSetup(templateId = id)
                        })
                    }
                },
                onDeleted = { navigateBack() }
            )
        }
    }
}

@Composable
expect fun BackHandler(enabled: Boolean, onBack: () -> Unit)
