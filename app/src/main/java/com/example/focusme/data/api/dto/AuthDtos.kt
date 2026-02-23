package com.example.focusme.data.api.dto

data class SignupRequest(
    val username: String,
    val email: String,
    val password: String,
    val confirmPassword: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val token: String,
    val password: String,
    val confirmPassword: String
)

data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    val avatarType: String? = null,
    val avatarInitials: String? = null,
    val avatarUrl: String? = null
)

data class LoginResponse(
    val accessToken: String,
    val user: UserDto
)

data class SignupResponse(
    val message: String,
    val user: UserDto
)

data class ApiError(
    val error: String? = null,
    val message: String? = null,
    val details: Any? = null
)