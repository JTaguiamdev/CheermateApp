# Quick Integration Guide - Task Actions Bottom Sheet

## 1. Show the Bottom Sheet

```kotlin
// Method 1: Basic usage
val bottomSheet = TaskActionsBottomSheet.newInstance()
bottomSheet.show(supportFragmentManager, TaskActionsBottomSheet.TAG)

// Method 2: With action listeners
val bottomSheet = TaskActionsBottomSheet.newInstance()
bottomSheet.setOnMarkCompletedListener {
    // Your logic here
}
bottomSheet.show(supportFragmentManager, TaskActionsBottomSheet.TAG)
```

## 2. Integration in TaskAdapter (RecyclerView)

```kotlin
class TaskAdapter : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
    
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        
        init {
            // Show bottom sheet on long press
            itemView.setOnLongClickListener {
                val task = tasks[adapterPosition]
                showTaskActions(task)
                true
            }
        }
        
        private fun showTaskActions(task: Task) {
            val bottomSheet = TaskActionsBottomSheet.newInstance()
            
            bottomSheet.setOnMarkCompletedListener {
                onTaskCompleted(task)
            }
            
            bottomSheet.setOnSnoozeListener {
                onTaskSnoozed(task)
            }
            
            bottomSheet.setOnWontDoListener {
                onTaskWontDo(task)
            }
            
            bottomSheet.show(
                (itemView.context as AppCompatActivity).supportFragmentManager,
                TaskActionsBottomSheet.TAG
            )
        }
    }
    
    private fun onTaskCompleted(task: Task) {
        // Update task status in database
        lifecycleScope.launch {
            taskRepository.updateStatus(task.id, Status.COMPLETED)
        }
    }
    
    private fun onTaskSnoozed(task: Task) {
        // Show snooze dialog
    }
    
    private fun onTaskWontDo(task: Task) {
        // Update task status
        lifecycleScope.launch {
            taskRepository.updateStatus(task.id, Status.WONT_DO)
        }
    }
}
```

## 3. Integration in Task Detail Activity

```kotlin
class TaskDetailActivity : AppCompatActivity() {
    
    private lateinit var task: Task
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup button to show actions
        findViewById<Button>(R.id.btnTaskActions).setOnClickListener {
            showTaskActionsBottomSheet()
        }
    }
    
    private fun showTaskActionsBottomSheet() {
        val bottomSheet = TaskActionsBottomSheet.newInstance()
        
        bottomSheet.setOnMarkCompletedListener {
            markTaskAsCompleted()
        }
        
        bottomSheet.setOnSnoozeListener {
            showSnoozeDialog()
        }
        
        bottomSheet.setOnWontDoListener {
            markTaskAsWontDo()
        }
        
        bottomSheet.show(supportFragmentManager, TaskActionsBottomSheet.TAG)
    }
    
    private fun markTaskAsCompleted() {
        lifecycleScope.launch {
            val db = AppDb.get(this@TaskDetailActivity)
            db.taskDao().updateTaskStatus(task.TaskID, Status.COMPLETED)
            Toast.makeText(this@TaskDetailActivity, "Task completed!", Toast.LENGTH_SHORT).show()
            finish() // Close activity
        }
    }
    
    private fun showSnoozeDialog() {
        // Show your existing snooze dialog
    }
    
    private fun markTaskAsWontDo() {
        lifecycleScope.launch {
            val db = AppDb.get(this@TaskDetailActivity)
            db.taskDao().updateTaskStatus(task.TaskID, Status.WONT_DO)
            Toast.makeText(this@TaskDetailActivity, "Task marked as won't do", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
```

## 4. Integration in MainActivity (Task List Fragment)

```kotlin
class MainActivity : AppCompatActivity() {
    
    private fun setupTaskListInteractions() {
        taskRecyclerView?.setOnItemLongClickListener { _, _, position, _ ->
            val task = tasks[position]
            showTaskActionsForTask(task)
            true
        }
    }
    
    private fun showTaskActionsForTask(task: Task) {
        val bottomSheet = TaskActionsBottomSheet.newInstance()
        
        bottomSheet.setOnMarkCompletedListener {
            handleTaskCompletion(task)
        }
        
        bottomSheet.setOnSnoozeListener {
            showSnoozeDialogForTask(task)
        }
        
        bottomSheet.setOnWontDoListener {
            handleTaskWontDo(task)
        }
        
        bottomSheet.show(supportFragmentManager, TaskActionsBottomSheet.TAG)
    }
    
    private fun handleTaskCompletion(task: Task) {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)
                withContext(Dispatchers.IO) {
                    db.taskDao().updateTaskStatus(task.TaskID, Status.COMPLETED)
                }
                Toast.makeText(this@MainActivity, "Task completed!", Toast.LENGTH_SHORT).show()
                loadRecentTasks() // Refresh list
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error updating task", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun handleTaskWontDo(task: Task) {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)
                withContext(Dispatchers.IO) {
                    db.taskDao().updateTaskStatus(task.TaskID, Status.WONT_DO)
                }
                Toast.makeText(this@MainActivity, "Task marked as won't do", Toast.LENGTH_SHORT).show()
                loadRecentTasks() // Refresh list
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error updating task", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

## 5. Settings Integration

To add dark mode toggle to settings screen:

```kotlin
class FragmentSettingsActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_settings)
        
        setupDarkModeToggle()
    }
    
    private fun setupDarkModeToggle() {
        val switchDarkMode = findViewById<SwitchCompat>(R.id.switchDarkMode)
        
        // Set initial state
        switchDarkMode.isChecked = ThemeManager.isDarkModeActive(this)
        
        // Handle toggle
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            ThemeManager.toggleDarkMode(this)
            // Activity will recreate automatically
        }
    }
}
```

Add to `fragment_settings.xml`:
```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="16dp">
    
    <TextView
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="Dark Mode"
        android:textSize="16sp" />
    
    <androidx.appcompat.widget.SwitchCompat
        android:id="@+id/switchDarkMode"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />
