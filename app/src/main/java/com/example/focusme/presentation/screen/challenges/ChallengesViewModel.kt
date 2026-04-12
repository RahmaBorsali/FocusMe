package com.example.focusme.presentation.screen.challenges

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusme.data.repository.Challenge
import com.example.focusme.data.repository.ChallengeInvitation
import com.example.focusme.data.repository.ChallengeJoinRequest
import com.example.focusme.data.repository.ChallengeMessage
import com.example.focusme.data.repository.ChallengeOverview
import com.example.focusme.data.repository.ChallengeParticipant
import com.example.focusme.data.repository.ChallengeRole
import com.example.focusme.data.repository.ChallengeStatus
import com.example.focusme.data.repository.ChallengeVisibility
import com.example.focusme.data.repository.ChallengesRepository
import com.example.focusme.data.repository.CreateChallengeInput
import com.example.focusme.data.repository.FriendsRepository
import com.example.focusme.data.repository.GoalType
import com.example.focusme.data.repository.IncomingJoinRequest
import com.example.focusme.data.repository.JoinChallengeResult
import com.example.focusme.data.repository.JoinRequestStatus
import com.example.focusme.data.repository.JoinRequestType
import com.example.focusme.data.repository.LeaveChallengeResult
import com.example.focusme.data.repository.MembershipStatus
import com.example.focusme.data.repository.OutgoingJoinRequest
import com.example.focusme.presentation.model.FriendStatus
import com.example.focusme.presentation.model.UserUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.LocalDate

sealed interface ContentState<out T> {
    data object Loading : ContentState<Nothing>
    data class Success<T>(val data: T) : ContentState<T>
    data class Error(val message: String) : ContentState<Nothing>
}

enum class ChallengesHomeTab(val title: String) {
    MINE("Mes challenges"),
    FRIENDS("Mes amis"),
    INVITATIONS("Invitations")
}

data class ChallengesHomeUiState(
    val selectedTab: ChallengesHomeTab = ChallengesHomeTab.MINE,
    val myChallenges: List<Challenge> = emptyList(),
    val friendChallenges: List<Challenge> = emptyList(),
    val incomingJoinRequests: List<IncomingJoinRequest> = emptyList(),
    val outgoingJoinRequests: List<OutgoingJoinRequest> = emptyList(),
    val incomingInvitations: List<ChallengeInvitation> = emptyList(),
    val outgoingInvitations: List<ChallengeInvitation> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val joinCodeInput: String = "",
    val joinCodeError: String? = null,
    val isJoiningByCode: Boolean = false,
    val processingFriendChallengeIds: Set<String> = emptySet(),
    val processingInvitationIds: Set<String> = emptySet(),
    val processingIncomingJoinRequestIds: Set<String> = emptySet(),
    val processingOutgoingJoinRequestChallengeIds: Set<String> = emptySet(),
    val friendActionMessage: String? = null,
    val globalError: String? = null
)

data class CreateChallengeUiState(
    val title: String = "",
    val description: String = "",
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now().plusDays(7),
    val visibility: ChallengeVisibility = ChallengeVisibility.FRIENDS,
    val goalType: GoalType = GoalType.FOCUS_MINUTES,
    val targetValue: String = "300",
    val maxParticipants: String = "8",
    val isSubmitting: Boolean = false,
    val fieldErrors: Map<String, String> = emptyMap(),
    val submitError: String? = null
)

data class ChallengeDetailsUiState(
    val overviewState: ContentState<ChallengeOverview> = ContentState.Loading,
    val isRefreshing: Boolean = false,
    val actionMessage: String? = null,
    val actionError: String? = null,
    val hasPendingJoinRequest: Boolean = false,
    val pendingJoinRequestId: String? = null,
    val pendingInvitation: ChallengeInvitation? = null,
    val isJoining: Boolean = false,
    val isRespondingToInvitation: Boolean = false,
    val isCancellingRequest: Boolean = false,
    val isLeaving: Boolean = false
)

data class LeaderboardUiState(
    val state: ContentState<List<com.example.focusme.data.repository.LeaderboardEntry>> = ContentState.Loading,
    val isRefreshing: Boolean = false
)

data class ParticipantsUiState(
    val state: ContentState<List<ChallengeParticipant>> = ContentState.Loading,
    val isRefreshing: Boolean = false,
    val isRemoving: Boolean = false,
    val actionError: String? = null
)

data class ChallengeChatUiState(
    val state: ContentState<List<ChallengeMessage>> = ContentState.Loading,
    val isRefreshing: Boolean = false,
    val composer: String = "",
    val isSending: Boolean = false,
    val actionError: String? = null
)

data class ChallengeInvitationsUiState(
    val incomingState: ContentState<List<ChallengeInvitation>> = ContentState.Loading,
    val outgoingState: ContentState<List<ChallengeInvitation>> = ContentState.Loading,
    val manageState: ContentState<List<ChallengeInvitation>> = ContentState.Loading,
    val inviteeUserId: String = "",
    val selectedFriend: UserUi? = null,
    val friends: List<UserUi> = emptyList(),
    val isRefreshing: Boolean = false,
    val isInviting: Boolean = false,
    val actionMessage: String? = null,
    val actionError: String? = null
)

data class ChallengeJoinRequestsUiState(
    val state: ContentState<List<ChallengeJoinRequest>> = ContentState.Loading,
    val isRefreshing: Boolean = false,
    val processingRequestIds: Set<String> = emptySet(),
    val actionError: String? = null
)

data class IncomingJoinRequestsUiState(
    val state: ContentState<List<IncomingJoinRequest>> = ContentState.Loading,
    val isRefreshing: Boolean = false,
    val processingRequestIds: Set<String> = emptySet(),
    val actionError: String? = null
)

data class MyJoinRequestsUiState(
    val state: ContentState<List<OutgoingJoinRequest>> = ContentState.Loading,
    val isRefreshing: Boolean = false,
    val processingChallengeIds: Set<String> = emptySet(),
    val actionError: String? = null,
    val actionMessage: String? = null
)

