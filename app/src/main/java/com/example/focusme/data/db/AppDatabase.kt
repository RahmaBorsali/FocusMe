package com.example.focusme.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.focusme.data.local.StudySessionDao
import com.example.focusme.data.local.StudySessionEntity

@Database(
    entities = [FriendEntity::class, StudySessionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun friendsDao(): FriendsDao
    abstract fun studySessionDao(): StudySessionDao
}
