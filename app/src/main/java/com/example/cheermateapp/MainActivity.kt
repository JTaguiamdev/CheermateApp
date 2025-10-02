package com.example.cheermateapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.example.cheermateapp.data.model.Priority.High
import com.example.cheermateapp.data.model.Priority.Low
import com.example.cheermateapp.data.model.Priority.Medium
import com.example.cheermateapp.data.model.Status
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.cheermateapp.data.db.AppDb
import com.example.cheermateapp.data.model.Personality
import com.example.cheermateapp.data.model.Task
import com.example.cheermateapp.data.model.Priority
import com.example.cheermateapp.data.model.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SHOW_DASHBOARD = "SHOW_DASHBOARD"
        const val EXTRA_USER_ID = "USER_ID"
    }

    private val uiScope = CoroutineScope(Dispatchers.Main)
    private var userId: Int = 0

    // ✅ LIVE TASK UPDATE SYSTEM
    private var taskUpdateHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var taskUpdateRunnable: Runnable? = null
    private var isTaskUpdateActive = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            val showDashboard = intent?.getBooleanExtra(EXTRA_SHOW_DASHBOARD, false) == true
            userId = intent?.getStringExtra(EXTRA_USER_ID)?.toIntOrNull() ?: 0

            if (showDashboard && userId != 0) {
                setContentView(R.layout.activity_main)

                setupToolbar()
                setupGreeting()
                setupBottomNavigation()
                setupHomeScreenInteractions()
                setupCalendarView()

                // Load dashboard data
                loadUserData()
                loadTaskStatistics()
                loadRecentTasks()

                // ✅ START LIVE TASK UPDATES
                startLiveTaskUpdates()

                showHomeScreen()

            } else {
                // Redirect to login if not properly authenticated
                startActivity(Intent(this, ActivityLogin::class.java))
                finish()
            }

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Critical error in onCreate", e)
            Toast.makeText(this, "Loading dashboard...", Toast.LENGTH_SHORT).show()
            createFallbackLayout()
        }
    }

    // ✅ START LIVE TASK UPDATES (Call this in onCreate)
    private fun startLiveTaskUpdates() {
        stopLiveTaskUpdates() // Stop any existing updates

        isTaskUpdateActive = true
        taskUpdateRunnable = object : Runnable {
            override fun run() {
                if (isTaskUpdateActive) {
                    // Update overdue tasks
                    updateOverdueTasks()

                    // Refresh dashboard if on home screen
                    if (findViewById<ScrollView>(R.id.homeScroll)?.visibility == View.VISIBLE) {
                        loadTaskStatistics()
                        loadRecentTasks()
                    }

                    // Refresh tasks fragment if active
                    if (findViewById<FrameLayout>(R.id.contentContainer)?.visibility == View.VISIBLE) {
                        loadTasksFragmentData()
                    }

                    // Schedule next update in 30 seconds
                    taskUpdateHandler.postDelayed(this, 30000) // Update every 30 seconds
                }
            }
        }

        // Start immediate update, then every 30 seconds
        taskUpdateHandler.post(taskUpdateRunnable!!)

        android.util.Log.d("MainActivity", "✅ Live task updates started")
    }

    // ✅ STOP LIVE TASK UPDATES (Call this in onDestroy)
    private fun stopLiveTaskUpdates() {
        isTaskUpdateActive = false
        taskUpdateRunnable?.let { taskUpdateHandler.removeCallbacks(it) }
        android.util.Log.d("MainActivity", "⏹️ Live task updates stopped")
    }

    // ✅ SIMPLIFIED VERSION - WITHOUT OVERDUE STATUS
    private fun updateOverdueTasks() {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)
                val updatedCount: Int = withContext(Dispatchers.IO) {
                    val currentDateTime: Long = System.currentTimeMillis()

                    // Get all pending tasks that are overdue
                    val overdueTasks: List<Task> = db.taskDao().getPendingTasks(userId)
                        .filter { task ->
                            task.DueAt != null && task.DueAt!!.isNotEmpty() &&
                                    isTaskOverdue(task, currentDateTime)
                        }

                    // Since no Overdue status, just log them for now
                    overdueTasks.forEach { task ->
                        android.util.Log.d("MainActivity", "📅 Task '${task.Title}' is overdue")
                    }

                    overdueTasks.size
                }

                if (updatedCount > 0) {
                    android.util.Log.d("MainActivity", "📅 Found $updatedCount overdue tasks")
                }

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error checking overdue tasks", e)
            }
        }
    }



    // ✅ FIXED - Use only existing status values
    private fun isTaskOverdue(task: Task, currentTime: Long): Boolean {
        if (task.Status == Status.Completed) {
            return false // Already completed
        }

        try {
            val dueDate = task.DueAt ?: return false
            val dueTime = task.DueTime

            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsedDate = dateFormat.parse(dueDate)

            if (parsedDate != null) {
                calendar.time = parsedDate

                // If has due time, parse and set it
                if (!dueTime.isNullOrBlank()) {
                    try {
                        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val parsedTime = timeFormat.parse(dueTime)
                        if (parsedTime != null) {
                            val timeCalendar = Calendar.getInstance()
                            timeCalendar.time = parsedTime

                            calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                            calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                        }
                    } catch (e: Exception) {
                        calendar.set(Calendar.HOUR_OF_DAY, 23)
                        calendar.set(Calendar.MINUTE, 59)
                    }
                } else {
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                }

                return currentTime > calendar.timeInMillis
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error checking if task is overdue", e)
        }

        return false
    }


    // ✅ FIXED EXTENSION FUNCTION WITH FULLY QUALIFIED STATUS NAMES:
    private fun getTaskStatusEmoji(task: Task): String {
        return when (task.Status) {
            Status.Pending -> "⏳"
            Status.InProgress -> "⏳"
            Status.OverDue -> "🔄"
            Status.Cancelled -> "❌"
            Status.Completed -> "✅"
        }
    }


    private fun Task.getPriorityColor(): Int {
        return when (this.Priority) {
            com.example.cheermateapp.data.model.Priority.High -> android.graphics.Color.RED
            com.example.cheermateapp.data.model.Priority.Medium -> android.graphics.Color.parseColor("#FFA500") // Orange
            com.example.cheermateapp.data.model.Priority.Low -> android.graphics.Color.GREEN
            else -> android.graphics.Color.GRAY
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

    // ✅ SETUP METHODS
    private fun setupToolbar() {
        try {
            val toolbar = findViewById<Toolbar>(R.id.toolbar)
            if (toolbar != null) {
                setSupportActionBar(toolbar)
                supportActionBar?.setDisplayShowTitleEnabled(false)
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error setting up toolbar", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupGreeting() {
        try {
            val tvGreeting = findViewById<TextView>(R.id.tvGreeting)

            val hour = java.time.LocalTime.now().hour
            val greeting = when (hour) {
                in 6..11 -> "Good morning"
                in 12..16 -> "Good afternoon"
                in 17..20 -> "Good evening"
                else -> "Good night"
            }

            tvGreeting?.text = greeting

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error setting up greeting", e)
        }
    }

    // ✅ UPDATED BOTTOM NAVIGATION TO HANDLE HOME AND FRAGMENT SWITCHING
    private fun setupBottomNavigation() {
        try {
            val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
            bottomNav?.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        showHomeScreen()
                        true
                    }

                    R.id.nav_tasks -> {
                        navigateToTasks()
                        true
                    }

                    R.id.nav_settings -> {
                        navigateToSettings()
                        true
                    }

                    else -> false
                }
            }

            bottomNav?.selectedItemId = R.id.nav_home

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error setting up bottom navigation", e)
        }
    }

    private fun showHomeScreen() {
        try {
            findViewById<ScrollView>(R.id.homeScroll)?.visibility = View.VISIBLE
            val container = findViewById<FrameLayout>(R.id.contentContainer)
            container?.removeAllViews()
            container?.visibility = View.GONE
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error showing home screen", e)
        }
    }

    // ✅ NAVIGATION METHODS - FRAGMENT SWITCHING (NOT SEPARATE ACTIVITIES)
    private fun navigateToTasks() {
        try {
            // Hide home screen
            findViewById<ScrollView>(R.id.homeScroll)?.visibility = View.GONE

            // Show content container
            val container = findViewById<FrameLayout>(R.id.contentContainer)
            container?.removeAllViews()
            container?.visibility = View.VISIBLE

            // Inflate tasks fragment layout into the container
            LayoutInflater.from(this).inflate(R.layout.fragment_tasks, container, true)

            // Initialize task-specific functionality
            setupTasksFragment()

            android.util.Log.d("MainActivity", "✅ Loaded Tasks Fragment")

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error loading tasks fragment", e)
            Toast.makeText(this, "Error loading tasks", Toast.LENGTH_SHORT).show()
            showHomeScreen() // Fallback to home
        }
    }

    private fun navigateToSettings() {
        try {
            // Hide home screen
            findViewById<ScrollView>(R.id.homeScroll)?.visibility = View.GONE

            // Show content container
            val container = findViewById<FrameLayout>(R.id.contentContainer)
            container?.removeAllViews()
            container?.visibility = View.VISIBLE

            // Inflate settings fragment layout into the container
            LayoutInflater.from(this).inflate(R.layout.fragment_settings, container, true)

            // Initialize settings-specific functionality
            setupSettingsFragment()

            android.util.Log.d("MainActivity", "✅ Loaded Settings Fragment")

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error loading settings fragment", e)
            Toast.makeText(this, "Error loading settings", Toast.LENGTH_SHORT).show()
            showHomeScreen() // Fallback to home
        }
    }

    // ✅ FRAGMENT SETUP METHODS - MAKE FRAGMENTS FUNCTIONAL
    private fun setupTasksFragment() {
        try {
            // Initialize all task fragment components
            val tvTasksTitle = findViewById<TextView>(R.id.tvTasksTitle)
            val btnAddTask = findViewById<TextView>(R.id.btnAddTask)
            val tvTasksSub = findViewById<TextView>(R.id.tvTasksSub)
            val etSearch = findViewById<EditText>(R.id.etSearch)
            val btnSort = findViewById<TextView>(R.id.btnSort)
            val chipFound = findViewById<TextView>(R.id.chipFound)
            val tabAll = findViewById<TextView>(R.id.tabAll)
            val tabToday = findViewById<TextView>(R.id.tabToday)
            val tabPending = findViewById<TextView>(R.id.tabPending)
            val tabDone = findViewById<TextView>(R.id.tabDone)
            val cardEmpty = findViewById<LinearLayout>(R.id.cardEmpty)

            // Setup task fragment interactions
            btnAddTask?.setOnClickListener {
                showQuickAddTaskDialog()
            }

            btnSort?.setOnClickListener {
                showSortOptionsDialog()
            }

            // Search functionality
            etSearch?.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    searchTasks(s?.toString())
                }
            })

            // Filter tabs
            tabAll?.setOnClickListener {
                setTaskFilter("ALL")
                updateTaskTabSelection(tabAll)
            }

            tabToday?.setOnClickListener {
                setTaskFilter("TODAY")
                updateTaskTabSelection(tabToday)
            }

            tabPending?.setOnClickListener {
                setTaskFilter("PENDING")
                updateTaskTabSelection(tabPending)
            }

            tabDone?.setOnClickListener {
                setTaskFilter("DONE")
                updateTaskTabSelection(tabDone)
            }

            // Load initial task data
            loadTasksFragmentData()

            android.util.Log.d("MainActivity", "✅ Tasks fragment initialized")

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error setting up tasks fragment", e)
            Toast.makeText(this, "Error loading tasks", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSettingsFragment() {
        try {
            // Initialize all settings fragment components
            val tvProfileName = findViewById<TextView>(R.id.tvProfileName)
            val tvProfileEmail = findViewById<TextView>(R.id.tvProfileEmail)
            val tvCurrentPersona = findViewById<TextView>(R.id.tvCurrentPersona)
            val chipPersona = findViewById<TextView>(R.id.chipPersona)
            val tvStatTotal = findViewById<TextView>(R.id.tvStatTotal)
            val tvStatCompleted = findViewById<TextView>(R.id.tvStatCompleted)
            val tvStatSuccess = findViewById<TextView>(R.id.tvStatSuccess)
            val cardProfile = findViewById<LinearLayout>(R.id.cardProfile)
            val cardStats = findViewById<LinearLayout>(R.id.cardStats)
            val rowPersonality = findViewById<LinearLayout>(R.id.rowPersonality)
            val rowDarkMode = findViewById<LinearLayout>(R.id.rowDarkMode)
            val rowNotifications = findViewById<LinearLayout>(R.id.rowNotifications)
            val cardSignOut = findViewById<LinearLayout>(R.id.cardSignOut)
            val switchDarkMode = findViewById<Switch>(R.id.switchDarkMode)
            val switchNotifications = findViewById<Switch>(R.id.switchNotifications)

            // Setup settings interactions
            cardProfile?.setOnClickListener {
                showProfileEditDialog()
            }

            cardStats?.setOnClickListener {
                showDetailedStatistics()
            }

            rowPersonality?.setOnClickListener {
                showPersonalitySelectionDialog()
            }

            switchDarkMode?.setOnCheckedChangeListener { _, isChecked ->
                Toast.makeText(
                    this,
                    if (isChecked) "🌙 Dark mode enabled" else "☀️ Light mode enabled",
                    Toast.LENGTH_SHORT
                ).show()
            }

            switchNotifications?.setOnCheckedChangeListener { _, isChecked ->
                Toast.makeText(
                    this,
                    if (isChecked) "🔔 Notifications enabled" else "🔕 Notifications disabled",
                    Toast.LENGTH_SHORT
                ).show()
            }

            cardSignOut?.setOnClickListener {
                showLogoutConfirmation()
            }

            // Load settings data
            loadSettingsFragmentData()

            android.util.Log.d("MainActivity", "✅ Settings fragment initialized")

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error setting up settings fragment", e)
            Toast.makeText(this, "Error loading settings", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ TASK FRAGMENT DATA METHODS
    private var currentTaskFilter = "ALL"
    private var currentTasks = mutableListOf<Task>()

    private fun loadTasksFragmentData() {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)
                val tasks: List<Task> = withContext(Dispatchers.IO) {
                    when (currentTaskFilter) {
                        "ALL" -> db.taskDao().getAllTasksForUser(userId)
                        "TODAY" -> {
                            val todayStr = dateToString(Date())
                            db.taskDao().getTodayTasks(userId, todayStr)
                        }
                        "PENDING" -> db.taskDao().getPendingTasks(userId)
                        "DONE" -> db.taskDao().getCompletedTasks(userId)
                        else -> db.taskDao().getAllTasksForUser(userId)
                    }
                }

                currentTasks.clear()
                currentTasks.addAll(tasks)
                updateTasksFragmentUI()

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error loading tasks fragment data", e)
            }
        }
    }

    private fun updateTasksFragmentUI() {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)
                val counts: Map<String, Int> = withContext(Dispatchers.IO) {
                    val todayStr = dateToString(Date())
                    mapOf(
                        "all" to db.taskDao().getAllTasksCount(userId),
                        "today" to db.taskDao().getTodayTasksCount(userId, todayStr),
                        "pending" to db.taskDao().getPendingTasksCount(userId),
                        "done" to db.taskDao().getCompletedTasksCount(userId)
                    )
                }

                // Update UI elements
                findViewById<TextView>(R.id.tvTasksSub)?.text = "${counts["all"]} total tasks"
                findViewById<TextView>(R.id.chipFound)?.text = "${currentTasks.size} found"

                findViewById<TextView>(R.id.tabAll)?.text = "All (${counts["all"]})"
                findViewById<TextView>(R.id.tabToday)?.text = "Today (${counts["today"]})"
                findViewById<TextView>(R.id.tabPending)?.text = "Pending (${counts["pending"]})"
                findViewById<TextView>(R.id.tabDone)?.text = "Done (${counts["done"]})"

                // Update empty state
                val cardEmpty = findViewById<LinearLayout>(R.id.cardEmpty)
                cardEmpty?.visibility = if (currentTasks.isEmpty()) View.VISIBLE else View.GONE

                // Show task list (simplified - you can enhance this)
                displayTasksList()

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error updating tasks fragment UI", e)
            }
        }
    }

    private fun displayTasksList() {
        try {
            if (currentTasks.isNotEmpty()) {
                // Create a simple task list display
                val taskListText = StringBuilder()
                taskListText.append("📋 Your Tasks:\n\n")

                currentTasks.take(5).forEach { task ->
                    taskListText.append("${task.getStatusEmoji()} ${task.Title}\n")
                    taskListText.append("   Priority: ${task.Priority} | Status: ${task.Status}\n")
                    if (task.DueAt != null) {
                        taskListText.append("   Due: ${task.getFormattedDueDateTime()}\n")
                    }
                    taskListText.append("\n")
                }

                if (currentTasks.size > 5) {
                    taskListText.append("... and ${currentTasks.size - 5} more tasks\n")
                }

                // Show in a toast for now (you can create a proper RecyclerView later)
                Toast.makeText(this, taskListText.toString(), Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error displaying tasks list", e)
        }
    }

    private fun setTaskFilter(filter: String) {
        currentTaskFilter = filter
        loadTasksFragmentData()
    }

    private fun updateTaskTabSelection(selectedTab: TextView) {
        try {
            // Reset all tabs
            listOf(R.id.tabAll, R.id.tabToday, R.id.tabPending, R.id.tabDone).forEach { tabId ->
                findViewById<TextView>(tabId)?.apply {
                    setBackgroundResource(android.R.color.transparent)
                    alpha = 0.7f
                }
            }

            // Highlight selected tab
            selectedTab.setBackgroundResource(R.drawable.bg_chip_glass)
            selectedTab.alpha = 1.0f
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error updating tab selection", e)
        }
    }

    private fun searchTasks(query: String?) {
        // Simple search implementation
        if (query.isNullOrBlank()) {
            loadTasksFragmentData()
        } else {
            val filteredTasks: List<Task> = currentTasks.filter { task ->
                task.Title.contains(query, ignoreCase = true) ||
                        (task.Description?.contains(query, ignoreCase = true) == true)
            }
            currentTasks.clear()
            currentTasks.addAll(filteredTasks)
            updateTasksFragmentUI()
        }
    }

    private fun showSortOptionsDialog() {
        val sortOptions = arrayOf(
            "📅 Due Date",
            "🎯 Priority",
            "📝 Title",
            "📊 Status"
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
                        priority2.compareTo(priority1) // Reverse order for descending
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
                            com.example.cheermateapp.data.model.Status.Pending -> 1
                            com.example.cheermateapp.data.model.Status.InProgress -> 2
                            com.example.cheermateapp.data.model.Status.OverDue -> 3  // ✅ FIXED: OverDue (capital D)
                            com.example.cheermateapp.data.model.Status.Completed -> 4
                            com.example.cheermateapp.data.model.Status.Cancelled -> 5
                        }
                        val status2 = when (task2.Status) {
                            com.example.cheermateapp.data.model.Status.Pending -> 1
                            com.example.cheermateapp.data.model.Status.InProgress -> 2
                            com.example.cheermateapp.data.model.Status.OverDue -> 3  // ✅ FIXED: OverDue (capital D)
                            com.example.cheermateapp.data.model.Status.Completed -> 4
                            com.example.cheermateapp.data.model.Status.Cancelled -> 5
                        }
                        status1.compareTo(status2)
                    }
                }
                4 -> { // Progress (High first)
                    currentTasks.sortedWith { task1, task2 ->
                        task2.TaskProgress.compareTo(task1.TaskProgress) // Reverse for descending
                    }
                }
                else -> currentTasks
            }
            currentTasks.clear()
            currentTasks.addAll(sortedTasks)
            updateTasksFragmentUI()

            val sortNames = arrayOf("Due Date", "Priority", "Title", "Status", "Progress")
            val sortName = if (sortType in sortNames.indices) sortNames[sortType] else "Unknown"
            Toast.makeText(this, "📊 Sorted by $sortName", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            android.util.Log.e("FragmentTaskActivity", "Error sorting tasks", e)
            Toast.makeText(this, "Error sorting tasks", Toast.LENGTH_SHORT).show()
        }
    }


    // ✅ FIXED SETTINGS FRAGMENT DATA METHODS
    private fun loadSettingsFragmentData() {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)

                val user = withContext(Dispatchers.IO) {
                    db.userDao().getById(userId)
                }

                val personality: Personality? = withContext(Dispatchers.IO) {
                    db.personalityDao().getByUser(userId)
                }

                // ✅ FIXED: Define completedTasks properly
                val stats: Map<String, Int> = withContext(Dispatchers.IO) {
                    val totalTasks = db.taskDao().getAllTasksCount(userId)
                    val completedTasks = db.taskDao().getCompletedTasksCount(userId)

                    mapOf(
                        "total" to totalTasks,
                        "completed" to completedTasks
                    )
                }

                // Update profile info
                if (user != null) {
                    val displayName = when {
                        !user.FirstName.isNullOrBlank() && !user.LastName.isNullOrBlank() ->
                            "${user.FirstName} ${user.LastName}"

                        !user.FirstName.isNullOrBlank() -> user.FirstName
                        else -> user.Username
                    }
                    findViewById<TextView>(R.id.tvProfileName)?.text = displayName
                    findViewById<TextView>(R.id.tvProfileEmail)?.text =
                        user.Email ?: "user@example.com"
                }

                // Update personality info
                if (personality != null) {
                    findViewById<TextView>(R.id.tvCurrentPersona)?.text = personality.Name
                    findViewById<TextView>(R.id.chipPersona)?.text =
                        "${personality.Name} Personality"
                }

                // Update statistics
                val successRate = if (stats["total"]!! > 0) {
                    (stats["completed"]!! * 100) / stats["total"]!!
                } else 0

                findViewById<TextView>(R.id.tvStatTotal)?.text = "${stats["total"]}"
                findViewById<TextView>(R.id.tvStatCompleted)?.text = "${stats["completed"]}"
                findViewById<TextView>(R.id.tvStatSuccess)?.text = "$successRate%"

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error loading settings fragment data", e)
            }
        }
    }

    private fun showProfileEditDialog() {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)
                val user = withContext(Dispatchers.IO) {
                    db.userDao().getById(currentUserId)
                }

                if (user == null) {
                    Toast.makeText(this@MainActivity, "Error loading user profile", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Create custom dialog view
                val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
                val etEditUsername = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEditUsername)
                val etEditEmail = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEditEmail)
                val etEditFirstName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEditFirstName)
                val etEditLastName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEditLastName)

                // Pre-fill with current values
                etEditUsername.setText(user.Username)
                etEditEmail.setText(user.Email)
                etEditFirstName.setText(user.FirstName)
                etEditLastName.setText(user.LastName)

                androidx.appcompat.app.AlertDialog.Builder(this@MainActivity)
                    .setTitle("Edit Profile")
                    .setView(dialogView)
                    .setPositiveButton("Save") { _, _ ->
                        val newUsername = etEditUsername.text?.toString()?.trim().orEmpty()
                        val newEmail = etEditEmail.text?.toString()?.trim().orEmpty()
                        val newFirstName = etEditFirstName.text?.toString()?.trim().orEmpty()
                        val newLastName = etEditLastName.text?.toString()?.trim().orEmpty()

                        // Validate inputs
                        if (newUsername.isEmpty()) {
                            Toast.makeText(this@MainActivity, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        if (!com.example.cheermateapp.util.InputValidationUtil.isValidUsername(newUsername)) {
                            Toast.makeText(
                                this@MainActivity,
                                "Username must be 3-20 characters, letters, numbers, and underscore only",
                                Toast.LENGTH_LONG
                            ).show()
                            return@setPositiveButton
                        }

                        if (newEmail.isNotEmpty() && !com.example.cheermateapp.util.InputValidationUtil.isValidEmail(newEmail)) {
                            Toast.makeText(this@MainActivity, "Invalid email format", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        // Save changes
                        saveProfileChanges(user, newUsername, newEmail, newFirstName, newLastName)
                    }
                    .setNeutralButton("Change Password") { _, _ ->
                        showChangePasswordDialog(user)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error showing profile edit dialog", e)
                Toast.makeText(this@MainActivity, "Error loading profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Save profile changes to the database
     */
    private fun saveProfileChanges(
        user: User,
        newUsername: String,
        newEmail: String,
        newFirstName: String,
        newLastName: String
    ) {
        uiScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val db = AppDb.get(this@MainActivity)

                    // Check if new username already exists (if changed)
                    if (newUsername != user.Username) {
                        val existingUser = db.userDao().findByUsername(newUsername)
                        if (existingUser != null && existingUser.User_ID != user.User_ID) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Username already exists",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            return@withContext
                        }
                    }

                    // Update user
                    val updatedUser = user.copy(
                        Username = newUsername,
                        Email = newEmail,
                        FirstName = newFirstName,
                        LastName = newLastName,
                        UpdatedAt = System.currentTimeMillis()
                    )
                    db.userDao().update(updatedUser)
                }

                Toast.makeText(this@MainActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                
                // Reload settings fragment to show updated info
                loadSettingsFragmentData()

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error saving profile changes", e)
                Toast.makeText(this@MainActivity, "Error updating profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Show dialog to change password
     */
    private fun showChangePasswordDialog(user: User) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etCurrentPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etConfirmPassword)

        androidx.appcompat.app.AlertDialog.Builder(this@MainActivity)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val currentPassword = etCurrentPassword.text?.toString()?.trim().orEmpty()
                val newPassword = etNewPassword.text?.toString()?.trim().orEmpty()
                val confirmPassword = etConfirmPassword.text?.toString()?.trim().orEmpty()

                // Validate inputs
                if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(this@MainActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword != confirmPassword) {
                    Toast.makeText(this@MainActivity, "New passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (!com.example.cheermateapp.util.InputValidationUtil.isValidPassword(newPassword)) {
                    Toast.makeText(this@MainActivity, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Change password
                changePassword(user, currentPassword, newPassword)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Change user password in the database
     */
    private fun changePassword(user: User, currentPassword: String, newPassword: String) {
        uiScope.launch {
            try {
                val success = withContext(Dispatchers.IO) {
                    val db = AppDb.get(this@MainActivity)

                    // Verify current password
                    val isCurrentPasswordValid = com.example.cheermateapp.util.PasswordHashUtil.verifyPassword(
                        currentPassword,
                        user.PasswordHash
                    )

                    if (!isCurrentPasswordValid) {
                        return@withContext false
                    }

                    // Hash new password
                    val newHashedPassword = com.example.cheermateapp.util.PasswordHashUtil.hashPassword(newPassword)

                    // Update user
                    val updatedUser = user.copy(
                        PasswordHash = newHashedPassword,
                        UpdatedAt = System.currentTimeMillis()
                    )
                    db.userDao().update(updatedUser)
                    
                    return@withContext true
                }

                if (success) {
                    Toast.makeText(this@MainActivity, "Password changed successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Current password is incorrect", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error changing password", e)
                Toast.makeText(this@MainActivity, "Error changing password: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showPersonalitySelectionDialog() {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)
                val personalities: List<Personality> = withContext(Dispatchers.IO) {
                    db.personalityDao().getAll()
                }

                val personalityNames = personalities.map { it.Name }.toTypedArray()

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Choose Your Personality")
                    .setItems(personalityNames) { _, which ->
                        val selectedPersonality = personalities[which]
                        updateUserPersonality(selectedPersonality.Personality_ID)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error loading personalities", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun updateUserPersonality(personalityId: Int) {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)
                withContext(Dispatchers.IO) {
                    db.userDao().updatePersonality(userId, personalityId)
                }

                Toast.makeText(this@MainActivity, "✅ Personality updated!", Toast.LENGTH_SHORT)
                    .show()
                loadSettingsFragmentData()

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error updating personality", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Sign Out") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        val prefs = getSharedPreferences("CheerMate_Login", MODE_PRIVATE)
        prefs.edit().clear().apply()

        val intent = Intent(this, ActivityLogin::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // ✅ HOME SCREEN INTERACTIONS (DASHBOARD ONLY)
    private fun setupHomeScreenInteractions() {
        try {
            findViewById<View>(R.id.cardCalendar)?.setOnClickListener {
                Toast.makeText(this, "📅 Select a date to see tasks!", Toast.LENGTH_SHORT).show()
            }

            findViewById<TextView>(R.id.quickAddChip)?.setOnClickListener {
                showQuickAddTaskDialog()
            }

            findViewById<View>(R.id.cardPersonality)?.setOnClickListener {
                navigateToSettings() // Navigate to settings instead of handling here
            }

            findViewById<TextView>(R.id.btnMotivate)?.setOnClickListener {
                showMotivationalMessage()
            }

            findViewById<View>(R.id.cardStats)?.setOnClickListener {
                showDetailedStatistics()
            }

            findViewById<View>(R.id.cardProgress)?.setOnClickListener {
                showProgressDetails()
            }

            findViewById<View>(R.id.cardRecent)?.setOnClickListener {
                navigateToTasks() // Navigate to full task management
            }

            findViewById<TextView>(R.id.btnAddTaskChip)?.setOnClickListener {
                showQuickAddTaskDialog()
            }

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error setting up home interactions", e)
        }
    }

    // ✅ ENHANCED ADD TASK DIALOG (PROGRAMMATIC VERSION - NO LAYOUT FILE NEEDED)
    private fun showQuickAddTaskDialog() {
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 20)
        }

        // Title Input
        val etTitle = EditText(this).apply {
            hint = "Task Title (Required)"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setPadding(20, 20, 20, 20)
            setBackgroundResource(android.R.drawable.edit_text)
        }

        // Description Input
        val etDescription = EditText(this).apply {
            hint = "Description (Optional)"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
            maxLines = 3
            setPadding(20, 20, 20, 20)
            setBackgroundResource(android.R.drawable.edit_text)
        }

        // Priority Spinner
        val tvPriorityLabel = TextView(this).apply {
            text = "Priority:"
            textSize = 16f
            setPadding(0, 20, 0, 8)
        }

        val spinnerPriority = Spinner(this).apply {
            val priorities = arrayOf("Low", "Medium", "High")
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, priorities).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            setSelection(1) // Default to Medium
        }

        // Date and Time Layout
        val dateTimeLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 20, 0, 0)
        }

        val etDueDate = EditText(this).apply {
            hint = "Due Date (Required)"
            isFocusable = false
            isClickable = true
            setPadding(20, 20, 20, 20)
            setBackgroundResource(android.R.drawable.edit_text)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, 10, 0)
            }

            // Set today as default
            val today = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            setText(dateFormat.format(today.time))

            setOnClickListener { showDatePicker(this) }
        }

        val etDueTime = EditText(this).apply {
            hint = "Due Time (Optional)"
            isFocusable = false
            isClickable = true
            setPadding(20, 20, 20, 20)
            setBackgroundResource(android.R.drawable.edit_text)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(10, 0, 0, 0)
            }

            setOnClickListener { showTimePicker(this) }
        }

        dateTimeLayout.addView(etDueDate)
        dateTimeLayout.addView(etDueTime)

        // Add all views to dialog
        dialogLayout.addView(etTitle)
        dialogLayout.addView(etDescription)
        dialogLayout.addView(tvPriorityLabel)
        dialogLayout.addView(spinnerPriority)
        dialogLayout.addView(dateTimeLayout)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Task")
            .setView(dialogLayout)
            .setPositiveButton("Create Task", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val createButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            createButton.setOnClickListener {
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val dueDate = etDueDate.text.toString().trim()
                val dueTime = etDueTime.text.toString().trim()
                val selectedPriority = when (spinnerPriority.selectedItem.toString()) {
                    "High" -> Priority.High
                    "Medium" -> Priority.Medium
                    "Low" -> Priority.Low
                    else -> Priority.Medium
                }

                if (title.isEmpty()) {
                    Toast.makeText(this@MainActivity, "❌ Task title is required", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (dueDate.isEmpty()) {
                    Toast.makeText(this@MainActivity, "❌ Due date is required", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Create the task
                createDetailedTask(
                    title = title,
                    description = if (description.isEmpty()) null else description,
                    dueDate = dueDate,
                    dueTime = if (dueTime.isEmpty()) null else dueTime,
                    priority = selectedPriority
                )

                dialog.dismiss()
            }
        }

        dialog.show()
    }

    // ✅ DATE PICKER HELPER
    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()

        // Parse current date if exists
        if (editText.text.isNotEmpty()) {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val currentDate = dateFormat.parse(editText.text.toString())
                if (currentDate != null) {
                    calendar.time = currentDate
                }
            } catch (e: Exception) {
                // Use current date if parsing fails
            }
        }

        val datePickerDialog = android.app.DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                editText.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set minimum date to today
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    // ✅ TIME PICKER HELPER
    private fun showTimePicker(editText: EditText) {
        val calendar = Calendar.getInstance()

        // Parse current time if exists
        if (editText.text.isNotEmpty()) {
            try {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val currentTime = timeFormat.parse(editText.text.toString())
                if (currentTime != null) {
                    calendar.time = currentTime
                }
            } catch (e: Exception) {
                // Use current time if parsing fails
            }
        }

        val timePickerDialog = android.app.TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val selectedTime = Calendar.getInstance()
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedTime.set(Calendar.MINUTE, minute)
                editText.setText(timeFormat.format(selectedTime.time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // 24-hour format
        )

        timePickerDialog.show()
    }

    // ✅ CREATE DETAILED TASK WITH ALL FIELDS
    private fun createDetailedTask(
        title: String,
        description: String?,
        dueDate: String,
        dueTime: String?,
        priority: Priority
    ) {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)

                val taskId: Int = withContext(Dispatchers.IO) {
                    db.taskDao().getNextTaskIdForUser(userId)
                }

                val newTask = Task.create(
                    userId = userId,
                    taskId = taskId,
                    title = title,
                    description = description,
                    priority = priority,
                    dueAt = dueDate,
                    dueTime = dueTime,
                    status = Status.Pending
                )

                withContext(Dispatchers.IO) {
                    db.taskDao().insert(newTask)
                }

                // Refresh dashboard data
                loadTaskStatistics()
                loadRecentTasks()

                val timeText = if (dueTime != null) " at $dueTime" else ""
                Toast.makeText(
                    this@MainActivity,
                    "✅ Task '$title' created for $dueDate$timeText!",
                    Toast.LENGTH_LONG
                ).show()

                android.util.Log.d("MainActivity", "✅ Created detailed task: $title")

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error creating detailed task", e)
                Toast.makeText(this@MainActivity, "❌ Error creating task", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✅ DASHBOARD INFO DIALOGS (READ-ONLY)
    private fun showDetailedStatistics() {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)

                val actualStats: Map<String, Int> = withContext(Dispatchers.IO) {
                    val today = Calendar.getInstance()
                    val todayStr = dateToString(today.time)

                    val totalTasks = db.taskDao().getAllTasksCount(userId)
                    val completedTasks = db.taskDao().getCompletedTasksCount(userId)
                    val todayTasks = db.taskDao().getTodayTasksCount(userId, todayStr)
                    // ✅ FIXED: Use the correct method signature
                    val overdueTasks = db.taskDao().getOverdueTasksCount(userId)
                    val pendingTasks = db.taskDao().getPendingTasksCount(userId)
                    val successRate = if (totalTasks > 0) (completedTasks * 100) / totalTasks else 0

                    mapOf(
                        "total" to totalTasks,
                        "completed" to completedTasks,
                        "today" to todayTasks,
                        "overdue" to overdueTasks,
                        "pending" to pendingTasks,
                        "successRate" to successRate
                    )
                }

                val message = """
            📊 Your Task Statistics (${dateToString(Date())})
            
            📋 Total Tasks: ${actualStats["total"]}
            ✅ Completed: ${actualStats["completed"]} 
            ⏰ Due Today: ${actualStats["today"]}
            🔴 Overdue: ${actualStats["overdue"]}
            ⏳ Pending: ${actualStats["pending"]}
            
            📈 Success Rate: ${actualStats["successRate"]}%
            
            ${if (actualStats["total"] == 0) "Start creating tasks to track your progress!" else "Keep up the great work! 💪"}
            """.trimIndent()

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Task Statistics")
                    .setMessage(message)
                    .setPositiveButton("Manage Tasks") { _, _ -> navigateToTasks() }
                    .setNegativeButton("Close", null)
                    .show()

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error showing detailed statistics", e)
                Toast.makeText(this@MainActivity, "Error loading statistics", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showProgressDetails() {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)

                val actualProgress: Map<String, Int> = withContext(Dispatchers.IO) {
                    val today = Calendar.getInstance()
                    val todayStr = dateToString(today.time)

                    val totalTasksToday = db.taskDao().getTodayTasksCount(userId, todayStr)
                    val completedToday = db.taskDao().getCompletedTodayTasksCount(userId, todayStr)
                    val remainingToday = totalTasksToday - completedToday
                    val progressPercent = if (totalTasksToday > 0) (completedToday * 100) / totalTasksToday else 0

                    mapOf(
                        "goal" to totalTasksToday,
                        "completed" to completedToday,
                        "remaining" to remainingToday,
                        "percent" to progressPercent
                    )
                }

                val message = """
            📈 Today's Progress (${dateToString(Date())})
            
            🎯 Today's Goal: Complete ${actualProgress["goal"]} tasks
            ✅ Completed: ${actualProgress["completed"]} tasks
            ⏳ Remaining: ${actualProgress["remaining"]} tasks
            
            📊 Progress: ${actualProgress["percent"]}%
            
            ${if (actualProgress["goal"] == 0) "Create tasks for today to track your progress!" else "Keep going! 💪"}
            """.trimIndent()

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Daily Progress")
                    .setMessage(message)
                    .setPositiveButton("Add Tasks") { _, _ -> navigateToTasks() }
                    .setNegativeButton("Close", null)
                    .show()

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error showing progress details", e)
            }
        }
    }

    private fun showMotivationalMessage() {
        try {
            uiScope.launch {
                try {
                    val db = AppDb.get(this@MainActivity)

                    val personality: Personality? = withContext(Dispatchers.IO) {
                        db.personalityDao().getByUser(userId)
                    }

                    val motivationalMessages = when (personality?.Name?.lowercase()) {
                        "kalog" -> arrayOf(
                            "🤣 Time to turn that task list into a comedy show!",
                            "😄 Let's tackle these tasks with a smile and a laugh!",
                            "🎭 Make productivity fun - you've got this!",
                            "😆 Turn your to-do list into a ta-da list!"
                        )
                        "gen z" -> arrayOf(
                            "💯 No cap, you're about to absolutely slay these tasks!",
                            "🔥 It's giving main character energy - let's go!",
                            "✨ Periodt! Time to show these tasks who's boss!",
                            "💅 About to serve some serious productivity looks!"
                        )
                        "softy" -> arrayOf(
                            "🌸 You've got this, take it one gentle step at a time",
                            "💕 Be kind to yourself while you accomplish amazing things",
                            "🌺 Small progress is still progress - you're doing great!",
                            "🤗 Sending you gentle motivation and warm encouragement!"
                        )
                        "grey" -> arrayOf(
                            "⚖️ Steady progress leads to lasting success",
                            "🧘 Focus on what matters, let go of what doesn't",
                            "📚 Wisdom comes from consistent, thoughtful action",
                            "🎯 Balance effort with patience - you're on the right path"
                        )
                        "flirty" -> arrayOf(
                            "😉 Hey gorgeous, ready to charm those tasks into submission?",
                            "💋 You're about to make productivity look effortlessly sexy",
                            "😘 Wink at your goals and watch them fall for you!",
                            "🌹 Turn on that irresistible charm and conquer your day!"
                        )
                        else -> arrayOf(
                            "🌟 You have everything it takes to succeed!",
                            "💪 Believe in yourself - you're stronger than you know!",
                            "🚀 Ready to launch into an amazing day of achievement!",
                            "✨ Your potential is limitless - let's unlock it together!"
                        )
                    }

                    val randomMessage = motivationalMessages.random()
                    Toast.makeText(this@MainActivity, randomMessage, Toast.LENGTH_LONG).show()

                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "💪 You've got this! Let's make today amazing!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "💪 Stay motivated and keep pushing forward!", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ DASHBOARD DATA LOADING - FULLY IMPLEMENTED
    private fun loadUserData() {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)

                val user = withContext(Dispatchers.IO) {
                    db.userDao().getById(userId)
                }

                val personality: Personality? = withContext(Dispatchers.IO) {
                    db.personalityDao().getByUser(userId)
                }

                val tvUsername = findViewById<TextView>(R.id.tvUsername)

                if (user != null) {
                    val displayName = when {
                        !user.FirstName.isNullOrBlank() && !user.LastName.isNullOrBlank() ->
                            "${user.FirstName} ${user.LastName}"
                        !user.FirstName.isNullOrBlank() -> user.FirstName
                        else -> user.Username
                    }

                    tvUsername?.text = displayName
                } else {
                    tvUsername?.text = "User"
                }

                val title = findViewById<TextView>(R.id.personalityTitle)
                val desc = findViewById<TextView>(R.id.personalityDesc)

                if (personality != null) {
                    title?.text = "${personality.Name} Vibes"
                    desc?.text = personality.Description
                } else {
                    title?.text = "Your Personality"
                    desc?.text = "No personality selected yet."
                }

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error loading user data", e)
                findViewById<TextView>(R.id.tvUsername)?.text = "User"
                Toast.makeText(this@MainActivity, "Error loading user data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadTaskStatistics() {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)

                val stats: Map<String, Int> = withContext(Dispatchers.IO) {
                    val totalTasks = db.taskDao().getAllTasksCount(userId)
                    val completedTasks = db.taskDao().getCompletedTasksCount(userId)

                    val today = Calendar.getInstance()
                    val todayStr = dateToString(today.time)

                    val todayTasks = db.taskDao().getTodayTasksCount(userId, todayStr)
                    // ✅ FIXED: Use the correct method signature
                    val overdueTasks = db.taskDao().getOverdueTasksCount(userId)

                    android.util.Log.d("MainActivity", "✅ Dashboard statistics - Today: $todayStr")
                    android.util.Log.d("MainActivity", "✅ Today tasks: $todayTasks, Overdue: $overdueTasks")

                    mapOf(
                        "total" to totalTasks,
                        "completed" to completedTasks,
                        "today" to todayTasks,
                        "overdue" to overdueTasks
                    )
                }

                updateStatisticsDisplay(stats)
                updateProgressDisplay(stats["completed"] ?: 0, stats["total"] ?: 0)

                android.util.Log.d("MainActivity", "✅ Dashboard statistics loaded")

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error loading dashboard statistics", e)
                val fallbackStats: Map<String, Int> = mapOf("total" to 0, "completed" to 0, "today" to 0, "overdue" to 0)
                updateStatisticsDisplay(fallbackStats)
            }
        }
    }

    private fun updateStatisticsDisplay(stats: Map<String, Int>) {
        try {
            updateGridStatistics(stats)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error updating statistics display", e)
        }
    }

    private fun updateGridStatistics(stats: Map<String, Int>) {
        try {
            val cardStats = findViewById<LinearLayout>(R.id.cardStats)
            val gridLayout = cardStats?.getChildAt(0) as? GridLayout

            if (gridLayout != null) {
                for (i in 0 until gridLayout.childCount) {
                    val cellLayout = gridLayout.getChildAt(i) as? LinearLayout
                    if (cellLayout != null && cellLayout.childCount >= 2) {
                        val valueTextView = cellLayout.getChildAt(0) as? TextView
                        val labelTextView = cellLayout.getChildAt(1) as? TextView

                        when (labelTextView?.text?.toString()?.lowercase()) {
                            "total" -> valueTextView?.text = stats["total"].toString()
                            "done" -> valueTextView?.text = stats["completed"].toString()
                            "today" -> valueTextView?.text = stats["today"].toString()
                            "overdue" -> valueTextView?.text = stats["overdue"].toString()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error updating grid statistics", e)
        }
    }

    private fun updateProgressDisplay(completed: Int, total: Int) {
        try {
            val progressSubtitle = findViewById<TextView>(R.id.progressSubtitle)
            val progressPercent = findViewById<TextView>(R.id.progressPercent)
            val progressFill = findViewById<View>(R.id.progressFill)

            val percentage = if (total > 0) (completed * 100) / total else 0

            progressSubtitle?.text = "$completed of $total tasks completed"
            progressPercent?.text = "$percentage%"

            progressFill?.layoutParams?.let { params ->
                if (params is LinearLayout.LayoutParams) {
                    params.weight = percentage.coerceAtLeast(5).toFloat()
                    progressFill.layoutParams = params
                }
            }

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error updating progress display", e)
        }
    }

    // ✅ LOAD ALL RECENT TASKS (NOT JUST 3) WITH LIVE UPDATES
    private fun loadRecentTasks() {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)
                val recentTasks: List<Task> = withContext(Dispatchers.IO) {
                    // Get ALL pending and overdue tasks, ordered by due date and priority
                    db.taskDao().getPendingTasks(userId) // Show ALL active tasks, not just 3
                }

                updateRecentTasksDisplay(recentTasks)

                android.util.Log.d("MainActivity", "✅ Loaded ${recentTasks.size} recent tasks for dashboard")

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error loading recent tasks", e)
                updateRecentTasksDisplay(emptyList<Task>())
            }
        }
    }

    // ✅ ENHANCED RECENT TASKS DISPLAY - SHOW ALL TASKS WITH LIVE STATUS
    private fun updateRecentTasksDisplay(tasks: List<Task>) {
        try {
            val recentTasksContainer = findViewById<LinearLayout>(R.id.cardRecent)

            if (recentTasksContainer != null && recentTasksContainer.childCount > 1) {
                val contentArea = recentTasksContainer.getChildAt(1) as? LinearLayout
                contentArea?.removeAllViews()

                if (tasks.isEmpty()) {
                    val emptyText = TextView(this)
                    emptyText.text = "🎉 No pending tasks!\nTap + to create your first task!"
                    emptyText.textSize = 14f
                    emptyText.setTextColor(resources.getColor(android.R.color.white))
                    emptyText.gravity = android.view.Gravity.CENTER
                    emptyText.setPadding(20, 30, 20, 30)
                    contentArea?.addView(emptyText)
                } else {
                    // ✅ DECLARE ALL TASK GROUPS PROPERLY
                    val overdueTasks = tasks.filter { task ->
                        task.Status == com.example.cheermateapp.data.model.Status.Pending &&
                                isTaskOverdue(task, System.currentTimeMillis())
                    }

                    // ✅ ADDED: Declare pendingTasks variable
                    val pendingTasks = tasks.filter { task ->
                        task.Status == com.example.cheermateapp.data.model.Status.Pending &&
                                !isTaskOverdue(task, System.currentTimeMillis())
                    }

                    // ✅ ADDED: Also get in-progress and completed tasks
                    val inProgressTasks = tasks.filter { task ->
                        task.Status == com.example.cheermateapp.data.model.Status.InProgress
                    }

                    val completedTasks = tasks.filter { task ->
                        task.Status == com.example.cheermateapp.data.model.Status.Completed
                    }

                    // Show overdue tasks first
                    if (overdueTasks.isNotEmpty()) {
                        val overdueHeader = TextView(this)
                        overdueHeader.text = "🔴 OVERDUE TASKS (${overdueTasks.size})"
                        overdueHeader.textSize = 12f
                        overdueHeader.setTextColor(android.graphics.Color.RED)
                        overdueHeader.setTypeface(null, android.graphics.Typeface.BOLD)
                        overdueHeader.setPadding(8, 8, 8, 8)
                        contentArea?.addView(overdueHeader)

                        overdueTasks.forEach { task ->
                            createTaskCard(task, contentArea)
                        }
                    }

                    // ✅ FIXED: Now pendingTasks is properly declared
                    if (pendingTasks.isNotEmpty()) {
                        val pendingHeader = TextView(this)
                        pendingHeader.text = "⏳ PENDING TASKS (${pendingTasks.size})"
                        pendingHeader.textSize = 12f
                        pendingHeader.setTextColor(android.graphics.Color.YELLOW)
                        pendingHeader.setTypeface(null, android.graphics.Typeface.BOLD)
                        pendingHeader.setPadding(8, 16, 8, 8)
                        contentArea?.addView(pendingHeader)

                        pendingTasks.forEach { task ->
                            createTaskCard(task, contentArea)
                        }
                    }

                    // ✅ ADDED: Show in-progress tasks
                    if (inProgressTasks.isNotEmpty()) {
                        val inProgressHeader = TextView(this)
                        inProgressHeader.text = "🔄 IN PROGRESS (${inProgressTasks.size})"
                        inProgressHeader.textSize = 12f
                        inProgressHeader.setTextColor(android.graphics.Color.CYAN)
                        inProgressHeader.setTypeface(null, android.graphics.Typeface.BOLD)
                        inProgressHeader.setPadding(8, 16, 8, 8)
                        contentArea?.addView(inProgressHeader)

                        inProgressTasks.forEach { task ->
                            createTaskCard(task, contentArea)
                        }
                    }

                    // ✅ ADDED: Show completed tasks (recent ones)
                    if (completedTasks.isNotEmpty() && completedTasks.size <= 3) {
                        val completedHeader = TextView(this)
                        completedHeader.text = "✅ RECENTLY COMPLETED (${completedTasks.size})"
                        completedHeader.textSize = 12f
                        completedHeader.setTextColor(android.graphics.Color.GREEN)
                        completedHeader.setTypeface(null, android.graphics.Typeface.BOLD)
                        completedHeader.setPadding(8, 16, 8, 8)
                        contentArea?.addView(completedHeader)

                        completedTasks.take(3).forEach { task ->
                            createTaskCard(task, contentArea)
                        }
                    }

                    // ✅ ENHANCED: Add summary stats
                    if (tasks.isNotEmpty()) {
                        val summaryText = TextView(this)
                        val totalTasks = tasks.size
                        val completedCount = completedTasks.size
                        val progressPercent = if (totalTasks > 0) (completedCount * 100) / totalTasks else 0

                        summaryText.text = "📊 Progress: $completedCount/$totalTasks tasks ($progressPercent%)"
                        summaryText.textSize = 11f
                        summaryText.setTextColor(android.graphics.Color.LTGRAY)
                        summaryText.gravity = android.view.Gravity.CENTER
                        summaryText.setPadding(8, 8, 8, 8)
                        contentArea?.addView(summaryText)
                    }

                    // Add action buttons
                    val buttonLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(0, 16, 0, 0)
                    }

                    val viewAllButton = Button(this).apply {
                        text = "📋 Manage All"
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                            setMargins(0, 0, 8, 0)
                        }
                        setOnClickListener { navigateToTasks() }
                    }

                    val addTaskButton = Button(this).apply {
                        text = "➕ Add Task"
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                            setMargins(8, 0, 0, 0)
                        }
                        setOnClickListener { showQuickAddTaskDialog() }
                    }

                    buttonLayout.addView(viewAllButton)
                    buttonLayout.addView(addTaskButton)
                    contentArea?.addView(buttonLayout)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error updating recent tasks display", e)
            Toast.makeText(this, "Error displaying tasks", Toast.LENGTH_SHORT).show()
        }
    }


    // ✅ FIXED - Declare topRow before using it
    private fun createTaskCard(task: Task, container: LinearLayout?) {
        try {
            val cardView = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 12, 16, 12)
                background = createRoundedDrawable(android.graphics.Color.parseColor("#2A2A3A"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 12)
                }
            }

            // ✅ DECLARE topRow here
            val topRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            // ✅ Now you can use topRow (line 1653 equivalent)
            val statusEmoji = TextView(this).apply {
                text = task.getStatusEmoji()
                textSize = 16f
                setPadding(0, 0, 12, 0)
            }
            topRow.addView(statusEmoji)

            val titleText = TextView(this).apply {
                text = task.Title
                textSize = 16f
                setTextColor(android.graphics.Color.WHITE)
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            topRow.addView(titleText)

            val priorityChip = TextView(this).apply {
                text = task.Priority.toString()
                textSize = 10f
                setTextColor(android.graphics.Color.WHITE)
                setPadding(8, 4, 8, 4)
                background = createRoundedDrawable(task.getPriorityColor())
            }
            topRow.addView(priorityChip)

            // Add topRow to cardView
            cardView.addView(topRow)

            // Add other elements to cardView...
            if (!task.Description.isNullOrBlank()) {
                val descriptionText = TextView(this).apply {
                    text = task.Description
                    textSize = 14f
                    setTextColor(resources.getColor(android.R.color.darker_gray))
                    setPadding(0, 8, 0, 0)
                }
                cardView.addView(descriptionText)
            }

            val dueDateText = TextView(this).apply {
                text = "📅 ${task.getFormattedDueDateTime()}"
                textSize = 12f
                setTextColor(resources.getColor(android.R.color.darker_gray))
                setPadding(0, 8, 0, 0)
            }
            cardView.addView(dueDateText)

            // Add click listener
            cardView.setOnClickListener {
                showTaskDetailsDialog(task)
            }

            container?.addView(cardView)

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error creating task card", e)
        }
    }


    // ✅ TASK DETAILS DIALOG
    private fun showTaskDetailsDialog(task: Task) {
        val message = buildString {
            append("📝 Title: ${task.Title}\n")
            if (!task.Description.isNullOrBlank()) {
                append("📄 Description: ${task.Description}\n")
            }
            append("🎯 Priority: ${task.Priority}\n")
            append("📊 Status: ${task.Status}\n")
            append("📅 Due Date: ${task.DueAt ?: "Not set"}\n")
            if (!task.DueTime.isNullOrBlank()) {
                append("⏰ Due Time: ${task.DueTime}\n")
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Task Details")
            .setMessage(message)
            .setPositiveButton("Mark as Done") { _, _ ->
                markTaskAsDone(task)
            }
            .setNeutralButton("Edit") { _, _ ->
                navigateToTasks()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    // ✅ TASK QUICK ACTIONS
    private fun showTaskQuickActions(task: Task) {
        val actions = arrayOf(
            "✅ Mark as Done",
            "🔄 Mark as Pending",
            "🗑️ Delete Task",
            "✏️ Edit Task"
        )

        AlertDialog.Builder(this)
            .setTitle("Quick Actions: ${task.Title}")
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> markTaskAsDone(task)
                    1 -> markTaskAsPending(task)
                    2 -> deleteTask(task)
                    3 -> navigateToTasks()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ✅ TASK ACTION METHODS - KEEP ONLY THESE VERSIONS
    private fun markTaskAsDone(task: Task) {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)
                withContext(Dispatchers.IO) {
                    db.taskDao().updateTaskStatus(
                        task.User_ID,
                        task.Task_ID,
                        "Completed"
                    )
                    // Also update progress to 100%
                    db.taskDao().updateTaskProgress(task.User_ID, task.Task_ID, 100)
                }

                Toast.makeText(this@MainActivity, "✅ Task '${task.Title}' marked as done!", Toast.LENGTH_SHORT).show()
                loadTaskStatistics()
                loadRecentTasks()

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error marking task as done", e)
                Toast.makeText(this@MainActivity, "❌ Error updating task", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performDeleteTask(task: Task) {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)
                withContext(Dispatchers.IO) {
                    // Use soft delete instead of hard delete
                    db.taskDao().softDelete(task.User_ID, task.Task_ID)
                }

                Toast.makeText(this@MainActivity, "🗑️ Task '${task.Title}' deleted!", Toast.LENGTH_SHORT).show()
                loadTaskStatistics()
                loadRecentTasks()

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error deleting task", e)
                Toast.makeText(this@MainActivity, "❌ Error deleting task", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✅ ADDITIONAL HELPER METHODS
    private fun markTaskAsInProgress(task: Task) {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)
                withContext(Dispatchers.IO) {
                    db.taskDao().updateTaskStatus(
                        task.User_ID,
                        task.Task_ID,
                        "InProgress"
                    )
                }

                Toast.makeText(this@MainActivity, "🔄 Task '${task.Title}' is in progress!", Toast.LENGTH_SHORT).show()
                loadTaskStatistics()
                loadRecentTasks()

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error marking task as in progress", e)
                Toast.makeText(this@MainActivity, "❌ Error updating task", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // ✅ USE THIS CORRECT VERSION INSTEAD
    private fun markTaskAsPending(task: Task) {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)
                withContext(Dispatchers.IO) {
                    // ✅ FIXED: Use database DAO method instead of direct assignment
                    db.taskDao().updateTaskStatus(
                        task.User_ID,
                        task.Task_ID,
                        "Pending"
                    )
                }

                Toast.makeText(this@MainActivity, "⏳ Task '${task.Title}' marked as pending!", Toast.LENGTH_SHORT).show()
                loadTaskStatistics()
                loadRecentTasks()

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error marking task as pending", e)
                Toast.makeText(this@MainActivity, "❌ Error updating task", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun deleteTask(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete '${task.Title}'?")
            .setPositiveButton("Delete") { _, _ ->
                uiScope.launch {
                    try {
                        val db = AppDb.get(this@MainActivity)
                        withContext(Dispatchers.IO) {
                            db.taskDao().delete(task)
                        }

                        Toast.makeText(this@MainActivity, "🗑️ Task deleted!", Toast.LENGTH_SHORT).show()
                        loadTaskStatistics()
                        loadRecentTasks()

                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "❌ Error deleting task", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ✅ CALENDAR INTEGRATION (READ-ONLY PREVIEW) - FIXED VERSION
    private fun setupCalendarView() {
        android.util.Log.d("MainActivity", "🔧 setupCalendarView() called")
        try {
            val calendarPlaceholder = findViewById<LinearLayout>(R.id.calendarPlaceholder)
            android.util.Log.d("MainActivity", "📅 calendarPlaceholder found: ${calendarPlaceholder != null}")

            if (calendarPlaceholder == null) {
                android.util.Log.e("MainActivity", "❌ calendarPlaceholder is NULL!")
                return
            }

            // ✅ CLEAR ALL EXISTING VIEWS (INCLUDING THE DEFAULT TextView)
            calendarPlaceholder.removeAllViews()

            android.util.Log.d("MainActivity", "✅ Cleared existing calendar content")

            // ✅ CREATE AND CONFIGURE CalendarView
            val calendarView = CalendarView(this)
            calendarView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            // ✅ CONFIGURE CALENDAR SETTINGS
            calendarView.firstDayOfWeek = Calendar.MONDAY
            calendarView.date = System.currentTimeMillis()

            try {
                // ✅ STYLING (OPTIONAL)
                calendarView.setBackgroundColor(android.graphics.Color.TRANSPARENT)

                // ✅ DATE SELECTION LISTENER
                calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
                    showTasksForDate(year, month, dayOfMonth)
                    android.util.Log.d("MainActivity", "📅 Date selected: $dayOfMonth/${month + 1}/$year")
                }

                android.util.Log.d("MainActivity", "✅ Calendar styling and listener applied")

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "⚠️ Error applying calendar styling", e)
            }

            // ✅ ADD CALENDAR TO CONTAINER
            calendarPlaceholder.addView(calendarView)

            android.util.Log.d("MainActivity", "✅ Calendar added to placeholder")

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "❌ Error setting up calendar", e)
            setupFallbackCalendar()
        }
    }

    private fun showTasksForDate(year: Int, month: Int, day: Int) {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)
                val tasksForDate: List<Task> = withContext(Dispatchers.IO) {
                    val calendar = Calendar.getInstance()
                    calendar.set(year, month, day)
                    val dateStr = dateToString(calendar.time)

                    db.taskDao().getTodayTasks(userId, dateStr)
                }

                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day)
                val formattedDate = dateFormat.format(calendar.time)

                if (tasksForDate.isNotEmpty()) {
                    val taskTitles = tasksForDate.take(3).joinToString("\n") { "• ${it.Title}" }
                    val message = if (tasksForDate.size > 3) {
                        "$taskTitles\n...and ${tasksForDate.size - 3} more tasks"
                    } else {
                        taskTitles
                    }

                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Tasks for $formattedDate")
                        .setMessage(message)
                        .setPositiveButton("View All") { _, _ -> navigateToTasks() }
                        .setNegativeButton("Close", null)
                        .show()
                } else {
                    Toast.makeText(this@MainActivity, "No tasks for $formattedDate", Toast.LENGTH_SHORT).show()
                }

                android.util.Log.d("MainActivity", "Loaded ${tasksForDate.size} tasks for: $formattedDate")

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error loading tasks for date", e)
            }
        }
    }

    private fun setupFallbackCalendar() {
        try {
            android.util.Log.d("MainActivity", "⚠️ Setting up fallback calendar")

            val calendarPlaceholder = findViewById<LinearLayout>(R.id.calendarPlaceholder)
            calendarPlaceholder?.removeAllViews()

            val fallbackText = TextView(this)
            fallbackText.text = "📅 Calendar View\nTap dates to see tasks"
            fallbackText.gravity = android.view.Gravity.CENTER
            fallbackText.setPadding(20, 40, 20, 40)
            fallbackText.setTextColor(resources.getColor(android.R.color.white))
            fallbackText.textSize = 16f

            calendarPlaceholder?.addView(fallbackText)

            android.util.Log.d("MainActivity", "✅ Fallback calendar setup complete")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "❌ Error setting up fallback calendar", e)
        }
    }

    private fun createFallbackLayout() {
        try {
            val layout = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(50, 50, 50, 50)
            }

            val welcomeText = TextView(this).apply {
                text = "🎉 CheerMate Dashboard 🎉"
                textSize = 24f
                setPadding(0, 0, 0, 30)
            }

            val userText = TextView(this).apply {
                text = "Welcome! Dashboard is loading...\nUser ID: $userId"
                textSize = 18f
                setPadding(0, 0, 0, 20)
            }

            val actionButton = Button(this).apply {
                text = "Go to Tasks"
                setOnClickListener { navigateToTasks() }
            }

            layout.addView(welcomeText)
            layout.addView(userText)
            layout.addView(actionButton)
            setContentView(layout)

            Toast.makeText(this, "Dashboard loaded in fallback mode", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Fallback layout failed", e)
            Toast.makeText(this, "Critical error - redirecting to login", Toast.LENGTH_LONG).show()

            try {
                startActivity(Intent(this, ActivityLogin::class.java))
                finish()
            } catch (ex: Exception) {
                android.util.Log.e("MainActivity", "Failed to redirect to login", ex)
            }
        }
    }

    // ✅ HELPER METHODS
    private fun dateToString(date: Date): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(date)
    }

    private fun createRoundedDrawable(color: Int): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            setColor(color)
            cornerRadius = 12f
        }
    }

    // ✅ LIFECYCLE METHODS
    override fun onResume() {
        super.onResume()
        // Restart live updates when app becomes visible
        startLiveTaskUpdates()
    }

    override fun onPause() {
        super.onPause()
        // Pause live updates when app goes to background
        stopLiveTaskUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop live updates when activity is destroyed
        stopLiveTaskUpdates()
    }
}
