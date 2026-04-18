package com.example.focusme.presentation.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.focusme.presentation.ui.components.PrimaryButton
import com.example.focusme.presentation.ui.theme.*

@Composable
fun SignupScreen(
    onBack: () -> Unit,
    onSignup: (String, String, String, String) -> Unit,
    vm: AuthViewModel = viewModel()
) {
    val uiState by vm.ui.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var show1 by remember { mutableStateOf(false) }
    var show2 by remember { mutableStateOf(false) }

    // Pre-fill from Google if available
    LaunchedEffect(uiState.isNewUser) {
        if (uiState.isNewUser) {
            uiState.googleName?.let { username = it }
            uiState.googleEmail?.let { email = it }
        }
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success != null) {
            showDialog = true
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Registration Successful ✨") },
            text = { Text("Check your email verification to active your account.") },
            confirmButton = {
                TextButton(onClick = { 
                    showDialog = false
                    onSignup(username, email, password, confirm) // This triggers navigation in caller
                }) {
                    Text("OK", color = PinkPrimary, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
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
            .verticalScroll(rememberScrollState())
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
            "Create Account",
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark
        )
        Text(
            "Join the community and stay focused.",
            fontSize = 16.sp,
            color = TextGray,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(40.dp))

        // Registration form
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    vm.clearFieldErrors()
                },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PinkPrimary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                isError = uiState.usernameError != null,
                supportingText = {
                    uiState.usernameError?.let { Text(it) }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkPrimary,
                    unfocusedBorderColor = BorderSoft,
                    focusedLabelColor = PinkPrimary,
                    errorBorderColor = Color(0xFFD32F2F),
                    errorLabelColor = Color(0xFFD32F2F)
                )
            )

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
                    IconButton(onClick = { show1 = !show1 }) {
                        Icon(
                            if (show1) Icons.Default.Visibility else Icons.Default.VisibilityOff,
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
                visualTransformation = if (show1) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkPrimary,
                    unfocusedBorderColor = BorderSoft,
                    focusedLabelColor = PinkPrimary,
                    errorBorderColor = Color(0xFFD32F2F),
                    errorLabelColor = Color(0xFFD32F2F)
                )
            )

            OutlinedTextField(
                value = confirm,
                onValueChange = {
                    confirm = it
                    vm.clearFieldErrors()
                },
                label = { Text("Confirm Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PinkPrimary) },
                trailingIcon = {
                    IconButton(onClick = { show2 = !show2 }) {
                        Icon(
                            if (show2) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = TextGray
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                isError = uiState.confirmError != null,
                supportingText = {
                    uiState.confirmError?.let { Text(it) }
                },
                visualTransformation = if (show2) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkPrimary,
                    unfocusedBorderColor = BorderSoft,
                    focusedLabelColor = PinkPrimary,
                    errorBorderColor = Color(0xFFD32F2F),
                    errorLabelColor = Color(0xFFD32F2F)
                )
            )

            if (uiState.error != null) {
                Text(uiState.error!!, color = Color(0xFFD32F2F), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(24.dp))

            PrimaryButton(
                text = if (uiState.loading) "Creating account..." else "Create Account",
                onClick = { 
                    if (!uiState.loading) {
                        vm.signup(username.trim(), email.trim(), password, confirm) 
                    }
                },
                modifier = Modifier.fillMaxWidth().height(58.dp)
            )
        }
        
        Spacer(Modifier.height(24.dp))
    }
}
