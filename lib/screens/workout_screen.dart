// lib/screens/workout_screen.dart

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/models.dart';
import '../providers/providers.dart';
import '../theme/theme.dart';
import '../widgets/common.dart';

class WorkoutScreen extends ConsumerStatefulWidget {
  final String? templateId;
  const WorkoutScreen({super.key, required this.templateId});

  @override
  ConsumerState<WorkoutScreen> createState() => _WorkoutScreenState();
}

class _WorkoutScreenState extends ConsumerState<WorkoutScreen> {
  String? _supersetSourceId;
  bool _showSupersetPicker = false;

  @override
  Widget build(BuildContext context) {
    final dark = ref.watch(darkThemeProvider);
    final c = GainzThemeColors(dark: dark);
    final workout = ref.watch(activeWorkoutProvider(widget.templateId));
    final notifier = ref.read(activeWorkoutProvider(widget.templateId).notifier);

    return Scaffold(
      backgroundColor: c.background,
      body: SafeArea(
        child: Column(
          children: [
            // ── Top bar ───────────────────────────────────────────────────
            _TopBar(
              c: c,
              startedAt: workout.startedAt,
              onFinish: () => _confirmFinish(context, c, notifier),
            ),

            // ── Contenu scrollable ────────────────────────────────────────
            Expanded(
              child: ListView(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                children: [
                  const SizedBox(height: 12),

                  // Titre
                  TextField(
                    onChanged: notifier.updateTitle,
                    controller: TextEditingController(text: workout.title)
                      ..selection = TextSelection.collapsed(offset: workout.title.length),
                    style: TextStyle(
                        color: c.text,
                        fontSize: 22,
                        fontWeight: FontWeight.bold),
                    decoration: InputDecoration(
                      hintText: 'Titre de l\'entraînement',
                      hintStyle: TextStyle(color: c.textMuted),
                      border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide(color: c.border)),
                      enabledBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide(color: c.border)),
                      focusedBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide(color: c.accent, width: 1.5)),
                      filled: true,
                      fillColor: c.surface,
                    ),
                  ),
                  const SizedBox(height: 8),

                  // Notes générales
                  TextField(
                    onChanged: notifier.updateNotes,
                    maxLines: 2,
                    style: TextStyle(color: c.text, fontSize: 14),
                    decoration: InputDecoration(
                      hintText: 'Notes générales…',
                      hintStyle: TextStyle(color: c.textMuted),
                      border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide(color: c.border)),
                      enabledBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide(color: c.border)),
                      focusedBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide(color: c.accent, width: 1.5)),
                      filled: true,
                      fillColor: c.surface,
                    ),
                  ),
                  const SizedBox(height: 20),

                  // Exercices
                  ...workout.exercises.asMap().entries.map((entry) {
                    final idx = entry.key;
                    final ex = entry.value;
                    // Ne pas re-afficher le 2e d'un superset
                    final isSecond = idx > 0 &&
                        workout.exercises[idx - 1].supersetWith == ex.id;
                    if (isSecond) return const SizedBox.shrink();

                    final partner = ex.supersetWith != null
                        ? workout.exercises
                            .where((e) => e.id == ex.supersetWith)
                            .firstOrNull
                        : null;

                    return Padding(
                      padding: const EdgeInsets.only(bottom: 12),
                      child: ExerciseCard(
                        exercise: ex,
                        partner: partner,
                        c: c,
                        notifier: notifier,
                        allExercises: workout.exercises,
                      ),
                    );
                  }),

                  // Ajouter exercice
                  OutlinedButton(
                    onPressed: notifier.addExercise,
                    style: OutlinedButton.styleFrom(
                      foregroundColor: c.accent,
                      side: BorderSide(color: c.border),
                      shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12)),
                      minimumSize: const Size.fromHeight(52),
                    ),
                    child: const Text('+ Ajouter un exercice',
                        style: TextStyle(fontWeight: FontWeight.w600)),
                  ),
                  const SizedBox(height: 40),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _confirmFinish(BuildContext context, GainzThemeColors c,
      ActiveWorkoutNotifier notifier) {
    final workout = ref.read(activeWorkoutProvider(widget.templateId));
    showDialog(
      context: context,
      builder: (_) => AlertDialog(
        backgroundColor: c.surface,
        title: Text('Terminer l\'entraînement ?',
            style: TextStyle(color: c.text)),
        content: Text(
          '${workout.exercises.length} exercice(s) · '
          '${workout.exercises.fold(0, (sum, e) => sum + e.sets.length)} série(s)\n'
          'Durée : ${formatDuration(DateTime.now().difference(workout.startedAt))}',
          style: TextStyle(color: c.textSec),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text('Continuer', style: TextStyle(color: c.textMuted)),
          ),
          ElevatedButton(
            onPressed: () async {
              await notifier.finishWorkout();
              if (context.mounted) {
                Navigator.pop(context); // ferme dialog
                Navigator.pop(context); // retour home
              }
            },
            style: ElevatedButton.styleFrom(
                backgroundColor: c.accent, foregroundColor: Colors.black),
            child:
                const Text('Terminer ✓', style: TextStyle(fontWeight: FontWeight.bold)),
          ),
        ],
      ),
    );
  }
}

