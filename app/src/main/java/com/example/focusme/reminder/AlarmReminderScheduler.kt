package com.example.focusme.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object AlarmReminderScheduler {

    fun scheduleExact(
        context: Context,
        taskId: Long,
        triggerAtMillis: Long,
        title: String,
        body: String
    ) {                                    
        val now = System.currentTimeMillis()
        val safeTriggerAt = if (triggerAtMillis <= now) now + 30_000L else triggerAtMillis

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Android 12+ : si exact alarm pas autorisé, on ne peut pas garantir l’exactitude
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            // fallback (moins exact)
            scheduleInexact(context, taskId, safeTriggerAt, title, body)
            return
        }

        val pi = pendingIntent(context, taskId, title, body)

        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, safeTriggerAt, pi)
    }

    private fun scheduleInexact(
        context: Context,
        taskId: Long,
        triggerAtMillis: Long,
        title: String,
        body: String
    ) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(context, taskId, title, body)
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
    }

    fun cancel(context: Context, taskId: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(context, taskId, null, null, forCancel = true)
        am.cancel(pi)
    }

    private fun pendingIntent(
        context: Context,
        taskId: Long,
        title: String?,
        body: String?,
        forCancel: Boolean = false
    ): PendingIntent {
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra("notifId", taskId.toIntSafe())
            if (!forCancel) {
                putExtra("title", title)
                putExtra("body", body)
            }
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)

        return PendingIntent.getBroadcast(
            context,
            taskId.toIntSafe(),
            intent,
            flags
        )
    }

    private fun Long.toIntSafe(): Int {
        val v = (this % Int.MAX_VALUE).toInt()
        return if (v == 0) 1001 else v
    }
}
