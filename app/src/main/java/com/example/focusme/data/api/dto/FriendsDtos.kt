package com.example.focusme.data.api.dto

data class FriendRequestCreate(val toUserId: String)

data class FriendRequestResult(val id: String, val status: String)

data class IncomingRequestItem(
    val requestId: String,
    val fromUser: UserDto?,
    val createdAt: String
)

data class OutgoingRequestItem(
    val requestId: String,
    val toUser: UserDto?,
    val createdAt: String
)