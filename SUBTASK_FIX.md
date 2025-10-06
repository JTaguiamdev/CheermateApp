# Subtask Addition Fix

## Problem
When attempting to add subtasks in the `fragment_tasks_extension` layout, the operation would fail with an "Error adding subtask" message. The subtasks were not being saved or displayed in the list below the input field.

## Root Cause
The `SubTask` entity uses a composite primary key consisting of three fields:
- `Task_ID`
- `User_ID`
- `Subtask_ID`

In the original implementation, all new subtasks were created with `Subtask_ID = 0`. This caused a primary key constraint violation when trying to insert a second subtask for the same task, as Room would try to insert a duplicate key `(Task_ID, User_ID, 0)`.

## Solution
The fix involves two changes:

### 1. SubTaskDao.kt
Added a new query method to generate the next available `Subtask_ID`:

```kotlin
@Query("SELECT COALESCE(MAX(Subtask_ID), 0) + 1 FROM SubTask WHERE Task_ID = :taskId AND User_ID = :userId")
suspend fun getNextSubtaskId(taskId: Int, userId: Int): Int
```

This query:
- Finds the maximum `Subtask_ID` for the given task and user
- Returns 0 if no subtasks exist (handled by `COALESCE`)
- Adds 1 to get the next available ID

### 2. FragmentTaskExtensionActivity.kt
Updated the `addSubtask()` method to fetch and use the next available ID:

```kotlin
// Get the next available Subtask_ID for this task and user
val nextSubtaskId = withContext(Dispatchers.IO) {
    db.subTaskDao().getNextSubtaskId(taskId, userId)
}

val newSubtask = SubTask(
    Subtask_ID = nextSubtaskId,  // Changed from 0 to nextSubtaskId
    Task_ID = taskId,
    User_ID = userId,
    // ... other fields
)
```

## Result
Subtasks can now be successfully added and will be:
- Saved to the database with unique IDs
- Displayed in the subtasks list
- Properly managed (check/uncheck, delete) as intended

## Files Modified
- `app/src/main/java/com/example/cheermateapp/data/dao/SubTaskDao.kt`
- `app/src/main/java/com/example/cheermateapp/FragmentTaskExtensionActivity.kt`
