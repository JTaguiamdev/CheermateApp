# Phase 2 Quick Reference Guide

Quick reference for using Phase 2 features in CheermateApp v1.5

---

## 🔄 Recurring Tasks

### Create a Recurring Task
```kotlin
// Get next ID
val recurringTaskId = db.recurringTaskDao().getNextRecurringTaskId(userId)

// Create daily recurring task
val dailyTask = RecurringTask.create(
    recurringTaskId = recurringTaskId,
    userId = userId,
    title = "Daily Exercise",
    description = "30 minutes of cardio",
    priority = Priority.Medium,
    frequency = RecurringFrequency.DAILY,
    startDate = "2025-01-01",
    timeOfDay = "07:00"
)
db.recurringTaskDao().insert(dailyTask)

// Create weekly recurring task (every Monday)
val weeklyTask = RecurringTask.create(
    recurringTaskId = recurringTaskId,
    userId = userId,
    title = "Team Meeting",
    frequency = RecurringFrequency.WEEKLY,
    startDate = "2025-01-01",
    dayOfWeek = 1, // Monday
    timeOfDay = "10:00"
)
db.recurringTaskDao().insert(weeklyTask)
```

### Check and Generate Tasks
```kotlin
// Get all active recurring tasks
val recurringTasks = db.recurringTaskDao().getActiveRecurringTasks(userId)

for (recurringTask in recurringTasks) {
    if (recurringTask.shouldGenerateToday()) {
        // Generate the actual task
        val taskId = db.taskDao().getNextTaskIdForUser(userId)
        val nextOccurrence = recurringTask.getNextOccurrence()
        
        val task = Task.create(
            taskId = taskId,
            userId = userId,
            title = recurringTask.Title,
            description = recurringTask.Description,
            priority = recurringTask.Priority,
            dueAt = Task.dateToString(nextOccurrence ?: Date()),
            dueTime = recurringTask.TimeOfDay
        )
        
        db.taskDao().insert(task)
        
        // Update last generated date
        db.recurringTaskDao().updateLastGenerated(
            userId,
            recurringTask.RecurringTask_ID,
            Task.dateToString(Date())
        )
    }
}
```

---

## 📋 Task Templates

### Use Predefined Templates
```kotlin
// Load default templates for a new user
val defaultTemplates = TaskTemplate.getDefaultTemplates(userId)
db.taskTemplateDao().insertAll(defaultTemplates)

// Get all templates
val templates = db.taskTemplateDao().getAllTemplates(userId)

// Get template by ID
val template = db.taskTemplateDao().getTemplateById(userId, templateId)

// Create task from template
if (template != null) {
    val taskId = db.taskDao().getNextTaskIdForUser(userId)
    val task = template.toTask(
        taskId = taskId,
        dueDate = "2025-01-15",
        dueTime = "14:00"
    )
    
    db.taskDao().insert(task)
    db.taskTemplateDao().incrementUsageCount(userId, templateId)
}
```

### Create Custom Template
```kotlin
val templateId = db.taskTemplateDao().getNextTemplateId(userId)

val customTemplate = TaskTemplate.create(
    templateId = templateId,
    userId = userId,
    name = "Morning Routine",
    description = "Daily morning tasks",
    category = "Personal",
    title = "Complete Morning Routine",
    taskDescription = "Meditation, breakfast, planning",
    priority = Priority.High,
    estimatedDuration = 60,
    defaultDueInDays = 0
)

db.taskTemplateDao().insert(customTemplate)
```

---

## 🔗 Task Dependencies

### Add Task Dependency
```kotlin
// Check for circular dependency first
val wouldBeCircular = db.taskDependencyDao().wouldCreateCircularDependency(
    userId, taskId = 2, dependsOnTaskId = 1
)

if (!wouldBeCircular) {
    val dependency = TaskDependency.create(
        taskId = 2,           // Task 2 depends on...
        userId = userId,
        dependsOnTaskId = 1,  // ...Task 1
        dependsOnUserId = userId
    )
    db.taskDependencyDao().insert(dependency)
}
```

### Check Prerequisites
```kotlin
// Get all prerequisites for a task
val prerequisites = db.taskDependencyDao().getPrerequisiteTasks(userId, taskId)

// Check if task can be started
val canStart = db.taskDependencyDao().canTaskBeStarted(userId, taskId)

if (!canStart) {
    val incompleteCount = db.taskDependencyDao().countIncompletePrerequisites(userId, taskId)
    println("Complete $incompleteCount prerequisites first")
}
```

