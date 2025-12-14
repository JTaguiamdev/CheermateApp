package com.cheermateapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cheermateapp.util.UpcomingAlarmManager

/**
 * Receiver that triggers "Upcoming Alarm" notifications
 * Shows notification 1 hour before the actual alarm
 */
class UpcomingAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra("TASK_ID", -1)
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Task Reminder"
        val alarmTime = intent.getLongExtra("ALARM_TIME", 0)

        android.util.Log.d("UpcomingAlarmReceiver", "📅 Upcoming alarm notification triggered")
        android.util.Log.d("UpcomingAlarmReceiver", "📋 Task: '$taskTitle' (ID: $taskId)")
        android.util.Log.d("UpcomingAlarmReceiver", "⏰ Alarm time: ${java.util.Date(alarmTime)}")

        if (taskId != -1 && alarmTime != 0L) {
            UpcomingAlarmManager.showUpcomingAlarmNotification(context, taskId, taskTitle, alarmTime)
        } else {
            android.util.Log.e("UpcomingAlarmReceiver", "❌ Invalid data - Task ID: $taskId, Alarm Time: $alarmTime")
        }
    }
}