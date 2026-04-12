package com.example.focusme.presentation.screen.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusme.data.repository.Challenge
import com.example.focusme.data.repository.ChallengeInvitation
import com.example.focusme.data.repository.ChallengeRole
import com.example.focusme.data.repository.IncomingJoinRequest
import com.example.focusme.data.repository.JoinRequestStatus
import com.example.focusme.data.repository.MembershipStatus
import com.example.focusme.data.repository.OutgoingJoinRequest
import com.example.focusme.presentation.ui.components.SoftCard
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextGray
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen(
    onGoCreate: () -> Unit,
    onOpenIncomingRequests: () -> Unit,
    onOpenMyRequests: () -> Unit,
    onOpenDetails: (String) -> Unit,
    vm: ChallengesHomeViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsState()
    var pendingAccessChallenge by remember { mutableStateOf<Challenge?>(null) }

    LaunchedEffect(Unit) {
        if (!ui.isLoading) {
            vm.refreshAll(isPullRefresh = true)
        }
    }

    ChallengeScreenContainer(
        title = "Defis",
        subtitle = "Rejoins un defi, suis ta progression et garde le rythme avec tes amis.",
        actions = {
            IconButton(onClick = { vm.refreshAll(isPullRefresh = true) }) {
                Icon(Icons.Default.Refresh, contentDescription = null)
            }
            IconButton(onClick = onGoCreate) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) {
        ChallengeSegmentedTabs(
            selected = ui.selectedTab.title,
            options = ChallengesHomeTab.entries.map { it.title },
            onSelect = { label ->
                vm.selectTab(ChallengesHomeTab.entries.first { it.title == label })
            }
        )
        Spacer(Modifier.height(16.dp))
        if (ui.isLoading) {
            FullScreenLoading()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    JoinByCodeEntryCard(
                        code = ui.joinCodeInput,
                        errorMessage = ui.joinCodeError,
                        isJoining = ui.isJoiningByCode,
                        onCodeChange = vm::updateJoinCodeInput,
                        onJoinClick = { vm.joinChallengeByCode(onOpenDetails) }
                    )
                }
                if (!ui.friendActionMessage.isNullOrBlank()) {
                    item {
                        StateInfoCard(
                            title = "Action prise en compte",
                            message = ui.friendActionMessage!!
                        )
                    }
                }
                item {
                    SectionTitle(
                        title = when (ui.selectedTab) {
                            ChallengesHomeTab.MINE -> "Mes challenges"
                            ChallengesHomeTab.FRIENDS -> "Challenges d'amis"
                            ChallengesHomeTab.INVITATIONS -> "Invitations"
                        }
                    )
                }
                when (ui.selectedTab) {
                    ChallengesHomeTab.MINE -> myChallengesSection(ui.myChallenges, onOpenDetails, onGoCreate)
                    ChallengesHomeTab.FRIENDS -> friendsSection(
                        challenges = ui.friendChallenges,
                        incomingInvitations = ui.incomingInvitations,
                        outgoingJoinRequests = ui.outgoingJoinRequests,
                        processingChallengeIds = ui.processingFriendChallengeIds,
                        processingInvitationIds = ui.processingInvitationIds,
                        onRequestAccess = { pendingAccessChallenge = it },
                        onPrimaryAction = vm::handleFriendChallengePrimaryAction,
                        onAcceptInvitation = { invitation -> vm.acceptInvitation(invitation, onOpenDetails) },
                        onRejectInvitation = vm::rejectInvitation,
                        onOpen = onOpenDetails
                    )
                    ChallengesHomeTab.INVITATIONS -> invitationsSection(
                        incomingJoinRequests = ui.incomingJoinRequests,
                        outgoingJoinRequests = ui.outgoingJoinRequests,
                        incomingJoinRequestsCount = ui.incomingJoinRequests.count { it.status == JoinRequestStatus.PENDING },
                        outgoingJoinRequestsCount = ui.outgoingJoinRequests.count { it.status == JoinRequestStatus.PENDING },
                        processingIncomingJoinRequestIds = ui.processingIncomingJoinRequestIds,
                        processingOutgoingJoinRequestChallengeIds = ui.processingOutgoingJoinRequestChallengeIds,
                        onOpenIncomingRequests = onOpenIncomingRequests,
                        onOpenMyRequests = onOpenMyRequests,
                        incoming = ui.incomingInvitations,
                        outgoing = ui.outgoingInvitations,
                        onOpen = onOpenDetails,
                        onAcceptJoinRequest = vm::acceptIncomingJoinRequest,
                        onRejectJoinRequest = vm::rejectIncomingJoinRequest,
                        onCancelOutgoingJoinRequest = vm::cancelOutgoingJoinRequest,
                        onAccept = { vm.acceptInvitation(it, onOpenDetails) },
                        onReject = vm::rejectInvitation
                    )
                }
            }
        }
    }

    pendingAccessChallenge?.let { challenge ->
        AlertDialog(
            onDismissRequest = { pendingAccessChallenge = null },
            title = { Text("Envoyer une demande ?") },
            text = { Text("Le createur devra ensuite accepter ou refuser ta demande d'acces a ce defi.") },
            confirmButton = {
                PrimaryChallengeButton(
                    text = "Envoyer la demande",
                    onClick = {
                        vm.handleFriendChallengePrimaryAction(challenge)
                        pendingAccessChallenge = null
                    }
                )
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingAccessChallenge = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.myChallengesSection(
    challenges: List<Challenge>,
    onOpen: (String) -> Unit,
    onGoCreate: () -> Unit
) {
    if (challenges.isEmpty()) {
        item {
            EmptyChallengesCard(
                title = "Aucun challenge pour le moment",
                subtitle = "Cree ton premier defi pour lancer une dynamique quotidienne avec tes amis.",
                actionLabel = "Creer un challenge",
                onAction = onGoCreate
            )
        }
        return
    }
    items(challenges, key = { it.id }) { challenge ->
        ChallengeCard(
            challenge = challenge,
            ctaLabel = if (challenge.myRole == ChallengeRole.OWNER) "Gerer" else "Voir le detail",
            secondaryLabel = if (challenge.myRole != ChallengeRole.VIEWER) "Classement" else null,
            onClick = { onOpen(challenge.id) },
            onSecondaryClick = { onOpen(challenge.id) },
            emphasize = challenge.myRole == ChallengeRole.OWNER || challenge.status.label == "En cours"
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.friendsSection(
    challenges: List<Challenge>,
    incomingInvitations: List<ChallengeInvitation>,
    outgoingJoinRequests: List<OutgoingJoinRequest>,
    processingChallengeIds: Set<String>,
    processingInvitationIds: Set<String>,
    onRequestAccess: (Challenge) -> Unit,
    onPrimaryAction: (Challenge) -> Unit,
    onAcceptInvitation: (ChallengeInvitation) -> Unit,
    onRejectInvitation: (ChallengeInvitation) -> Unit,
    onOpen: (String) -> Unit
) {
    if (challenges.isEmpty()) {
        item { EmptyChallengesCard(title = "Tes amis n'ont pas encore lance de defi", subtitle = "Quand ils creeront un challenge, tu le verras ici.") }
        return
    }
    val pendingRequestsByChallengeId = outgoingJoinRequests
        .filter { it.status == JoinRequestStatus.PENDING }
        .associateBy { it.challenge.id }
    val incomingInvitationsByChallengeId = incomingInvitations
        .filter { it.status.equals("pending", ignoreCase = true) }
        .associateBy { it.challengeId }
    items(challenges, key = { it.id }) { challenge ->
        val pendingRequest = pendingRequestsByChallengeId[challenge.id]
        val incomingInvitation = incomingInvitationsByChallengeId[challenge.id]
        val membershipStatus = if (
            challenge.membershipStatus == MembershipStatus.NOT_JOINED &&
            pendingRequest != null
        ) {
            MembershipStatus.PENDING_REQUEST
        } else {
            challenge.membershipStatus
        }
        val pendingRequestType = pendingRequest?.requestType ?: challenge.myJoinRequestType
        val isProcessing = processingChallengeIds.contains(challenge.id)
        val isProcessingInvitation = incomingInvitation != null && processingInvitationIds.contains(incomingInvitation.id)
        val hasIncomingInvitation =
            membershipStatus == MembershipStatus.NOT_JOINED && incomingInvitation != null
        ChallengeCard(
            challenge = challenge,
            ctaLabel = when (membershipStatus) {
                MembershipStatus.OWNER -> "Ton challenge"
                MembershipStatus.JOINED -> "Ouvrir"
                MembershipStatus.PENDING_REQUEST -> "En attente"
                MembershipStatus.NOT_JOINED -> when {
                    hasIncomingInvitation -> if (isProcessingInvitation) "Traitement..." else "Accepter l'invitation"
                    isProcessing -> "Envoi..."
                    else -> "Demander l'acces"
                }
            },
            membershipLabel = when (membershipStatus) {
                MembershipStatus.OWNER -> "Owner"
                MembershipStatus.JOINED -> "Deja rejoint"
                MembershipStatus.PENDING_REQUEST -> "En attente"
                MembershipStatus.NOT_JOINED -> if (hasIncomingInvitation) "Invitation recue" else null
            },
            membershipTint = when (membershipStatus) {
                MembershipStatus.OWNER -> PinkPrimary
                MembershipStatus.JOINED -> androidx.compose.ui.graphics.Color(0xFF299764)
                MembershipStatus.PENDING_REQUEST -> PinkPrimary
                MembershipStatus.NOT_JOINED -> PinkPrimary
            },
            membershipBackground = when (membershipStatus) {
                MembershipStatus.OWNER -> PinkPrimary.copy(alpha = 0.10f)
                MembershipStatus.JOINED -> androidx.compose.ui.graphics.Color(0xFFEAFBF2)
                MembershipStatus.PENDING_REQUEST -> PinkPrimary.copy(alpha = 0.10f)
                MembershipStatus.NOT_JOINED -> PinkPrimary.copy(alpha = 0.10f)
            },
            supportingNote = when {
                hasIncomingInvitation ->
                    "${incomingInvitation?.inviterName?.ifBlank { "Un ami" } ?: "Un ami"} t'a deja envoye une invitation pour ce challenge."
                membershipStatus == MembershipStatus.PENDING_REQUEST ->
                    "${pendingRequestType.requestLabel()} envoyee au createur. Tu restes hors du challenge jusqu'a sa reponse."
                membershipStatus == MembershipStatus.JOINED ->
                    "Tu participes a ce challenge. Ouvre-le pour suivre ta progression."
                membershipStatus == MembershipStatus.NOT_JOINED ->
                    "Sans code, l'acces passe par une validation du createur."
                else -> "Tu geres deja ce challenge."
            },
            supportingNoteColor = when (membershipStatus) {
                MembershipStatus.JOINED -> androidx.compose.ui.graphics.Color(0xFF299764)
                else -> PinkPrimary
            },
            onPrimaryActionClick = when (membershipStatus) {
                MembershipStatus.OWNER -> null
                MembershipStatus.JOINED -> { { onOpen(challenge.id) } }
                MembershipStatus.PENDING_REQUEST -> { {} }
                MembershipStatus.NOT_JOINED -> {
                    if (hasIncomingInvitation) {
                        { onAcceptInvitation(incomingInvitation!!) }
                    } else {
                        { onRequestAccess(challenge) }
                    }
                }
            },
            primaryActionEnabled = when (membershipStatus) {
                MembershipStatus.OWNER -> false
                MembershipStatus.JOINED -> true
                MembershipStatus.PENDING_REQUEST -> false
                MembershipStatus.NOT_JOINED -> if (hasIncomingInvitation) !isProcessingInvitation else !isProcessing
            },
            secondaryLabel = when (membershipStatus) {
                MembershipStatus.OWNER -> "Voir"
                MembershipStatus.JOINED -> if (isProcessing) "Sortie..." else "Quitter"
                MembershipStatus.PENDING_REQUEST -> if (isProcessing) "Annulation..." else "Annuler la demande"
                MembershipStatus.NOT_JOINED -> if (hasIncomingInvitation) "Refuser" else "Voir"
            },
            onClick = { onOpen(challenge.id) },
            onSecondaryClick = when (membershipStatus) {
                MembershipStatus.OWNER -> { { onOpen(challenge.id) } }
                MembershipStatus.JOINED,
                MembershipStatus.PENDING_REQUEST -> { { onPrimaryAction(challenge) } }
                MembershipStatus.NOT_JOINED -> {
                    if (hasIncomingInvitation) {
                        { onRejectInvitation(incomingInvitation!!) }
                    } else {
                        { onOpen(challenge.id) }
                    }
                }
            }
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.invitationsSection(
    incomingJoinRequests: List<IncomingJoinRequest>,
    outgoingJoinRequests: List<OutgoingJoinRequest>,
    incomingJoinRequestsCount: Int,
    outgoingJoinRequestsCount: Int,
    processingIncomingJoinRequestIds: Set<String>,
    processingOutgoingJoinRequestChallengeIds: Set<String>,
    onOpenIncomingRequests: () -> Unit,
    onOpenMyRequests: () -> Unit,
    incoming: List<ChallengeInvitation>,
    outgoing: List<ChallengeInvitation>,
    onOpen: (String) -> Unit,
    onAcceptJoinRequest: (IncomingJoinRequest) -> Unit,
    onRejectJoinRequest: (IncomingJoinRequest) -> Unit,
    onCancelOutgoingJoinRequest: (OutgoingJoinRequest) -> Unit,
    onAccept: (ChallengeInvitation) -> Unit,
    onReject: (ChallengeInvitation) -> Unit
) {
    if (incoming.isEmpty() && outgoing.isEmpty() && incomingJoinRequestsCount == 0 && outgoingJoinRequestsCount == 0) {
        item { EmptyChallengesCard(title = "Aucune invitation", subtitle = "Quand un ami t'invitera dans un defi, tu la verras ici.") }
        return
    }
    if (incomingJoinRequests.isNotEmpty()) {
        item { SectionTitle(title = "Demandes recues") }
        items(incomingJoinRequests.take(3), key = { it.id }) { request ->
            InlineIncomingJoinRequestCard(
                request = request,
                isProcessing = processingIncomingJoinRequestIds.contains(request.id),
                onOpenChallenge = { onOpen(request.challenge.id) },
                onAccept = { onAcceptJoinRequest(request) },
                onReject = { onRejectJoinRequest(request) }
            )
        }
        if (incomingJoinRequests.size > 3) {
            item {
                InlineActionHintCard(
                    text = "${incomingJoinRequests.size - 3} autre(s) demande(s) attendent encore une reponse.",
                    actionLabel = "Tout voir",
                    onAction = onOpenIncomingRequests
                )
            }
        }
    }
    if (outgoingJoinRequests.isNotEmpty()) {
        item { SectionTitle(title = "Mes demandes") }
        items(outgoingJoinRequests.take(3), key = { it.id }) { request ->
            InlineOutgoingJoinRequestCard(
                request = request,
                isProcessing = processingOutgoingJoinRequestChallengeIds.contains(request.challenge.id),
                onOpenChallenge = { onOpen(request.challenge.id) },
                onCancel = { onCancelOutgoingJoinRequest(request) }
            )
        }
        if (outgoingJoinRequests.size > 3) {
            item {
                InlineActionHintCard(
                    text = "${outgoingJoinRequests.size - 3} autre(s) demande(s) sont encore en attente.",
                    actionLabel = "Mes demandes",
                    onAction = onOpenMyRequests
                )
            }
        }
    }
    if (incoming.isNotEmpty()) {
        item { SectionTitle(title = "Invitations recues") }
        items(incoming, key = { it.id }) { invitation ->
            InvitationCard(
                invitation = invitation,
                primaryLabel = "Accepter",
                secondaryLabel = "Refuser",
                onOpen = { onOpen(invitation.challengeId) },
                onPrimary = { onAccept(invitation) },
                onSecondary = { onReject(invitation) }
            )
        }
    }
    if (outgoing.isNotEmpty()) {
        item { SectionTitle(title = "Invitations envoyees") }
        items(outgoing, key = { it.id }) { invitation ->
            InvitationCard(
                invitation = invitation,
                primaryLabel = "Voir",
                secondaryLabel = invitation.status.ifBlank { "En attente" },
                onOpen = { onOpen(invitation.challengeId) },
                onPrimary = { onOpen(invitation.challengeId) },
                onSecondary = null
            )
        }
    }
}

@Composable
private fun InlineIncomingJoinRequestCard(
    request: IncomingJoinRequest,
    isProcessing: Boolean,
    onOpenChallenge: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Avatar(url = request.avatarUrl, fallback = request.username.take(1))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        request.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        request.requestType.ownerActionText(request.challenge.title),
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
                MiniPendingPill(label = "Pending")
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
}

@Composable
private fun InlineOutgoingJoinRequestCard(
    request: OutgoingJoinRequest,
    isProcessing: Boolean,
    onOpenChallenge: () -> Unit,
    onCancel: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        request.challenge.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${request.requestType.requestLabel()} envoyee a ${request.ownerUsername}",
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
                MiniPendingPill(label = "En attente")
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(onClick = onOpenChallenge, modifier = Modifier.weight(1f)) {
                    Text("Voir")
                }
                PrimaryChallengeButton(
                    text = if (isProcessing) "Annulation..." else "Annuler",
                    enabled = !isProcessing,
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MiniPendingPill(label: String) {
    Row(
        modifier = Modifier
            .background(PinkPrimary.copy(alpha = 0.10f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.HourglassTop,
            contentDescription = null,
            tint = PinkPrimary,
            modifier = Modifier.height(14.dp)
        )
        Text(
            label,
            color = PinkPrimary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
        )
    }
}

@Composable
private fun InlineActionHintCard(
    text: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                color = TextGray,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.height(0.dp))
            OutlinedButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun InvitationCard(
    invitation: ChallengeInvitation,
    primaryLabel: String,
    secondaryLabel: String,
    onOpen: () -> Unit,
    onPrimary: () -> Unit,
    onSecondary: (() -> Unit)?
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(invitation.challengeTitle(), style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
            Spacer(Modifier.height(4.dp))
            Text("Invite par ${invitation.inviterName.ifBlank { "un ami" }}", color = TextGray)
            val date = invitation.createdAt.toReadableDateTime()
            if (date.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(date, color = TextGray, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                PrimaryChallengeButton(text = primaryLabel, onClick = onPrimary, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = { onSecondary?.invoke() ?: onOpen() }, modifier = Modifier.weight(1f)) {
                    Text(secondaryLabel)
                }
            }
        }
    }
}

@Composable
private fun JoinRequestsSummaryCard(
    incomingCount: Int,
    outgoingCount: Int,
    onOpenIncomingRequests: () -> Unit,
    onOpenMyRequests: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                "Demandes d'acces",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Le code rejoint directement. Sans code, un challenge entre amis peut maintenant passer par une validation du proprietaire.",
                color = TextGray,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SoftCard(
                    modifier = Modifier.weight(1f),
                    padding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text("Recues", color = TextGray, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "$incomingCount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                    )
                }
                SoftCard(
                    modifier = Modifier.weight(1f),
                    padding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text("Mes demandes", color = TextGray, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "$outgoingCount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onOpenIncomingRequests, modifier = Modifier.weight(1f)) {
                    Text("Demandes recues")
                }
                OutlinedButton(onClick = onOpenMyRequests, modifier = Modifier.weight(1f)) {
                    Text("Mes demandes")
                }
            }
            if (incomingCount > 0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tu peux accepter ou refuser les demandes recues depuis cet espace, sans chercher le challenge a la main.",
                    color = TextGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
