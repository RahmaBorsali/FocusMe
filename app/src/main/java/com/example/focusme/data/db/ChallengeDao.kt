package com.example.focusme.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(challenge: ChallengeEntity): Long

    @Update
    suspend fun update(challenge: ChallengeEntity)

    @Query("DELETE FROM challenges WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM challenges ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<ChallengeEntity>>


}
