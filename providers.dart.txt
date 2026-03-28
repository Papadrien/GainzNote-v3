// lib/providers/providers.dart
// Tous les providers Riverpod de l'app.
// Un provider = une source de données réactive.
// Quand un provider change, tous les widgets qui l'écoutent se rebuilden.

import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/models.dart';
import '../services/database.dart';

// ─── Thème ────────────────────────────────────────────────────────────────────

// Provider simple pour le mode sombre
final darkThemeProvider = StateProvider<bool>((ref) => true);

// ─── Historique ───────────────────────────────────────────────────────────────

// FutureProvider : charge la liste des workouts depuis SQLite
// Utilisé sur l'écran d'accueil et l'historique
final workoutsProvider = FutureProvider<List<Workout>>((ref) async {
  return DatabaseService.getAllWorkouts();
});

// Provider pour un workout spécifique (détail)
final workoutDetailProvider =
    FutureProvider.family<Workout?, String>((ref, id) async {
  return DatabaseService.getWorkoutById(id);
});

// ─── Workout actif ────────────────────────────────────────────────────────────

// StateNotifier qui gère l'état de l'entraînement en cours
class ActiveWorkoutNotifier extends StateNotifier<Workout> {
  ActiveWorkoutNotifier({String? templateId})
      : super(Workout(
          id: newId(),
          title: '',
          notes: '',
          startedAt: DateTime.now(),
        )) {
    if (templateId != null) _loadTemplate(templateId);
    _startAutoSave();
  }

  void _startAutoSave() {
    // Sauvegarde automatique toutes les 30s
    Future.doWhile(() async {
      await Future.delayed(const Duration(seconds: 30));
      if (!mounted) return false;
      await DatabaseService.saveWorkout(state);
      return true;
    });
  }

  Future<void> _loadTemplate(String templateId) async {
    final template = await DatabaseService.getWorkoutById(templateId);
    if (template == null) return;
    state = state.copyWith(
      title: template.title,
      notes: template.notes,
      exercises: template.exercises.map((ex) {
        return ex.copyWith(
          sets: ex.sets.map((s) => s.copyWith(
            repsPlaceholder: s.reps,
            clearReps: true,        // efface les reps, garde le placeholder
          )).toList(),
        );
      }).toList(),
    );
  }

  // ── Workout ────────────────────────────────────────────────────────────────

  void updateTitle(String title) => state = state.copyWith(title: title);
  void updateNotes(String notes) => state = state.copyWith(notes: notes);

  // ── Exercices ──────────────────────────────────────────────────────────────

  void addExercise() {
    final ex = makeExercise(workoutId: state.id, position: state.exercises.length);
    state = state.copyWith(exercises: [...state.exercises, ex]);
  }

  void updateExerciseName(String exId, String name) {
    state = state.copyWith(
      exercises: state.exercises.map((e) {
        return e.id == exId ? e.copyWith(name: name) : e;
      }).toList(),
    );
  }

  void removeExercise(String exId) {
    final partnerId = state.exercises.firstWhere((e) => e.id == exId).supersetWith;
    state = state.copyWith(
      exercises: state.exercises
          .where((e) => e.id != exId)
          .map((e) => e.id == partnerId ? e.copyWith(clearSuperset: true) : e)
          .toList(),
    );
  }

  // ── Séries ─────────────────────────────────────────────────────────────────

  void addSets(String exId, {int count = 1}) {
    state = state.copyWith(
      exercises: state.exercises.map((ex) {
        if (ex.id != exId) return ex;
        final newSets = List.generate(count, (_) => makeSet(exerciseId: exId));
        return ex.copyWith(sets: [...ex.sets, ...newSets]);
      }).toList(),
    );
  }

  void removeSet(String exId, String setId) {
    state = state.copyWith(
      exercises: state.exercises.map((ex) {
        if (ex.id != exId || ex.sets.length <= 1) return ex;
        return ex.copyWith(sets: ex.sets.where((s) => s.id != setId).toList());
      }).toList(),
    );
  }

  void updateSet(String exId, String setId,
      {double? weight, bool clearWeight = false,
       int? reps, bool clearReps = false, String? notes}) {
    state = state.copyWith(
      exercises: state.exercises.map((ex) {
        if (ex.id != exId) return ex;
        return ex.copyWith(
          sets: ex.sets.map((s) {
            if (s.id != setId) return s;
            return s.copyWith(
              weightKg: weight, clearWeight: clearWeight,
              reps: reps, clearReps: clearReps,
              notes: notes,
            );
          }).toList(),
        );
      }).toList(),
    );
  }

  // Propage la charge d'une série vers toutes les suivantes
  void propagateWeight(String exId, String setId) {
    state = state.copyWith(
      exercises: state.exercises.map((ex) {
        if (ex.id != exId) return ex;
        final idx = ex.sets.indexWhere((s) => s.id == setId);
        if (idx < 0) return ex;
        final weight = ex.sets[idx].weightKg;
        return ex.copyWith(
          sets: ex.sets.asMap().entries.map((entry) {
            return entry.key > idx
                ? entry.value.copyWith(weightKg: weight)
                : entry.value;
          }).toList(),
        );
      }).toList(),
    );
  }

  // ── Superset ───────────────────────────────────────────────────────────────

  void linkSuperset(String exAId, String exBId) {
    state = state.copyWith(
      exercises: state.exercises.map((e) {
        if (e.id == exAId) return e.copyWith(supersetWith: exBId);
        if (e.id == exBId) return e.copyWith(supersetWith: exAId);
        return e;
      }).toList(),
    );
  }

  void unlinkSuperset(String exId) {
    final partnerId = state.exercises
        .firstWhere((e) => e.id == exId, orElse: () => state.exercises.first)
        .supersetWith;
    state = state.copyWith(
      exercises: state.exercises.map((e) {
        if (e.id == exId || e.id == partnerId) return e.copyWith(clearSuperset: true);
        return e;
      }).toList(),
    );
  }

  // ── Terminer ───────────────────────────────────────────────────────────────

  Future<void> finishWorkout() async {
    state = state.copyWith(finishedAt: DateTime.now());
    await DatabaseService.saveWorkout(state);
  }
}

// Le provider du workout actif. On le crée avec .family pour passer le templateId.
final activeWorkoutProvider = StateNotifierProvider.family
    .autoDispose<ActiveWorkoutNotifier, Workout, String?>(
  (ref, templateId) => ActiveWorkoutNotifier(templateId: templateId),
);
