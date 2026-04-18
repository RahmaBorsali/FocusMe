package com.example.focusme.data.api

import com.example.focusme.data.api.dto.AttachmentDto
import com.example.focusme.data.api.dto.ChatConversationDto
import com.example.focusme.data.api.dto.ChatMessageDto
import com.example.focusme.data.api.dto.CreateConversationBody
import com.example.focusme.data.api.dto.SendChatMessageBody
import okhttp3.MultipartBody
import retrofit2.http.*

interface ChatApi {

    @GET("api/chat/conversations")
    suspend fun getConversations(): List<ChatConversationDto>

    @POST("api/chat/conversations")
    suspend fun getOrCreateConversation(@Body body: CreateConversationBody): ChatConversationDto

    @GET("api/chat/conversations/{conversationId}/messages")
    suspend fun getMessages(
        @Path("conversationId") conversationId: String,
        @Query("limit") limit: Int = 50,
        @Query("before") before: String? = null
    ): List<ChatMessageDto>

    @Multipart
    @POST("api/chat/upload")
    suspend fun uploadFile(@Part file: MultipartBody.Part): AttachmentDto

    @POST("api/chat/messages")
    suspend fun sendMessage(@Body body: SendChatMessageBody): Map<String, Any>

    @POST("api/chat/conversations/{conversationId}/read")
    suspend fun markConversationRead(@Path("conversationId") conversationId: String): Map<String, Any>
}
