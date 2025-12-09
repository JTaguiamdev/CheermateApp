# Notification and Alarm System Implementation Guide

This document outlines the steps to implement a robust notification and alarm system for CheermateApp, leveraging the existing database schema and architecture.

## 1. Goal

To schedule and display notifications for task reminders. When a user sets a reminder for a task, the app should fire a notification at the specified time.

## 2. Prerequisites (Verified ✅)

The database schema is ready for this implementation.
-   **`Task` table**: Contains `DueAt` and `DueTime` columns.
-   **`TaskReminder` table**: Contains `RemindAt` (a `Long` timestamp) which is perfect for scheduling with `AlarmManager`.

## 3. Architecture Overview

The implementation will follow Android best practices for background work and integrate seamlessly with the existing MVVM architecture.

-   **`AlarmManager`**: The standard Android system service for scheduling exact alarms at a future time. It will be used to trigger our reminder logic even if the app is not running.
-   **`BroadcastReceiver`**: A receiver that listens for the alarm intent fired by `AlarmManager`. This receiver will be the entry point for our notification logic.
-   **`TaskRepository`**: The existing repository will be used within the `BroadcastReceiver` to fetch the details of the task that needs a notification.
-   **`NotificationManager`**: The standard Android system service for creating and displaying notifications. We will use it to build a rich notification for the task reminder.
-   **`AlarmScheduler` (New Helper Class)**: A new helper class to encapsulate the logic for scheduling and canceling alarms. This keeps the `ViewModel` clean.
-   **`TaskViewModel`**: The `ViewModel` will orchestrate the process by calling the `AlarmScheduler` when a task with a reminder is created or updated.
-   **`BOOT_COMPLETED` Receiver**: A `BroadcastReceiver` that listens for the device boot event to reschedule all active reminders, as alarms are cleared on reboot.

```
┌──────────────────┐      ┌───────────────────┐      ┌───────────────────┐
│ TaskViewModel    │ ────▶│  AlarmScheduler   │ ────▶│   AlarmManager    │
│ (Create/Update   │      │ (schedule/cancel) │      │ (System Service)  │
│      Task)       │      └───────────────────┘      └─────────┬─────────┘
└──────────────────┘                                          │
                                                              │ (Fires at RemindAt time)
                                                              ↓
┌──────────────────┐      ┌───────────────────┐      ┌───────────────────┐
│ BroadcastReceiver│ ────▶│  TaskRepository   │ ────▶│ NotificationManager
│(Receives Alarm)  │      │   (Get Task)      │      │ (Display Notice)  │
└──────────────────┘      └───────────────────┘      └───────────────────┘
```

## 4. Implementation Steps

### Step 1: Create the `AlarmReceiver`

Create a new `BroadcastReceiver` to handle the incoming alarm.

**File:** `app/src/main/java/com/cheermateapp/receiver/AlarmReceiver.kt`

```kotlin
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
        val taskId = intent.getIntExtra("TASK_ID", -1)
        val userId = intent.getIntExtra("USER_ID", -1)
        if (taskId == -1 || userId == -1) return

        val db = (context.applicationContext as CheermateApp).db
        val taskRepository = TaskRepository(db.taskDao(), db.subTaskDao(), db.taskReminderDao(), db.taskDependencyDao())

        CoroutineScope(Dispatchers.IO).launch {
            val task = taskRepository.getTaskById(userId, taskId) // Assumes getTaskById exists in repo
            task?.let {
                // You can also get the personalized message here
                val message = "Reminder for: ${it.Title}"
                showNotification(context, it.Task_ID, "Cheermate Reminder", message)
            }
        }
    }

    private fun showNotification(context: Context, taskId: Int, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "cheermate_reminders" // Make sure channel is created on app start

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_bell) // Replace with your notification icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(taskId, notification)
    }
}
```

### Step 2: Create the `AlarmScheduler` Helper

Create a helper to manage scheduling and canceling alarms.

**File:** `app/src/main/java/com/cheermateapp/util/AlarmScheduler.kt`

```kotlin
package com.cheermateapp.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.cheermateapp.data.model.Task
import com.cheermateapp.data.model.TaskReminder
import com.cheermateapp.receiver.AlarmReceiver

object AlarmScheduler {

    fun schedule(context: Context, task: Task, reminder: TaskReminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TASK_ID", task.Task_ID)
            putExtra("USER_ID", task.User_ID)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.TaskReminder_ID, // Use a unique ID for the reminder
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Check for exact alarm permission
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.RemindAt,
                pendingIntent
            )
        }
    }

    fun cancel(context: Context, reminder: TaskReminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.TaskReminder_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
```

