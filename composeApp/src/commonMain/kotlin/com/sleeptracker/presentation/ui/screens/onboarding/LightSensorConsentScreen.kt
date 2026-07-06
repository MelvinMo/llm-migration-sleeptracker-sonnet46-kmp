package com.sleeptracker.presentation.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeptracker.constants.AppColors
import com.sleeptracker.model.UserConsentPreferences
import com.sleeptracker.utils.isIos
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import sleeptracker.composeapp.generated.resources.Res
import sleeptracker.composeapp.generated.resources.bedroom_light_bg

// MIGRATION: TypeScript `(onboarding)/light-sensor-consent.tsx` → `LightSensorConsentScreen.kt`.
// RN title: "Purpose:", exact body text, link "More about collecting ambient light data",
// toggle label "Yes, you have my permission to access my light sensor to track ambient light levels."
// flex: topHalf=3, bottomHalf=4. backgroundColor: 'black'.

@OptIn(ExperimentalResourceApi::class)
@Composable
fun LightSensorConsentScreen(
    consentPreferences: UserConsentPreferences,
    onUpdateConsent: (UserConsentPreferences) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
    onNavigateToPrivacyPolicy: (sectionId: String) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // Top 37%: background image with header (flex: 3 of 3+4=7)
        Box(modifier = Modifier.fillMaxWidth().weight(3f)) {
            Image(
                painter            = painterResource(Res.drawable.bedroom_light_bg),
                contentDescription = null,
                modifier           = Modifier.fillMaxSize(),
                contentScale       = ContentScale.Crop
            )
            OnboardingHeader(
                title       = "Your Privacy Matters to Us",
                onBackPress = onNavigateBack
            )
        }

        // Bottom 57%: consent content (flex: 4 of 7)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(4f)
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Text(
                text       = "Purpose:",
                color      = AppColors.White,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text       = "The ambient light sensor on your device will be used to monitor the light " +
                             "conditions in your sleep environment only while you are sleeping, helping us " +
                             "to understand how light exposure affects your sleep quality.",
                color      = AppColors.White,
                fontSize   = 16.sp,
                lineHeight = 24.sp
            )
            if (isIos) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text       = "Note: The ambient light sensor is not available on this device. " +
                                 "This feature is only active on Android.",
                    color      = AppColors.LightGrey,
                    fontSize   = 14.sp,
                    lineHeight = 20.sp
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text           = "More about collecting ambient light data",
                color          = AppColors.HyperlinkBlue,
                fontSize       = 14.sp,
                textDecoration = TextDecoration.Underline,
                modifier       = Modifier.clickable { onNavigateToPrivacyPolicy("lightSensor") }
            )
            Spacer(Modifier.height(16.dp))
            PermissionsToggle(
                label           = "Yes, you have my permission to access my light sensor to track ambient light levels.",
                checked         = consentPreferences.lightSensorEnabled,
                onCheckedChange = { enabled ->
                    onUpdateConsent(consentPreferences.copy(lightSensorEnabled = enabled))
                }
            )
            Spacer(Modifier.weight(1f))
            OnboardingContinueButton(onClick = onNavigateNext)
        }
    }
}
