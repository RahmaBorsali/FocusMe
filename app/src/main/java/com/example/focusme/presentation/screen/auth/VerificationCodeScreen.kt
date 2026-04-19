package com.example.focusme.presentation.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusme.presentation.ui.components.PrimaryButton
import com.example.focusme.presentation.ui.components.SweetAlertDialog
import com.example.focusme.presentation.ui.components.SweetAlertType
import com.example.focusme.presentation.ui.theme.*

enum class VerificationMode { SIGNUP, RESET_PASSWORD }

@Composable
fun VerificationCodeScreen(
    email: String,
    mode: VerificationMode,
    onBack: () -> Unit,
    onSuccess: (String) -> Unit,
    vm: AuthViewModel = viewModel()
) {
    val uiState by vm.ui.collectAsState()
    val code = remember { mutableStateListOf("", "", "", "", "", "") }
    val focusRequesters = remember { List(6) { FocusRequester() } }
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.success) {
        if (uiState.success == "Email verifier avec succes !" && mode == VerificationMode.SIGNUP) {
            showSuccessDialog = true
        }
    }

    if (showSuccessDialog) {
        SweetAlertDialog(
            title = "Compte activé ! ✨",
            message = "Ton adresse email a été vérifiée. Tu peux maintenant te connecter.",
            type = SweetAlertType.INFO,
            onConfirm = {
                showSuccessDialog = false
                onSuccess("") // For signup, we go to login
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

        Text(
            if (mode == VerificationMode.SIGNUP) "Verify Account" else "Verification Code",
            fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = TextDark
        )
        Text(
            "Enter the 6-digit code sent to $email.",
            fontSize = 16.sp, color = TextGray, modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 6) {
                OutlinedTextField(
                    value = code[i],
                    onValueChange = { newValue ->
                        if (newValue.length <= 1) {
                            code[i] = newValue
                            if (newValue.isNotEmpty() && i < 5) {
                                focusRequesters[i + 1].requestFocus()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .focusRequester(focusRequesters[i]),
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PinkPrimary,
                        unfocusedBorderColor = BorderSoft
                    )
                )
            }
        }

        if (uiState.error != null) {
            Spacer(Modifier.height(16.dp))
            Text(uiState.error!!, color = Color.Red, fontSize = 14.sp)
        }

        Spacer(Modifier.height(48.dp))

        PrimaryButton(
            text = if (uiState.loading) "Verifying..." else "Verify Code",
            onClick = {
                val fullCode = code.joinToString("")
                if (fullCode.length == 6) {
                    if (mode == VerificationMode.SIGNUP) {
                        vm.verifySignupEmail(email, fullCode, onDone = {})
                    } else {
                        onSuccess(fullCode)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(58.dp)
        )
    }
}
