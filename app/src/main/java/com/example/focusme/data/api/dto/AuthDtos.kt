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

data class GoogleAuthRequest(
    val idToken: String,
    val mode: String = "login"
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val token: String,
    val password: String,
    val confirmPassword: String
)

data class VerifyEmailRequest(
    val email: String,
    val token: String
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

data class GoogleDataDto(
    val email: String,
    val displayName: String?,
    val avatarUrl: String?,
    val googleSub: String?
)

data class GoogleAuthResponse(
    val accessToken: String? = null,
    val user: UserDto? = null,
    val isNewUser: Boolean? = false,
    val googleData: GoogleDataDto? = null
)
