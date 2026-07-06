package com.sleeptracker.presentation.ui.screens.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeptracker.constants.AppColors

// MIGRATION: TypeScript `(onboarding)/questions-explanation.tsx` → `QuestionsExplanationScreen.kt`.
// backgroundColor: 'black'. Exact RN text preserved.

@Composable
fun QuestionsExplanationScreen(
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit
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
                text       = "Help us understand your current sleep quality",
                color      = AppColors.White,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text       = "The next few screens will ask you questions about your current sleep quality " +
                             "and sleep habits. This will help us understand your sleep better and provide " +
                             "personalized insights.",
                color      = AppColors.White,
                fontSize   = 16.sp,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text       = "Since this data is also personal health information, it will be encrypted and " +
                             "stored in your device (otherwise the cloud if you opted in)",
                color      = AppColors.White,
                fontSize   = 16.sp,
                lineHeight = 24.sp
            )

            Spacer(Modifier.weight(1f))
            OnboardingContinueButton(onClick = onNavigateNext)
            Spacer(Modifier.height(16.dp))
        }
    }
}
