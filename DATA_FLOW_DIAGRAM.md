# FragmentTaskActivity - Data Flow Diagram

## User Interaction Flow

```
┌──────────────────────────────────────────────────────────────────┐
│                     FragmentTaskActivity                          │
└──────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────┐
│  STEP 1: User Clicks a Tab Button                              │
└────────────────────────────────────────────────────────────────┘
         │
         ├─► tabAll.onClick()    → setFilter(FilterType.ALL)
         ├─► tabToday.onClick()  → setFilter(FilterType.TODAY)
         ├─► tabPending.onClick() → setFilter(FilterType.PENDING)
         └─► tabDone.onClick()   → setFilter(FilterType.DONE)
         │
         ▼
┌────────────────────────────────────────────────────────────────┐
│  STEP 2: Filter Method Calls Database Query                    │
└────────────────────────────────────────────────────────────────┘
         │
         ├─► FilterType.ALL     → db.taskDao().getAllTasksForUser(userId)
         ├─► FilterType.TODAY   → db.taskDao().getTodayTasks(userId, date)
         ├─► FilterType.PENDING → db.taskDao().getPendingTasks(userId)
         └─► FilterType.DONE    → db.taskDao().getCompletedTasks(userId)
         │
         ▼
┌────────────────────────────────────────────────────────────────┐
│  STEP 3: Database Returns List<Task>                           │
└────────────────────────────────────────────────────────────────┘
         │
         │  Example Task Object:
         │  ┌─────────────────────────────────────┐
         │  │ Task_ID: 1                          │
         │  │ User_ID: 100                        │
         │  │ Title: "Complete Android App"       │
         │  │ Description: "Finish the project"   │
         │  │ Priority: High                      │
         │  │ Status: Pending                     │
         │  │ TaskProgress: 75                    │
         │  │ DueAt: "2025-09-29"                 │
         │  │ DueTime: "14:30"                    │
         │  └─────────────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────────────────────────────┐
│  STEP 4: Display First Task in Card                            │
└────────────────────────────────────────────────────────────────┘
         │
         │  displayTaskInCard(tasks)
         │      └─► showTaskInCard(tasks[0])
         │
         ├─► tvTaskTitle.text = "Complete Android App"
         ├─► tvTaskDescription.text = "Finish the project"
         ├─► tvTaskPriority.text = "🔴 High" (with red color)
         ├─► tvTaskStatus.text = "⏳ Pending"
         ├─► tvTaskDueDate.text = "Sep 29, 2025 at 2:30 PM"
         └─► tvTaskProgress.text = "75%"
         │
         ▼
┌────────────────────────────────────────────────────────────────┐
│  STEP 5: Update Navigation Controls                            │
└────────────────────────────────────────────────────────────────┘
         │
         │  updateNavigationState()
         │
         ├─► If 0-1 tasks: Hide navigation layout
         └─► If 2+ tasks:
             ├─► Show navigation layout
             ├─► tvTaskCounter.text = "1 / 3"
             ├─► Enable/disable btnPreviousTask (alpha 0.5 if disabled)
             └─► Enable/disable btnNextTask (alpha 0.5 if disabled)

┌────────────────────────────────────────────────────────────────┐
│  STEP 6: User Actions on Displayed Task                        │
└────────────────────────────────────────────────────────────────┘
         │
         ├─► btnPreviousTask.onClick()
         │   └─► navigateToPreviousTask()
         │       ├─► currentTaskIndex--
         │       ├─► showTaskInCard(tasks[currentTaskIndex])
         │       └─► updateNavigationState()
         │
         ├─► btnNextTask.onClick()
         │   └─► navigateToNextTask()
         │       ├─► currentTaskIndex++
         │       ├─► showTaskInCard(tasks[currentTaskIndex])
         │       └─► updateNavigationState()
         │
         └─► btnMarkDone.onClick()
             └─► markTaskAsDone(task)
                 ├─► db.taskDao().updateTaskStatus(userId, taskId, "Completed")
                 ├─► db.taskDao().updateTaskProgress(userId, taskId, 100)
                 ├─► Show success toast
                 └─► filterTasks(currentFilter) // Refresh display

```

## Database Query Details

### 1. getAllTasksForUser(userId: Int)
```sql
SELECT * FROM Task 
WHERE User_ID = :userId 
  AND DeletedAt IS NULL 
ORDER BY CreatedAt DESC
```
**Returns:** All non-deleted tasks for the user, newest first

