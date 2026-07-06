package com.sleeptracker.presentation.ui.components.transparency

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeptracker.constants.AppColors
import com.sleeptracker.model.DataDestination
import com.sleeptracker.model.PrivacyRisk
import com.sleeptracker.model.TransparencyEvent
import com.sleeptracker.utils.formatPrivacyViolations
import com.sleeptracker.utils.getPrivacyRiskIcon
import com.sleeptracker.utils.getPrivacyRiskLabel
import com.sleeptracker.utils.handleLinkPress

// Generic privacy page matching RN PrivacyStatisticsPage / PrivacySleepPage layout.
@Composable
fun PrivacyDataPage(
    transparency: TransparencyEvent,
    onNavigateToPrivacyPolicy: (sectionId: String?) -> Unit,
    sectionTitle: String? = null,
    onNavigateToConsentPreferences: (() -> Unit)? = null,
    sensorIconSlot: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val risk = transparency.privacyRisk ?: PrivacyRisk.LOW

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // Optional section title with sensor icon (used in journal accelerometer block)
        if (sectionTitle != null) {
            Row(
                modifier          = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text       = sectionTitle,
                    color      = AppColors.White,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.weight(1f)
                )
                sensorIconSlot?.invoke()
            }
        }

        // Risk label header
        Text(
            text       = getPrivacyRiskLabel(risk),
            color      = AppColors.White,
            fontSize   = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        // Violation description (non-LOW only)
        if (risk != PrivacyRisk.LOW) {
            Spacer(Modifier.height(4.dp))
            Text(
                text       = formatPrivacyViolations(transparency),
                color      = AppColors.White,
                fontSize   = 14.sp,
                lineHeight = 20.sp,
                modifier   = Modifier.padding(bottom = 12.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        // Purpose card
        PrivacyInfoCard(label = "Purpose", value = transparency.aiExplanation?.why ?: "")

        // Storage + Access cards (LOW risk only)
        if (risk == PrivacyRisk.LOW) {
            Spacer(Modifier.height(20.dp))
            PrivacyInfoCard(label = "Storage", value = transparency.aiExplanation?.storage ?: "")
            Spacer(Modifier.height(20.dp))
            PrivacyInfoCard(label = "Access",  value = transparency.aiExplanation?.access ?: "")
        }

        Spacer(Modifier.height(8.dp))

        PrivacyLink("Privacy Policy Section") {
            onNavigateToPrivacyPolicy(transparency.aiExplanation?.privacyPolicyLink?.firstOrNull())
        }
        PrivacyLink("PIPEDA Regulation") {
            handleLinkPress(transparency.aiExplanation?.regulationLink?.firstOrNull() ?: "")
        }
        if (onNavigateToConsentPreferences != null) {
            PrivacyLink("Opt Out") { onNavigateToConsentPreferences() }
        }

        Spacer(Modifier.height(20.dp))

        PrivacyLink("View Full Privacy Policy", fontSize = 16) {
            onNavigateToPrivacyPolicy(null)
        }
    }
}

// Journal privacy page — two sections: Journal + Activity Tracker (accelerometer).
// Mirrors PrivacyJournalPage.tsx which renders the more-severe section first.
@Composable
fun PrivacyJournalPage(
    journalTransparency: TransparencyEvent,
    accelerometerTransparency: TransparencyEvent,
    onNavigateToPrivacyPolicy: (sectionId: String?) -> Unit,
    onNavigateToConsentPreferences: () -> Unit,
    modifier: Modifier = Modifier
) {
    val journalRisk = journalTransparency.privacyRisk ?: PrivacyRisk.LOW
    val accelRisk   = accelerometerTransparency.privacyRisk ?: PrivacyRisk.LOW
    val accelMoreSevere = accelRisk > journalRisk

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        val journalSection: @Composable () -> Unit = {
            PrivacyDataPage(
                transparency              = journalTransparency,
                onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
                sectionTitle              = "Journal"
            )
        }
        val accelSection: @Composable () -> Unit = {
            PrivacyDataPage(
                transparency              = accelerometerTransparency,
                onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
                sectionTitle              = "Activity Tracker",
                onNavigateToConsentPreferences = onNavigateToConsentPreferences,
                sensorIconSlot = {
                    SensorPrivacyIcon(
                        sensorType  = "accelerometer",
                        iconName    = getPrivacyRiskIcon(accelRisk),
                        storageType = if (accelerometerTransparency.storageLocation == DataDestination.GOOGLE_CLOUD) "cloud" else "local",
                        onPress     = {}
                    )
                }
            )
        }

        if (accelMoreSevere) {
            accelSection()
            journalSection()
        } else {
            journalSection()
            accelSection()
        }
    }
}

// ── Private helpers ───────────────────────────────────────────────────────────

@Composable
private fun PrivacyInfoCard(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1F1F2E), RoundedCornerShape(12.dp))
            .padding(15.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("$label: ") }
                append(value)
            },
            color      = AppColors.White,
            fontSize   = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun PrivacyLink(label: String, fontSize: Int = 14, onClick: () -> Unit) {
    Text(
        text       = label,
        color      = AppColors.HyperlinkBlue,
        fontSize   = fontSize.sp,
        textDecoration = TextDecoration.Underline,
        modifier   = Modifier
            .padding(top = 8.dp)
            .clickable(onClick = onClick)
    )
}
