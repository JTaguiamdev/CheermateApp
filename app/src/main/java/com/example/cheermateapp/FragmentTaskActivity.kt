package com.example.cheermateapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
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

    // ✅ Task description display (from fragment_tasks.xml)
    private lateinit var tvTaskDescription: TextView

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

    // ✅ UPDATED: Initialize views
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

            // Task description display (from fragment_tasks.xml)
            tvTaskDescription = findViewById(R.id.tvTaskDescription)

            // Navigation elements
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

    // ✅ FIXED: Setup interactions
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

            // Navigation button interactions
            btnPreviousTask.setOnClickListener {
                navigateToPreviousTask()
            }

            btnNextTask.setOnClickListener {
                navigateToNextTask()
            }

            updateTabSelection(tabAll)

            android.util.Log.d("FragmentTaskActivity", "✅ Interactions setup complete")
        } catch (e: Exception) {
            android.util.Log.e("FragmentTaskActivity", "Error setting up interactions", e)
        }
    }

    // ✅ NAVIGATION METHODS
    private fun navigateToPreviousTask() {
        if (filteredTasks.isNotEmpty() && currentTaskIndex > 0) {
            currentTaskIndex--
            displaySingleTask(filteredTasks[currentTaskIndex])
            updateNavigationState()
            Toast.makeText(this, "◀ Previous task", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToNextTask() {
        if (filteredTasks.isNotEmpty() && currentTaskIndex < filteredTasks.size - 1) {
            currentTaskIndex++
            displaySingleTask(filteredTasks[currentTaskIndex])
            updateNavigationState()
            Toast.makeText(this, "Next task ▶", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateNavigationState() {
        try {
            if (filteredTasks.size <= 1) {
                layoutNavigation.visibility = View.GONE
            } else {
                layoutNavigation.visibility = View.VISIBLE

                // Update counter
                tvTaskCounter.text = "${currentTaskIndex + 1} / ${filteredTasks.size}"

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

    // ✅ MAIN DISPLAY METHOD
    private fun displayTaskInCard(tasks: List<Task>) {
        try {
            filteredTasks = tasks
            currentTaskIndex = 0

            if (tasks.isEmpty()) {
                showEmptyState()
            } else {
                displaySingleTask(tasks[currentTaskIndex])
                updateNavigationState()
            }
        } catch (e: Exception) {
            android.util.Log.e("FragmentTaskActivity", "Error displaying task in card", e)
            showEmptyState()
        }
    }

    // ✅ DISPLAY SINGLE TASK USING item_task.xml LAYOUT
    private fun displaySingleTask(task: Task) {
        try {
            android.util.Log.d("DEBUG", "🎯 displaySingleTask called with task: ${task.Title}")

            currentDisplayedTask = task

            // Hide tvTaskDescription and show task in cardEmpty
            tvTaskDescription.visibility = View.GONE
            cardEmpty.visibility = View.VISIBLE

            // Clear cardEmpty and inflate item_task.xml
            cardEmpty.removeAllViews()
            inflateTaskItemLayout(task, cardEmpty)

            android.util.Log.d("DEBUG", "✅ Task displayed using item_task.xml layout")

        } catch (e: Exception) {
            android.util.Log.e("DEBUG", "❌ Error in displaySingleTask: ${e.message}", e)
            showEmptyState()
        }
    }

    // ✅ INFLATE AND POPULATE item_task.xml LAYOUT
    private fun inflateTaskItemLayout(task: Task, container: LinearLayout) {
        try {
            // Inflate the item_task.xml layout
            val inflater = LayoutInflater.from(this)
            val taskItemView = inflater.inflate(R.layout.item_task, container, false)

            // ✅ FIND ALL VIEWS FROM item_task.xml
            val layoutPriorityIndicator = taskItemView.findViewById<View>(R.id.layoutPriorityIndicator)
            val tvTaskTitle = taskItemView.findViewById<TextView>(R.id.tvTaskTitle)
            val tvTaskDescriptionItem = taskItemView.findViewById<TextView>(R.id.tvTaskDescription)
            val tvTaskPriority = taskItemView.findViewById<TextView>(R.id.tvTaskPriority)
            val tvTaskStatus = taskItemView.findViewById<TextView>(R.id.tvTaskStatus)
            val progressBarLayout = taskItemView.findViewById<LinearLayout>(R.id.progressBar)?.parent as? LinearLayout
            val progressBar = taskItemView.findViewById<ProgressBar>(R.id.progressBar)
            val tvTaskProgress = taskItemView.findViewById<TextView>(R.id.tvTaskProgress)
            val tvTaskDueDate = taskItemView.findViewById<TextView>(R.id.tvTaskDueDate)
            val btnComplete = taskItemView.findViewById<TextView>(R.id.btnComplete)
            val btnEdit = taskItemView.findViewById<TextView>(R.id.btnEdit)
            val btnDelete = taskItemView.findViewById<TextView>(R.id.btnDelete)

            // ✅ POPULATE ALL FIELDS WITH TASK DATA

            // 1. Priority Indicator Bar Color
            val priorityColor = when (task.Priority) {
                Priority.High -> android.graphics.Color.RED
                Priority.Medium -> android.graphics.Color.parseColor("#FFA500") // Orange
                Priority.Low -> android.graphics.Color.GREEN
            }
            layoutPriorityIndicator.setBackgroundColor(priorityColor)

            // 2. Task Title
            tvTaskTitle.text = task.Title

            // 3. Task Description
            if (!task.Description.isNullOrBlank()) {
                tvTaskDescriptionItem.text = task.Description
                tvTaskDescriptionItem.visibility = View.VISIBLE
            } else {
                tvTaskDescriptionItem.visibility = View.GONE
            }

            // 4. Priority Text
            tvTaskPriority.text = task.getPriorityText()

            // 5. Status Text
            tvTaskStatus.text = task.getStatusText()

            // 6. Progress Bar and Percentage
            if (task.TaskProgress > 0) {
                progressBarLayout?.visibility = View.VISIBLE
                progressBar?.progress = task.TaskProgress
                tvTaskProgress.text = "${task.TaskProgress}%"
            } else {
                progressBarLayout?.visibility = View.GONE
            }

            // 7. Due Date
            tvTaskDueDate.text = "📅 Due: ${task.getFormattedDueDateTime()}"

            // 8. Button States based on Task Status
            when (task.Status) {
                Status.Completed -> {
                    btnComplete.text = "✅ Completed"
                    btnComplete.isClickable = false
                    btnComplete.alpha = 0.6f
                }
                Status.Pending -> {
                    btnComplete.text = "✅ Complete"
                    btnComplete.isClickable = true
                    btnComplete.alpha = 1.0f
                }
                Status.InProgress -> {
                    btnComplete.text = "✅ Finish"
                    btnComplete.isClickable = true
                    btnComplete.alpha = 1.0f
                }
                Status.OverDue -> {
                    btnComplete.text = "🔴 Complete"
                    btnComplete.isClickable = true
                    btnComplete.alpha = 1.0f
                }
                Status.Cancelled -> {
                    btnComplete.text = "❌ Cancelled"
                    btnComplete.isClickable = false
                    btnComplete.alpha = 0.6f
                }
            }

            // ✅ SET UP CLICK LISTENERS FOR ACTION BUTTONS

            // Complete Button
            btnComplete.setOnClickListener {
                if (task.Status != Status.Completed && task.Status != Status.Cancelled) {
                    markTaskAsDone(task)
                }
            }

            // Edit Button
            btnEdit.setOnClickListener {
                showTaskQuickActions(task)
            }

            // Delete Button
            btnDelete.setOnClickListener {
                deleteTask(task)
            }

            // ✅ ADD MAIN CARD CLICK LISTENER
            taskItemView.setOnClickListener {
                showTaskDetailsDialog(task)
            }

            // ✅ ADD THE INFLATED VIEW TO CONTAINER
            container.addView(taskItemView)

            android.util.Log.d("FragmentTaskActivity", "✅ Successfully inflated and populated item_task.xml")

        } catch (e: Exception) {
            android.util.Log.e("FragmentTaskActivity", "❌ Error inflating task item layout: ${e.message}", e)
        }
    }

    // ✅ EXTENSION METHODS FROM MAINACTIVITY
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

    private fun Task.getFormattedDueDateTime(): String {
        val dueDate = this.DueAt ?: return "No due date"
        val dueTime = this.DueTime

        return if (dueTime.isNullOrBlank()) {
            dueDate
        } else {
            "$dueDate at $dueTime"
        }
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

    // ✅ EMPTY STATE
    private fun showEmptyState() {
        try {
            currentDisplayedTask = null
            filteredTasks = emptyList()
            currentTaskIndex = 0

            // Show empty message in tvTaskDescription
            tvTaskDescription.text = "📋 No tasks available\n\nSelect a filter to view your tasks or add a new task to get started!"
            tvTaskDescription.visibility = View.VISIBLE

            // Clear cardEmpty
            cardEmpty.removeAllViews()
            cardEmpty.visibility = View.GONE

            // Hide navigation
            layoutNavigation.visibility = View.GONE

            android.util.Log.d("FragmentTaskActivity", "📋 Showing empty state")

        } catch (e: Exception) {
            android.util.Log.e("FragmentTaskActivity", "Error showing empty state", e)
        }
    }

    // ✅ IMPLEMENT showTaskDetailsDialog FROM MAINACTIVITY
    private fun showTaskDetailsDialog(task: Task) {
        val message = buildString {
            append("📝 Title: ${task.Title}\n")
            if (!task.Description.isNullOrBlank()) {
                append("📄 Description: ${task.Description}\n")
            }
            append("🎯 Priority: ${task.Priority}\n")
            append("📊 Status: ${task.Status}\n")
            append("📈 Progress: ${task.TaskProgress}%\n")
            append("📅 Due Date: ${task.DueAt ?: "Not set"}\n")
            if (!task.DueTime.isNullOrBlank()) {
                append("⏰ Due Time: ${task.DueTime}\n")
            }
            append("📅 Created: ${formatTimestamp(task.CreatedAt)}")
        }

        AlertDialog.Builder(this)
            .setTitle("Task Details")
            .setMessage(message)
            .setPositiveButton("Mark as Done") { _, _ ->
                markTaskAsDone(task)
            }
            .setNeutralButton("Edit") { _, _ ->
                showTaskQuickActions(task)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    // ✅ IMPLEMENT showTaskQuickActions FROM MAINACTIVITY
    private fun showTaskQuickActions(task: Task) {
        val actions = arrayOf(
            "✅ Mark as Done",
            "🔄 Mark as In Progress",
            "⏳ Mark as Pending",
            "✏️ Edit Task",
            "🗑️ Delete Task"
        )

        AlertDialog.Builder(this)
            .setTitle("Quick Actions: ${task.Title}")
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> markTaskAsDone(task)
                    1 -> markTaskAsInProgress(task)
                    2 -> markTaskAsPending(task)
                    3 -> showEditTaskDialog(task)
                    4 -> deleteTask(task)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ✅ TASK ACTION METHODS
    private fun markTaskAsDone(task: Task) {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentTaskActivity)
                withContext(Dispatchers.IO) {
                    db.taskDao().updateTaskStatus(task.Task_ID, task.User_ID, "Completed")
                    db.taskDao().updateTaskProgress(task.User_ID, task.Task_ID, 100)
                }

                Toast.makeText(this@FragmentTaskActivity, "✅ Task '${task.Title}' marked as done!", Toast.LENGTH_SHORT).show()
                loadTasks()

            } catch (e: Exception) {
                android.util.Log.e("FragmentTaskActivity", "Error marking task as done", e)
                Toast.makeText(this@FragmentTaskActivity, "❌ Error updating task", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun markTaskAsInProgress(task: Task) {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentTaskActivity)
                withContext(Dispatchers.IO) {
                    db.taskDao().updateTaskStatus(task.Task_ID, task.User_ID, "InProgress")
                }

                Toast.makeText(this@FragmentTaskActivity, "🔄 Task '${task.Title}' is in progress!", Toast.LENGTH_SHORT).show()
                loadTasks()

            } catch (e: Exception) {
                android.util.Log.e("FragmentTaskActivity", "Error marking task as in progress", e)
                Toast.makeText(this@FragmentTaskActivity, "❌ Error updating task", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun markTaskAsPending(task: Task) {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentTaskActivity)
                withContext(Dispatchers.IO) {
                    db.taskDao().updateTaskStatus(task.Task_ID, task.User_ID, "Pending")
                }

                Toast.makeText(this@FragmentTaskActivity, "⏳ Task '${task.Title}' marked as pending!", Toast.LENGTH_SHORT).show()
                loadTasks()

            } catch (e: Exception) {
                android.util.Log.e("FragmentTaskActivity", "Error marking task as pending", e)
                Toast.makeText(this@FragmentTaskActivity, "❌ Error updating task", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✅ HELPER METHOD: Format timestamps
    private fun formatTimestamp(timestamp: Long): String {
        return try {
            val date = Date(timestamp)
            val format = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
            format.format(date)
        } catch (e: Exception) {
            "Unknown date"
        }
    }

    // ✅ DEBUG METHODS - (abbreviated for space)
    private fun debugTaskLoading() {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentTaskActivity)
                val userTasks = withContext(Dispatchers.IO) {
                    db.taskDao().getAllTasksForUser(userId)
                }
                android.util.Log.d("DEBUG", "Tasks for user $userId: ${userTasks.size}")
            } catch (e: Exception) {
                android.util.Log.e("DEBUG", "Debug error: ${e.message}", e)
            }
        }
    }

    private fun debugAllTasks() {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentTaskActivity)
                val allTasks = withContext(Dispatchers.IO) {
                    db.taskDao().debugGetAllTasksForUser(userId)
                }
                android.util.Log.d("FragmentTaskActivity", "🔍 Total tasks in DB: ${allTasks.size}")
            } catch (e: Exception) {
                android.util.Log.e("FragmentTaskActivity", "❌ Debug error", e)
            }
        }
    }

    private suspend fun debugDatabaseContent() {
        try {
            val db = AppDb.get(this)
            val allTasks = withContext(Dispatchers.IO) {
                db.taskDao().debugGetAllTasksForUser(userId)
            }
            android.util.Log.d("FragmentTaskActivity", "Total tasks in DB: ${allTasks.size}")
        } catch (e: Exception) {
            android.util.Log.e("FragmentTaskActivity", "Debug error", e)
        }
    }

    // ✅ CREATE TEST TASKS
    private suspend fun createTestTasks() {
        try {
            val db = AppDb.get(this)
            val existingTasks = withContext(Dispatchers.IO) {
                db.taskDao().getAllTasksForUser(userId)
            }

            if (existingTasks.isEmpty()) {
                withContext(Dispatchers.IO) {
                    val currentTime = System.currentTimeMillis()

                    val task1 = Task(
                        Task_ID = 1, User_ID = userId,
                        Title = "Complete Android App",
                        Description = "Finish the CheermateApp project with all CRUD operations",
                        Priority = Priority.High, Status = Status.Pending, TaskProgress = 75,
                        DueAt = "2025-09-29", DueTime = "14:30",
                        CreatedAt = currentTime, UpdatedAt = currentTime, DeletedAt = null
                    )

                    val task2 = Task(
                        Task_ID = 2, User_ID = userId,
                        Title = "Study for Exam",
                        Description = "Computer Science midterm exam preparation",
                        Priority = Priority.Medium, Status = Status.InProgress, TaskProgress = 50,
                        DueAt = "2025-09-30", DueTime = "10:00",
                        CreatedAt = currentTime, UpdatedAt = currentTime, DeletedAt = null
                    )

                    val task3 = Task(
                        Task_ID = 3, User_ID = userId,
                        Title = "Buy Groceries",
                        Description = "Weekly shopping list for household items",
                        Priority = Priority.Low, Status = Status.Completed, TaskProgress = 100,
                        DueAt = "2025-09-28", DueTime = "16:00",
                        CreatedAt = currentTime - 86400000L, UpdatedAt = currentTime, DeletedAt = null
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

    // ✅ FILTERING AND TAB MANAGEMENT
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

    private fun filterTasks(filterType: FilterType) {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentTaskActivity)
                val tasks = withContext(Dispatchers.IO) {
                    when (filterType) {
                        FilterType.ALL -> db.taskDao().getAllTasksForUser(userId)
                        FilterType.TODAY -> {
                            val todayStr = getCurrentDateString()
                            db.taskDao().getTodayTasks(userId, todayStr)
                        }
                        FilterType.PENDING -> db.taskDao().getPendingTasks(userId)
                        FilterType.DONE -> db.taskDao().getCompletedTasks(userId)
                    }
                }

                allTasks.clear()
                allTasks.addAll(tasks)
                displayTaskInCard(tasks)
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
                tvTasksSub.text = "${counts["all"]} total tasks"
                chipFound.text = "${currentTasks.size} found"

            } catch (e: Exception) {
                android.util.Log.e("FragmentTaskActivity", "Error updating tab counts", e)
            }
        }
    }

    private fun searchTasks(query: String?) {
        if (query.isNullOrBlank()) {
            displayTaskInCard(allTasks)
        } else {
            val searchResults = allTasks.filter { task ->
                task.Title.contains(query, ignoreCase = true) ||
                        (task.Description?.contains(query, ignoreCase = true) == true)
            }
            displayTaskInCard(searchResults)
        }
        chipFound.text = "${filteredTasks.size} found"
    }

    // ✅ MINIMAL IMPLEMENTATIONS OF REQUIRED METHODS
    private fun showSortOptionsDialog() {
        Toast.makeText(this, "📊 Sort functionality coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun showAddTaskDialog() {
        Toast.makeText(this, "➕ Add task functionality coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun showEditTaskDialog(task: Task) {
        Toast.makeText(this, "✏️ Edit task functionality coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun deleteTask(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete '${task.Title}'?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val db = AppDb.get(this@FragmentTaskActivity)
                        withContext(Dispatchers.IO) {
                            db.taskDao().softDelete(task.Task_ID, task.User_ID)
                        }
                        Toast.makeText(this@FragmentTaskActivity, "🗑️ Task deleted!", Toast.LENGTH_SHORT).show()
                        loadTasks()
                    } catch (e: Exception) {
                        Toast.makeText(this@FragmentTaskActivity, "❌ Error deleting task", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ✅ HELPER METHODS
    private fun getCurrentDateString(): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(Date())
    }

    // ✅ NAVIGATION
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

    override fun onSupportNavigateUp(): Boolean {
        navigateToMain()
        return true
    }

    override fun onResume() {
        super.onResume()
        loadTasks()
    }
}
