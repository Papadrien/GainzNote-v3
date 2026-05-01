package com.junade.gainznote.i18n

/** Langue supportée. */
enum class Lang { FR, EN }

/**
 * Objet de traduction centralisé.
 * Utiliser `S.xxx` partout dans l'UI au lieu de chaînes en dur.
 * Appeler `S.setLang(lang)` au démarrage ou quand l'utilisateur change la langue.
 */
object S {
    var lang: Lang = Lang.FR
        private set

    fun setLang(l: Lang) { lang = l }

    /** Initialise la langue à partir du code système ("fr", "en", …). */
    fun initFromSystem(code: String) {
        lang = if (code.startsWith("fr")) Lang.FR else Lang.EN
    }

    // ── App ───────────────────────────────────────────────────────────────
    val appName get() = "GainzNote"
        val newWorkoutTitle get() = when (lang) {
        Lang.FR -> "Nouvel entraînement"
        Lang.EN -> "New workout"
    }

    val subtitle get() = when (lang) {
        Lang.FR -> "Ton carnet de musculation"
        Lang.EN -> "Your workout logbook"
    }

    // ── Home ──────────────────────────────────────────────────────────────
    val newWorkout get() = when (lang) {
        Lang.FR -> "Démarrer"
        Lang.EN -> "Start"
    }
    val inProgress get() = when (lang) {
        Lang.FR -> "En cours"
        Lang.EN -> "In progress"
    }
    val recent get() = when (lang) {
        Lang.FR -> "Récents"
        Lang.EN -> "Recent"
    }
    val seeAll get() = when (lang) {
        Lang.FR -> "Voir tout →"
        Lang.EN -> "See all →"
    }
    val settings get() = when (lang) {
        Lang.FR -> "Paramètres"
        Lang.EN -> "Settings"
    }
    val darkMode get() = when (lang) {
        Lang.FR -> "Mode sombre"
        Lang.EN -> "Dark mode"
    }
    val blackBg get() = when (lang) {
        Lang.FR -> "Fond noir"
        Lang.EN -> "Black background"
    }
    val chronoNotif get() = when (lang) {
        Lang.FR -> "Notification chronomètre"
        Lang.EN -> "Timer notification"
    }
    val chronoNotifDesc get() = when (lang) {
        Lang.FR -> "Affiche le temps de repos dans les notifications"
        Lang.EN -> "Shows rest time in notifications"
    }
    val backupData get() = when (lang) {
        Lang.FR -> "Sauvegarder les données"
        Lang.EN -> "Backup data"
    }
    val restoreData get() = when (lang) {
        Lang.FR -> "Restaurer les données"
        Lang.EN -> "Restore data"
    }
    val untitledWorkout get() = when (lang) {
        Lang.FR -> "Entraînement sans titre"
        Lang.EN -> "Untitled workout"
    }
    val untitled get() = when (lang) {
        Lang.FR -> "Sans titre"
        Lang.EN -> "Untitled"
    }
    val deleteInProgressDesc get() = when (lang) {
        Lang.FR -> "Supprimer l'entraînement en cours"
        Lang.EN -> "Delete in-progress workout"
    }

    fun exerciseCount(n: Int) = when (lang) {
        Lang.FR -> "$n exercice(s)"
        Lang.EN -> if (n == 1) "1 exercise" else "$n exercises"
    }
    fun exerciseCountInProgress(n: Int) = when (lang) {
        Lang.FR -> "$n exercice(s) · En cours…"
        Lang.EN -> if (n == 1) "1 exercise · In progress…" else "$n exercises · In progress…"
    }

