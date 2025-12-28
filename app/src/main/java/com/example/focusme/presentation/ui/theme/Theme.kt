package com.example.focusme.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColors = lightColorScheme(
    primary = PinkPrimary,
    secondary = PinkPrimaryDark,
    background = PinkSoftBg,
    surface = SurfaceCard
)

@Composable
fun StudyFocusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColors,
        typography = AppTypography,
        content = content
    )
}
