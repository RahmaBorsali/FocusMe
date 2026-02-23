package com.example.focusme.presentation.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusme.presentation.ui.components.PrimaryButton
import com.example.focusme.presentation.ui.components.SoftCard
import com.example.focusme.presentation.ui.theme.*

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLogin: (String, String) -> Unit,
    onCreateAccount: () -> Unit,
    onForgotPassword: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    val uiState by vm.ui.collectAsState()
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPwd by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.success) {
        if (uiState.success != null) {
            onLogin(email, password) // Since navigation is handled by caller or we can do it here
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AppBg, Color.White)
                )
            )
            .padding(24.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(top = 16.dp)
                .clip(CircleShape)
                .background(PinkPrimary.copy(alpha = 0.1f))
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PinkPrimary)
        }

        Spacer(Modifier.height(40.dp))

        Text(
            "Welcome Back",
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark
        )
        Text(
            "Log in to continue your journey.",
            fontSize = 16.sp,
            color = TextGray,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(40.dp))

        // Input Fields Area
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PinkPrimary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkPrimary,
                    unfocusedBorderColor = BorderSoft,
                    focusedLabelColor = PinkPrimary
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PinkPrimary) },
                trailingIcon = {
                    IconButton(onClick = { showPwd = !showPwd }) {
                        Icon(
                            if (showPwd) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = TextGray
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                visualTransformation = if (showPwd) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkPrimary,
                    unfocusedBorderColor = BorderSoft,
                    focusedLabelColor = PinkPrimary
                )
            )

            Text(
                "Forgot Password?",
                modifier = Modifier.align(Alignment.End).clickable { onForgotPassword() },
                color = PinkPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )

            if (uiState.error != null) {
                Text(uiState.error!!, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(Modifier.height(24.dp))

            PrimaryButton(
                text = if (uiState.loading) "Logging in..." else "Log In",
                onClick = { 
                    if (!uiState.loading) {
                        vm.login(email.trim(), password) {
                            // Navigation handled by effect
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(58.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account? ", color = TextGray)
            Text(
                "Sign Up",
                color = PinkPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onCreateAccount)
            )
        }
    }
}