    // ── Workout ───────────────────────────────────────────────────────────
    fun startedAt(date: String) = when (lang) {
        Lang.FR -> "Démarré $date"
        Lang.EN -> "Started $date"
    }
    val finish get() = when (lang) {
        Lang.FR -> "Terminer"
        Lang.EN -> "Finish"
    }
    val workoutTitlePlaceholder get() = when (lang) {
        Lang.FR -> "Titre de l'entraînement"
        Lang.EN -> "Workout title"
    }
    val generalNotesPlaceholder get() = when (lang) {
        Lang.FR -> "Notes générales…"
        Lang.EN -> "General notes…"
    }
    val addExercise get() = when (lang) {
        Lang.FR -> "+ Ajouter un exercice"
        Lang.EN -> "+ Add exercise"
    }
    val finishWorkoutTitle get() = when (lang) {
        Lang.FR -> "Terminer l'entraînement ?"
        Lang.EN -> "Finish workout?"
    }
    fun finishWorkoutBody(exercises: Int, sets: Int) = when (lang) {
        Lang.FR -> "$exercises exercice(s) · $sets série(s)"
        Lang.EN -> "$exercises exercise(s) · $sets set(s)"
    }
    val finishConfirm get() = when (lang) {
        Lang.FR -> "Terminer ✓"
        Lang.EN -> "Finish ✓"
    }
    val continueWorkout get() = when (lang) {
        Lang.FR -> "Continuer"
        Lang.EN -> "Continue"
    }
    val supersetPickerTitle get() = when (lang) {
        Lang.FR -> "Associer en superset avec…"
        Lang.EN -> "Link as superset with…"
    }
    val noExerciseAvailable get() = when (lang) {
        Lang.FR -> "Aucun exercice disponible."
        Lang.EN -> "No exercise available."
    }
    val unnamedExercise get() = when (lang) {
        Lang.FR -> "Exercice sans nom"
        Lang.EN -> "Unnamed exercise"
    }
    val cancel get() = when (lang) {
        Lang.FR -> "Annuler"
        Lang.EN -> "Cancel"
    }
    val addSetsTitle get() = when (lang) {
        Lang.FR -> "Ajouter des séries"
        Lang.EN -> "Add sets"
    }
    val add get() = when (lang) {
        Lang.FR -> "Ajouter"
        Lang.EN -> "Add"
    }
    val exerciseNamePlaceholder get() = when (lang) {
        Lang.FR -> "Nom de l'exercice"
        Lang.EN -> "Exercise name"
    }
    val moveUp get() = when (lang) {
        Lang.FR -> "↑  Déplacer vers le haut"
        Lang.EN -> "↑  Move up"
    }
    val unlinkSuperset get() = when (lang) {
        Lang.FR -> "Retirer du superset"
        Lang.EN -> "Remove from superset"
    }
    val linkSuperset get() = when (lang) {
        Lang.FR -> "Associer en superset"
        Lang.EN -> "Link as superset"
    }
    val deleteExercise get() = when (lang) {
        Lang.FR -> "Supprimer l'exercice"
        Lang.EN -> "Delete exercise"
    }
    val addSet get() = when (lang) {
        Lang.FR -> "+ Série"
        Lang.EN -> "+ Set"
    }
    val addMultiple get() = when (lang) {
        Lang.FR -> "+ Plusieurs"
        Lang.EN -> "+ Multiple"
    }

    // ── History ───────────────────────────────────────────────────────────
    val history get() = when (lang) {
        Lang.FR -> "Historique"
        Lang.EN -> "History"
    }
    val noWorkoutRecorded get() = when (lang) {
        Lang.FR -> "Aucun entraînement enregistré"
        Lang.EN -> "No workout recorded"
    }
    fun exercisesCount(n: Int) = when (lang) {
        Lang.FR -> "$n exercices"
        Lang.EN -> if (n == 1) "1 exercise" else "$n exercises"
    }
    fun setsCount(n: Int) = when (lang) {
        Lang.FR -> "$n séries"
        Lang.EN -> if (n == 1) "1 set" else "$n sets"
    }
    val useAsTemplate get() = when (lang) {
        Lang.FR -> "↻  Utiliser comme base"
        Lang.EN -> "↻  Use as template"
    }
    val replayCircuit get() = when (lang) {
        Lang.FR -> "↻  Rejouer ce circuit"
        Lang.EN -> "↻  Replay this circuit"
    }

