package com.example.focusme.data.repository

import android.content.Context
import com.example.focusme.data.api.ApiClient
import com.example.focusme.data.api.dto.TaskCreateRequest
import com.example.focusme.data.local.DbProvider
import com.example.focusme.data.local.PlannerTaskEntity
import com.example.focusme.data.local.TokenStore
import android.util.Log
import kotlinx.coroutines.flow.Flow

class PlannerRepository(private val context: Context) {
    private val dao = DbProvider.get(context).plannerTaskDao()
    private val api = ApiClient.socialApi(context)
    private val userId = TokenStore(context).getUserIdBlocking()

    fun observeByDate(dateKey: String): Flow<List<PlannerTaskEntity>> =
        dao.observeTasksByDate(dateKey)

    suspend fun insert(task: PlannerTaskEntity): Long {
        // 1. Local (including userId if available)
        val newId = dao.insert(task.copy(userId = userId))

        // 2. Backend
        try {
            val response = api.createTask(TaskCreateRequest(
                title = task.title,
                userId = userId,
                isDone = false,
                dueDate = task.dateKey
            ))
            if (response.isSuccessful) {
                val remoteId = response.body()?.id
                if (remoteId != null) {
                    // Update local with remoteId
                    val updated = dao.getById(newId)
                    if (updated != null) {
                        dao.update(updated.copy(remoteId = remoteId))
                    }
                }
            } else {
                Log.e("PlannerRepository", "Backend task create failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("PlannerRepository", "Backend task create exception", e)
        }
        return newId
    }

    suspend fun deleteById(id: Long) =
        dao.deleteById(id)

    suspend fun getById(id: Long): PlannerTaskEntity? =
        dao.getById(id)

    suspend fun update(task: PlannerTaskEntity) =
        dao.update(task)

    suspend fun completeTask(id: Long, isDone: Boolean) {
        val task = dao.getById(id) ?: return
        dao.update(task.copy(isDone = isDone))

        try {
            val remoteId = task.remoteId
            if (!remoteId.isNullOrEmpty() && isDone) {
                api.completeTask(remoteId)
            }
        } catch (e: Exception) {
            Log.e("PlannerRepository", "completeTask sync failed", e)
        }
    }

    suspend fun postponeTask(id: Long, newDateKey: String) {
        val task = dao.getById(id) ?: return
        
        // 1. Update locally
        dao.update(task.copy(dateKey = newDateKey, isDone = false))

        // 2. Update backend
        try {
            val remoteId = task.remoteId
            if (!remoteId.isNullOrEmpty()) {
                api.postponeTask(remoteId, mapOf("newDate" to newDateKey))
            }
        } catch (e: Exception) {
            Log.e("PlannerRepository", "postponeTask sync failed", e)
        }
    }
}


