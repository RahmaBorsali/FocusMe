package com.example.focusme.presentation.screen.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.focusme.presentation.ui.components.PrimaryButton
import com.example.focusme.presentation.ui.components.SweetAlertDialog
import com.example.focusme.presentation.ui.components.SweetAlertType
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

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLogin(email, password)
        }
    }

    LaunchedEffect(uiState.isNewUser) {
        if (uiState.isNewUser) {
            onCreateAccount()
        }
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success != null) {
            onLogin(email, password)
        }
    }

    if (uiState.showLoginSignupRedirect) {
        SweetAlertDialog(
            title = "Account Not Found",
            message = "This Google account is not registered. Please sign up first to join FocusMe!",
            type = SweetAlertType.WARNING,
            confirmButtonText = "Get Started",
            onConfirm = {
                vm.dismissDialogs()
                onCreateAccount()
            },
            onDismiss = { vm.dismissDialogs() }
        )
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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PinkPrimary)
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
                onValueChange = {
                    email = it
                    vm.clearFieldErrors()
                },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PinkPrimary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                isError = uiState.emailError != null,
                supportingText = {
                    uiState.emailError?.let { Text(it) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkPrimary,
                    unfocusedBorderColor = BorderSoft,
                    focusedLabelColor = PinkPrimary,
                    errorBorderColor = Color(0xFFD32F2F),
                    errorLabelColor = Color(0xFFD32F2F)
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    vm.clearFieldErrors()
                },
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
                isError = uiState.passwordError != null,
                supportingText = {
                    uiState.passwordError?.let { Text(it) }
                },
                visualTransformation = if (showPwd) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkPrimary,
                    unfocusedBorderColor = BorderSoft,
                    focusedLabelColor = PinkPrimary,
                    errorBorderColor = Color(0xFFD32F2F),
                    errorLabelColor = Color(0xFFD32F2F)
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
                Text(uiState.error!!, color = Color(0xFFD32F2F), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(12.dp))

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

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSoft)
                Text(
                    " OR ",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = TextGray,
                    fontSize = 14.sp
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSoft)
            }
            Spacer(Modifier.height(16.dp))

            val activity = androidx.compose.ui.platform.LocalContext.current as? androidx.activity.ComponentActivity
            Button(
                onClick = {
                    if (!uiState.loading && activity != null) {
                        vm.loginWithGoogle(activity, "login")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                enabled = !uiState.loading && activity != null,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = TextDark
                ),
                border = BorderStroke(1.dp, BorderSoft),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = PinkPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        if (uiState.loading) "Connecting..." else "Continue with Google",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }
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
