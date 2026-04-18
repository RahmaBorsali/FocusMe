package com.example.focusme.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.focusme.presentation.ui.theme.PinkPrimary

enum class SweetAlertType {
    WARNING, ERROR, INFO
}

@Composable
fun SweetAlertDialog(
    title: String,
    message: String,
    type: SweetAlertType = SweetAlertType.WARNING,
    confirmButtonText: String = "OK",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon Header
                val (icon, color) = when (type) {
                    SweetAlertType.WARNING -> Icons.Default.Warning to Color(0xFFFFA000)
                    SweetAlertType.ERROR -> Icons.Default.Error to Color(0xFFD32F2F)
                    SweetAlertType.INFO -> Icons.Default.Info to Color(0xFF1976D2)
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Title
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3142),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                // Message
                Text(
                    text = message,
                    fontSize = 16.sp,
                    color = Color(0xFF9094A6),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(Modifier.height(32.dp))

                // Action Button
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
                ) {
                    Text(
                        text = confirmButtonText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
