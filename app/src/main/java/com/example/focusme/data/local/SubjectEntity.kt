package com.example.focusme.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val label: String,
    val emoji: String,
    val colorArgb: Long,
    val createdAt: Long = System.currentTimeMillis()
)