// ─── Top bar ──────────────────────────────────────────────────────────────────

class _TopBar extends StatefulWidget {
  final GainzThemeColors c;
  final DateTime startedAt;
  final VoidCallback onFinish;
  const _TopBar({required this.c, required this.startedAt, required this.onFinish});

  @override
  State<_TopBar> createState() => _TopBarState();
}

class _TopBarState extends State<_TopBar> {
  late String _elapsed;

  @override
  void initState() {
    super.initState();
    _elapsed = formatDuration(DateTime.now().difference(widget.startedAt));
    _tick();
  }

  void _tick() async {
    while (mounted) {
      await Future.delayed(const Duration(seconds: 30));
      if (!mounted) return;
      setState(() {
        _elapsed = formatDuration(DateTime.now().difference(widget.startedAt));
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      color: widget.c.background,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text('GainzNote',
                style: TextStyle(
                    color: widget.c.accent,
                    fontSize: 20,
                    fontWeight: FontWeight.w900,
                    letterSpacing: -0.5)),
            Text(
              'Démarré à ${formatTime(widget.startedAt)} · $_elapsed',
              style: TextStyle(color: widget.c.textMuted, fontSize: 12),
            ),
          ]),
          ElevatedButton(
            onPressed: widget.onFinish,
            style: ElevatedButton.styleFrom(
              backgroundColor: widget.c.accent,
              foregroundColor: Colors.black,
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(10)),
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
            ),
            child:
                const Text('Terminer', style: TextStyle(fontWeight: FontWeight.bold)),
          ),
        ],
      ),
    );
  }
}

// ─── ExerciseCard ─────────────────────────────────────────────────────────────

class ExerciseCard extends StatelessWidget {
  final Exercise exercise;
  final Exercise? partner;
  final GainzThemeColors c;
  final ActiveWorkoutNotifier notifier;
  final List<Exercise> allExercises;

  const ExerciseCard({
    super.key,
    required this.exercise,
    required this.partner,
    required this.c,
    required this.notifier,
    required this.allExercises,
  });

