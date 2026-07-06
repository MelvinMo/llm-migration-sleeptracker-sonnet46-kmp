package com.sleeptracker.presentation.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeptracker.constants.AppColors
import com.sleeptracker.presentation.viewmodel.AuthViewModel
import com.sleeptracker.presentation.viewmodel.UserProfileViewModel

// MIGRATION: TypeScript `(tabs)/profile/index.tsx` → `ProfileScreen.kt`.
// Layout: flex:1 + justifyContent:'space-between' → Arrangement.SpaceBetween.
// MenuItem: paddingVertical:28, paddingHorizontal:16, bg:lightBlack, fontSize:18, fontWeight:500.
// Logout: paddingVertical:18, paddingHorizontal:30, fontSize:18, fontWeight:bold.

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    profileViewModel: UserProfileViewModel,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToConsentPreferences: () -> Unit = {}
) {
    val user = authViewModel.currentUser

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Page header — paddingTop:30 + vertical:20 = 50dp from top (status bar clearance)
        Text(
            text       = "Profile",
            color      = AppColors.White,
            fontSize   = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp)
        )

        // 2. User info — alignItems:'center', marginBottom:50 provided by SpaceBetween gap
        Column(
            modifier            = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val displayName = user?.firstName?.takeIf { it.isNotBlank() } ?: "Guest"
            Text(
                text       = "Hello, $displayName",
                color      = AppColors.White,
                fontSize   = 24.sp,
                fontWeight = FontWeight.W600
            )
        }

        // 3. Menu items — width:'100%', marginBottom:50 provided by SpaceBetween gap
        Column(modifier = Modifier.fillMaxWidth()) {
            MenuItem(label = "Consent Preferences", onClick = onNavigateToConsentPreferences)
            Spacer(Modifier.height(15.dp))
            MenuItem(label = "Privacy Policy",       onClick = onNavigateToPrivacyPolicy)
        }

        // 4. Logout button — paddingVertical:18, paddingHorizontal:30, borderRadius:12
        Button(
            onClick         = { authViewModel.logout() },
            modifier        = Modifier.fillMaxWidth(),
            shape           = RoundedCornerShape(12.dp),
            contentPadding  = PaddingValues(horizontal = 30.dp, vertical = 18.dp),
            colors          = ButtonDefaults.buttonColors(containerColor = AppColors.Accent)
        ) {
            Text("LOGOUT", color = AppColors.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

// MIGRATION: `MenuItem.tsx` — paddingVertical:28, paddingHorizontal:16, bg:lightBlack,
// borderRadius:12, marginBottom:15, fontSize:18, fontWeight:'500', chevron-forward in generalBlue.
@Composable
private fun MenuItem(label: String, onClick: () -> Unit) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .background(AppColors.LightBlack, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text       = label,
            color      = AppColors.White,
            fontSize   = 18.sp,
            fontWeight = FontWeight.W500,
            modifier   = Modifier.weight(1f)
        )
        Icon(
            imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint               = AppColors.GeneralBlue,
            modifier           = Modifier.size(18.dp)
        )
    }
}
