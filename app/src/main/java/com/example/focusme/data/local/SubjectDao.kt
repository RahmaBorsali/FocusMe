package com.example.focusme.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {

    @Query("SELECT * FROM subjects ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SubjectEntity): Long

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteById(id: Long)
}