  @override
  Widget build(BuildContext context) {
    final isSuperset = partner != null;

    return Container(
      decoration: BoxDecoration(
        color: c.surface,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(
            color: isSuperset ? c.superset : c.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header
          Padding(
            padding: const EdgeInsets.fromLTRB(12, 10, 4, 0),
            child: Row(
              children: [
                if (isSuperset)
                  Container(
                    margin: const EdgeInsets.only(right: 8),
                    padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                    decoration: BoxDecoration(
                      color: c.supersetDim,
                      borderRadius: BorderRadius.circular(5),
                    ),
                    child: Text('SUPERSET',
                        style: TextStyle(
                            color: c.superset,
                            fontSize: 9,
                            fontWeight: FontWeight.bold,
                            letterSpacing: 0.8)),
                  ),
                Expanded(
                  child: TextField(
                    onChanged: (v) => notifier.updateExerciseName(exercise.id, v),
                    style: TextStyle(
                        color: c.text,
                        fontSize: 15,
                        fontWeight: FontWeight.w600),
                    decoration: InputDecoration(
                      hintText: 'Nom de l\'exercice',
                      hintStyle: TextStyle(color: c.textMuted, fontSize: 15),
                      border: InputBorder.none,
                      enabledBorder: InputBorder.none,
                      focusedBorder: InputBorder.none,
                      contentPadding: EdgeInsets.zero,
                      isDense: true,
                    ),
                    controller: TextEditingController(text: exercise.name)
                      ..selection = TextSelection.collapsed(
                          offset: exercise.name.length),
                  ),
                ),
                _ExerciseMenu(
                  c: c,
                  isSuperset: isSuperset,
                  onRemove: () => notifier.removeExercise(exercise.id),
                  onLinkSuperset: () => _showSupersetDialog(context),
                  onUnlinkSuperset: () => notifier.unlinkSuperset(exercise.id),
                ),
              ],
            ),
          ),

          Divider(color: c.border, thickness: 0.5, height: 16),

          // En-têtes colonnes
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 12),
            child: Row(
              children: [
                SizedBox(width: 28, child: Text('#', style: TextStyle(color: c.textMuted, fontSize: 11))),
                Expanded(child: Text('kg', style: TextStyle(color: c.textMuted, fontSize: 11))),
                Expanded(child: Text('reps', style: TextStyle(color: c.textMuted, fontSize: 11))),
                Expanded(flex: 2, child: Text('note', style: TextStyle(color: c.textMuted, fontSize: 11))),
                const SizedBox(width: 64),
              ],
            ),
          ),
          const SizedBox(height: 4),

          // Séries
          ...exercise.sets.asMap().entries.map((entry) => SetRow(
                index: entry.key,
                set: entry.value,
                c: c,
                onUpdate: (w, r, n) =>
                    notifier.updateSet(exercise.id, entry.value.id,
                        weight: w, reps: r, notes: n),
                onPropagate: () =>
                    notifier.propagateWeight(exercise.id, entry.value.id),
                onRemove: () => notifier.removeSet(exercise.id, entry.value.id),
              )),

          // Boutons ajout séries
          Padding(
            padding: const EdgeInsets.fromLTRB(8, 4, 8, 8),
            child: Row(
              children: [
                Expanded(
                  child: TextButton(
                    onPressed: () => notifier.addSets(exercise.id),
                    child: Text('+ Série', style: TextStyle(color: c.accent, fontSize: 13)),
                  ),
                ),
                Expanded(
                  child: TextButton(
                    onPressed: () => _showAddSetsDialog(context),
                    child: Text('+ Plusieurs', style: TextStyle(color: c.accent, fontSize: 13)),
                  ),
                ),
              ],
            ),
          ),

          // Partenaire superset
          if (isSuperset && partner != null) ...[
            Divider(color: c.superset.withOpacity(0.3), height: 1),
            Container(
              width: double.infinity,
              color: c.supersetDim.withOpacity(0.4),
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              child: Text(
                '↕ ${partner!.name.isEmpty ? "Exercice partenaire" : partner!.name}',
                style: TextStyle(color: c.superset, fontSize: 13, fontWeight: FontWeight.w500),
              ),
            ),
          ],
        ],
      ),
    );
  }

  void _showSupersetDialog(BuildContext context) {
    final candidates = allExercises
        .where((e) => e.id != exercise.id && e.supersetWith == null)
        .toList();
    showDialog(
      context: context,
      builder: (_) => AlertDialog(
        backgroundColor: c.surface,
        title: Text('Associer en superset', style: TextStyle(color: c.text)),
        content: candidates.isEmpty
            ? Text('Aucun autre exercice disponible.',
                style: TextStyle(color: c.textSec))
            : Column(
                mainAxisSize: MainAxisSize.min,
                children: candidates
                    .map((ex) => ListTile(
                          title: Text(
                              ex.name.isEmpty ? 'Exercice sans nom' : ex.name,
                              style: TextStyle(color: c.superset)),
                          onTap: () {
                            notifier.linkSuperset(exercise.id, ex.id);
                            Navigator.pop(context);
                          },
                        ))
                    .toList(),
              ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text('Annuler', style: TextStyle(color: c.textMuted)),
          ),
        ],
      ),
    );
  }

  void _showAddSetsDialog(BuildContext context) {
    int count = 3;
    showDialog(
      context: context,
      builder: (_) => StatefulBuilder(
        builder: (ctx, setState) => AlertDialog(
          backgroundColor: c.surface,
          title: Text('Ajouter des séries', style: TextStyle(color: c.text)),
          content: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              IconButton(
                onPressed: () => setState(() => count = (count - 1).clamp(1, 20)),
                icon: Icon(Icons.remove, color: c.textSec),
              ),
              Text('$count', style: TextStyle(color: c.text, fontSize: 24, fontWeight: FontWeight.bold)),
              IconButton(
                onPressed: () => setState(() => count = (count + 1).clamp(1, 20)),
                icon: Icon(Icons.add, color: c.textSec),
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: Text('Annuler', style: TextStyle(color: c.textMuted)),
            ),
            ElevatedButton(
              onPressed: () {
                notifier.addSets(exercise.id, count: count);
                Navigator.pop(context);
              },
              style: ElevatedButton.styleFrom(
                  backgroundColor: c.accent, foregroundColor: Colors.black),
              child: const Text('Ajouter'),
            ),
          ],
        ),
      ),
    );
  }
}

