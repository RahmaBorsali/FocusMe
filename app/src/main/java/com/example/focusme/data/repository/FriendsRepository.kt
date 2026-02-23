package com.example.focusme.data.repository

import android.content.Context
import com.example.focusme.data.api.ApiClient
import com.example.focusme.data.api.dto.*
import retrofit2.HttpException

class FriendsRepository(context: Context) {
    private val api = ApiClient.friendsApi(context)

    suspend fun search(q: String): Result<List<UserDto>> = safe { api.searchUsers(q) }
    suspend fun sendRequest(toUserId: String): Result<FriendRequestResult> = safe { api.sendRequest(FriendRequestCreate(toUserId)) }
    suspend fun incoming(): Result<List<IncomingRequestItem>> = safe { api.incoming() }
    suspend fun outgoing(): Result<List<OutgoingRequestItem>> = safe { api.outgoing() }
    suspend fun accept(requestId: String): Result<Unit> = safe { api.accept(requestId); Unit }
    suspend fun reject(requestId: String): Result<Unit> = safe { api.reject(requestId); Unit }
    suspend fun friends(): Result<List<UserDto>> = safe { api.friends() }
    suspend fun deleteFriend(friendId: String): Result<Unit> = safe { api.deleteFriend(friendId); Unit }
    suspend fun suggestions(): Result<List<UserDto>> = safe { api.suggestions() }

    private suspend fun <T> safe(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: HttpException) {
        Result.failure(RuntimeException(e.response()?.errorBody()?.string() ?: e.message()))
    } catch (e: Exception) {
        Result.failure(e)
    }

}