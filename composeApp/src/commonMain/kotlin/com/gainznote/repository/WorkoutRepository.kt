package com.gainznote.repository

import com.gainznote.db.DatabaseDriverFactory
import com.gainznote.db.GainzNoteDatabase
import com.gainznote.model.Exercise
import com.gainznote.model.TrainingSet
import com.gainznote.model.Workout
import com.gainznote.ui.workout.newId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class WorkoutRepository(driverFactory: DatabaseDriverFactory) {
    private val db = GainzNoteDatabase(driverFactory.createDriver())
    private val q = db.gainzNoteQueries

    suspend fun getAllWorkouts(): List<Workout> = withContext(Dispatchers.IO) {
        q.getAllWorkouts().executeAsList().map { buildWorkout(it.id) }
    }

    suspend fun getFinishedWorkouts(): List<Workout> = withContext(Dispatchers.IO) {
        q.getFinishedWorkouts().executeAsList().map { buildWorkout(it.id) }
    }

    suspend fun getInProgressWorkouts(): List<Workout> = withContext(Dispatchers.IO) {
        q.getInProgressWorkouts().executeAsList().map { buildWorkout(it.id) }
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
            q.insertWorkout(workout.id, workout.title, workout.notes, workout.startedAt, workout.finishedAt)
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

    suspend fun deleteAllWorkouts() = withContext(Dispatchers.IO) {
        getAllWorkouts().forEach { q.deleteWorkout(it.id) }
    }

    suspend fun hasWorkouts(): Boolean = withContext(Dispatchers.IO) {
        q.getAllWorkouts().executeAsList().isNotEmpty()
    }

    // ─── Export JSON ──────────────────────────────────────────────────────────

    suspend fun exportJson(): String = withContext(Dispatchers.IO) {
        val workouts = getAllWorkouts()
        buildString {
            append("[")
            workouts.forEachIndexed { wi, w ->
                if (wi > 0) append(",")
                append("{")
                append("\"id\":${w.id.j()},")
                append("\"title\":${w.title.j()},")
                append("\"notes\":${w.notes.j()},")
                append("\"startedAt\":${w.startedAt.j()},")
                append("\"finishedAt\":${w.finishedAt.jn()},")
                append("\"exercises\":[")
                w.exercises.forEachIndexed { ei, ex ->
                    if (ei > 0) append(",")
                    append("{")
                    append("\"id\":${ex.id.j()},")
                    append("\"workoutId\":${ex.workoutId.j()},")
                    append("\"name\":${ex.name.j()},")
                    append("\"position\":${ex.position},")
                    append("\"supersetWith\":${ex.supersetWith.jn()},")
                    append("\"sets\":[")
                    ex.sets.forEachIndexed { si, s ->
                        if (si > 0) append(",")
                        append("{")
                        append("\"id\":${s.id.j()},")
                        append("\"exerciseId\":${s.exerciseId.j()},")
                        append("\"position\":${s.position},")
                        append("\"weightKg\":${s.weightKg ?: "null"},")
                        append("\"reps\":${s.reps ?: "null"},")
                        append("\"repsPlaceholder\":${s.repsPlaceholder ?: "null"},")
                        append("\"notes\":${s.notes.j()}")
                        append("}")
                    }
                    append("]}")
                }
                append("]}")
            }
            append("]")
        }
    }

    // ─── Import JSON ──────────────────────────────────────────────────────────

    suspend fun importJson(json: String) = withContext(Dispatchers.IO) {
        val workouts = parseJsonWorkouts(json)
        db.transaction {
            workouts.forEach { workout ->
                q.insertWorkout(workout.id, workout.title, workout.notes, workout.startedAt, workout.finishedAt)
                workout.exercises.forEachIndexed { i, ex ->
                    q.insertExercise(ex.id, workout.id, ex.name, i.toLong(), ex.supersetWith)
                    ex.sets.forEachIndexed { j, s ->
                        q.insertSet(s.id, ex.id, j.toLong(), s.weightKg, s.reps?.toLong(), s.repsPlaceholder?.toLong(), s.notes)
                    }
                }
            }
        }
    }

    // ─── Helpers JSON ─────────────────────────────────────────────────────────

    private fun String.j() = "\"${replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")}\""
    private fun String?.jn() = if (this == null) "null" else j()

    private fun parseJsonWorkouts(json: String): List<Workout> {
        val result = mutableListOf<Workout>()
        val trimmed = json.trim().removePrefix("[").removeSuffix("]").trim()
        if (trimmed.isBlank()) return result
        for (wStr in splitJsonObjects(trimmed)) {
            try { result.add(parseWorkoutJson(wStr)) } catch (_: Exception) {}
        }
        return result
    }

    private fun splitJsonObjects(str: String): List<String> {
        val out = mutableListOf<String>(); var depth = 0; var start = -1
        for (i in str.indices) {
            when (str[i]) {
                '{' -> { if (depth == 0) start = i; depth++ }
                '}' -> { depth--; if (depth == 0 && start >= 0) { out.add(str.substring(start, i + 1)); start = -1 } }
            }
        }
        return out
    }

    private fun sf(json: String, key: String): String? =
        "\"$key\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"".toRegex().find(json)
            ?.groupValues?.getOrNull(1)?.replace("\\\"","\"")?.replace("\\\\","\\")?.replace("\\n","\n")

    private fun snf(json: String, key: String): String? {
        if ("\"$key\"\\s*:\\s*null".toRegex().containsMatchIn(json)) return null
        return sf(json, key)
    }

    private fun df(json: String, key: String): Double? =
        "\"$key\"\\s*:\\s*([0-9.eE+\\-]+)".toRegex().find(json)?.groupValues?.getOrNull(1)?.toDoubleOrNull()

    private fun inf(json: String, key: String): Int? {
        if ("\"$key\"\\s*:\\s*null".toRegex().containsMatchIn(json)) return null
        return "\"$key\"\\s*:\\s*([0-9]+)".toRegex().find(json)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    private fun arrBlock(json: String, key: String): String {
        val ki = json.indexOf("\"$key\""); if (ki < 0) return ""
        val as_ = json.indexOf('[', ki); if (as_ < 0) return ""
        var d = 0
        for (i in as_ until json.length) when (json[i]) {
            '[' -> d++; ']' -> { d--; if (d == 0) return json.substring(as_ + 1, i) }
        }
        return ""
    }

    private fun parseWorkoutJson(json: String): Workout {
        val wId = sf(json, "id") ?: newId()
        val exBlock = arrBlock(json, "exercises")
        val exercises = splitJsonObjects(exBlock).map { parseExerciseJson(it, wId) }
        return Workout(wId, sf(json, "title") ?: "", sf(json, "notes") ?: "",
            sf(json, "startedAt") ?: Clock.System.now().toString(),
            snf(json, "finishedAt"), exercises)
    }

    private fun parseExerciseJson(json: String, workoutId: String): Exercise {
        val exId = sf(json, "id") ?: newId()
        val sets = splitJsonObjects(arrBlock(json, "sets")).map { parseSetJson(it, exId) }
        return Exercise(exId, workoutId, sf(json, "name") ?: "",
            inf(json, "position") ?: 0, snf(json, "supersetWith"), sets)
    }

    private fun parseSetJson(json: String, exerciseId: String): TrainingSet =
        TrainingSet(sf(json, "id") ?: newId(), exerciseId,
            inf(json, "position") ?: 0, df(json, "weightKg"),
            inf(json, "reps"), inf(json, "repsPlaceholder"), sf(json, "notes") ?: "")
}