// ─── SetRow ───────────────────────────────────────────────────────────────────

class SetRow extends StatelessWidget {
  final int index;
  final TrainingSet set;
  final GainzThemeColors c;
  final void Function(double?, int?, String?) onUpdate;
  final VoidCallback onPropagate;
  final VoidCallback onRemove;

  const SetRow({
    super.key,
    required this.index,
    required this.set,
    required this.c,
    required this.onUpdate,
    required this.onPropagate,
    required this.onRemove,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 3),
      child: Row(
        children: [
          SizedBox(
            width: 28,
            child: Text('${index + 1}', style: TextStyle(color: c.textMuted, fontSize: 12)),
          ),
          Expanded(
            child: SetField(
              value: set.weightKg?.toString(),
              placeholder: '0',
              keyboardType: const TextInputType.numberWithOptions(decimal: true),
              c: c,
              onChanged: (v) => onUpdate(double.tryParse(v), null, null),
            ),
          ),
          const SizedBox(width: 4),
          Expanded(
            child: SetField(
              value: set.reps?.toString(),
              placeholder: set.repsPlaceholder?.toString() ?? '0',
              placeholderIsHint: set.repsPlaceholder != null,
              keyboardType: TextInputType.number,
              c: c,
              onChanged: (v) => onUpdate(null, int.tryParse(v), null),
            ),
          ),
          const SizedBox(width: 4),
          Expanded(
            flex: 2,
            child: SetField(
              value: set.notes.isEmpty ? null : set.notes,
              placeholder: '…',
              c: c,
              onChanged: (v) => onUpdate(null, null, v),
            ),
          ),
          const SizedBox(width: 4),
          // Propager le poids
          GestureDetector(
            onTap: onPropagate,
            child: Padding(
              padding: const EdgeInsets.all(6),
              child: Text('⬇', style: TextStyle(color: c.textMuted, fontSize: 14)),
            ),
          ),
          // Supprimer
          GestureDetector(
            onTap: onRemove,
            child: Padding(
              padding: const EdgeInsets.all(6),
              child: Text('✕', style: TextStyle(color: c.danger, fontSize: 12)),
            ),
          ),
        ],
      ),
    );
  }
}

// ─── Menu exercice ────────────────────────────────────────────────────────────

class _ExerciseMenu extends StatelessWidget {
  final GainzThemeColors c;
  final bool isSuperset;
  final VoidCallback onRemove;
  final VoidCallback onLinkSuperset;
  final VoidCallback onUnlinkSuperset;

  const _ExerciseMenu({
    required this.c,
    required this.isSuperset,
    required this.onRemove,
    required this.onLinkSuperset,
    required this.onUnlinkSuperset,
  });

  @override
  Widget build(BuildContext context) {
    return PopupMenuButton<String>(
      color: c.surface,
      icon: Icon(Icons.more_vert, color: c.textMuted),
      onSelected: (v) {
        if (v == 'superset') onLinkSuperset();
        if (v == 'unsuperset') onUnlinkSuperset();
        if (v == 'delete') onRemove();
      },
      itemBuilder: (_) => [
        if (!isSuperset)
          PopupMenuItem(
            value: 'superset',
            child: Text('Associer en superset', style: TextStyle(color: c.text)),
          ),
        if (isSuperset)
          PopupMenuItem(
            value: 'unsuperset',
            child: Text('Retirer le superset', style: TextStyle(color: c.superset)),
          ),
        PopupMenuItem(
          value: 'delete',
          child: Text('Supprimer l\'exercice', style: TextStyle(color: c.danger)),
        ),
      ],
    );
  }
}
