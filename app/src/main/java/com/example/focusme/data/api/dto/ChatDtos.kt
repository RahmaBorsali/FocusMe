package com.example.focusme.data.api.dto

data class ChatUserDto(
    val id: String,
    val username: String? = null,
    val email: String? = null,
    val avatarType: String? = null,
    val avatarInitials: String? = null,
    val avatarUrl: String? = null
)

data class ChatConversationLastMessageDto(
    val text: String? = null,
    val senderId: String? = null,
    val createdAt: String? = null
)

data class ChatConversationDto(
    val id: String,
    val peer: ChatUserDto,
    val unreadCount: Int? = null,
    val lastMessage: ChatConversationLastMessageDto? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class CreateConversationBody(
    val targetUserId: String
)

data class ChatMessageDto(
    val id: String,
    val conversationId: String,
    val sender: ChatUserDto? = null,
    val recipient: ChatUserDto? = null,
    val text: String? = null,
    val createdAt: String? = null,
    val readAt: String? = null
)

data class SendChatMessageBody(
    val conversationId: String? = null,
    val targetUserId: String? = null,
    val text: String
)
