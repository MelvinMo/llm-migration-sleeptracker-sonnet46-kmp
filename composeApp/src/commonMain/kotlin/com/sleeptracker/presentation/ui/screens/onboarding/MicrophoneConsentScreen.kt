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
import sleeptracker.composeapp.generated.resources.microphone_bg

// MIGRATION: TypeScript `(onboarding)/index.tsx` → `MicrophoneConsentScreen.kt`.
// RN title: "Purpose:", exact body text, link "Read more about sound data and snoring detection",
// toggle label "Yes, you have permission to access my microphone to record my sleep sounds."
// flex: topHalf=3, bottomHalf=4. backgroundColor: 'black'.

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MicrophoneConsentScreen(
    consentPreferences: UserConsentPreferences,
    onUpdateConsent: (UserConsentPreferences) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
    onNavigateToPrivacyPolicy: (sectionId: String) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // Top 37%: background image with onboarding header overlay (flex: 3 of 3+4=7)
        Box(modifier = Modifier.fillMaxWidth().weight(3f)) {
            Image(
                painter            = painterResource(Res.drawable.microphone_bg),
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
                text       = "Your microphone will listen for sounds like snoring or sleep talking only " +
                             "while you are sleeping. Analyzing these sounds will help you detect potential " +
                             "sleep disruptions and get a clearer picture of your sleep environment.",
                color      = AppColors.White,
                fontSize   = 16.sp,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text           = "Read more about sound data and snoring detection",
                color          = AppColors.HyperlinkBlue,
                fontSize       = 14.sp,
                textDecoration = TextDecoration.Underline,
                modifier       = Modifier.clickable { onNavigateToPrivacyPolicy("microphone") }
            )
            Spacer(Modifier.height(16.dp))
            PermissionsToggle(
                label           = "Yes, you have permission to access my microphone to record my sleep sounds.",
                checked         = consentPreferences.microphoneEnabled,
                onCheckedChange = { enabled ->
                    onUpdateConsent(consentPreferences.copy(microphoneEnabled = enabled))
                }
            )
            Spacer(Modifier.weight(1f))
            OnboardingContinueButton(onClick = onNavigateNext)
        }
    }
}
