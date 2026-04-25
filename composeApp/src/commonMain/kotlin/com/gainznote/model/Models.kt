package com.gainznote.model

/** Type d'entraînement. */
enum class WorkoutType {
    MUSCULATION,
    CARDIO,
    CIRCUIT;

    companion object {
        fun parse(raw: String?): WorkoutType = when (raw) {
            "CARDIO" -> CARDIO
            "CIRCUIT" -> CIRCUIT
            else -> MUSCULATION
        }
    }
}

/** Type de saisie pour un exercice de circuit. */
enum class CircuitInputType {
    REPS,
    REPS_WEIGHT,
    DURATION;

    companion object {
        fun parse(raw: String?): CircuitInputType = when (raw) {
            "REPS_WEIGHT" -> REPS_WEIGHT
            "DURATION" -> DURATION
            else -> REPS
        }
    }
}

// ── Muscu ────────────────────────────────────────────────────────────────────

data class TrainingSet(
    val id: String,
    val exerciseId: String,
    val position: Int,
    val weightKg: Double? = null,
    val reps: Int? = null,
    val repsPlaceholder: Int? = null,
    val notes: String = ""
)

data class Exercise(
    val id: String,
    val workoutId: String,
    val name: String,
    val position: Int,
    val supersetWith: String? = null,
    val sets: List<TrainingSet> = emptyList()
)

// ── Cardio ───────────────────────────────────────────────────────────────────

data class CardioSegment(
    val id: String,
    val cardioExerciseId: String,
    val position: Int,
    val intensity: String = "",
    val durationSeconds: Long = 0
)

data class CardioExercise(
    val id: String,
    val workoutId: String,
    val name: String,
    val position: Int,
    val segments: List<CardioSegment> = emptyList()
)

// ── Circuit ──────────────────────────────────────────────────────────────────

data class CircuitConfig(
    val workoutId: String,
    val totalRounds: Int = 3,
    val restBetweenExercisesSeconds: Long = 0,
    val restBetweenRoundsSeconds: Long = 0
)

data class CircuitPerformance(
    val id: String,
    val circuitExerciseId: String,
    val roundNumber: Int,
    val reps: Int? = null,
    val weightKg: Double? = null,
    val durationSeconds: Long? = null,
    val notes: String = ""
)

data class CircuitExercise(
    val id: String,
    val workoutId: String,
    val name: String,
    val position: Int,
    val inputType: CircuitInputType = CircuitInputType.REPS,
    val performances: List<CircuitPerformance> = emptyList()
)

// ── Workout (agrégé) ─────────────────────────────────────────────────────────

data class Workout(
    val id: String,
    val title: String,
    val notes: String,
    val startedAt: String,
    val finishedAt: String? = null,
    val type: WorkoutType = WorkoutType.MUSCULATION,
    val exercises: List<Exercise> = emptyList(),
    val cardioExercises: List<CardioExercise> = emptyList(),
    val circuitConfig: CircuitConfig? = null,
    val circuitExercises: List<CircuitExercise> = emptyList()
)
