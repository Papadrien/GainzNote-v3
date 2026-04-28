# 🏋️ GainzNote

Une application Kotlin Multiplatform pour le suivi de vos entraînements et progrès fitness.

## 🚀 Configuration du projet

### Prérequis
- JDK 17+
- Android SDK (API 26+)
- Xcode 14+ (pour iOS)

### Structure du projet
```
GainzNote/
├── androidApp/          # Application Android
│   ├── src/androidMain/
│   └── build.gradle.kts
├── composeApp/          # Code partagé Kotlin Multiplatform
│   ├── src/commonMain/
│   ├── src/androidMain/
│   ├── src/iosMain/
│   └── build.gradle.kts
├── gradle/             # Configuration Gradle
├── codemagic.yaml      # Configuration CI/CD
└── fix_version_extraction.sh # Script de correction
```

## 🔧 Build et développement

### Build Android
```bash
# Debug
./gradlew :androidApp:assembleDebug

# Release
./gradlew :androidApp:assembleRelease

# Bundle AAB pour Play Store
./gradlew :androidApp:bundleRelease
```

### Build iOS
```bash
# Framework KMP
./gradlew :composeApp:linkDebugFrameworkIosArm64

# App iOS (depuis le dossier iosApp)
cd iosApp
xcodebuild -project iosApp.xcodeproj -scheme iosApp build
```

## 🛠️ Correction du problème de build

### Problème résolu
L'erreur `grep: androidApp/build.gradle.kts: No such file or directory` était causée par l'absence des fichiers de configuration Gradle requis.

### Solutions appliquées
1. ✅ Création de `build.gradle.kts` principal
2. ✅ Création de `settings.gradle.kts`
3. ✅ Création de `androidApp/build.gradle.kts` avec configuration appropriée
4. ✅ Création de `composeApp/build.gradle.kts` pour le code partagé
5. ✅ Amélioration de `codemagic.yaml` avec gestion robuste des versions
6. ✅ Script `fix_version_extraction.sh` amélioré avec fallbacks

### Utilisation du script de correction
```bash
chmod +x fix_version_extraction.sh
./fix_version_extraction.sh
```

Le script :
- Trouve automatiquement les fichiers build.gradle.kts
- Extrait `versionName` et `versionCode` ou utilise des valeurs par défaut
- Configure les variables d'environnement pour CI/CD

## 📦 CI/CD avec Codemagic

Workflows configurés :
- **android-debug** : Build auto sur `develop` 
- **android-release** : Build signé sur `master`
- **ios-debug** : Build iOS manuel
- **ios-release** : Build iOS signé manuel

### Variables CI/CD requises
```bash
# Pour signing Android
CM_KEYSTORE=<keystore_base64>
CM_KEYSTORE_PASSWORD=<password>
CM_KEY_ALIAS=<alias>
CM_KEY_PASSWORD=<key_password>

# Pour iOS (optionnel)
APPLE_SIGN_IDENTITY=<identity>
APPLE_PROVISIONING_PROFILE=<profile>
```

## 🏗️ Technologies utilisées

- **Kotlin 2.1.0** - Langage principal
- **Compose Multiplatform 1.8.0** - UI moderne
- **SQLDelight 2.0.2** - Base de données
- **Coroutines 1.9.0** - Programmation asynchrone
- **Navigation Compose** - Navigation
- **Google Play Services** - Ads et Billing

## ⚡ Performance

Optimisations appliquées :
- Configuration Gradle optimisée
- R8 full mode activé
- Build cache activé
- Compilation incrémentale
- Configuration cache

## 🚀 Démarrage rapide

```bash
# Clone et setup
git clone <votre-repo>
cd GainzNote

# Rendre les scripts exécutables
chmod +x gradlew
chmod +x fix_version_extraction.sh

# Setup local.properties (remplacer par votre SDK path)
echo "sdk.dir=/path/to/android/sdk" > local.properties

# Build debug
./gradlew :androidApp:assembleDebug
```

## 🤝 Contribution

1. Fork le projet
2. Créez une branche feature
3. Committez vos changements  
4. Push vers la branche
5. Ouvrez une Pull Request

---

**✅ Projet corrigé et prêt pour le build !**
