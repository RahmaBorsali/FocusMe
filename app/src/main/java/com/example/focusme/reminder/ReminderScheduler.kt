package com.example.focusme.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.content.getSystemService
import com.example.focusme.R

object ReminderScheduler {

    private const val CHANNEL_ID = "focus_reminder"
    private const val CHANNEL_NAME = "Focus Reminder"

    fun createChannel(context: Context) {
        // ✅ NotificationChannel existe seulement à partir de API 26
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService<NotificationManager>() ?: return

        val soundUri: Uri = Uri.parse("android.resource://${context.packageName}/${R.raw.alarm_sound}")

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setSound(soundUri, audioAttributes)
            enableVibration(true)
        }

        manager.createNotificationChannel(channel)
    }

    fun scheduleInMinutes(context: Context, minutes: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAt = System.currentTimeMillis() + minutes * 60_000L

        // ✅ setExactAndAllowWhileIdle existe à partir de API 23 -> OK pour minSdk 24
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
    }

    fun channelId(): String = CHANNEL_ID
}
