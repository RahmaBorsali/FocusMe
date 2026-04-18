package com.example.focusme.presentation.screen.challenges

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusme.data.repository.ChallengeInvitation
import com.example.focusme.data.repository.ChallengeOverview
import com.example.focusme.data.repository.ChallengeVisibility
import com.example.focusme.data.repository.MembershipStatus
import com.example.focusme.presentation.ui.components.SoftCard
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextGray
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeDetailsScreen(
    id: String,
    onBack: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenParticipants: (String) -> Unit,
    onOpenChat: () -> Unit,
    onOpenJoinRequests: () -> Unit,
    onOpenJoinByCode: () -> Unit,
    vm: ChallengeDetailsViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsState()
    var showRequestDialog by remember { mutableStateOf(false) }
    var codeFeedback by remember { mutableStateOf<String?>(null) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LaunchedEffect(id) { vm.load(id) }

    ChallengeScreenContainer(
        title = "Detail du challenge",
        subtitle = "Progression, classement et discussion dans une vue plus simple.",
        actions = {
            OutlinedButton(onClick = onBack) { Text("Retour") }
            IconButton(onClick = { vm.load(id, refresh = true) }) { Icon(Icons.Default.Refresh, contentDescription = null) }
        }
    ) {
        when (val state = ui.overviewState) {
            ContentState.Loading -> FullScreenLoading()
            is ContentState.Error -> ErrorCard(message = state.message, onRetry = { vm.load(id) })
            is ContentState.Success -> ChallengeDetailsContent(
                overview = state.data,
                pendingInvitation = ui.pendingInvitation,
                actionMessage = ui.actionMessage,
                actionError = ui.actionError,
                onCancelPendingRequest = { vm.cancelPendingRequest(id) },
                onAcceptInvitation = { invitation -> vm.acceptPendingInvitation(id, invitation) },
                onRejectInvitation = { invitation -> vm.rejectPendingInvitation(id, invitation) },
                onRequestJoin = { showRequestDialog = true },
                onLeave = { vm.leave(id, onBack) },
                onOpenJoinByCode = onOpenJoinByCode,
                onOpenLeaderboard = onOpenLeaderboard,
                onOpenParticipants = { onOpenParticipants(state.data.myRole.name.lowercase()) },
                onOpenChat = onOpenChat,
                onOpenJoinRequests = onOpenJoinRequests,
                onCopyCode = { code ->
                    clipboardManager.setText(AnnotatedString(code))
                    codeFeedback = "Code copie dans le presse-papiers."
                },
                onShareCode = { code, title ->
                    val text = "Rejoins mon challenge Focus Me \"$title\" avec ce code: $code"
                    val intent = Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        },
                        null
                    )
                    context.startActivity(intent)
                    codeFeedback = "Ouverture du partage..."
                },
                codeFeedback = codeFeedback,
                loadingJoin = ui.isJoining,
                loadingInvitationResponse = ui.isRespondingToInvitation,
                loadingCancelRequest = ui.isCancellingRequest,
                loadingLeave = ui.isLeaving
            )
        }
    }

    if (showRequestDialog) {
        AlertDialog(
            onDismissRequest = { showRequestDialog = false },
            title = { Text("Envoyer une demande ?") },
            text = { Text("Le createur devra ensuite accepter ou refuser ta demande d'acces a ce defi.") },
            confirmButton = {
                PrimaryChallengeButton(text = "Envoyer la demande") {
                    vm.requestAccess(id)
                    showRequestDialog = false
                }
            },
            dismissButton = { OutlinedButton(onClick = { showRequestDialog = false }) { Text("Annuler") } }
        )
    }
}

