package com.gainznote.repository

import com.gainznote.db.DatabaseDriverFactory
import com.gainznote.db.GainzNoteDatabase
import com.gainznote.model.Exercise
import com.gainznote.model.TrainingSet
import com.gainznote.model.Workout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class WorkoutRepository(driverFactory: DatabaseDriverFactory) {
    private val db = GainzNoteDatabase(driverFactory.createDriver())
    private val q = db.gainzNoteQueries

    suspend fun getAllWorkouts(): List<Workout> = withContext(Dispatchers.IO) {
        q.getAllWorkouts().executeAsList().map { buildWorkout(it.id) }
    }

    suspend fun getWorkoutById(id: String): Workout? = withContext(Dispatchers.IO) {
        q.getWorkoutById(id).executeAsOneOrNull()?.let { buildWorkout(it.id) }
    }

    private fun buildWorkout(id: String): Workout {
        val row = q.getWorkoutById(id).executeAsOne()
        val exercises = q.getExercisesForWorkout(id).executeAsList().map { ex ->
            val sets = q.getSetsForExercise(ex.id).executeAsList().map { s ->
                TrainingSet(s.id, s.exercise_id, s.position.toInt(),
                    s.weight_kg, s.reps?.toInt(), s.reps_placeholder?.toInt(), s.notes)
            }
            Exercise(ex.id, ex.workout_id, ex.name, ex.position.toInt(), ex.superset_with, sets)
        }
        return Workout(row.id, row.title, row.notes, row.started_at, row.finished_at, exercises)
    }

    suspend fun saveWorkout(workout: Workout) = withContext(Dispatchers.IO) {
        db.transaction {
            q.insertWorkout(workout.id, workout.title, workout.notes,
                workout.startedAt, workout.finishedAt)
            q.deleteExercisesForWorkout(workout.id)
            workout.exercises.forEachIndexed { i, ex ->
                q.insertExercise(ex.id, workout.id, ex.name, i.toLong(), ex.supersetWith)
                q.deleteSetsForExercise(ex.id)
                ex.sets.forEachIndexed { j, s ->
                    q.insertSet(s.id, ex.id, j.toLong(), s.weightKg,
                        s.reps?.toLong(), s.repsPlaceholder?.toLong(), s.notes)
                }
            }
        }
    }

    suspend fun deleteWorkout(id: String) = withContext(Dispatchers.IO) {
        q.deleteWorkout(id)
    }
}
