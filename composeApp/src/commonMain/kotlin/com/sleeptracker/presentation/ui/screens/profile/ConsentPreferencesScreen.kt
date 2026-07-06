package com.sleeptracker.presentation.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeptracker.constants.AppColors
import com.sleeptracker.model.UserConsentPreferences
import com.sleeptracker.presentation.ui.screens.onboarding.OnboardingHeader
import com.sleeptracker.presentation.ui.screens.onboarding.PermissionsToggle
import com.sleeptracker.presentation.viewmodel.TransparencyViewModel
import com.sleeptracker.presentation.viewmodel.UserProfileUiState
import com.sleeptracker.presentation.viewmodel.UserProfileViewModel

// MIGRATION: TypeScript `(tabs)/profile/consent-preferences.tsx` → `ConsentPreferencesScreen.kt`.
// Header title "Your Privacy Matters to Us" (matches RN). backgroundColor: 'black'.
// Toggle labels and link texts are the full RN strings — pixel-perfect parity.

@Composable
fun ConsentPreferencesScreen(
    profileViewModel: UserProfileViewModel,
    transparencyViewModel: TransparencyViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPrivacyPolicy: (sectionId: String?) -> Unit
) {
    val profileState by profileViewModel.uiState.collectAsState()
    val prefs = (profileState as? UserProfileUiState.Loaded)?.userConsentPreferences
        ?: UserConsentPreferences.DEFAULT

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        OnboardingHeader(title = "Your Privacy Matters to Us", onBackPress = onNavigateBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 0.dp, vertical = 8.dp)
        ) {
            // Microphone
            PermissionsToggle(
                label           = "Yes, you have permission to access my microphone to record my sleep sounds.",
                checked         = prefs.microphoneEnabled,
                onCheckedChange = {
                    val newPrefs = prefs.copy(microphoneEnabled = it)
                    profileViewModel.setUserConsentPreferences(newPrefs)
                    transparencyViewModel.recalculateRisksForConsent(newPrefs)
                }
            )
            Text(
                text           = "Read more about sound data and snoring detection",
                color          = AppColors.HyperlinkBlue,
                fontSize       = 14.sp,
                textDecoration = TextDecoration.Underline,
                modifier       = Modifier
                    .padding(horizontal = 20.dp)
                    .clickable { onNavigateToPrivacyPolicy("microphone") }
            )
            Spacer(Modifier.height(32.dp))

            // Accelerometer
            PermissionsToggle(
                label           = "Yes, you have my permission to access my accelerometer to track my activity levels.",
                checked         = prefs.accelerometerEnabled,
                onCheckedChange = {
                    val newPrefs = prefs.copy(accelerometerEnabled = it)
                    profileViewModel.setUserConsentPreferences(newPrefs)
                    transparencyViewModel.recalculateRisksForConsent(newPrefs)
                }
            )
            Text(
                text           = "More about collecting activity data",
                color          = AppColors.HyperlinkBlue,
                fontSize       = 14.sp,
                textDecoration = TextDecoration.Underline,
                modifier       = Modifier
                    .padding(horizontal = 20.dp)
                    .clickable { onNavigateToPrivacyPolicy("accelerometer") }
            )
            Spacer(Modifier.height(32.dp))

            // Light Sensor
            PermissionsToggle(
                label           = "Yes, you have my permission to access my light sensor to track ambient light levels.",
                checked         = prefs.lightSensorEnabled,
                onCheckedChange = {
                    val newPrefs = prefs.copy(lightSensorEnabled = it)
                    profileViewModel.setUserConsentPreferences(newPrefs)
                    transparencyViewModel.recalculateRisksForConsent(newPrefs)
                }
            )
            Text(
                text           = "More about collecting ambient light data",
                color          = AppColors.HyperlinkBlue,
                fontSize       = 14.sp,
                textDecoration = TextDecoration.Underline,
                modifier       = Modifier
                    .padding(horizontal = 20.dp)
                    .clickable { onNavigateToPrivacyPolicy("lightSensor") }
            )
            Spacer(Modifier.height(32.dp))

            // Cloud Storage
            PermissionsToggle(
                label           = "Yes, you have my permission to store my personal health information on secure Google Cloud servers",
                checked         = prefs.cloudStorageEnabled,
                onCheckedChange = {
                    val newPrefs = prefs.copy(cloudStorageEnabled = it)
                    profileViewModel.setUserConsentPreferences(newPrefs)
                    transparencyViewModel.recalculateRisksForConsent(newPrefs)
                }
            )
            Text(
                text           = "More about data storage and data access",
                color          = AppColors.HyperlinkBlue,
                fontSize       = 14.sp,
                textDecoration = TextDecoration.Underline,
                modifier       = Modifier
                    .padding(horizontal = 20.dp)
                    .clickable { onNavigateToPrivacyPolicy("cloudVsLocalStorage") }
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}
