package com.example.focusme.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Query("UPDATE tasks SET is_done = 1 WHERE id = :taskId")
    suspend fun markAsDone(taskId: Long)

    @Query("UPDATE tasks SET due_date = :newDate, is_done = 0 WHERE id = :taskId")
    suspend fun postponeTask(taskId: Long, newDate: String)

    @Query("SELECT * FROM tasks WHERE session_id = :sessionId ORDER BY id ASC")
    fun getBySession(sessionId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getById(taskId: Long): TaskEntity?

    @androidx.room.Update
    suspend fun update(task: TaskEntity): Int

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteById(taskId: Long)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}