sealed interface JoinByCodeLookupState {
    data object Idle : JoinByCodeLookupState
    data object Loading : JoinByCodeLookupState
    data class Preview(val challenge: Challenge, val availability: JoinCodeAvailability) : JoinByCodeLookupState
    data class Error(val title: String, val message: String) : JoinByCodeLookupState
}

enum class JoinCodeAvailability {
    READY,
    ALREADY_JOINED,
    CHALLENGE_FULL,
    CHALLENGE_FINISHED,
    ACCESS_RESTRICTED,
    JOINED_SUCCESS
}

data class JoinByCodeUiState(
    val code: String = "",
    val lookupState: JoinByCodeLookupState = JoinByCodeLookupState.Idle,
    val isJoining: Boolean = false,
    val actionError: String? = null
)

class ChallengesHomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ChallengesRepository(app)

    private val _uiState = MutableStateFlow(ChallengesHomeUiState())
    val uiState: StateFlow<ChallengesHomeUiState> = _uiState.asStateFlow()

    init {
        refreshAll()
    }

    fun selectTab(tab: ChallengesHomeTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun updateJoinCodeInput(value: String) {
        _uiState.update {
            it.copy(
                joinCodeInput = value.uppercase().replace(" ", ""),
                joinCodeError = null,
                friendActionMessage = null,
                globalError = null
            )
        }
    }

    fun refreshAll(isPullRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !isPullRefresh && it.myChallenges.isEmpty() && it.friendChallenges.isEmpty(),
                    isRefreshing = isPullRefresh,
                    friendActionMessage = if (isPullRefresh) null else it.friendActionMessage,
                    globalError = null
                )
            }
            val previous = uiState.value

            val mineResult = runCatching { repo.getMyChallenges() }
            val friendsResult = runCatching { repo.getFriendChallenges() }
            val incomingJoinRequestsResult = runCatching { repo.getIncomingJoinRequests() }
            val outgoingJoinRequestsResult = runCatching { repo.getOutgoingJoinRequests() }
            val incomingInvitationsResult = runCatching { repo.getIncomingInvitations() }
            val outgoingInvitationsResult = runCatching { repo.getOutgoingInvitations() }

            val mainFailures = listOfNotNull(
                mineResult.exceptionOrNull(),
                friendsResult.exceptionOrNull()
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    myChallenges = mineResult.getOrElse { previous.myChallenges },
                    friendChallenges = friendsResult.getOrElse { previous.friendChallenges },
                    incomingJoinRequests = incomingJoinRequestsResult.getOrElse { previous.incomingJoinRequests },
                    outgoingJoinRequests = outgoingJoinRequestsResult.getOrElse { previous.outgoingJoinRequests },
                    incomingInvitations = incomingInvitationsResult.getOrElse { previous.incomingInvitations },
                    outgoingInvitations = outgoingInvitationsResult.getOrElse { previous.outgoingInvitations },
                    globalError = if (
                        mainFailures.size == 2 &&
                        previous.myChallenges.isEmpty() &&
                        previous.friendChallenges.isEmpty()
                    ) {
                        mainFailures.first().toUserMessage()
                    } else {
                        null
                    }
                )
            }
        }
    }

    fun joinChallengeByCode(onJoined: (String) -> Unit) {
        val code = uiState.value.joinCodeInput.trim()
        if (code.isBlank()) {
            _uiState.update { it.copy(joinCodeError = "Entre un code pour rejoindre un defi.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isJoiningByCode = true,
                    joinCodeError = null,
                    friendActionMessage = null,
                    globalError = null
                )
            }
            runCatching {
                val challenge = repo.getChallengeByCode(code)
                if (challenge.joined || challenge.myRole != ChallengeRole.VIEWER) {
                    challenge.id
                } else {
                    when (repo.joinChallenge(challenge.id, code)) {
                        is JoinChallengeResult.Joined -> challenge.id
                        is JoinChallengeResult.PendingApproval -> {
                            throw IllegalStateException("Ce code doit rejoindre directement le challenge.")
                        }
                    }
                }
            }.onSuccess { challengeId ->
                _uiState.update {
                    it.copy(
                        isJoiningByCode = false,
                        joinCodeInput = "",
                        joinCodeError = null,
                        selectedTab = ChallengesHomeTab.MINE
                    )
                }
                refreshAll()
                onJoined(challengeId)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isJoiningByCode = false,
                        joinCodeError = throwable.toJoinCodeMessage()
                    )
                }
            }
        }
    }

    fun handleFriendChallengePrimaryAction(challenge: Challenge) {
        when (challenge.membershipStatus) {
            MembershipStatus.PENDING_REQUEST -> cancelPendingFriendRequest(challenge.id)
            MembershipStatus.NOT_JOINED -> requestChallengeAccess(challenge.id)
            MembershipStatus.JOINED -> leaveFriendChallenge(challenge.id)
            MembershipStatus.OWNER -> Unit
        }
    }

    private fun requestChallengeAccess(challengeId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    processingFriendChallengeIds = it.processingFriendChallengeIds + challengeId,
                    friendActionMessage = null,
                    globalError = null
                )
            }
            runCatching { repo.requestAccessChallenge(challengeId) }
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            processingFriendChallengeIds = it.processingFriendChallengeIds - challengeId,
                            friendActionMessage = when (result) {
                                is JoinChallengeResult.Joined -> "Tu participes deja a ce challenge."
                                is JoinChallengeResult.PendingApproval -> result.requestType.pendingApprovalMessage()
                            }
                        )
                    }
                    refreshAll()
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            processingFriendChallengeIds = it.processingFriendChallengeIds - challengeId,
                            globalError = throwable.toUserMessage()
                        )
                    }
                }
        }
    }

    private fun cancelPendingFriendRequest(challengeId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    processingFriendChallengeIds = it.processingFriendChallengeIds + challengeId,
                    friendActionMessage = null,
                    globalError = null
                )
            }
            runCatching { repo.cancelMyJoinRequest(challengeId) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            processingFriendChallengeIds = it.processingFriendChallengeIds - challengeId,
                            friendActionMessage = "Demande annulee."
                        )
                    }
                    refreshAll()
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            processingFriendChallengeIds = it.processingFriendChallengeIds - challengeId,
                            globalError = throwable.toUserMessage()
                        )
                    }
                }
        }
    }

    private fun leaveFriendChallenge(challengeId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    processingFriendChallengeIds = it.processingFriendChallengeIds + challengeId,
                    friendActionMessage = null,
                    globalError = null
                )
            }
            runCatching { repo.leaveChallenge(challengeId) }
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            processingFriendChallengeIds = it.processingFriendChallengeIds - challengeId,
                            friendActionMessage = when (result) {
                                LeaveChallengeResult.Left -> "Tu as quitte ce challenge."
                                LeaveChallengeResult.CancelledRequest -> "Demande annulee."
                                LeaveChallengeResult.NotJoined -> "Tu n'es plus membre de ce challenge."
                            }
                        )
                    }
                    refreshAll()
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            processingFriendChallengeIds = it.processingFriendChallengeIds - challengeId,
                            globalError = throwable.toUserMessage()
                        )
                    }
                }
        }
    }

    fun acceptInvitation(invitation: ChallengeInvitation, onOpenChallenge: (String) -> Unit) {
        val challengeId = invitation.challengeId
        if (challengeId.isBlank()) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    processingInvitationIds = it.processingInvitationIds + invitation.id,
                    friendActionMessage = null,
                    globalError = null
                )
            }
            runCatching {
                repo.acceptInvitation(challengeId, invitation.id)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        processingInvitationIds = it.processingInvitationIds - invitation.id,
                        friendActionMessage = "Invitation acceptee. Tu as rejoint ${invitation.challengeTitle()}."
                    )
                }
                refreshAll()
                onOpenChallenge(challengeId)
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        processingInvitationIds = state.processingInvitationIds - invitation.id,
                        globalError = it.toUserMessage()
                    )
                }
            }
        }
    }

    fun rejectInvitation(invitation: ChallengeInvitation) {
        val challengeId = invitation.challengeId
        if (challengeId.isBlank()) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    processingInvitationIds = it.processingInvitationIds + invitation.id,
                    friendActionMessage = null,
                    globalError = null
                )
            }
            runCatching {
                repo.rejectInvitation(challengeId, invitation.id)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        processingInvitationIds = it.processingInvitationIds - invitation.id,
                        friendActionMessage = "Invitation refusee pour ${invitation.challengeTitle()}."
                    )
                }
                refreshAll()
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        processingInvitationIds = state.processingInvitationIds - invitation.id,
                        globalError = it.toUserMessage()
                    )
                }
            }
        }
    }

    fun acceptIncomingJoinRequest(request: IncomingJoinRequest) {
        val challengeId = request.challenge.id
        val requestId = request.id
        if (challengeId.isBlank() || requestId.isBlank()) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    processingIncomingJoinRequestIds = it.processingIncomingJoinRequestIds + requestId,
                    friendActionMessage = null,
                    globalError = null
                )
            }
            runCatching { repo.acceptJoinRequest(challengeId, requestId) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            processingIncomingJoinRequestIds = it.processingIncomingJoinRequestIds - requestId,
                            friendActionMessage = "${request.username} rejoint maintenant ${request.challenge.title}."
                        )
                    }
                    refreshAll()
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            processingIncomingJoinRequestIds = it.processingIncomingJoinRequestIds - requestId,
                            globalError = throwable.toUserMessage()
                        )
                    }
                }
        }
    }

    fun rejectIncomingJoinRequest(request: IncomingJoinRequest) {
        val challengeId = request.challenge.id
        val requestId = request.id
        if (challengeId.isBlank() || requestId.isBlank()) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    processingIncomingJoinRequestIds = it.processingIncomingJoinRequestIds + requestId,
                    friendActionMessage = null,
                    globalError = null
                )
            }
            runCatching { repo.rejectJoinRequest(challengeId, requestId) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            processingIncomingJoinRequestIds = it.processingIncomingJoinRequestIds - requestId,
                            friendActionMessage = "${request.requestType.requestLabel()} refusee pour ${request.challenge.title}."
                        )
                    }
                    refreshAll()
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            processingIncomingJoinRequestIds = it.processingIncomingJoinRequestIds - requestId,
                            globalError = throwable.toUserMessage()
                        )
                    }
                }
        }
    }

    fun cancelOutgoingJoinRequest(request: OutgoingJoinRequest) {
        val challengeId = request.challenge.id
        if (challengeId.isBlank()) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    processingOutgoingJoinRequestChallengeIds = it.processingOutgoingJoinRequestChallengeIds + challengeId,
                    friendActionMessage = null,
                    globalError = null
                )
            }
            runCatching { repo.cancelMyJoinRequest(challengeId) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            processingOutgoingJoinRequestChallengeIds = it.processingOutgoingJoinRequestChallengeIds - challengeId,
                            friendActionMessage = "${request.requestType.requestLabel()} annulee pour ${request.challenge.title}."
                        )
                    }
                    refreshAll()
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            processingOutgoingJoinRequestChallengeIds = it.processingOutgoingJoinRequestChallengeIds - challengeId,
                            globalError = throwable.toUserMessage()
                        )
                    }
                }
        }
    }
}

class CreateChallengeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ChallengesRepository(app)

    private val _uiState = MutableStateFlow(CreateChallengeUiState())
    val uiState: StateFlow<CreateChallengeUiState> = _uiState.asStateFlow()

    fun updateTitle(value: String) = _uiState.updateField("title") { it.copy(title = value) }
    fun updateDescription(value: String) = _uiState.update { it.copy(description = value, submitError = null) }
    fun updateStartDate(value: LocalDate) = _uiState.update { it.copy(startDate = value, fieldErrors = it.fieldErrors - "startDate" - "endDate") }
    fun updateEndDate(value: LocalDate) = _uiState.update { it.copy(endDate = value, fieldErrors = it.fieldErrors - "endDate") }
    fun updateVisibility(value: ChallengeVisibility) = _uiState.update { it.copy(visibility = value, submitError = null) }
    fun updateGoalType(value: GoalType) = _uiState.update { it.copy(goalType = value, submitError = null) }
    fun updateTargetValue(value: String) = _uiState.updateField("targetValue") { it.copy(targetValue = value) }
    fun updateMaxParticipants(value: String) = _uiState.updateField("maxParticipants") { it.copy(maxParticipants = value) }

    fun submit(onCreated: (String) -> Unit) {
        val current = uiState.value
        val errors = validate(current)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors, submitError = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }
            runCatching {
                repo.createChallenge(
                    CreateChallengeInput(
                        title = current.title,
                        description = current.description,
                        startDate = current.startDate.toString(),
                        endDate = current.endDate.toString(),
                        visibility = current.visibility,
                        goalType = current.goalType,
                        targetValue = current.targetValue.toInt(),
                        maxParticipants = current.maxParticipants.toIntOrNull()
                    )
                )
            }.onSuccess { created ->
                _uiState.update { it.copy(isSubmitting = false) }
                onCreated(created.id)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submitError = throwable.toUserMessage()
                    )
                }
            }
        }
    }

    private fun validate(state: CreateChallengeUiState): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (state.title.trim().length < 3) errors["title"] = "Choisis un titre plus clair."
        if (state.endDate.isBefore(state.startDate)) errors["endDate"] = "La date de fin doit etre apres le debut."
        if (state.targetValue.toIntOrNull()?.let { it > 0 } != true) errors["targetValue"] = "Entre un objectif valide."
        if (state.maxParticipants.isNotBlank() && state.maxParticipants.toIntOrNull()?.let { it >= 2 } != true) {
            errors["maxParticipants"] = "Mets au moins 2 participants."
        }
        return errors
    }
}

class ChallengeDetailsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ChallengesRepository(app)

    private val _uiState = MutableStateFlow(ChallengeDetailsUiState())
    val uiState: StateFlow<ChallengeDetailsUiState> = _uiState.asStateFlow()

    fun load(id: String, refresh: Boolean = false) {
        viewModelScope.launch {
            val previousInvitation = uiState.value.pendingInvitation
            _uiState.update {
                it.copy(
                    overviewState = if (!refresh) ContentState.Loading else it.overviewState,
                    isRefreshing = refresh,
                    actionError = null
                )
            }
            val overviewResult = runCatching { repo.getOverview(id) }
            val pendingInvitation = runCatching { repo.getIncomingInvitationForChallenge(id) }
                .getOrElse { previousInvitation?.takeIf { invitation -> invitation.challengeId == id } }
            overviewResult
                .onSuccess { overview ->
                    _uiState.update {
                        it.copy(
                            overviewState = ContentState.Success(overview),
                            isRefreshing = false,
                            hasPendingJoinRequest = overview.challenge.membershipStatus == MembershipStatus.PENDING_REQUEST,
                            pendingJoinRequestId = overview.challenge.myJoinRequestId,
                            pendingInvitation = pendingInvitation
                        )
                    }
                }
                .onFailure {
                    runCatching { repo.getChallengeDetails(id) }
                        .onSuccess { challenge ->
                            val fallbackOverview = challenge.toFallbackOverview()
                            _uiState.update {
                                it.copy(
                                    overviewState = ContentState.Success(fallbackOverview),
                                    isRefreshing = false,
                                    hasPendingJoinRequest = challenge.membershipStatus == MembershipStatus.PENDING_REQUEST,
                                    pendingJoinRequestId = challenge.myJoinRequestId,
                                    pendingInvitation = pendingInvitation,
                                    actionMessage = if (
                                        challenge.membershipStatus == MembershipStatus.PENDING_REQUEST
                                    ) {
                                        "Apercu simplifie charge. Ta ${challenge.myJoinRequestType.requestLabel().lowercase()} est toujours en attente."
                                    } else {
                                        it.actionMessage
                                    }
                                )
                            }
                        }
                        .onFailure { throwable ->
                            _uiState.update {
                                it.copy(
                                    overviewState = ContentState.Error(throwable.toUserMessage()),
                                    isRefreshing = false,
                                    hasPendingJoinRequest = false,
                                    pendingJoinRequestId = null,
                                    pendingInvitation = pendingInvitation
                                )
                            }
                        }
                }
        }
    }

    fun join(id: String, code: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isJoining = true, actionError = null, actionMessage = null) }
            runCatching { repo.joinChallenge(id, code) }
                .onSuccess { result ->
                    when (result) {
                        is JoinChallengeResult.Joined -> {
                            _uiState.update {
                                it.copy(
                                    isJoining = false,
                                    actionMessage = if (code.isNullOrBlank()) {
                                        "Tu as rejoint le challenge."
                                    } else {
                                        "Challenge rejoint avec le code."
                                    },
                                    hasPendingJoinRequest = false,
                                    pendingJoinRequestId = null
                                )
                            }
                        }
                        is JoinChallengeResult.PendingApproval -> {
                            _uiState.update {
                                it.copy(
                                    isJoining = false,
                                    actionMessage = result.requestType.pendingApprovalMessage(),
                                    hasPendingJoinRequest = true,
                                    pendingJoinRequestId = result.requestId
                                )
                            }
                        }
                    }
                    load(id, refresh = true)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isJoining = false,
                            actionError = throwable.toUserMessage()
                        )
                    }
                }
        }
    }

    fun requestAccess(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isJoining = true, actionError = null, actionMessage = null) }
            runCatching { repo.requestAccessChallenge(id) }
                .onSuccess { result ->
                    when (result) {
                        is JoinChallengeResult.Joined -> {
                            _uiState.update {
                                it.copy(
                                    isJoining = false,
                                    actionMessage = "Tu participes deja a ce challenge.",
                                    hasPendingJoinRequest = false,
                                    pendingJoinRequestId = null
                                )
                            }
                        }
                        is JoinChallengeResult.PendingApproval -> {
                            _uiState.update {
                                it.copy(
                                    isJoining = false,
                                    actionMessage = result.requestType.pendingApprovalMessage(),
                                    hasPendingJoinRequest = true,
                                    pendingJoinRequestId = result.requestId
                                )
                            }
                        }
                    }
                    load(id, refresh = true)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isJoining = false,
                            actionError = throwable.toUserMessage()
                        )
                    }
                }
        }
    }

    fun acceptPendingInvitation(id: String, invitation: ChallengeInvitation) {
        val challengeId = invitation.challengeId.ifBlank { id }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRespondingToInvitation = true,
                    actionError = null,
                    actionMessage = null
                )
            }
            runCatching { repo.acceptInvitation(challengeId, invitation.id) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isRespondingToInvitation = false,
                            pendingInvitation = null,
                            actionMessage = "Invitation acceptee. Tu as rejoint le challenge."
                        )
                    }
                    load(id, refresh = true)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isRespondingToInvitation = false,
                            actionError = throwable.toUserMessage()
                        )
                    }
                }
        }
    }

    fun rejectPendingInvitation(id: String, invitation: ChallengeInvitation) {
        val challengeId = invitation.challengeId.ifBlank { id }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRespondingToInvitation = true,
                    actionError = null,
                    actionMessage = null
                )
            }
            runCatching { repo.rejectInvitation(challengeId, invitation.id) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isRespondingToInvitation = false,
                            pendingInvitation = null,
                            actionMessage = "Invitation refusee."
                        )
                    }
                    load(id, refresh = true)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isRespondingToInvitation = false,
                            actionError = throwable.toUserMessage()
                        )
                    }
                }
        }
    }

    fun cancelPendingRequest(id: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCancellingRequest = true,
                    actionError = null,
                    actionMessage = null
                )
            }
            runCatching { repo.cancelMyJoinRequest(id) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isCancellingRequest = false,
                            hasPendingJoinRequest = false,
                            pendingJoinRequestId = null,
                            actionMessage = "Ta demande a ete annulee."
                        )
                    }
                    load(id, refresh = true)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isCancellingRequest = false,
                            actionError = throwable.toUserMessage()
                        )
                    }
                }
        }
    }

    fun leave(id: String, onLeft: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLeaving = true, actionError = null, actionMessage = null) }
            runCatching { repo.leaveChallenge(id) }
                .onSuccess { result ->
                    when (result) {
                        LeaveChallengeResult.Left -> {
                            _uiState.update { it.copy(isLeaving = false) }
                            onLeft()
                        }
                        LeaveChallengeResult.CancelledRequest,
                        LeaveChallengeResult.NotJoined -> {
                            _uiState.update {
                                it.copy(
                                    isLeaving = false,
                                    actionMessage = "L'etat du challenge a ete mis a jour."
                                )
                            }
                            load(id, refresh = true)
                        }
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLeaving = false,
                            actionError = throwable.toUserMessage()
                        )
                    }
                }
        }
    }
}

class ChallengeLeaderboardViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ChallengesRepository(app)

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    fun load(id: String, refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    state = if (!refresh) ContentState.Loading else it.state,
                    isRefreshing = refresh
                )
            }
            runCatching { repo.getLeaderboard(id) }
                .onSuccess { rows ->
                    _uiState.update {
                        it.copy(state = ContentState.Success(rows), isRefreshing = false)
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(state = ContentState.Error(throwable.toUserMessage()), isRefreshing = false)
                    }
                }
        }
    }
}

class ChallengeParticipantsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ChallengesRepository(app)

    private val _uiState = MutableStateFlow(ParticipantsUiState())
    val uiState: StateFlow<ParticipantsUiState> = _uiState.asStateFlow()

    fun load(id: String, refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    state = if (!refresh) ContentState.Loading else it.state,
                    isRefreshing = refresh,
                    actionError = null
                )
            }
            runCatching { repo.getParticipants(id) }
                .onSuccess { participants ->
                    _uiState.update {
                        it.copy(state = ContentState.Success(participants), isRefreshing = false)
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(state = ContentState.Error(throwable.toUserMessage()), isRefreshing = false)
                    }
                }
        }
    }

    fun removeParticipant(challengeId: String, userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRemoving = true, actionError = null) }
            runCatching { repo.removeParticipant(challengeId, userId) }
                .onSuccess {
                    _uiState.update { it.copy(isRemoving = false) }
                    load(challengeId, refresh = true)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(isRemoving = false, actionError = throwable.toUserMessage())
                    }
                }
        }
    }
}

class ChallengeChatViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ChallengesRepository(app)

    private val _uiState = MutableStateFlow(ChallengeChatUiState())
    val uiState: StateFlow<ChallengeChatUiState> = _uiState.asStateFlow()

    fun updateComposer(value: String) {
        _uiState.update { it.copy(composer = value, actionError = null) }
    }

    fun load(id: String, refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    state = if (!refresh) ContentState.Loading else it.state,
                    isRefreshing = refresh,
                    actionError = null
                )
            }
            runCatching { repo.getMessages(id) }
                .onSuccess { messages ->
                    _uiState.update {
                        it.copy(state = ContentState.Success(messages.reversed()), isRefreshing = false)
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(state = ContentState.Error(throwable.toUserMessage()), isRefreshing = false)
                    }
                }
        }
    }

    fun sendMessage(id: String) {
        val text = uiState.value.composer.trim()
        if (text.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, actionError = null) }
            runCatching { repo.sendMessage(id, text) }
                .onSuccess {
                    _uiState.update { it.copy(isSending = false, composer = "") }
                    load(id, refresh = true)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(isSending = false, actionError = throwable.toUserMessage())
                    }
                }
        }
    }
}

class ChallengeInvitationsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ChallengesRepository(app)
    private val friendsRepo = FriendsRepository(app)

    private val _uiState = MutableStateFlow(ChallengeInvitationsUiState())
    val uiState: StateFlow<ChallengeInvitationsUiState> = _uiState.asStateFlow()

    init {
        loadFriends()
    }

    fun updateInviteeUserId(value: String) {
        val normalized = value.trim()
        _uiState.update { state ->
            state.copy(
                inviteeUserId = value,
                selectedFriend = state.friends.firstOrNull { friend ->
                    friend.id.equals(normalized, ignoreCase = true) ||
                        friend.username.equals(normalized.replace(" ", "").lowercase(), ignoreCase = true) ||
                        friend.name.equals(normalized, ignoreCase = true)
                },
                actionError = null,
                actionMessage = null
            )
        }
    }

    fun selectFriend(friend: UserUi) {
        _uiState.update {
            it.copy(
                inviteeUserId = friend.name,
                selectedFriend = friend,
                actionError = null,
                actionMessage = null
            )
        }
    }

    fun loadInbox(refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    incomingState = if (!refresh) ContentState.Loading else it.incomingState,
                    outgoingState = if (!refresh) ContentState.Loading else it.outgoingState,
                    isRefreshing = refresh,
                    actionError = null
                )
            }
            val incomingResult = runCatching { repo.getIncomingInvitations() }
            val outgoingResult = runCatching { repo.getOutgoingInvitations() }
            _uiState.update {
                it.copy(
                    incomingState = incomingResult.fold(
                        onSuccess = { list -> ContentState.Success(list) },
                        onFailure = { error -> ContentState.Error(error.toUserMessage()) }
                    ),
                    outgoingState = outgoingResult.fold(
                        onSuccess = { list -> ContentState.Success(list) },
                        onFailure = { error -> ContentState.Error(error.toUserMessage()) }
                    ),
                    isRefreshing = false
                )
            }
        }
    }

    fun loadManage(challengeId: String, refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    manageState = if (!refresh) ContentState.Loading else it.manageState,
                    isRefreshing = refresh,
                    actionError = null
                )
            }
            runCatching { repo.getChallengeInvitations(challengeId) }
                .onSuccess { invitations ->
                    _uiState.update {
                        it.copy(manageState = ContentState.Success(invitations), isRefreshing = false)
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(manageState = ContentState.Error(throwable.toUserMessage()), isRefreshing = false)
                    }
                }
        }
    }

    fun invite(challengeId: String) {
        val typedValue = uiState.value.inviteeUserId.trim()
        val selectedFriend = uiState.value.selectedFriend
        val userId = selectedFriend?.id ?: typedValue
        if (userId.isBlank()) {
            _uiState.update { it.copy(actionError = "Entre l'identifiant d'un ami a inviter.", actionMessage = null) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isInviting = true, actionError = null, actionMessage = null) }
            runCatching { repo.inviteFriend(challengeId, userId) }
                .onSuccess { invitation ->
                    _uiState.update {
                        it.copy(
                            isInviting = false,
                            inviteeUserId = "",
                            selectedFriend = null,
                            actionMessage = "Invitation envoyee a ${invitation.inviteeName.ifBlank { invitation.inviteeId ?: "cet ami" }}."
                        )
                    }
                    loadManage(challengeId, refresh = true)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(isInviting = false, actionError = throwable.toUserMessage(), actionMessage = null)
                    }
                }
        }
    }

    fun accept(invitation: ChallengeInvitation) {
        val challengeId = invitation.challengeId
        if (challengeId.isBlank()) return
        viewModelScope.launch {
            runCatching { repo.acceptInvitation(challengeId, invitation.id) }
                .onSuccess { loadInbox(refresh = true) }
                .onFailure { throwable ->
                    _uiState.update { it.copy(actionError = throwable.toUserMessage()) }
                }
        }
    }

    fun reject(invitation: ChallengeInvitation) {
        val challengeId = invitation.challengeId
        if (challengeId.isBlank()) return
        viewModelScope.launch {
            runCatching { repo.rejectInvitation(challengeId, invitation.id) }
                .onSuccess { loadInbox(refresh = true) }
                .onFailure { throwable ->
                    _uiState.update { it.copy(actionError = throwable.toUserMessage()) }
                }
        }
    }

    private fun loadFriends() {
        viewModelScope.launch {
            friendsRepo.friends()
                .onSuccess { friends ->
                    _uiState.update {
                        it.copy(
                            friends = friends.map { user ->
                                UserUi(
                                    id = user.id,
                                    name = user.username,
                                    username = user.username.replace(" ", "").lowercase(),
                                    status = FriendStatus.FRIEND
                                )
                            }
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(actionError = throwable.message ?: "Impossible de charger tes amis.") }
                }
        }
    }
}

class ChallengeJoinRequestsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ChallengesRepository(app)

    private val _uiState = MutableStateFlow(ChallengeJoinRequestsUiState())
    val uiState: StateFlow<ChallengeJoinRequestsUiState> = _uiState.asStateFlow()

    fun load(challengeId: String, refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    state = if (!refresh) ContentState.Loading else it.state,
                    isRefreshing = refresh,
                    actionError = null
                )
            }
            runCatching { repo.getChallengeJoinRequests(challengeId) }
                .onSuccess { requests ->
                    _uiState.update {
                        it.copy(
                            state = ContentState.Success(requests),
                            isRefreshing = false
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            state = ContentState.Error(throwable.toUserMessage()),
                            isRefreshing = false
                        )
                    }
                }
        }
    }

    fun accept(challengeId: String, requestId: String) {
        processRequest(challengeId, requestId) {
            repo.acceptJoinRequest(challengeId, requestId)
        }
    }

    fun reject(challengeId: String, requestId: String) {
        processRequest(challengeId, requestId) {
            repo.rejectJoinRequest(challengeId, requestId)
        }
    }

    private fun processRequest(
        challengeId: String,
        requestId: String,
        action: suspend () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    processingRequestIds = it.processingRequestIds + requestId,
                    actionError = null
                )
            }
            runCatching { action() }
                .onSuccess {
                    _uiState.update {
                        it.copy(processingRequestIds = it.processingRequestIds - requestId)
                    }
                    load(challengeId, refresh = true)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            processingRequestIds = it.processingRequestIds - requestId,
                            actionError = throwable.toUserMessage()
                        )
                    }
                }
        }
    }

}

class IncomingJoinRequestsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ChallengesRepository(app)

    private val _uiState = MutableStateFlow(IncomingJoinRequestsUiState())
    val uiState: StateFlow<IncomingJoinRequestsUiState> = _uiState.asStateFlow()

    fun load(refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    state = if (!refresh) ContentState.Loading else it.state,
                    isRefreshing = refresh,
                    actionError = null
                )
            }
            runCatching { repo.getIncomingJoinRequests() }
                .onSuccess { requests ->
                    _uiState.update {
                        it.copy(
                            state = ContentState.Success(requests),
                            isRefreshing = false
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            state = ContentState.Error(throwable.toUserMessage()),
                            isRefreshing = false
                        )
                    }
                }
        }
    }

    fun accept(request: IncomingJoinRequest) {
        val challengeId = request.challenge.id
        processRequest(request.id) {
            repo.acceptJoinRequest(challengeId, request.id)
        }
    }

    fun reject(request: IncomingJoinRequest) {
        val challengeId = request.challenge.id
        processRequest(request.id) {
            repo.rejectJoinRequest(challengeId, request.id)
        }
    }

    private fun processRequest(
        requestId: String,
        action: suspend () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    processingRequestIds = it.processingRequestIds + requestId,
                    actionError = null
                )
            }
            runCatching { action() }
                .onSuccess {
                    _uiState.update {
                        it.copy(processingRequestIds = it.processingRequestIds - requestId)
                    }
                    load(refresh = true)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            processingRequestIds = it.processingRequestIds - requestId,
                            actionError = throwable.toUserMessage()
                        )
                    }
                }
        }
    }
}

class MyJoinRequestsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ChallengesRepository(app)

    private val _uiState = MutableStateFlow(MyJoinRequestsUiState())
    val uiState: StateFlow<MyJoinRequestsUiState> = _uiState.asStateFlow()

    fun load(refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    state = if (!refresh) ContentState.Loading else it.state,
                    isRefreshing = refresh,
                    actionError = null
                )
            }
            runCatching { repo.getOutgoingJoinRequests() }
                .onSuccess { requests ->
                    _uiState.update {
                        it.copy(
                            state = ContentState.Success(requests),
                            isRefreshing = false
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            state = ContentState.Error(throwable.toUserMessage()),
                            isRefreshing = false
                        )
                    }
                }
        }
    }

    fun cancel(request: OutgoingJoinRequest) {
        val challengeId = request.challenge.id
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    processingChallengeIds = it.processingChallengeIds + challengeId,
                    actionError = null,
                    actionMessage = null
                )
            }
            runCatching { repo.cancelMyJoinRequest(challengeId) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            processingChallengeIds = it.processingChallengeIds - challengeId,
                            actionMessage = "${request.requestType.requestLabel()} annulee."
                        )
                    }
                    load(refresh = true)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            processingChallengeIds = it.processingChallengeIds - challengeId,
                            actionError = throwable.toUserMessage()
                        )
                    }
                }
        }
    }
}

class JoinByCodeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ChallengesRepository(app)

    private val _uiState = MutableStateFlow(JoinByCodeUiState())
    val uiState: StateFlow<JoinByCodeUiState> = _uiState.asStateFlow()

    fun updateCode(value: String) {
        _uiState.update {
            it.copy(
                code = value.uppercase().replace(" ", ""),
                actionError = null,
                lookupState = if (it.lookupState is JoinByCodeLookupState.Error) JoinByCodeLookupState.Idle else it.lookupState
            )
        }
    }

    fun clearPreview() {
        _uiState.update { it.copy(lookupState = JoinByCodeLookupState.Idle, actionError = null) }
    }

    fun lookupChallenge() {
        val code = uiState.value.code.trim()
        if (code.isBlank()) {
            _uiState.update {
                it.copy(
                    lookupState = JoinByCodeLookupState.Error(
                        title = "Code requis",
                        message = "Entre un code pour retrouver un challenge."
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    lookupState = JoinByCodeLookupState.Loading,
                    actionError = null
                )
            }
            runCatching { repo.getChallengeByCode(code) }
                .onSuccess { challenge ->
                    _uiState.update {
                        it.copy(
                            lookupState = JoinByCodeLookupState.Preview(
                                challenge = challenge,
                                availability = challenge.resolveJoinCodeAvailability()
                            )
                        )
                    }
                }
                .onFailure { throwable ->
                    val error = throwable.toJoinByCodeLookupError()
                    _uiState.update {
                        it.copy(
                            lookupState = JoinByCodeLookupState.Error(
                                title = error.first,
                                message = error.second
                            )
                        )
                    }
                }
        }
    }

    fun joinOrOpenPreviewedChallenge(
        onOpenChallenge: (String) -> Unit
    ) {
        val previewState = uiState.value.lookupState as? JoinByCodeLookupState.Preview ?: return
        val challenge = previewState.challenge
        when (previewState.availability) {
            JoinCodeAvailability.ALREADY_JOINED,
            JoinCodeAvailability.JOINED_SUCCESS -> {
                onOpenChallenge(challenge.id)
                return
            }
            JoinCodeAvailability.CHALLENGE_FINISHED,
            JoinCodeAvailability.CHALLENGE_FULL,
            JoinCodeAvailability.ACCESS_RESTRICTED -> return
            JoinCodeAvailability.READY -> Unit
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isJoining = true, actionError = null) }
            runCatching { repo.joinChallenge(challenge.id, uiState.value.code.trim()) }
                .onSuccess { result ->
                    when (result) {
                        is JoinChallengeResult.Joined -> {
                            _uiState.update {
                                it.copy(
                                    isJoining = false,
                                    lookupState = JoinByCodeLookupState.Preview(
                                        challenge = challenge.copy(
                                            participantsCount = challenge.participantsCount + 1,
                                            membershipStatus = MembershipStatus.JOINED,
                                            myRole = ChallengeRole.MEMBER,
                                            joined = true
                                        ),
                                        availability = JoinCodeAvailability.JOINED_SUCCESS
                                    )
                                )
                            }
                        }
                        is JoinChallengeResult.PendingApproval -> {
                            _uiState.update {
                                it.copy(
                                    isJoining = false,
                                    actionError = "Ce challenge demande maintenant une validation du proprietaire."
                                )
                            }
                        }
                    }
                }
                .onFailure { throwable ->
                    val error = throwable.toJoinByCodeJoinError()
                    _uiState.update {
                        it.copy(
                            isJoining = false,
                            actionError = error.second,
                            lookupState = JoinByCodeLookupState.Error(
                                title = error.first,
                                message = error.second
                            )
                        )
                    }
                }
        }
    }
}

