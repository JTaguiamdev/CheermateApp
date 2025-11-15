package com.cheermateapp.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.cheermateapp.receiver.ReminderReceiver

/**
 * Utility class for scheduling and managing task reminders using AlarmManager
 */
object ReminderManager {
    
    /**
     * Schedule a reminder for a task
     * @param context Application context
     * @param taskId Task ID
     * @param taskTitle Task title
     * @param taskDescription Task description
     * @param userId User ID
     * @param reminderTimeMillis Time in milliseconds when reminder should trigger
     */
    fun scheduleReminder(
        context: Context,
        taskId: Int,
        taskTitle: String,
        taskDescription: String?,
        userId: Int,
        reminderTimeMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("TASK_ID", taskId)
            putExtra("TASK_TITLE", taskTitle)
            putExtra("TASK_DESCRIPTION", taskDescription)
            putExtra("USER_ID", userId)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId, // Use taskId as request code to make it unique
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Schedule alarm
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // For API 23+, use setExactAndAllowWhileIdle for better reliability
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeMillis,
                    pendingIntent
                )
            }
            
            android.util.Log.d(
                "ReminderManager",
                "⏰ Reminder scheduled for task: $taskTitle at ${java.util.Date(reminderTimeMillis)}"
            )
        } catch (e: SecurityException) {
            android.util.Log.e("ReminderManager", "Permission denied for scheduling alarm", e)
        }
    }
    
    /**
     * Cancel a scheduled reminder
     * @param context Application context
     * @param taskId Task ID to cancel reminder for
     */
    fun cancelReminder(context: Context, taskId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        
        android.util.Log.d("ReminderManager", "❌ Reminder cancelled for task ID: $taskId")
    }
    
    /**
     * Check if exact alarm permission is granted (for Android 12+)
     */
    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return alarmManager.canScheduleExactAlarms()
        }
        return true
    }
    
    /**
     * Calculate reminder time based on minutes before due date
     * @param dueTimeMillis Due time in milliseconds
     * @param minutesBefore Minutes before due time to remind
     * @return Reminder time in milliseconds
     */
    fun calculateReminderTime(dueTimeMillis: Long, minutesBefore: Int): Long {
        return dueTimeMillis - (minutesBefore * 60 * 1000)
    }
}
