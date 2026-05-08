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
    namespace = "fr.junade.gainznote"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    defaultConfig {
        applicationId = "fr.junade.gainznote"
        minSdk = libs.versions.androidMinSdk.get().toInt()
        targetSdk = libs.versions.androidTargetSdk.get().toInt()
        val timestamp = providers.exec {
            commandLine("date", "+%m%d%H%M")
        }.standardOutput.asText.get().trim().toInt()
        versionCode = timestamp
        versionName = "1.0.$timestamp"
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
    applicationVariants.all {
        val variant = this
        val versionCode = variant.versionCode
        val buildType = variant.buildType.name
        variant.outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output.outputFileName = "GainzNote-1.0.$versionCode-$buildType.apk"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

afterEvaluate {
    listOf("debug", "release").forEach { buildType ->
        val capturedVersionCode = android.defaultConfig.versionCode
        tasks.named("bundle${buildType.replaceFirstChar { it.uppercase() }}") {
            doLast {
                val aabDir = layout.buildDirectory.dir("outputs/bundle/$buildType").get().asFile
                aabDir.listFiles { f -> f.extension == "aab" }?.forEach { aab ->
                    aab.renameTo(File(aab.parent, "GainzNote-1.0.$capturedVersionCode-$buildType.aab"))
                }
            }
        }
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
