package fr.junade.gainznote

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Centralise tous les événements Firebase Analytics de GainzNote.
 *
 * Événements envoyés :
 *  - app_open              → au démarrage de l'application
 *  - workout_start         → début d'un entraînement (param: workout_type)
 *  - workout_finish        → fin d'un entraînement
 *  - workout_from_previous → entraînement lancé depuis un précédent (template/historique)
 *  - history_open          → ouverture de la page historique
 */
object AnalyticsHelper {

    private lateinit var analytics: FirebaseAnalytics

    fun init(fa: FirebaseAnalytics) {
        analytics = fa
    }

    // ── Événements ────────────────────────────────────────────────────────────

    /** Lancé une fois par démarrage de l'application. */
    fun logAppOpen() {
        analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)
    }

    /**
     * Lancé au début d'un entraînement.
     * @param workoutType "musculation" | "cardio" | "circuit"
     */
    fun logWorkoutStart(workoutType: String) {
        val params = Bundle().apply {
            putString("workout_type", workoutType)
        }
        analytics.logEvent("workout_start", params)
    }

    /** Lancé quand l'utilisateur termine un entraînement. */
    fun logWorkoutFinish() {
        analytics.logEvent("workout_finish", null)
    }

    /**
     * Lancé quand un entraînement est démarré depuis un entraînement précédent
     * (bouton "Utiliser comme modèle" depuis l'historique ou le détail).
     * @param workoutType "musculation" | "cardio" | "circuit"
     */
    fun logWorkoutFromPrevious(workoutType: String) {
        val params = Bundle().apply {
            putString("workout_type", workoutType)
        }
        analytics.logEvent("workout_from_previous", params)
    }

    /** Lancé à l'ouverture de la page historique. */
    fun logHistoryOpen() {
        analytics.logEvent("history_open", null)
    }
}
