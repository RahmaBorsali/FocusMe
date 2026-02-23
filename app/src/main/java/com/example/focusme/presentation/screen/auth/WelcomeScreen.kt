package com.example.focusme.presentation.screen.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusme.R
import com.example.focusme.presentation.ui.components.PrimaryButton
import com.example.focusme.presentation.ui.theme.*

@Composable
fun WelcomeScreen(
    onStartJourney: () -> Unit,
    onHaveAccount: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AppBg, Color.White)
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(60.dp))

        // Large attractive title with a modern touch
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "FocusMe",
                fontSize = 54.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PinkPrimary,
                letterSpacing = (-1.5).sp
            )
            Text(
                "Master your time, master your life",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextGray
            )
        }

        Spacer(Modifier.weight(0.7f))

        // Central Illustration/Logo area
        Box(contentAlignment = Alignment.Center) {
            // Very subtle soft glow/shadow effect
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(PinkPrimary.copy(alpha = 0.03f))
            )
            
            // Minimalist logo container
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(45.dp))
                    .background(Color.White)
                    .border(1.dp, BorderSoft, RoundedCornerShape(45.dp))
                    .padding(30.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_focusme_logo),
                    contentDescription = "FocusMe logo",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Action area
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryButton(
                text = "Get Started ✨",
                onClick = onStartJourney,
                modifier = Modifier.fillMaxWidth().height(58.dp)
            )

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Already have an account? ",
                    color = TextGray,
                    fontSize = 15.sp
                )
                Text(
                    "Log in",
                    color = PinkPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.clickable(onClick = onHaveAccount)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Continue as guest",
                modifier = Modifier.clickable(onClick = onContinueAsGuest),
                color = TextGray.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
        
        Spacer(Modifier.height(20.dp))
    }
}