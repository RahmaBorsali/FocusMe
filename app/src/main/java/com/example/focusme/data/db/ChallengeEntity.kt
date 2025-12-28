package com.example.focusme.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val status: String, // "PENDING" | "ACTIVE" | "FINISHED"
    val createdAtMillis: Long = System.currentTimeMillis()
)
