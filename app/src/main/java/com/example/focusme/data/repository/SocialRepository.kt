package com.example.focusme.data.repository

import android.content.Context
import com.example.focusme.data.api.ApiClient
import com.example.focusme.data.api.dto.FeedItem
import com.example.focusme.data.api.dto.FriendStats
import com.example.focusme.data.api.dto.StatsSyncRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.HttpException

class SocialRepository(private val context: Context) {

    companion object {
        private val _latestFeedItems = MutableStateFlow<List<FeedItem>>(emptyList())
        val latestFeedItems: StateFlow<List<FeedItem>> = _latestFeedItems.asStateFlow()
    }

    private val api = ApiClient.socialApi(context)

    suspend fun syncSessionStats(
        userId: String,
        date: String,
        focusMinutes: Int,
        sessionsCount: Int,
        tasksCompleted: Int,
        streak: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        safe {
            val request = StatsSyncRequest(
                userId = userId,
                date = date,
                focusMinutes = focusMinutes,
                sessionsCount = sessionsCount,
                tasksCompleted = tasksCompleted,
                streak = streak
            )
            api.syncStats(request)
            Unit
        }
    }

    suspend fun getFriendsFeed(userId: String, limit: Int = 20): Result<List<FeedItem>> =
        withContext(Dispatchers.IO) {
            safe {
                val response = api.getFriendsFeed(userId, limit)
                val items = response.body() ?: emptyList()
                _latestFeedItems.value = items
                items
            }

        }

    suspend fun getFriendsStats(userId: String): Result<List<FriendStats>> =
        withContext(Dispatchers.IO) {
            safe {
                val response = api.getFriendsStats(userId)
                response.body() ?: emptyList()
            }
        }

    private suspend fun <T> safe(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: HttpException) {
        Result.failure(RuntimeException(e.response()?.errorBody()?.string() ?: e.message()))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
