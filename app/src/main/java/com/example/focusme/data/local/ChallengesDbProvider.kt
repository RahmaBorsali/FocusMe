package com.example.focusme.data.local

import android.content.Context
import androidx.room.Room
import com.example.focusme.data.db.ChallengesDatabase

object ChallengesDbProvider {
    @Volatile private var INSTANCE: ChallengesDatabase? = null

    fun db(context: Context): ChallengesDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                ChallengesDatabase::class.java,
                "focusme_challenges_db"
            ).build().also { INSTANCE = it }
        }
    }
}
