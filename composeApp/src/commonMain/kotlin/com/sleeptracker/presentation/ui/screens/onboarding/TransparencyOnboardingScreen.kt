package com.sleeptracker.presentation.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeptracker.constants.AppColors
import com.sleeptracker.presentation.ui.components.transparency.PrivacyIcon
import com.sleeptracker.presentation.ui.components.transparency.SensorPrivacyIcon

// MIGRATION: TypeScript `(onboarding)/transparency.tsx` → `TransparencyOnboardingScreen.kt`.
// Key translations:
//   `<ScrollView>` → `Column` with `Modifier.verticalScroll(rememberScrollState())`
//   `<PrivacyIcon>` demo icons → `PrivacyIcon` composable with `onPress = {}`
//   `<SensorPrivacyIcon>` demo icons → `SensorPrivacyIcon` composable
//   `setHasCompletedPrivacyOnboarding(true)` → called from AppNavigation when `onNavigateNext()` fires
//   `router.push('/questions-explanation')` → `onNavigateNext()` callback
//   The TypeScript also set `hasCompletedPrivacyOnboarding=true` here; in Compose the LaunchedEffect
//   in AppNavigation handles the state-based redirect after `profileViewModel.setHasCompletedPrivacyOnboarding(true)`.

@Composable
fun TransparencyOnboardingScreen(
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit   // caller sets hasCompletedPrivacyOnboarding = true before navigating
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black)
    ) {
        OnboardingHeader(
            title       = "Your Privacy Matters to Us",
            onBackPress = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Scrollable content area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text       = "Privacy Features In this App",
                    color      = AppColors.White,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text     = "This app is designed to prioritize transparency by embedding details about " +
                               "data collection within the UI. Our real-time privacy analysis system monitors " +
                               "data collection and provides instant visual feedback through dynamic privacy icons.",
                    color    = AppColors.White,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
                Spacer(Modifier.height(20.dp))

                // Key features list
                Text(
                    text       = "Key Features:",
                    color      = AppColors.White,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                FeatureBullet(
                    bold = "Tooltip System:",
                    text = " Click privacy icons next to data types for contextual information"
                )
                FeatureBullet(
                    bold = "Privacy Pages:",
                    text = " Transform entire screens to show comprehensive privacy details"
                )
                FeatureBullet(
                    bold = "Real-time Analysis:",
                    text = " AI-powered system detects and explains privacy risks as they occur"
                )
                Spacer(Modifier.height(24.dp))

                // Privacy risk indicator demos
                // MIGRATION: `<PrivacyIcon handleIconPress={() => {}} ...>` → `onPress = {}`
                Text(
                    text      = "Privacy Risk Indicators",
                    color     = AppColors.White,
                    fontSize  = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment     = Alignment.Top
                ) {
                    PrivacyIconDemoItem(
                        iconName    = "privacy_high",
                        label       = "Major Risk",
                        description = "Policy violations, unauthorized collection",
                        modifier    = Modifier.weight(1f)
                    )
                    PrivacyIconDemoItem(
                        iconName    = "privacy_medium",
                        label       = "Medium Risk",
                        description = "Suboptimal practices, vague purposes",
                        modifier    = Modifier.weight(1f)
                    )
                    PrivacyIconDemoItem(
                        iconName    = "privacy_low",
                        label       = "Low Risk",
                        description = "Compliant, secure data handling. You will see this by default",
                        modifier    = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(24.dp))

                // Sensor data icon demos
                Text(
                    text      = "Sensor Data Icons",
                    color     = AppColors.White,
                    fontSize  = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text     = "Below are examples of icons used to convey sensor data privacy risks:",
                    color    = AppColors.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(16.dp))
                // First row: 2 sensor icons
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment     = Alignment.Top
                ) {
                    SensorIconDemoItem(
                        sensorType  = "accelerometer",
                        iconName    = "privacy_high",
                        storageType = "cloud",
                        description = "Major risk due to accelerometer data being stored in cloud",
                        modifier    = Modifier.weight(1f)
                    )
                    SensorIconDemoItem(
                        sensorType  = "light",
                        iconName    = "privacy_medium",
                        storageType = "local",
                        description = "Medium risk due to light sensor data being stored locally",
                        modifier    = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(12.dp))
                // Second row: 1 sensor icon centered (same width as individual row items above)
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.Top
                ) {
                    SensorIconDemoItem(
                        sensorType  = "microphone",
                        iconName    = "privacy_low",
                        storageType = "cloud",
                        description = "Low risk from microphone data being stored in cloud",
                        modifier    = Modifier.fillMaxWidth(0.5f)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // Continue button — stays below scroll area (outside the scrollable Column)
            Spacer(Modifier.height(8.dp))
            OnboardingContinueButton(onClick = onNavigateNext)
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── Private helper composables ───────────────────────────────────────────────

@Composable
private fun FeatureBullet(bold: String, text: String) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("• ", color = AppColors.Accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(
            text     = "$bold$text",
            color    = AppColors.White,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun PrivacyIconDemoItem(
    iconName: String,
    label: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PrivacyIcon(iconName = iconName, isOpen = false, onPress = {})
        Spacer(Modifier.height(8.dp))
        Text(
            text       = label,
            color      = AppColors.White,
            fontSize   = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text     = description,
            color    = AppColors.White,
            fontSize = 12.sp,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun SensorIconDemoItem(
    sensorType: String,
    iconName: String,
    storageType: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SensorPrivacyIcon(
            sensorType  = sensorType,
            iconName    = iconName,
            storageType = storageType,
            onPress     = {}
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text     = description,
            color    = AppColors.White,
            fontSize = 12.sp,
            lineHeight = 14.sp
        )
    }
}
