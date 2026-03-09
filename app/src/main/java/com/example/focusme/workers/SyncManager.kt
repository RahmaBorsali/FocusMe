package com.example.focusme.workers

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object SyncManager {

    fun syncAfterSession(
        context: Context,
        userId: String,
        focusMinutes: Int,
        tasksCompleted: Int,
        streak: Int
    ) {
        val inputData = workDataOf(
            StatsSyncWorker.KEY_USER_ID to userId,
            StatsSyncWorker.KEY_FOCUS_MINUTES to focusMinutes,
            StatsSyncWorker.KEY_TASKS_COMPLETED to tasksCompleted,
            StatsSyncWorker.KEY_STREAK to streak
        )

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<StatsSyncWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "stats_sync_$userId",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