    // ── Detail ────────────────────────────────────────────────────────────
    val delete get() = when (lang) {
        Lang.FR -> "Supprimer"
        Lang.EN -> "Delete"
    }
    val deleteConfirmTitle get() = when (lang) {
        Lang.FR -> "Supprimer ?"
        Lang.EN -> "Delete?"
    }
    val deleteConfirmBody get() = when (lang) {
        Lang.FR -> "Action irréversible."
        Lang.EN -> "This cannot be undone."
    }
    val weight get() = when (lang) {
        Lang.FR -> "Poids"
        Lang.EN -> "Weight"
    }
    val reps get() = when (lang) {
        Lang.FR -> "Reps"
        Lang.EN -> "Reps"
    }
    val notes get() = when (lang) {
        Lang.FR -> "Notes"
        Lang.EN -> "Notes"
    }

    // ── Dates ─────────────────────────────────────────────────────────────
    val daysShort get() = when (lang) {
        Lang.FR -> listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
        Lang.EN -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }
    val daysLong get() = when (lang) {
        Lang.FR -> listOf("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche")
        Lang.EN -> listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    }
    val monthsShort get() = when (lang) {
        Lang.FR -> listOf("jan", "fév", "mar", "avr", "mai", "jun", "jul", "aoû", "sep", "oct", "nov", "déc")
        Lang.EN -> listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    }
    fun dateAtTime(day: String, dayNum: Int, month: String, hour: String, min: String) = when (lang) {
        Lang.FR -> "$day $dayNum $month à $hour:$min"
        Lang.EN -> "$day $month $dayNum, $hour:$min"
    }
    fun dateShort(day: String, dayNum: Int, month: String, hour: String, min: String) = when (lang) {
        Lang.FR -> "$day $dayNum $month · $hour:$min"
        Lang.EN -> "$day $month $dayNum · $hour:$min"
    }

    // ── Ads / Purchase ────────────────────────────────────────────────────
    val removeAds get() = when (lang) {
        Lang.FR -> "Supprimer les publicités"
        Lang.EN -> "Remove ads"
    }
    val removeAdsTestHint get() = when (lang) {
        Lang.FR -> "⚠ Mode test — simule un achat"
        Lang.EN -> "⚠ Test mode — simulates a purchase"
    }
    val adsRemoved get() = when (lang) {
        Lang.FR -> "✓ Publicités supprimées"
        Lang.EN -> "✓ Ads removed"
    }
    val adsRemovedTestHint get() = when (lang) {
        Lang.FR -> "⚠ Mode test — appuyez pour réactiver"
        Lang.EN -> "⚠ Test mode — tap to re-enable"
    }
    val removeAdsPrice get() = when (lang) {
        Lang.FR -> "Supprimer les publicités — 1,99€"
        Lang.EN -> "Remove ads — \$1.99"
    }
    val removeAdsPriceDesc get() = when (lang) {
        Lang.FR -> "Achat unique · Supprime toutes les publicités"
        Lang.EN -> "One-time purchase · Removes all ads"
    }
    val purchaseError get() = when (lang) {
        Lang.FR -> "Achat indisponible pour le moment"
        Lang.EN -> "Purchase unavailable at the moment"
    }
    val language get() = when (lang) {
        Lang.FR -> "Langue"
        Lang.EN -> "Language"
    }
    val chooseLanguage get() = when (lang) {
        Lang.FR -> "Choisir la langue"
        Lang.EN -> "Choose language"
    }
    val languageAuto get() = when (lang) {
        Lang.FR -> "Automatique (système)"
        Lang.EN -> "Automatic (system)"
    }

