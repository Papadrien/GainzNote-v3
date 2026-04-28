package com.gainznote.repository

import com.gainznote.db.DatabaseDriverFactory
import com.gainznote.db.GainzNoteDatabase
import com.gainznote.model.AppSettings
import com.gainznote.model.CardioExercise
import com.gainznote.model.CardioSegment
import com.gainznote.model.CircuitConfig
import com.gainznote.model.CircuitExercise
import com.gainznote.model.CircuitInputType
import com.gainznote.model.CircuitPerformance
import com.gainznote.model.Exercise
import com.gainznote.model.TrainingSet
import com.gainznote.model.Workout
import com.gainznote.model.WorkoutType
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
        val type = WorkoutType.parse(row.type)
        val exercises = if (type == WorkoutType.MUSCULATION) {
            q.getExercisesForWorkout(id).executeAsList().map { ex ->
                val sets = q.getSetsForExercise(ex.id).executeAsList().map { s ->
                    TrainingSet(s.id, s.exercise_id, s.position.toInt(),
                        s.weight_kg, s.reps?.toInt(), s.reps_placeholder?.toInt(), s.notes)
                }
                Exercise(ex.id, ex.workout_id, ex.name, ex.position.toInt(), ex.superset_with, sets)
            }
        } else emptyList()

        val cardioExercises = if (type == WorkoutType.CARDIO) {
            q.getCardioExercisesForWorkout(id).executeAsList().map { ce ->
                val segs = q.getCardioSegmentsForExercise(ce.id).executeAsList().map { s ->
                    CardioSegment(s.id, s.cardio_exercise_id, s.position.toInt(),
                        s.intensity, s.duration_seconds)
                }
                CardioExercise(ce.id, ce.workout_id, ce.name, ce.position.toInt(), segs)
            }
        } else emptyList()

        val circuitConfig = if (type == WorkoutType.CIRCUIT) {
            q.getCircuitConfig(id).executeAsOneOrNull()?.let { cc ->
                CircuitConfig(cc.workout_id, cc.total_rounds.toInt(),
                    cc.rest_between_exercises_seconds, cc.rest_between_rounds_seconds)
            }
        } else null

        val circuitExercises = if (type == WorkoutType.CIRCUIT) {
            q.getCircuitExercisesForWorkout(id).executeAsList().map { ce ->
                val perfs = q.getCircuitPerformancesForExercise(ce.id).executeAsList().map { p ->
                    CircuitPerformance(p.id, p.circuit_exercise_id, p.round_number.toInt(),
                        p.reps?.toInt(), p.weight_kg, p.duration_seconds, p.notes)
                }
                CircuitExercise(ce.id, ce.workout_id, ce.name, ce.position.toInt(),
                    CircuitInputType.parse(ce.input_type), perfs)
            }
        } else emptyList()

        return Workout(
            id = row.id, title = row.title, notes = row.notes,
            startedAt = row.started_at, finishedAt = row.finished_at, type = type,
            exercises = exercises, cardioExercises = cardioExercises,
            circuitConfig = circuitConfig, circuitExercises = circuitExercises
        )
    }

    suspend fun saveWorkout(workout: Workout) = withContext(Dispatchers.IO) {
        db.transaction {
            q.insertWorkout(workout.id, workout.title, workout.notes,
                workout.startedAt, workout.finishedAt, workout.type.name)
            q.deleteExercisesForWorkout(workout.id)
            if (workout.type == WorkoutType.MUSCULATION) {
                workout.exercises.forEachIndexed { i, ex ->
                    q.insertExercise(ex.id, workout.id, ex.name, i.toLong(), ex.supersetWith)
                    q.deleteSetsForExercise(ex.id)
                    ex.sets.forEachIndexed { j, s ->
                        q.insertSet(s.id, ex.id, j.toLong(), s.weightKg,
                            s.reps?.toLong(), s.repsPlaceholder?.toLong(), s.notes)
                    }
                }
            }
            q.deleteCardioExercisesForWorkout(workout.id)
            if (workout.type == WorkoutType.CARDIO) {
                workout.cardioExercises.forEachIndexed { i, ce ->
                    q.insertCardioExercise(ce.id, workout.id, ce.name, i.toLong())
                    q.deleteCardioSegmentsForExercise(ce.id)
                    ce.segments.forEachIndexed { j, s ->
                        q.insertCardioSegment(s.id, ce.id, j.toLong(), s.intensity, s.durationSeconds)
                    }
                }
            }
            q.deleteCircuitConfig(workout.id)
            q.deleteCircuitExercisesForWorkout(workout.id)
            if (workout.type == WorkoutType.CIRCUIT) {
                workout.circuitConfig?.let { cc ->
                    q.upsertCircuitConfig(workout.id, cc.totalRounds.toLong(),
                        cc.restBetweenExercisesSeconds, cc.restBetweenRoundsSeconds)
                }
                workout.circuitExercises.forEachIndexed { i, ce ->
                    q.insertCircuitExercise(ce.id, workout.id, ce.name, i.toLong(), ce.inputType.name)
                    q.deleteCircuitPerformancesForExercise(ce.id)
                    ce.performances.forEach { p ->
                        q.insertCircuitPerformance(p.id, ce.id, p.roundNumber.toLong(),
                            p.reps?.toLong(), p.weightKg, p.durationSeconds, p.notes)
                    }
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

    suspend fun getAppSettings(): AppSettings = withContext(Dispatchers.IO) {
        q.getAppSettings().executeAsOneOrNull()?.let {
            AppSettings(
                darkTheme = it.dark_theme != 0L,
                chronoNotifEnabled = it.chrono_notif_enabled != 0L,
                adFree = it.ad_free != 0L,
                language = it.language,
                lastWorkoutType = WorkoutType.parse(it.last_workout_type)
            )
        } ?: AppSettings()
    }

    suspend fun saveAppSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        q.upsertAppSettings(
            dark_theme = if (settings.darkTheme) 1L else 0L,
            black_bg = 0L,
            chrono_notif_enabled = if (settings.chronoNotifEnabled) 1L else 0L,
            ad_free = if (settings.adFree) 1L else 0L,
            language = settings.language,
            last_workout_type = settings.lastWorkoutType.name
        )
    }

    // ─── Export JSON ──────────────────────────────────────────────────────────
    //
    // Format : tableau racine [{...workout}, ...]. Chaque workout embarque selon
    // son type : MUSCULATION -> "exercises", CARDIO -> "cardioExercises",
    // CIRCUIT -> "circuitConfig" + "circuitExercises" (avec "performances").
    // Rétrocompat import : un objet sans "type" est traité comme MUSCULATION.

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
                append("\"type\":${w.type.name.j()}")
                if (w.type == WorkoutType.MUSCULATION) {
                    append(",\"exercises\":[")
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
                    append("]")
                }
                if (w.type == WorkoutType.CARDIO) {
                    append(",\"cardioExercises\":[")
                    w.cardioExercises.forEachIndexed { ei, ce ->
                        if (ei > 0) append(",")
                        append("{")
                        append("\"id\":${ce.id.j()},")
                        append("\"workoutId\":${ce.workoutId.j()},")
                        append("\"name\":${ce.name.j()},")
                        append("\"position\":${ce.position},")
                        append("\"segments\":[")
                        ce.segments.forEachIndexed { si, seg ->
                            if (si > 0) append(",")
                            append("{")
                            append("\"id\":${seg.id.j()},")
                            append("\"cardioExerciseId\":${seg.cardioExerciseId.j()},")
                            append("\"position\":${seg.position},")
                            append("\"intensity\":${seg.intensity.j()},")
                            append("\"durationSeconds\":${seg.durationSeconds}")
                            append("}")
                        }
                        append("]}")
                    }
                    append("]")
                }
                if (w.type == WorkoutType.CIRCUIT) {
                    val cfg = w.circuitConfig
                    if (cfg != null) {
                        append(",\"circuitConfig\":{")
                        append("\"workoutId\":${cfg.workoutId.j()},")
                        append("\"totalRounds\":${cfg.totalRounds},")
                        append("\"restBetweenExercisesSeconds\":${cfg.restBetweenExercisesSeconds},")
                        append("\"restBetweenRoundsSeconds\":${cfg.restBetweenRoundsSeconds}")
                        append("}")
                    }
                    append(",\"circuitExercises\":[")
                    w.circuitExercises.forEachIndexed { ei, ce ->
                        if (ei > 0) append(",")
                        append("{")
                        append("\"id\":${ce.id.j()},")
                        append("\"workoutId\":${ce.workoutId.j()},")
                        append("\"name\":${ce.name.j()},")
                        append("\"position\":${ce.position},")
                        append("\"inputType\":${ce.inputType.name.j()},")
                        append("\"performances\":[")
                        ce.performances.forEachIndexed { pi, p ->
                            if (pi > 0) append(",")
                            append("{")
                            append("\"id\":${p.id.j()},")
                            append("\"circuitExerciseId\":${p.circuitExerciseId.j()},")
                            append("\"roundNumber\":${p.roundNumber},")
                            append("\"reps\":${p.reps ?: "null"},")
                            append("\"weightKg\":${p.weightKg ?: "null"},")
                            append("\"durationSeconds\":${p.durationSeconds ?: "null"},")
                            append("\"notes\":${p.notes.j()}")
                            append("}")
                        }
                        append("]}")
                    }
                    append("]")
                }
                append("}")
            }
            append("]")
        }
    }

    // ─── Import JSON ──────────────────────────────────────────────────────────

    suspend fun importJson(json: String) = withContext(Dispatchers.IO) {
        val workouts = parseJsonWorkouts(json)
        workouts.forEach { saveWorkout(it) }
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

    private fun df(json: String, key: String): Double? {
        if ("\"$key\"\\s*:\\s*null".toRegex().containsMatchIn(json)) return null
        return "\"$key\"\\s*:\\s*(-?[0-9.eE+\\-]+)".toRegex().find(json)?.groupValues?.getOrNull(1)?.toDoubleOrNull()
    }

    private fun inf(json: String, key: String): Int? {
        if ("\"$key\"\\s*:\\s*null".toRegex().containsMatchIn(json)) return null
        return "\"$key\"\\s*:\\s*(-?[0-9]+)".toRegex().find(json)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    private fun lnf(json: String, key: String): Long? {
        if ("\"$key\"\\s*:\\s*null".toRegex().containsMatchIn(json)) return null
        return "\"$key\"\\s*:\\s*(-?[0-9]+)".toRegex().find(json)?.groupValues?.getOrNull(1)?.toLongOrNull()
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

    private fun objBlock(json: String, key: String): String? {
        val ki = json.indexOf("\"$key\""); if (ki < 0) return null
        val os = json.indexOf('{', ki); if (os < 0) return null
        var d = 0
        for (i in os until json.length) when (json[i]) {
            '{' -> d++; '}' -> { d--; if (d == 0) return json.substring(os, i + 1) }
        }
        return null
    }

    private fun parseWorkoutJson(json: String): Workout {
        val wId = sf(json, "id") ?: newId()
        val type = WorkoutType.parse(sf(json, "type"))
        val startedAt = sf(json, "startedAt") ?: Clock.System.now().toString()
        val title = sf(json, "title") ?: ""
        val notes = sf(json, "notes") ?: ""
        val finishedAt = snf(json, "finishedAt")

        // Muscu (toujours parsé si présent, comme avant — rétrocompat)
        val exercises = splitJsonObjects(arrBlock(json, "exercises")).map { parseExerciseJson(it, wId) }

        // Cardio
        val cardioExercises = splitJsonObjects(arrBlock(json, "cardioExercises"))
            .map { parseCardioExerciseJson(it, wId) }

        // Circuit
        val circuitConfig = objBlock(json, "circuitConfig")?.let { block ->
            CircuitConfig(
                workoutId = wId,
                totalRounds = inf(block, "totalRounds") ?: 3,
                restBetweenExercisesSeconds = lnf(block, "restBetweenExercisesSeconds") ?: 0L,
                restBetweenRoundsSeconds = lnf(block, "restBetweenRoundsSeconds") ?: 0L
            )
        }
        val circuitExercises = splitJsonObjects(arrBlock(json, "circuitExercises"))
            .map { parseCircuitExerciseJson(it, wId) }

        return Workout(
            id = wId, title = title, notes = notes,
            startedAt = startedAt, finishedAt = finishedAt, type = type,
            exercises = exercises,
            cardioExercises = cardioExercises,
            circuitConfig = circuitConfig,
            circuitExercises = circuitExercises
        )
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

    private fun parseCardioExerciseJson(json: String, workoutId: String): CardioExercise {
        val ceId = sf(json, "id") ?: newId()
        val segs = splitJsonObjects(arrBlock(json, "segments"))
            .map { parseCardioSegmentJson(it, ceId) }
        return CardioExercise(ceId, workoutId, sf(json, "name") ?: "",
            inf(json, "position") ?: 0, segs)
    }

    private fun parseCardioSegmentJson(json: String, cardioExerciseId: String): CardioSegment =
        CardioSegment(
            id = sf(json, "id") ?: newId(),
            cardioExerciseId = cardioExerciseId,
            position = inf(json, "position") ?: 0,
            intensity = sf(json, "intensity") ?: "",
            durationSeconds = lnf(json, "durationSeconds") ?: 0L
        )

    private fun parseCircuitExerciseJson(json: String, workoutId: String): CircuitExercise {
        val ceId = sf(json, "id") ?: newId()
        val perfs = splitJsonObjects(arrBlock(json, "performances"))
            .map { parseCircuitPerformanceJson(it, ceId) }
        return CircuitExercise(
            id = ceId, workoutId = workoutId,
            name = sf(json, "name") ?: "",
            position = inf(json, "position") ?: 0,
            inputType = CircuitInputType.parse(sf(json, "inputType")),
            performances = perfs
        )
    }

    private fun parseCircuitPerformanceJson(json: String, circuitExerciseId: String): CircuitPerformance =
        CircuitPerformance(
            id = sf(json, "id") ?: newId(),
            circuitExerciseId = circuitExerciseId,
            roundNumber = inf(json, "roundNumber") ?: 1,
            reps = inf(json, "reps"),
            weightKg = df(json, "weightKg"),
            durationSeconds = lnf(json, "durationSeconds"),
            notes = sf(json, "notes") ?: ""
        )

}
