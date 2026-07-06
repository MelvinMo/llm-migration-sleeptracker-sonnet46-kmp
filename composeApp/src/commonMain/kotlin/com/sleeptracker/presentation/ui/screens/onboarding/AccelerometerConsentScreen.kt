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
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import sleeptracker.composeapp.generated.resources.Res
import sleeptracker.composeapp.generated.resources.running_bg

// MIGRATION: TypeScript `(onboarding)/accelerometer-consent.tsx` → `AccelerometerConsentScreen.kt`.
// RN title: "Purpose:", exact body text, link "More about collecting activity data",
// toggle label "Yes, you have my permission to access my accelerometer to track my activity levels."
// flex: topHalf=3, bottomHalf=4. backgroundColor: 'black'.

@OptIn(ExperimentalResourceApi::class)
@Composable
fun AccelerometerConsentScreen(
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
                painter            = painterResource(Res.drawable.running_bg),
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
                text       = "The accelerometer on your device will be used to track your body movements " +
                             "during sleep and throughout the day continuously in the background. " +
                             "This will help us to correlate activity levels with sleep quality.",
                color      = AppColors.White,
                fontSize   = 16.sp,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text           = "More about collecting activity data",
                color          = AppColors.HyperlinkBlue,
                fontSize       = 14.sp,
                textDecoration = TextDecoration.Underline,
                modifier       = Modifier.clickable { onNavigateToPrivacyPolicy("accelerometer") }
            )
            Spacer(Modifier.height(16.dp))
            PermissionsToggle(
                label           = "Yes, you have my permission to access my accelerometer to track my activity levels.",
                checked         = consentPreferences.accelerometerEnabled,
                onCheckedChange = { enabled ->
                    onUpdateConsent(consentPreferences.copy(accelerometerEnabled = enabled))
                }
            )
            Spacer(Modifier.weight(1f))
            OnboardingContinueButton(onClick = onNavigateNext)
        }
    }
}