private fun Throwable.toUserMessage(): String = when (this) {
    is HttpException -> when (backendErrorCode()) {
        "INVALID_ID" -> "Le backend attend le vrai identifiant technique de l'ami. Choisis ton ami dans la liste au lieu de taper son pseudo."
        "CANNOT_INVITE_SELF" -> "Tu ne peux pas t'inviter toi-meme."
        "USER_NOT_FOUND" -> "Aucun ami correspondant n'a ete trouve."
        "NOT_FRIEND" -> "Cette personne doit etre ton ami avant de recevoir une invitation."
        "ALREADY_PARTICIPANT" -> "Cet ami participe deja a ce challenge."
        "CHALLENGE_FULL" -> "Le challenge a atteint sa limite de participants."
        else -> when (code()) {
            400 -> "La requete n'a pas pu etre traitee."
            401 -> "Ta session a expire. Reconnecte-toi."
            403 -> "Cette action n'est pas autorisee."
            404 -> "Challenge introuvable."
            409 -> "Cette action est deja en cours ou impossible."
            else -> "Une erreur est survenue. Reessaie."
        }
    }
    else -> message ?: "Une erreur reseau est survenue."
}

private fun Throwable.toJoinCodeMessage(): String = when (this) {
    is HttpException -> when (code()) {
        400, 404 -> "Code invalide."
        403 -> "Ce challenge n'est pas accessible."
        409 -> "Le challenge est deja plein ou termine."
        else -> "Impossible de rejoindre ce challenge pour l'instant."
    }
    is IllegalStateException -> "Ce code n'a pas permis de rejoindre directement le challenge."
    else -> message ?: "Impossible de verifier ce code."
}

private fun Throwable.toJoinByCodeLookupError(): Pair<String, String> = when (this) {
    is HttpException -> when (code()) {
        400 -> "Code invalide" to "Verifie le code puis reessaie."
        403 -> "Acces refuse" to "Tu n'as pas acces a ce challenge avec ce compte."
        404 -> "Challenge introuvable" to "Aucun challenge ne correspond a ce code."
        else -> "Impossible de charger le challenge" to "Reessaie dans quelques instants."
    }
    else -> "Connexion impossible" to (message ?: "Verifie ta connexion puis reessaie.")
}

private fun Throwable.toJoinByCodeJoinError(): Pair<String, String> = when (this) {
    is HttpException -> when (code()) {
        400 -> "Code invalide" to "Ce code n'est plus valide pour rejoindre le challenge."
        403 -> "Acces refuse" to "Tu ne peux pas rejoindre ce challenge."
        404 -> "Challenge introuvable" to "Ce challenge n'existe plus."
        409 -> "Challenge indisponible" to "Ce challenge est plein, termine ou deja rejoint."
        else -> "Impossible de rejoindre" to "Reessaie dans quelques instants."
    }
    else -> "Connexion impossible" to (message ?: "Verifie ta connexion puis reessaie.")
}

private fun JoinRequestType.pendingApprovalMessage(): String = when (this) {
    JoinRequestType.REQUEST_ACCESS -> "Demande d'acces envoyee. Le proprietaire doit maintenant l'accepter ou la refuser."
    JoinRequestType.JOIN -> "Demande de participation envoyee. Le proprietaire doit maintenant l'accepter ou la refuser."
}

private fun HttpException.backendErrorCode(): String? =
    response()
        ?.errorBody()
        ?.string()
        ?.let { body ->
            Regex("\"error\"\\s*:\\s*\"([^\"]+)\"")
                .find(body)
                ?.groupValues
                ?.getOrNull(1)
        }

private fun MutableStateFlow<CreateChallengeUiState>.updateField(
    key: String,
    transform: (CreateChallengeUiState) -> CreateChallengeUiState
) {
    update { state ->
        transform(state).copy(fieldErrors = state.fieldErrors - key, submitError = null)
    }
}

val Challenge.isJoinable: Boolean
    get() = membershipStatus == MembershipStatus.NOT_JOINED && status != ChallengeStatus.FINISHED

private fun Challenge.resolveJoinCodeAvailability(): JoinCodeAvailability = when {
    membershipStatus == MembershipStatus.OWNER || membershipStatus == MembershipStatus.JOINED ->
        JoinCodeAvailability.ALREADY_JOINED
    status == ChallengeStatus.FINISHED -> JoinCodeAvailability.CHALLENGE_FINISHED
    maxParticipants != null && participantsCount >= maxParticipants -> JoinCodeAvailability.CHALLENGE_FULL
    else -> JoinCodeAvailability.READY
}

private fun Challenge.toFallbackOverview(): ChallengeOverview =
    ChallengeOverview(
        challenge = this,
        myRole = myRole,
        myEntry = null,
        leaderboardPreview = emptyList(),
        recentMessages = emptyList(),
        pendingInvitationsCount = 0,
        pendingJoinRequestsCount = 0
    )
