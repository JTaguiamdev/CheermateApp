# Quick Reference: CardView to LinearLayout Migration

## What Changed

### fragment_tasks_extension.xml
```diff
- <androidx.cardview.widget.CardView
-     android:id="@+id/task_card"
-     app:cardCornerRadius="1dp"
-     app:cardBackgroundColor="#33FFFFFF"
-     app:cardElevation="2dp">
-     <LinearLayout padding="16dp">
-         <!-- content -->
-     </LinearLayout>
- </androidx.cardview.widget.CardView>

+ <LinearLayout
+     android:id="@+id/task_card"
+     android:background="@drawable/bg_card_glass_hover"
+     android:elevation="2dp"
+     android:padding="16dp">
+     <!-- content -->
+ </LinearLayout>
```

### FragmentTaskExtensionActivity.kt
```diff
- private lateinit var taskCard: androidx.cardview.widget.CardView
- private lateinit var subtaskCard: androidx.cardview.widget.CardView
+ private lateinit var taskCard: LinearLayout
+ private lateinit var subtaskCard: LinearLayout
+ private lateinit var tvItemsCount: TextView

  private fun displaySubtasks() {
+     val completedCount = subtasks.count { it.IsCompleted }
+     tvItemsCount.text = "$completedCount/${subtasks.size} items"
+     subtasksContainer.visibility = if (subtasks.isEmpty()) View.GONE else View.VISIBLE
  }
```

## Why This Change?

1. **Consistency** - Matches activity_main.xml pattern
2. **Simplicity** - Removes unnecessary view nesting
3. **Functionality** - Adds subtask progress tracking

## Subtask Features

| Feature | How to Use |
|---------|-----------|
| **Add** | Type in input field → "Add" button appears → Click to save |
| **Complete** | Check the checkbox → Item gets strikethrough |
| **Count** | Automatically shows "X/Y items" |
| **Delete** | Click trash icon → Confirm → Removed |

## Testing Quick Checks

✅ Cards display with rounded corners  
✅ Glass background effect visible  
✅ Subtask count updates on toggle  
✅ Empty state shows "No subtasks yet"  
✅ Add/delete operations work  

## Result

Same visual design, cleaner code, better subtask functionality! 🎉
