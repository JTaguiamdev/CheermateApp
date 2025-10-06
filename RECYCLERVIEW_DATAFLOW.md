# RecyclerView Data Flow Diagram

## Component Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         MainActivity                             │
│                                                                  │
│  Properties:                                                     │
│  - taskRecyclerView: RecyclerView?                              │
│  - taskAdapter: TaskAdapter?                                    │
│  - currentTasks: MutableList<Task>                              │
│  - currentTaskFilter: String ("ALL", "TODAY", "PENDING", "DONE")│
└───────────────────────┬─────────────────────────────────────────┘
                        │
                        │ navigateToTasks()
                        ▼
┌─────────────────────────────────────────────────────────────────┐
│                    setupTasksFragment()                          │
│                                                                  │
│  1. Initialize RecyclerView                                     │
│  2. Set LinearLayoutManager                                     │
│  3. Create TaskAdapter with callbacks                           │
│  4. Attach adapter to RecyclerView                              │
│  5. Setup FAB click listeners                                   │
│  6. Setup tab filter click listeners                            │
│  7. Call loadTasksFragmentData()                                │
└───────────────────────┬─────────────────────────────────────────┘
                        │
                        │ Initial load & filter changes
                        ▼
┌─────────────────────────────────────────────────────────────────┐
│                   loadTasksFragmentData()                        │
│                                                                  │
│  1. Check currentTaskFilter                                     │
│  2. Query database based on filter:                             │
│     - ALL: getAllTasksForUser(userId)                           │
│     - TODAY: getTodayTasks(userId, todayStr)                    │
│     - PENDING: getPendingTasks(userId)                          │
│     - DONE: getCompletedTasks(userId)                           │
│  3. Update currentTasks list                                    │
│  4. Call updateTasksFragmentUI()                                │
└───────────────────────┬─────────────────────────────────────────┘
                        │
                        │ UI update
                        ▼
┌─────────────────────────────────────────────────────────────────┐
│                  updateTasksFragmentUI()                         │
│                                                                  │
│  1. Query database for task counts                              │
│  2. Update tab labels with counts                               │
│  3. Update "X found" chip                                       │
│  4. Check if tasks list is empty:                               │
│     - Empty: Show empty state TextView                          │
│     - Not empty: Show RecyclerView                              │
│  5. Call taskAdapter?.updateTasks(currentTasks)                 │
└───────────────────────┬─────────────────────────────────────────┘
                        │
                        │ Adapter update
                        ▼
┌─────────────────────────────────────────────────────────────────┐
│                    TaskAdapter.updateTasks()                     │
│                                                                  │
│  1. Clear existing tasks                                        │
│  2. Add new tasks to list                                       │
│  3. Call notifyDataSetChanged()                                 │
│  4. RecyclerView re-renders with new data                       │
└───────────────────────┬─────────────────────────────────────────┘
                        │
                        │ Display
                        ▼
┌─────────────────────────────────────────────────────────────────┐
│                      RecyclerView Display                        │
│                                                                  │
│  For each task in list:                                         │
│  1. TaskAdapter.onCreateViewHolder() creates ViewHolder         │
│  2. TaskAdapter.onBindViewHolder() binds task data              │
│  3. Display item_task.xml layout                                │
│  4. Setup click listeners for buttons                           │
└─────────────────────────────────────────────────────────────────┘
```

## User Interaction Flows

### Filter Tab Click Flow

```
User Clicks Tab (e.g., "Today")
         │
         ▼
updateTabSelection(tab)
  - Highlight selected tab
  - Dim other tabs
         │
         ▼
setTaskFilter("TODAY")
  - Update currentTaskFilter
         │
         ▼
loadTasksFragmentData()
  - Query: getTodayTasks(userId, todayStr)
  - Update currentTasks
         │
         ▼
updateTasksFragmentUI()
  - Update counts
  - Call taskAdapter.updateTasks()
         │
         ▼
RecyclerView displays filtered tasks
```

### Task Complete Flow

```
User Clicks "Complete" Button
         │
         ▼
TaskAdapter.onBindViewHolder()
  btnComplete.setOnClickListener
         │
         ▼
MainActivity.onTaskComplete(task)
  - Create updated task copy
  - Set Status = Completed
  - Set TaskProgress = 100
         │
         ▼
Database Update
  db.taskDao().update(updatedTask)
         │
         ▼
loadTasksFragmentData()
  - Reload tasks from database
         │
         ▼
updateTasksFragmentUI()
  - Update RecyclerView
         │
         ▼
Task moves to "Done" filter
(if currently in Pending filter, it disappears)
```

### Task Delete Flow

```
User Clicks "Delete" Button
         │
         ▼
TaskAdapter.onBindViewHolder()
  btnDelete.setOnClickListener
         │
         ▼
MainActivity.onTaskDelete(task)
  - Show confirmation dialog
         │
         ▼
User Confirms Delete
         │
         ▼
