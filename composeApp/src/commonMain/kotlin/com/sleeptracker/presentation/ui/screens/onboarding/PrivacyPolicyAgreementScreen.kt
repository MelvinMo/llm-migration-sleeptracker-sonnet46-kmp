package com.sleeptracker.presentation.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeptracker.constants.AppColors
import com.sleeptracker.model.UserConsentPreferences

// MIGRATION: TypeScript `(onboarding)/privacy-policy-agreement.tsx` → Compose.
// Custom 24×24 checkbox: borderRadius 6, border generalBlue (2dp), fill generalBlue when checked,
// white "✓" checkmark 16sp bold. backgroundColor: 'black'. Exact RN description text.
// Continue button disabled (not re-labelled) when unchecked — matches RN GeneralButton disabled prop.

@Composable
fun PrivacyPolicyAgreementScreen(
    consentPreferences: UserConsentPreferences,
    onUpdateConsent: (UserConsentPreferences) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit
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
                text       = "The previous screens explained the most important parts of the privacy policy. " +
                             "Before you proceed, please review the full Privacy Policy to understand in " +
                             "greater detail how we collect, use, and protect your health data.",
                color      = AppColors.White,
                fontSize   = 16.sp,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text           = "Read our full Privacy Policy",
                color          = AppColors.HyperlinkBlue,
                fontSize       = 16.sp,
                textDecoration = TextDecoration.Underline,
                modifier       = Modifier.clickable { onNavigateToPrivacyPolicy() }
            )
            Spacer(Modifier.height(32.dp))

            // Custom checkbox — 24×24, borderRadius 6, generalBlue border/fill, "✓" checkmark
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onUpdateConsent(
                            consentPreferences.copy(
                                agreedToPrivacyPolicy = !consentPreferences.agreedToPrivacyPolicy
                            )
                        )
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier         = Modifier
                        .size(24.dp)
                        .background(
                            color  = if (consentPreferences.agreedToPrivacyPolicy) AppColors.GeneralBlue else Color.Transparent,
                            shape  = RoundedCornerShape(6.dp)
                        )
                        .border(2.dp, AppColors.GeneralBlue, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (consentPreferences.agreedToPrivacyPolicy) {
                        Text(
                            text       = "✓",
                            color      = AppColors.White,
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text     = "I have read and agree to the Privacy Policy.",
                    color    = AppColors.White,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.weight(1f))
            OnboardingContinueButton(
                title   = "Continue",
                onClick = { if (consentPreferences.agreedToPrivacyPolicy) onNavigateNext() },
                enabled = consentPreferences.agreedToPrivacyPolicy
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
