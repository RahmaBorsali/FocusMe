package com.example.focusme.data.repository

import android.content.Context
import com.example.focusme.data.api.ApiClient
import com.example.focusme.data.api.dto.ChatConversationDto
import com.example.focusme.data.api.dto.ChatConversationLastMessageDto
import com.example.focusme.data.api.dto.ChatMessageDto
import com.example.focusme.data.api.dto.ChatUserDto
import com.example.focusme.data.api.dto.CreateConversationBody
import com.example.focusme.data.api.dto.SendChatMessageBody

class ChatRepository(
    context: Context
) {
    private val api = ApiClient.chatApi(context)

    suspend fun getConversations(): List<ChatConversation> =
        api.getConversations().map { it.toDomain() }

    suspend fun getOrCreateConversation(targetUserId: String): ChatConversation =
        api.getOrCreateConversation(CreateConversationBody(targetUserId)).toDomain()

    suspend fun getMessages(
        conversationId: String,
        limit: Int = 50,
        before: String? = null
    ): List<DirectChatMessage> =
        api.getMessages(conversationId, limit = limit, before = before)
            .map { it.toDomain() }
            .sortedBy { it.createdAt.orEmpty() }

    suspend fun sendMessage(conversationId: String, text: String) {
        api.sendMessage(
            SendChatMessageBody(
                conversationId = conversationId,
                text = text.trim()
            )
        )
    }

    suspend fun markConversationRead(conversationId: String) {
        api.markConversationRead(conversationId)
    }

    private fun ChatConversationDto.toDomain(): ChatConversation =
        ChatConversation(
            id = id,
            peer = peer.toDomain(),
            unreadCount = unreadCount ?: 0,
            lastMessage = lastMessage?.toDomain(),
            createdAt = createdAt,
            updatedAt = updatedAt
        )

    private fun ChatConversationLastMessageDto.toDomain(): ChatConversationLastMessage =
        ChatConversationLastMessage(
            text = text?.ifBlank { "Message vide" } ?: "Message vide",
            senderId = senderId.orEmpty(),
            createdAt = createdAt
        )

    private fun ChatMessageDto.toDomain(): DirectChatMessage =
        DirectChatMessage(
            id = id,
            conversationId = conversationId,
            sender = (sender ?: ChatUserDto(id = "")).toDomain(),
            recipient = (recipient ?: ChatUserDto(id = "")).toDomain(),
            text = text?.trim().takeUnless { it.isNullOrBlank() } ?: "Message vide",
            createdAt = createdAt,
            readAt = readAt
        )

    private fun ChatUserDto.toDomain(): ChatUser =
        ChatUser(
            id = id,
            username = username?.trim().takeUnless { it.isNullOrBlank() } ?: "Ami",
            email = email.orEmpty(),
            avatarType = avatarType.orEmpty(),
            avatarInitials = avatarInitials?.trim().takeUnless { it.isNullOrBlank() } ?: username.orEmpty().take(2).uppercase(),
            avatarUrl = avatarUrl.orEmpty()
        )
}