### Step 3: Integrate with `TaskViewModel`

Update the `TaskViewModel` to call the `AlarmScheduler`. You'll need a method to create/update a task that also accepts a reminder.

```kotlin
// In TaskViewModel.kt

fun insertTaskWithReminder(task: Task, reminder: TaskReminder) {
    viewModelScope.launch {
        _taskOperationState.value = UiState.Loading
        
        // First, insert the task and reminder into the DB
        // This should be a single transaction in the repository
        when (val result = taskRepository.insertTaskAndReminder(task, reminder)) {
            is DataResult.Success -> {
                val newReminder = result.data // Assume repo returns the created reminder
                AlarmScheduler.schedule(getApplication(), task, newReminder)
                _taskOperationState.value = UiState.Success("Task with reminder created")
            }
            is DataResult.Error -> {
                _taskOperationState.value = UiState.Error(result.message ?: "Failed", result.exception)
            }
            // ...
        }
    }
}

fun deleteTaskWithReminder(task: Task, reminder: TaskReminder) {
    viewModelScope.launch {
        // ... delete logic
        // On success:
        AlarmScheduler.cancel(getApplication(), reminder)
    }
}
```
***Note:** You will need to add `insertTaskAndReminder` and `getTaskById` methods to your `TaskRepository` and `TaskDao`.*

### Step 4: Handle Device Boot

Create a receiver to reschedule alarms when the device reboots.

**File:** `app/src/main/java/com/cheermateapp/receiver/BootCompletedReceiver.kt`

```kotlin
package com.cheermateapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cheermateapp.CheermateApp
import com.cheermateapp.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val db = (context.applicationContext as CheermateApp).db
            val reminderDao = db.taskReminderDao() // Assuming direct DAO access for simplicity here

            CoroutineScope(Dispatchers.IO).launch {
                val reminders = reminderDao.getAllActiveReminders() // Needs to be implemented
                reminders.forEach { reminder ->
                    val task = db.taskDao().getTaskById(reminder.User_ID, reminder.Task_ID) // Needs to be implemented
                    task?.let {
                        AlarmScheduler.schedule(context, it, reminder)
                    }
                }
            }
        }
    }
}
```

### Step 5: Update `AndroidManifest.xml`

Declare the receivers and add necessary permissions.

```xml
<manifest ...>
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <application ...>
        ...
        <receiver
            android:name=".receiver.AlarmReceiver"
            android:enabled="true" />

        <receiver
            android:name=".receiver.BootCompletedReceiver"
            android:enabled="true"
            android:exported="true">
            <intent-filter>
                <action android:name="android.permission.action.BOOT_COMPLETED" />
            </intent-filter>
        </receiver>
        ...
    </application>
</manifest>
```

### Step 6: Create Notification Channel

Create a notification channel when the application starts.

**File:** `app/src/main/java/com/cheermateapp/CheermateApp.kt`

```kotlin
// In your Application class (e.g., CheermateApp.kt)
override fun onCreate() {
    super.onCreate()
    // ... existing code
    createNotificationChannel()
}

private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Task Reminders"
        val descriptionText = "Channel for Cheermate task reminders"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("cheermate_reminders", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
```

## 5. Summary of New Files and Changes

-   **New Files:**
    -   `NOTIFICATION_AND_ALARM_IMPLEMENTATION.md` (this file)
    -   `app/src/main/java/com/cheermateapp/receiver/AlarmReceiver.kt`
    -   `app/src/main/java/com/cheermateapp/util/AlarmScheduler.kt`
    -   `app/src/main/java/com/cheermateapp/receiver/BootCompletedReceiver.kt`
-   **Modified Files:**
    -   `app/src/main/java/com/cheermateapp/ui/viewmodel/TaskViewModel.kt`: Add methods to call `AlarmScheduler`.
    -   `app/src/main/java/com/cheermateapp/data/repository/TaskRepository.kt`: Add methods to get reminders and tasks together.
    -   `app/src/main/java/com/cheermateapp/data/dao/TaskReminderDao.kt` and `TaskDao.kt`: Add queries to get active reminders and tasks by ID.
    -   `app/src/main/AndroidManifest.xml`: Add permissions and declare receivers.
    -   `app/src/main/java/com/cheermateapp/CheermateApp.kt`: Add notification channel creation.

This guide provides a complete, robust, and architecturally consistent plan for implementing the notification and alarm system.
