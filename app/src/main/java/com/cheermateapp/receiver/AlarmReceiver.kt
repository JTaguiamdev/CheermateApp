package com.cheermateapp.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.cheermateapp.CheermateApp
import com.cheermateapp.R
import com.cheermateapp.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val currentTime = System.currentTimeMillis()
        val taskId = intent.getIntExtra("TASK_ID", -1)
        val userId = intent.getIntExtra("USER_ID", -1)
        
        android.util.Log.d("AlarmReceiver", "🔔 ALARM TRIGGERED! Current time: ${java.util.Date(currentTime)}")
        android.util.Log.d("AlarmReceiver", "📋 Task ID: $taskId, User ID: $userId")
        android.util.Log.d("AlarmReceiver", "📦 Intent extras: ${intent.extras?.keySet()?.joinToString()}")

        if (taskId == -1 || userId == -1) {
            android.util.Log.e("AlarmReceiver", "❌ INVALID DATA - Task ID: $taskId, User ID: $userId")
            return
        }

        val db = (context.applicationContext as CheermateApp).db
        val taskRepository = TaskRepository(db.taskDao(), db.subTaskDao(), db.taskReminderDao())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                android.util.Log.d("AlarmReceiver", "🔍 Fetching task from database...")
                val task = taskRepository.getTaskForReceiver(userId, taskId)
                
                if (task != null) {
                    android.util.Log.d("AlarmReceiver", "✅ Task found: '${task.Title}'")
                    android.util.Log.d("AlarmReceiver", "📝 Task description: '${task.Description ?: "No description"}'")
                    
                    val message = "Reminder for: ${task.Title}"
                    android.util.Log.d("AlarmReceiver", "📤 Showing notification: $message")
                    showNotification(context, task.Task_ID, "Cheermate Reminder", message)
                    
                    android.util.Log.d("AlarmReceiver", "✨ Notification displayed successfully!")
                } else {
                    android.util.Log.e("AlarmReceiver", "❌ Task NOT FOUND in database - Task ID: $taskId, User ID: $userId")
                }
            } catch (e: Exception) {
                android.util.Log.e("AlarmReceiver", "💥 ERROR processing alarm", e)
            }
        }
    }

    private fun showNotification(context: Context, taskId: Int, title: String, message: String) {
        try {
            android.util.Log.d("AlarmReceiver", "🔔 Creating notification...")
            android.util.Log.d("AlarmReceiver", "📱 Title: '$title', Message: '$message'")
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "task_reminders" // Use existing channel from NotificationUtil
            
            android.util.Log.d("AlarmReceiver", "📢 Using channel ID: $channelId")

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_bell)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVibrate(longArrayOf(0, 500, 250, 500))
                .build()

            android.util.Log.d("AlarmReceiver", "🚀 Posting notification with ID: $taskId")
            notificationManager.notify(taskId, notification)
            android.util.Log.d("AlarmReceiver", "✅ Notification posted successfully!")
            
        } catch (e: Exception) {
            android.util.Log.e("AlarmReceiver", "💥 ERROR creating/showing notification", e)
        }
    }
}
