import 'dart:math';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../data/repositories/animal_repository.dart';
import 'storage_service.dart';

/// Service de gamification : gère le déblocage progressif des animaux.
///
/// Règles :
/// - Chien et Chat sont débloqués par défaut.
/// - Les autres animaux se débloquent aléatoirement à l'arrêt du minuteur.
/// - La probabilité diminue au fur et à mesure des déblocages :
///   - 0 animal bonus débloqué  → 1/3
///   - 1 animal bonus débloqué  → 1/6
///   - 2 animaux bonus débloqués → 1/10
///   - 3+ animaux bonus débloqués → 1/20 (plancher)
class GamificationService {
  final StorageService _storage;
  final AnimalRepository _animalRepo;
  final Random _random;

  static const List<String> _defaultUnlocked = ['dog', 'cat'];

  GamificationService(this._storage, this._animalRepo, [Random? random])
      : _random = random ?? Random();

  /// Retourne la liste des IDs d'animaux débloqués.
  List<String> getUnlockedAnimalIds() {
    final stored = _storage.getUnlockedAnimalIds();
    if (stored.isEmpty) {
      // Première utilisation : initialiser avec les animaux par défaut
      _storage.saveUnlockedAnimalIds(_defaultUnlocked);
      return List.from(_defaultUnlocked);
    }
    return stored;
  }

  /// Vérifie si un animal est débloqué.
  bool isUnlocked(String animalId) {
    return getUnlockedAnimalIds().contains(animalId);
  }

  /// Retourne la liste des IDs d'animaux encore verrouillés.
  List<String> getLockedAnimalIds() {
    final unlocked = getUnlockedAnimalIds();
    return _animalRepo
        .getAll()
        .map((a) => a.id)
        .where((id) => !unlocked.contains(id))
        .toList();
  }

  /// Nombre d'animaux bonus débloqués (hors chien et chat).
  int get _bonusUnlockedCount {
    final unlocked = getUnlockedAnimalIds();
    return unlocked.where((id) => !_defaultUnlocked.contains(id)).length;
  }

  /// Probabilité de débloquer un animal (dépend du nombre déjà débloqués).
  /// Retourne la probabilité sous forme 1/N.
  int get unlockChanceDenominator {
    final bonus = _bonusUnlockedCount;
    if (bonus <= 0) return 3;   // 1/3
    if (bonus == 1) return 6;   // 1/6
    if (bonus == 2) return 10;  // 1/10
    return 20;                  // 1/20 (plancher)
  }

  /// Tente de débloquer un animal aléatoire.
  /// Retourne l'ID de l'animal débloqué, ou null si pas de chance ou tous débloqués.
  Future<String?> tryUnlockAnimal() async {
    final locked = getLockedAnimalIds();
    if (locked.isEmpty) return null; // Tous débloqués

    // Tirage au sort
    final denom = unlockChanceDenominator;
    final roll = _random.nextInt(denom);
    if (roll != 0) return null; // Pas de chance

    // Choisir un animal aléatoire parmi les verrouillés
    final chosen = locked[_random.nextInt(locked.length)];

    // Débloquer
    final unlocked = getUnlockedAnimalIds();
    unlocked.add(chosen);
    await _storage.saveUnlockedAnimalIds(unlocked);

    return chosen;
  }

  /// Vérifie si tous les animaux sont débloqués.
  bool get allUnlocked => getLockedAnimalIds().isEmpty;
}

final gamificationServiceProvider = Provider<GamificationService>((ref) {
  return GamificationService(
    ref.read(storageServiceProvider),
    ref.read(animalRepoProvider),
  );
});

final animalRepoProvider = Provider((ref) => AnimalRepository());
