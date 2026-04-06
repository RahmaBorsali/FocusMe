package com.example.focusme.data.repository

import android.content.Context
import com.example.focusme.data.api.ApiClient
import com.example.focusme.data.api.dto.ChallengeDto
import com.example.focusme.data.api.dto.ChallengeDetailsDto
import com.example.focusme.data.api.dto.ChallengeEntryDto
import com.example.focusme.data.api.dto.ChallengeGoalDto
import com.example.focusme.data.api.dto.ChallengeInvitationDto
import com.example.focusme.data.api.dto.ChallengeJoinRequestDto
import com.example.focusme.data.api.dto.ChallengeMessageDto
import com.example.focusme.data.api.dto.ChallengeOverviewDto
import com.example.focusme.data.api.dto.CreateChallengeBody
import com.example.focusme.data.api.dto.IncomingJoinRequestDto
import com.example.focusme.data.api.dto.InviteCreateBody
import com.example.focusme.data.api.dto.JoinBody
import com.example.focusme.data.api.dto.JoinRequestChallengeDto
import com.example.focusme.data.api.dto.LeaderboardRowDto
import com.example.focusme.data.api.dto.MessageCreateBody
import com.example.focusme.data.api.dto.OutgoingJoinRequestDto
import com.example.focusme.data.api.dto.ParticipantDto
import com.example.focusme.data.local.TokenStore
import retrofit2.HttpException

