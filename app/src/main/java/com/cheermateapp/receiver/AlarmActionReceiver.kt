package com.cheermateapp.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cheermateapp.util.ReminderManager

/**
 * Handles alarm action buttons from notifications
 * - Snooze: Reschedules alarm for 10 minutes later
 * - Stop: Completely dismisses the alarm
 */
class AlarmActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val taskId = intent.getIntExtra("TASK_ID", -1)
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: ""
        val taskDescription = intent.getStringExtra("TASK_DESCRIPTION") ?: ""

        android.util.Log.d("AlarmActionReceiver", "🔘 Action received: $action for Task ID: $taskId")

        when (action) {
            "ACTION_SNOOZE" -> {
                handleSnoozeAction(context, taskId, taskTitle, taskDescription)
            }
            "ACTION_STOP" -> {
                handleStopAction(context, taskId)
            }
            "ACTION_DISMISS_UPCOMING" -> {
                handleDismissUpcomingAction(context, taskId)
            }
            else -> {
                android.util.Log.w("AlarmActionReceiver", "⚠️ Unknown action: $action")
            }
        }
    }

    private fun handleSnoozeAction(context: Context, taskId: Int, taskTitle: String, taskDescription: String) {
        try {
            android.util.Log.d("AlarmActionReceiver", "😴 Processing SNOOZE action for task: '$taskTitle'")

            // ✅ Calculate snooze time (10 minutes from now)
            val snoozeTime = System.currentTimeMillis() + (10 * 60 * 1000)
            android.util.Log.d("AlarmActionReceiver", "⏰ Snooze time calculated: ${java.util.Date(snoozeTime)}")

            // ✅ Schedule new alarm
            ReminderManager.scheduleReminder(
                context,
                taskId,
                taskTitle,
                taskDescription,
                1, // Default user ID - you might want to pass this properly
                snoozeTime
            )

            // ✅ Dismiss current alarm notification
            dismissAlarmNotification(context, taskId)

            // ✅ Show snooze confirmation notification
            showSnoozeConfirmation(context, taskId, taskTitle)

            android.util.Log.d("AlarmActionReceiver", "✅ Alarm snoozed successfully for 10 minutes")

        } catch (e: Exception) {
            android.util.Log.e("AlarmActionReceiver", "💥 Error handling snooze action", e)
        }
    }

    private fun handleStopAction(context: Context, taskId: Int) {
        try {
            android.util.Log.d("AlarmActionReceiver", "🛑 Processing STOP action for Task ID: $taskId")

            // ✅ Cancel any future alarms for this task
            ReminderManager.cancelReminder(context, taskId)

            // ✅ Dismiss alarm notification
            dismissAlarmNotification(context, taskId)

            android.util.Log.d("AlarmActionReceiver", "✅ Alarm stopped completely")

        } catch (e: Exception) {
            android.util.Log.e("AlarmActionReceiver", "💥 Error handling stop action", e)
        }
    }

    private fun dismissAlarmNotification(context: Context, taskId: Int) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(taskId)
            android.util.Log.d("AlarmActionReceiver", "🔕 Alarm notification dismissed for Task ID: $taskId")
        } catch (e: Exception) {
            android.util.Log.e("AlarmActionReceiver", "💥 Error dismissing notification", e)
        }
    }

    private fun showSnoozeConfirmation(context: Context, taskId: Int, taskTitle: String) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "task_reminders"

            val confirmationNotification = androidx.core.app.NotificationCompat.Builder(context, channelId)
                .setSmallIcon(com.cheermateapp.R.drawable.ic_snooze)
                .setContentTitle("😴 Alarm Snoozed")
                .setContentText("$taskTitle will ring again in 10 minutes")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .setTimeoutAfter(5000) // Auto-dismiss after 5 seconds
                .build()

            // Use different notification ID to avoid conflict
            notificationManager.notify(taskId + 1000, confirmationNotification)
            android.util.Log.d("AlarmActionReceiver", "✅ Snooze confirmation notification shown")

        } catch (e: Exception) {
            android.util.Log.e("AlarmActionReceiver", "💥 Error showing snooze confirmation", e)
        }
    }

    private fun handleDismissUpcomingAction(context: Context, taskId: Int) {
        try {
            android.util.Log.d("AlarmActionReceiver", "🗑️ Processing DISMISS UPCOMING action for Task ID: $taskId")

            // ✅ Cancel the scheduled alarm
            ReminderManager.cancelReminder(context, taskId)

            // ✅ Dismiss upcoming alarm notification
            com.cheermateapp.util.UpcomingAlarmManager.dismissUpcomingAlarmNotification(context, taskId)

            android.util.Log.d("AlarmActionReceiver", "✅ Upcoming alarm dismissed completely")

        } catch (e: Exception) {
            android.util.Log.e("AlarmActionReceiver", "💥 Error handling dismiss upcoming action", e)
        }
    }
}