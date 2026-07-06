import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// MIGRATION: Replaces frontend/package.json + app.json combined.
// KMP uses a single Gradle module to produce both Android APK and iOS framework.
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    // MIGRATION: Android target replaces Expo's android/ directory and react-native-gradle-plugin
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // MIGRATION: iOS target replaces Expo's ios/ directory and CocoaPods setup
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        // ── COMMON ────────────────────────────────────────────────────────────
        commonMain.dependencies {
            // Compose Multiplatform 1.8.x
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)

            // Navigation — replaces expo-router
            implementation(libs.navigation.compose)

            // ViewModel + Lifecycle — replaces Zustand stores
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.runtime.compose)

            // SQLDelight — replaces expo-sqlite
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)

            // Ktor — replaces custom HttpClient over fetch()
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)

            // Coroutines — replaces async/await + Promises
            implementation(libs.kotlinx.coroutines.core)

            // Serialization — replaces JSON.parse / JSON.stringify
            implementation(libs.kotlinx.serialization.json)

            // DateTime — replaces JavaScript Date
            implementation(libs.kotlinx.datetime)

            // UUID — replaces Math.random() for IDs
            implementation(libs.uuid)

            // Koin DI — replaces services/index.ts singletons
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }

        // ── ANDROID ───────────────────────────────────────────────────────────
        androidMain.dependencies {
            implementation(libs.activity.compose)
            implementation(libs.core.ktx)

            // Android-specific Ktor engine
            implementation(libs.ktor.client.okhttp)

            // Android SQLDelight driver
            implementation(libs.sqldelight.android.driver)

            // WorkManager — replaces expo-background-fetch + expo-task-manager
            implementation(libs.workmanager)

            // DataStore — replaces AsyncStorage on Android
            implementation(libs.datastore.preferences)

            // Security crypto — EncryptedSharedPreferences for AndroidSecureStorage
            implementation(libs.security.crypto)

            // Koin Android
            implementation(libs.koin.android)

            // Coroutines Android
            implementation(libs.kotlinx.coroutines.android)
        }

        // ── iOS ───────────────────────────────────────────────────────────────
        iosMain.dependencies {
            // iOS-specific Ktor engine
            implementation(libs.ktor.client.darwin)

            // iOS SQLDelight driver
            implementation(libs.sqldelight.native.driver)
        }
    }
}

android {
    namespace = "com.sleeptracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mcscert.sleeptracker.kotlin" // MIGRATION: Preserved from app.json
        minSdk = 26 // Android 8.0 required for ForegroundService with notification channel
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// MIGRATION: SQLDelight 2.x configuration.
// Preserves exact table names (journals, sensor_data) from expo-sqlite schema.
sqldelight {
    databases {
        create("SleepTrackerDatabase") {
            packageName.set("com.sleeptracker.database")
            // MIGRATION: Same database filename as LocalDatabaseManager.ts
            generateAsync = true
        }
    }
}