class ChallengesRepository(
    context: Context
) {
    private val api = ApiClient.challengesApi(context)
    private val tokenStore = TokenStore(context)

    suspend fun getMyChallenges(): List<Challenge> =
        api.mine()
            .map { it.toDomain() }
            .filter { challenge ->
                challenge.membershipStatus == MembershipStatus.OWNER ||
                    challenge.membershipStatus == MembershipStatus.JOINED
            }

    suspend fun getDiscoverChallenges(): List<Challenge> = api.discover().map { it.toDomain() }

    suspend fun getFriendChallenges(): List<Challenge> =
        api.friends()
            .map { it.toDomain() }
            .filter { challenge ->
                challenge.myJoinRequestStatus != JoinRequestStatus.REJECTED &&
                    (
                        challenge.membershipStatus == MembershipStatus.NOT_JOINED ||
                            challenge.membershipStatus == MembershipStatus.PENDING_REQUEST
                        )
            }

    suspend fun getIncomingInvitations(): List<ChallengeInvitation> =
        api.incomingInvitations().map { it.toInvitation() }

    suspend fun getOutgoingInvitations(): List<ChallengeInvitation> =
        api.outgoingInvitations().map { it.toInvitation() }

    suspend fun getIncomingJoinRequests(): List<IncomingJoinRequest> =
        api.incomingJoinRequests()
            .mapNotNull { it.toIncomingJoinRequest() }
            .filter { it.status == JoinRequestStatus.PENDING }

    suspend fun getOutgoingJoinRequests(): List<OutgoingJoinRequest> =
        api.outgoingJoinRequests()
            .mapNotNull { it.toOutgoingJoinRequest() }
            .filter { it.status == JoinRequestStatus.PENDING }

    suspend fun getChallengeDetails(id: String): Challenge = api.details(id).toDomain()

    suspend fun getOverview(id: String): ChallengeOverview = api.overview(id).toOverview()

    suspend fun getChallengeByCode(code: String): Challenge = api.previewByCode(code).toDomain()

    suspend fun createChallenge(input: CreateChallengeInput): Challenge {
        val created = api.create(
            CreateChallengeBody(
                title = input.title.trim(),
                description = input.description.trim().ifBlank { null },
                startDate = input.startDate,
                endDate = input.endDate,
                visibility = input.visibility.apiValue,
                goalType = input.goalType.apiValue,
                targetValue = input.targetValue,
                maxParticipants = input.maxParticipants,
                joinCode = null
            )
        )
        return created.toDomain()
    }

    suspend fun joinChallenge(id: String, code: String? = null): JoinChallengeResult {
        val response = api.join(id, JoinBody(code?.trim()?.ifBlank { null }))
        if (!response.isSuccessful) throw HttpException(response)
        val body = response.body()
        val membershipStatus = body?.membershipStatus.toMembershipStatus(
            joined = body?.joined,
            role = null,
            joinRequestStatus = null
        )
        return if (
            response.code() == 202 ||
            body?.status.equals("pending_approval", ignoreCase = true) ||
            membershipStatus == MembershipStatus.PENDING_REQUEST
        ) {
            JoinChallengeResult.PendingApproval(
                requestId = body?.requestId,
                membershipStatus = membershipStatus
            )
        } else {
            JoinChallengeResult.Joined(membershipStatus = membershipStatus)
        }
    }

    suspend fun leaveChallenge(id: String): LeaveChallengeResult {
        val response = api.leave(id)
        val membershipStatus = response.membershipStatus.toMembershipStatus(
            joined = response.joined,
            role = null,
            joinRequestStatus = null
        )
        return when {
            response.cancelledRequest == true -> LeaveChallengeResult.CancelledRequest
            response.left == true -> LeaveChallengeResult.Left
            else -> when (membershipStatus) {
                MembershipStatus.NOT_JOINED,
                MembershipStatus.PENDING_REQUEST -> LeaveChallengeResult.NotJoined
                MembershipStatus.JOINED -> LeaveChallengeResult.Left
                MembershipStatus.OWNER -> LeaveChallengeResult.NotJoined
            }
        }
    }

    suspend fun getLeaderboard(id: String): List<LeaderboardEntry> {
        val currentUserId = tokenStore.getUserIdBlocking()
        return api.leaderboard(id).mapIndexed { index, dto ->
            dto.toLeaderboardEntry(currentUserId = currentUserId, fallbackRank = index + 1)
        }
    }

    suspend fun getParticipants(id: String): List<ChallengeParticipant> =
        api.participants(id).map { it.toParticipant() }

    suspend fun getChallengeJoinRequests(challengeId: String): List<ChallengeJoinRequest> =
        api.joinRequests(challengeId)
            .mapNotNull { it.toChallengeJoinRequest(challengeId) }
            .filter { it.status == JoinRequestStatus.PENDING }

    suspend fun acceptJoinRequest(challengeId: String, requestId: String) {
        api.acceptJoinRequest(challengeId, requestId)
    }

    suspend fun rejectJoinRequest(challengeId: String, requestId: String) {
        api.rejectJoinRequest(challengeId, requestId)
    }

    suspend fun cancelMyJoinRequest(challengeId: String) {
        api.cancelMyJoinRequest(challengeId)
    }

    suspend fun removeParticipant(challengeId: String, userId: String) {
        api.kick(challengeId, userId)
    }

    suspend fun getMessages(id: String, limit: Int = 30, before: String? = null): List<ChallengeMessage> =
        api.messages(id, limit = limit, before = before).map { it.toMessage() }

    suspend fun sendMessage(id: String, text: String) {
        api.sendMessage(id, MessageCreateBody(text.trim()))
    }

    suspend fun inviteFriend(challengeId: String, userId: String): ChallengeInvitation =
        api.invite(challengeId, InviteCreateBody(userId)).toInvitation()

    suspend fun getChallengeInvitations(challengeId: String): List<ChallengeInvitation> =
        api.invitations(challengeId).map { it.toInvitation() }

    suspend fun acceptInvitation(challengeId: String, invitationId: String) {
        api.acceptInvite(challengeId, invitationId)
    }

    suspend fun rejectInvitation(challengeId: String, invitationId: String) {
        api.rejectInvite(challengeId, invitationId)
    }

    private fun ChallengeOverviewDto.toOverview(): ChallengeOverview =
        ChallengeOverview(
            challenge = challenge.toDomain(myRoleOverride = myRole),
            myRole = myRole.toRole(),
            myEntry = myEntry?.toEntry(),
            leaderboardPreview = leaderboardPreview.orEmpty().mapIndexed { index, dto ->
                dto.toLeaderboardEntry(
                    currentUserId = tokenStore.getUserIdBlocking(),
                    fallbackRank = index + 1
                )
            },
            recentMessages = recentMessages.orEmpty().map { it.toMessage() },
            pendingInvitationsCount = pendingInvitationsCount ?: 0,
            pendingJoinRequestsCount = pendingJoinRequestsCount ?: 0
        )

    private fun ChallengeDetailsDto.toDomain(): Challenge =
        challenge.toDomain(myRoleOverride = myRole)

    private fun ChallengeDto.toDomain(myRoleOverride: String? = null): Challenge {
        val goal = goal.toDomain(goalMinutes)
        val joinRequestStatus = myJoinRequestStatus.toNullableJoinRequestStatus()
        val membership = membershipStatus.toMembershipStatus(
            joined = joined,
            role = myRoleOverride ?: myRole,
            joinRequestStatus = joinRequestStatus
        )
        val resolvedRole = membership.toRole()
        return Challenge(
            id = id,
            title = title,
            description = description.orEmpty(),
            creatorId = creatorId,
            startDate = startDate,
            endDate = endDate,
            visibility = visibility.toVisibility(),
            status = status.toStatus(),
            participantsCount = participantsCount ?: 0,
            maxParticipants = maxParticipants,
            goal = goal,
            joinCode = joinCode,
            membershipStatus = membership,
            myJoinRequestStatus = joinRequestStatus,
            myJoinRequestId = myJoinRequestId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            myRole = resolvedRole,
            joined = membership == MembershipStatus.OWNER || membership == MembershipStatus.JOINED
        )
    }

    private fun ChallengeGoalDto?.toDomain(fallbackMinutes: Int?): ChallengeGoal {
        val fallbackType = if ((this?.type ?: "").isBlank()) {
            if (fallbackMinutes != null) GoalType.FOCUS_MINUTES else GoalType.SESSIONS_COUNT
        } else {
            this?.type.toGoalType()
        }
        val target = this?.targetValue ?: fallbackMinutes ?: 0
        return ChallengeGoal(
            type = fallbackType,
            targetValue = target,
            unit = this?.unit ?: fallbackType.unitLabel
        )
    }

    private fun ChallengeEntryDto.toEntry(): ChallengeEntry =
        ChallengeEntry(
            userId = userId,
            username = username.orEmpty(),
            avatarUrl = avatarUrl,
            score = score ?: focusMinutes ?: tasksCompleted ?: sessionsCount ?: 0,
            progress = progress ?: 0,
            focusMinutes = focusMinutes ?: 0,
            sessionsCount = sessionsCount ?: 0,
            tasksCompleted = tasksCompleted ?: 0,
            streak = streak ?: 0,
            joinedAt = joinedAt
        )

    private fun LeaderboardRowDto.toLeaderboardEntry(
        currentUserId: String?,
        fallbackRank: Int
    ): LeaderboardEntry =
        LeaderboardEntry(
            userId = userId.orEmpty(),
            username = username?.ifBlank { "Membre Focus Me" } ?: "Membre Focus Me",
            avatarUrl = avatarUrl,
            score = score ?: focusMinutes ?: tasksCompleted ?: sessionsCount ?: 0,
            progress = progress ?: 0,
            focusMinutes = focusMinutes ?: 0,
            sessionsCount = sessionsCount ?: 0,
            tasksCompleted = tasksCompleted ?: 0,
            streak = streak ?: 0,
            rank = rank ?: fallbackRank,
            isCurrentUser = currentUserId != null && currentUserId == userId
        )

    private fun ParticipantDto.toParticipant(): ChallengeParticipant =
        ChallengeParticipant(
            userId = userId.orEmpty(),
            username = username?.ifBlank { "Participant" } ?: "Participant",
            avatarUrl = avatarUrl,
            isOwner = isOwner == true,
            joinedAt = joinedAt
        )

    private fun ChallengeMessageDto.toMessage(): ChallengeMessage =
        ChallengeMessage(
            id = id ?: "${createdAt.orEmpty()}-${text.orEmpty().hashCode()}",
            userId = userId.orEmpty(),
            username = username?.ifBlank { "Membre Focus Me" } ?: "Membre Focus Me",
            avatarUrl = avatarUrl,
            text = text?.ifBlank { "Message vide" } ?: "Message vide",
            createdAt = createdAt
        )

    private fun ChallengeInvitationDto.toInvitation(): ChallengeInvitation {
        val nestedChallenge = challenge?.toDomain()
        val inviterName = inviter?.username?.ifBlank { null } ?: username?.ifBlank { null } ?: "Un ami"
        val inviterAvatar = inviter?.avatarUrl ?: avatarUrl
        return ChallengeInvitation(
            id = id,
            challengeId = challengeId ?: nestedChallenge?.id.orEmpty(),
            challenge = nestedChallenge,
            inviterId = fromUserId ?: inviter?.id,
            inviterName = inviterName,
            inviterAvatarUrl = inviterAvatar,
            inviteeId = toUserId ?: invitee?.id,
            inviteeName = invitee?.username?.ifBlank { "" } ?: "",
            inviteeAvatarUrl = invitee?.avatarUrl,
            createdAt = createdAt,
            status = status?.ifBlank { "pending" } ?: "pending"
        )
    }

    private fun IncomingJoinRequestDto.toIncomingJoinRequest(): IncomingJoinRequest? {
        val challengeInfo = challenge?.toJoinRequestChallengeInfo() ?: return null
        return IncomingJoinRequest(
            id = id.orEmpty(),
            status = status.toJoinRequestStatus(),
            createdAt = createdAt,
            fromUserId = fromUser?.id,
            username = fromUser?.username?.ifBlank { "Membre Focus Me" } ?: "Membre Focus Me",
            avatarUrl = fromUser?.avatarUrl,
            challenge = challengeInfo
        )
    }

    private fun OutgoingJoinRequestDto.toOutgoingJoinRequest(): OutgoingJoinRequest? {
        val challengeInfo = challenge?.toJoinRequestChallengeInfo() ?: return null
        return OutgoingJoinRequest(
            id = id.orEmpty(),
            status = status.toJoinRequestStatus(),
            createdAt = createdAt,
            ownerId = owner?.id,
            ownerUsername = owner?.username?.ifBlank { "Owner Focus Me" } ?: "Owner Focus Me",
            ownerAvatarUrl = owner?.avatarUrl,
            challenge = challengeInfo
        )
    }

    private fun ChallengeJoinRequestDto.toChallengeJoinRequest(challengeId: String): ChallengeJoinRequest? =
        id?.let {
            ChallengeJoinRequest(
                id = it,
                challengeId = challengeId,
                fromUserId = fromUserId,
                username = username?.ifBlank { "Membre Focus Me" } ?: "Membre Focus Me",
                avatarUrl = avatarUrl,
                createdAt = createdAt,
                status = status.toJoinRequestStatus()
            )
        }

    private fun JoinRequestChallengeDto.toJoinRequestChallengeInfo(): JoinRequestChallengeInfo? {
        val requestId = id ?: return null
        val goalInfo = if (goal != null) goal.toDomain(fallbackMinutes = null) else null
        return JoinRequestChallengeInfo(
            id = requestId,
            title = title?.ifBlank { "Challenge Focus Me" } ?: "Challenge Focus Me",
            description = description.orEmpty(),
            visibility = visibility.toVisibility(),
            status = status.toStatus(),
            participantsCount = participantsCount ?: 0,
            maxParticipants = maxParticipants,
            goal = goalInfo
        )
    }
}