@Composable
private fun ChallengeDetailsContent(
    overview: ChallengeOverview,
    pendingInvitation: ChallengeInvitation?,
    actionMessage: String?,
    actionError: String?,
    onCancelPendingRequest: () -> Unit,
    onAcceptInvitation: (ChallengeInvitation) -> Unit,
    onRejectInvitation: (ChallengeInvitation) -> Unit,
    onRequestJoin: () -> Unit,
    onLeave: () -> Unit,
    onOpenJoinByCode: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenParticipants: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenJoinRequests: () -> Unit,
    onCopyCode: (String) -> Unit,
    onShareCode: (String, String) -> Unit,
    codeFeedback: String?,
    loadingJoin: Boolean,
    loadingInvitationResponse: Boolean,
    loadingCancelRequest: Boolean,
    loadingLeave: Boolean
) {
    val membershipStatus = overview.challenge.membershipStatus
    val hasPendingInvitation =
        membershipStatus == MembershipStatus.NOT_JOINED && pendingInvitation != null
    val canAccessMemberExperience =
        membershipStatus == MembershipStatus.OWNER || membershipStatus == MembershipStatus.JOINED

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxSize()) {
        item {
            ChallengeCard(
                challenge = overview.challenge,
                ctaLabel = when (membershipStatus) {
                    MembershipStatus.OWNER -> "Tu es owner"
                    MembershipStatus.JOINED -> if (loadingLeave) "Sortie..." else "Quitter"
                    MembershipStatus.PENDING_REQUEST -> if (loadingCancelRequest) "Annulation..." else "Annuler la demande"
                    MembershipStatus.NOT_JOINED -> when {
                        hasPendingInvitation -> if (loadingInvitationResponse) "Traitement..." else "Accepter l'invitation"
                        overview.challenge.visibility == ChallengeVisibility.PRIVATE -> "J'ai un code"
                        loadingJoin -> "Envoi..."
                        else -> "Demander l'acces"
                    }
                },
                membershipLabel = when (membershipStatus) {
                    MembershipStatus.OWNER -> "Owner"
                    MembershipStatus.JOINED -> "Participant"
                    MembershipStatus.PENDING_REQUEST -> "En attente"
                    MembershipStatus.NOT_JOINED -> if (hasPendingInvitation) "Invitation recue" else null
                },
                membershipTint = when (membershipStatus) {
                    MembershipStatus.JOINED -> androidx.compose.ui.graphics.Color(0xFF299764)
                    else -> PinkPrimary
                },
                membershipBackground = when (membershipStatus) {
                    MembershipStatus.JOINED -> androidx.compose.ui.graphics.Color(0xFFEAFBF2)
                    else -> PinkPrimary.copy(alpha = 0.10f)
                },
                onPrimaryActionClick = when (membershipStatus) {
                    MembershipStatus.OWNER -> null
                    MembershipStatus.JOINED -> { { onLeave() } }
                    MembershipStatus.PENDING_REQUEST -> { { onCancelPendingRequest() } }
                    MembershipStatus.NOT_JOINED -> {
                        {
                            when {
                                hasPendingInvitation -> onAcceptInvitation(pendingInvitation!!)
                                overview.challenge.visibility == ChallengeVisibility.PRIVATE -> onOpenJoinByCode()
                                else -> onRequestJoin()
                            }
                        }
                    }
                },
                primaryActionEnabled = membershipStatus != MembershipStatus.OWNER &&
                    !loadingJoin &&
                    !loadingInvitationResponse &&
                    !loadingLeave &&
                    !loadingCancelRequest,
                secondaryLabel = when (membershipStatus) {
                    MembershipStatus.OWNER,
                    MembershipStatus.JOINED -> "Classement"
                    MembershipStatus.PENDING_REQUEST -> "J'ai un code"
                    MembershipStatus.NOT_JOINED -> if (hasPendingInvitation) {
                        "Refuser"
                    } else if (overview.challenge.visibility == ChallengeVisibility.PRIVATE) {
                        null
                    } else {
                        "J'ai un code"
                    }
                },
                onClick = {},
                onSecondaryClick = when (membershipStatus) {
                    MembershipStatus.OWNER,
                    MembershipStatus.JOINED -> onOpenLeaderboard
                    MembershipStatus.PENDING_REQUEST -> onOpenJoinByCode
                    MembershipStatus.NOT_JOINED -> if (hasPendingInvitation) {
                        { onRejectInvitation(pendingInvitation!!) }
                    } else if (overview.challenge.visibility == ChallengeVisibility.PRIVATE) {
                        null
                    } else {
                        onOpenJoinByCode
                    }
                },
                emphasize = true
            )
        }
        if (!actionMessage.isNullOrBlank()) {
            item {
                StateInfoCard(
                    title = "Action prise en compte",
                    message = actionMessage
                )
            }
        }
        if (membershipStatus == MembershipStatus.PENDING_REQUEST) {
            item {
                StateInfoCard(
                    title = "Validation en attente",
                    message = "Ta ${overview.challenge.myJoinRequestType.requestLabel().lowercase()} a bien ete envoyee. Le proprietaire du challenge doit maintenant l'accepter ou la refuser, ou tu peux encore l'annuler."
                )
            }
        }
        if (hasPendingInvitation) {
            item {
                StateInfoCard(
                    title = "Invitation recue",
                    message = "${pendingInvitation?.inviterName?.ifBlank { "Un ami" } ?: "Un ami"} t'a invite a rejoindre ce challenge. Tu peux accepter l'invitation directement ici ou la refuser."
                )
            }
        }
        if (
            membershipStatus == MembershipStatus.NOT_JOINED &&
            !hasPendingInvitation &&
            overview.challenge.visibility != ChallengeVisibility.PRIVATE
        ) {
            item {
                StateInfoCard(
                    title = "Comment rejoindre ce defi",
                    message = "Sans code, tu envoies une demande au createur. Avec le code du challenge, tu rejoins directement."
                )
            }
        }
        if (canAccessMemberExperience) {
            item { ProgressCard(overview) }
            item { QuickAccessRow(onOpenParticipants, onOpenChat) }
        } else {
            item {
                StateInfoCard(
                    title = "Acces membre verrouille",
                    message = "Le chat, les participants et le classement complet apparaissent ici une fois le challenge rejoint."
                )
            }
        }
        if (membershipStatus == MembershipStatus.OWNER && overview.challenge.visibility == ChallengeVisibility.FRIENDS) {
            item {
                OwnerJoinRequestsCard(
                    pendingCount = overview.pendingJoinRequestsCount,
                    onOpenJoinRequests = onOpenJoinRequests
                )
            }
        }
        if (membershipStatus == MembershipStatus.OWNER && !overview.challenge.joinCode.isNullOrBlank()) {
            item {
                val joinCode = overview.challenge.joinCode.orEmpty()
                ChallengeCodeCard(
                    code = joinCode,
                    feedback = codeFeedback,
                    onCopy = { onCopyCode(joinCode) },
                    onShare = { onShareCode(joinCode, overview.challenge.title) }
                )
            }
        }
        if (canAccessMemberExperience) {
            item { LeaderboardPreviewCard(entries = overview.leaderboardPreview, onSeeAll = onOpenLeaderboard) }
            item { ParticipantsPreviewCard(onOpenParticipants) }
            item { RecentMessagesCard(overview, onOpenChat) }
        }
        if (actionError != null) {
            item { ErrorCard(message = actionError) }
        }
    }
}

