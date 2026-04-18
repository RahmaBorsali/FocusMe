package com.example.focusme.presentation.screen.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusme.presentation.ui.components.PrimaryButton
import com.example.focusme.presentation.ui.components.SweetAlertDialog
import com.example.focusme.presentation.ui.components.SweetAlertType
import com.example.focusme.presentation.ui.theme.*

@Composable
fun SignupChoiceScreen(
    onBack: () -> Unit,
    onContinueWithGoogle: () -> Unit,
    onContinueWithEmail: () -> Unit,
    onLogin: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    val uiState by vm.ui.collectAsState()
    val activity = androidx.compose.ui.platform.LocalContext.current as? androidx.activity.ComponentActivity

    androidx.compose.runtime.LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLogin()
        }
    }

    androidx.compose.runtime.LaunchedEffect(uiState.isNewUser) {
        if (uiState.isNewUser) {
            onContinueWithEmail() // Redirige vers SignupScreen
        }
    }

    if (uiState.showSignupEmailExistsDialog) {
        SweetAlertDialog(
            title = "Email Already Exists",
            message = "This email is already registered. Please log in to your account.",
            type = SweetAlertType.INFO,
            confirmButtonText = "Log In",
            onConfirm = {
                vm.dismissDialogs()
                onLogin()
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
            "Join FocusMe",
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark,
            lineHeight = 44.sp
        )
        Text(
            "The best way to stay productive and achieve your study goals.",
            fontSize = 16.sp,
            color = TextGray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Google Button
            Button(
                onClick = {
                    if (!uiState.loading && activity != null) {
                        vm.loginWithGoogle(activity, "signup")
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
                    GoogleLogoBadge()
                    Spacer(Modifier.width(12.dp))
                    Text(
                        if (uiState.loading) "Connexion Google..." else "Continue with Google",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            uiState.error?.let {
                Text(
                    text = it,
                    color = Color(0xFFD32F2F),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            // Email Button (Primary)
            PrimaryButton(
                text = "Continue with Email",
                onClick = onContinueWithEmail,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
            )
        }

        Spacer(Modifier.weight(0.5f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already have an account? ", color = TextGray)
            Text(
                "Log in",
                color = PinkPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onLogin)
            )
        }
    }
}

@Composable
private fun GoogleLogoBadge() {
    Surface(
        shape = CircleShape,
        color = Color.White,
        border = BorderStroke(1.dp, BorderSoft),
        modifier = Modifier.size(24.dp)
    ) {
        Canvas(modifier = Modifier.padding(4.dp)) {
            val stroke = Stroke(width = size.minDimension * 0.18f, cap = StrokeCap.Round)
            val arcSize = androidx.compose.ui.geometry.Size(size.width, size.height)

            drawArc(
                color = GoogleBlue,
                startAngle = -40f,
                sweepAngle = 110f,
                useCenter = false,
                style = stroke,
                size = arcSize
            )
            drawArc(
                color = GoogleRed,
                startAngle = 70f,
                sweepAngle = 85f,
                useCenter = false,
                style = stroke,
                size = arcSize
            )
            drawArc(
                color = GoogleYellow,
                startAngle = 155f,
                sweepAngle = 75f,
                useCenter = false,
                style = stroke,
                size = arcSize
            )
            drawArc(
                color = GoogleGreen,
                startAngle = 230f,
                sweepAngle = 95f,
                useCenter = false,
                style = stroke,
                size = arcSize
            )

            val centerY = size.height / 2f
            drawLine(
                color = GoogleBlue,
                start = androidx.compose.ui.geometry.Offset(size.width * 0.54f, centerY),
                end = androidx.compose.ui.geometry.Offset(size.width * 0.88f, centerY),
                strokeWidth = size.minDimension * 0.18f,
                cap = StrokeCap.Round
            )
        }
    }
}
