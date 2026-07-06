package com.sleeptracker.presentation.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeptracker.constants.AppColors
import com.sleeptracker.model.GeneralSleepData

// MIGRATION: TypeScript `(onboarding)/questions.tsx` → `QuestionsScreen.kt`.
// Key translations:
//   `useState<string>()` for selectedOption → `remember { mutableStateOf<String?>(null) }`
//   `sleepOptions` array → `val sleepOptions = listOf(...)`
//   `<OnboardingQuestionOption>` → `OnboardingQuestionOption` composable
//   `generalSleepDataRepository.createSleepData(sleepData)` → `onSaveGeneralSleepData(data)` callback
//   `setHasCompletedAppOnboarding(true)` + `router.replace('/(tabs)/sleep/')` →
//     `onComplete()` callback; caller sets the flag; LaunchedEffect in AppNavigation redirects.
//
// MIGRATION: Continuing without selecting an option is allowed (matches TypeScript behavior).
// `onSaveGeneralSleepData` is a non-suspend callback; the underlying coroutine is managed
// by SleepViewModel.viewModelScope (fire-and-forget, same as TypeScript's unawaited async call).

@Composable
fun QuestionsScreen(
    userId: String?,
    onNavigateBack: () -> Unit,
    onSaveGeneralSleepData: (GeneralSleepData) -> Unit,
    onComplete: () -> Unit
) {
    val sleepOptions = listOf("6 hours or less", "6 - 8 hours", "8 - 10 hours")
    var selectedOption by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // MIGRATION: TypeScript used `<OnboardingHeader title="">` with empty title string
        OnboardingHeader(title = "", onBackPress = onNavigateBack)

        // Main content — vertically centered (TypeScript `justifyContent: 'center'`)
        Column(
            modifier            = Modifier.fillMaxWidth().weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text       = "How much sleep do you usually get at night?",
                color      = AppColors.White,
                fontSize   = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(32.dp))

            sleepOptions.forEach { option ->
                OnboardingQuestionOption(
                    label      = option,
                    isSelected = selectedOption == option,
                    onPress    = { selectedOption = option }
                )
            }
        }

        // Continue button at bottom — navigation proceeds even without a selection
        OnboardingContinueButton(
            onClick = {
                // Fire-and-forget save (selection is optional per TypeScript source)
                val selected = selectedOption
                if (selected != null && !userId.isNullOrEmpty()) {
                    onSaveGeneralSleepData(
                        GeneralSleepData(
                            userId               = userId,
                            currentSleepDuration = selected,
                            snoring              = "",
                            tirednessFrequency   = "",
                            daytimeSleepiness    = ""
                        )
                    )
                }
                // MIGRATION: `setHasCompletedAppOnboarding(true)` + `router.replace('/(tabs)/sleep/')`
                // → onComplete() sets the flag, LaunchedEffect in AppNavigation handles the redirect
                onComplete()
            }
        )
        Spacer(Modifier.height(16.dp))
    }
}
