package com.gainznote.ui.workout

import com.gainznote.model.Exercise
import com.gainznote.model.TrainingSet
import com.gainznote.model.Workout
import com.gainznote.repository.WorkoutRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

fun newId() = Clock.System.now().toEpochMilliseconds().toString() + (1000..9999).random()
fun makeSet(exId: String = "", pos: Int = 0, placeholder: Int? = null) =
    TrainingSet(newId(), exId, pos, repsPlaceholder = placeholder)
fun makeExercise(workoutId: String, pos: Int) =
    Exercise(newId(), workoutId, "", pos, sets = listOf(makeSet()))

class WorkoutViewModel(
    private val repo: WorkoutRepository,
    private val scope: CoroutineScope,
    templateId: String?
) {
    private val _state = MutableStateFlow(
        Workout(newId(), "", "", Clock.System.now().toString())
    )
    val state: StateFlow<Workout> = _state

    init {
        if (templateId != null) scope.launch {
            repo.getWorkoutById(templateId)?.let { t ->
                _state.value = _state.value.copy(
                    title = t.title, notes = t.notes,
                    exercises = t.exercises.map { ex ->
                        ex.copy(id = newId(), supersetWith = null,
                            sets = ex.sets.map { s ->
                                s.copy(id = newId(), reps = null, repsPlaceholder = s.reps)
                            })
                    }
                )
            }
        }
        // Autosave toutes les 30s
        scope.launch {
            while (true) {
                kotlinx.coroutines.delay(30_000)
                repo.saveWorkout(_state.value)
            }
        }
    }

    private fun update(fn: Workout.() -> Workout) { _state.value = _state.value.fn() }
    private fun updateExercises(fn: List<Exercise>.() -> List<Exercise>) =
        update { copy(exercises = exercises.fn()) }

    fun updateTitle(v: String) = update { copy(title = v) }
    fun updateNotes(v: String) = update { copy(notes = v) }

    fun addExercise() = updateExercises { this + makeExercise(_state.value.id, size) }
    fun removeExercise(id: String) = updateExercises { filter { it.id != id }.also {
        // délie le superset si nécessaire
        val partnerId = firstOrNull { it.id == id }?.supersetWith
        map { if (it.id == partnerId) it.copy(supersetWith = null) else it }
    }}
    fun updateExerciseName(id: String, name: String) =
        updateExercises { map { if (it.id == id) it.copy(name = name) else it } }

    fun addSets(exId: String, count: Int = 1) = updateExercises {
        map { if (it.id != exId) it else
            it.copy(sets = it.sets + List(count) { makeSet(exId) }) }
    }
    fun removeSet(exId: String, setId: String) = updateExercises {
        map { ex -> if (ex.id != exId || ex.sets.size <= 1) ex else
            ex.copy(sets = ex.sets.filter { it.id != setId }) }
    }
    fun updateSet(exId: String, setId: String, weight: Double? = null,
                  clearWeight: Boolean = false, reps: Int? = null,
                  clearReps: Boolean = false, notes: String? = null) = updateExercises {
        map { ex -> if (ex.id != exId) ex else ex.copy(sets = ex.sets.map { s ->
            if (s.id != setId) s else s.copy(
                weightKg = if (clearWeight) null else weight ?: s.weightKg,
                reps = if (clearReps) null else reps ?: s.reps,
                notes = notes ?: s.notes
            )
        })}
    }
    fun propagateWeight(exId: String, setId: String) = updateExercises {
        map { ex -> if (ex.id != exId) ex else {
            val idx = ex.sets.indexOfFirst { it.id == setId }
            val w = ex.sets.getOrNull(idx)?.weightKg
            ex.copy(sets = ex.sets.mapIndexed { i, s -> if (i > idx) s.copy(weightKg = w) else s })
        }}
    }
    fun linkSuperset(aId: String, bId: String) = updateExercises {
        map { when (it.id) { aId -> it.copy(supersetWith = bId); bId -> it.copy(supersetWith = aId); else -> it } }
    }
    fun unlinkSuperset(exId: String) {
        val partnerId = _state.value.exercises.firstOrNull { it.id == exId }?.supersetWith
        updateExercises { map { if (it.id == exId || it.id == partnerId) it.copy(supersetWith = null) else it } }
    }

    fun finish(onDone: () -> Unit) = scope.launch {
        repo.saveWorkout(_state.value.copy(finishedAt = Clock.System.now().toString()))
        onDone()
    }
}
