package com.example.focusme.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.focusme.data.local.*

@Database(
    entities = [
        FriendEntity::class,
        StudySessionEntity::class,

        SubjectEntity::class,
        PlannerTaskEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun friendsDao(): FriendsDao
    abstract fun studySessionDao(): StudySessionDao

    abstract fun subjectDao(): SubjectDao
    abstract fun plannerTaskDao(): PlannerTaskDao
}