</LinearLayout>
```

## 6. Testing the Implementation

### Test Case 1: Show Bottom Sheet
```kotlin
@Test
fun testShowBottomSheet() {
    val activity = ActivityScenario.launch(MainActivity::class.java)
    activity.onActivity { act ->
        val bottomSheet = TaskActionsBottomSheet.newInstance()
        bottomSheet.show(act.supportFragmentManager, TaskActionsBottomSheet.TAG)
        
        // Verify bottom sheet is shown
        assertTrue(bottomSheet.isVisible)
    }
}
```

### Test Case 2: Mark Completed Listener
```kotlin
@Test
fun testMarkCompletedListener() {
    var listenerCalled = false
    
    val bottomSheet = TaskActionsBottomSheet.newInstance()
    bottomSheet.setOnMarkCompletedListener {
        listenerCalled = true
    }
    
    // Simulate click
    bottomSheet.show(supportFragmentManager, TaskActionsBottomSheet.TAG)
    // Trigger action_mark_completed click
    
    assertTrue(listenerCalled)
}
```

## 7. Common Patterns

### Pattern 1: Show on FAB long press
```kotlin
fab.setOnLongClickListener {
    val bottomSheet = TaskActionsBottomSheet.newInstance()
    bottomSheet.show(supportFragmentManager, TaskActionsBottomSheet.TAG)
    true
}
```

### Pattern 2: Show from context menu
```kotlin
override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
    // Instead of context menu, show bottom sheet
    showTaskActionsBottomSheet()
}
```

### Pattern 3: Show with task data
```kotlin
fun showTaskActionsBottomSheet(taskId: Int) {
    lifecycleScope.launch {
        val task = db.taskDao().getById(taskId)
        
        val bottomSheet = TaskActionsBottomSheet.newInstance()
        bottomSheet.setOnMarkCompletedListener {
            updateTaskStatus(task, Status.COMPLETED)
        }
        bottomSheet.show(supportFragmentManager, TaskActionsBottomSheet.TAG)
    }
}
```

## 8. Troubleshooting

### Issue: Fragment transaction exception
**Solution**: Ensure you're using `supportFragmentManager`, not `fragmentManager`

### Issue: Dark mode doesn't persist
**Solution**: Check that `ThemeManager.initializeTheme()` is called in Application.onCreate()

### Issue: Button clicks don't work
**Solution**: Verify listeners are set before calling `show()`

### Issue: Colors don't change
**Solution**: Clean and rebuild project, verify both color resource files exist

## 9. Customization

### Change button order
Edit `bottom_sheet_task_actions.xml` and reorder LinearLayout blocks

### Change icons
Replace emoji text in TextViews with drawable icons:
```xml
<ImageView
    android:layout_width="32dp"
    android:layout_height="32dp"
    android:src="@drawable/ic_check"
    app:tint="@color/text_primary" />
```

### Add new actions
Add a new LinearLayout block in the layout file and handle in Kotlin:
```kotlin
val actionNewAction = view.findViewById<LinearLayout>(R.id.action_new)
actionNewAction.setOnClickListener {
    onNewActionListener?.invoke()
    dismiss()
}
```

## 10. Pro Tips

✅ Use coroutines for database operations
✅ Show toast feedback for user actions
✅ Dismiss bottom sheet after action (except dark mode toggle)
✅ Handle edge cases (null task, database errors)
✅ Test on different screen sizes
✅ Verify accessibility with TalkBack
✅ Consider adding haptic feedback
✅ Use view binding for cleaner code

## Need Help?

Check:
- `DARK_MODE_IMPLEMENTATION.md` - Full documentation
- `VISUAL_GUIDE_TASK_ACTIONS.md` - Visual design guide
- `TaskActionsExampleActivity.kt` - Example implementation
