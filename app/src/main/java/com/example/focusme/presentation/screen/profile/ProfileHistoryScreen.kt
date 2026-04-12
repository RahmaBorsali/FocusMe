package com.example.focusme.presentation.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusme.data.local.StudySessionEntity
import com.example.focusme.presentation.ui.theme.AppBg
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextDark
import com.example.focusme.presentation.ui.theme.TextGray

@Composable
fun ProfileHistoryScreen(
    onBack: () -> Unit,
    vm: ProfileViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsState()
    var filter by rememberSaveable { mutableStateOf(ProfileHistoryFilter.ALL) }
    var deleteTarget by remember { mutableStateOf<StudySessionEntity?>(null) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    val filteredSessions = remember(ui.sessions, filter) { filterSessions(ui.sessions, filter) }
    val groupedSessions = remember(filteredSessions) { groupSessionsByDate(filteredSessions) }
    val totalSeconds = filteredSessions.sumOf { it.durationSeconds }
    val averageSeconds = if (filteredSessions.isNotEmpty()) totalSeconds / filteredSessions.size else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ProfileDetailHeader(
                title = "Historique des sessions",
                subtitle = "Suis ta progression",
                onBack = onBack
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HistoryStatCard("Sessions", filteredSessions.size.toString(), Icons.AutoMirrored.Filled.MenuBook, Modifier.weight(1f))
                HistoryStatCard("Temps total", formatDurationCompact(totalSeconds), Icons.Default.Timer, Modifier.weight(1f))
                HistoryStatCard("Temps moyen", formatDurationCompact(averageSeconds), Icons.Default.Timer, Modifier.weight(1f))
            }
        }

        item {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HistoryFilterChip("Tous", filter == ProfileHistoryFilter.ALL) { filter = ProfileHistoryFilter.ALL }
                HistoryFilterChip("Aujourd'hui", filter == ProfileHistoryFilter.TODAY) { filter = ProfileHistoryFilter.TODAY }
                HistoryFilterChip("Cette semaine", filter == ProfileHistoryFilter.THIS_WEEK) { filter = ProfileHistoryFilter.THIS_WEEK }
                HistoryFilterChip("Ce mois", filter == ProfileHistoryFilter.THIS_MONTH) { filter = ProfileHistoryFilter.THIS_MONTH }
            }
        }

        if (groupedSessions.isEmpty()) {
            item {
                HistoryEmptyState()
            }
        } else {
            item {
                TextButton(
                    onClick = { showClearHistoryDialog = true },
                    enabled = !ui.isClearingHistory,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (ui.isClearingHistory) "Suppression..." else "Effacer l'historique filtre",
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            groupedSessions.forEach { group ->
                item {
                    HistoryDayHeader(group)
                }
                items(group.sessions, key = { it.id }) { session ->
                    HistorySessionCard(
                        session = session,
                        onDelete = { deleteTarget = session }
                    )
                }
            }
        }
    }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Supprimer cette session ?", fontWeight = FontWeight.ExtraBold) },
            text = { Text("Elle sera retiree de l'historique local.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteSession(deleteTarget!!.id)
                        deleteTarget = null
                    }
                ) {
                    Text("Supprimer", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Annuler", color = TextDark)
                }
            }
        )
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Effacer l'historique ?", fontWeight = FontWeight.ExtraBold) },
            text = { Text("Toutes les sessions enregistrees localement seront supprimees.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.clearHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Tout effacer", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Annuler", color = TextDark)
                }
            }
        )
    }
}

@Composable
private fun ProfileDetailHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
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
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                color = TextDark,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = subtitle,
                color = PinkPrimary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun HistoryStatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        tonalElevation = 2.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = TextDark, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = label, tint = TextGray, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(label, color = TextGray, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun HistoryFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) PinkPrimary else Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else TextDark,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun HistoryDayHeader(group: ProfileHistoryGroup) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatHistoryDate(group.date),
                color = TextDark,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFFFF0F6))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "${formatDurationCompact(group.totalSeconds)} • ${group.sessions.size} sessions",
                    color = PinkPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun HistorySessionCard(
    session: StudySessionEntity,
    onDelete: () -> Unit
) {
    val endTime = formatSessionClock(session.createdAtMillis)
    val startTime = formatSessionClock(session.createdAtMillis - session.durationSeconds * 1000L)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFFBE7F1)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = PinkPrimary, modifier = Modifier.size(30.dp))
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    color = TextDark,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$startTime - $endTime",
                    color = TextGray,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Concentration ${formatDurationCompact(session.durationSeconds)}",
                    color = PinkPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Focus ${starText(session.focusRate)}   •   Satisfaction ${starText(session.satisfactionRate)}",
                    color = TextGray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Supprimer", tint = Color(0xFFE53935))
            }
        }
    }
}

@Composable
private fun HistoryEmptyState() {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Aucune session sur cette periode",
                color = TextDark,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Continue a enregistrer tes sessions depuis le minuteur pour enrichir l'historique.",
                color = TextGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun starText(value: Int): String =
    if (value <= 0) "--" else buildString { repeat(value) { append("★") } }
