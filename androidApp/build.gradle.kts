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

android {
    namespace = "com.junade.gainznote.android"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    defaultConfig {
        applicationId = "com.junade.gainznote.android"
        minSdk = libs.versions.androidMinSdk.get().toInt()
        targetSdk = libs.versions.androidTargetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
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