private val ChallengeVisibility.apiValue: String
    get() = when (this) {
        ChallengeVisibility.PUBLIC -> "public"
        ChallengeVisibility.PRIVATE -> "private"
        ChallengeVisibility.FRIENDS -> "friends"
    }

private val GoalType.apiValue: String
    get() = when (this) {
        GoalType.FOCUS_MINUTES -> "focus_minutes"
        GoalType.SESSIONS_COUNT -> "sessions_count"
        GoalType.TASKS_COMPLETED -> "tasks_completed"
    }

private val GoalType.unitLabel: String
    get() = when (this) {
        GoalType.FOCUS_MINUTES -> "minutes"
        GoalType.SESSIONS_COUNT -> "sessions"
        GoalType.TASKS_COMPLETED -> "tasks"
    }

private fun String?.toStatus(): ChallengeStatus = when (this?.lowercase()) {
    "ongoing" -> ChallengeStatus.ONGOING
    "finished" -> ChallengeStatus.FINISHED
    else -> ChallengeStatus.UPCOMING
}

private fun String?.toVisibility(): ChallengeVisibility = when (this?.lowercase()) {
    "public" -> ChallengeVisibility.PUBLIC
    "friends" -> ChallengeVisibility.FRIENDS
    else -> ChallengeVisibility.PRIVATE
}

