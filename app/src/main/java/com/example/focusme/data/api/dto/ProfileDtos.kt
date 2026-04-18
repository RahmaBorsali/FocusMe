package com.example.focusme.data.api.dto

data class ProfileResponse(
    val id: String,
    val username: String,
    val email: String,
    val avatarType: String? = null,
    val avatarInitials: String? = null,
    val avatarUrl: String? = null,
    val displayName: String? = null,
    val studyGoal: String? = null,
    val createdAt: String? = null
)

data class UpdateProfileRequest(
    val username: String? = null,
    val displayName: String? = null,
    val studyGoal: String? = null
)
