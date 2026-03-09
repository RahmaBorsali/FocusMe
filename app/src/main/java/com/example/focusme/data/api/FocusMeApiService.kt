package com.example.focusme.data.api

import com.example.focusme.data.api.dto.FeedItem
import com.example.focusme.data.api.dto.FriendStats
import com.example.focusme.data.api.dto.SessionSaveRequest
import com.example.focusme.data.api.dto.StatsSyncRequest
import com.example.focusme.data.api.dto.TaskCreateRequest
import com.example.focusme.data.api.dto.TaskCreateResponse
import retrofit2.Response
import retrofit2.http.*

interface FocusMeApiService {

    @PATCH("api/tasks/{taskId}/complete")
    suspend fun completeTask(@Path("taskId") taskId: String): Response<Unit>

    @POST("api/tasks")
    suspend fun createTask(@Body request: TaskCreateRequest): Response<TaskCreateResponse>

    @PATCH("api/tasks/{taskId}/postpone")
    suspend fun postponeTask(
        @Path("taskId") taskId: String,
        @Body body: Map<String, String>
    ): Response<Unit>

    @POST("api/stats/sync")
    suspend fun syncStats(@Body request: StatsSyncRequest): Response<Map<String, Any>>

    @POST("api/sessions")
    suspend fun saveSession(@Body request: SessionSaveRequest): Response<Map<String, Any>>

    @GET("api/feed/{userId}")
    suspend fun getFriendsFeed(
        @Path("userId") userId: String,
        @Query("limit") limit: Int = 20
    ): Response<List<FeedItem>>

    @GET("api/stats/friends/{userId}")
    suspend fun getFriendsStats(
        @Path("userId") userId: String
    ): Response<List<FriendStats>>
}
