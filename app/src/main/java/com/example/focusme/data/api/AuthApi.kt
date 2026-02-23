package com.example.focusme.data.api

import com.example.focusme.data.api.dto.*
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/signup")
    suspend fun signup(@Body req: SignupRequest): SignupResponse

    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): LoginResponse

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body req: ForgotPasswordRequest): Map<String, String>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body req: ResetPasswordRequest): Map<String, String>
}