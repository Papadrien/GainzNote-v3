package com.gainznote.ui.workout

import com.gainznote.model.Exercise
import com.gainznote.model.TrainingSet
import com.gainznote.model.Workout
import com.gainznote.repository.WorkoutRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
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
    private val templateId: String?,
    private val resumeId: String? = null  // reprendre un entraînement en cours
) {
    private val _state = MutableStateFlow(
        Workout(newId(), "", "", Clock.System.now().toString())
    )
    val state: StateFlow<Workout> = _state

    init {
        when {
            resumeId != null -> {
                // Reprendre un entraînement en cours existant
                scope.launch {
                    repo.getWorkoutById(resumeId)?.let { existing ->
                        _state.value = existing
                    }
                }
            }
            templateId != null -> {
                scope.launch {
                    repo.getWorkoutById(templateId)?.let { t ->
                        val idMap = t.exercises.associate { ex -> ex.id to newId() }
                        _state.value = _state.value.copy(
                            title = t.title,
                            notes = "",
                            exercises = t.exercises.map { ex ->
                                ex.copy(
                                    id = idMap[ex.id]!!,
                                    supersetWith = ex.supersetWith?.let { idMap[it] },
                                    sets = ex.sets.map { s ->
                                        s.copy(id = newId(), reps = null, repsPlaceholder = s.reps)
                                    }
                                )
                            }
                        )
                    }
                    // Sauvegarder immédiatement comme "en cours" (finishedAt = null)
                    repo.saveWorkout(_state.value)
                }
            }
            else -> {
                // Nouvel entraînement vide — sauvegarder immédiatement pour apparaître dans "en cours"
                scope.launch { repo.saveWorkout(_state.value) }
            }
        }

        // Autosave : 2s après chaque modification
        @OptIn(FlowPreview::class)
        scope.launch {
            _state.debounce(2000).collect { workout ->
                repo.saveWorkout(workout)
            }
        }
    }

    private fun update(fn: Workout.() -> Workout) { _state.value = _state.value.fn() }
    private fun updateExercises(fn: List<Exercise>.() -> List<Exercise>) =
        update { copy(exercises = exercises.fn()) }

    fun updateTitle(v: String) = update { copy(title = v) }
    fun updateNotes(v: String) = update { copy(notes = v) }

    fun addExercise() = updateExercises { this + makeExercise(_state.value.id, size) }

    fun removeExercise(id: String) {
        val partnerId = _state.value.exercises.firstOrNull { it.id == id }?.supersetWith
        updateExercises {
            filter { it.id != id }
                .map { if (it.id == partnerId) it.copy(supersetWith = null) else it }
        }
    }

    fun updateExerciseName(id: String, name: String) =
        updateExercises { map { if (it.id == id) it.copy(name = name) else it } }

    fun moveExerciseUp(id: String) = updateExercises {
        val idx = indexOfFirst { it.id == id }
        if (idx <= 0) return@updateExercises this
        val list = toMutableList()
        val tmp = list[idx]; list[idx] = list[idx - 1]; list[idx - 1] = tmp
        list
    }

    fun addSets(exId: String, count: Int = 1) = updateExercises {
        map { if (it.id != exId) it else it.copy(sets = it.sets + List(count) { makeSet(exId) }) }
    }

    fun removeSet(exId: String, setId: String) = updateExercises {
        map { ex -> if (ex.id != exId || ex.sets.size <= 1) ex else
            ex.copy(sets = ex.sets.filter { it.id != setId }) }
    }

    fun updateSetWeight(exId: String, setId: String, weight: Double?) = updateExercises {
        map { ex -> if (ex.id != exId) ex else ex.copy(sets = ex.sets.map { s ->
            if (s.id != setId) s else s.copy(weightKg = weight)
        })}
    }

    fun updateSetReps(exId: String, setId: String, reps: Int?) = updateExercises {
        map { ex -> if (ex.id != exId) ex else ex.copy(sets = ex.sets.map { s ->
            if (s.id != setId) s else s.copy(reps = reps)
        })}
    }

    fun updateSetNotes(exId: String, setId: String, notes: String) = updateExercises {
        map { ex -> if (ex.id != exId) ex else ex.copy(sets = ex.sets.map { s ->
            if (s.id != setId) s else s.copy(notes = notes)
        })}
    }

    fun propagateWeight(exId: String, setId: String) = updateExercises {
        map { ex -> if (ex.id != exId) ex else {
            val idx = ex.sets.indexOfFirst { it.id == setId }
            val w = ex.sets.getOrNull(idx)?.weightKg
            if (w == null) ex
            else ex.copy(sets = ex.sets.mapIndexed { i, s -> if (i > idx) s.copy(weightKg = w) else s })
        }}
    }

    fun linkSuperset(aId: String, bId: String) = updateExercises {
        map { when (it.id) { aId -> it.copy(supersetWith = bId); bId -> it.copy(supersetWith = aId); else -> it } }
    }

    fun unlinkSuperset(exId: String) {
        val partnerId = _state.value.exercises.firstOrNull { it.id == exId }?.supersetWith
        updateExercises { map { if (it.id == exId || it.id == partnerId) it.copy(supersetWith = null) else it } }
    }

    // Terminer : marque finishedAt → disparaît de la section "En cours"
    fun finish(onDone: () -> Unit) = scope.launch {
        repo.saveWorkout(_state.value.copy(finishedAt = Clock.System.now().toString()))
        onDone()
    }
}
