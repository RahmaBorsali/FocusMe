package com.example.focusme.presentation.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusme.presentation.ui.components.PrimaryButton
import com.example.focusme.presentation.ui.components.SweetAlertDialog
import com.example.focusme.presentation.ui.components.SweetAlertType
import com.example.focusme.presentation.ui.theme.*

@Composable
fun ResetPasswordConfirmScreen(
    email: String,
    code: String,
    onBack: () -> Unit,
    onResetSuccess: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    val uiState by vm.ui.collectAsState()
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var show1 by remember { mutableStateOf(false) }
    var show2 by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.success) {
        if (uiState.success == "Mot de passe changer.") {
            showSuccessDialog = true
        }
    }

    if (showSuccessDialog) {
        SweetAlertDialog(
            title = "Success! ✨",
            message = "Your password has been updated successfully.",
            type = SweetAlertType.INFO,
            onConfirm = {
                showSuccessDialog = false
                onResetSuccess()
            },
            onDismiss = { showSuccessDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(AppBg, Color.White)))
            .padding(24.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(top = 16.dp).clip(CircleShape).background(PinkPrimary.copy(alpha = 0.1f))
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PinkPrimary)
        }

        Spacer(Modifier.height(40.dp))

        Text("Set New Password", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
        Text(
            "Final step! Choose a strong password for $email.",
            fontSize = 16.sp, color = TextGray, modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.height(48.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; vm.clearFieldErrors() },
            label = { Text("New Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PinkPrimary) },
            trailingIcon = {
                IconButton(onClick = { show1 = !show1 }) {
                    Icon(if (show1) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            singleLine = true,
            visualTransformation = if (show1) VisualTransformation.None else PasswordVisualTransformation(),
            isError = uiState.passwordError != null,
            supportingText = { uiState.passwordError?.let { Text(it) } },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, unfocusedBorderColor = BorderSoft)
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it; vm.clearFieldErrors() },
            label = { Text("Confirm New Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PinkPrimary) },
            trailingIcon = {
                IconButton(onClick = { show2 = !show2 }) {
                    Icon(if (show2) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            singleLine = true,
            visualTransformation = if (show2) VisualTransformation.None else PasswordVisualTransformation(),
            isError = uiState.confirmError != null,
            supportingText = { uiState.confirmError?.let { Text(it) } },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, unfocusedBorderColor = BorderSoft)
        )

        if (uiState.error != null) {
            Spacer(Modifier.height(16.dp))
            Text(uiState.error!!, color = Color.Red, fontSize = 14.sp)
        }

        Spacer(Modifier.height(48.dp))

        PrimaryButton(
            text = if (uiState.loading) "Updating..." else "Update Password",
            onClick = {
                if (!uiState.loading) {
                    vm.resetPassword(email, code, password, confirm, onDone = {})
                }
            },
            modifier = Modifier.fillMaxWidth().height(58.dp)
        )
    }
}
