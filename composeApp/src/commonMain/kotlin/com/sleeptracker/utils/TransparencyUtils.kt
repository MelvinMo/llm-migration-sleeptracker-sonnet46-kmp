package com.sleeptracker.utils

import androidx.compose.ui.graphics.Color
import com.sleeptracker.constants.AppColors
import com.sleeptracker.model.PrivacyRisk
import com.sleeptracker.model.TransparencyEvent

// MIGRATION: TypeScript `utils/transparency.ts` → Kotlin top-level functions.
// `Linking.openURL` → `expect fun openUrl(url: String)` via expect/actual.
// `Alert.alert` → `expect fun showAlert(title: String, message: String)`.

const val PIPEDA_BASE_URL =
    "https://www.priv.gc.ca/en/privacy-topics/privacy-laws-in-canada/the-personal-information-protection-and-electronic-documents-act-pipeda/p_principle/principles"

// MIGRATION: `handleLinkPress` was async with React Native `Linking`.
// In KMP we declare an expect fun for platform URL opening.
// The actual implementations are in androidMain / iosMain.
expect fun openUrl(url: String)

fun handleLinkPress(regulationLink: String) {
    val url = if (regulationLink.isEmpty()) {
        PIPEDA_BASE_URL
    } else {
        "${PIPEDA_BASE_URL}/p_${regulationLink}/"
    }
    openUrl(url)
}

// MIGRATION: `getPrivacyRiskColor` returns `Color` instead of a hex string.
// Compose Material3 uses Color objects; we map directly to AppColors constants.
fun getPrivacyRiskColor(risk: PrivacyRisk): Color = when (risk) {
    PrivacyRisk.HIGH   -> AppColors.TooltipRed
    PrivacyRisk.MEDIUM -> AppColors.TooltipYellow
    PrivacyRisk.LOW    -> AppColors.TooltipGreen
}

// MIGRATION: `getPrivacyRiskIcon` returns a string icon name.
// In Compose we use Material Icons or custom painter resources.
// The string names map to drawable resource names in androidMain/iosMain.
// MIGRATION_FLAG: React Native used expo-symbols / Ionicons. Compose uses Material Icons
// or painterResource(). Icon asset files must be added to androidMain/res/drawable.
fun getPrivacyRiskIcon(risk: PrivacyRisk): String = when (risk) {
    PrivacyRisk.HIGH   -> "privacy_high"    // ic_privacy_high drawable
    PrivacyRisk.MEDIUM -> "privacy_medium"
    PrivacyRisk.LOW    -> "privacy_low"
}

// MIGRATION: `getPrivacyRiskIconForPage` — returns the highest severity icon across all risks
fun getPrivacyRiskIconForPage(risks: List<PrivacyRisk>): String = when {
    risks.contains(PrivacyRisk.HIGH)   -> "privacy_high"
    risks.contains(PrivacyRisk.MEDIUM) -> "privacy_medium"
    else                               -> "privacy_low"
}

// MIGRATION: `getPrivacyRiskColorForPage` — returns the highest severity color
fun getPrivacyRiskColorForPage(risks: List<PrivacyRisk>): Color = when {
    risks.contains(PrivacyRisk.HIGH)   -> AppColors.TooltipRed
    risks.contains(PrivacyRisk.MEDIUM) -> AppColors.TooltipYellow
    else                               -> AppColors.TooltipGreen
}

// MIGRATION: `getPrivacyRiskLabel` — exact label strings preserved from source
fun getPrivacyRiskLabel(risk: PrivacyRisk): String = when (risk) {
    PrivacyRisk.HIGH   -> "Major Privacy Violation Detected:"
    PrivacyRisk.MEDIUM -> "Some Privacy Concerns Detected:"
    PrivacyRisk.LOW    -> "No Privacy Violations Detected"
}

// MIGRATION: `formatPrivacyViolations` — returns the privacy explanation text
fun formatPrivacyViolations(transparency: TransparencyEvent): String {
    val issues = transparency.regulatoryCompliance?.issues
    if (!issues.isNullOrEmpty()) {
        return transparency.aiExplanation?.privacyExplanation ?: issues
    }
    return when (transparency.privacyRisk) {
        PrivacyRisk.HIGH        -> "Sensitive biometric data is being transmitted to external cloud servers without sufficient safeguards."
        PrivacyRisk.MEDIUM      -> "Personal sensor data is being collected and may be shared or stored externally."
        PrivacyRisk.LOW, null   -> "No privacy violations detected"
    }
}
