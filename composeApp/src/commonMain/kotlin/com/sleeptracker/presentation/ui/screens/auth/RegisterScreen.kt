package com.sleeptracker.presentation.ui.screens.auth

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeptracker.constants.AppColors
import com.sleeptracker.presentation.viewmodel.AuthUiState
import com.sleeptracker.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

// MIGRATION: TypeScript `(auth)/register.tsx` → `RegisterScreen.kt`.
// Field order: Email → FirstName → LastName → Password → ConfirmPassword (matches RN).
// "Do you have an account?" white + "Sign In" in HyperlinkBlue 16sp/w600.
// backgroundColor: 'black'. GeneralButton text "Register" in LightBlack.

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val uiState         by authViewModel.uiState.collectAsState()
    val scope           = rememberCoroutineScope()
    var email           by remember { mutableStateOf("") }
    var firstName       by remember { mutableStateOf("") }
    var lastName        by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error           by remember { mutableStateOf<String?>(null) }
    var showPassword    by remember { mutableStateOf(false) }
    var showConfirm     by remember { mutableStateOf(false) }

    val isLoading = uiState is AuthUiState.Loading

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Register Now!", color = AppColors.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(text = "Create an account", color = AppColors.White, fontSize = 16.sp)
        Spacer(Modifier.height(40.dp))

        // Field order matches RN: Email, FirstName, LastName, Password, ConfirmPassword
        AuthTextField(value = email, onValueChange = { email = it }, label = "Email", keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(12.dp))
        AuthTextField(value = firstName, onValueChange = { firstName = it }, label = "First Name")
        Spacer(Modifier.height(12.dp))
        AuthTextField(value = lastName, onValueChange = { lastName = it }, label = "Last Name")
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            value         = password,
            onValueChange = { password = it },
            label         = "Password",
            isPassword    = true,
            showPassword  = showPassword,
            trailingIcon  = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector        = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (showPassword) "Hide password" else "Show password",
                        tint               = AppColors.InputFieldPlaceholder,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            value         = confirmPassword,
            onValueChange = { confirmPassword = it },
            label         = "Confirm Password",
            isPassword    = true,
            showPassword  = showConfirm,
            trailingIcon  = {
                IconButton(onClick = { showConfirm = !showConfirm }) {
                    Icon(
                        imageVector        = if (showConfirm) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (showConfirm) "Hide password" else "Show password",
                        tint               = AppColors.InputFieldPlaceholder,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }
        )

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(text = error!!, color = AppColors.TooltipRed, fontSize = 12.sp)
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick  = {
                if (password != confirmPassword) {
                    error = "Passwords do not match"
                    return@Button
                }
                scope.launch {
                    val result = authViewModel.register(firstName, lastName, email, password)
                    if (result.success) onRegisterSuccess() else error = result.message
                }
            },
            enabled  = !isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(8.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = AppColors.Accent)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = AppColors.White, modifier = Modifier.size(20.dp))
            } else {
                Text("Register", color = AppColors.LightBlack, fontWeight = FontWeight.W600, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(16.dp))
        // "Do you have an account?" white + "Sign In" in HyperlinkBlue — matches RN
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("Do you have an account? ", color = AppColors.White, fontSize = 16.sp)
            TextButton(onClick = onNavigateToLogin, contentPadding = PaddingValues(0.dp)) {
                Text("Sign In", color = AppColors.HyperlinkBlue, fontSize = 16.sp, fontWeight = FontWeight.W600)
            }
        }
    }
}
