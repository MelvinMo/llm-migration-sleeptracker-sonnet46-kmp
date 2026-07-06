package com.sleeptracker.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// MIGRATION: TypeScript interface TransparencyEvent → Kotlin @Serializable data class.
// @Serializable replaces JSON.parse/JSON.stringify usage throughout the Zustand store.
// All nullable fields mapped with `?` to preserve TypeScript's optional `?` semantics.
@Serializable
data class TransparencyEvent(
    val timestamp: String? = null,          // ISO string; KMP uses String not Date (kotlinx-datetime handles parsing)
    val dataType: DataType,
    val source: DataSource,

    // Sensor-specific fields
    val sensorType: String? = null,
    val samplingRate: Double? = null,
    val duration: Double? = null,

    // Storage-specific
    val encryptionMethod: EncryptionMethod? = null,
    val storageLocation: DataDestination? = null,

    // Transmission-specific
    val endpoint: String? = null,
    // MIGRATION: TypeScript union 'HTTP' | 'HTTPS' | 'WSS' → enum class Protocol
    val protocol: Protocol? = null,

    val backgroundMode: Boolean? = null,

    // AI-generated fields
    val privacyRisk: PrivacyRisk? = null,
    val regulatoryCompliance: RegulatoryCompliance? = null,
    val aiExplanation: AIExplanation? = null
)

// MIGRATION: TypeScript `interface AIPrompt` → Kotlin @Serializable data class.
// Used to POST to /transparency/ai/ — Ktor serializes this automatically.
@Serializable
data class AIPrompt(
    val transparencyEvent: TransparencyEvent,
    val privacyPolicy: String,
    val userConsentPreferences: UserConsentPreferences,
    val regulationFrameworks: List<RegulatoryFramework>,
    val pipedaRegulations: String? = null,
    val userRiskTolerance: String? = null  // MIGRATION: `any` → String? (serialized JSON if needed)
)

// MIGRATION: TypeScript `enum DataType` → Kotlin `enum class DataType`.
// @SerialName preserves the exact string values used in backend/JSON for wire compatibility.
@Serializable
enum class DataType {
    @SerialName("SENSOR_AUDIO") SENSOR_AUDIO,
    @SerialName("SENSOR_MOTION") SENSOR_MOTION,
    @SerialName("SENSOR_LIGHT") SENSOR_LIGHT,
    @SerialName("USER_JOURNAL") USER_JOURNAL,
    @SerialName("USER_PROFILE") USER_PROFILE,
    @SerialName("GENERAL_SLEEP") GENERAL_SLEEP,
    @SerialName("SLEEP_STATISTICS") SLEEP_STATISTICS,
    @SerialName("DEVICE_INFO") DEVICE_INFO,
    @SerialName("LOCATION") LOCATION,
    @SerialName("USAGE_ANALYTICS") USAGE_ANALYTICS
}

@Serializable
enum class DataSource {
    @SerialName("MICROPHONE") MICROPHONE,
    @SerialName("ACCELEROMETER") ACCELEROMETER,
    @SerialName("LIGHT_SENSOR") LIGHT_SENSOR,
    @SerialName("USER_INPUT") USER_INPUT,
    @SerialName("DERIVED_DATA") DERIVED_DATA,
    @SerialName("SYSTEM_INFO") SYSTEM_INFO
}

@Serializable
enum class DataDestination {
    // MIGRATION: ASYNC_STORAGE → DATASTORE_PREFERENCES (Android DataStore replaces AsyncStorage)
    @SerialName("ASYNC_STORAGE") ASYNC_STORAGE,         // kept for JSON wire compatibility
    @SerialName("SECURE_STORE") SECURE_STORE,
    @SerialName("SQLITE_DB") SQLITE_DB,
    @SerialName("MEMORY") MEMORY,
    @SerialName("GOOGLE_CLOUD") GOOGLE_CLOUD,
    @SerialName("THIRD_PARTY") THIRD_PARTY
}

@Serializable
enum class EncryptionMethod {
    @SerialName("NONE") NONE,
    @SerialName("AES_256") AES_256,
    @SerialName("JWT") JWT,
    @SerialName("DEVICE_KEYCHAIN") DEVICE_KEYCHAIN
}

@Serializable
enum class PrivacyRisk {
    @SerialName("LOW") LOW,
    @SerialName("MEDIUM") MEDIUM,
    @SerialName("HIGH") HIGH
}

@Serializable
enum class RegulatoryFramework {
    @SerialName("PIPEDA") PIPEDA,
    @SerialName("PHIPA") PHIPA,
    @SerialName("GDPR") GDPR
}

// MIGRATION: TypeScript union 'HTTP' | 'HTTPS' | 'WSS' → sealed enum for type safety
@Serializable
enum class Protocol {
    @SerialName("HTTP") HTTP,
    @SerialName("HTTPS") HTTPS,
    @SerialName("WSS") WSS
}

@Serializable
data class RegulatoryCompliance(
    val framework: RegulatoryFramework,
    val compliant: Boolean,
    val issues: String? = null,
    val relevantSections: List<String>? = null
)

