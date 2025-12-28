package com.example.focusme.presentation.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusme.data.local.StudySessionEntity
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextDark
import com.example.focusme.presentation.ui.theme.TextGray

@Composable
fun ProfileScreen(vm: ProfileViewModel = viewModel()) {

    val sessions by vm.sessions.collectAsState()
    var deleteTarget by remember { mutableStateOf<StudySessionEntity?>(null) }

    val bg = Color(0xFFFBEAF2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(18.dp)
    ) {
        Text(
            text = "Profil",
            color = TextDark,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Historique de tes sessions",
            color = TextGray,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(Modifier.height(16.dp))

        if (sessions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF9AA0A6), modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(10.dp))
                    Text("Aucune session", color = TextDark, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Termine une session dans le minuteur\npour la voir ici.", color = TextGray, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(sessions, key = { it.id }) { s ->
                    SessionCard(
                        session = s,
                        onDelete = { deleteTarget = s }
                    )
                }
            }
        }
    }

    // ✅ Confirm delete dialog
    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Supprimer la session ?") },
            text = { Text("Cette action est définitive.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteSession(deleteTarget!!.id)
                        deleteTarget = null
                    }
                ) {
                    Text("Supprimer", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun SessionCard(
    session: StudySessionEntity,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(PinkPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) { Text("⏱️") }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(session.title, color = TextDark, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(
                "${formatDuration(session.durationSeconds)} • XP ${session.xpPoints} • Tasks ${session.tasksCount}",
                color = TextGray,
                style = MaterialTheme.typography.bodySmall
            )
        }

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F))
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val m = (seconds / 60).coerceAtLeast(0)
    val s = (seconds % 60).coerceAtLeast(0)
    return "%d:%02d".format(m, s)
}
