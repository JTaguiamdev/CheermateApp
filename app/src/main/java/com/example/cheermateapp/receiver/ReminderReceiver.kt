package com.example.cheermateapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.cheermateapp.util.NotificationUtil

/**
 * BroadcastReceiver for handling task reminder alarms
 */
class ReminderReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        android.util.Log.d("ReminderReceiver", "📬 Reminder received")
        
        // Extract data from intent
        val taskId = intent.getIntExtra("TASK_ID", -1)
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Task Reminder"
        val taskDescription = intent.getStringExtra("TASK_DESCRIPTION")
        val userId = intent.getIntExtra("USER_ID", -1)
        
        if (taskId == -1 || userId == -1) {
            android.util.Log.e("ReminderReceiver", "Invalid task or user ID")
            return
        }
        
        // Show notification
        NotificationUtil.showTaskReminder(
            context,
            taskId,
            taskTitle,
            taskDescription,
            userId
        )
        
        android.util.Log.d("ReminderReceiver", "✅ Reminder notification triggered for task: $taskTitle")
    }
}
