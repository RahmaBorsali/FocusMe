package com.example.focusme.presentation.screen.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusme.data.repository.ChallengeRole
import com.example.focusme.data.repository.ChallengeParticipant
import com.example.focusme.presentation.ui.components.SoftCard
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextGray
import androidx.compose.ui.unit.dp

@Composable
fun ChallengeParticipantsScreen(
    id: String,
    myRole: String?,
    onBack: () -> Unit,
    vm: ChallengeParticipantsViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsState()
    val isOwner = myRole == "owner" || myRole == ChallengeRole.OWNER.name
    LaunchedEffect(id) { vm.load(id) }

    ChallengeScreenContainer(
        title = "Participants",
        subtitle = "Le groupe complet du defi, avec owner et date d'arrivee.",
        actions = { OutlinedButton(onClick = onBack) { Text("Retour") } }
    ) {
        when (val state = ui.state) {
            ContentState.Loading -> FullScreenLoading()
            is ContentState.Error -> ErrorCard(message = state.message, onRetry = { vm.load(id) })
            is ContentState.Success -> {
                if (state.data.isEmpty()) {
                    EmptyChallengesCard(title = "Personne ici pour l'instant", subtitle = "Invite des amis pour lancer le challenge.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                        items(state.data, key = { it.userId }) { participant ->
                            ParticipantRow(participant = participant, showRemove = isOwner && !participant.isOwner, onRemove = { vm.removeParticipant(id, participant.userId) })
                        }
                        if (ui.actionError != null) {
                            item { ErrorCard(message = ui.actionError!!) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantRow(
    participant: ChallengeParticipant,
    showRemove: Boolean,
    onRemove: () -> Unit
) {
    SoftCard(modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
            Row {
                Avatar(url = participant.avatarUrl, fallback = participant.username.take(1))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(participant.username, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        if (participant.isOwner) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(PinkPrimary.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Owner", color = PinkPrimary, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                            }
                        }
                        Text(
                            if (participant.isOwner) participant.joinedAt.toReadableDateTime().ifBlank { "Createur" }
                            else participant.joinedAt.toReadableDateTime().ifBlank { "Participant" },
                            color = TextGray
                        )
                    }
                }
            }
            if (showRemove) {
                OutlinedButton(onClick = onRemove) { Text("Exclure") }
            }
        }
    }
}
