package com.example.focusme.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.focusme.R

object NotificationHelper {

    const val CHANNEL_ID = "focusme_timer_channel"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "FocusMe Timer",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications du minuteur FocusMe"
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun showTimerFinished(context: Context) {
        ensureChannel(context)

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // ✅ ton icône
            .setContentTitle("FocusMe")
            .setContentText("⏰ Session terminée !")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(1001, notif)
        } catch (_: SecurityException) {
        }
    }
}
