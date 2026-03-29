// lib/models/models.dart
// Les modèles de données de l'application.
// On utilise des classes immuables avec copyWith() pour faciliter
// les mises à jour d'état avec Riverpod.

import 'package:uuid/uuid.dart';

const _uuid = Uuid();
String newId() => _uuid.v4();

// ─── TrainingSet ──────────────────────────────────────────────────────────────

class TrainingSet {
  final String id;
  final String exerciseId;
  final int position;
  final double? weightKg;
  final int? reps;
  final int? repsPlaceholder; // valeur du workout précédent, affiché en gris
  final String notes;

  const TrainingSet({
    required this.id,
    required this.exerciseId,
    required this.position,
    this.weightKg,
    this.reps,
    this.repsPlaceholder,
    this.notes = '',
  });

  TrainingSet copyWith({
    double? weightKg,
    bool clearWeight = false,
    int? reps,
    bool clearReps = false,
    int? repsPlaceholder,
    String? notes,
    int? position,
  }) =>
      TrainingSet(
        id: id,
        exerciseId: exerciseId,
        position: position ?? this.position,
        weightKg: clearWeight ? null : (weightKg ?? this.weightKg),
        reps: clearReps ? null : (reps ?? this.reps),
        repsPlaceholder: repsPlaceholder ?? this.repsPlaceholder,
        notes: notes ?? this.notes,
      );

  // Conversion depuis/vers Map pour SQLite
  Map<String, dynamic> toMap() => {
        'id': id,
        'exercise_id': exerciseId,
        'position': position,
        'weight_kg': weightKg,
        'reps': reps,
        'reps_placeholder': repsPlaceholder,
        'notes': notes,
      };

  factory TrainingSet.fromMap(Map<String, dynamic> m) => TrainingSet(
        id: m['id'] as String,
        exerciseId: m['exercise_id'] as String,
        position: m['position'] as int,
        weightKg: m['weight_kg'] as double?,
        reps: m['reps'] as int?,
        repsPlaceholder: m['reps_placeholder'] as int?,
        notes: (m['notes'] as String?) ?? '',
      );

  // Pour l'export JSON
  Map<String, dynamic> toJson() => toMap();
  factory TrainingSet.fromJson(Map<String, dynamic> j) => TrainingSet.fromMap(j);
}

// ─── Exercise ─────────────────────────────────────────────────────────────────

class Exercise {
  final String id;
  final String workoutId;
  final String name;
  final int position;
  final String? supersetWith; // id de l'exercice partenaire
  final List<TrainingSet> sets;

  const Exercise({
    required this.id,
    required this.workoutId,
    required this.name,
    required this.position,
    this.supersetWith,
    this.sets = const [],
  });

  Exercise copyWith({
    String? name,
    int? position,
    String? supersetWith,
    bool clearSuperset = false,
    List<TrainingSet>? sets,
  }) =>
      Exercise(
        id: id,
        workoutId: workoutId,
        name: name ?? this.name,
        position: position ?? this.position,
        supersetWith: clearSuperset ? null : (supersetWith ?? this.supersetWith),
        sets: sets ?? this.sets,
      );

  Map<String, dynamic> toMap() => {
        'id': id,
        'workout_id': workoutId,
        'name': name,
        'position': position,
        'superset_with': supersetWith,
      };

  factory Exercise.fromMap(Map<String, dynamic> m) => Exercise(
        id: m['id'] as String,
        workoutId: m['workout_id'] as String,
        name: (m['name'] as String?) ?? '',
        position: m['position'] as int,
        supersetWith: m['superset_with'] as String?,
      );

  Map<String, dynamic> toJson() => {
        ...toMap(),
        'sets': sets.map((s) => s.toJson()).toList(),
      };

  factory Exercise.fromJson(Map<String, dynamic> j) => Exercise.fromMap(j).copyWith(
        sets: (j['sets'] as List<dynamic>?)
                ?.map((s) => TrainingSet.fromJson(s as Map<String, dynamic>))
                .toList() ??
            [],
      );
}

// ─── Workout ──────────────────────────────────────────────────────────────────

class Workout {
  final String id;
  final String title;
  final String notes;
  final DateTime startedAt;
  final DateTime? finishedAt;
  final List<Exercise> exercises;

  const Workout({
    required this.id,
    required this.title,
    required this.notes,
    required this.startedAt,
    this.finishedAt,
    this.exercises = const [],
  });

  Workout copyWith({
    String? title,
    String? notes,
    DateTime? finishedAt,
    bool clearFinished = false,
    List<Exercise>? exercises,
  }) =>
      Workout(
        id: id,
        title: title ?? this.title,
        notes: notes ?? this.notes,
        startedAt: startedAt,
        finishedAt: clearFinished ? null : (finishedAt ?? this.finishedAt),
        exercises: exercises ?? this.exercises,
      );

  bool get isFinished => finishedAt != null;

  Duration get duration =>
      (finishedAt ?? DateTime.now()).difference(startedAt);

  Map<String, dynamic> toMap() => {
        'id': id,
        'title': title,
        'notes': notes,
        'started_at': startedAt.toIso8601String(),
        'finished_at': finishedAt?.toIso8601String(),
      };

  factory Workout.fromMap(Map<String, dynamic> m) => Workout(
        id: m['id'] as String,
        title: (m['title'] as String?) ?? '',
        notes: (m['notes'] as String?) ?? '',
        startedAt: DateTime.parse(m['started_at'] as String),
        finishedAt: m['finished_at'] != null
            ? DateTime.parse(m['finished_at'] as String)
            : null,
      );

  Map<String, dynamic> toJson() => {
        ...toMap(),
        'exercises': exercises.map((e) => e.toJson()).toList(),
      };

  factory Workout.fromJson(Map<String, dynamic> j) => Workout.fromMap(j).copyWith(
        exercises: (j['exercises'] as List<dynamic>?)
                ?.map((e) => Exercise.fromJson(e as Map<String, dynamic>))
                .toList() ??
            [],
      );
}

// ─── Factories ────────────────────────────────────────────────────────────────

TrainingSet makeSet({String exerciseId = '', int position = 0, int? placeholder}) =>
    TrainingSet(
      id: newId(),
      exerciseId: exerciseId,
      position: position,
      repsPlaceholder: placeholder,
    );

Exercise makeExercise({required String workoutId, required int position}) => Exercise(
      id: newId(),
      workoutId: workoutId,
      name: '',
      position: position,
      sets: [makeSet()],
    );
