package com.example.focusme.presentation.screen.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusme.data.repository.JoinRequestStatus
import com.example.focusme.data.repository.OutgoingJoinRequest
import com.example.focusme.presentation.ui.components.SoftCard
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextGray

@Composable
fun MyJoinRequestsScreen(
    onBack: () -> Unit,
    onOpenChallenge: (String) -> Unit,
    vm: MyJoinRequestsViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    ChallengeScreenContainer(
        title = "Mes demandes",
        subtitle = "Suis les challenges entre amis ou tu attends encore la validation du proprietaire.",
        actions = {
            OutlinedButton(onClick = onBack) { Text("Retour") }
            IconButton(onClick = { vm.load(refresh = true) }) {
                Icon(Icons.Default.Refresh, contentDescription = null)
            }
        }
    ) {
        when (val state = ui.state) {
            ContentState.Loading -> FullScreenLoading()
            is ContentState.Error -> ErrorCard(message = state.message, onRetry = { vm.load() })
            is ContentState.Success -> {
                if (state.data.isEmpty()) {
                    EmptyChallengesCard(
                        title = "Aucune demande envoyee",
                        subtitle = "Quand tu demandes l'acces a un challenge entre amis sans code, il apparait ici."
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (!ui.actionError.isNullOrBlank()) {
                            ErrorCard(message = ui.actionError!!)
                            Spacer(Modifier.height(12.dp))
                        }
                        if (!ui.actionMessage.isNullOrBlank()) {
                            StateInfoCard(
                                title = "Action prise en compte",
                                message = ui.actionMessage!!
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.data, key = { it.id }) { request ->
                                MyJoinRequestCard(
                                    request = request,
                                    isProcessing = ui.processingChallengeIds.contains(request.challenge.id),
                                    onOpenChallenge = { onOpenChallenge(request.challenge.id) },
                                    onCancel = { vm.cancel(request) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MyJoinRequestCard(
    request: OutgoingJoinRequest,
    isProcessing: Boolean,
    onOpenChallenge: () -> Unit,
    onCancel: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    request.challenge.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Owner: ${request.ownerUsername}",
                    color = TextGray,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (request.challenge.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        request.challenge.description,
                        color = TextGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    request.createdAt.toReadableDateTime().ifBlank { "Demande recente" },
                    color = TextGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            StatusChip(status = request.status)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onOpenChallenge,
                modifier = Modifier.weight(1f)
            ) {
                Text("Voir")
            }
            if (request.status == JoinRequestStatus.PENDING) {
                PrimaryChallengeButton(
                    text = if (isProcessing) "Annulation..." else "Annuler",
                    enabled = !isProcessing,
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                )
            } else {
                PrimaryChallengeButton(
                    text = "Ouvrir",
                    onClick = onOpenChallenge,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: JoinRequestStatus) {
    val (label, bg) = when (status) {
        JoinRequestStatus.PENDING -> "Pending approval" to PinkPrimary.copy(alpha = 0.10f)
        JoinRequestStatus.ACCEPTED -> "Accepted" to androidx.compose.ui.graphics.Color(0xFFEAFBF2)
        JoinRequestStatus.REJECTED -> "Rejected" to androidx.compose.ui.graphics.Color(0xFFFFF0F2)
    }
    val tint = when (status) {
        JoinRequestStatus.PENDING -> PinkPrimary
        JoinRequestStatus.ACCEPTED -> androidx.compose.ui.graphics.Color(0xFF299764)
        JoinRequestStatus.REJECTED -> androidx.compose.ui.graphics.Color(0xFFCC4B6C)
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.HourglassTop,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
        Text(
            label,
            color = tint,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}
