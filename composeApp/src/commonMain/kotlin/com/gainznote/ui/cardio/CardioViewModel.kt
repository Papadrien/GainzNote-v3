package com.gainznote.ui.cardio

import com.gainznote.model.CardioExercise
import com.gainznote.model.CardioSegment
import com.gainznote.model.Workout
import com.gainznote.model.WorkoutType
import com.gainznote.repository.WorkoutRepository
import com.gainznote.ui.workout.newId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

fun makeCardioSegment(cardioExerciseId: String = "") =
    CardioSegment(id = newId(), cardioExerciseId = cardioExerciseId, position = 0)

fun makeCardioExercise(workoutId: String, pos: Int) =
    CardioExercise(
        id = newId(), workoutId = workoutId, name = "", position = pos,
        segments = listOf(makeCardioSegment())
    )

class CardioViewModel(
    private val repo: WorkoutRepository,
    private val scope: CoroutineScope,
    private val templateId: String?,
    private val resumeId: String? = null
) {
    private val _state = MutableStateFlow(
        Workout(
            id = resumeId ?: newId(),
            title = "",
            notes = "",
            startedAt = Clock.System.now().toString(),
            type = WorkoutType.CARDIO
        )
    )
    val state: StateFlow<Workout> = _state

    init {
        when {
            resumeId != null -> scope.launch {
                repo.getWorkoutById(resumeId)?.let { existing ->
                    _state.value = existing
                }
            }
            templateId != null -> scope.launch {
                repo.getWorkoutById(templateId)?.let { t ->
                    val newExercises = t.cardioExercises.map { ce ->
                        val newCeId = newId()
                        ce.copy(
                            id = newCeId,
                            segments = ce.segments.map { seg ->
                                seg.copy(id = newId(), cardioExerciseId = newCeId)
                            }
                        )
                    }
                    _state.value = _state.value.copy(
                        title = t.title,
                        notes = "",
                        type = WorkoutType.CARDIO,
                        cardioExercises = newExercises
                    )
                }
                repo.saveWorkout(_state.value)
            }
            else -> scope.launch { repo.saveWorkout(_state.value) }
        }

        @OptIn(FlowPreview::class)
        scope.launch {
            _state.debounce(2000).collect { w -> repo.saveWorkout(w) }
        }
    }

    private fun update(fn: Workout.() -> Workout) { _state.value = _state.value.fn() }
    private fun updateExercises(fn: List<CardioExercise>.() -> List<CardioExercise>) =
        update { copy(cardioExercises = cardioExercises.fn()) }

    fun updateTitle(v: String) = update { copy(title = v) }
    fun updateNotes(v: String) = update { copy(notes = v) }

    fun addExercise() = updateExercises { this + makeCardioExercise(_state.value.id, size) }

    fun removeExercise(id: String) = updateExercises { filter { it.id != id } }

    fun updateExerciseName(exId: String, name: String) = updateExercises {
        map { if (it.id == exId) it.copy(name = name) else it }
    }

    fun addSegment(exId: String) = updateExercises {
        map { ce ->
            if (ce.id != exId) ce
            else ce.copy(segments = ce.segments + makeCardioSegment(exId))
        }
    }

    fun removeSegment(exId: String, segId: String) = updateExercises {
        map { ce ->
            if (ce.id != exId || ce.segments.size <= 1) ce
            else ce.copy(segments = ce.segments.filter { it.id != segId })
        }
    }

    fun updateSegmentIntensity(exId: String, segId: String, intensity: String) = updateExercises {
        map { ce ->
            if (ce.id != exId) ce
            else ce.copy(segments = ce.segments.map { s ->
                if (s.id != segId) s else s.copy(intensity = intensity)
            })
        }
    }

    fun updateSegmentDuration(exId: String, segId: String, durationSec: Long) = updateExercises {
        map { ce ->
            if (ce.id != exId) ce
            else ce.copy(segments = ce.segments.map { s ->
                if (s.id != segId) s else s.copy(durationSeconds = durationSec)
            })
        }
    }

    fun finish(onDone: () -> Unit) = scope.launch {
        val finishedWorkout = _state.value.copy(finishedAt = Clock.System.now().toString())
        repo.saveWorkout(finishedWorkout)
        // S'assurer que l'état local reflète la fin
        _state.value = finishedWorkout
        onDone()
    }
}
