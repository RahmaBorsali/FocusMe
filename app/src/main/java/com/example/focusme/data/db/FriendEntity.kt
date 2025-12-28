package com.example.focusme.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val username: String
)
