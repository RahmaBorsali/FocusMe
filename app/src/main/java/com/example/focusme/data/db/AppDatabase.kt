package com.example.focusme.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.focusme.data.local.*
import com.example.focusme.data.local.TaskEntity
import com.example.focusme.data.local.TaskDao

@Database(
    entities = [
        StudySessionEntity::class,
        SubjectEntity::class,
        PlannerTaskEntity::class,
        TaskEntity::class,
        MusicTrackEntity::class
    ],
    version = 14,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studySessionDao(): StudySessionDao
    abstract fun subjectDao(): SubjectDao
    abstract fun plannerTaskDao(): PlannerTaskDao
    abstract fun taskDao(): TaskDao
    abstract fun musicDao(): MusicDao
}
