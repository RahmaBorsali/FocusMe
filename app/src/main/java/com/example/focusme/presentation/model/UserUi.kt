package com.example.focusme.presentation.model

enum class FriendStatus { NONE, FRIEND, OUTGOING_PENDING, INCOMING_PENDING }

data class UserUi(
    val id: String,
    val name: String,
    val username: String,
    val status: FriendStatus = FriendStatus.NONE,
    val incomingRequestId: String? = null
)