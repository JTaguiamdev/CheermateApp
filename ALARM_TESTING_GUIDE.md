# 🔔 Alarm System Testing Guide

This guide provides comprehensive instructions for testing the CheermateApp alarm and notification system. The system has been enhanced with detailed logging to help you verify functionality.

## 📋 Prerequisites

Before testing, ensure:
1. **Notification Permission**: Grant notification permission when prompted
2. **Exact Alarm Permission**: For Android 12+, enable "Alarms & reminders" in app settings
3. **Logcat Access**: Use Android Studio's Logcat to monitor detailed logs

## 🧪 Testing Methods

### Method 1: Using the AlarmTestHelper (Recommended)

The `AlarmTestHelper` class provides convenient methods for testing. Add these test calls to your MainActivity or create a debug menu:

```kotlin
// Add to your MainActivity or any activity for testing

import com.cheermateapp.util.AlarmTestHelper

// Test a quick alarm (triggers in 15 seconds)
AlarmTestHelper.scheduleTestAlarm(this, 15, "🧪 Quick Test")

// Test multiple alarms at different intervals
AlarmTestHelper.scheduleMultipleTestAlarms(this)

// Check system status
AlarmTestHelper.checkAlarmSystemStatus(this)

// List all active reminders
AlarmTestHelper.listActiveReminders(this)
```

### Method 2: Manual Testing via UI

1. **Create a Task with Reminder**:
   - Open the app and create a new task
   - Set a due date/time that's a few minutes from now
   - Add a reminder (e.g., 1 minute before due time)
   - Save the task

2. **Wait and Monitor**:
   - Keep the app in background or close it
   - Monitor logcat for alarm triggers
   - Check for notification appearance

### Method 3: Direct ReminderManager Testing

Add this code to test the ReminderManager directly:

```kotlin
// Schedule an alarm for 30 seconds from now
val futureTime = System.currentTimeMillis() + (30 * 1000)
ReminderManager.scheduleReminder(
    this,
    taskId = 999,
    taskTitle = "🧪 Direct Test",
    taskDescription = "Testing ReminderManager directly",
    userId = 1,
    reminderTimeMillis = futureTime
)
```

## 📊 Key Log Tags to Monitor

When testing, filter your logcat for these tags to see detailed information:

### 🔔 AlarmReceiver Logs
- `AlarmReceiver`: Shows when alarms are triggered and notifications are created
- Look for: 
  - `🔔 ALARM TRIGGERED!`
  - `✅ Task found`
  - `✨ Notification displayed successfully!`

### ⏰ ReminderManager Logs  
- `ReminderManager`: Shows alarm scheduling and cancellation
- Look for:
  - `📋 SCHEDULING ALARM:`
  - `✅ ALARM SCHEDULED SUCCESSFULLY!`
  - `❌ PERMISSION DENIED` (if permissions missing)

### 🧪 AlarmTestHelper Logs
- `AlarmTestHelper`: Shows test alarm creation and system status
- Look for:
  - `🧪 CREATING TEST ALARM`
  - `🎯 TEST ALARM SETUP COMPLETE!`
  - `🔍 CHECKING ALARM SYSTEM STATUS`

## 🔍 Detailed Testing Steps

### Step 1: System Status Check
```kotlin
AlarmTestHelper.checkAlarmSystemStatus(this)
```
**Expected Logs:**
```
AlarmTestHelper: 🔍 CHECKING ALARM SYSTEM STATUS
AlarmTestHelper: ⚡ Can schedule exact alarms: true
AlarmTestHelper: 📱 Notifications enabled: true  
AlarmTestHelper: 📢 Notification channel exists: true
AlarmTestHelper: ✅ System status check complete
```

### Step 2: Schedule Test Alarm
```kotlin
AlarmTestHelper.scheduleTestAlarm(this, 15) // 15 seconds
```
**Expected Logs:**
```
AlarmTestHelper: 🧪 CREATING TEST ALARM
AlarmTestHelper: ⏱️ Will trigger in 15 seconds
AlarmTestHelper: 📋 Test task details:
AlarmTestHelper:   Title: 🧪 Test Alarm
AlarmTestHelper: ✅ Task inserted with ID: [ID]
AlarmTestHelper: ✅ Reminder inserted with ID: [ID]
ReminderManager: 📋 SCHEDULING ALARM:
ReminderManager:   Task ID: [ID]
ReminderManager:   Time Until Alarm: 15 seconds
ReminderManager: ✅ ALARM SCHEDULED SUCCESSFULLY!
AlarmTestHelper: 🎯 TEST ALARM SETUP COMPLETE!
```

