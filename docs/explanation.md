I understand your request regarding the subtask checkbox behavior and the representation of the `IsCompleted` attribute.

**Regarding "Allow Unchecking":**
The current code already implements toggling functionality. In `FragmentTaskExtensionActivity.kt`, when `cbSubTask` is clicked, the `onSubTaskToggle` lambda is executed:
```kotlin
onSubTaskToggle = { subtask ->
    val updatedSubtask = subtask.copy(
        IsCompleted = !subtask.IsCompleted // This line toggles the boolean value
    )
    updateSubtask(updatedSubtask)
}
```
This line `IsCompleted = !subtask.IsCompleted` correctly flips the `IsCompleted` status from `true` to `false` and vice-versa. The `updateSubtask` function then saves this change to the database and updates the UI. If you are experiencing an issue where the subtask cannot be unchecked, it might be a subtle UI refresh bug or a perception issue rather than the core logic preventing unchecking.

**Regarding "Change `IsCompleted` from 0/1 to 'No'/'Yes'":**
The `IsCompleted` attribute in your `SubTask` data class is currently defined as a `Boolean`. In databases like SQLite (which Room uses), `Boolean` values are natively stored as `0` (for false) and `1` (for true). This is the standard, most efficient, and robust way to store boolean states in a database.

Changing `IsCompleted` from a `Boolean` (stored as `0`/`1`) to a `String` (to store `"No"`/`"Yes"`) would have significant implications:

1.  **Database Schema Migration:** This would be a breaking change to your database schema. If your app is already in production or has existing users, this change would require a complex database migration. Without a carefully planned migration, existing user data for subtasks could be lost or cause the app to crash.
2.  **Inefficiency:** Storing strings like `"Yes"` or `"No"` consumes more storage space and is less efficient for database operations (e.g., querying for completed tasks) compared to using native boolean types (`0` or `1`).
3.  **Increased Complexity:** All parts of your code that read or write the `IsCompleted` status would need to be updated to convert between `Boolean` and `"Yes"`/`"No"` strings, adding unnecessary complexity.

**Recommendation:**
I strongly recommend against changing the `IsCompleted` type from `Boolean` to `String` in the database. The current `Boolean` representation is industry standard and more efficient.

Instead, let's address the "unchecking" issue. Given the current code, it *should* allow unchecking. If it's not working as expected, we can investigate further into the UI refresh or specific use case.

If you still wish to proceed with changing the database representation to "No"/"Yes" strings, please be aware of the complexities of database migration and confirm that you understand these implications. I would need explicit instructions on how to handle the database migration (e.g., increasing the database version, writing specific `Migration` code).

Please let me know how you would like to proceed.