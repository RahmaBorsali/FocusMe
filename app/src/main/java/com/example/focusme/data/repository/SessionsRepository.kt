package com.example.focusme.data.repository

import android.content.Context
import com.example.focusme.data.api.ApiClient
import com.example.focusme.data.api.dto.SessionSaveRequest
import com.example.focusme.data.local.DbProvider
import com.example.focusme.data.local.StudySessionDao
import com.example.focusme.data.local.StudySessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import android.util.Log
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class SessionsRepository(private val context: Context) {

    private val dao: StudySessionDao = DbProvider.db(context).studySessionDao()
    private val api = ApiClient.socialApi(context)

    fun observeSessions(): Flow<List<StudySessionEntity>> = dao.observeAll()

    suspend fun insertLocal(entity: StudySessionEntity): Long = withContext(Dispatchers.IO) {
        try {
            val id = dao.insert(entity)
            Log.d("SessionsRepository", "Local session save successful: $id")
            id
        } catch (e: Exception) {
            Log.e("SessionsRepository", "Local session save failed", e)
            -1L
        }
    }

    suspend fun syncRemote(
        durationSeconds: Int,
        userId: String? = null,
        taskTitles: List<String> = emptyList(),
        taskIds: List<String> = emptyList()
    ) = withContext(Dispatchers.IO) {
        if (!userId.isNullOrBlank() && userId != "local") {
            try {
                val dateStr = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date.toString()

                val request = SessionSaveRequest(
                    userId = userId,
                    durationMinutes = (durationSeconds / 60).coerceAtLeast(1),
                    taskIds = taskIds,
                    completedTaskTitles = taskTitles,
                    date = dateStr
                )
                
                val response = api.saveSession(request)
                if (response.isSuccessful) {
                    Log.d("SessionsRepository", "Backend session save successful")
                } else {
                    Log.e("SessionsRepository", "Backend session save failed: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("SessionsRepository", "Backend session sync exception", e)
            }
        }
    }



    suspend fun deleteSession(id: Long) = dao.deleteById(id)
}

