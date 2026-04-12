package com.example.focusme.presentation.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusme.presentation.ui.theme.AppBg
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextDark
import com.example.focusme.presentation.ui.theme.TextGray

@Composable
fun ProfileAchievementsScreen(
    onBack: () -> Unit,
    vm: ProfileViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsState()
    val levelInfo = remember(ui.totalXp) { profileLevelInfo(ui.totalXp) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AchievementsHeader(onBack = onBack)
        }

        item {
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = Color.White,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Niveau ${levelInfo.level}",
                        color = PinkPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = levelInfo.title,
                        color = TextDark,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${levelInfo.currentXp} XP cumules",
                        color = TextGray
                    )
                    Spacer(Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { levelInfo.progress.coerceAtLeast(0.02f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = PinkPrimary,
                        trackColor = Color(0xFFF7D5E6)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "+${levelInfo.remainingXp} XP pour le prochain niveau",
                        color = PinkPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            AchievementCard(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                iconTint = Color(0xFFEC5A9A),
                title = "Session Starter",
                current = ui.totalSessions,
                target = 10,
                description = "Atteins 10 sessions enregistrees."
            )
        }

        item {
            AchievementCard(
                icon = Icons.Default.LocalFireDepartment,
                iconTint = Color(0xFFFF8C42),
                title = "Serie solide",
                current = ui.streakDays,
                target = 7,
                description = "Maintiens une serie de 7 jours."
            )
        }

        item {
            AchievementCard(
                icon = Icons.Default.Bolt,
                iconTint = Color(0xFFF3A638),
                title = "Reservoir a XP",
                current = ui.totalXp,
                target = 500,
                description = "Accumule 500 XP au total."
            )
        }

        item {
            AchievementCard(
                icon = Icons.Default.TaskAlt,
                iconTint = Color(0xFF49B96E),
                title = "Execution focus",
                current = ui.completedTasksCount,
                target = 25,
                description = "Complete 25 taches depuis FocusMe."
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Resume rapide",
                        color = TextDark,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    AchievementSummaryRow("Sessions totales", ui.totalSessions.toString())
                    AchievementSummaryRow("Meilleure session", formatDurationLong(ui.longestSessionSeconds))
                    AchievementSummaryRow("Matieres creees", ui.subjectsCount.toString())
                    AchievementSummaryRow("XP total", ui.totalXp.toString())
                }
            }
        }
    }
}

@Composable
private fun AchievementsHeader(onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = TextDark)
        }
        Spacer(Modifier.size(16.dp))
        Column {
            Text(
                text = "Succes",
                color = TextDark,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Tes niveaux et ta progression",
                color = PinkPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AchievementCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    current: Int,
    target: Int,
    description: String
) {
    val progress = (current / target.toFloat()).coerceIn(0f, 1f)

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(iconTint.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.size(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = TextDark, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                    Text(description, color = TextGray)
                }
            }
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress.coerceAtLeast(0.02f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = iconTint,
                trackColor = iconTint.copy(alpha = 0.14f)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "${current.coerceAtMost(target)}/$target",
                color = iconTint,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AchievementSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextGray)
        Text(value, color = TextDark, fontWeight = FontWeight.Bold)
    }
}
