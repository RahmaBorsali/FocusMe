package com.example.focusme.presentation.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusme.presentation.ui.theme.AppBg
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextDark
import com.example.focusme.presentation.ui.theme.TextGray
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime

@Composable
fun ProfileStatsScreen(
    onBack: () -> Unit,
    vm: ProfileViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsState()
    val weeklySummary = remember(ui.sessions) { computeWeeklySummary(ui.sessions) }
    val heatmap = remember(ui.sessions) { buildHeatmap(ui.sessions) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StatsHeader(onBack = onBack)
        }

        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Vue d'ensemble",
                        color = TextDark,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatsMetricCard("Total focus", formatDurationLong(ui.totalFocusSeconds), Icons.Default.Timer, Modifier.weight(1f))
                        StatsMetricCard("Moyenne", formatDurationLong(ui.averageSessionSeconds), Icons.Default.AutoGraph, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatsMetricCard("Serie", "${ui.streakDays} jours", Icons.Default.LocalFireDepartment, Modifier.weight(1f))
                        StatsMetricCard("XP", ui.totalXp.toString(), Icons.Default.Bolt, Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Cette semaine",
                        color = TextDark,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = formatWeekRange(weeklySummary),
                        color = TextGray
                    )
                    Spacer(Modifier.height(18.dp))
                    StatsWeekBars(weeklySummary)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = if (weeklySummary.hasActivity) {
                            "Pic de la semaine: ${formatDurationLong(weeklySummary.bestDaySeconds)}"
                        } else {
                            "Aucune activite detectee cette semaine."
                        },
                        color = PinkPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Heatmap des 28 derniers jours",
                        color = TextDark,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(14.dp))
                    heatmap.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { seconds ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(26.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(heatColor(seconds, heatmap.flatten().maxOrNull() ?: 0))
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Plus la case est coloree, plus tu as focus ce jour-la.",
                        color = TextGray
                    )
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Indicateurs qualitatifs",
                        color = TextDark,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    StatsInsightRow("Focus moyen", formatStarsLabel(ui.averageFocusRate))
                    StatsInsightRow("Satisfaction moyenne", formatStarsLabel(ui.averageSatisfactionRate))
                    StatsInsightRow("Meilleur focus", formatStarsLabel(ui.bestFocusRate))
                    StatsInsightRow("Taches completees", ui.completedTasksCount.toString())
                    StatsInsightRow("Matieres creees", ui.subjectsCount.toString())
                }
            }
        }
    }
}

@Composable
private fun StatsHeader(onBack: () -> Unit) {
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
        androidx.compose.foundation.layout.Spacer(Modifier.size(16.dp))
        Column {
            Text(
                text = "Statistiques avancees",
                color = TextDark,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Heatmap d'activite et insights",
                color = PinkPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StatsMetricCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFFFF7FB))
            .padding(16.dp)
    ) {
        Column {
            Icon(icon, contentDescription = label, tint = PinkPrimary)
            Spacer(Modifier.height(14.dp))
            Text(value, color = TextDark, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(label, color = TextGray)
        }
    }
}

@Composable
private fun StatsWeekBars(summary: ProfileWeeklySummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        summary.days.forEach { day ->
            val ratio = if (summary.bestDaySeconds == 0) 0f else day.totalSeconds / summary.bestDaySeconds.toFloat()
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height((30 + ratio * 84).dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (day.totalSeconds > 0) {
                                Brush.verticalGradient(listOf(Color(0xFFF59BC0), PinkPrimary))
                            } else {
                                Brush.verticalGradient(listOf(Color(0xFFF1F3F7), Color(0xFFE7EAF0)))
                            }
                        )
                )
                Spacer(Modifier.height(8.dp))
                Text(day.label, color = TextGray, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun StatsInsightRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextGray)
        Text(value, color = TextDark, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
    }
}

private fun buildHeatmap(sessions: List<com.example.focusme.data.local.StudySessionEntity>): List<List<Int>> {
    val today = kotlinx.datetime.Clock.System.now()
        .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        .date
    val dates = (27 downTo 0).map { offset -> LocalDate.fromEpochDays(today.toEpochDays() - offset) }
    val values = dates.map { date ->
        sessions.filter { sessionLocalDate(it) == date }.sumOf { it.durationSeconds }
    }
    return values.chunked(7)
}

private fun heatColor(value: Int, maxValue: Int): Color {
    if (maxValue <= 0 || value <= 0) return Color(0xFFF0F2F7)
    val ratio = value / maxValue.toFloat()
    return when {
        ratio < 0.25f -> Color(0xFFFAD4E6)
        ratio < 0.5f -> Color(0xFFF7A9CE)
        ratio < 0.75f -> Color(0xFFF06AAE)
        else -> PinkPrimary
    }
}
