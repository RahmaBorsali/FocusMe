package com.example.focusme.data.local

import android.content.Context
import androidx.room.Room
import com.example.focusme.data.db.AppDatabase

object DbProvider {
    @Volatile private var INSTANCE: AppDatabase? = null

    fun db(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "focusme_db"
            )
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
        }
    }
}
