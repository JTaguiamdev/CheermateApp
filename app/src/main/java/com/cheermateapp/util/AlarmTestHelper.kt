package com.cheermateapp.util

import android.content.Context
import android.util.Log
import com.cheermateapp.CheermateApp
import com.cheermateapp.data.model.Task
import com.cheermateapp.data.model.TaskReminder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * Helper class for testing the alarm system functionality
 */
object AlarmTestHelper {
    
    private const val TAG = "AlarmTestHelper"
    
    /**
     * Create a test reminder that will trigger in the specified number of seconds
     * @param context Application context
     * @param seconds Number of seconds from now when the alarm should trigger
     * @param taskTitle Optional custom title for the test task
     * @return Pair of created Task and TaskReminder, or null if failed
     */
    suspend fun scheduleTestAlarm(
        context: Context, 
        seconds: Int = 10, 
        taskTitle: String = "🧪 Test Alarm"
    ): Pair<Task, TaskReminder>? {
        
        try {
            Log.d(TAG, "🧪 CREATING TEST ALARM")
            Log.d(TAG, "⏱️ Will trigger in $seconds seconds")
            
            val db = (context.applicationContext as CheermateApp).db
            val currentTime = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp()
            val triggerTime = System.currentTimeMillis() + (seconds * 1000)
            
            // Create a test task
            val testTask = Task(
                Task_ID = 0, // Will be auto-generated
                User_ID = 1, // Assuming user ID 1 exists
                Title = taskTitle,
                Description = "This is a test alarm created for testing purposes. Time created: ${Date()}",
                CreatedAt = currentTime,
                DueAt = null,
                DueTime = null,
                Status = com.cheermateapp.data.model.Status.Pending,
                Priority = com.cheermateapp.data.model.Priority.Medium,
                Category = com.cheermateapp.data.model.Category.Others,
                UpdatedAt = currentTime
            )
            
            Log.d(TAG, "📋 Test task details:")
            Log.d(TAG, "  Title: ${testTask.Title}")
            Log.d(TAG, "  Description: ${testTask.Description}")
            Log.d(TAG, "  Created at: ${testTask.CreatedAt}")
            
            // Insert the task first (needs to be in a coroutine)
            return withContext(Dispatchers.IO) {
                val insertedTaskId = db.taskDao().insert(testTask).toInt()
                val finalTask = testTask.copy(Task_ID = insertedTaskId)
            
            Log.d(TAG, "✅ Task inserted with ID: $insertedTaskId")
            
                // Create the reminder
                val testReminder = TaskReminder(
                    TaskReminder_ID = 0, // Will be auto-generated
                    Task_ID = insertedTaskId,
                    User_ID = 1,
                    RemindAt = triggerTime,
                    ReminderType = com.cheermateapp.data.model.ReminderType.AT_SPECIFIC_TIME,
                    CreatedAt = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp(),
                    UpdatedAt = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp()
                )
                
                Log.d(TAG, "⏰ Test reminder details:")
                Log.d(TAG, "  Will trigger at: ${Date(testReminder.RemindAt)}")
                
                // Insert the reminder
                val insertedReminderId = db.taskReminderDao().insert(testReminder).toInt()
                val finalReminder = testReminder.copy(TaskReminder_ID = insertedReminderId)
            
            Log.d(TAG, "✅ Reminder inserted with ID: $insertedReminderId")
            
                // Schedule the alarm
                Log.d(TAG, "📡 Scheduling alarm with ReminderManager...")
                withContext(Dispatchers.Main) {
                    ReminderManager.scheduleReminder(
                        context,
                        finalTask.Task_ID,
                        finalTask.Title,
                        finalTask.Description,
                        finalTask.User_ID,
                        finalReminder.RemindAt
                    )
                }
                
                Log.d(TAG, "🎯 TEST ALARM SETUP COMPLETE!")
                Log.d(TAG, "⏰ Expect notification in $seconds seconds at ${Date(triggerTime)}")
                Log.d(TAG, "👀 Watch logcat for 'AlarmReceiver' tags when it triggers")
                
                Pair(finalTask, finalReminder)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 ERROR creating test alarm", e)
            return null
        }
    }
    
    /**
     * Schedule multiple test alarms at different intervals for comprehensive testing
     */
    suspend fun scheduleMultipleTestAlarms(context: Context) {
        Log.d(TAG, "🧪 SCHEDULING MULTIPLE TEST ALARMS")
        
        val intervals = listOf(
            15 to "🔔 Quick Test (15s)",
            30 to "🔔 Medium Test (30s)", 
            60 to "🔔 Long Test (1m)"
        )
        
        intervals.forEach { (seconds, title) ->
            scheduleTestAlarm(context, seconds, title)
            kotlinx.coroutines.delay(100) // Small delay to ensure different IDs
        }
        
        Log.d(TAG, "✅ All test alarms scheduled!")
    }
    
    /**
     * Cancel a test alarm
     */
    suspend fun cancelTestAlarm(context: Context, taskId: Int) {
        try {
            Log.d(TAG, "❌ Cancelling test alarm for task ID: $taskId")
            ReminderManager.cancelReminder(context, taskId)
            
            // Also remove from database
            val db = (context.applicationContext as CheermateApp).db
            withContext(kotlinx.coroutines.Dispatchers.IO) {
                // Find the task first to get user ID
                val task = db.taskDao().getTaskByCompositeKey(1, taskId) // Assuming user ID 1
                if (task != null) {
                    db.taskReminderDao().deleteAllForTask(taskId, task.User_ID)
                    db.taskDao().delete(task)
                }
            }
            
            Log.d(TAG, "✅ Test alarm cancelled and cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "💥 ERROR cancelling test alarm", e)
        }
    }
    
    /**
     * List all active reminders in the database for debugging
     */
    suspend fun listActiveReminders(context: Context) {
        try {
            Log.d(TAG, "📋 LISTING ALL ACTIVE REMINDERS")
            val db = (context.applicationContext as CheermateApp).db
            
            withContext(kotlinx.coroutines.Dispatchers.IO) {
                val reminders = db.taskReminderDao().getAllActiveReminders()
                
                if (reminders.isEmpty()) {
                    Log.d(TAG, "📭 No active reminders found")
                } else {
                    Log.d(TAG, "📬 Found ${reminders.size} active reminders:")
                    reminders.forEachIndexed { index, reminder ->
                        val task = db.taskDao().getTaskByCompositeKey(reminder.User_ID, reminder.Task_ID)
                        Log.d(TAG, "  ${index + 1}. Task: '${task?.Title}' | Reminder ID: ${reminder.TaskReminder_ID}")
                        Log.d(TAG, "      ⏰ Scheduled for: ${Date(reminder.RemindAt)}")
                        Log.d(TAG, "      🔄 Active: ${reminder.IsActive}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 ERROR listing reminders", e)
        }
    }
    
    /**
     * Check alarm permissions and system status
     */
    fun checkAlarmSystemStatus(context: Context) {
        Log.d(TAG, "🔍 CHECKING ALARM SYSTEM STATUS")
        
        // Check exact alarm permission
        val canScheduleExact = ReminderManager.canScheduleExactAlarms(context)
        Log.d(TAG, "⚡ Can schedule exact alarms: $canScheduleExact")
        
        // Check notification permission (simplified check)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val areNotificationsEnabled = notificationManager.areNotificationsEnabled()
        Log.d(TAG, "📱 Notifications enabled: $areNotificationsEnabled")
        
        // Check if notification channel exists
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel("task_reminders")
            Log.d(TAG, "📢 Notification channel exists: ${channel != null}")
            Log.d(TAG, "📢 Channel importance: ${channel?.importance}")
        }
        
        Log.d(TAG, "✅ System status check complete")
    }
}