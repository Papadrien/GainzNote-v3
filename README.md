# GainzNote 💪
**Carnet d'entraînement de musculation — Flutter + Riverpod + SQLite**

---

## Stack
| | |
|---|---|
| Framework | Flutter 3.24 |
| État | Riverpod 2 |
| BDD locale | sqflite (SQLite) |
| Export/Import | share_plus + file_picker |
| Build cloud | Codemagic CI/CD |

---

## Tester sur mobile SANS ordi — via Codemagic

### Étape 1 : Mettre le projet sur GitHub
1. Crée un **nouveau repo** sur [github.com](https://github.com) (ex: `gainznote`)
2. Sur ton téléphone ou depuis n'importe quel navigateur, upload tous les fichiers de ce projet dans le repo (bouton **Add file → Upload files**)
3. Structure à respecter :
```
gainznote/
├── lib/
│   ├── main.dart
│   ├── models/models.dart
│   ├── providers/providers.dart
│   ├── services/database.dart
│   ├── theme/theme.dart
│   ├── widgets/common.dart
│   └── screens/
│       ├── home_screen.dart
│       ├── workout_screen.dart
│       ├── history_screen.dart
│       └── detail_screen.dart
├── android/
│   └── app/
│       ├── build.gradle
│       └── src/main/AndroidManifest.xml
├── pubspec.yaml
└── codemagic.yaml
```

### Étape 2 : Connecter Codemagic
1. Va sur [codemagic.io](https://codemagic.io)
2. **Sign in with GitHub**
3. Clique **Add application** → sélectionne ton repo `gainznote`
4. Codemagic détecte `codemagic.yaml` automatiquement
5. Édite `codemagic.yaml` : remplace `TON_EMAIL@example.com` par ton email

### Étape 3 : Lancer le build
1. Dans Codemagic, clique **Start new build**
2. Sélectionne le workflow `android-release`
3. Attends ~10 minutes
4. Tu reçois un **email avec le lien de téléchargement de l'APK**
5. Ouvre le lien sur ton Android → installe l'APK

> **Note** : Pour installer un APK hors Play Store, il faut activer
> *Paramètres → Sécurité → Sources inconnues* sur ton téléphone.

---

## Fonctionnalités

### Entraînement actif
- Titre, notes, heure de début automatique
- Ajout d'exercices
- Séries avec poids, reps, notes
- **⬇ Propagation du poids** d'une série sur les suivantes
- **+ Plusieurs séries** en une fois (dialogue avec sélecteur)
- **Superset** : lier 2 exercices (menu ⋮ → Associer en superset)
- Confirmation avant de terminer
- Sauvegarde automatique toutes les 30s

### Historique
- Liste complète triée par date
- Détail complet de chaque séance
- **↻ Utiliser comme base** : reprend exercices + charges + notes
  Les reps précédentes s'affichent en **jaune** comme rappel

### Accueil
- 3 entraînements récents
- Switch thème clair / sombre
- Export JSON (via share sheet Android)
- Import JSON (depuis le stockage)

---

## Développement local (si tu as un PC plus tard)

```bash
# Installer Flutter : https://flutter.dev/docs/get-started/install
flutter pub get
flutter run          # sur émulateur ou téléphone branché en USB
flutter build apk    # génère l'APK
```

---

## Ajouter iOS (nécessite un Mac avec Xcode)

```bash
flutter build ios --release
```
Puis signe avec ton Apple Developer account.

---

## Structure du code

```
lib/
├── main.dart              ← point d'entrée, ProviderScope
├── models/models.dart     ← Workout, Exercise, TrainingSet + factories
├── providers/providers.dart ← Riverpod : thème, workouts, workout actif
├── services/database.dart ← SQLite : CRUD complet
├── theme/theme.dart       ← couleurs dark/light, ThemeData
├── widgets/common.dart    ← widgets réutilisables + helpers date/durée
└── screens/
    ├── home_screen.dart   ← accueil + paramètres
    ├── workout_screen.dart ← saisie entraînement actif
    ├── history_screen.dart ← liste historique
    └── detail_screen.dart  ← détail + bouton "utiliser comme base"
```
