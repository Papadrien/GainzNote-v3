import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../data/repositories/animal_repository.dart';
import 'storage_service.dart';

final animalRepoProvider = Provider((ref) => AnimalRepository());

/// Service de gestion du déblocage des animaux.
/// Crocodile et Chat sont débloqués par défaut.
/// Les autres nécessitent le visionnage d'une pub OU l'achat premium.
class GamificationService {
  final StorageService _storage;
  GamificationService(this._storage);

  // --- Premium (achat in-app) ---

  /// Vérifie si l'utilisateur a acheté le pack premium.
  bool isPremiumUnlocked() => _storage.getPremiumUnlocked();

  /// Débloque tous les animaux via l'achat premium.
  Future<void> unlockAllAnimals() async {
    await _storage.savePremiumUnlocked(true);
  }

  // --- Déblocage individuel ---

  /// Vérifie si un animal est débloqué (par pub, par défaut, ou par premium).
  bool isUnlocked(String animalId) {
    if (isPremiumUnlocked()) return true; // Premium → tout débloqué
    return _storage.isAnimalUnlocked(animalId);
  }

  /// Retourne les IDs des animaux verrouillés.
  List<String> getLockedAnimalIds() {
    if (isPremiumUnlocked()) return []; // Premium → rien de verrouillé
    final unlocked = _storage.getUnlockedAnimalIds();
    return AnimalRepository.animals
        .map((a) => a.id)
        .where((id) => !unlocked.contains(id))
        .toList();
  }

  /// Retourne true s'il reste des animaux verrouillés.
  bool hasLockedAnimals() => getLockedAnimalIds().isNotEmpty;

  /// Débloque un animal (après visionnage de pub).
  Future<void> unlockAnimal(String animalId) async {
    await _storage.unlockAnimal(animalId);
  }
}

final gamificationServiceProvider = Provider<GamificationService>((ref) {
  final storage = ref.watch(storageServiceProvider);
  return GamificationService(storage);
});
