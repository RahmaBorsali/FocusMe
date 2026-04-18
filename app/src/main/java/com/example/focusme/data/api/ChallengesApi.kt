package com.example.focusme.data.api

import com.example.focusme.data.api.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ChallengesApi {

    @POST("api/challenges")
    suspend fun create(@Body body: CreateChallengeBody): ChallengeDto

    @GET("api/challenges/discover")
    suspend fun discover(): List<ChallengeDto>

    @GET("api/challenges/friends")
    suspend fun friends(): List<ChallengeDto>

    @GET("api/challenges/mine")
    suspend fun mine(): List<ChallengeDto>

    @GET("api/challenges/invitations/incoming")
    suspend fun incomingInvitations(): List<ChallengeInvitationDto>

    @GET("api/challenges/invitations/outgoing")
    suspend fun outgoingInvitations(): List<ChallengeInvitationDto>

    @GET("api/challenges/requests/incoming")
    suspend fun incomingJoinRequests(): List<IncomingJoinRequestDto>

    @GET("api/challenges/requests/outgoing")
    suspend fun outgoingJoinRequests(): List<OutgoingJoinRequestDto>

    @GET("api/challenges/{id}")
    suspend fun details(@Path("id") id: String): ChallengeDetailsDto

    @GET("api/challenges/{id}/overview")
    suspend fun overview(@Path("id") id: String): ChallengeOverviewDto

    @POST("api/challenges/{id}/join")
    suspend fun join(@Path("id") id: String, @Body body: JoinBody? = null): Response<JoinChallengeResponseDto>

    @POST("api/challenges/{id}/request-access")
    suspend fun requestAccess(@Path("id") id: String): Response<JoinChallengeResponseDto>

    @POST("api/challenges/{id}/leave")
    suspend fun leave(@Path("id") id: String): LeaveChallengeResponseDto

    @GET("api/challenges/{id}/leaderboard")
    suspend fun leaderboard(@Path("id") id: String): List<LeaderboardRowDto>

    @GET("api/challenges/{id}/participants")
    suspend fun participants(@Path("id") id: String): List<ParticipantDto>

    @GET("api/challenges/{id}/requests")
    suspend fun joinRequests(@Path("id") id: String): List<ChallengeJoinRequestDto>

    @POST("api/challenges/{id}/requests/{requestId}/accept")
    suspend fun acceptJoinRequest(@Path("id") id: String, @Path("requestId") requestId: String): OkResponse

    @POST("api/challenges/{id}/requests/{requestId}/reject")
    suspend fun rejectJoinRequest(@Path("id") id: String, @Path("requestId") requestId: String): OkResponse

    @DELETE("api/challenges/{id}/my-request")
    suspend fun cancelMyJoinRequest(@Path("id") id: String): OkResponse

    @DELETE("api/challenges/{id}/participants/{userId}")
    suspend fun kick(@Path("id") id: String, @Path("userId") userId: String): OkResponse

    @GET("api/challenges/{id}/messages")
    suspend fun messages(
        @Path("id") id: String,
        @Query("limit") limit: Int? = null,
        @Query("before") before: String? = null
    ): List<ChallengeMessageDto>

    @Multipart
    @POST("api/challenges/{id}/upload")
    suspend fun uploadFile(@Path("id") id: String, @Part file: MultipartBody.Part): AttachmentDto

    @POST("api/challenges/{id}/messages")
    suspend fun sendMessage(@Path("id") id: String, @Body body: MessageCreateBody): IdResponse

    @GET("api/challenges/code/{code}")
    suspend fun previewByCode(@Path("code") code: String): ChallengeDto

    @PATCH("api/challenges/{id}")
    suspend fun update(@Path("id") id: String, @Body body: PatchChallengeBody): ChallengeDto

    @DELETE("api/challenges/{id}")
    suspend fun delete(@Path("id") id: String): OkResponse

    @POST("api/challenges/{id}/invite")
    suspend fun invite(@Path("id") id: String, @Body body: InviteCreateBody): ChallengeInvitationDto

    @GET("api/challenges/{id}/invitations")
    suspend fun invitations(@Path("id") id: String): List<ChallengeInvitationDto>

    @POST("api/challenges/{id}/invitations/{invId}/accept")
    suspend fun acceptInvite(@Path("id") id: String, @Path("invId") invId: String): OkResponse

    @POST("api/challenges/{id}/invitations/{invId}/reject")
    suspend fun rejectInvite(@Path("id") id: String, @Path("invId") invId: String): OkResponse
}
