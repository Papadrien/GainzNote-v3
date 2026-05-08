plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

fun gitCommitCount(): Int {
    return try {
        val process = ProcessBuilder("git", "rev-list", "--count", "HEAD")
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
        process.inputStream.bufferedReader().readLine()?.trim()?.toInt() ?: 1
    } catch (e: Exception) {
        1
    }
}

fun gitTag(): String {
    return try {
        val process = ProcessBuilder("git", "describe", "--tags", "--abbrev=0")
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
        process.inputStream.bufferedReader().readLine()?.trim() ?: "1.0.0"
    } catch (e: Exception) {
        "1.0.0"
    }
}

android {
    namespace = "fr.junade.gainznote"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    defaultConfig {
        applicationId = "fr.junade.gainznote"
        minSdk = libs.versions.androidMinSdk.get().toInt()
        targetSdk = libs.versions.androidTargetSdk.get().toInt()
        versionCode = gitCommitCount()
        versionName = gitTag()
    }
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH")?.let { rootProject.file(it) } ?: file("keystore/gainznote-release.jks"))
            storePassword = System.getenv("STORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: "GainzNote-key"
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.splashscreen)
    implementation(libs.google.play.services.ads)
    implementation(libs.google.billing)
}
