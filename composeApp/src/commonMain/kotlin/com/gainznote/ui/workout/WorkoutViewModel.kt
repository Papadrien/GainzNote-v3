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

fun newId(): String = buildString {
    val hex = "0123456789abcdef"
    repeat(8)  { append(hex.random()) }; append('-')
    repeat(4)  { append(hex.random()) }; append('-')
    append('4'); repeat(3) { append(hex.random()) }; append('-')
    append(listOf('8','9','a','b').random()); repeat(3) { append(hex.random()) }; append('-')
    repeat(12) { append(hex.random()) }
}

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
            repo.getWorkoutById(templateId)?.let { template ->
                _state.value = _state.value.copy(
                    title = template.title,
                    notes = "",
                    exercises = template.exercises.map { ex ->
                        ex.copy(
                            id = newId(),
                            supersetWith = null,
                            sets = ex.sets.map { s ->
                                s.copy(
                                    id = newId(),
                                    reps = null,
                                    repsPlaceholder = s.reps,
                                )
                            }
                        )
                    }
                )
            }
        }

        @OptIn(FlowPreview::class)
        scope.launch {
            state.debounce(10_000).collect { workout ->
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

    fun addSets(exId: String, count: Int = 1) = updateExercises {
        map { ex ->
            if (ex.id != exId) ex
            else ex.copy(sets = ex.sets + List(count) { makeSet(exId) })
        }
    }

    fun removeSet(exId: String, setId: String) = updateExercises {
        map { ex ->
            if (ex.id != exId || ex.sets.size <= 1) ex
            else ex.copy(sets = ex.sets.filter { it.id != setId })
        }
    }

    fun updateSetWeight(exId: String, setId: String, weight: Double?) = updateExercises {
        map { ex ->
            if (ex.id != exId) ex
            else ex.copy(sets = ex.sets.map { s ->
                if (s.id != setId) s else s.copy(weightKg = weight)
            })
        }
    }

    fun updateSetReps(exId: String, setId: String, reps: Int?) = updateExercises {
        map { ex ->
            if (ex.id != exId) ex
            else ex.copy(sets = ex.sets.map { s ->
                if (s.id != setId) s else s.copy(reps = reps)
            })
        }
    }

    fun updateSetNotes(exId: String, setId: String, notes: String) = updateExercises {
        map { ex ->
            if (ex.id != exId) ex
            else ex.copy(sets = ex.sets.map { s ->
                if (s.id != setId) s else s.copy(notes = notes)
            })
        }
    }

    fun propagateWeight(exId: String, setId: String) = updateExercises {
        map { ex ->
            if (ex.id != exId) ex
            else {
                val idx = ex.sets.indexOfFirst { it.id == setId }
                val w = ex.sets.getOrNull(idx)?.weightKg
                if (w == null) ex
                else ex.copy(sets = ex.sets.mapIndexed { i, s ->
                    if (i > idx) s.copy(weightKg = w) else s
                })
            }
        }
    }

    fun linkSuperset(aId: String, bId: String) = updateExercises {
        map { ex ->
            when (ex.id) {
                aId  -> ex.copy(supersetWith = bId)
                bId  -> ex.copy(supersetWith = aId)
                else -> ex
            }
        }
    }

    fun unlinkSuperset(exId: String) {
        val partnerId = _state.value.exercises.firstOrNull { it.id == exId }?.supersetWith
        updateExercises {
            map { ex ->
                if (ex.id == exId || ex.id == partnerId) ex.copy(supersetWith = null) else ex
            }
        }
    }

    fun finish(onDone: () -> Unit) = scope.launch {
        repo.saveWorkout(_state.value.copy(finishedAt = Clock.System.now().toString()))
        onDone()
    }
}
