package com.sleeptracker.presentation.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeptracker.constants.AppColors
import com.sleeptracker.presentation.viewmodel.AuthUiState
import com.sleeptracker.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

// MIGRATION: TypeScript `(auth)/index.tsx` → `LoginScreen.kt`.
// Title "Welcome Back!", subtitle "Sign in to your account" (both white, 32sp/16sp).
// Button text "Sign In". backgroundColor: 'black'.
// "Don't have an account?" white + "Register" in HyperlinkBlue 16sp/w600.

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val uiState      by authViewModel.uiState.collectAsState()
    val scope        = rememberCoroutineScope()
    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var error        by remember { mutableStateOf<String?>(null) }
    var showPassword by remember { mutableStateOf(false) }

    val isLoading = uiState is AuthUiState.Loading

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text       = "Welcome Back!",
            color      = AppColors.White,
            fontSize   = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(text = "Sign in to your account", color = AppColors.White, fontSize = 16.sp)

        Spacer(Modifier.height(40.dp))

        AuthTextField(
            value         = email,
            onValueChange = { email = it },
            label         = "Email",
            keyboardType  = KeyboardType.Email
        )
        Spacer(Modifier.height(16.dp))
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

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(text = error!!, color = AppColors.TooltipRed, fontSize = 12.sp)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick  = {
                scope.launch {
                    val result = authViewModel.login(email, password)
                    if (result.success) {
                        onLoginSuccess()
                    } else {
                        error = result.message
                    }
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
                Text("Sign In", color = AppColors.LightBlack, fontWeight = FontWeight.W600, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(16.dp))
        // "Don't have an account?" white + "Register" in HyperlinkBlue — matches RN
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("Don't have an account? ", color = AppColors.White, fontSize = 16.sp)
            TextButton(onClick = onNavigateToRegister, contentPadding = PaddingValues(0.dp)) {
                Text("Register", color = AppColors.HyperlinkBlue, fontSize = 16.sp, fontWeight = FontWeight.W600)
            }
        }
    }
}

// MIGRATION: RN `AuthInput` — filled rectangle, NO border, NO floating label,
// placeholder text, borderRadius:8, paddingHorizontal:16, paddingVertical:16,
// fontSize:16, color:white, placeholderTextColor:#9CA3AF.
// Uses BasicTextField inside a styled Box to exactly replicate the RN look.
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.InputFieldBackground),
        contentAlignment = Alignment.CenterStart
    ) {
        androidx.compose.foundation.text.BasicTextField(
            value               = value,
            onValueChange       = onValueChange,
            singleLine          = true,
            textStyle           = androidx.compose.ui.text.TextStyle(
                color    = AppColors.White,
                fontSize = 16.sp
            ),
            keyboardOptions     = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction    = ImeAction.Next
            ),
            visualTransformation = if (isPassword && !showPassword)
                PasswordVisualTransformation() else VisualTransformation.None,
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(end = if (trailingIcon != null) 40.dp else 0.dp),
            decorationBox       = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text  = label,
                        color = Color(0xFF9CA3AF),
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        )
        if (trailingIcon != null) {
            Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)) {
                trailingIcon()
            }
        }
    }
}
