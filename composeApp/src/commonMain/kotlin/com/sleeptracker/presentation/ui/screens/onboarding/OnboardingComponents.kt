package com.sleeptracker.presentation.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeptracker.constants.AppColors

// MIGRATION: React Native shared onboarding components → Compose equivalents.
//   `OnboardingHeader` → header Row with back button + title
//   `PermissionsToggle` → Row with label + Switch
//   `GeneralButton` → filled Box-based button composable
//   `OnboardingQuestionOption` → selectable option Row

// ─── OnboardingHeader ─────────────────────────────────────────────────────────
// MIGRATION: RN `OnboardingHeader` — paddingHorizontal:20, title fontSize:24/bold,
//   chevron-back size:24 in generalBlue, back button is 40×40 TouchableOpacity.
@Composable
fun OnboardingHeader(
    title: String,
    onBackPress: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackPress != null) {
            Box(
                modifier          = Modifier
                    .size(40.dp)
                    .clickable(onClick = onBackPress),
                contentAlignment  = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint               = AppColors.GeneralBlue,
                    modifier           = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text       = title,
                color      = AppColors.White,
                fontSize   = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Start,
                modifier   = Modifier.weight(1f)
            )
        } else {
            Text(
                text       = title,
                color      = AppColors.White,
                fontSize   = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─── PermissionsToggle ────────────────────────────────────────────────────────
// MIGRATION: RN PermissionsToggle — paddingHorizontal:20, fontSize:16, flex:2 label,
//   trackColor { false: "#ccc", true: "#4CAF50" }, thumbColor white always.
@Composable
fun PermissionsToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text     = label,
            color    = AppColors.White,
            fontSize = 16.sp,
            modifier = Modifier.weight(2f)
        )
        Spacer(Modifier.width(10.dp))
        Switch(
            checked         = checked,
            onCheckedChange = onCheckedChange,
            colors          = SwitchDefaults.colors(
                checkedThumbColor   = AppColors.White,
                checkedTrackColor   = Color(0xFF4CAF50),
                uncheckedThumbColor = AppColors.White,
                uncheckedTrackColor = Color(0xFFCCCCCC)
            )
        )
    }
}

// ─── GeneralButton (OnboardingContinueButton) ─────────────────────────────────
// MIGRATION: RN GeneralButton — backgroundColor:generalBlue, borderRadius:8,
//   paddingVertical:16, text color:lightBlack (#181719), fontSize:16, fontWeight:600.
@Composable
fun OnboardingContinueButton(
    title: String = "Continue",
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier         = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (enabled) AppColors.Accent else AppColors.Accent.copy(alpha = 0.6f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = title,
            color      = AppColors.LightBlack,
            fontSize   = 16.sp,
            fontWeight = FontWeight.W600
        )
    }
}

// ─── OnboardingQuestionOption ─────────────────────────────────────────────────
// MIGRATION: RN OnboardingQuestionOption — transparent bg unselected / generalBlue
//   filled when selected; border always generalBlue; checkmark icon when selected;
//   text:generalBlue unselected → white selected; fontSize:18/bold; borderRadius:15.
@Composable
fun OnboardingQuestionOption(
    label: String,
    isSelected: Boolean,
    onPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(if (isSelected) AppColors.GeneralBlue else Color.Transparent)
            .border(1.dp, AppColors.GeneralBlue, RoundedCornerShape(15.dp))
            .clickable(onClick = onPress)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text       = label,
            color      = if (isSelected) AppColors.White else AppColors.GeneralBlue,
            fontSize   = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.weight(1f),
            textAlign  = TextAlign.Center
        )
        if (isSelected) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector        = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint               = AppColors.White,
                modifier           = Modifier.size(24.dp)
            )
        }
    }
}
