package com.cheermateapp.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.cheermateapp.R
import com.cheermateapp.receiver.AlarmActionReceiver
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages "Upcoming Alarm" notifications that show before the actual alarm triggers
 * Similar to Google Clock's upcoming alarm feature
 */
object UpcomingAlarmManager {

    /**
     * Show upcoming alarm notification
     * @param context Application context
     * @param taskId Task ID
     * @param taskTitle Task title
     * @param alarmTime When the alarm will trigger (in milliseconds)
     */
    fun showUpcomingAlarmNotification(
        context: Context,
        taskId: Int,
        taskTitle: String,
        alarmTime: Long
    ) {
        try {
            val currentTime = System.currentTimeMillis()
            val timeUntilAlarm = alarmTime - currentTime
            
            android.util.Log.d("UpcomingAlarmManager", "⏰ Showing upcoming alarm notification")
            android.util.Log.d("UpcomingAlarmManager", "📋 Task: '$taskTitle' (ID: $taskId)")
            android.util.Log.d("UpcomingAlarmManager", "⏱️ Alarm time: ${Date(alarmTime)}")
            android.util.Log.d("UpcomingAlarmManager", "⏳ Time until alarm: ${timeUntilAlarm / 1000 / 60} minutes")

            // ✅ Format alarm time for display
            val alarmTimeFormatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(alarmTime))
            val alarmDateFormatted = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(alarmTime))

            // ✅ Calculate time remaining text
            val timeRemainingText = formatTimeRemaining(timeUntilAlarm)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "upcoming_alarms"

            // ✅ Create dismiss action
            val dismissIntent = Intent(context, AlarmActionReceiver::class.java).apply {
                action = "ACTION_DISMISS_UPCOMING"
                putExtra("TASK_ID", taskId)
            }
            val dismissPendingIntent = PendingIntent.getBroadcast(
                context, taskId * 10 + 3, dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_alarm_large)
                .setContentTitle("⏰ Upcoming Alarm")
                .setContentText("$taskTitle • $alarmDateFormatted at $alarmTimeFormatted")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("$taskTitle\n$alarmDateFormatted at $alarmTimeFormatted\n$timeRemainingText"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(false)
                .setOngoing(true) // Keep visible until alarm triggers or dismissed
                .addAction(R.drawable.ic_stop, "Dismiss Alarm", dismissPendingIntent)
                .setColor(context.resources.getColor(R.color.notification_upcoming, null))
                .build()

            // Use unique notification ID for upcoming alarms
            val upcomingNotificationId = taskId + 2000
            notificationManager.notify(upcomingNotificationId, notification)
            
            android.util.Log.d("UpcomingAlarmManager", "✅ Upcoming alarm notification shown with ID: $upcomingNotificationId")

        } catch (e: Exception) {
            android.util.Log.e("UpcomingAlarmManager", "💥 Error showing upcoming alarm notification", e)
        }
    }

    /**
     * Dismiss upcoming alarm notification
     */
    fun dismissUpcomingAlarmNotification(context: Context, taskId: Int) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val upcomingNotificationId = taskId + 2000
            notificationManager.cancel(upcomingNotificationId)
            
            android.util.Log.d("UpcomingAlarmManager", "🔕 Upcoming alarm notification dismissed for Task ID: $taskId")
        } catch (e: Exception) {
            android.util.Log.e("UpcomingAlarmManager", "💥 Error dismissing upcoming alarm notification", e)
        }
    }

    /**
     * Schedule upcoming alarm notification to show 1 hour before actual alarm
     */
    fun scheduleUpcomingAlarmNotification(
        context: Context,
        taskId: Int,
        taskTitle: String,
        alarmTime: Long
    ) {
        try {
            val currentTime = System.currentTimeMillis()
            val oneHourBefore = alarmTime - (60 * 60 * 1000) // 1 hour before
            
            android.util.Log.d("UpcomingAlarmManager", "📅 Scheduling upcoming alarm notification")
            android.util.Log.d("UpcomingAlarmManager", "⏰ Alarm time: ${Date(alarmTime)}")
            android.util.Log.d("UpcomingAlarmManager", "📢 Notification time: ${Date(oneHourBefore)}")

            if (oneHourBefore > currentTime) {
                // Schedule notification for 1 hour before alarm
                val upcomingIntent = Intent(context, com.cheermateapp.receiver.UpcomingAlarmReceiver::class.java).apply {
                    putExtra("TASK_ID", taskId)
                    putExtra("TASK_TITLE", taskTitle)
                    putExtra("ALARM_TIME", alarmTime)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    taskId + 5000, // Unique request code for upcoming notifications
                    upcomingIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    oneHourBefore,
                    pendingIntent
                )

                android.util.Log.d("UpcomingAlarmManager", "✅ Upcoming alarm notification scheduled successfully")
            } else {
                // Less than 1 hour until alarm - show notification immediately
                showUpcomingAlarmNotification(context, taskId, taskTitle, alarmTime)
                android.util.Log.d("UpcomingAlarmManager", "⚡ Showing upcoming alarm notification immediately (less than 1 hour)")
            }

        } catch (e: Exception) {
            android.util.Log.e("UpcomingAlarmManager", "💥 Error scheduling upcoming alarm notification", e)
        }
    }

    private fun formatTimeRemaining(timeUntilAlarm: Long): String {
        val minutes = timeUntilAlarm / 1000 / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> {
                val remainingHours = hours % 24
                if (remainingHours > 0) {
                    "Alarm in ${days}d ${remainingHours}h"
                } else {
                    "Alarm in ${days} day${if (days > 1) "s" else ""}"
                }
            }
            hours > 0 -> {
                val remainingMinutes = minutes % 60
                if (remainingMinutes > 0) {
                    "Alarm in ${hours}h ${remainingMinutes}m"
                } else {
                    "Alarm in ${hours} hour${if (hours > 1) "s" else ""}"
                }
            }
            minutes > 0 -> "Alarm in ${minutes} minute${if (minutes > 1) "s" else ""}"
            else -> "Alarm in less than 1 minute"
        }
    }
}