    // ── Android-only (MainActivity / ChronoService) ──────────────────────
    val dataSaved get() = when (lang) {
        Lang.FR -> "Données sauvegardées ✓"
        Lang.EN -> "Data saved ✓"
    }
    val saveError get() = when (lang) {
        Lang.FR -> "Erreur lors de la sauvegarde"
        Lang.EN -> "Error while saving"
    }
    val invalidFileFormat get() = when (lang) {
        Lang.FR -> "Format de fichier invalide"
        Lang.EN -> "Invalid file format"
    }
    val fileMalformatted get() = when (lang) {
        Lang.FR -> "Erreur : fichier mal formaté"
        Lang.EN -> "Error: malformed file"
    }
    val notifPermDenied get() = when (lang) {
        Lang.FR -> "Autorisation refusée — la notification chrono ne s'affichera pas"
        Lang.EN -> "Permission denied — timer notification won't show"
    }
    val dataRestored get() = when (lang) {
        Lang.FR -> "Données restaurées ✓"
        Lang.EN -> "Data restored ✓"
    }
    val restoreDialogTitle get() = when (lang) {
        Lang.FR -> "Restaurer les données"
        Lang.EN -> "Restore data"
    }
    val restoreDialogBody get() = when (lang) {
        Lang.FR -> "Des entraînements existent déjà.\n\nQue souhaitez-vous faire ?"
        Lang.EN -> "Workouts already exist.\n\nWhat would you like to do?"
    }
    val addToExisting get() = when (lang) {
        Lang.FR -> "Ajouter aux existants"
        Lang.EN -> "Add to existing"
    }
    val dataAdded get() = when (lang) {
        Lang.FR -> "Données ajoutées ✓"
        Lang.EN -> "Data added ✓"
    }
    val overwriteAll get() = when (lang) {
        Lang.FR -> "Écraser tout"
        Lang.EN -> "Overwrite all"
    }
    val dataReplaced get() = when (lang) {
        Lang.FR -> "Données remplacées ✓"
        Lang.EN -> "Data replaced ✓"
    }
    val importError get() = when (lang) {
        Lang.FR -> "Erreur lors de l'import"
        Lang.EN -> "Import error"
    }
    val chronoNotifTitle get() = when (lang) {
        Lang.FR -> "GainzNote · Temps de repos"
        Lang.EN -> "GainzNote · Rest time"
    }
    val chronoChannelName get() = when (lang) {
        Lang.FR -> "Chronomètre de repos"
        Lang.EN -> "Rest timer"
    }
    val chronoChannelDesc get() = when (lang) {
        Lang.FR -> "Affiche le temps de repos en cours pendant l'entraînement"
        Lang.EN -> "Shows current rest time during workout"
    }

    // ── Workout types (Cardio, Circuit) ───────────────────────────────────
    val workoutTypeMusculation get() = when (lang) {
        Lang.FR -> "Musculation"
        Lang.EN -> "Strength training"
    }
    val workoutTypeCardio get() = when (lang) {
        Lang.FR -> "Cardio"
        Lang.EN -> "Cardio"
    }
    val workoutTypeCircuit get() = when (lang) {
        Lang.FR -> "Circuit"
        Lang.EN -> "Circuit"
    }
    val selectWorkoutType get() = when (lang) {
        Lang.FR -> "Type"
        Lang.EN -> "Type"
    }
    val back get() = when (lang) {
        Lang.FR -> "Retour"
        Lang.EN -> "Back"
    }
    val cardioComingSoon get() = when (lang) {
        Lang.FR -> "Cardio — bientôt disponible"
        Lang.EN -> "Cardio — coming soon"
    }
    val cardioComingSoonDesc get() = when (lang) {
        Lang.FR -> "Cette fonctionnalité arrive très prochainement."
        Lang.EN -> "This feature is coming soon."
    }
    val circuitComingSoon get() = when (lang) {
        Lang.FR -> "Circuit — bientôt disponible"
        Lang.EN -> "Circuit — coming soon"
    }
    val circuitComingSoonDesc get() = when (lang) {
        Lang.FR -> "Cette fonctionnalité arrive très prochainement."
        Lang.EN -> "This feature is coming soon."
    }
    val overwriteInProgressTitle get() = when (lang) {
        Lang.FR -> "Un entraînement est en cours"
        Lang.EN -> "A workout is in progress"
    }
    val overwriteInProgressBody get() = when (lang) {
        Lang.FR -> "Démarrer un nouvel entraînement écrasera celui en cours.\n\nContinuer ?"
        Lang.EN -> "Starting a new workout will overwrite the one in progress.\n\nContinue?"
    }
    val overwriteConfirm get() = when (lang) {
        Lang.FR -> "Écraser"
        Lang.EN -> "Overwrite"
    }


    // ── Duration units (h/m/s) ─────────────────────────────────────────────
    val hoursShort get() = when (lang) {
        Lang.FR -> "H"
        Lang.EN -> "H"
    }
    val minutesShort get() = when (lang) {
        Lang.FR -> "M"
        Lang.EN -> "M"
    }
    val secondsShort get() = when (lang) {
        Lang.FR -> "S"
        Lang.EN -> "S"
    }


