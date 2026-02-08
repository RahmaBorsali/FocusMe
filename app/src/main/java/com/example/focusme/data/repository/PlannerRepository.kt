package com.example.focusme.data.repository

import android.content.Context
import com.example.focusme.data.local.DbProvider
import com.example.focusme.data.local.PlannerTaskEntity
import kotlinx.coroutines.flow.Flow

class PlannerRepository(context: Context) {
    private val dao = DbProvider.get(context).plannerTaskDao()

    fun observeByDate(dateKey: String): Flow<List<PlannerTaskEntity>> =
        dao.observeTasksByDate(dateKey)

    suspend fun insert(task: PlannerTaskEntity) =
        dao.insert(task)

    suspend fun deleteById(id: Long) =
        dao.deleteById(id)

    suspend fun getById(id: Long): PlannerTaskEntity? =
        dao.getById(id)

    suspend fun update(task: PlannerTaskEntity) =
        dao.update(task)
}
