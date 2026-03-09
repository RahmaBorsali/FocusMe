package com.example.focusme.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.focusme.data.repository.SocialRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class StatsSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_FOCUS_MINUTES = "focus_minutes"
        const val KEY_TASKS_COMPLETED = "tasks_completed"
        const val KEY_STREAK = "streak"
    }

    override suspend fun doWork(): Result {
        val userId = inputData.getString(KEY_USER_ID) ?: return Result.failure()
        val focusMinutes = inputData.getInt(KEY_FOCUS_MINUTES, 0)
        val tasksCompleted = inputData.getInt(KEY_TASKS_COMPLETED, 0)
        val streak = inputData.getInt(KEY_STREAK, 0)

        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .toString()

        return SocialRepository(applicationContext)
            .syncSessionStats(
                userId = userId,
                date = today,
                focusMinutes = focusMinutes,
                sessionsCount = 1,
                tasksCompleted = tasksCompleted,
                streak = streak
            )
            .fold(
                onSuccess = { Result.success() },
                onFailure = { Result.retry() }
            )
    }
}