    // ── Cardio ─────────────────────────────────────────────────────────────
    val addCardioExercise get() = when (lang) {
        Lang.FR -> "+ Ajouter un exercice cardio"
        Lang.EN -> "+ Add cardio exercise"
    }
    val addSegment get() = when (lang) {
        Lang.FR -> "+ Ajouter un segment"
        Lang.EN -> "+ Add segment"
    }
    val cardioExerciseNamePlaceholder get() = when (lang) {
        Lang.FR -> "Nom de l'exercice (ex: Vélo, Tapis...)"
        Lang.EN -> "Exercise name (e.g. Bike, Treadmill...)"
    }
    val intensityPlaceholder get() = when (lang) {
        Lang.FR -> "Intensité (ex: Niveau 5, 8 km/h, RPE 7...)"
        Lang.EN -> "Intensity (e.g. Level 5, 8 km/h, RPE 7...)"
    }
    fun segmentLabel(index: Int) = when (lang) {
        Lang.FR -> "Segment $index"
        Lang.EN -> "Segment $index"
    }
    fun finishCardioBody(exercises: Int, segments: Int) = when (lang) {
        Lang.FR -> "$exercises exercice(s) · $segments segment(s)"
        Lang.EN -> "$exercises exercise(s) · $segments segment(s)"
    }
    fun segmentsCount(n: Int) = when (lang) {
        Lang.FR -> "$n segment(s)"
        Lang.EN -> if (n == 1) "1 segment" else "$n segments"
    }
    fun cardioExercisesCount(n: Int) = when (lang) {
        Lang.FR -> "$n exercice(s) cardio"
        Lang.EN -> if (n == 1) "1 cardio exercise" else "$n cardio exercises"
    }


    fun roundsCount(n: Int) = when (lang) {
        Lang.FR -> "$n tour(s)"
        Lang.EN -> if (n == 1) "1 round" else "$n rounds"
    }


    val intensity get() = when (lang) {
        Lang.FR -> "Intensité"
        Lang.EN -> "Intensity"
    }
    val duration get() = when (lang) {
        Lang.FR -> "Durée"
        Lang.EN -> "Duration"
    }
    val circuitDetailComingSoon get() = when (lang) {
        Lang.FR -> "Détails du circuit — bientôt"
        Lang.EN -> "Circuit details — coming soon"
    }


    // ── Circuit ────────────────────────────────────────────────────────────
    val addCircuitExercise get() = when (lang) {
        Lang.FR -> "+ Ajouter un exercice"
        Lang.EN -> "+ Add exercise"
    }
    val circuitExerciseNamePlaceholder get() = when (lang) {
        Lang.FR -> "Nom de l'exercice (ex: Pompes, Squats...)"
        Lang.EN -> "Exercise name (e.g. Push-ups, Squats...)"
    }
    val circuitConfig get() = when (lang) {
        Lang.FR -> "Configuration"
        Lang.EN -> "Configuration"
    }
    val totalRounds get() = when (lang) {
        Lang.FR -> "Nombre de tours"
        Lang.EN -> "Number of rounds"
    }
    val restBetweenExercises get() = when (lang) {
        Lang.FR -> "Repos entre exercices"
        Lang.EN -> "Rest between exercises"
    }
    val restBetweenRounds get() = when (lang) {
        Lang.FR -> "Repos entre tours"
        Lang.EN -> "Rest between rounds"
    }
    val startCircuit get() = when (lang) {
        Lang.FR -> "Démarrer"
        Lang.EN -> "Start"
    }
    val inputType get() = when (lang) {
        Lang.FR -> "Type de saisie"
        Lang.EN -> "Input type"
    }
    val inputTypeReps get() = when (lang) {
        Lang.FR -> "Répétitions"
        Lang.EN -> "Reps"
    }
    val inputTypeRepsWeight get() = when (lang) {
        Lang.FR -> "Reps + poids"
        Lang.EN -> "Reps + weight"
    }
    val inputTypeDuration get() = when (lang) {
        Lang.FR -> "Durée"
        Lang.EN -> "Duration"
    }
    val restInProgress get() = when (lang) {
        Lang.FR -> "Repos en cours"
        Lang.EN -> "Resting"
    }
    val skipRest get() = when (lang) {
        Lang.FR -> "Passer"
        Lang.EN -> "Skip"
    }
    val validateAndNext get() = when (lang) {
        Lang.FR -> "Valider et passer au suivant"
        Lang.EN -> "Validate and next"
    }
    val recap get() = when (lang) {
        Lang.FR -> "Récapitulatif"
        Lang.EN -> "Summary"
    }
    val exercise get() = when (lang) {
        Lang.FR -> "Exercice"
        Lang.EN -> "Exercise"
    }
    val save get() = when (lang) {
        Lang.FR -> "Enregistrer"
        Lang.EN -> "Save"
    }
    val notesPlaceholder get() = when (lang) {
        Lang.FR -> "Notes..."
        Lang.EN -> "Notes..."
    }
    fun roundProgress(current: Int, total: Int) = when (lang) {
        Lang.FR -> "Tour $current / $total"
        Lang.EN -> "Round $current / $total"
    }
    fun exerciseProgress(current: Int, total: Int) = when (lang) {
        Lang.FR -> "Exercice $current / $total"
        Lang.EN -> "Exercise $current / $total"
    }
    fun finishCircuitBody(exercises: Int, performances: Int) = when (lang) {
        Lang.FR -> "$exercises exercice(s) · $performances performance(s)"
        Lang.EN -> "$exercises exercise(s) · $performances performance(s)"
    }