---

## 🌙 Dark Mode

### Initialize on App Start
```kotlin
// In Application onCreate()
ThemeManager.initializeTheme(this)
```

### Toggle Dark Mode
```kotlin
// Get current mode
val currentMode = ThemeManager.getThemeMode(context)
val isDark = ThemeManager.isDarkModeActive(context)

// Set specific mode
ThemeManager.setThemeMode(context, ThemeManager.THEME_DARK)
ThemeManager.setThemeMode(context, ThemeManager.THEME_LIGHT)
ThemeManager.setThemeMode(context, ThemeManager.THEME_SYSTEM)

// Toggle
ThemeManager.toggleDarkMode(context)

// In Activity after theme change
recreate() // Apply theme immediately
```

---

## 📊 Analytics

### Calculate Trends
```kotlin
// Get all tasks for user
val tasks = db.taskDao().getAllTasksForUser(userId)

// Weekly trends
val weeklyTrends = AnalyticsUtil.calculateWeeklyTrends(tasks, weeksCount = 4)
weeklyTrends.forEach { trend ->
    println("${trend.period}: ${trend.completedTasks}/${trend.totalTasks} (${trend.completionRate}%)")
}

// Daily analytics for last 7 days
val startDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.time
val endDate = Date()
val dailyAnalytics = AnalyticsUtil.calculateDailyAnalytics(tasks, startDate, endDate)

// Priority distribution
val distribution = AnalyticsUtil.calculatePriorityDistribution(tasks)
println("High: ${distribution.highPriority}, Medium: ${distribution.mediumPriority}, Low: ${distribution.lowPriority}")

// Current streak
val streak = AnalyticsUtil.calculateCurrentStreak(tasks)
println("Current streak: $streak days 🔥")

// Average completion time
val avgTime = AnalyticsUtil.calculateAverageCompletionTime(tasks)
if (avgTime != null) {
    println("Average: ${AnalyticsUtil.formatDuration(avgTime)}")
}
```

---

## 💾 Data Export/Import

### Export Tasks
```kotlin
// Export to JSON
val jsonData = DataExportImport.exportToJson(tasks)
val jsonFile = DataExportImport.writeToFile(context, "my_tasks.json", jsonData)

// Export to CSV
val csvData = DataExportImport.exportToCsv(tasks)
val csvFile = DataExportImport.writeToFile(context, "my_tasks.csv", csvData)

// Create automatic backup
val backupFile = DataExportImport.createBackup(context, tasks, format = "json")
if (backupFile != null) {
    Toast.makeText(context, "Backup saved: ${backupFile.name}", Toast.LENGTH_SHORT).show()
}
```

### Import Tasks
```kotlin
// Import from JSON file
val importedTasks = DataExportImport.restoreFromBackup(file, format = "json")

if (importedTasks != null) {
    // Insert imported tasks
    importedTasks.forEach { task ->
        try {
            db.taskDao().insert(task)
        } catch (e: Exception) {
            // Handle conflicts
        }
    }
    Toast.makeText(context, "Imported ${importedTasks.size} tasks", Toast.LENGTH_SHORT).show()
}
```

---

## 📦 Bulk Operations

### Bulk Update Status
```kotlin
val taskIds = listOf(1, 2, 3, 4, 5)

val result = BulkTaskOperations.bulkUpdateStatus(
    taskDao = db.taskDao(),
    userId = userId,
    taskIds = taskIds,
    newStatus = Status.Completed
)

println("Success: ${result.successCount}, Failed: ${result.failureCount}")
result.errors.forEach { error -> println(error) }
```

### Bulk Update Priority
```kotlin
val result = BulkTaskOperations.bulkUpdatePriority(
    taskDao = db.taskDao(),
    userId = userId,
    taskIds = taskIds,
    newPriority = Priority.High
)
```

### Bulk Delete
```kotlin
val result = BulkTaskOperations.bulkDelete(
    taskDao = db.taskDao(),
    userId = userId,
    taskIds = taskIds
)
```

