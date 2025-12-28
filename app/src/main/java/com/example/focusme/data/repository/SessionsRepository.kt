package com.example.focusme.data.repository

import android.content.Context
import com.example.focusme.data.local.DbProvider
import com.example.focusme.data.local.StudySessionDao
import com.example.focusme.data.local.StudySessionEntity
import kotlinx.coroutines.flow.Flow

class SessionsRepository(context: Context) {

    private val dao: StudySessionDao = DbProvider.db(context).studySessionDao()

    fun observeSessions(): Flow<List<StudySessionEntity>> = dao.observeAll()
    suspend fun insertSession(entity: StudySessionEntity) = dao.insert(entity)
    suspend fun deleteSession(id: Long) = dao.deleteById(id)
}
