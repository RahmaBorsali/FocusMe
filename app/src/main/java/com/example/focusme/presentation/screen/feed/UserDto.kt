package com.example.focusme.presentation.screen.feed

data class UserDto(
    val id: Int,
    val name: String,
    val username: String,
    val isFriend: Boolean = false
)
