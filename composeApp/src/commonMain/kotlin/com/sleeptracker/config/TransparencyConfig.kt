package com.sleeptracker.config

// MIGRATION: TypeScript `transparencyConfig.ts` → Kotlin object singleton.
// `const` at module level → Kotlin `object` with `val` properties.
// These are compile-time toggles; no runtime DI needed.

// MIGRATION: TypeScript `interface TransparencyUIConfig` → data class
data class TransparencyUIConfig(
    val journalTooltipEnabled: Boolean,
    val sleepPageTooltipEnabled: Boolean,
    val sleepModeTooltipEnabled: Boolean
)

// MIGRATION: `TRANSPARENCY_UI_CONFIG` module-level const → object val
object TransparencyConfig {
    val UI: TransparencyUIConfig = TransparencyUIConfig(
        journalTooltipEnabled = true,
        sleepPageTooltipEnabled = true,
        sleepModeTooltipEnabled = true
    )

    // MIGRATION: `IN_DEMO_MODE = true` → val IN_DEMO_MODE preserved for Demo Mode parity
    const val IN_DEMO_MODE: Boolean = true

    // MIGRATION: `transparencyDemoConfig` object → data class instance.
    // Controls which sensors are "active" and which encryption flags are reported in demo mode.
    // When IN_DEMO_MODE=true these override the real UserConsentPreferences for transparency UI.
    data class DemoConfig(
        val collectAudio: Boolean,
        val collectLight: Boolean,
        val collectAccelerometer: Boolean,
        val encryptedAtRest: Boolean,
        val encryptedInTransit: Boolean
    )

    val DEMO: DemoConfig = DemoConfig(
        collectAudio = true,
        collectLight = true,
        collectAccelerometer = true,
        encryptedAtRest = false,
        encryptedInTransit = false
    )
}
