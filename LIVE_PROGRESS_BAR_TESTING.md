# Live Progress Bar Testing Guide

## Overview
This guide explains how to test the live progress bar updates feature that was implemented to automatically update the progress bar when tasks are marked as done.

## What Was Implemented

### 1. Flow-Based Observables in TaskDao
Added Flow versions of count queries that automatically emit new values when the database changes:
- `getAllTasksCountFlow(userId: Int): Flow<Int>`
- `getCompletedTasksCountFlow(userId: Int): Flow<Int>`
- `getTodayTasksCountFlow(userId: Int, date: String): Flow<Int>`
- `getTasksCompletedTodayByUpdateFlow(userId: Int, todayTimestamp: Long): Flow<Int>`

### 2. MainActivity Progress Bar Observer
Added `observeTaskChangesForProgressBar()` method that:
- Observes today's tasks count and completed today count using Flow.combine
- Automatically updates the progress bar when either value changes
- Updates happen on the main thread to ensure smooth UI updates

### 3. FragmentTaskActivity Progress Bar Observer
Added `observeTaskChangesForProgressBar()` method that:
- Observes all tasks count and completed tasks count
- Automatically updates the progress bar when tasks are marked as done
- Uses Flow.combine to merge multiple data streams

## How to Test Manually

### Test Case 1: Home Screen Progress Bar (MainActivity)
1. Launch the app and log in
2. Navigate to the home screen (Dashboard)
3. Note the current progress bar percentage (e.g., "2 of 5 tasks completed today - 40%")
4. Tap the "+" FAB button to add a new task
5. Create a task with today's date
6. **Expected Result**: Progress bar should update immediately showing "2 of 6 tasks completed today - 33%"
7. Navigate to the Tasks screen
8. Mark one of today's tasks as complete
9. Go back to the home screen
10. **Expected Result**: Progress bar should show "3 of 6 tasks completed today - 50%" without needing to refresh

### Test Case 2: Tasks Screen Progress Bar (FragmentTaskActivity)
1. Launch the app and navigate to FragmentTaskActivity
2. Note the current progress bar (e.g., "5 of 10 tasks completed - 50%")
3. Mark a pending task as done by tapping it and selecting "Mark as Done"
4. **Expected Result**: Progress bar should immediately update to "6 of 10 tasks completed - 60%"
5. Create a new task
6. **Expected Result**: Progress bar should update to "6 of 11 tasks completed - 54%"
7. Delete a completed task
8. **Expected Result**: Progress bar should update to "5 of 10 tasks completed - 50%"

### Test Case 3: Multiple Rapid Updates
1. Navigate to the Tasks screen
2. Quickly mark 3 tasks as complete in succession
3. **Expected Result**: Progress bar should animate smoothly through each percentage change
4. No crashes or ANR (Application Not Responding) errors should occur

### Test Case 4: Background to Foreground
1. Mark a task as complete
2. Press the home button to send app to background
3. Wait 5 seconds
4. Return to the app
5. **Expected Result**: Progress bar should still show the correct updated percentage

## Expected Behaviors

### Visual Indicators
- ✅ Progress bar width changes smoothly (animated by Android's layout system)
- ✅ Percentage text updates immediately (e.g., "40%" → "50%")
- ✅ Subtitle text updates (e.g., "4 of 10 tasks" → "5 of 10 tasks")
- ✅ No flickering or visual glitches

### Performance
- ✅ Updates happen within 100ms of task status change
- ✅ No lag or frame drops during updates
- ✅ App remains responsive during updates

### Debugging
- Check Logcat for messages like:
  ```
  D/MainActivity: 🔄 Progress bar updated live: 3/6
  D/FragmentTaskActivity: 🔄 Progress bar updated live: 5/10
  ```

## Technical Details

### Flow Collection Lifecycle
- Flow collection starts in `onCreate()` of both activities
- Collection continues until the activity is destroyed
- Flow automatically stops when the lifecycle ends (using `lifecycleScope`)

### Thread Safety
- Database queries run on `Dispatchers.IO`
- UI updates happen on `Dispatchers.Main` via `withContext(Dispatchers.Main)`
- Flow.combine ensures thread-safe merging of data streams

### Edge Cases Handled
1. **Zero Tasks**: Progress bar shows "0 of 0 tasks completed - 0%"
2. **All Tasks Complete**: Progress bar shows "10 of 10 tasks completed - 100%"
3. **Activity Restart**: Flow collection restarts automatically
4. **Database Errors**: Caught and logged, doesn't crash the app

## Troubleshooting

### Progress Bar Not Updating
1. Check Logcat for Flow collection errors
2. Verify userId is correctly passed to the activity
3. Check that database updates are using the correct DAO methods
4. Ensure task status is being updated correctly in the database

### Progress Bar Updates Too Slowly
1. Check if database is on the main thread (should be on IO dispatcher)
2. Verify Flow collection is using `lifecycleScope` not `GlobalScope`
3. Check for any blocking operations in the update path

### App Crashes on Task Update
1. Check for null pointer exceptions in progress bar view references
2. Ensure progress bar layout exists in the current activity
3. Verify weight calculations don't divide by zero

## Code References

### Key Files Modified
1. `app/src/main/java/com/example/cheermateapp/data/dao/TaskDao.kt` - Added Flow queries
2. `app/src/main/java/com/example/cheermateapp/MainActivity.kt` - Added observer for home screen
3. `app/src/main/java/com/example/cheermateapp/FragmentTaskActivity.kt` - Added observer for tasks screen

### Key Methods
- `TaskDao.getAllTasksCountFlow()` - Observes total task count
- `TaskDao.getCompletedTasksCountFlow()` - Observes completed task count
- `MainActivity.observeTaskChangesForProgressBar()` - Sets up Flow collection for home screen
- `FragmentTaskActivity.observeTaskChangesForProgressBar()` - Sets up Flow collection for tasks screen
- `MainActivity.updateProgressDisplay()` - Updates the progress bar UI
- `FragmentTaskActivity.updateProgressCard()` - Updates the progress card UI

## Future Enhancements

1. **Animation**: Add smooth animation to progress bar changes
2. **Notifications**: Show a toast when progress milestones are reached (25%, 50%, 75%, 100%)
3. **Vibration**: Add haptic feedback when tasks are completed
4. **Sound**: Play a success sound when tasks are marked as done
5. **Statistics**: Track progress over time and show trends

## Related Issues

- Original request: "make sure this one show live update of the progress bar, like if there is a done task it should be updated live automatically"
- Solution: Implemented Flow-based database observations for real-time UI updates
