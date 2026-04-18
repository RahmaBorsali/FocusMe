package com.example.focusme.data.repository

import android.content.Context
import com.example.focusme.data.api.ApiClient
import com.example.focusme.data.api.dto.AttachmentDto
import com.example.focusme.data.api.dto.ChatConversationDto
import com.example.focusme.data.api.dto.ChatConversationLastMessageDto
import com.example.focusme.data.api.dto.ChatMessageDto
import com.example.focusme.data.api.dto.ChatUserDto
import com.example.focusme.data.api.dto.CreateConversationBody
import com.example.focusme.data.api.dto.SendChatMessageBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

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

    suspend fun sendMessage(conversationId: String, text: String? = null, attachment: ChatAttachment? = null) {
        api.sendMessage(
            SendChatMessageBody(
                conversationId = conversationId,
                text = text?.trim(),
                attachment = attachment?.toDto()
            )
        )
    }

    suspend fun uploadFile(file: File): ChatAttachment {
        val requestFile = file.asRequestBody(null)
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        return api.uploadFile(body).toDomain()
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
            text = text?.trim(),
            attachment = attachment?.toDomain(),
            createdAt = createdAt,
            readAt = readAt
        )

    private fun AttachmentDto.toDomain(): ChatAttachment =
        ChatAttachment(
            url = url,
            type = type,
            fileName = fileName,
            fileSize = fileSize
        )

    private fun ChatAttachment.toDto(): AttachmentDto =
        AttachmentDto(
            url = url,
            type = type,
            fileName = fileName,
            fileSize = fileSize
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
