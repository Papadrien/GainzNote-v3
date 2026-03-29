# GainzNote 💪
Carnet de musculation — Kotlin Multiplatform + Compose Multiplatform + SQLDelight

## Structure
```
GainzNote/
├── composeApp/          ← code partagé (UI + logique + BDD)
├── androidApp/          ← point d'entrée Android
├── iosApp/              ← point d'entrée iOS (nécessite Mac/Xcode)
├── codemagic.yaml       ← pipeline CI/CD
└── gradle/
```

## Build via Codemagic (sans ordi)
1. Push ce repo sur GitHub
2. Connecte-toi sur codemagic.io avec GitHub
3. Ajoute ce repo → workflow `android-release` détecté automatiquement
4. Édite `codemagic.yaml` : remplace `TON_EMAIL@example.com`
5. Lance le build → reçois l'APK par email en ~10min

## Build local
```bash
./gradlew :androidApp:assembleDebug
```
