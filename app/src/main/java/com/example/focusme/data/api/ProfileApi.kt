package com.example.focusme.data.api

import com.example.focusme.data.api.dto.ProfileResponse
import com.example.focusme.data.api.dto.UpdateProfileRequest
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part

interface ProfileApi {
    @GET("profile/me")
    suspend fun me(): ProfileResponse

    @PATCH("profile/me")
    suspend fun updateMe(@Body req: UpdateProfileRequest): ProfileResponse

    @Multipart
    @POST("profile/avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): ProfileResponse

    @DELETE("profile/me")
    suspend fun deleteMe(): Map<String, String>
}
