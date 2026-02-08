package com.example.focusme.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannerTaskDao {

    @Query("SELECT * FROM planner_tasks WHERE dateKey = :dateKey ORDER BY id DESC")
    fun observeTasksByDate(dateKey: String): Flow<List<PlannerTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: PlannerTaskEntity): Long

    @Query("DELETE FROM planner_tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM planner_tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PlannerTaskEntity?

    @Update
    suspend fun update(task: PlannerTaskEntity)
}
