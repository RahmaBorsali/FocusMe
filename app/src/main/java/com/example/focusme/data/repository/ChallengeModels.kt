package com.example.focusme.data.repository

enum class ChallengeStatus {
    UPCOMING,
    ONGOING,
    FINISHED
}

enum class ChallengeVisibility {
    PUBLIC,
    PRIVATE,
    FRIENDS
}

enum class GoalType {
    FOCUS_MINUTES,
    SESSIONS_COUNT,
    TASKS_COMPLETED
}

enum class ChallengeRole {
    OWNER,
    MEMBER,
    VIEWER
}

enum class JoinRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

enum class JoinRequestType {
    JOIN,
    REQUEST_ACCESS
}

enum class ChallengeInviteKind {
    INVITE,
    JOIN_REQUEST
}

enum class MembershipStatus {
    OWNER,
    JOINED,
    PENDING_REQUEST,
    NOT_JOINED
}

data class ChallengeGoal(
    val type: GoalType,
    val targetValue: Int,
    val unit: String
)

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val creatorId: String?,
    val startDate: String,
    val endDate: String,
    val visibility: ChallengeVisibility,
    val status: ChallengeStatus,
    val participantsCount: Int,
    val maxParticipants: Int?,
    val goal: ChallengeGoal,
    val joinCode: String?,
    val membershipStatus: MembershipStatus,
    val myJoinRequestStatus: JoinRequestStatus?,
    val myJoinRequestId: String?,
    val myJoinRequestType: JoinRequestType?,
    val createdAt: String?,
    val updatedAt: String?,
    val myRole: ChallengeRole,
    val joined: Boolean
)

data class ChallengeEntry(
    val userId: String?,
    val username: String,
    val avatarUrl: String?,
    val score: Int,
    val progress: Int,
    val focusMinutes: Int,
    val sessionsCount: Int,
    val tasksCompleted: Int,
    val streak: Int,
    val joinedAt: String?
)

data class LeaderboardEntry(
    val userId: String,
    val username: String,
    val avatarUrl: String?,
    val score: Int,
    val progress: Int,
    val focusMinutes: Int,
    val sessionsCount: Int,
    val tasksCompleted: Int,
    val streak: Int,
    val rank: Int,
    val isCurrentUser: Boolean
)

data class ChallengeParticipant(
    val userId: String,
    val username: String,
    val avatarUrl: String?,
    val isOwner: Boolean,
    val joinedAt: String?
)

data class ChallengeMessage(
    val id: String,
    val userId: String,
    val username: String,
    val avatarUrl: String?,
    val text: String,
    val createdAt: String?
)

data class ChallengeInvitation(
    val id: String,
    val challengeId: String,
    val kind: ChallengeInviteKind,
    val requestType: JoinRequestType?,
    val challenge: Challenge?,
    val inviterId: String?,
    val inviterName: String,
    val inviterAvatarUrl: String?,
    val inviteeId: String?,
    val inviteeName: String,
    val inviteeAvatarUrl: String?,
    val createdAt: String?,
    val decisionAt: String?,
    val status: String
)

data class ChallengeOverview(
    val challenge: Challenge,
    val myRole: ChallengeRole,
    val myEntry: ChallengeEntry?,
    val leaderboardPreview: List<LeaderboardEntry>,
    val recentMessages: List<ChallengeMessage>,
    val pendingInvitationsCount: Int,
    val pendingJoinRequestsCount: Int
)

sealed interface JoinChallengeResult {
    val membershipStatus: MembershipStatus

    data class Joined(
        override val membershipStatus: MembershipStatus = MembershipStatus.JOINED
    ) : JoinChallengeResult

    data class PendingApproval(
        val requestId: String?,
        val requestType: JoinRequestType,
        override val membershipStatus: MembershipStatus = MembershipStatus.PENDING_REQUEST
    ) : JoinChallengeResult
}

sealed interface LeaveChallengeResult {
    val membershipStatus: MembershipStatus

    data object Left : LeaveChallengeResult {
        override val membershipStatus: MembershipStatus = MembershipStatus.NOT_JOINED
    }

    data object CancelledRequest : LeaveChallengeResult {
        override val membershipStatus: MembershipStatus = MembershipStatus.NOT_JOINED
    }

    data object NotJoined : LeaveChallengeResult {
        override val membershipStatus: MembershipStatus = MembershipStatus.NOT_JOINED
    }
}

data class JoinRequestChallengeInfo(
    val id: String,
    val title: String,
    val description: String,
    val visibility: ChallengeVisibility,
    val status: ChallengeStatus,
    val participantsCount: Int,
    val maxParticipants: Int?,
    val goal: ChallengeGoal?
)

data class IncomingJoinRequest(
    val id: String,
    val challengeId: String,
    val kind: ChallengeInviteKind,
    val requestType: JoinRequestType,
    val status: JoinRequestStatus,
    val createdAt: String?,
    val decisionAt: String?,
    val fromUserId: String?,
    val username: String,
    val avatarUrl: String?,
    val challenge: JoinRequestChallengeInfo
)

data class OutgoingJoinRequest(
    val id: String,
    val challengeId: String,
    val kind: ChallengeInviteKind,
    val requestType: JoinRequestType,
    val status: JoinRequestStatus,
    val createdAt: String?,
    val decisionAt: String?,
    val ownerId: String?,
    val ownerUsername: String,
    val ownerAvatarUrl: String?,
    val challenge: JoinRequestChallengeInfo
)

data class ChallengeJoinRequest(
    val id: String,
    val challengeId: String,
    val kind: ChallengeInviteKind,
    val requestType: JoinRequestType,
    val fromUserId: String?,
    val username: String,
    val avatarUrl: String?,
    val createdAt: String?,
    val decisionAt: String?,
    val status: JoinRequestStatus
)

data class CreateChallengeInput(
    val title: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val visibility: ChallengeVisibility,
    val goalType: GoalType,
    val targetValue: Int,
    val maxParticipants: Int?
)
