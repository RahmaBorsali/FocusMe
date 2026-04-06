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
import androidx.compose.material.icons.filled.PersonAddAlt1
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
import com.example.focusme.data.repository.IncomingJoinRequest
import com.example.focusme.presentation.ui.components.SoftCard
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextGray

@Composable
fun IncomingJoinRequestsScreen(
    onBack: () -> Unit,
    onOpenChallenge: (String) -> Unit,
    vm: IncomingJoinRequestsViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    ChallengeScreenContainer(
        title = "Demandes recues",
        subtitle = "Accepte ou refuse rapidement les demandes de participation envoyees a tes challenges.",
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
                        title = "Aucune demande recue",
                        subtitle = "Quand quelqu'un demandera l'acces a l'un de tes challenges entre amis, tu le verras ici."
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (!ui.actionError.isNullOrBlank()) {
                            ErrorCard(message = ui.actionError!!)
                            Spacer(Modifier.height(12.dp))
                        }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.data, key = { it.id }) { request ->
                                IncomingJoinRequestCard(
                                    request = request,
                                    isProcessing = ui.processingRequestIds.contains(request.id),
                                    onOpenChallenge = { onOpenChallenge(request.challenge.id) },
                                    onAccept = { vm.accept(request) },
                                    onReject = { vm.reject(request) }
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
private fun IncomingJoinRequestCard(
    request: IncomingJoinRequest,
    isProcessing: Boolean,
    onOpenChallenge: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(url = request.avatarUrl, fallback = request.username.take(1))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    request.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Veut rejoindre ${request.challenge.title}",
                    color = TextGray,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    request.createdAt.toReadableDateTime().ifBlank { "Demande recente" },
                    color = TextGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(PinkPrimary.copy(alpha = 0.10f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAddAlt1,
                    contentDescription = null,
                    tint = PinkPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "Pending",
                    color = PinkPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onOpenChallenge, modifier = Modifier.fillMaxWidth()) {
            Text("Voir le challenge")
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onReject,
                enabled = !isProcessing,
                modifier = Modifier.weight(1f)
            ) {
                Text("Refuser")
            }
            PrimaryChallengeButton(
                text = if (isProcessing) "Traitement..." else "Accepter",
                enabled = !isProcessing,
                onClick = onAccept,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
