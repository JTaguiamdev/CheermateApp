package com.cheermateapp.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.cheermateapp.AlarmActivity
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
        
        android.util.Log.d("AlarmReceiver", "🚨 FULL ALARM TRIGGERED! Current time: ${java.util.Date(currentTime)}")
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
                    
                    // 🚨 LAUNCH FULL-SCREEN ALARM ACTIVITY
                    launchAlarmActivity(context, taskId, userId, task.Title, task.Description ?: "")
                    
                    // 🔔 Also show notification with action buttons
                    showAlarmNotification(context, taskId, task.Title, task.Description ?: "")
                    
                    android.util.Log.d("AlarmReceiver", "🚨 Full alarm system activated!")
                } else {
                    android.util.Log.e("AlarmReceiver", "❌ Task NOT FOUND in database - Task ID: $taskId, User ID: $userId")
                }
            } catch (e: Exception) {
                android.util.Log.e("AlarmReceiver", "💥 ERROR processing alarm", e)
            }
        }
    }
    
    private fun launchAlarmActivity(context: Context, taskId: Int, userId: Int, taskTitle: String, taskDescription: String) {
        try {
            android.util.Log.d("AlarmReceiver", "🚨 Launching full-screen alarm activity")
            
            val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
                putExtra("TASK_ID", taskId)
                putExtra("USER_ID", userId)
                putExtra("TASK_TITLE", taskTitle)
                putExtra("TASK_DESCRIPTION", taskDescription)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            
            context.startActivity(alarmIntent)
            android.util.Log.d("AlarmReceiver", "✅ Alarm activity launched successfully!")
            
        } catch (e: Exception) {
            android.util.Log.e("AlarmReceiver", "💥 ERROR launching alarm activity", e)
        }
    }

    private fun showAlarmNotification(context: Context, taskId: Int, taskTitle: String, taskDescription: String) {
        try {
            android.util.Log.d("AlarmReceiver", "🔔 Creating alarm notification with action buttons...")
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "task_reminders"
            
            // 🔘 Create Snooze action
            val snoozeIntent = Intent(context, AlarmActionReceiver::class.java).apply {
                action = "ACTION_SNOOZE"
                putExtra("TASK_ID", taskId)
                putExtra("TASK_TITLE", taskTitle)
                putExtra("TASK_DESCRIPTION", taskDescription)
            }
            val snoozePendingIntent = android.app.PendingIntent.getBroadcast(
                context, taskId * 10 + 1, snoozeIntent, 
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            
            // 🛑 Create Stop action  
            val stopIntent = Intent(context, AlarmActionReceiver::class.java).apply {
                action = "ACTION_STOP"
                putExtra("TASK_ID", taskId)
            }
            val stopPendingIntent = android.app.PendingIntent.getBroadcast(
                context, taskId * 10 + 2, stopIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_alarm_large)
                .setContentTitle("🚨 ALARM: $taskTitle")
                .setContentText(taskDescription.ifEmpty { "Task reminder alarm" })
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(false) // Don't auto-dismiss
                .setOngoing(true) // Keep notification until manually dismissed
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVibrate(longArrayOf(0, 500, 200, 500))
                .addAction(R.drawable.ic_snooze, "Snooze 10m", snoozePendingIntent)
                .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
                .setFullScreenIntent(
                    android.app.PendingIntent.getActivity(
                        context, taskId,
                        Intent(context, AlarmActivity::class.java).apply {
                            putExtra("TASK_ID", taskId)
                            putExtra("TASK_TITLE", taskTitle)
                            putExtra("TASK_DESCRIPTION", taskDescription)
                        },
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                    ), true
                )
                .build()

            android.util.Log.d("AlarmReceiver", "🚀 Posting alarm notification with ID: $taskId")
            notificationManager.notify(taskId, notification)
            android.util.Log.d("AlarmReceiver", "✅ Alarm notification posted successfully!")
            
        } catch (e: Exception) {
            android.util.Log.e("AlarmReceiver", "💥 ERROR creating alarm notification", e)
        }
    }
}
