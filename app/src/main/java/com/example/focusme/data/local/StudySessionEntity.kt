package com.example.focusme.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val durationSeconds: Int,
    val tasksCount: Int,
    val xpPoints: Int,
    val focusRate: Int,
    val satisfactionRate: Int,
    val visibility: String,
    val createdAtMillis: Long
)