### Filter for Bulk Selection
```kotlin
val allTasks = db.taskDao().getAllTasksForUser(userId)

// Get all overdue high priority tasks
val overdueHighPriority = BulkTaskOperations.filterTasksForBulkSelection(
    tasks = allTasks,
    status = Status.Pending,
    priority = Priority.High,
    isOverdue = true
)

// Bulk complete them
val taskIds = overdueHighPriority.map { it.Task_ID }
BulkTaskOperations.bulkComplete(db.taskDao(), userId, taskIds)
```

---

## 🎯 Common Use Cases

### Daily Task Generation from Recurring Tasks
```kotlin
// Run this daily (e.g., in a WorkManager job)
lifecycleScope.launch {
    val recurringTasks = withContext(Dispatchers.IO) {
        db.recurringTaskDao().getActiveRecurringTasks(userId)
    }
    
    recurringTasks.filter { it.shouldGenerateToday() }.forEach { recurringTask ->
        val taskId = withContext(Dispatchers.IO) {
            db.taskDao().getNextTaskIdForUser(userId)
        }
        
        val task = Task.create(
            taskId = taskId,
            userId = userId,
            title = recurringTask.Title,
            description = recurringTask.Description,
            priority = recurringTask.Priority,
            dueAt = RecurringTask.getCurrentDateString(),
            dueTime = recurringTask.TimeOfDay
        )
        
        withContext(Dispatchers.IO) {
            db.taskDao().insert(task)
            db.recurringTaskDao().updateLastGenerated(
                userId,
                recurringTask.RecurringTask_ID,
                RecurringTask.getCurrentDateString()
            )
        }
    }
}
```

### Weekly Analytics Report
```kotlin
lifecycleScope.launch {
    val tasks = withContext(Dispatchers.IO) {
        db.taskDao().getAllTasksForUser(userId)
    }
    
    val trends = AnalyticsUtil.calculateWeeklyTrends(tasks, weeksCount = 1)
    val thisWeek = trends.lastOrNull()
    val streak = AnalyticsUtil.calculateCurrentStreak(tasks)
    
    val report = """
        📊 Weekly Report
        
        Tasks Completed: ${thisWeek?.completedTasks}/${thisWeek?.totalTasks}
        Completion Rate: ${thisWeek?.completionRate?.toInt()}%
        Current Streak: $streak days
    """.trimIndent()
    
    // Show in dialog or notification
}
```

---

## 🐛 Error Handling

### Safe Dependency Creation
```kotlin
try {
    val wouldBeCircular = db.taskDependencyDao().wouldCreateCircularDependency(
        userId, taskId, dependsOnTaskId
    )
    
    if (wouldBeCircular) {
        Toast.makeText(context, "Cannot create circular dependency", Toast.LENGTH_SHORT).show()
        return
    }
    
    val dependency = TaskDependency.create(taskId, userId, dependsOnTaskId, userId)
    db.taskDependencyDao().insert(dependency)
    Toast.makeText(context, "Dependency added", Toast.LENGTH_SHORT).show()
    
} catch (e: Exception) {
    Log.e("TaskDependency", "Error creating dependency", e)
    Toast.makeText(context, "Failed to add dependency", Toast.LENGTH_SHORT).show()
}
```

### Safe Bulk Operations
```kotlin
lifecycleScope.launch {
    val result = withContext(Dispatchers.IO) {
        BulkTaskOperations.bulkUpdateStatus(
            taskDao = db.taskDao(),
            userId = userId,
            taskIds = selectedTaskIds,
            newStatus = Status.Completed
        )
    }
    
    if (result.failureCount > 0) {
        val message = "Updated ${result.successCount} tasks. ${result.failureCount} failed."
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
        
        // Show errors in log
        result.errors.forEach { error ->
            Log.e("BulkOperation", error)
        }
    } else {
        Toast.makeText(this@MainActivity, "All tasks updated successfully", Toast.LENGTH_SHORT).show()
    }
}
```

---

## 📚 Additional Resources

- **Detailed Guide:** [PHASE2_IMPLEMENTATION.md](PHASE2_IMPLEMENTATION.md)
- **Implementation Summary:** [PHASE2_SUMMARY.md](PHASE2_SUMMARY.md)
- **Testing Guide:** [TESTING_CHECKLIST.md](TESTING_CHECKLIST.md)
- **Roadmap:** [ROADMAP.md](ROADMAP.md)

---

**Last Updated:** January 2025  
**Version:** 1.5