### Step 3: Wait for Alarm Trigger (15 seconds later)
**Expected Logs:**
```
AlarmReceiver: 🔔 ALARM TRIGGERED! Current time: [timestamp]
AlarmReceiver: 📋 Task ID: [ID], User ID: 1
AlarmReceiver: 🔍 Fetching task from database...
AlarmReceiver: ✅ Task found: '🧪 Test Alarm'
AlarmReceiver: 📤 Showing notification: Reminder for: 🧪 Test Alarm
AlarmReceiver: 🔔 Creating notification...
AlarmReceiver: 🚀 Posting notification with ID: [ID]
AlarmReceiver: ✅ Notification posted successfully!
AlarmReceiver: ✨ Notification displayed successfully!
```

### Step 4: Verify Notification
- Check notification panel for "Cheermate Reminder" notification
- Notification should show: "Reminder for: 🧪 Test Alarm"
- Should have vibration and sound (if enabled)

## 🚨 Troubleshooting

### Problem: No alarm triggers
**Check:**
1. **Permissions**: Look for `❌ PERMISSION DENIED` in logs
2. **Battery Optimization**: Disable for CheermateApp in device settings
3. **Do Not Disturb**: Temporarily disable DND mode
4. **App Standby**: Keep app active or add to battery whitelist

### Problem: Alarm triggers but no notification
**Check:**
1. **Notification Permission**: Enable in app settings
2. **Channel Settings**: Check if "task_reminders" channel is blocked
3. **App Notifications**: Ensure app notifications aren't disabled

### Problem: Database errors
**Look for:**
```
AlarmReceiver: ❌ Task NOT FOUND in database
AlarmReceiver: 💥 ERROR processing alarm
```

### Problem: Scheduling errors
**Look for:**
```
ReminderManager: ❌ PERMISSION DENIED for scheduling alarm
ReminderManager: 💥 UNEXPECTED ERROR scheduling alarm
```

## 📱 Device-Specific Testing

### Android 12+ (API 31+)
- Must grant "Alarms & reminders" permission
- Check `canScheduleExactAlarms()` returns true

### Android 13+ (API 33+)  
- Must grant notification permission via runtime prompt
- Check notifications work even when app is closed

### Doze Mode Testing
1. Put device in Doze mode: `adb shell dumpsys deviceidle force-idle`
2. Schedule alarm
3. Verify it still triggers (should use `setExactAndAllowWhileIdle`)
4. Exit Doze: `adb shell dumpsys deviceidle unforce`

## 🔧 Quick Debug Commands

Add these to your activity for quick testing:

```kotlin
// Quick 10-second test
findViewById<Button>(R.id.test_button).setOnClickListener {
    AlarmTestHelper.scheduleTestAlarm(this, 10)
}

// Check system
findViewById<Button>(R.id.status_button).setOnClickListener {
    AlarmTestHelper.checkAlarmSystemStatus(this)
}

// List reminders  
findViewById<Button>(R.id.list_button).setOnClickListener {
    AlarmTestHelper.listActiveReminders(this)
}
```

## ✅ Success Criteria

A successful test should show:

1. **✅ Scheduling Success**: 
   - `✅ ALARM SCHEDULED SUCCESSFULLY!` in logs
   - No permission errors

2. **✅ Trigger Success**:
   - `🔔 ALARM TRIGGERED!` appears at expected time
   - Task found in database
   - Notification created and posted

3. **✅ User Experience**:
   - Notification appears in notification panel
   - Has correct title and message
   - Vibrates/sounds as expected
   - Can be dismissed by tapping

## 📈 Advanced Testing

### Boot Testing
1. Schedule alarms
2. Restart device
3. Check that `BootCompletedReceiver` reschedules alarms
4. Monitor logs for boot receiver activity

### Background Testing
1. Schedule alarms
2. Force-stop app
3. Verify alarms still trigger
4. Test with app in background for extended periods

### Multiple Alarms Testing
```kotlin
AlarmTestHelper.scheduleMultipleTestAlarms(this)
```
- Tests scheduling multiple alarms
- Verifies unique IDs don't conflict
- Checks all alarms trigger correctly

Remember to clean up test alarms after testing using `AlarmTestHelper.cancelTestAlarm()` or by deleting test tasks from the app.