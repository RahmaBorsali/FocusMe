package com.example.focusme.data.repository

import android.content.Context
import com.example.focusme.data.local.SubjectDao
import com.example.focusme.data.local.SubjectEntity
import com.example.focusme.data.local.DbProvider
import kotlinx.coroutines.flow.Flow

class SubjectRepository(context: Context) {
    private val dao: SubjectDao = DbProvider.db(context).subjectDao()

    fun observeAll(): Flow<List<SubjectEntity>> = dao.observeAll()

    suspend fun insert(entity: SubjectEntity): Long = dao.insert(entity)

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
