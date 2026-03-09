package com.example.focusme.data.repository

import android.content.Context
import android.util.Log
import com.example.focusme.data.api.ApiClient
import com.example.focusme.data.local.DbProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

import com.example.focusme.data.local.TokenStore

class TaskRepository(private val context: Context) {

    private val dao = DbProvider.get(context).taskDao()
    private val api = ApiClient.socialApi(context)
    private val userId = TokenStore(context).getUserIdBlocking()

    suspend fun completeTask(taskId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val task = dao.getById(taskId) ?: return@withContext Result.failure(RuntimeException("Tâche introuvable"))
            
            // 1. Local update
            dao.markAsDone(taskId)
            
            // 2. Backend sync using remoteId
            val remoteId = task.remoteId
            if (!remoteId.isNullOrEmpty()) {
                val response = api.completeTask(remoteId)
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(RuntimeException("Erreur serveur : ${response.code()}"))
            } else {
                Result.success(Unit) // No remote ID yet, just local success
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", "completeTask sync failed", e)
            Result.success(Unit)
        }
    }

    suspend fun postponeTask(taskId: Long, newDate: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val task = dao.getById(taskId) ?: return@withContext Result.failure(RuntimeException("Tâche introuvable"))

            // 1. Local update
            dao.postponeTask(taskId, newDate)
            
            // 2. Backend sync using remoteId
            val remoteId = task.remoteId
            if (!remoteId.isNullOrEmpty()) {
                val response = api.postponeTask(remoteId, mapOf("newDate" to newDate))
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(RuntimeException("Erreur serveur : ${response.code()}"))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", "postponeTask sync failed", e)
            Result.success(Unit)
        }
    }
}

