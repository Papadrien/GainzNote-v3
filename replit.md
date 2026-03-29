# GainzNote

A Kotlin Multiplatform (KMP) + Compose Multiplatform workout tracker (bodybuilding log app) built for Android (and iOS via Xcode/Codemagic).

## Architecture

- **Language**: Kotlin (Multiplatform)
- **UI**: Compose Multiplatform
- **Database**: SQLDelight with SQLite
- **Build System**: Gradle 8.9 (via gradlew wrapper)
- **Structure**:
  - `composeApp/` — shared code (UI + business logic + database)
    - `src/commonMain/` — shared Kotlin/Compose code
    - `src/androidMain/` — Android-specific implementations
    - `src/iosMain/` — iOS-specific implementations
    - `src/commonMain/sqldelight/` — SQLDelight schema (`GainzNote.sq`)
  - `androidApp/` — Android app entry point (`MainActivity`)

## Environment Setup

### Requirements (auto-configured in Replit)
- **Java**: GraalVM CE 22.3.1 (Java 19)
- **Android SDK**: Installed at `/home/runner/android-sdk`
  - `platforms;android-35`
  - `build-tools;35.0.0`
  - `platform-tools`
- **Gradle Wrapper**: `./gradlew` (Gradle 8.9)
- **local.properties**: Points to `sdk.dir=/home/runner/android-sdk`

### Key Configuration Fixes Applied
1. Migrated `androidApp/src/main/` → `androidApp/src/androidMain/` (KMP layout v2)
2. Fixed `AndroidManifest.xml` theme from `android:style/Theme.Material.NoTitleBar` → `@style/Theme.AppCompat.Light.NoActionBar`
3. Added `androidx.appcompat:appcompat:1.7.0` dependency
4. Fixed `WorkoutRepository.kt`: `gainzNoteDatabaseQueries` → `gainzNoteQueries` (SQLDelight generates this name)
5. Added explicit `kotlinx.datetime` imports in `HomeScreen.kt`
6. Set `jvmTarget = JVM_17` in `androidApp/build.gradle.kts` to match Java compile options

## Workflow

**Start application** — runs `./gradlew :androidApp:assembleDebug`

Outputs APK at: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`

Note: iOS targets are disabled on Linux (requires macOS + Xcode). Use Codemagic CI/CD for iOS builds.

## Building for Release (Android)

```bash
./gradlew :androidApp:assembleRelease
```

## CI/CD (Codemagic)

See `codemagic.yaml` for automated build pipeline configuration.
