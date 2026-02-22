package com.example.focusme.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Tâche"
        val body = intent.getStringExtra("body") ?: "N'oublie pas ta tâche !"
        val notifId = intent.getIntExtra("notifId", 1001)

        ReminderNotifications.show(context, notifId, title, body)
    }
}
