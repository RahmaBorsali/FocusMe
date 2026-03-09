package com.example.focusme.presentation.model

data class FeedItem(
    val friendId: String,
    val friendName: String,
    val avatarUrl: String,
    val actionType: String, // "SESSION", "TASKS", "STREAK"
    val value: Int,
    val message: String,
    val timestamp: String
)
