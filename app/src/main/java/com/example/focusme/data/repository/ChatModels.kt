package com.example.focusme.data.repository

data class ChatUser(
    val id: String,
    val username: String,
    val email: String,
    val avatarType: String,
    val avatarInitials: String,
    val avatarUrl: String
)

data class ChatAttachment(
    val url: String,
    val type: String,
    val fileName: String,
    val fileSize: Long
)

data class ChatConversationLastMessage(
    val text: String,
    val senderId: String,
    val createdAt: String?
)

data class ChatConversation(
    val id: String,
    val peer: ChatUser,
    val unreadCount: Int,
    val lastMessage: ChatConversationLastMessage?,
    val createdAt: String?,
    val updatedAt: String?
)

data class DirectChatMessage(
    val id: String,
    val conversationId: String,
    val sender: ChatUser,
    val recipient: ChatUser,
    val text: String? = null,
    val attachment: ChatAttachment? = null,
    val createdAt: String?,
    val readAt: String?
)
