package com.cheermateapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.cheermateapp.data.model.Status
import androidx.lifecycle.lifecycleScope
import com.cheermateapp.data.db.AppDb
import com.cheermateapp.data.model.Task
import com.cheermateapp.data.model.Priority
import com.cheermateapp.data.model.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class FragmentTaskActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USER_ID = "USER_ID"
        const val EXTRA_SHOW_DASHBOARD = "SHOW_DASHBOARD"
    }

    // ✅ Original Variables
    private lateinit var tvTasksTitle: TextView
    private lateinit var tvTasksSub: TextView
    private lateinit var etSearch: EditText
    private lateinit var btnSort: TextView
    private lateinit var chipFound: TextView
    private lateinit var tabAll: TextView
    private lateinit var tabToday: TextView
    private lateinit var tabPending: TextView
    private lateinit var tabDone: TextView
    
    // NEW: RecyclerView and adapter
    private lateinit var recyclerViewTasks: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var taskListAdapter: TaskListAdapter
    
    // FAB button
    private lateinit var fabAddTask: com.google.android.material.floatingactionbutton.FloatingActionButton
    
    // Progress card elements
    private var progressSubtitle: TextView? = null
    private var progressPercent: TextView? = null
    private var progressCompleted: View? = null

    private var currentFilter = FilterType.ALL
    private var userId: Int = 0
    private var currentTasks = mutableListOf<Task>()
    private var allTasks = mutableListOf<Task>()
    private var filteredTasks: List<Task> = emptyList()

    enum class FilterType {
        ALL, TODAY, PENDING, DONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.cheermateapp.util.ThemeManager.initializeTheme(this)
        setContentView(R.layout.fragment_tasks)

        userId = intent?.getStringExtra(EXTRA_USER_ID)?.toIntOrNull() ?: 0
        android.util.Log.d("FragmentTaskActivity", "🔍 onCreate: userId = $userId")

        initializeViews()
        loadTasks()
        debugAllTasks()
        debugTaskLoading()
        
        // ✅ OBSERVE TASK CHANGES FOR LIVE PROGRESS BAR UPDATES
        observeTaskChangesForProgressBar()
    }

    // ✅ FIXED: Proper loadTasks method
    private fun loadTasks() {
        lifecycleScope.launch {
            try {
                createTestTasks()
                debugDatabaseContent()

                val db = AppDb.get(this@FragmentTaskActivity)
                val allTasks = withContext(Dispatchers.IO) {
                    db.taskDao().getAllTasksForUser(userId)
                }

                android.util.Log.d("FragmentTaskActivity", "Loaded ${allTasks.size} tasks for user $userId")

                currentTasks.clear()
                currentTasks.addAll(allTasks)

                filterTasks(currentFilter)
                updateTabCounts()

            } catch (e: Exception) {
                android.util.Log.e("FragmentTaskActivity", "Error loading tasks", e)
                Toast.makeText(this@FragmentTaskActivity, "Error loading tasks: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ✅ ADD: Debug method
    private fun debugTaskLoading() {
        lifecycleScope.launch {
            try {
                android.util.Log.d("DEBUG", "=== TASK LOADING DEBUG ===")
                android.util.Log.d("DEBUG", "User ID: $userId")

                val db = AppDb.get(this@FragmentTaskActivity)

                // Check if database exists
                android.util.Log.d("DEBUG", "Database created successfully.")

                // Check all tasks in database
                val allTasksInDb = withContext(Dispatchers.IO) {
                    db.taskDao().getAllTasks() // Get ALL tasks regardless of user
                }
                android.util.Log.d("DEBUG", "Total tasks in entire database: ${allTasksInDb.size}")

                // Check tasks for this user
                val userTasks = withContext(Dispatchers.IO) {
                    db.taskDao().getAllTasksForUser(userId)
                }
                android.util.Log.d("DEBUG", "Tasks for user $userId: ${userTasks.size}")

                // List each task
                userTasks.forEachIndexed { index, task ->
                    android.util.Log.d("DEBUG", "Task $index: ${task.Title} (ID: ${task.Task_ID}, UserID: ${task.User_ID})")
                }

                android.util.Log.d("DEBUG", "Current tasks list size: ${currentTasks.size}")
                android.util.Log.d("DEBUG", "Filtered tasks list size: ${filteredTasks.size}")
                android.util.Log.d("DEBUG", "=== END DEBUG ===")

            } catch (e: Exception) {
                android.util.Log.e("DEBUG", "Debug error: ${e.message}", e)
            }
        }
    }

    // ✅ UPDATED: Initialize all views including RecyclerView
    private fun initializeViews() {
        try {
            // Original views
            tvTasksTitle = findViewById(R.id.tvTasksTitle)
            tvTasksSub = findViewById(R.id.tvTasksSub)
            etSearch = findViewById(R.id.etSearch)
            btnSort = findViewById(R.id.btnSort)
            chipFound = findViewById(R.id.chipFound)
            tabAll = findViewById(R.id.tabAll)
            tabToday = findViewById(R.id.tabToday)
            tabPending = findViewById(R.id.tabPending)
            tabDone = findViewById(R.id.tabDone)

            // NEW: RecyclerView and related views
            recyclerViewTasks = findViewById(R.id.recyclerViewTasks)
            tvEmptyState = findViewById(R.id.tvEmptyState)

            // FAB button
            fabAddTask = findViewById(R.id.fabAddTask)
            
            // Progress card elements (optional - may not exist in all layouts)
            progressSubtitle = findViewById(R.id.progressSubtitle)
            progressPercent = findViewById(R.id.progressPercent)
            progressCompleted = findViewById(R.id.progressCompleted)

            // Setup RecyclerView
            recyclerViewTasks.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
            taskListAdapter = TaskListAdapter(
                emptyList(),
                onTaskClick = { task ->
                    showTaskDetailsDialog(task)
                },
                onTaskUpdate = { task, category, priority, dueDate ->
                    updateTask(task, category, priority, dueDate)
                }
            )
            recyclerViewTasks.adapter = taskListAdapter

            setupInteractions()

            android.util.Log.d("FragmentTaskActivity", "✅ All views initialized successfully")
        } catch (e: Exception) {
            android.util.Log.e("FragmentTaskActivity", "Error initializing views", e)
            Toast.makeText(this, "Error initializing task layout", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ FIXED: Setup interactions with proper navigation
    private fun setupInteractions() {
        try {
            // FAB click listener
            fabAddTask.setOnClickListener {
                android.util.Log.d("FAB_DEBUG", "===== FAB CLICKED =====")
                android.util.Log.d("FAB_DEBUG", "FAB ID: ${fabAddTask.id}")
                android.util.Log.d("FAB_DEBUG", "Calling showAddTaskDialog()")
                showAddTaskDialog()
            }

            btnSort.setOnClickListener {
                showSortOptionsDialog()
            }

            etSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    searchTasks(s?.toString())
                }
            })

            // Filter tabs
            tabAll.setOnClickListener {
                android.util.Log.d("FragmentTaskActivity", "🔍 ALL tab clicked")
                setFilter(FilterType.ALL)
                updateTabSelection(tabAll)
            }

            tabToday.setOnClickListener {
                android.util.Log.d("FragmentTaskActivity", "🔍 TODAY tab clicked")
                setFilter(FilterType.TODAY)
                updateTabSelection(tabToday)
            }

            tabPending.setOnClickListener {
                android.util.Log.d("FragmentTaskActivity", "🔍 PENDING tab clicked")
                setFilter(FilterType.PENDING)
                updateTabSelection(tabPending)
            }

            tabDone.setOnClickListener {
                android.util.Log.d("FragmentTaskActivity", "🔍 DONE tab clicked")
                setFilter(FilterType.DONE)
                updateTabSelection(tabDone)
            }

            updateTabSelection(tabAll)

            android.util.Log.d("FragmentTaskActivity", "✅ Interactions setup complete")
        } catch (e: Exception) {
            android.util.Log.e("FragmentTaskActivity", "Error setting up interactions", e)
        }
    }

    // ✅ FIXED: All navigation methods at class level



    // ✅ UPDATED: Display tasks with navigation support
    private fun displayTaskInCard(tasks: List<Task>) {
        try {
            filteredTasks = tasks
            currentTasks.clear()
            currentTasks.addAll(tasks)

            if (tasks.isEmpty()) {
                showEmptyState()
            } else {
                showTasksList(tasks)
            }
        } catch (e: Exception) {
            android.util.Log.e("FragmentTaskActivity", "Error displaying tasks", e)
            showEmptyState()
        }
    }

    // NEW: Show tasks in RecyclerView
    private fun showTasksList(tasks: List<Task>) {
        recyclerViewTasks.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
        taskListAdapter.updateTasks(tasks)
    }


    // ✅ NEW: Mark task as done (UPDATE OPERATION)
    // ✅ UPDATE TASK WITH CATEGORY, PRIORITY, AND DUE DATE
    private fun updateTask(task: Task, category: Category?, priority: Priority?, dueDate: String?) {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentTaskActivity)
                withContext(Dispatchers.IO) {
                    // Create updated task
                    val updatedTask = task.copy(
                        Category = category ?: task.Category,
                        Priority = priority ?: task.Priority,
                        DueAt = dueDate ?: task.DueAt,
                        UpdatedAt = System.currentTimeMillis()
                    )
                    
                    // Update in database
                    db.taskDao().update(updatedTask)
                }

                Toast.makeText(this@FragmentTaskActivity, "✅ Task '${task.Title}' updated!", Toast.LENGTH_SHORT).show()

                // Reload current filter to refresh display
                filterTasks(currentFilter)

            } catch (e: Exception) {
                android.util.Log.e("FragmentTaskActivity", "Error updating task", e)
                Toast.makeText(this@FragmentTaskActivity, "Error updating task", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun markTaskAsDone(task: Task) {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentTaskActivity)
                withContext(Dispatchers.IO) {
                    db.taskDao().updateTaskStatus(task.User_ID, task.Task_ID, "Completed")
                    db.taskDao().updateTaskProgress(task.User_ID, task.Task_ID, 100)
                }

                Toast.makeText(this@FragmentTaskActivity, "✅ Task '${task.Title}' completed!", Toast.LENGTH_SHORT).show()

                // ✅ FIXED: Update progress bar immediately before reloading tasks
                updateTabCounts()

                // Reload current filter to refresh display
                filterTasks(currentFilter)

            } catch (e: Exception) {
                android.util.Log.e("FragmentTaskActivity", "Error marking task as done", e)
                Toast.makeText(this@FragmentTaskActivity, "Error completing task", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✅ FIXED: Extension methods with proper private modifier
    private fun Task.getPriorityText(): String {
        return when (this.Priority) {
            com.cheermateapp.data.model.Priority.High -> "🔴 High"
            com.cheermateapp.data.model.Priority.Medium -> "🟡 Medium"
            com.cheermateapp.data.model.Priority.Low -> "🟢 Low"
        }
    }

    private fun Task.getStatusText(): String {
        return when (this.Status) {
            com.cheermateapp.data.model.Status.Pending -> "⏳ Pending"
            com.cheermateapp.data.model.Status.InProgress -> "🔄 In Progress"
            com.cheermateapp.data.model.Status.OverDue -> "🔴 Overdue"
            com.cheermateapp.data.model.Status.Done -> "✅ Done"
            com.cheermateapp.data.model.Status.Cancelled -> "❌ Cancelled"
        }
    }

    private fun Task.getFormattedDueDateTime(): String {
        return try {
            if (DueAt != null) {
                val dateStr = DueAt
                val timeStr = DueTime

                val parsedDate: Date? = try {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                } catch (e: java.text.ParseException) {
                    try {
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).parse(dateStr)
                    } catch (e2: java.text.ParseException) {
                        null
                    }
                }
                
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val formattedDate = if (parsedDate != null) outputFormat.format(parsedDate) else dateStr
                
                val formattedTime = if (!timeStr.isNullOrBlank() && timeStr != "00:00") {
                    " at $timeStr"
                } else {
                    ""
                }
                
                "$formattedDate$formattedTime"
            } else {
                "No due date"
            }
        } catch (e: Exception) {
            android.util.Log.e("Task", "Error formatting date: ${e.message}", e)
            "Date not available"
        }
    }

    private fun Task.getStatusEmoji(): String {
        return when (this.Status) {
            com.cheermateapp.data.model.Status.Pending -> "⏳"
            com.cheermateapp.data.model.Status.InProgress -> "🔄"
            com.cheermateapp.data.model.Status.OverDue -> "🔴"
            com.cheermateapp.data.model.Status.Done -> "✅"
            com.cheermateapp.data.model.Status.Cancelled -> "❌"
        }
    }

    private fun Task.getPriorityColor(): Int {
        return when (this.Priority) {
            com.cheermateapp.data.model.Priority.High -> android.graphics.Color.RED
            com.cheermateapp.data.model.Priority.Medium -> android.graphics.Color.parseColor("#FFA500") // Orange
            com.cheermateapp.data.model.Priority.Low -> android.graphics.Color.GREEN
        }
    }

    private fun createRoundedDrawable(color: Int): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            setColor(color)
            cornerRadius = 12f
        }
    }

    // ✅ FIXED: Corrected showEmptyState method
    private fun showEmptyState() {
        try {
            filteredTasks = emptyList()
            currentTasks.clear()

            recyclerViewTasks.visibility = View.GONE
            tvEmptyState.visibility = View.VISIBLE

            val emptyMessage = when (currentFilter) {
                FilterType.ALL -> "📋 No tasks available\n\nTap the + button to create your first task!"
                FilterType.TODAY -> "📅 No tasks due today\n\nGreat! You're all caught up for today!"
                FilterType.PENDING -> "⏳ No pending tasks\n\nAll taskgit s are either completed or not yet assigned!"
                FilterType.DONE -> "✅ No completed tasks yet\n\nStart completing tasks to see them here!"
            }

            tvEmptyState.text = emptyMessage

            android.util.Log.d("FragmentTaskActivity", "📋 Showing empty state")

        } catch (e: Exception) {
            android.util.Log.e("FragmentTaskActivity", "Error showing empty state", e)
        }
    }

    // ✅ EXTENSION METHODS
    private fun Task.getSummary(): String {
        val statusEmoji = this.getStatusEmoji()
        val priorityEmoji = when (this.Priority) {
            com.cheermateapp.data.model.Priority.High -> "🔴"
            com.cheermateapp.data.model.Priority.Medium -> "🟡"
            com.cheermateapp.data.model.Priority.Low -> "🟢"
        }
        return "$statusEmoji $priorityEmoji ${this.Title}"
    }

    private fun Task.isOverdue(): Boolean {
        return Status == com.cheermateapp.data.model.Status.OverDue
    }

    private fun Task.isToday(): Boolean {
        return try {
            if (DueAt != null) {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                DueAt == today
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // ✅ DEBUG METHODS
    private fun debugAllTasks() {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentTaskActivity)
                val allTasks = withContext(Dispatchers.IO) {
                    db.taskDao().getAllTasksForUser(userId)
                }

                android.util.Log.d("FragmentTaskActivity", "🔍 === DATABASE DEBUG ===")
                android.util.Log.d("FragmentTaskActivity", "🔍 User ID: $userId")
                android.util.Log.d("FragmentTaskActivity", "🔍 Total tasks in DB: ${allTasks.size}")

                allTasks.forEachIndexed { index, task ->
                    android.util.Log.d("FragmentTaskActivity", "🔍 Task $index: ${task.Title}")
                    android.util.Log.d("FragmentTaskActivity", "   - Status: ${task.Status}")
                    android.util.Log.d("FragmentTaskActivity", "   - User_ID: ${task.User_ID}")
                    android.util.Log.d("FragmentTaskActivity", "   - DueAt: ${task.DueAt}")
                    android.util.Log.d("FragmentTaskActivity", "   - DeletedAt: ${task.DeletedAt}")
                }
                android.util.Log.d("FragmentTaskActivity", "🔍 === END DEBUG ===")

            } catch (e: Exception) {
                android.util.Log.e("FragmentTaskActivity", "❌ Debug error", e)
            }
        }
    }

    private suspend fun debugDatabaseContent() {
        try {
            val db = AppDb.get(this)
            val allTasks = withContext(Dispatchers.IO) {
                db.taskDao().getAllTasksForUser(userId)
            }

            android.util.Log.d("FragmentTaskActivity", "=== DATABASE DEBUG ===")
            android.util.Log.d("FragmentTaskActivity", "User ID: $userId")
            android.util.Log.d("FragmentTaskActivity", "Total tasks in DB: ${allTasks.size}")

            allTasks.forEachIndexed { index, task ->
                android.util.Log.d("FragmentTaskActivity", "Task $index: ${task.Title} (Status: ${task.Status}, DeletedAt: ${task.DeletedAt})")
            }
            android.util.Log.d("FragmentTaskActivity", "=== END DEBUG ===")

        } catch (e: Exception) {
            android.util.Log.e("FragmentTaskActivity", "Debug error", e)
        }
    }

    // ✅ CRUD OPERATION: CREATE - Create test tasks
    private suspend fun createTestTasks() {
        try {
            val db = AppDb.get(this)

            val existingTasks = withContext(Dispatchers.IO) {
                db.taskDao().getAllTasksForUser(userId)
            }

            android.util.Log.d("FragmentTaskActivity", "Existing tasks for user $userId: ${existingTasks.size}")

            if (existingTasks.isEmpty()) {
                android.util.Log.d("FragmentTaskActivity", "Creating test tasks for user $userId")

                withContext(Dispatchers.IO) {
                    val currentTime = System.currentTimeMillis()

                    // ✅ FIXED: Use Long timestamps instead of String dates
                    val task1 = Task(
                        Task_ID = 1,
                        User_ID = userId,
                        Title = "Complete Android App",
                        Description = "Finish the CheermateApp project",
                        Priority = Priority.High,
                        Status = Status.Pending,
                        TaskProgress = 75,
                        DueAt = "2025-09-29",
                        DueTime = "14:30",
                        CreatedAt = currentTime,  // ✅ Long timestamp
                        UpdatedAt = currentTime,  // ✅ Long timestamp
                        DeletedAt = null
                    )

                    val task2 = Task(
                        Task_ID = 2,
                        User_ID = userId,
                        Title = "Study for Exam",
                        Description = "Computer Science midterm exam",
                        Priority = Priority.Medium,
                        Status = Status.InProgress,
                        TaskProgress = 50,
                        DueAt = "2025-09-30",
                        DueTime = "10:00",
                        CreatedAt = currentTime,  // ✅ Long timestamp
                        UpdatedAt = currentTime,  // ✅ Long timestamp
                        DeletedAt = null
                    )

                    val task3 = Task(
                        Task_ID = 3,
                        User_ID = userId,
                        Title = "Buy Groceries",
                        Description = "Weekly shopping list",
                        Priority = Priority.Low,
                        Status = Status.Done,
                        TaskProgress = 100,
                        DueAt = "2025-09-28",
                        DueTime = "16:00",
                        CreatedAt = currentTime - 86400000L,  // ✅ 1 day ago (Long timestamp)
                        UpdatedAt = currentTime,  // ✅ Long timestamp
                        DeletedAt = null
                    )

                    db.taskDao().insert(task1)
                    db.taskDao().insert(task2)
                    db.taskDao().insert(task3)

                    android.util.Log.d("FragmentTaskActivity", "✅ Created 3 test tasks successfully")
                }
            }

        } catch (e: Exception) {
            android.util.Log.e("FragmentTaskActivity", "❌ Error creating test tasks: ${e.message}", e)
        }
    }

    // ✅ FILTERING AND SEARCH METHODS
    private fun updateTabSelection(selectedTab: TextView) {
        try {
            listOf(tabAll, tabToday, tabPending, tabDone).forEach { tab ->
                tab.setBackgroundResource(android.R.color.transparent)
                tab.alpha = 0.7f
            }

            selectedTab.setBackgroundResource(R.drawable.bg_chip_glass)
            selectedTab.alpha = 1.0f
        } catch (e: Exception) {
            android.util.Log.e("FragmentTaskActivity", "Error updating tab selection", e)
        }
    }

    private fun setFilter(filterType: FilterType) {
        currentFilter = filterType
        filterTasks(currentFilter)
    }

    // ✅ CRUD OPERATION: READ - Filter and display tasks
    private fun filterTasks(filterType: FilterType) {
        android.util.Log.d("FragmentTaskActivity", "🔍 filterTasks called with: $filterType")
        android.util.Log.d("FragmentTaskActivity", "🔍 Current userId: $userId")

        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentTaskActivity)
                val tasks = withContext(Dispatchers.IO) {
                    when (filterType) {
                        FilterType.ALL -> {
                            android.util.Log.d("FragmentTaskActivity", "🔍 Getting ALL tasks...")
                            db.taskDao().getAllTasksForUser(userId)
                        }
                        FilterType.TODAY -> {
                            val todayStr = getCurrentDateString()
                            android.util.Log.d("FragmentTaskActivity", "🔍 Getting TODAY tasks for date: $todayStr")
                            db.taskDao().getTodayTasks(userId, todayStr)
                        }
                        FilterType.PENDING -> {
                            android.util.Log.d("FragmentTaskActivity", "🔍 Getting PENDING tasks...")
                            db.taskDao().getPendingTasks(userId)
                        }
                        FilterType.DONE -> {
                            android.util.Log.d("FragmentTaskActivity", "🔍 Getting COMPLETED tasks...")
                            db.taskDao().getCompletedTasks(userId)
                        }
                    }
                }

                android.util.Log.d("FragmentTaskActivity", "🔍 Filter $filterType returned ${tasks.size} tasks")

                allTasks.clear()
                allTasks.addAll(tasks)

                // ✅ Display filtered tasks in card
                displayTaskInCard(tasks)
                
                // ✅ Update chipFound with the filtered count immediately
                chipFound.text = "${tasks.size} found"
                
                updateTabCounts()

            } catch (e: Exception) {
                android.util.Log.e("FragmentTaskActivity", "❌ Error filtering tasks", e)
                Toast.makeText(this@FragmentTaskActivity, "Error filtering tasks: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateTabCounts() {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentTaskActivity)
                val counts = withContext(Dispatchers.IO) {
                    val todayStr = getCurrentDateString()
                    mapOf(
                        "all" to db.taskDao().getAllTasksCount(userId),
                        "today" to db.taskDao().getTodayTasksCount(userId, todayStr),
                        "pending" to db.taskDao().getPendingTasksCount(userId),
                        "done" to db.taskDao().getCompletedTasksCount(userId),
                        "inProgress" to db.taskDao().getInProgressTasksCount(userId)
                    )
                }

                tabAll.text = "All (${counts["all"]})"
                tabToday.text = "Today (${counts["today"]})"
                tabPending.text = "Pending (${counts["pending"]})"
                tabDone.text = "Done (${counts["done"]})"

                val totalTasks = counts["all"] ?: 0
                tvTasksSub.text = "$totalTasks total tasks"

                // ✅ Update progress card
                updateProgressCard(counts["done"] ?: 0, counts["inProgress"] ?: 0, counts["all"] ?: 0)

            } catch (e: Exception) {
                android.util.Log.e("FragmentTaskActivity", "Error updating tab counts", e)
            }
        }
    }
    
    // ✅ NEW: Update progress card with completion percentage
    private fun updateProgressCard(completed: Int, inProgress: Int, total: Int) {
        try {
            val percentage = if (total > 0) (completed * 100) / total else 0
            val inProgressPercentage = if (total > 0) (inProgress * 100) / total else 0

            progressSubtitle?.text = "$completed of $total tasks completed"
            progressPercent?.text = "$percentage%"

            val progressCompleted = findViewById<View>(R.id.progressCompleted)
            val progressInProgress = findViewById<View>(R.id.progressInProgress)

            // Update progress bar fill using weight
            progressCompleted?.layoutParams?.let { params ->
                if (params is LinearLayout.LayoutParams) {
                    params.weight = percentage.coerceAtLeast(0).toFloat()
                    progressCompleted.layoutParams = params
                }
            }

            progressInProgress?.layoutParams?.let { params ->
                if (params is LinearLayout.LayoutParams) {
                    params.weight = inProgressPercentage.coerceAtLeast(0).toFloat()
                    progressInProgress.layoutParams = params
                }
            }

            // Update the remainder weight
            val progressBar = progressCompleted?.parent as? LinearLayout
            if (progressBar != null && progressBar.childCount > 2) {
                val remainder = progressBar.getChildAt(2)
                remainder?.layoutParams?.let { remParams ->
                    if (remParams is LinearLayout.LayoutParams) {
                        remParams.weight = (100 - percentage - inProgressPercentage).toFloat()
                        remainder.layoutParams = remParams
                    }
                }
                // ✅ Request layout update to force redraw
                progressBar.requestLayout()
            }

            android.util.Log.d("FragmentTaskActivity", "✅ Progress updated: $percentage% completed, $inProgressPercentage% in progress ($completed/$inProgress/$total)")
        } catch (e: Exception) {
            android.util.Log.e("FragmentTaskActivity", "Error updating progress card", e)
        }
    }

    // ✅ OBSERVE TASK CHANGES FOR LIVE PROGRESS BAR UPDATES
    private fun observeTaskChangesForProgressBar() {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentTaskActivity)

                // Observe all tasks and completed tasks count
                kotlinx.coroutines.flow.combine(
                    db.taskDao().getAllTasksCountFlow(userId),
                    db.taskDao().getCompletedTasksCountFlow(userId),
                    db.taskDao().getInProgressTasksCountFlow(userId)
                ) { total, completed, inProgress ->
                    Triple(total, completed, inProgress)
                }.collect { (total, completed, inProgress) ->
                    // Update progress bar on main thread
                    withContext(Dispatchers.Main) {
                        updateProgressCard(completed, inProgress, total)
                        android.util.Log.d("FragmentTaskActivity", "🔄 Progress bar updated live: $completed/$inProgress/$total")
                    }
                }

            } catch (e: Exception) {
                android.util.Log.e("FragmentTaskActivity", "Error observing task changes for progress bar", e)
            }
        }
    }

    private fun searchTasks(query: String?) {
        if (query.isNullOrBlank()) {
            displayTaskInCard(allTasks)
            chipFound.text = "${allTasks.size} found"
        } else {
            val searchResults = allTasks.filter { task ->
                task.Title.contains(query, ignoreCase = true) ||
                        (task.Description?.contains(query, ignoreCase = true) == true)
            }
            displayTaskInCard(searchResults)
            
            // Display task titles in chipFound
            if (searchResults.isEmpty()) {
                chipFound.text = "No tasks found"
            } else if (searchResults.size == 1) {
                chipFound.text = "Found: ${searchResults[0].Title}"
            } else {
                // Show first task title and count
                chipFound.text = "Found: ${searchResults[0].Title} +${searchResults.size - 1} more"
            }
        }
    }

    private fun showSortOptionsDialog() {
        val sortOptions = arrayOf(
            "📅 Due Date",
            "🎯 Priority",
            "📝 Title",
            "📊 Status",
            "📈 Progress"
        )

        val dialog = AlertDialog.Builder(this)
            .setTitle("Sort Tasks By")
            .setItems(sortOptions) { _, which ->
                sortTasks(which)
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun sortTasks(sortType: Int) {
        try {
            val sortedTasks: List<Task> = when (sortType) {
                0 -> { // Due Date
                    currentTasks.sortedWith { task1, task2 ->
                        val date1 = task1.DueAt ?: ""
                        val date2 = task2.DueAt ?: ""
                        date1.compareTo(date2)
                    }
                }
                1 -> { // Priority (High first)
                    currentTasks.sortedWith { task1, task2 ->
                        val priority1 = when (task1.Priority) {
                            Priority.High -> 3
                            Priority.Medium -> 2
                            Priority.Low -> 1
                        }
                        val priority2 = when (task2.Priority) {
                            Priority.High -> 3
                            Priority.Medium -> 2
                            Priority.Low -> 1
                        }
                        priority2.compareTo(priority1)
                    }
                }
                2 -> { // Title A-Z
                    currentTasks.sortedWith { task1, task2 ->
                        task1.Title.compareTo(task2.Title, ignoreCase = true)
                    }
                }
                3 -> { // Status
                    currentTasks.sortedWith { task1, task2 ->
                        val status1 = when (task1.Status) {
                            Status.Pending -> 1
                            Status.InProgress -> 2
                            Status.OverDue -> 3
                            Status.Done -> 4
                            Status.Cancelled -> 5
                        }
                        val status2 = when (task2.Status) {
                            Status.Pending -> 1
                            Status.InProgress -> 2
                            Status.OverDue -> 3
                            Status.Done -> 4
                            Status.Cancelled -> 5
                        }
                        status1.compareTo(status2)
                    }
                }
                4 -> { // Progress (High first)
                    currentTasks.sortedWith { task1, task2 ->
                        task2.TaskProgress.compareTo(task1.TaskProgress)
                    }
                }
                else -> currentTasks
            }

            displayTaskInCard(sortedTasks)

            val sortNames = arrayOf("Due Date", "Priority", "Title", "Status", "Progress")
            val sortName = if (sortType in sortNames.indices) sortNames[sortType] else "Unknown"
            Toast.makeText(this, "📊 Sorted by $sortName", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            android.util.Log.e("FragmentTaskActivity", "Error sorting tasks", e)
            Toast.makeText(this, "Error sorting tasks", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ CRUD OPERATION: READ - Show task details dialog
    // Show task details in a dialog with custom layout
    private fun showTaskDetailsDialog(task: Task) {
        try {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_task_details, null)
            
            // Find views
            val layoutPriorityIndicator = dialogView.findViewById<View>(R.id.layoutPriorityIndicator)
            val tvTaskTitle = dialogView.findViewById<TextView>(R.id.tvTaskTitle)
            val tvTaskDescription = dialogView.findViewById<TextView>(R.id.tvTaskDescription)
            val tvTaskCategory = dialogView.findViewById<TextView>(R.id.tvTaskCategory)
            val tvTaskPriority = dialogView.findViewById<TextView>(R.id.tvTaskPriority)
            val tvTaskStatus = dialogView.findViewById<TextView>(R.id.tvTaskStatus)
            val tvTaskDueDate = dialogView.findViewById<TextView>(R.id.tvTaskDueDate)
            val tvTaskProgress = dialogView.findViewById<TextView>(R.id.tvTaskProgress)
            val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)
            val progressBarLayout = dialogView.findViewById<LinearLayout>(R.id.progressBarLayout)
            val btnComplete = dialogView.findViewById<TextView>(R.id.btnComplete)
            val btnEdit = dialogView.findViewById<TextView>(R.id.btnEdit)
            val btnDelete = dialogView.findViewById<TextView>(R.id.btnDelete)
            
            // Set priority indicator color
            layoutPriorityIndicator.setBackgroundColor(task.getPriorityColor())
            
            // Set task title
            tvTaskTitle.text = task.Title
            
            // Set description
            if (!task.Description.isNullOrBlank()) {
                tvTaskDescription.text = task.Description
                tvTaskDescription.visibility = View.VISIBLE
            } else {
                tvTaskDescription.visibility = View.GONE
            }
            
            // Set category
            tvTaskCategory.text = task.Category.getDisplayText()
            
            // Set priority
            tvTaskPriority.text = task.Priority.name
            
            // Set status
            tvTaskStatus.text = "${task.getStatusEmoji()} ${task.Status.name}"
            
            // Set progress
            if (task.TaskProgress > 0) {
                progressBarLayout.visibility = View.VISIBLE
                progressBar.progress = task.TaskProgress
                tvTaskProgress.text = "${task.TaskProgress}%"
            } else {
                progressBarLayout.visibility = View.GONE
            }
            
            // Set due date
            tvTaskDueDate.text = task.getFormattedDueDateTime()
            tvTaskDueDate.setTextColor(0xFFE53E3E.toInt()) // Red
            
            // Create dialog
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()
            
            // Apply rounded corners to dialog window
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            
            // Set up button click listeners
            btnComplete.setOnClickListener {
                dialog.dismiss()
                markTaskAsDone(task)
            }
            
            btnEdit.setOnClickListener {
                dialog.dismiss()
                showEditTaskDialog(task)
            }
            
            btnDelete.setOnClickListener {
                dialog.dismiss()
                deleteTask(task)
            }
            
            // Update button state based on task status
            if (task.Status == Status.Done) {
                btnComplete.text = "✅ Completed"
                btnComplete.alpha = 0.5f
                btnComplete.isEnabled = false
            }
            
            dialog.show()
            
        } catch (e: Exception) {
            android.util.Log.e("FragmentTaskActivity", "Error showing task details", e)
            // Fallback to simple dialog
            showTaskDetailDialog(task)
        }
    }

    // Fallback simple dialog
    private fun showTaskDetailDialog(task: Task) {
        val message = """
        📋 ${task.Title}
        
        ${if (task.Description?.isNotBlank() == true) "📝 ${task.Description}\n" else ""}🎯 Priority: ${task.Priority}
        📊 Status: ${task.Status}
        📈 Progress: ${task.TaskProgress}%
        ${if (task.DueAt != null) "📅 Due: ${task.getFormattedDueDateTime()}\n" else ""}
        
        ${task.getStatusEmoji()} ${task.Status}
        ${if (task.isOverdue()) "🔴 OVERDUE" else ""}
        ${if (task.isToday()) "📅 DUE TODAY" else ""}
    """.trimIndent()

        val dialog = AlertDialog.Builder(this)
            .setTitle("Task Details")
            .setMessage(message)
            .setPositiveButton("Edit") { _, _ -> showEditTaskDialog(task) }
            .setNeutralButton("Close", null)
            .setNegativeButton("Delete") { _, _ -> deleteTask(task) }
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    // ✅ CRUD OPERATION: UPDATE - Complete Edit Dialog with all fields
    private fun showEditTaskDialog(task: Task) {
        try {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Edit Task")

            val scrollView = ScrollView(this)
            val container = LinearLayout(this)
            container.orientation = LinearLayout.VERTICAL
            container.setPadding(50, 20, 50, 20)

            // Task Title (pre-filled)
            val titleInput = EditText(this)
            titleInput.hint = "Task title"
            titleInput.setText(task.Title)
            titleInput.setPadding(16, 16, 16, 16)
            container.addView(titleInput)

            // Description (pre-filled)
            val descriptionInput = EditText(this)
            descriptionInput.hint = "Description (optional)"
            descriptionInput.setText(task.Description ?: "")
            descriptionInput.setPadding(16, 16, 16, 16)
            descriptionInput.minLines = 2
            container.addView(descriptionInput)

            // Priority Spinner (pre-selected)
            val priorityLabel = TextView(this)
            priorityLabel.text = "Priority:"
            priorityLabel.setPadding(0, 16, 0, 8)
            container.addView(priorityLabel)

            val prioritySpinner = Spinner(this)
            val priorities = arrayOf("Low", "Medium", "High")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            prioritySpinner.adapter = adapter

            // Set current priority selection
            val currentPriorityIndex = when (task.Priority) {
                Priority.Low -> 0
                Priority.Medium -> 1
                Priority.High -> 2
            }
            prioritySpinner.setSelection(currentPriorityIndex)
            container.addView(prioritySpinner)

            // Status Spinner (pre-selected)
            val statusLabel = TextView(this)
            statusLabel.text = "Status:"
            statusLabel.setPadding(0, 16, 0, 8)
            container.addView(statusLabel)

            val statusSpinner = Spinner(this)
            val statuses = arrayOf("Pending", "InProgress", "Completed", "Cancelled")
            val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses)
            statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            statusSpinner.adapter = statusAdapter

            // Set current status selection
            val currentStatusIndex = when (task.Status) {
                Status.Pending -> 0
                Status.InProgress -> 1
                Status.Done -> 2
                Status.Cancelled -> 3
                Status.OverDue -> 0 // Default to Pending for OverDue
            }
            statusSpinner.setSelection(currentStatusIndex)
            container.addView(statusSpinner)

            // Progress Slider
            val progressLabel = TextView(this)
            progressLabel.text = "Progress: ${task.TaskProgress}%"
            progressLabel.setPadding(0, 16, 0, 8)
            container.addView(progressLabel)

            val progressSeekBar = SeekBar(this)
            progressSeekBar.max = 100
            progressSeekBar.progress = task.TaskProgress
            progressSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    progressLabel.text = "Progress: $progress%"
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
            container.addView(progressSeekBar)

            // Date Selection (pre-filled)
            val dateLabel = TextView(this)
            dateLabel.text = "Due Date:"
            dateLabel.setPadding(0, 16, 0, 8)
            container.addView(dateLabel)

            val dateButton = Button(this)
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

            // Parse existing due date
            var selectedDate: Date = try {
                if (task.DueAt != null) {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(task.DueAt) ?: Date()
                } else {
                    Date()
                }
            } catch (e: Exception) {
                Date()
            }
            calendar.time = selectedDate
            dateButton.text = "📅 ${dateFormat.format(selectedDate)}"

            dateButton.setOnClickListener {
                showDatePicker { year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar.time
                    dateButton.text = "📅 ${dateFormat.format(selectedDate)}"
                }
            }
            container.addView(dateButton)

            // Time Selection (pre-filled)
            val timeLabel = TextView(this)
            timeLabel.text = "Due Time:"
            timeLabel.setPadding(0, 16, 0, 8)
            container.addView(timeLabel)

            val timeButton = Button(this)
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

            // Parse existing due time
            var selectedHour = 12
            var selectedMinute = 0
            try {
                if (task.DueTime != null) {
                    val timeParts = task.DueTime.split(":")
                    selectedHour = timeParts[0].toInt()
                    selectedMinute = timeParts[1].toInt()
                }
            } catch (e: Exception) {
                selectedHour = 12
                selectedMinute = 0
            }

            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            calendar.set(Calendar.MINUTE, selectedMinute)
            timeButton.text = "🕐 ${timeFormat.format(calendar.time)}"

            timeButton.setOnClickListener {
                showTimePicker(selectedHour, selectedMinute) { hour, minute ->
                    selectedHour = hour
                    selectedMinute = minute
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    timeButton.text = "🕐 ${timeFormat.format(calendar.time)}"
                }
            }
            container.addView(timeButton)

            scrollView.addView(container)
            builder.setView(scrollView)

            builder.setPositiveButton("Update Task", null)
            builder.setNegativeButton("Cancel", null)

            val dialog = builder.create()
            dialog.show()

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val title = titleInput.text.toString().trim()
                if (title.isEmpty()) {
                    titleInput.error = "Task title is required"
                    titleInput.requestFocus()
                    Toast.makeText(this@FragmentTaskActivity, "⚠️ Please enter a task title", Toast.LENGTH_SHORT).show()
                } else {
                    val priority = prioritySpinner.selectedItem.toString()
                    val status = statusSpinner.selectedItem.toString()
                    val description = descriptionInput.text.toString().trim()
                    val progress = progressSeekBar.progress

                    val finalCalendar = Calendar.getInstance()
                    finalCalendar.time = selectedDate
                    finalCalendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                    finalCalendar.set(Calendar.MINUTE, selectedMinute)

                    updateTask(task, title, description, priority, status, progress, finalCalendar.time)
                    dialog.dismiss()
                }
            }

        } catch (e: Exception) {
            android.util.Log.e("FragmentTaskActivity", "Error showing edit task dialog", e)
            Toast.makeText(this, "✏️ Edit task dialog error", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ CRUD OPERATION: UPDATE - Update existing task
    private fun updateTask(
        originalTask: Task,
        title: String,
        description: String,
        priority: String,
        status: String,
        progress: Int,
        dueDate: Date
    ) {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentTaskActivity)

                val priorityEnum = when (priority.uppercase()) {
                    "LOW" -> Priority.Low
                    "HIGH" -> Priority.High
                    else -> Priority.Medium
                }

                val statusEnum = when (status.uppercase()) {
                    "PENDING" -> Status.Pending
                    "INPROGRESS" -> Status.InProgress
                    "DONE" -> Status.Done
                    "CANCELLED" -> Status.Cancelled
                    else -> Status.Pending
                }

                val updatedTask = originalTask.copy(
                    Title = title,
                    Description = if (description.isBlank()) null else description,
                    Priority = priorityEnum,
                    Status = statusEnum,
                    TaskProgress = progress,
                    DueAt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dueDate),
                    DueTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(dueDate),
                    UpdatedAt = System.currentTimeMillis()
                )

                withContext(Dispatchers.IO) {
                    db.taskDao().update(updatedTask)
                }

                loadTasks()

                val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                val formattedDate = dateFormat.format(dueDate)

                Toast.makeText(
                    this@FragmentTaskActivity,
                    "✅ Task '$title' updated!\n📅 Due: $formattedDate",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                android.util.Log.e("FragmentTaskActivity", "Error updating task", e)
                Toast.makeText(this@FragmentTaskActivity, "❌ Update Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✅ CRUD OPERATION: DELETE - Delete task with confirmation
    private fun deleteTask(task: Task) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete '${task.Title}'?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val db = AppDb.get(this@FragmentTaskActivity)
                        withContext(Dispatchers.IO) {
                            db.taskDao().softDelete(task.User_ID, task.Task_ID)
                        }

                        Toast.makeText(this@FragmentTaskActivity, "🗑️ Task deleted", Toast.LENGTH_SHORT).show()
                        loadTasks()

                    } catch (e: Exception) {
                        android.util.Log.e("FragmentTaskActivity", "Error deleting task", e)
                        Toast.makeText(this@FragmentTaskActivity, "Error deleting task", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    // ✅ CRUD OPERATION: CREATE - Add new task dialog
    private fun showAddTaskDialog() {
        try {
            android.util.Log.d("DIALOG_DEBUG", "===== SHOW ADD TASK DIALOG START =====")
            android.util.Log.d("DIALOG_DEBUG", "Method: showAddTaskDialog() in FragmentTaskActivity")
            android.util.Log.d("DIALOG_DEBUG", "Inflating layout: R.layout.dialog_add_task")
            
            // Inflate the XML layout
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
            android.util.Log.d("DIALOG_DEBUG", "Layout inflated successfully: ${dialogView.javaClass.simpleName}")
            android.util.Log.d("DIALOG_DEBUG", "Dialog view children count: ${(dialogView as? android.view.ViewGroup)?.childCount}")
            
            // Get references to all input fields (now using standard EditText)
            val etTaskTitle = dialogView.findViewById<EditText>(R.id.etTaskTitle)
            val etTaskDescription = dialogView.findViewById<EditText>(R.id.etTaskDescription)
            val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
            val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinnerPriority)
            val etDueDate = dialogView.findViewById<EditText>(R.id.etDueDate)
            val etDueTime = dialogView.findViewById<EditText>(R.id.etDueTime)
            val spinnerReminder = dialogView.findViewById<Spinner>(R.id.spinnerReminder)
            
            // Set up spinners with icons using the helper
            com.cheermateapp.util.TaskDialogSpinnerHelper.setupCategorySpinner(this, spinnerCategory)
            com.cheermateapp.util.TaskDialogSpinnerHelper.setupPrioritySpinner(this, spinnerPriority)
            com.cheermateapp.util.TaskDialogSpinnerHelper.setupReminderSpinner(this, spinnerReminder)
            
            // Set default due date to today
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            var selectedDate = calendar.time
            etDueDate.setText(dateFormat.format(selectedDate))
            
            // Track selected time
            var selectedHour = calendar.get(Calendar.HOUR_OF_DAY)
            var selectedMinute = calendar.get(Calendar.MINUTE)
            
            // Set up date picker
            etDueDate.setOnClickListener {
                showDatePicker { year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar.time
                    etDueDate.setText(dateFormat.format(calendar.time))
                }
            }
            
            // Set up time picker
            etDueTime.setOnClickListener {
                showTimePicker(selectedHour, selectedMinute) { hour, minute ->
                    selectedHour = hour
                    selectedMinute = minute
                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    etDueTime.setText(timeFormat.format(calendar.time))
                }
            }
            
            // Get references to buttons inside the custom layout
            val btnCreateTask = dialogView.findViewById<Button>(R.id.btnCreateTask)
            val btnCancelTask = dialogView.findViewById<Button>(R.id.btnCancelTask)

            // Create and show the dialog
            android.util.Log.d("DIALOG_DEBUG", "Creating AlertDialog.Builder")
            val builder = AlertDialog.Builder(this)
            android.util.Log.d("DIALOG_DEBUG", "Setting dialog view (no title override)")
            builder.setView(dialogView)
            // We no longer set positive/negative buttons here, as they are inside the layout.
            android.util.Log.d("DIALOG_DEBUG", "Dialog buttons will be handled by the custom layout")

            android.util.Log.d("DIALOG_DEBUG", "Creating dialog instance")
            val dialog = builder.create()

            android.util.Log.d("DIALOG_DEBUG", "Setting dialog properties")
            // Make dialog non-cancelable by tapping outside to ensure proper task creation
            dialog.setCanceledOnTouchOutside(false)

            // Set dialog window properties for proper rendering
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            // Set click listener for the "Cancel" button inside the dialog
            btnCancelTask.setOnClickListener {
                dialog.dismiss()
            }

            // Set click listener for the "Create Task" button inside the dialog
            btnCreateTask.setOnClickListener {
                val title = etTaskTitle.text.toString().trim()
                val description = etTaskDescription.text.toString().trim()
                val dueDate = etDueDate.text.toString().trim()
                val dueTime = etDueTime.text.toString().trim()
                
                // Get selected values from spinners using helper methods
                val category = com.cheermateapp.util.TaskDialogSpinnerHelper.getSelectedCategory(spinnerCategory)
                val priority = com.cheermateapp.util.TaskDialogSpinnerHelper.getSelectedPriority(spinnerPriority)
                val reminderOption = com.cheermateapp.util.TaskDialogSpinnerHelper.getSelectedReminder(spinnerReminder)
                
                // Validation
                if (title.isEmpty()) {
                    Toast.makeText(this@FragmentTaskActivity, "⚠️ Please enter a task title", Toast.LENGTH_SHORT).show()
                    etTaskTitle.error = "Task title is required"
                    etTaskTitle.requestFocus()
                    return@setOnClickListener
                }
                
                if (dueDate.isEmpty()) {
                    Toast.makeText(this@FragmentTaskActivity, "⚠️ Please select a due date", Toast.LENGTH_SHORT).show()
                    etDueDate.error = "Due date is required"
                    return@setOnClickListener
                }
                
                // Build final datetime
                val finalCalendar = Calendar.getInstance()
                finalCalendar.time = selectedDate
                finalCalendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                finalCalendar.set(Calendar.MINUTE, selectedMinute)
                
                // Create the task
                createNewTask(title, description, category, priority, finalCalendar.time, reminderOption)
                dialog.dismiss()
            }

            android.util.Log.d("DIALOG_DEBUG", "===== SHOWING DIALOG =====")
            android.util.Log.d("DIALOG_DEBUG", "Dialog class: ${dialog.javaClass.simpleName}")
            android.util.Log.d("DIALOG_DEBUG", "Window background: transparent")
            dialog.show()
            android.util.Log.d("DIALOG_DEBUG", "Dialog.show() called - should be visible now")
            android.util.Log.d("DIALOG_DEBUG", "===== SHOW ADD TASK DIALOG END =====")

        } catch (e: Exception) {
            android.util.Log.e("FragmentTaskActivity", "Error showing add task dialog", e)
            Toast.makeText(this, "📝 Task creation dialog error", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ DIALOG HELPER METHODS
    private fun showDatePicker(onDateSelected: (year: Int, month: Int, day: Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = android.app.DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                onDateSelected(year, month, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun showTimePicker(currentHour: Int, currentMinute: Int, onTimeSelected: (hour: Int, minute: Int) -> Unit) {
        val timePickerDialog = android.app.TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                onTimeSelected(hourOfDay, minute)
            },
            currentHour,
            currentMinute,
            false
        )
        timePickerDialog.show()
    }

    // ✅ CRUD OPERATION: CREATE - Create new task
    private fun createNewTask(title: String, description: String, category: String, priority: String, dueDate: Date, reminderOption: String) {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentTaskActivity)

                val taskId = withContext(Dispatchers.IO) {
                    db.taskDao().getNextTaskIdForUser(userId)
                }

                val categoryEnum = when (category) {
                    "Work" -> Category.Work
                    "Personal" -> Category.Personal
                    "Shopping" -> Category.Shopping
                    "Others" -> Category.Others
                    else -> Category.Work
                }

                val priorityEnum = when (priority.uppercase()) {
                    "LOW" -> Priority.Low
                    "HIGH" -> Priority.High
                    else -> Priority.Medium
                }

                val currentTime = System.currentTimeMillis()  // ✅ Get current timestamp

                val newTask = Task(
                    Task_ID = taskId,
                    User_ID = userId,
                    Title = title,
                    Description = if (description.isBlank()) null else description,
                    Category = categoryEnum,
                    Priority = priorityEnum,
                    Status = Status.Pending,
                    TaskProgress = 0,
                    DueAt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dueDate),
                    DueTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(dueDate),
                    CreatedAt = currentTime,  // ✅ Long timestamp
                    UpdatedAt = currentTime,  // ✅ Long timestamp
                    DeletedAt = null
                )

                withContext(Dispatchers.IO) {
                    db.taskDao().insert(newTask)
                }

                // Create reminder if requested
                if (reminderOption != "None") {
                    createTaskReminder(taskId, newTask.DueAt!!, newTask.DueTime!!, reminderOption)
                }

                loadTasks()

                val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                val formattedDate = dateFormat.format(dueDate)
                val reminderText = if (reminderOption != "None") "\n🔔 Reminder: $reminderOption" else ""

                Toast.makeText(
                    this@FragmentTaskActivity,
                    "✅ Task '$title' created!\n📅 Due: $formattedDate$reminderText",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                android.util.Log.e("FragmentTaskActivity", "Error creating task", e)
                Toast.makeText(this@FragmentTaskActivity, "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✅ CREATE TASK REMINDER
    private fun createTaskReminder(
        taskId: Int,
        dueDate: String,
        dueTime: String,
        reminderOption: String
    ) {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentTaskActivity)

                // Parse due date and time
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val dueDateTime = dateFormat.parse("$dueDate $dueTime")

                if (dueDateTime != null) {
                    val dueTimeMillis = dueDateTime.time

                    // Calculate reminder time based on option
                    val remindAtMillis = when (reminderOption) {
                        "10 minutes before" -> dueTimeMillis - (10 * 60 * 1000)
                        "30 minutes before" -> dueTimeMillis - (30 * 60 * 1000)
                        "At specific time" -> dueTimeMillis
                        else -> dueTimeMillis
                    }

                    // Get next reminder ID
                    val reminderId = withContext(Dispatchers.IO) {
                        val existingReminders = db.taskReminderDao().getRemindersByTask(taskId)
                        if (existingReminders.isEmpty()) 1 else existingReminders.maxOf { it.TaskReminder_ID } + 1
                    }

                    val reminder = TaskReminder(
                        TaskReminder_ID = reminderId,
                        Task_ID = taskId,
                        User_ID = userId,
                        RemindAt = remindAtMillis,
                        IsActive = true
                    )

                    withContext(Dispatchers.IO) {
                        db.taskReminderDao().insert(reminder)
                    }

                    android.util.Log.d("FragmentTaskActivity", "✅ Created reminder for task $taskId at $remindAtMillis")
                }

            } catch (e: Exception) {
                android.util.Log.e("FragmentTaskActivity", "Error creating task reminder", e)
                // Don't show error to user - reminder is optional feature
            }
        }
    }

    // ✅ NAVIGATION METHODS
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_SHOW_DASHBOARD, true)
        intent.putExtra(MainActivity.EXTRA_USER_ID, userId.toString())
        startActivity(intent)
        finish()
    }

    private fun navigateToSettings() {
        val intent = Intent(this, FragmentSettingsActivity::class.java)
        intent.putExtra(EXTRA_USER_ID, userId.toString())
        startActivity(intent)
        finish()
    }

    // ✅ HELPER METHODS
    private fun getCurrentDateString(): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = format.format(Date())
        android.util.Log.d("FragmentTaskActivity", "🔍 getCurrentDateString: $todayStr")
        return todayStr
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    // ✅ ACTIVITY LIFECYCLE METHODS
    override fun onSupportNavigateUp(): Boolean {
        navigateToMain()
        return true
    }

    override fun onResume() {
        super.onResume()
        loadTasks()
    }
}
