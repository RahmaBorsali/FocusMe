package com.example.focusme.data.api

import com.example.focusme.data.api.dto.ChatConversationDto
import com.example.focusme.data.api.dto.ChatMessageDto
import com.example.focusme.data.api.dto.CreateConversationBody
import com.example.focusme.data.api.dto.SendChatMessageBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

    @POST("api/chat/messages")
    suspend fun sendMessage(@Body body: SendChatMessageBody): Map<String, Any>

    @POST("api/chat/conversations/{conversationId}/read")
    suspend fun markConversationRead(@Path("conversationId") conversationId: String): Map<String, Any>
}
