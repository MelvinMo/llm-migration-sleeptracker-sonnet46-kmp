package com.sleeptracker.storage

// MIGRATION: TypeScript `AsyncStorage` (non-sensitive key-value store) →
// Android DataStore Preferences / iOS NSUserDefaults via expect/actual.
// Stores onboarding flags, user JSON, transparency events, and consent preferences.

expect class AppDataStore {
    suspend fun setString(key: String, value: String)
    suspend fun getString(key: String): String?
    suspend fun remove(key: String)
}

// Key constants — match TypeScript AsyncStorage keys exactly
object DataStoreKeys {
    // authStore.ts
    const val USER = "user"

    // userProfileStore.ts
    const val HAS_COMPLETED_PRIVACY_ONBOARDING = "hasCompletedPrivacyOnboarding"
    const val HAS_COMPLETED_APP_ONBOARDING = "hasCompletedAppOnboarding"
    const val USER_CONSENT_PREFERENCES = "userConsentPreferences"

    // transparencyStore.ts
    const val LIGHT_SENSOR_TRANSPARENCY = "lightSensorTransparency"
    const val MICROPHONE_TRANSPARENCY = "microphoneTransparency"
    const val ACCELEROMETER_TRANSPARENCY = "accelerometerTransparency"
    const val JOURNAL_TRANSPARENCY = "journalTransparency"
    const val STATISTICS_TRANSPARENCY = "statisticsTransparency"
    const val GENERAL_SLEEP_TRANSPARENCY = "generalSleepTransparency"
}
