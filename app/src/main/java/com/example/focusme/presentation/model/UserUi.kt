package com.example.focusme.presentation.model

data class UserUi(
    val id: String,
    val name: String,
    val username: String,
    val isFriend: Boolean = false
)