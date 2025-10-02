package com.example.cheermateapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.cheermateapp.data.model.Status
import androidx.lifecycle.lifecycleScope
import com.example.cheermateapp.data.db.AppDb
import com.example.cheermateapp.data.model.Task
import com.example.cheermateapp.data.model.Priority
import com.example.cheermateapp.data.model.*
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
    private lateinit var btnAddTask: TextView
    private lateinit var tvTasksSub: TextView
    private lateinit var etSearch: EditText
    private lateinit var btnSort: TextView
    private lateinit var chipFound: TextView
    private lateinit var tabAll: TextView
    private lateinit var tabToday: TextView
    private lateinit var tabPending: TextView
    private lateinit var tabDone: TextView
    private lateinit var cardEmpty: LinearLayout

    // ✅ NEW: Task display elements
    private lateinit var tvTaskTitle: TextView
    private lateinit var tvTaskDescription: TextView
    private lateinit var tvTaskPriority: TextView
    private lateinit var tvTaskStatus: TextView
    private lateinit var tvTaskDueDate: TextView
    private lateinit var layoutPriority: LinearLayout
    private lateinit var layoutStatus: LinearLayout
    private lateinit var layoutDueDate: LinearLayout
    private lateinit var btnMarkDone: TextView

    // ✅ Navigation elements
    private lateinit var layoutNavigation: LinearLayout
    private lateinit var btnPreviousTask: TextView
    private lateinit var btnNextTask: TextView
    private lateinit var tvTaskCounter: TextView

    private var currentFilter = FilterType.ALL
    private var userId: Int = 0
    private var currentTasks = mutableListOf<Task>()
    private var allTasks = mutableListOf<Task>()
    private var currentDisplayedTask: Task? = null
    private var currentTaskIndex: Int = 0
    private var filteredTasks: List<Task> = emptyList()

    enum class FilterType {
        ALL, TODAY, PENDING, DONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_tasks)

        userId = intent?.getStringExtra(EXTRA_USER_ID)?.toIntOrNull() ?: 0
        android.util.Log.d("FragmentTaskActivity", "🔍 onCreate: userId = $userId")

        initializeViews()
        loadTasks()
        debugAllTasks()
        debugTaskLoading()
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
                android.util.Log.d("DEBUG", "Database created successfully: ${db != null}")

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

    // ✅ UPDATED: Initialize all views including new task display elements
    private fun initializeViews() {
        try {
            // Original views
            tvTasksTitle = findViewById(R.id.tvTasksTitle)
            btnAddTask = findViewById(R.id.btnAddTask)
            tvTasksSub = findViewById(R.id.tvTasksSub)
            etSearch = findViewById(R.id.etSearch)
            btnSort = findViewById(R.id.btnSort)
            chipFound = findViewById(R.id.chipFound)
            tabAll = findViewById(R.id.tabAll)
            tabToday = findViewById(R.id.tabToday)
            tabPending = findViewById(R.id.tabPending)
            tabDone = findViewById(R.id.tabDone)
            cardEmpty = findViewById(R.id.cardEmpty)

            // NEW: Initialize task display elements
            tvTaskTitle = findViewById(R.id.tvTaskTitle)
            tvTaskDescription = findViewById(R.id.tvTaskDescription)
            tvTaskPriority = findViewById(R.id.tvTaskPriority)
            tvTaskStatus = findViewById(R.id.tvTaskStatus)
            tvTaskDueDate = findViewById(R.id.tvTaskDueDate)
            layoutPriority = findViewById(R.id.layoutPriority)
            layoutStatus = findViewById(R.id.layoutStatus)
            layoutDueDate = findViewById(R.id.layoutDueDate)
            btnMarkDone = findViewById(R.id.btnMarkDone)

            // ✅ ADD: Navigation elements
            layoutNavigation = findViewById(R.id.layoutNavigation)
            btnPreviousTask = findViewById(R.id.btnPreviousTask)
            btnNextTask = findViewById(R.id.btnNextTask)
            tvTaskCounter = findViewById(R.id.tvTaskCounter)

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
            btnAddTask.setOnClickListener {
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

            // NEW: Mark as Done button click
            btnMarkDone.setOnClickListener {
                currentDisplayedTask?.let { task ->
                    markTaskAsDone(task)
                }
            }

            // ✅ FIXED: Navigation button interactions
            btnPreviousTask.setOnClickListener {
                navigateToPreviousTask()
            }

            btnNextTask.setOnClickListener {
                navigateToNextTask()
            }

            // ✅ ADD: Card click to show task details
            cardEmpty.setOnClickListener {
                currentDisplayedTask?.let { task ->
                    showTaskDetailDialog(task)
                }
            }

            updateTabSelection(tabAll)

            android.util.Log.d("FragmentTaskActivity", "✅ Interactions setup complete")
        } catch (e: Exception) {
            android.util.Log.e("FragmentTaskActivity", "Error setting up interactions", e)
        }
    }

    // ✅ FIXED: All navigation methods at class level
    private fun navigateToPreviousTask() {
        if (filteredTasks.isEmpty()) {
            return
        }
        
        if (currentTaskIndex > 0) {
            currentTaskIndex--
            showTaskInCard(filteredTasks[currentTaskIndex])
            updateNavigationState()
            Toast.makeText(this, "◀ Previous task", Toast.LENGTH_SHORT).show()
        } else {
            // Already at the first task
            Toast.makeText(this, "Start of list", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToNextTask() {
        if (filteredTasks.isEmpty()) {
            return
        }
        
        if (currentTaskIndex < filteredTasks.size - 1) {
            currentTaskIndex++
            showTaskInCard(filteredTasks[currentTaskIndex])
            updateNavigationState()
            Toast.makeText(this, "Next task ▶", Toast.LENGTH_SHORT).show()
        } else {
            // Already at the last task
            Toast.makeText(this, "End of list", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateNavigationState() {
        try {
            if (filteredTasks.size <= 1) {
                // Hide navigation if only one or no tasks
                layoutNavigation.visibility = View.GONE
            } else {
                // Show navigation if multiple tasks
                layoutNavigation.visibility = View.VISIBLE

                // Update counter
                tvTaskCounter.text = "${currentTaskIndex + 1}/${filteredTasks.size}"

                // Enable/disable buttons based on position
                btnPreviousTask.alpha = if (currentTaskIndex > 0) 1.0f else 0.5f
                btnPreviousTask.isClickable = currentTaskIndex > 0

                btnNextTask.alpha = if (currentTaskIndex < filteredTasks.size - 1) 1.0f else 0.5f
                btnNextTask.isClickable = currentTaskIndex < filteredTasks.size - 1
            }
        } catch (e: Exception) {
            android.util.Log.e("FragmentTaskActivity", "Error updating navigation state", e)
        }
    }

    // ✅ UPDATED: Display tasks with navigation support
    private fun displayTaskInCard(tasks: List<Task>) {
        try {
            filteredTasks = tasks
            currentTasks.clear()
            currentTasks.addAll(tasks)
            currentTaskIndex = 0 // Reset to first task

            if (tasks.isEmpty()) {
                showEmptyState()
            } else {
                showTaskInCard(tasks[currentTaskIndex])
                updateNavigationState()
            }
        } catch (e: Exception) {
            android.util.Log.e("FragmentTaskActivity", "Error displaying task in card", e)
            showEmptyState()
        }
    }

    // ✅ FIXED: Use existing XML views to display task data
    private fun showTaskInCard(task: Task) {
        try {
            android.util.Log.d("DEBUG", "🎯 showTaskInCard called with task: ${task.Title}")

            currentDisplayedTask = task
            
            // ✅ Ensure card is visible when showing task
            cardEmpty.visibility = View.VISIBLE

            // Update the existing XML TextViews with task data from database
            tvTaskTitle.text = task.Title
            tvTaskTitle.visibility = View.VISIBLE
            tvTaskTitle.gravity = android.view.Gravity.START // Reset to left alignment for tasks
            
            // Show or hide description based on whether it exists
            if (!task.Description.isNullOrBlank()) {
                tvTaskDescription.text = task.Description
                tvTaskDescription.visibility = View.VISIBLE
            } else {
                tvTaskDescription.visibility = View.GONE
            }
            
            // Display priority with emoji and color
            tvTaskPriority.text = task.getPriorityText()
            tvTaskPriority.setTextColor(task.getPriorityColor())
            layoutPriority.visibility = View.VISIBLE
            
            // Display status
            tvTaskStatus.text = task.getStatusText()
            layoutStatus.visibility = View.VISIBLE
            
            // Display due date
            tvTaskDueDate.text = task.getFormattedDueDateTime()
            layoutDueDate.visibility = View.VISIBLE
            
            // Show/hide Mark as Done button based on task status
            if (task.Status != Status.Completed) {
                btnMarkDone.text = when (task.Status) {
                    Status.Pending -> "✅ Mark as Done"
                    Status.InProgress -> "✅ Complete Task"
                    Status.OverDue -> "✅ Complete (Overdue)"
                    else -> "✅ Mark as Done"
                }
                btnMarkDone.visibility = View.VISIBLE
            } else {
                btnMarkDone.visibility = View.GONE
            }

            android.util.Log.d("DEBUG", "✅ showTaskInCard completed successfully")

        } catch (e: Exception) {
            android.util.Log.e("DEBUG", "❌ Error in showTaskInCard: ${e.message}", e)
            showEmptyState()
        }
    }


    // ✅ NEW: Mark task as done (UPDATE OPERATION)
    private fun markTaskAsDone(task: Task) {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentTaskActivity)
                withContext(Dispatchers.IO) {
                    db.taskDao().updateTaskStatus(task.User_ID, task.Task_ID, "Completed")
                    db.taskDao().updateTaskProgress(task.User_ID, task.Task_ID, 100)
                }

                Toast.makeText(this@FragmentTaskActivity, "✅ Task '${task.Title}' completed!", Toast.LENGTH_SHORT).show()

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
            com.example.cheermateapp.data.model.Priority.High -> "🔴 High"
            com.example.cheermateapp.data.model.Priority.Medium -> "🟡 Medium"
            com.example.cheermateapp.data.model.Priority.Low -> "🟢 Low"
        }
    }

    private fun Task.getStatusText(): String {
        return when (this.Status) {
            com.example.cheermateapp.data.model.Status.Pending -> "⏳ Pending"
            com.example.cheermateapp.data.model.Status.InProgress -> "🔄 In Progress"
            com.example.cheermateapp.data.model.Status.OverDue -> "🔴 Overdue"
            com.example.cheermateapp.data.model.Status.Completed -> "✅ Completed"
            com.example.cheermateapp.data.model.Status.Cancelled -> "❌ Cancelled"
        }
    }

    private fun Task.getFormattedDueDateTime(): String? {
        return try {
            if (DueAt != null) {
                val dateStr = DueAt
                val timeStr = DueTime ?: "00:00"

                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateStr)

                val formattedDate = if (date != null) outputFormat.format(date) else dateStr
                val formattedTime = if (timeStr != "00:00") " at $timeStr" else ""

                "$formattedDate$formattedTime"
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("Task", "Error formatting date: ${e.message}")
            null
        }
    }

    private fun Task.getStatusEmoji(): String {
        return when (this.Status) {
            com.example.cheermateapp.data.model.Status.Pending -> "⏳"
            com.example.cheermateapp.data.model.Status.InProgress -> "🔄"
            com.example.cheermateapp.data.model.Status.OverDue -> "🔴"
            com.example.cheermateapp.data.model.Status.Completed -> "✅"
            com.example.cheermateapp.data.model.Status.Cancelled -> "❌"
        }
    }

    private fun Task.getPriorityColor(): Int {
        return when (this.Priority) {
            com.example.cheermateapp.data.model.Priority.High -> android.graphics.Color.RED
            com.example.cheermateapp.data.model.Priority.Medium -> android.graphics.Color.parseColor("#FFA500") // Orange
            com.example.cheermateapp.data.model.Priority.Low -> android.graphics.Color.GREEN
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
            currentDisplayedTask = null
            filteredTasks = emptyList()
            currentTaskIndex = 0

            // Show appropriate empty message based on current filter
            val emptyMessage = when (currentFilter) {
                FilterType.ALL -> "No task available"
                FilterType.TODAY -> "No task available"
                FilterType.PENDING -> "No task available"
                FilterType.DONE -> "No task available"
            }

            // Show title with empty message (centered) and hide all task detail views
            tvTaskTitle.text = emptyMessage
            tvTaskTitle.visibility = View.VISIBLE
            tvTaskTitle.gravity = android.view.Gravity.CENTER
            tvTaskDescription.visibility = View.GONE
            layoutPriority.visibility = View.GONE
            layoutStatus.visibility = View.GONE
            layoutDueDate.visibility = View.GONE
            btnMarkDone.visibility = View.GONE
            layoutNavigation.visibility = View.GONE
            
            // ✅ Ensure card is visible even in empty state
            cardEmpty.visibility = View.VISIBLE

            android.util.Log.d("FragmentTaskActivity", "📋 Showing empty state: $emptyMessage")

        } catch (e: Exception) {
            android.util.Log.e("FragmentTaskActivity", "Error showing empty state", e)
        }
    }

    // ✅ EXTENSION METHODS
    private fun Task.getSummary(): String {
        val statusEmoji = this.getStatusEmoji()
        val priorityEmoji = when (this.Priority) {
            com.example.cheermateapp.data.model.Priority.High -> "🔴"
            com.example.cheermateapp.data.model.Priority.Medium -> "🟡"
            com.example.cheermateapp.data.model.Priority.Low -> "🟢"
        }
        return "$statusEmoji $priorityEmoji ${this.Title}"
    }

    private fun Task.isOverdue(): Boolean {
        return Status == com.example.cheermateapp.data.model.Status.OverDue
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
                        Status = Status.Completed,
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
                        "done" to db.taskDao().getCompletedTasksCount(userId)
                    )
                }

                tabAll.text = "All (${counts["all"]})"
                tabToday.text = "Today (${counts["today"]})"
                tabPending.text = "Pending (${counts["pending"]})"
                tabDone.text = "Done (${counts["done"]})"

                val totalTasks = counts["all"] ?: 0
                tvTasksSub.text = "$totalTasks total tasks"

            } catch (e: Exception) {
                android.util.Log.e("FragmentTaskActivity", "Error updating tab counts", e)
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

        AlertDialog.Builder(this)
            .setTitle("Sort Tasks By")
            .setItems(sortOptions) { _, which ->
                sortTasks(which)
            }
            .setNegativeButton("Cancel", null)
            .show()
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
                            Status.Completed -> 4
                            Status.Cancelled -> 5
                        }
                        val status2 = when (task2.Status) {
                            Status.Pending -> 1
                            Status.InProgress -> 2
                            Status.OverDue -> 3
                            Status.Completed -> 4
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

        AlertDialog.Builder(this)
            .setTitle("Task Details")
            .setMessage(message)
            .setPositiveButton("Edit") { _, _ -> showEditTaskDialog(task) }
            .setNeutralButton("Close", null)
            .setNegativeButton("Delete") { _, _ -> deleteTask(task) }
            .show()
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
                Status.Completed -> 2
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
                    "COMPLETED" -> Status.Completed
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
        AlertDialog.Builder(this)
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
            .show()
    }

    // ✅ CRUD OPERATION: CREATE - Add new task dialog
    private fun showAddTaskDialog() {
        try {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add New Task")

            val scrollView = ScrollView(this)
            val container = LinearLayout(this)
            container.orientation = LinearLayout.VERTICAL
            container.setPadding(50, 20, 50, 20)

            // Task Title
            val titleInput = EditText(this)
            titleInput.hint = "Task title"
            titleInput.setPadding(16, 16, 16, 16)
            container.addView(titleInput)

            // Description (Optional)
            val descriptionInput = EditText(this)
            descriptionInput.hint = "Description (optional)"
            descriptionInput.setPadding(16, 16, 16, 16)
            descriptionInput.minLines = 2
            container.addView(descriptionInput)

            // Priority Spinner
            val priorityLabel = TextView(this)
            priorityLabel.text = "Priority:"
            priorityLabel.setPadding(0, 16, 0, 8)
            container.addView(priorityLabel)

            val prioritySpinner = Spinner(this)
            val priorities = arrayOf("Low", "Medium", "High")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            prioritySpinner.adapter = adapter
            prioritySpinner.setSelection(1) // Default to Medium
            container.addView(prioritySpinner)

            // Date Selection
            val dateLabel = TextView(this)
            dateLabel.text = "Due Date:"
            dateLabel.setPadding(0, 16, 0, 8)
            container.addView(dateLabel)

            val dateButton = Button(this)
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            var selectedDate = calendar.time
            dateButton.text = "📅 ${dateFormat.format(selectedDate)}"

            dateButton.setOnClickListener {
                showDatePicker { year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar.time
                    dateButton.text = "📅 ${dateFormat.format(selectedDate)}"
                }
            }
            container.addView(dateButton)

            // Time Selection
            val timeLabel = TextView(this)
            timeLabel.text = "Due Time:"
            timeLabel.setPadding(0, 16, 0, 8)
            container.addView(timeLabel)

            val timeButton = Button(this)
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            var selectedHour = calendar.get(Calendar.HOUR_OF_DAY)
            var selectedMinute = calendar.get(Calendar.MINUTE)
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

            builder.setPositiveButton("Add Task", null)
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
                    val description = descriptionInput.text.toString().trim()

                    val finalCalendar = Calendar.getInstance()
                    finalCalendar.time = selectedDate
                    finalCalendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                    finalCalendar.set(Calendar.MINUTE, selectedMinute)

                    createNewTask(title, description, priority, finalCalendar.time)
                    dialog.dismiss()
                }
            }

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
    private fun createNewTask(title: String, description: String, priority: String, dueDate: Date) {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentTaskActivity)

                val taskId = withContext(Dispatchers.IO) {
                    db.taskDao().getNextTaskIdForUser(userId)
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

                loadTasks()

                val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                val formattedDate = dateFormat.format(dueDate)

                Toast.makeText(
                    this@FragmentTaskActivity,
                    "✅ Task '$title' created!\n📅 Due: $formattedDate",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                android.util.Log.e("FragmentTaskActivity", "Error creating task", e)
                Toast.makeText(this@FragmentTaskActivity, "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
