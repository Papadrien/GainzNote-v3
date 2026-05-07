package fr.junade.gainznote.ui.workout

import fr.junade.gainznote.model.Exercise
import fr.junade.gainznote.model.TrainingSet
import fr.junade.gainznote.model.Workout
import fr.junade.gainznote.repository.WorkoutRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

private var _idCounter = 0
fun newId(): String {
    _idCounter++
    return Clock.System.now().toEpochMilliseconds().toString() + "_" + _idCounter + "_" + (1000..9999).random()
}
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
        // En mode resume, utiliser le resumeId comme ID initial pour éviter
        // que l'autosave (debounce 2s) sauvegarde un état vide sous un ID temporaire
        // avant que getWorkoutById ait eu le temps de charger le vrai entraînement.
        Workout(resumeId ?: newId(), "", "", Clock.System.now().toString())
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
                    // Si le workout n'existe plus (supprimé), on repart d'un état vide
                    // avec l'ID correct, sans écraser quoi que ce soit d'autre
                }
            }
            templateId != null -> {
                scope.launch {
                    repo.getWorkoutById(templateId)?.let { t ->
                        val idMap = t.exercises.associate { ex -> ex.id to newId() }
                        // Mapper aussi les supersetId (identifiants de groupe) vers de nouveaux IDs
                        val supersetGroupMap = t.exercises.mapNotNull { it.supersetId }.distinct()
                            .associateWith { newId() }
                        _state.value = _state.value.copy(
                            title = t.title,
                            notes = "",
                            exercises = t.exercises.map { ex ->
                                ex.copy(
                                    id = idMap[ex.id]!!,
                                    supersetId = ex.supersetId?.let { supersetGroupMap[it] },
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
        val groupId = _state.value.exercises.firstOrNull { it.id == id }?.supersetId
        val groupSize = if (groupId != null) _state.value.exercises.count { it.supersetId == groupId } else 0
        updateExercises {
            filter { it.id != id }.map { ex ->
                // Si le groupe n'a plus qu'un membre après suppression, on dissout
                if (ex.supersetId == groupId && groupSize <= 2) ex.copy(supersetId = null) else ex
            }
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

    // Associe plusieurs exercices dans un même superset (groupe identifié par un supersetId partagé)
    fun linkSuperset(srcId: String, targetIds: List<String>) {
        val existing = _state.value.exercises.firstOrNull { it.id == srcId }?.supersetId
        val groupId = existing ?: newId()
        updateExercises {
            map { ex ->
                when {
                    ex.id == srcId || ex.id in targetIds -> ex.copy(supersetId = groupId)
                    else -> ex
                }
            }
        }
    }

    fun unlinkSuperset(exId: String) {
        val groupId = _state.value.exercises.firstOrNull { it.id == exId }?.supersetId
        // Si le groupe ne contient que 2 exercices, on dissout tout le groupe
        val groupMembers = _state.value.exercises.filter { it.supersetId == groupId }
        if (groupMembers.size <= 2) {
            updateExercises { map { if (it.supersetId == groupId) it.copy(supersetId = null) else it } }
        } else {
            updateExercises { map { if (it.id == exId) it.copy(supersetId = null) else it } }
        }
    }

    // Terminer : marque finishedAt → disparaît de la section "En cours"
    fun finish(onDone: () -> Unit) = scope.launch {
        repo.saveWorkout(_state.value.copy(finishedAt = Clock.System.now().toString()))
        onDone()
    }
}
