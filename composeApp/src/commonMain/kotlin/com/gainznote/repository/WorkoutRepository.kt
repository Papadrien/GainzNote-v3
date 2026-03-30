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
        q.getAllWorkouts().executeAsList().map { row -> buildWorkout(row.id, row.title, row.notes, row.started_at, row.finished_at) }
    }

    suspend fun getWorkoutById(id: String): Workout? = withContext(Dispatchers.IO) {
        q.getWorkoutById(id).executeAsOneOrNull()?.let { row ->
            buildWorkout(row.id, row.title, row.notes, row.started_at, row.finished_at)
        }
    }

    private fun buildWorkout(
        id: String,
        title: String,
        notes: String,
        startedAt: String,
        finishedAt: String?
    ): Workout {
        val exercises = q.getExercisesForWorkout(id).executeAsList().map { ex ->
            val sets = q.getSetsForExercise(ex.id).executeAsList().map { s ->
                TrainingSet(
                    id            = s.id,
                    exerciseId    = s.exercise_id,
                    position      = s.position.toInt(),
                    weightKg      = s.weight_kg,
                    reps          = s.reps?.toInt(),
                    repsPlaceholder = s.reps_placeholder?.toInt(),
                    notes         = s.notes
                )
            }
            Exercise(
                id          = ex.id,
                workoutId   = ex.workout_id,
                name        = ex.name,
                position    = ex.position.toInt(),
                supersetWith = ex.superset_with,
                sets        = sets
            )
        }
        return Workout(
            id         = id,
            title      = title,
            notes      = notes,
            startedAt  = startedAt,
            finishedAt = finishedAt,
            exercises  = exercises
        )
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