@Serializable
data class AIExplanation(
    val why: String,
    val storage: String,
    val access: String,
    val privacyExplanation: String,
    val privacyPolicyLink: List<String>,
    val regulationLink: List<String>
)

// MIGRATION: TypeScript `interface TransparencySettings` → data class
@Serializable
data class TransparencySettings(
    val realTimeNotifications: Boolean = false,
    val privacyDashboard: Boolean = false,
    val uiElementsVisible: Boolean = true,
    val aiExplanations: Boolean = true,
    val riskAssessment: Boolean = true,
    val regulatoryChecks: Boolean = true
)

// ─── Default events ───────────────────────────────────────────────────────────
// MIGRATION: TypeScript `const DEFAULT_*_TRANSPARENCY_EVENT` → companion object
// values on a DefaultTransparencyEvents object (Kotlin has no module-level `const`
// for complex objects; object singletons are idiomatic).

object DefaultTransparencyEvents {
    val JOURNAL = TransparencyEvent(
        dataType = DataType.USER_JOURNAL,
        source = DataSource.USER_INPUT,
        privacyRisk = PrivacyRisk.LOW,
        regulatoryCompliance = RegulatoryCompliance(
            framework = RegulatoryFramework.PIPEDA,
            compliant = true,
            issues = "",
            relevantSections = emptyList()
        ),
        aiExplanation = AIExplanation(
            why = "To analyze how your daily mood, habits, sleep goals affects your sleep quality.",
            privacyExplanation = "",
            storage = "",
            access = "",
            privacyPolicyLink = emptyList(),
            regulationLink = emptyList()
        )
    )

    val LIGHT_SENSOR = TransparencyEvent(
        dataType = DataType.SENSOR_LIGHT,
        source = DataSource.LIGHT_SENSOR,
        privacyRisk = PrivacyRisk.LOW,
        regulatoryCompliance = RegulatoryCompliance(
            framework = RegulatoryFramework.PIPEDA,
            compliant = true,
            issues = "",
            relevantSections = emptyList()
        ),
        aiExplanation = AIExplanation(
            why = "To understand how the light conditions in your sleep environment may affect your sleep quality",
            privacyExplanation = "",
            storage = "",
            access = "",
            privacyPolicyLink = emptyList(),
            regulationLink = emptyList()
        )
    )

    val MICROPHONE = TransparencyEvent(
        dataType = DataType.SENSOR_AUDIO,
        source = DataSource.MICROPHONE,
        privacyRisk = PrivacyRisk.LOW,
        regulatoryCompliance = RegulatoryCompliance(
            framework = RegulatoryFramework.PIPEDA,
            compliant = true,
            issues = "",
            relevantSections = emptyList()
        ),
        aiExplanation = AIExplanation(
            why = "To analyze sleep disturbances such as snoring and talking, as well as understanding the noise level in your sleep environment",
            privacyExplanation = "",
            storage = "",
            access = "",
            privacyPolicyLink = emptyList(),
            regulationLink = emptyList()
        )
    )

    val ACCELEROMETER = TransparencyEvent(
        dataType = DataType.SENSOR_MOTION,
        source = DataSource.ACCELEROMETER,
        privacyRisk = PrivacyRisk.LOW,
        regulatoryCompliance = RegulatoryCompliance(
            framework = RegulatoryFramework.PIPEDA,
            compliant = true,
            issues = "",
            relevantSections = emptyList()
        ),
        aiExplanation = AIExplanation(
            why = "To analyze how your movements during sleep and throughout the day impact sleep quality",
            privacyExplanation = "",
            storage = "",
            access = "",
            privacyPolicyLink = emptyList(),
            regulationLink = emptyList()
        )
    )

    val STATISTICS = TransparencyEvent(
        dataType = DataType.SLEEP_STATISTICS,
        source = DataSource.DERIVED_DATA,
        privacyRisk = PrivacyRisk.LOW,
        regulatoryCompliance = RegulatoryCompliance(
            framework = RegulatoryFramework.PIPEDA,
            compliant = true,
            issues = "",
            relevantSections = emptyList()
        ),
        aiExplanation = AIExplanation(
            why = "Provide summaries and actionable insights to help improve your sleep quality",
            privacyExplanation = "No privacy risks",
            storage = "This data is stored securely in your preferred storage location with encryption.",
            access = "No third parties have access to this data. Only you can view it through the app.",
            privacyPolicyLink = listOf("derivedData"),
            regulationLink = listOf("access")
        )
    )

    val GENERAL_SLEEP = TransparencyEvent(
        dataType = DataType.GENERAL_SLEEP,
        source = DataSource.USER_INPUT,
        privacyRisk = PrivacyRisk.LOW,
        regulatoryCompliance = RegulatoryCompliance(
            framework = RegulatoryFramework.PIPEDA,
            compliant = true,
            issues = "",
            relevantSections = emptyList()
        ),
        aiExplanation = AIExplanation(
            why = "To understand your current sleep quality and how we can improve it",
            privacyExplanation = "",
            storage = "",
            access = "",
            privacyPolicyLink = emptyList(),
            regulationLink = emptyList()
        )
    )
}
