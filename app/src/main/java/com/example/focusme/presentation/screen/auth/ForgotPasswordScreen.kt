package com.example.focusme.presentation.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusme.presentation.ui.components.PrimaryButton
import com.example.focusme.presentation.ui.theme.*

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onResetPassword: (String) -> Unit,
    vm: AuthViewModel = viewModel()
) {
    val uiState by vm.ui.collectAsState()
    var email by remember { mutableStateOf("") }

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
            "Forgot Password?",
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark
        )
        Text(
            "Enter your email address and we'll send you a link to reset your password.",
            fontSize = 16.sp,
            color = TextGray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.height(48.dp))

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

        uiState.error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = Color(0xFFD32F2F), style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(32.dp))

        PrimaryButton(
            text = if (uiState.loading) "Envoi..." else "Reset Password",
            onClick = {
                if (!uiState.loading) {
                    vm.forgotPassword(email.trim(), onDone = { onResetPassword(email.trim()) })
                }
            },
            modifier = Modifier.fillMaxWidth().height(58.dp)
        )

        Spacer(Modifier.weight(1f))
    }
}