@Composable
private fun OwnerJoinRequestsCard(
    pendingCount: Int,
    onOpenJoinRequests: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            SectionTitle(title = "Demandes de participation", action = "Gerer", onAction = onOpenJoinRequests)
            Spacer(Modifier.height(6.dp))
            Text(
                if (pendingCount > 0) {
                    "$pendingCount demande(s) attendent une reponse. Accepte ou refuse les acces a ce challenge."
                } else {
                    "Aucune demande en attente pour le moment. Les demandes sans code apparaitront ici."
                },
                color = TextGray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ProgressCard(overview: ChallengeOverview) {
    val myEntry = overview.myEntry
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            SectionTitle(title = "Ta progression")
            Spacer(Modifier.height(10.dp))
            if (myEntry == null) {
                Text("Rejoins ce challenge pour suivre ta progression ici.", color = TextGray)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricTile("Score", "${myEntry.score}", Modifier.weight(1f))
                    MetricTile("Progression", "${myEntry.progress}%", Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricTile("Minutes", "${myEntry.focusMinutes}", Modifier.weight(1f))
                    MetricTile("Serie", "${myEntry.streak}j", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    SoftCard(modifier = modifier, padding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
        Column {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
            Spacer(Modifier.height(2.dp))
            Text(label, color = TextGray, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun QuickAccessRow(
    onOpenParticipants: () -> Unit,
    onOpenChat: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        SmallActionCard(Icons.Default.Groups, "Participants", "Voir tout", onOpenParticipants, Modifier.weight(1f))
        SmallActionCard(Icons.Default.ChatBubbleOutline, "Chat", "Messages", onOpenChat, Modifier.weight(1f))
    }
}

@Composable
private fun SmallActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SoftCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, color = TextGray, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Ouvrir",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() }
            )
        }
    }
}

@Composable
private fun ParticipantsPreviewCard(onOpenParticipants: () -> Unit) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            SectionTitle(title = "Participants", action = "Voir la liste", onAction = onOpenParticipants)
            Spacer(Modifier.height(6.dp))
            Text("Retrouve tous les membres, le badge owner et la date d'arrivee de chacun.", color = TextGray, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun RecentMessagesCard(overview: ChallengeOverview, onOpenChat: () -> Unit) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            SectionTitle(title = "Messages recents", action = "Ouvrir le chat", onAction = onOpenChat)
            Spacer(Modifier.height(8.dp))
            if (overview.recentMessages.isEmpty()) {
                Text("Personne n'a encore lance la discussion. Sois la premiere personne a encourager le groupe.", color = TextGray)
            } else {
                overview.recentMessages.take(3).forEach { message ->
                    Row {
                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(message.username, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(message.text ?: if (message.attachment != null) "[Pièce jointe]" else "", color = TextGray, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
