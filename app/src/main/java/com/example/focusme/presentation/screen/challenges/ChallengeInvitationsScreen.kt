package com.example.focusme.presentation.screen.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusme.presentation.model.UserUi
import com.example.focusme.presentation.ui.components.SoftCard
import com.example.focusme.presentation.ui.theme.BorderSoft
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextGray

@Composable
fun ChallengeInvitationsScreen(
    challengeId: String,
    onBack: () -> Unit,
    vm: ChallengeInvitationsViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsState()
    LaunchedEffect(challengeId) { vm.loadManage(challengeId) }

    ChallengeScreenContainer(
        title = "Invitations",
        subtitle = "Invite un ami et suis les invitations deja envoyees.",
        actions = { OutlinedButton(onClick = onBack) { Text("Retour") } }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CodeHelpCard()
            Spacer(Modifier.height(12.dp))
            InviteComposer(
                value = ui.inviteeUserId,
                selectedFriend = ui.selectedFriend,
                isInviting = ui.isInviting,
                error = ui.actionError,
                onValueChange = vm::updateInviteeUserId,
                onInvite = { vm.invite(challengeId) }
            )
            Spacer(Modifier.height(14.dp))
            FriendSuggestionsCard(
                friends = ui.friends,
                query = ui.inviteeUserId,
                selectedFriend = ui.selectedFriend,
                onSelectFriend = vm::selectFriend
            )
            Spacer(Modifier.height(14.dp))
            if (!ui.actionMessage.isNullOrBlank()) {
                StateInfoCard(
                    title = "Invitation envoyee",
                    message = ui.actionMessage!!
                )
                Spacer(Modifier.height(14.dp))
            }

            when (val state = ui.manageState) {
                ContentState.Loading -> FullScreenLoading()
                is ContentState.Error -> ErrorCard(message = state.message, onRetry = { vm.loadManage(challengeId) })
                is ContentState.Success -> {
                    if (state.data.isEmpty()) {
                        EmptyChallengesCard(
                            title = "Aucune invitation envoyee",
                            subtitle = "Ajoute l'identifiant d'un ami pour lui proposer ce challenge."
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.data, key = { it.id }) { invitation ->
                                SoftCard(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        invitation.challengeTitle(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "Invite envoyee a ${invitation.inviteeName.ifBlank { invitation.inviteeId ?: "cet ami" }}",
                                        color = TextGray,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    StatusPill(
                                        text = when (invitation.status.lowercase()) {
                                            "accepted" -> "Acceptee"
                                            "rejected" -> "Refusee"
                                            else -> "En attente"
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeHelpCard() {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(PinkPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Key, contentDescription = null, tint = PinkPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Code et invitations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Le code rejoint directement un challenge. Ici, tu geres seulement les invitations classiques envoyees par le owner a un ami precis.",
                    color = TextGray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun InviteComposer(
    value: String,
    selectedFriend: UserUi?,
    isInviting: Boolean,
    error: String?,
    onValueChange: (String) -> Unit,
    onInvite: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF1F7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PersonAddAlt1, contentDescription = null, tint = PinkPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Inviter un ami",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Envoie une invitation classique. Ton ami pourra ensuite l'accepter ou la refuser.",
                    color = TextGray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Ami a inviter") },
            placeholder = { Text("Tape son pseudo ou choisis-le juste en dessous") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(Modifier.height(10.dp))

        PrimaryChallengeButton(
            text = if (isInviting) "Invitation..." else "Inviter cet ami",
            enabled = !isInviting,
            onClick = onInvite,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFFFFAFC))
                .border(1.dp, BorderSoft, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = error ?: if (selectedFriend != null) {
                    "Invitation prete pour ${selectedFriend.name}. L'app enverra le vrai identifiant backend de cet ami."
                } else {
                    "Choisis un ami de ta liste pour que l'app envoie le bon identifiant backend."
                },
                color = if (error != null) MaterialTheme.colorScheme.error else TextGray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun FriendSuggestionsCard(
    friends: List<UserUi>,
    query: String,
    selectedFriend: UserUi?,
    onSelectFriend: (UserUi) -> Unit
) {
    val normalizedQuery = query.trim().lowercase()
    val visibleFriends = if (normalizedQuery.isBlank()) {
        friends.take(6)
    } else {
        friends.filter { friend ->
            friend.name.lowercase().contains(normalizedQuery) ||
                friend.username.lowercase().contains(normalizedQuery) ||
                friend.id.lowercase().contains(normalizedQuery)
        }.take(6)
    }

    if (visibleFriends.isEmpty()) return

    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = if (selectedFriend != null) "Ami selectionne" else "Choisir un ami",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(10.dp))
            visibleFriends.forEach { friend ->
                val isSelected = selectedFriend?.id == friend.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) Color(0xFFFFF1F7) else Color.White)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) PinkPrimary.copy(alpha = 0.35f) else BorderSoft,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelectFriend(friend) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Avatar(url = null, fallback = friend.name.take(1))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(friend.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("@${friend.username}", color = TextGray, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Text(
                        text = if (isSelected) "Selectionne" else "Inviter",
                        color = PinkPrimary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun StatusPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(PinkPrimary.copy(alpha = 0.10f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = PinkPrimary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}
