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
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import sleeptracker.composeapp.generated.resources.Res
import sleeptracker.composeapp.generated.resources.journal_bg

// MIGRATION: TypeScript `(onboarding)/journal-data.tsx` → `JournalDataScreen.kt`.
// flex: topHalf=3, bottomHalf=6. backgroundColor: 'black'.
// Exact RN text for Journal Data and Derived Data sections preserved.

@OptIn(ExperimentalResourceApi::class)
@Composable
fun JournalDataScreen(
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
    onNavigateToPrivacyPolicy: (sectionId: String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // Top 33%: background image with header (flex: 3 of 3+6=9)
        Box(modifier = Modifier.fillMaxWidth().weight(3f)) {
            Image(
                painter            = painterResource(Res.drawable.journal_bg),
                contentDescription = null,
                modifier           = Modifier.fillMaxSize(),
                contentScale       = ContentScale.Crop
            )
            OnboardingHeader(
                title       = "Your Privacy Matters to Us",
                onBackPress = onNavigateBack
            )
        }

        // Bottom 67%: information content (flex: 6 of 9)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(6f)
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Text(
                text       = "Journal Data:",
                color      = AppColors.White,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text       = "Information about your mood, habits, symptoms can help us correlate your " +
                             "personal experiences with your sleep patterns. You can voluntarily provide " +
                             "us with this data by making diary entries and sleep notes in the app's Journal section.",
                color      = AppColors.White,
                fontSize   = 16.sp,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text           = "More about collecting journal data",
                color          = AppColors.HyperlinkBlue,
                fontSize       = 14.sp,
                textDecoration = TextDecoration.Underline,
                modifier       = Modifier.clickable { onNavigateToPrivacyPolicy("journalData") }
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text       = "Derived Data:",
                color      = AppColors.White,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text       = "The app will derive data about you such as sleep quality, correlations, " +
                             "insights and recommendations. This will be treated as sensitive personal health information.",
                color      = AppColors.White,
                fontSize   = 16.sp,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text           = "More about derived data",
                color          = AppColors.HyperlinkBlue,
                fontSize       = 14.sp,
                textDecoration = TextDecoration.Underline,
                modifier       = Modifier.clickable { onNavigateToPrivacyPolicy("derivedData") }
            )

            Spacer(Modifier.weight(1f))
            OnboardingContinueButton(onClick = onNavigateNext)
        }
    }
}