    // ── Back handler confirmation ──────────────────────────────────────────
    val leaveWorkoutTitle get() = when (lang) {
        Lang.FR -> "Quitter l'entraînement ?"
        Lang.EN -> "Leave workout?"
    }
    val leaveWorkoutBody get() = when (lang) {
        Lang.FR -> "Les modifications non terminées sont sauvegardées automatiquement. Vous pouvez y revenir plus tard depuis « En cours »."
        Lang.EN -> "Unfinished changes are auto-saved. You can come back later from « In progress »."
    }
    val leaveConfirm get() = when (lang) {
        Lang.FR -> "Quitter"
        Lang.EN -> "Leave"
    }
    val stay get() = when (lang) {
        Lang.FR -> "Rester"
        Lang.EN -> "Stay"
    }


    // ── Unit labels ────────────────────────────────────────────────────────
    val repsShort get() = when (lang) {
        Lang.FR -> "reps"
        Lang.EN -> "reps"
    }
    val kgShort get() = when (lang) {
        Lang.FR -> "kg"
        Lang.EN -> "kg"
    }


    // ── Accessibilité (C9) ────────────────────────────────────────────────
    val backDesc get() = when (lang) {
        Lang.FR -> "Retour"
        Lang.EN -> "Back"
    }
    val moveUpDesc get() = when (lang) {
        Lang.FR -> "Déplacer l'exercice vers le haut"
        Lang.EN -> "Move exercise up"
    }
    val increaseRoundsDesc get() = when (lang) {
        Lang.FR -> "Augmenter le nombre de tours"
        Lang.EN -> "Increase number of rounds"
    }
    val decreaseRoundsDesc get() = when (lang) {
        Lang.FR -> "Diminuer le nombre de tours"
        Lang.EN -> "Decrease number of rounds"
    }
    val addSegmentDesc get() = when (lang) {
        Lang.FR -> "Ajouter un segment"
        Lang.EN -> "Add a segment"
    }
    val removeSegmentDesc get() = when (lang) {
        Lang.FR -> "Supprimer ce segment"
        Lang.EN -> "Remove this segment"
    }
    val skipRestDesc get() = when (lang) {
        Lang.FR -> "Passer le repos"
        Lang.EN -> "Skip rest"
    }
    val removeExerciseDesc get() = when (lang) {
        Lang.FR -> "Supprimer cet exercice"
        Lang.EN -> "Remove this exercise"
    }
    val restTimerLabel get() = when (lang) {
        Lang.FR -> "Temps de repos restant"
        Lang.EN -> "Remaining rest time"
    }

}
