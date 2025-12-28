package com.example.focusme.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ChallengeEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ChallengesDatabase : RoomDatabase() {
    abstract fun challengeDao(): ChallengeDao
}
