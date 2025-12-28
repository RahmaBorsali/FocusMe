package com.example.focusme.data.repository

import com.example.focusme.data.db.ChallengeDao
import com.example.focusme.data.db.ChallengeEntity
import kotlinx.coroutines.flow.Flow

class ChallengesRepository(
    private val dao: ChallengeDao
) {
    fun observeAll(): Flow<List<ChallengeEntity>> = dao.observeAll()

    suspend fun create(title: String, description: String) {
        dao.insert(
            ChallengeEntity(
                title = title.trim(),
                description = description.trim(),
                status = "PENDING"
            )
        )
    }

    suspend fun setStatus(ch: ChallengeEntity, status: String) {
        dao.update(ch.copy(status = status))
    }

    suspend fun delete(id: Long) {
        dao.deleteById(id)
    }


}
