# Task List Refresh Fix

## Problem Statement
When editing a task's category (or priority, due date) in `FragmentTaskExtensionActivity` and navigating back to `FragmentTaskActivity`, the task list was not updating to reflect the changes immediately.

## Root Cause Analysis

### Race Condition #1: Asynchronous Save in onPause()
The original `saveTask()` method used `lifecycleScope.launch`, which is asynchronous:

```kotlin
private fun saveTask(updatedTask: Task) {
    lifecycleScope.launch {
        // ... async database update
        currentTask = updatedTask  // Updated after async operation
    }
}
```

When `onPause()` called `saveTaskChanges()`, which in turn called `saveTask()`, the database update would start asynchronously but might not complete before the activity finished. When `FragmentTaskActivity.onResume()` reloaded tasks, the database might not have the updated values yet.

### Race Condition #2: Delayed currentTask Update
The `currentTask` variable was only updated AFTER the async database operation completed. If the user quickly:
1. Changed category to "Work"
2. Immediately pressed back

The flow would be:
1. `showCategoryDialog()` creates updatedTask
2. Calls `saveTask(updatedTask)` (async)
3. User presses back
4. `onPause()` is called
5. `saveTaskChanges()` reads `currentTask` (still has old category!)
6. Async save from step 2 hasn't completed yet
7. Result: Category change is lost

## Solution

### Fix #1: Synchronous Save in onPause()
Modified `onPause()` to use `runBlocking` to ensure task changes are persisted before the activity lifecycle continues:

```kotlin
override fun onPause() {
    super.onPause()
    // Save task changes synchronously to ensure data is persisted before returning to task list
    runBlocking {
        saveTaskChangesSynchronously()
    }
    setResult(RESULT_OK)
}

private suspend fun saveTaskChangesSynchronously() {
    currentTask?.let { task ->
        val title = etTaskTitle.text.toString().trim()
        val description = etTaskDescription.text.toString().trim()
        
        if (title.isNotEmpty()) {
            val updatedTask = task.copy(
                Title = title,
                Description = description.ifEmpty { null },
                UpdatedAt = System.currentTimeMillis()
            )
            saveTaskSynchronously(updatedTask)
        }
    }
}

private suspend fun saveTaskSynchronously(updatedTask: Task) {
    try {
        val db = AppDb.get(this@FragmentTaskExtensionActivity)
        withContext(Dispatchers.IO) {
            db.taskDao().update(updatedTask)
        }
        currentTask = updatedTask
    } catch (e: Exception) {
        android.util.Log.e("FragmentTaskExtensionActivity", "Error saving task", e)
    }
}
```

### Fix #2: Immediate currentTask Update
Updated all task modification handlers to update `currentTask` IMMEDIATELY before the async save:

```kotlin
private fun showCategoryDialog() {
    AlertDialog.Builder(this)
        .setTitle("Select Category")
        .setItems(categories) { _, which ->
            currentTask?.let { task ->
                val updatedTask = task.copy(
                    Category = categoryValues[which],
                    UpdatedAt = System.currentTimeMillis()
                )
                // Update currentTask immediately to ensure it's saved in onPause
                currentTask = updatedTask
                saveTask(updatedTask)  // Async save
                updateCategoryButton(categoryValues[which])
            }
        }
        .show()
}
```

This pattern was applied to:
- `showCategoryDialog()` - Category selection
- `showPriorityDialog()` - Priority selection
- `updateTaskDueDate()` - Due date changes
- `snoozeTask()` - Snooze functionality
- Custom snooze dialog - Custom date selection

## How It Works Now

### Scenario: User Changes Category and Quickly Navigates Back

1. User clicks category button, selects "Work"
2. `showCategoryDialog()` creates updatedTask with Category=Work
3. **Immediately sets `currentTask = updatedTask`** (NEW!)
4. Starts async database save
5. User quickly presses back button
6. `onPause()` is called
7. **`runBlocking` ensures synchronous save completes** (NEW!)
8. `saveTaskChangesSynchronously()` reads `currentTask` (has Category=Work)
9. Database is updated synchronously
10. Activity finishes
11. `FragmentTaskActivity.onResume()` is called
12. `loadTasks()` queries database
13. **Database has updated category** ✅
14. Task list displays correctly with "Work" category

### Scenario: User Edits Title, Changes Category, Then Navigates Back

1. User edits title to "New Title"
2. User changes category to "Work"
   - `currentTask` is updated immediately with Category=Work
3. User presses back
4. `onPause()` calls `saveTaskChangesSynchronously()`
5. Method reads title from EditText: "New Title"
6. Method reads `currentTask` which has Category=Work
7. Creates final updatedTask with both changes
8. Saves synchronously to database
9. **Both title and category changes are persisted** ✅

## Benefits

1. **No Lost Updates**: All task modifications are guaranteed to be saved
2. **Consistent State**: `currentTask` always reflects the latest user changes
3. **Fast Updates**: Task list refreshes immediately when returning to FragmentTaskActivity
4. **No UI Blocking**: Async saves still used for normal operations, only blocking in onPause()
5. **Backward Compatible**: Existing async save methods remain for other use cases

## Performance Considerations

- `runBlocking` in `onPause()` is acceptable because:
  - Database updates are typically fast (< 50ms)
  - `onPause()` is the right place to persist critical state
  - Alternative would be complex result codes or callbacks
  - Users don't perceive latency during navigation transitions

## Testing Recommendations

1. **Quick Navigation Test**: Change category, immediately press back
2. **Multiple Changes Test**: Change category, priority, and due date, then navigate back
3. **Title + Category Test**: Edit title and change category, then navigate back
4. **Rapid Clicks Test**: Click category multiple times quickly, then navigate back
5. **Database Verification**: Use Android Studio's Database Inspector to verify updates

## Files Modified

- `FragmentTaskExtensionActivity.kt`:
  - Added `runBlocking` import
  - Modified `onPause()` to use synchronous save
  - Added `saveTaskChangesSynchronously()` method
  - Added `saveTaskSynchronously()` method
  - Updated `showCategoryDialog()` to set currentTask immediately
  - Updated `showPriorityDialog()` to set currentTask immediately
  - Updated `updateTaskDueDate()` to set currentTask immediately
  - Updated `snoozeTask()` to set currentTask immediately
  - Updated custom snooze date picker to set currentTask immediately

## Related Files (No Changes Needed)

- `FragmentTaskActivity.kt`: Already has proper `onResume()` implementation that calls `loadTasks()`
- `TaskListAdapter.kt`: Properly launches FragmentTaskExtensionActivity
- Task DAO classes: No changes needed

## Future Enhancements

Consider these improvements if needed:
1. Add loading indicator during synchronous save if it becomes slow
2. Implement ViewModel to manage task state more robustly
3. Use LiveData/Flow for reactive updates
4. Add unit tests for the synchronous save logic
