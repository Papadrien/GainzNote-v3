package com.gainznote.ui.circuit

import com.gainznote.model.CircuitConfig
import com.gainznote.model.CircuitExercise
import com.gainznote.model.CircuitInputType
import com.gainznote.model.CircuitPerformance
import com.gainznote.model.Workout
import com.gainznote.model.WorkoutType
import com.gainznote.repository.WorkoutRepository
import com.gainznote.ui.workout.newId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

fun makeCircuitExercise(workoutId: String, pos: Int) =
    CircuitExercise(
        id = newId(), workoutId = workoutId, name = "", position = pos,
        inputType = CircuitInputType.REPS
    )

class CircuitViewModel(
    private val repo: WorkoutRepository,
    private val scope: CoroutineScope,
    private val templateId: String?,
    private val resumeId: String? = null,
    private var saveJob: Job? = null,
    private var isFinished: Boolean = false
) {
    private val _state = MutableStateFlow(
        Workout(
            id = resumeId ?: newId(),
            title = "",
            notes = "",
            startedAt = Clock.System.now().toString(),
            type = WorkoutType.CIRCUIT,
            circuitConfig = CircuitConfig(workoutId = resumeId ?: "")
        ).let { w ->
            // S'assurer que la config a le bon workoutId
            w.copy(circuitConfig = w.circuitConfig?.copy(workoutId = w.id))
        }
    )
    val state: StateFlow<Workout> = _state

    init {
        when {
            resumeId != null ->         scope.launch {
                repo.getWorkoutById(resumeId)?.let { existing ->
                    // Garantir qu'un circuit a toujours une config non-null
                    val cfg = existing.circuitConfig ?: CircuitConfig(workoutId = existing.id)
                    _state.value = existing.copy(circuitConfig = cfg)
                }
            }
            templateId != null ->         scope.launch {
                repo.getWorkoutById(templateId)?.let { t ->
                    val newExercises = t.circuitExercises.map { ce ->
                        val firstPerf = ce.performances.find { it.roundNumber == 1 }
                        // Copier la structure avec les références du 1er tour du template
                        ce.copy(
                            id = newId(), 
                            performances = emptyList(),
                            referenceReps = firstPerf?.reps,
                            referenceWeightKg = firstPerf?.weightKg,
                            referenceDurationSeconds = firstPerf?.durationSeconds
                        )
                    }
                    val cfg = t.circuitConfig?.copy(workoutId = _state.value.id)
                        ?: CircuitConfig(workoutId = _state.value.id)
                    _state.value = _state.value.copy(
                        title = t.title,
                        notes = "",
                        type = WorkoutType.CIRCUIT,
                        circuitConfig = cfg,
                        circuitExercises = newExercises
                    )
                }
                repo.saveWorkout(_state.value)
            }
            else ->         scope.launch { repo.saveWorkout(_state.value) }
        }

        @OptIn(FlowPreview::class)
        saveJob =         scope.launch {
            _state.debounce(2000).collect { w -> if (!isFinished) repo.saveWorkout(w) }
        }
    }

    private fun update(fn: Workout.() -> Workout) { _state.value = _state.value.fn() }
    private fun updateExercises(fn: List<CircuitExercise>.() -> List<CircuitExercise>) =
        update { copy(circuitExercises = circuitExercises.fn()) }

    fun updateTitle(v: String) = update { copy(title = v) }
    fun updateNotes(v: String) = update { copy(notes = v) }

    // Config
    fun updateTotalRounds(v: Int) = update {
        copy(circuitConfig = (circuitConfig ?: CircuitConfig(workoutId = id)).copy(totalRounds = v.coerceAtLeast(1)))
    }
    fun updateRestBetweenExercises(v: Long) = update {
        copy(circuitConfig = (circuitConfig ?: CircuitConfig(workoutId = id))
            .copy(restBetweenExercisesSeconds = v.coerceAtLeast(0)))
    }
    fun updateRestBetweenRounds(v: Long) = update {
        copy(circuitConfig = (circuitConfig ?: CircuitConfig(workoutId = id))
            .copy(restBetweenRoundsSeconds = v.coerceAtLeast(0)))
    }

    // Exercices
    fun addExercise() = updateExercises { this + makeCircuitExercise(_state.value.id, size) }

    fun removeExercise(id: String) = updateExercises { filter { it.id != id } }

    fun updateExerciseName(exId: String, name: String) = updateExercises {
        map { if (it.id == exId) it.copy(name = name) else it }
    }

    fun updateInputType(exId: String, type: CircuitInputType) = updateExercises {
        map { if (it.id == exId) it.copy(inputType = type) else it }
    }

    fun moveExerciseUp(id: String) = updateExercises {
        val idx = indexOfFirst { it.id == id }
        if (idx <= 0) return@updateExercises this
        val list = toMutableList()
        val tmp = list[idx]; list[idx] = list[idx - 1]; list[idx - 1] = tmp
        list
    }

    // Performances (pendant la séance)
    fun upsertPerformance(
        exId: String,
        roundNumber: Int,
        reps: Int?,
        weightKg: Double?,
        durationSeconds: Long?,
        notes: String = ""
    ) = updateExercises {
        map { ce ->
            if (ce.id != exId) ce
            else {
                val existing = ce.performances.firstOrNull { it.roundNumber == roundNumber }
                val newPerf = CircuitPerformance(
                    id = existing?.id ?: newId(),
                    circuitExerciseId = exId,
                    roundNumber = roundNumber,
                    reps = reps,
                    weightKg = weightKg,
                    durationSeconds = durationSeconds,
                    notes = notes
                )
                val withoutOld = ce.performances.filter { it.roundNumber != roundNumber }
                ce.copy(performances = (withoutOld + newPerf).sortedBy { it.roundNumber })
            }
        }
    }

    fun finish(onDone: () -> Unit) {
        isFinished = true
        scope.launch {
            saveJob?.cancel()
            repo.saveWorkout(_state.value.copy(finishedAt = Clock.System.now().toString()))
            onDone()
        }
    }
}
