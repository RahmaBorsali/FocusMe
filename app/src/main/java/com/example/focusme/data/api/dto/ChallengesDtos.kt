package com.example.focusme.data.api.dto

data class ChallengeDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val creatorId: String? = null,
    val startDate: String,
    val endDate: String,
    val visibility: String? = null,
    val status: String? = null,
    val participantsCount: Int? = null,
    val maxParticipants: Int? = null,
    val goal: ChallengeGoalDto? = null,
    val goalMinutes: Int? = null,
    val joinCode: String? = null,
    val membershipStatus: String? = null,
    val myJoinRequestStatus: String? = null,
    val myJoinRequestId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val myRole: String? = null,
    val joined: Boolean? = null
)

data class ChallengeGoalDto(
    val type: String? = null,
    val targetValue: Int? = null,
    val unit: String? = null
)

data class ChallengeOverviewDto(
    val challenge: ChallengeDto,
    val myRole: String? = null,
    val myEntry: ChallengeEntryDto? = null,
    val leaderboardPreview: List<LeaderboardRowDto>? = null,
    val recentMessages: List<ChallengeMessageDto>? = null,
    val pendingInvitationsCount: Int? = null,
    val pendingJoinRequestsCount: Int? = null
)

data class ChallengeDetailsDto(
    val challenge: ChallengeDto,
    val myRole: String? = null
)

data class ChallengeEntryDto(
    val userId: String? = null,
    val username: String? = null,
    val avatarUrl: String? = null,
    val score: Int? = null,
    val progress: Int? = null,
    val focusMinutes: Int? = null,
    val sessionsCount: Int? = null,
    val tasksCompleted: Int? = null,
    val streak: Int? = null,
    val joinedAt: String? = null
)

data class ChallengeUserDto(
    val id: String? = null,
    val username: String? = null,
    val avatarUrl: String? = null
)

data class ChallengeInvitationDto(
    val id: String,
    val challengeId: String? = null,
    val challenge: ChallengeDto? = null,
    val fromUserId: String? = null,
    val toUserId: String? = null,
    val inviter: ChallengeUserDto? = null,
    val invitee: ChallengeUserDto? = null,
    val username: String? = null,
    val avatarUrl: String? = null,
    val createdAt: String? = null,
    val status: String? = null
)

data class CreateChallengeBody(
    val title: String,
    val description: String? = null,
    val startDate: String,
    val endDate: String,
    val visibility: String,
    val goalType: String,
    val targetValue: Int,
    val maxParticipants: Int? = null,
    val joinCode: String? = null
)

data class JoinBody(
    val joinCode: String? = null
)

data class JoinChallengeResponseDto(
    val ok: Boolean = true,
    val joined: Boolean? = null,
    val status: String? = null,
    val membershipStatus: String? = null,
    val requestId: String? = null
)

data class LeaveChallengeResponseDto(
    val ok: Boolean = true,
    val left: Boolean? = null,
    val cancelledRequest: Boolean? = null,
    val joined: Boolean? = null,
    val membershipStatus: String? = null
)

data class OkResponse(
    val ok: Boolean = true
)

data class LeaderboardRowDto(
    val userId: String? = null,
    val username: String? = null,
    val avatarUrl: String? = null,
    val score: Int? = null,
    val progress: Int? = null,
    val focusMinutes: Int? = null,
    val sessionsCount: Int? = null,
    val tasksCompleted: Int? = null,
    val streak: Int? = null,
    val rank: Int? = null
)

data class ParticipantDto(
    val userId: String? = null,
    val username: String? = null,
    val avatarUrl: String? = null,
    val isOwner: Boolean? = null,
    val joinedAt: String? = null
)

data class ChallengeMessageDto(
    val id: String? = null,
    val userId: String? = null,
    val username: String? = null,
    val avatarUrl: String? = null,
    val text: String? = null,
    val createdAt: String? = null
)

data class MessageCreateBody(
    val text: String
)

data class InviteCreateBody(
    val toUserId: String
)

data class PatchChallengeBody(
    val description: String? = null,
    val endDate: String? = null,
    val visibility: String? = null,
    val targetValue: Int? = null,
    val maxParticipants: Int? = null
)

data class IdResponse(
    val id: String
)

data class JoinRequestChallengeDto(
    val id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val visibility: String? = null,
    val status: String? = null,
    val participantsCount: Int? = null,
    val maxParticipants: Int? = null,
    val goal: ChallengeGoalDto? = null
)

data class IncomingJoinRequestDto(
    val id: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val fromUser: ChallengeUserDto? = null,
    val challenge: JoinRequestChallengeDto? = null
)

data class OutgoingJoinRequestDto(
    val id: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val owner: ChallengeUserDto? = null,
    val challenge: JoinRequestChallengeDto? = null
)

data class ChallengeJoinRequestDto(
    val id: String? = null,
    val fromUserId: String? = null,
    val username: String? = null,
    val avatarUrl: String? = null,
    val createdAt: String? = null,
    val status: String? = null
)
