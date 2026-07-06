package com.sleeptracker.model

import kotlinx.serialization.Serializable

// MIGRATION: TypeScript `type UserConsentPreferences` → @Serializable data class.
// All defaults match DEFAULT_USER_CONSENT_PREFERENCES (all false = explicit opt-in required).
@Serializable
data class UserConsentPreferences(
    val accelerometerEnabled: Boolean = false,
    val lightSensorEnabled: Boolean = false,
    val microphoneEnabled: Boolean = false,
    val cloudStorageEnabled: Boolean = false,
    val agreedToPrivacyPolicy: Boolean = false,
    val analyticsEnabled: Boolean = false,
    val marketingCommunications: Boolean = false,
    val notificationsEnabled: Boolean = false
) {
    companion object {
        // MIGRATION: `DEFAULT_USER_CONSENT_PREFERENCES` constant → companion object val
        val DEFAULT = UserConsentPreferences()
    }
}
