package com.sleeptracker.presentation.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeptracker.constants.AppColors
import com.sleeptracker.model.UserConsentPreferences

// MIGRATION: TypeScript `(onboarding)/cloud-storage.tsx` → `CloudStorageScreen.kt`.
// Layout: OnboardingHeader + "Data Storage" section + description + PermissionsToggle +
//         "Data Access:" section + text + privacy-policy link + Continue button.
// justifyContent: space-between → Column with Spacer.weight(1f) at bottom.
// backgroundColor: 'black'. Exact RN text preserved.

@Composable
fun CloudStorageScreen(
    consentPreferences: UserConsentPreferences,
    onUpdateConsent: (UserConsentPreferences) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
    onNavigateToPrivacyPolicy: (sectionId: String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        OnboardingHeader(
            title       = "Your Privacy Matters to Us",
            onBackPress = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Text(
                text       = "Data Storage",
                color      = AppColors.White,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text       = "By default all of your personal health information (data collected and " +
                             "derived data) will be stored on your mobile device. If you opt in, we will " +
                             "store your personal health information in the cloud, allowing us to provide " +
                             "more complex sleep analysis. All data will be encrypted while in storage and " +
                             "when it is being transmitted.",
                color      = AppColors.White,
                fontSize   = 16.sp,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(8.dp))
            PermissionsToggle(
                label           = "Yes, you have my permission to store my personal health information on secure Google Cloud servers",
                checked         = consentPreferences.cloudStorageEnabled,
                onCheckedChange = { enabled ->
                    onUpdateConsent(consentPreferences.copy(cloudStorageEnabled = enabled))
                }
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text       = "Data Access:",
                color      = AppColors.White,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text       = "We are committed to strict limitations on data sharing. We do not give your " +
                             "personal information to any third parties for marketing, advertising, or any " +
                             "other commercial purposes.",
                color      = AppColors.White,
                fontSize   = 16.sp,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text           = "More about data storage and data access",
                color          = AppColors.HyperlinkBlue,
                fontSize       = 14.sp,
                textDecoration = TextDecoration.Underline,
                modifier       = Modifier.clickable { onNavigateToPrivacyPolicy("cloudVsLocalStorage") }
            )
            Spacer(Modifier.weight(1f))
            OnboardingContinueButton(onClick = onNavigateNext)
        }
    }
}