Database Delete
  db.taskDao().delete(task)
         │
         ▼
loadTasksFragmentData()
  - Reload tasks from database
         │
         ▼
updateTasksFragmentUI()
  - Update RecyclerView
         │
         ▼
Task removed from list
```

### FAB Click Flow

```
User Clicks FAB
  (fabAddTask in fragment OR fabAddTaskMain in activity)
         │
         ▼
showQuickAddTaskDialog()
  - Display dialog with form
  - Title, description, priority, date, time
         │
         ▼
User Fills Form & Clicks "Create Task"
         │
         ▼
Validate Input
  - Check title is not empty
  - Check date is provided
         │
         ▼
Create Task Object
  - Build Task with user data
  - Set User_ID, Status, Priority
         │
         ▼
Database Insert
  db.taskDao().insert(newTask)
         │
         ▼
loadTasksFragmentData()
  - Reload tasks from database
         │
         ▼
updateTasksFragmentUI()
  - Update RecyclerView
         │
         ▼
New task appears in list
```

## FAB Visibility Management

```
┌─────────────────────────────────────────────────────────────────┐
│                    Navigation State Machine                      │
└─────────────────────────────────────────────────────────────────┘

Home Screen
  - homeScroll: VISIBLE
  - contentContainer: GONE
  - fabAddTaskMain: GONE ❌
         │
         │ User clicks "Tasks" in bottom nav
         ▼
Tasks Fragment
  - homeScroll: GONE
  - contentContainer: VISIBLE (with fragment_tasks.xml)
  - fabAddTaskMain: VISIBLE ✅
  - fabAddTask (in fragment): VISIBLE ✅
         │
         │ User clicks "Settings" in bottom nav
         ▼
Settings Fragment
  - homeScroll: GONE
  - contentContainer: VISIBLE (with fragment_settings.xml)
  - fabAddTaskMain: GONE ❌
         │
         │ User clicks "Home" in bottom nav
         ▼
Back to Home Screen
  - homeScroll: VISIBLE
  - contentContainer: GONE
  - fabAddTaskMain: GONE ❌
```

## Database Query Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                        Room Database                             │
│                                                                  │
│  ┌──────────────────────────────────────────────────────┐      │
│  │                    TaskDao                            │      │
│  │                                                       │      │
│  │  - getAllTasksForUser(userId): List<Task>           │      │
│  │  - getTodayTasks(userId, date): List<Task>          │      │
│  │  - getPendingTasks(userId): List<Task>              │      │
│  │  - getCompletedTasks(userId): List<Task>            │      │
│  │  - insert(task): Long                                │      │
│  │  - update(task)                                      │      │
│  │  - delete(task)                                      │      │
│  │  - getAllTasksCount(userId): Int                     │      │
│  │  - getTodayTasksCount(userId, date): Int            │      │
│  │  - getPendingTasksCount(userId): Int                │      │
│  │  - getCompletedTasksCount(userId): Int              │      │
│  └──────────────────────────────────────────────────────┘      │
└───────────────────────┬─────────────────────────────────────────┘
                        │
                        │ Coroutine (Dispatchers.IO)
                        ▼
┌─────────────────────────────────────────────────────────────────┐
│                    MainActivity (UI Thread)                      │
│                                                                  │
│  withContext(Dispatchers.IO) {                                  │
│    // Database operations on background thread                  │
│  }                                                               │
│                                                                  │
│  Launch on Main Thread:                                         │
│    // UI updates                                                │
└─────────────────────────────────────────────────────────────────┘
```

## Empty State Logic

```
loadTasksFragmentData()
  ↓
currentTasks populated
  ↓
updateTasksFragmentUI()
  ↓
Check: currentTasks.isEmpty()?
  │
  ├─ YES (Empty) ────────────────┐
  │   - recyclerView: GONE       │
  │   - tvEmptyState: VISIBLE    │
  │   - Message based on filter: │
  │     * ALL: "No tasks..."     │
  │     * TODAY: "No tasks..."   │
  │     * PENDING: "All caught!" │
  │     * DONE: "No completed"   │
  │                              │
  └─ NO (Has Tasks) ─────────────┤
      - recyclerView: VISIBLE    │
      - tvEmptyState: GONE       │
      - taskAdapter.updateTasks() │
                                 │
                                 ▼
                         Display Result
```

## Thread Safety

All database operations are performed safely:

```
Main Thread (UI)
    │
    │ Launch coroutine
    ▼
Coroutine Scope (Main)
    │
    │ withContext(Dispatchers.IO)
    ▼
Background Thread (IO)
    │
    │ Database operation
    │ - Query
    │ - Insert
    │ - Update
    │ - Delete
    ▼
Return to Main Thread
    │
    │ Update UI
    ▼
RecyclerView refreshes
```

This ensures:
1. Database operations don't block UI
2. UI updates happen on main thread
3. No threading conflicts
4. Smooth user experience
