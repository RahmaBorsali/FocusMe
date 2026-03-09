package com.example.focusme.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    @ColumnInfo(name = "user_id") val userId: String? = null,
    @ColumnInfo(name = "is_done") val isDone: Boolean = false,
    @ColumnInfo(name = "session_id") val sessionId: String? = null,
    @ColumnInfo(name = "due_date") val dueDate: String? = null,
    @ColumnInfo(name = "remote_id") val remoteId: String? = null
)