### 2. getTodayTasks(userId: Int, date: String)
```sql
SELECT * FROM Task 
WHERE User_ID = :userId 
  AND DueAt = :date 
  AND DeletedAt IS NULL 
ORDER BY DueTime ASC
```
**Returns:** Tasks due today, sorted by time

### 3. getPendingTasks(userId: Int)
```sql
SELECT * FROM Task 
WHERE User_ID = :userId 
  AND Status IN ('Pending', 'InProgress') 
  AND DeletedAt IS NULL 
ORDER BY DueAt ASC
```
**Returns:** Incomplete tasks, sorted by due date

### 4. getCompletedTasks(userId: Int)
```sql
SELECT * FROM Task 
WHERE User_ID = :userId 
  AND Status = 'Completed' 
  AND DeletedAt IS NULL 
ORDER BY UpdatedAt DESC
```
**Returns:** Completed tasks, newest completion first

## UI Element Mapping

### Layout: fragment_tasks.xml
```
┌─────────────────────────────────────────────────────────┐
│  📋 Tasks                                          [+]   │
│  3 total tasks                                          │
├─────────────────────────────────────────────────────────┤
│  🔍 [Search Tasks.............]            [Sort]       │
│  3 found                                                │
├─────────────────────────────────────────────────────────┤
│  [All (3)] [Today (1)] [Pending (2)] [Done (1)]         │
├─────────────────────────────────────────────────────────┤
│  ┌───────────────────────────────────────────────────┐  │
│  │  Complete Android App              ← tvTaskTitle │  │
│  │  Finish the CheermateApp project                 │  │
│  │                            ← tvTaskDescription   │  │
│  │  Priority: 🔴 High          ← tvTaskPriority     │  │
│  │  Status: ⏳ Pending         ← tvTaskStatus       │  │
│  │  Due: Sep 29, 2025 at 2:30 PM ← tvTaskDueDate   │  │
│  │  Progress: 75%              ← tvTaskProgress     │  │
│  │                                                   │  │
│  │  [◀ Previous]  1 / 3  [Next ▶]    ← Navigation   │  │
│  │                                                   │  │
│  │  [✅ Mark as Done]              ← btnMarkDone    │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

## State Management

### Variables
```kotlin
private var currentFilter = FilterType.ALL
private var userId: Int = 0
private var currentTasks = mutableListOf<Task>()        // All loaded tasks
private var allTasks = mutableListOf<Task>()            // Backup for search
private var currentDisplayedTask: Task? = null          // Currently shown task
private var currentTaskIndex: Int = 0                   // Index in filtered list
private var filteredTasks: List<Task> = emptyList()     // Tasks matching filter
```

### State Transitions
```
Initial State:
  currentFilter = ALL
  currentTaskIndex = 0
  filteredTasks = empty

After Tab Click (e.g., "Today"):
  currentFilter = TODAY
  currentTaskIndex = 0
  filteredTasks = [tasks due today]
  currentDisplayedTask = filteredTasks[0]

After Navigation (e.g., "Next"):
  currentFilter = TODAY (unchanged)
  currentTaskIndex = 1
  filteredTasks = [tasks due today] (unchanged)
  currentDisplayedTask = filteredTasks[1]

After "Mark as Done":
  currentFilter = TODAY (unchanged)
  Database updated (Status = Completed, Progress = 100)
  Reload filter → filteredTasks refreshed
  currentTaskIndex = 0 (reset to first)
  Task may move to "Done" tab
```

## Extension Methods for Display

### Task.getPriorityText()
```kotlin
Priority.High   → "🔴 High"
Priority.Medium → "🟡 Medium"
Priority.Low    → "🟢 Low"
```

### Task.getStatusText()
```kotlin
Status.Pending    → "⏳ Pending"
Status.InProgress → "🔄 In Progress"
Status.Completed  → "✅ Completed"
Status.OverDue    → "🔴 Overdue"
Status.Cancelled  → "❌ Cancelled"
```

### Task.getFormattedDueDateTime()
```kotlin
DueAt = "2025-09-29", DueTime = "14:30"
→ "Sep 29, 2025 at 2:30 PM"

DueAt = "2025-09-30", DueTime = null
→ "Sep 30, 2025"
```

### Task.getPriorityColor()
```kotlin
Priority.High   → Color.RED
Priority.Medium → Color.ORANGE
Priority.Low    → Color.GREEN
```

## Summary

This data flow ensures that:
1. ✅ Tab buttons fetch different data from database
2. ✅ All UI fields display real database data
3. ✅ Navigation works smoothly between tasks
4. ✅ Mark as Done updates database and refreshes UI
5. ✅ User gets immediate visual feedback for all actions
