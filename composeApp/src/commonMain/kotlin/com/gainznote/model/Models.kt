package com.gainznote.model

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

data class Workout(
    val id: String,
    val title: String,
    val notes: String,
    val startedAt: String,
    val finishedAt: String? = null,
    val exercises: List<Exercise> = emptyList()
)
