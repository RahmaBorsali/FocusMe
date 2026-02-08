package com.example.focusme.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planner_tasks")
data class PlannerTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateKey: String,
    val title: String,
    val description: String,
    val minutes: Int,
    val priority: Int,
    val subjectId: Long
)
