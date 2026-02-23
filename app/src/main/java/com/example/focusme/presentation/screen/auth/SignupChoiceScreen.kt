package com.example.focusme.presentation.screen.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusme.presentation.ui.components.PrimaryButton
import com.example.focusme.presentation.ui.components.SoftCard
import com.example.focusme.presentation.ui.theme.*

@Composable
fun SignupChoiceScreen(
    onBack: () -> Unit,
    onContinueWithGoogle: () -> Unit,
    onContinueWithEmail: () -> Unit,
    onLogin: () -> Unit
) {
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
                onClick = onContinueWithGoogle,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
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
                        imageVector = Icons.Default.Circle, // Placeholder for Google Icon
                        contentDescription = null,
                        tint = GoogleRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Continue with Google", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
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