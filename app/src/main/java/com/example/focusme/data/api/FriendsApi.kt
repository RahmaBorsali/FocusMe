package com.example.focusme.data.api

import com.example.focusme.presentation.screen.feed.UserDto
import retrofit2.http.GET

interface FriendsApi {
    @GET("users")
    suspend fun getUsers(): List<UserDto>
}