private fun String?.toRole(): ChallengeRole = when (this?.lowercase()) {
    "owner" -> ChallengeRole.OWNER
    "member", "participant" -> ChallengeRole.MEMBER
    else -> ChallengeRole.VIEWER
}

private fun MembershipStatus.toRole(): ChallengeRole = when (this) {
    MembershipStatus.OWNER -> ChallengeRole.OWNER
    MembershipStatus.JOINED -> ChallengeRole.MEMBER
    MembershipStatus.PENDING_REQUEST,
    MembershipStatus.NOT_JOINED -> ChallengeRole.VIEWER
}

private fun String?.toJoinRequestStatus(): JoinRequestStatus = when (this?.lowercase()) {
    "accepted" -> JoinRequestStatus.ACCEPTED
    "rejected" -> JoinRequestStatus.REJECTED
    else -> JoinRequestStatus.PENDING
}

private fun String?.toNullableJoinRequestStatus(): JoinRequestStatus? = when (this?.lowercase()) {
    null, "" -> null
    "accepted" -> JoinRequestStatus.ACCEPTED
    "rejected" -> JoinRequestStatus.REJECTED
    else -> JoinRequestStatus.PENDING
}

private fun String?.toMembershipStatus(
    joined: Boolean?,
    role: String?,
    joinRequestStatus: JoinRequestStatus?
): MembershipStatus = when (this?.lowercase()) {
    "owner" -> MembershipStatus.OWNER
    "joined" -> MembershipStatus.JOINED
    "pending_request" -> MembershipStatus.PENDING_REQUEST
    "not_joined" -> MembershipStatus.NOT_JOINED
    else -> {
        when {
            joinRequestStatus == JoinRequestStatus.PENDING -> MembershipStatus.PENDING_REQUEST
            role.equals("owner", ignoreCase = true) -> MembershipStatus.OWNER
            joined == true || role.equals("member", ignoreCase = true) || role.equals("participant", ignoreCase = true) ->
                MembershipStatus.JOINED
            else -> MembershipStatus.NOT_JOINED
        }
    }
}

private fun String?.toGoalType(): GoalType = when (this?.lowercase()) {
    "tasks_completed" -> GoalType.TASKS_COMPLETED
    "sessions_count" -> GoalType.SESSIONS_COUNT
    else -> GoalType.FOCUS_MINUTES
}
