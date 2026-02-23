package com.example.focusme.data.api

import com.example.focusme.data.api.dto.*
import retrofit2.http.*

interface FriendsApi {
    @GET("users/search")
    suspend fun searchUsers(@Query("q") q: String): List<UserDto>

    @POST("friends/request")
    suspend fun sendRequest(@Body body: FriendRequestCreate): FriendRequestResult

    @GET("friends/requests/incoming")
    suspend fun incoming(): List<IncomingRequestItem>

    @GET("friends/requests/outgoing")
    suspend fun outgoing(): List<OutgoingRequestItem>

    @POST("friends/requests/{id}/accept")
    suspend fun accept(@Path("id") requestId: String): Map<String, Any>

    @POST("friends/requests/{id}/reject")
    suspend fun reject(@Path("id") requestId: String): Map<String, Any>

    @GET("friends")
    suspend fun friends(): List<UserDto>

    @DELETE("friends/{friendUserId}")
    suspend fun deleteFriend(@Path("friendUserId") friendUserId: String): Map<String, Any>

    @GET("users/suggestions")
    suspend fun suggestions(): List<UserDto>

}