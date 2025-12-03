package com.cheermateapp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.DatePicker // Added
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.cheermateapp.ActivityLogin
import com.cheermateapp.FragmentTaskExtensionActivity
import com.cheermateapp.TaskAdapter
import com.cheermateapp.TaskPagerAdapter
import com.cheermateapp.data.db.AppDb
import com.cheermateapp.data.model.Category
import com.cheermateapp.data.model.Personality
import com.cheermateapp.data.model.Priority
import com.cheermateapp.data.model.Status
import com.cheermateapp.data.model.Task
import com.cheermateapp.data.model.TaskReminder
import com.cheermateapp.data.model.User
import com.cheermateapp.data.model.getDisplayText
import com.cheermateapp.ui.tasks.TasksViewModel
import com.cheermateapp.ui.tasks.TasksViewModelFactory
import com.cheermateapp.util.InputValidationUtil
import com.cheermateapp.util.PasswordHashUtil
import com.cheermateapp.util.ThemeManager
import com.cheermateapp.util.ToastManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
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
    private lateinit var tasksViewModel: TasksViewModel

    private lateinit var progressCompleted: View
    private lateinit var progressInProgress: View
    private lateinit var progressSubtitle: TextView
    private lateinit var progressPercent: TextView

    private lateinit var tabAll: TextView
    private lateinit var tabToday: TextView
    private lateinit var tabPending: TextView
    private lateinit var tabDone: TextView
    private lateinit var tvTasksSub: TextView


    private var isTasksScreenInitialized: Boolean = false
    private var isSettingsScreenInitialized: Boolean = false
    private var isHomeInteractionSetup: Boolean = false

    // Cached roots for embedded "fragments" to avoid re-inflation
    private var tasksRootView: View? = null
    private var settingsRootView: View? = null

    // Container under CalendarView for selected-day tasks
    private var calendarTasksContainer: LinearLayout? = null
    private var selectedCalendarDateStr: String? = null
    private var calendarTasksObserverJob: kotlinx.coroutines.Job? = null
    private var lastCalendarEventMs: Long = 0L
    private var calendarDebounceJob: kotlinx.coroutines.Job? = null

    // Lightweight cache for calendar summaries to reduce DB/UI churn
    private data class CalendarSummary(val count: Int, val highest: Priority?)
    private val calendarSummaryCache = java.util.concurrent.ConcurrentHashMap<String, CalendarSummary>()

    // Reusable UI for calendar summary row
    private var calendarRow: LinearLayout? = null
    private var calendarRowIcon: TextView? = null
    private var calendarRowInfo: TextView? = null

    // Concurrency guards for fast navigation
    private var calendarUpdateJob: kotlinx.coroutines.Job? = null
    private var calendarUpdateSeq: Int = 0

    // ✅ RecyclerView and TaskAdapter for fragment_tasks
    private var taskRecyclerView: RecyclerView? = null
    private var taskAdapter: TaskAdapter? = null

    // ✅ Cached reference to FAB for better performance
    private val fabAddTask: FloatingActionButton? by lazy {
        findViewById(R.id.fabAddTask)
    }

    // ✅ Helper method to control FAB visibility
    private fun setFabVisibility(visible: Boolean) {
        fabAddTask?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    // ✅ Activity Result Launcher for task details
    private val taskDetailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // When returning from task detail activity, the UI will update reactively.
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.cheermateapp.util.ThemeManager.initializeTheme(this)

        try {
            val showDashboard = intent?.getBooleanExtra(EXTRA_SHOW_DASHBOARD, false) == true
            userId = intent?.getStringExtra(EXTRA_USER_ID)?.toIntOrNull() ?: 0

            if (showDashboard && userId != 0) {
                setContentView(R.layout.activity_main)

                val db = AppDb.get(this)
                val viewModelFactory = TasksViewModelFactory(db, userId)
                tasksViewModel = ViewModelProvider(this, viewModelFactory)[TasksViewModel::class.java]

                // Initialize views that relate to the progress bar
                progressCompleted = findViewById(R.id.progressCompleted)
                progressInProgress = findViewById(R.id.progressInProgress)
                progressSubtitle = findViewById(R.id.progressSubtitle)
                progressPercent = findViewById(R.id.progressPercent)

                setupToolbar()
                setupGreeting()
                setupBottomNavigation()

                // Set FAB icon tint to white for good contrast
                findViewById<FloatingActionButton>(R.id.fabAddTask)?.imageTintList =
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE)

                // Restore Settings tab immediately after nav setup if theme toggle caused recreation
                kotlin.runCatching {
                    val prefs = getSharedPreferences("cheermate_theme_prefs", MODE_PRIVATE)
                    if (prefs.getBoolean("return_to_settings", false)) {
                        android.util.Log.d("MainActivity", "Restoring Settings after theme toggle (early)")
                        findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)?.selectedItemId = R.id.nav_settings
                        navigateToSettings()
                        prefs.edit().putBoolean("return_to_settings", false).putBoolean("theme_applying", false).putBoolean("ignore_next_toggle", false).apply()
                    } else {
                        // Clear applying flag if set
                        if (prefs.getBoolean("theme_applying", false)) {
                            prefs.edit().putBoolean("theme_applying", false).putBoolean("ignore_next_toggle", false).apply()
                        }
                    }
                }

                // Only initialize Home UI and data if Home tab is selected to prevent flicker on Settings
                findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)?.let { nav ->
                    if (nav.selectedItemId == R.id.nav_home) {
                        // Load dashboard data
                        loadUserData()
                        loadTaskStatistics()
                        observeRecentTasks()
                        showHomeScreen()
                    }
                }

                // If theme was toggled from Settings, restore Settings fragment after recreation
                kotlin.runCatching {
                    val prefs = getSharedPreferences("cheermate_theme_prefs", MODE_PRIVATE)
                    if (prefs.getBoolean("return_to_settings", false)) {
                        findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)?.selectedItemId = R.id.nav_settings
                        navigateToSettings()
                        prefs.edit().putBoolean("return_to_settings", false).apply()
                    }
                }

                // Observe DB changes for live daily progress bar updates only on Home
                findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)?.let { nav ->
                    if (nav.selectedItemId == R.id.nav_home) {
                        startObserveTaskChangesForProgressBar()
                    }
                }

            } else {
                // Redirect to login if not properly authenticated
                startActivity(Intent(this, ActivityLogin::class.java))
                finish()
            }

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Critical error in onCreate", e)
            ToastManager.showToast(this, "Loading dashboard...", Toast.LENGTH_SHORT)
            finish()
        }
    }

    // ✅ OBSERVE RECENT TASKS FOR LIVE UPDATES
    private fun observeRecentTasks() {
        lifecycleScope.launch {
            // This now observes the shared ViewModel
            tasksViewModel.tasks.collectLatest { tasks ->
                updateRecentTasksDisplay(tasks)
                android.util.Log.d("MainActivity", "🔄 Recent tasks updated live: ${tasks.size} tasks")
            }
        }
    }

    // ✅ This method is now obsolete for tasks, but might be used for other things.
    private fun refreshCurrentView() {
        // Obsolete for tasks as it's reactive now.
    }

    // ✅ SIMPLIFIED VERSION - WITHOUT OVERDUE STATUS
    private fun updateOverdueTasks() {
        // This logic could be moved to a repository or use case
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
            
            val parsedDate: Date? = try {
                // Try parsing the primary format first
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dueDate)
            } catch (e: java.text.ParseException) {
                try {
                    // If it fails, try parsing the alternative format
                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).parse(dueDate)
                } catch (e2: java.text.ParseException) {
                    // If both fail, log the error and return false
                    android.util.Log.e("isTaskOverdue", "Could not parse date: '$dueDate'", e2)
                    null
                }
            }

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
            com.cheermateapp.data.model.Priority.High -> android.graphics.Color.RED
            com.cheermateapp.data.model.Priority.Medium -> android.graphics.Color.parseColor("#FFA500") // Orange
            com.cheermateapp.data.model.Priority.Low -> android.graphics.Color.GREEN
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

            // Preserve or restore tab selection; default to Home only if not returning to Settings
            val prefs = getSharedPreferences("cheermate_theme_prefs", MODE_PRIVATE)
            val returnToSettings = prefs.getBoolean("return_to_settings", false)
            if (returnToSettings) {
                bottomNav?.selectedItemId = R.id.nav_settings
                prefs.edit().putBoolean("return_to_settings", false).apply()
            } else {
                bottomNav?.selectedItemId = R.id.nav_home
            }

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error setting up bottom navigation", e)
        }
    }

    private fun showHomeScreen() {
        try {
            findViewById<ScrollView>(R.id.homeScroll)?.visibility = View.VISIBLE
            val container = findViewById<FrameLayout>(R.id.contentContainer)
            container?.visibility = View.GONE

            if (!isHomeInteractionSetup) {
                setupHomeScreenInteractions()
                isHomeInteractionSetup = true
            }
            
            // Show FAB on home screen
            setFabVisibility(true)
            // Ensure FAB is clickable and above other views
            findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddTask)?.apply {
                isEnabled = true
                isClickable = true
                bringToFront()
            }
            // Refresh home statistics, recent tasks, and progress when showing Home
            loadTaskStatistics()
            loadRecentTasks()
            startObserveTaskChangesForProgressBar()
            
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
            container?.visibility = View.VISIBLE

            // Reuse cached Tasks root view to avoid re-inflation and flicker
            if (tasksRootView == null) {
                tasksRootView = LayoutInflater.from(this).inflate(R.layout.fragment_tasks, container, false)
                container?.removeAllViews()
                container?.addView(tasksRootView)
                setupTasksFragment()
                isTasksScreenInitialized = true
                android.util.Log.d("MainActivity", "✅ Tasks Fragment inflated and initialized")
            } else {
                // If the cached view is not currently attached, attach it without re-initializing
                if (container?.childCount == 0 || container.getChildAt(0) !== tasksRootView) {
                    container?.removeAllViews()
                    container?.addView(tasksRootView)
                    android.util.Log.d("MainActivity", "✅ Tasks Fragment reused from cache")
                } else {
                    android.util.Log.d("MainActivity", "ℹ️ Tasks Fragment already visible, no re-attach")
                }
            }

            isSettingsScreenInitialized = false

            // Show FAB on tasks screen
            setFabVisibility(true)

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error loading tasks fragment", e)
            ToastManager.showToast(this, "Error loading tasks", Toast.LENGTH_SHORT)
            showHomeScreen() // Fallback to home
        }
    }

    private fun navigateToSettings() {
        try {
            // Hide home screen
            findViewById<ScrollView>(R.id.homeScroll)?.visibility = View.GONE

            // Show content container
            val container = findViewById<FrameLayout>(R.id.contentContainer)
            container?.visibility = View.VISIBLE

            // Reuse cached Settings root view to avoid re-inflation and flicker
            if (settingsRootView == null) {
                settingsRootView = LayoutInflater.from(this).inflate(R.layout.fragment_settings, container, false)
                container?.removeAllViews()
                container?.addView(settingsRootView)
                setupSettingsFragment()
                isSettingsScreenInitialized = true
                android.util.Log.d("MainActivity", "✅ Settings Fragment inflated and initialized")
            } else {
                // If the cached view is not currently attached, attach it without re-initializing
                if (container?.childCount == 0 || container.getChildAt(0) !== settingsRootView) {
                    container?.removeAllViews()
                    container?.addView(settingsRootView)
                    android.util.Log.d("MainActivity", "✅ Settings Fragment reused from cache")
                } else {
                    android.util.Log.d("MainActivity", "ℹ️ Settings Fragment already visible, no re-attach")
                }
            }

            isTasksScreenInitialized = false

            // Hide FAB on settings screen
            setFabVisibility(false)

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error loading settings fragment", e)
            ToastManager.showToast(this, "Error loading settings", Toast.LENGTH_SHORT)
            showHomeScreen() // Fallback to home
        }
    }

    // ✅ FRAGMENT SETUP METHODS - MAKE FRAGMENTS FUNCTIONAL
    private fun setupTasksFragment() {
        try {
            // ✅ Setup FAB for adding tasks (in fragment view)
            findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddTask)?.apply {
                isEnabled = true
                isClickable = true
                bringToFront()
                setOnClickListener { showQuickAddTaskDialog() }
            }

            // Initialize all task fragment components
            findViewById<TextView>(R.id.tvTasksTitle)
            tvTasksSub = checkNotNull(findViewById(R.id.tvTasksSub)) { "TextView with id tvTasksSub not found" }
            val etSearch = findViewById<EditText>(R.id.etSearch)
            val btnSort = findViewById<TextView>(R.id.btnSort)
            val chipFound = findViewById<TextView>(R.id.chipFound)
            tabAll = checkNotNull(findViewById(R.id.tabAll)) { "TextView with id tabAll not found" }
            tabToday = checkNotNull(findViewById(R.id.tabToday)) { "TextView with id tabToday not found" }
            tabPending = checkNotNull(findViewById(R.id.tabPending)) { "TextView with id tabPending not found" }
            tabDone = checkNotNull(findViewById(R.id.tabDone)) { "TextView with id tabDone not found" }
            val tvEmptyState = findViewById<TextView>(R.id.tvEmptyState)
            
            // ✅ Initialize RecyclerView and TaskAdapter
            taskRecyclerView = findViewById<RecyclerView>(R.id.recyclerViewTasks)
            taskRecyclerView?.layoutManager = LinearLayoutManager(this)
            
            taskAdapter = TaskAdapter(
                tasks = mutableListOf(),
                onTaskClick = { task -> onTaskClick(task) },
                onTaskComplete = { task -> onTaskDone(task) },
                onTaskEdit = { task -> onTaskEdit(task) },
                onTaskDelete = { task -> onTaskDelete(task) }
            )
            taskRecyclerView?.adapter = taskAdapter

            btnSort?.setOnClickListener {
                showSortOptionsDialog()
            }

            // Search functionality
            etSearch?.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    // tasksViewModel.setSearchQuery(s.toString())
                }
            })

            // Filter tabs
            tabAll?.setOnClickListener {
                tasksViewModel.setTaskFilter("ALL")
                updateTaskTabSelection(tabAll)
            }

            tabToday?.setOnClickListener {
                tasksViewModel.setTaskFilter("TODAY")
                updateTaskTabSelection(tabToday)
            }

            tabPending?.setOnClickListener {
                tasksViewModel.setTaskFilter("PENDING")
                updateTaskTabSelection(tabPending)
            }

            tabDone?.setOnClickListener {
                tasksViewModel.setTaskFilter("DONE")
                updateTaskTabSelection(tabDone)
            }

            observeTasks()

            android.util.Log.d("MainActivity", "✅ Tasks fragment initialized with RecyclerView")

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error setting up tasks fragment", e)
            ToastManager.showToast(this, "Error loading tasks", Toast.LENGTH_SHORT)
        }
    }

    private fun observeTasks() {
        lifecycleScope.launch {
            tasksViewModel.tasks.collectLatest { tasks ->
                taskAdapter?.updateTasks(tasks)
                findViewById<TextView>(R.id.chipFound)?.text = "${tasks.size} found"

                val tvEmptyState = findViewById<TextView>(R.id.tvEmptyState)
                if (tasks.isEmpty()) {
                    taskRecyclerView?.visibility = View.GONE
                    tvEmptyState?.visibility = View.VISIBLE
                    tvEmptyState?.text = when (tasksViewModel.taskFilter.value) {
                        "TODAY" -> "No tasks for today\n\nTap the + button to create your first task"
                        "PENDING" -> "No pending tasks\n\nAll caught up!"
                        "DONE" -> "No completed tasks yet\n\nComplete a task to see it here"
                        else -> "No tasks available\n\nTap the + button to create your first task"
                    }
                } else {
                    taskRecyclerView?.visibility = View.VISIBLE
                    tvEmptyState?.visibility = View.GONE
                }
            }
        }
        
        lifecycleScope.launch {
            tasksViewModel.allTasksCount.collectLatest {
                tabAll.text = "All ($it)"
                tvTasksSub.text = "$it total tasks"
            }
        }
        lifecycleScope.launch {
            tasksViewModel.todayTasksCount.collectLatest {
                tabToday.text = "Today ($it)"
            }
        }
        lifecycleScope.launch {
            tasksViewModel.pendingTasksCount.collectLatest {
                tabPending.text = "Pending ($it)"
            }
        }
        lifecycleScope.launch {
            tasksViewModel.completedTasksCount.collectLatest {
                tabDone.text = "Done ($it)"
            }
        }
    }
    
    private fun updateTaskTabSelection(selectedTab: TextView) {
        try {
            // Reset all tabs
            val tabs = listOf(findViewById<TextView>(R.id.tabAll), findViewById<TextView>(R.id.tabToday), findViewById<TextView>(R.id.tabPending), findViewById<TextView>(R.id.tabDone))
            tabs.forEach {
                tab ->
                tab?.apply {
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


    private fun setupSettingsFragment() {
        try {
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

            // Get current dark mode state and set the switch
            switchDarkMode?.isChecked = com.cheermateapp.util.ThemeManager.isDarkModeActive(this)

            switchDarkMode?.setOnCheckedChangeListener { _, isChecked ->
                val prefs = getSharedPreferences("cheermate_theme_prefs", MODE_PRIVATE)
                // Ignore the next toggle triggered by programmatic state restore after recreate
                if (prefs.getBoolean("ignore_next_toggle", false)) {
                    prefs.edit().putBoolean("ignore_next_toggle", false).apply()
                    return@setOnCheckedChangeListener
                }
                val newMode = if (isChecked) {
                    com.cheermateapp.util.ThemeManager.THEME_DARK
                } else {
                    com.cheermateapp.util.ThemeManager.THEME_SYSTEM
                }
                // Skip if a theme apply is already in progress
                if (prefs.getBoolean("theme_applying", false)) return@setOnCheckedChangeListener
                val currentMode = com.cheermateapp.util.ThemeManager.getThemeMode(this)
                val lastChangeTs = prefs.getLong("theme_change_ts", 0L)
                val now = System.currentTimeMillis()
                // Debounce rapid toggles and skip if mode unchanged
                if (newMode == currentMode || (now - lastChangeTs) < 1000L) {
                    return@setOnCheckedChangeListener
                }
                // Mark to return to Settings after activity recreation due to theme change
                prefs.edit()
                    .putBoolean("return_to_settings", true)
                    .putLong("theme_change_ts", now)
                    .putBoolean("theme_applying", true)
                    .putBoolean("ignore_next_toggle", true)
                    .apply()
                com.cheermateapp.util.ThemeManager.setThemeMode(this, newMode)
                // Persist to UserSettings.Appearance
                lifecycleScope.launch {
                    try {
                        val db = com.cheermateapp.data.db.AppDb.get(this@MainActivity)
                        val current = withContext(kotlinx.coroutines.Dispatchers.IO) { db.userSettingsDao().getSettingsByUser(userId) }
                        val appearance = com.cheermateapp.data.model.Appearance(theme = if (isChecked) "dark" else "light")
                        val newSettings = if (current != null) {
                            current.copy(Appearance = appearance)
                        } else {
                            com.cheermateapp.data.model.UserSettings(User_ID = userId, Appearance = appearance)
                        }
                        withContext(kotlinx.coroutines.Dispatchers.IO) { db.userSettingsDao().upsert(newSettings) }
                    } catch (_: Exception) {}
                }
                // Single controlled recreate to apply theme
                recreate()
            }

            switchNotifications?.setOnCheckedChangeListener { _, isChecked ->
                ToastManager.showToast(
                    this,
                    if (isChecked) "🔔 Notifications enabled" else "🔕 Notifications disabled",
                    Toast.LENGTH_SHORT
                )
            }

            cardSignOut?.setOnClickListener {
                showLogoutConfirmation()
            }

            // Load settings data
            loadSettingsFragmentData()

            android.util.Log.d("MainActivity", "✅ Settings fragment initialized")

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error setting up settings fragment", e)
            ToastManager.showToast(this, "Error loading settings", Toast.LENGTH_SHORT)
        }
    }

    // ✅ TASK ACTION HANDLERS FOR RECYCLERVIEW
    private fun onTaskClick(task: Task) {
        // Navigate to FragmentTaskExtensionActivity to show full task details
        val intent = Intent(this, FragmentTaskExtensionActivity::class.java)
        intent.putExtra(FragmentTaskExtensionActivity.EXTRA_TASK_ID, task.Task_ID)
        intent.putExtra(FragmentTaskExtensionActivity.EXTRA_USER_ID, task.User_ID)
        taskDetailLauncher.launch(intent) // Use launcher instead of startActivity
    }

    private fun onTaskDone(task: Task) {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)
                val updatedTask = task.copy(
                    Status = Status.Completed,
                    TaskProgress = 100
                )
                withContext(Dispatchers.IO) {
                    db.taskDao().update(updatedTask)
                }
                ToastManager.showToast(this@MainActivity, "✅ Task marked as done!", Toast.LENGTH_SHORT)
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error marking task as done", e)
                ToastManager.showToast(this@MainActivity, "Failed to update task", Toast.LENGTH_SHORT)
            }
        }
    }

    private fun onTaskEdit(task: Task) {
        // Show edit dialog with task details
        showEditTaskDialog(task)
    }
    
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
            progressLabel.text = "Progress: ${task.TaskProgress} %"
            progressLabel.setPadding(0, 16, 0, 8)
            container.addView(progressLabel)

            val progressSeekBar = SeekBar(this)
            progressSeekBar.max = 100
            progressSeekBar.progress = task.TaskProgress
            progressSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    progressLabel.text = "Progress: $progress %"
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

            val dateInput = EditText(this)
            dateInput.hint = "YYYY-MM-DD"
            dateInput.setText(task.DueAt ?: "")
            dateInput.setPadding(16, 16, 16, 16)
            dateInput.isFocusable = false
            dateInput.isClickable = true
            dateInput.setOnClickListener { showDatePicker(dateInput) }
            container.addView(dateInput)

            // Time Selection (pre-filled)
            val timeLabel = TextView(this)
            timeLabel.text = "Due Time:"
            timeLabel.setPadding(0, 16, 0, 8)
            container.addView(timeLabel)

            val timeInput = EditText(this)
            timeInput.hint = "HH:MM"
            timeInput.setText(task.DueTime ?: "")
            timeInput.setPadding(16, 16, 16, 16)
            timeInput.isFocusable = false
            timeInput.isClickable = true
            timeInput.setOnClickListener { showTimePicker(timeInput) }
            container.addView(timeInput)

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
                } else {
                    val priority = prioritySpinner.selectedItem.toString()
                    val status = statusSpinner.selectedItem.toString()
                    val description = descriptionInput.text.toString().trim()
                    val progress = progressSeekBar.progress
                    val dueDate = dateInput.text.toString()
                    val dueTime = timeInput.text.toString()

                    updateTask(task, title, description, priority, status, progress, dueDate, dueTime)
                    dialog.dismiss()
                }
            }

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error showing edit task dialog", e)
            ToastManager.showToast(this, "❌ Edit task dialog error", Toast.LENGTH_SHORT)
        }
    }
    
    private fun updateTask(
        originalTask: Task,
        title: String,
        description: String,
        priority: String,
        status: String,
        progress: Int,
        dueDate: String,
        dueTime: String
    ) {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)

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
                    DueAt = if (dueDate.isNotBlank()) dueDate else null,
                    DueTime = if (dueTime.isNotBlank()) dueTime else null,
                    UpdatedAt = System.currentTimeMillis()
                )

                withContext(Dispatchers.IO) {
                    db.taskDao().update(updatedTask)
                }
                
                ToastManager.showToast(
                    this@MainActivity,
                    "✅ Task '$title' updated successfully!",
                    Toast.LENGTH_LONG
                )

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error updating task", e)
                ToastManager.showToast(this@MainActivity, "❌ Update Error: ${e.message}", Toast.LENGTH_SHORT)
            }
        }
    }

    private fun onTaskDelete(task: Task) {
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
                        ToastManager.showToast(this@MainActivity, "🗑️ Task deleted", Toast.LENGTH_SHORT)
                        
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Error deleting task", e)
                        ToastManager.showToast(this@MainActivity, "Failed to delete task", Toast.LENGTH_SHORT)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
        val currentTasks = taskAdapter?.tasks ?: return
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
                            com.cheermateapp.data.model.Status.Pending -> 1
                            com.cheermateapp.data.model.Status.InProgress -> 2
                            com.cheermateapp.data.model.Status.OverDue -> 3
                            com.cheermateapp.data.model.Status.Completed -> 4
                            com.cheermateapp.data.model.Status.Cancelled -> 5
                        }
                        val status2 = when (task2.Status) {
                            com.cheermateapp.data.model.Status.Pending -> 1
                            com.cheermateapp.data.model.Status.InProgress -> 2
                            com.cheermateapp.data.model.Status.OverDue -> 3
                            com.cheermateapp.data.model.Status.Completed -> 4
                            com.cheermateapp.data.model.Status.Cancelled -> 5
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
            taskAdapter?.updateTasks(sortedTasks)

            val sortNames = arrayOf("Due Date", "Priority", "Title", "Status", "Progress")
            val sortName = if (sortType in sortNames.indices) sortNames[sortType] else "Unknown"
            ToastManager.showToast(this, "📊 Sorted by $sortName", Toast.LENGTH_SHORT)

        } catch (e: Exception) {
            android.util.Log.e("FragmentTaskActivity", "Error sorting tasks", e)
            ToastManager.showToast(this, "Error sorting tasks", Toast.LENGTH_SHORT)
        }
    }


    // ✅ FIXED SETTINGS FRAGMENT DATA METHODS
    // Cached settings data so we don't re-fetch or flicker when navigating
    private data class SettingsCache(
        val profileName: String,
        val profileEmail: String,
        val personaName: String?,
        val chipPersonaText: String?,
        val statTotal: Int,
        val statCompleted: Int,
        val statSuccess: Int
    )

    private var settingsCache: SettingsCache? = null

    private fun isThemeApplying(): Boolean {
        return getSharedPreferences("cheermate_theme_prefs", MODE_PRIVATE)
            .getBoolean("theme_applying", false)
    }

    private fun applySettingsCacheToViews(cache: SettingsCache) {
        // Avoid flicker during theme toggle
        if (isThemeApplying()) return
        findViewById<TextView>(R.id.tvProfileName)?.text = cache.profileName
        findViewById<TextView>(R.id.tvProfileEmail)?.text = cache.profileEmail
        findViewById<TextView>(R.id.tvCurrentPersona)?.text = cache.personaName ?: ""
        findViewById<TextView>(R.id.chipPersona)?.text = cache.chipPersonaText ?: "Your Personality"
        findViewById<TextView>(R.id.tvStatTotal)?.text = cache.statTotal.toString()
        findViewById<TextView>(R.id.tvStatCompleted)?.text = cache.statCompleted.toString()
        findViewById<TextView>(R.id.tvStatSuccess)?.text = "${cache.statSuccess}%"
    }

    private fun loadSettingsFragmentData(forceRefresh: Boolean = false) {
        uiScope.launch {
            try {
                // If we already have up-to-date values and we're not forcing a refresh,
                // just re-bind the existing cache to the (re)inflated views.
                val cached = settingsCache
                if (cached != null && !forceRefresh) {
                    applySettingsCacheToViews(cached)
                    android.util.Log.d("MainActivity", "Using cached settings data (no DB fetch)")
                    return@launch
                }

                val db = AppDb.get(this@MainActivity)

                val user = withContext(Dispatchers.IO) {
                    db.userDao().getById(userId)
                }

                val personality: Personality? = withContext(Dispatchers.IO) {
                    db.personalityDao().getByUser(userId)
                }

                val stats: Map<String, Int> = withContext(Dispatchers.IO) {
                    val totalTasks = db.taskDao().getAllTasksCount(userId)
                    val completedTasks = db.taskDao().getCompletedTasksCount(userId)

                    mapOf(
                        "total" to totalTasks,
                        "completed" to completedTasks
                    )
                }

                // Build display values
                val profileName: String
                val profileEmail: String
                if (user != null) {
                    profileName = when {
                        !user.FirstName.isNullOrBlank() && !user.LastName.isNullOrBlank() ->
                            "${user.FirstName} ${user.LastName}"
                        !user.FirstName.isNullOrBlank() -> user.FirstName
                        else -> user.Username
                    }
                    profileEmail = user.Email ?: "user@example.com"
                } else {
                    profileName = "User"
                    profileEmail = "user@example.com"
                }

                val personaName = personality?.Name
                val chipPersonaText = personality?.let { "${it.Name} Personality" }

                val successRate = if (stats["total"]!! > 0) {
                    (stats["completed"]!! * 100) / stats["total"]!!
                } else 0

                val newCache = SettingsCache(
                    profileName = profileName,
                    profileEmail = profileEmail,
                    personaName = personaName,
                    chipPersonaText = chipPersonaText,
                    statTotal = stats["total"] ?: 0,
                    statCompleted = stats["completed"] ?: 0,
                    statSuccess = successRate
                )

                settingsCache = newCache
                // Avoid flicker during theme toggle
                if (!isThemeApplying()) {
                    applySettingsCacheToViews(newCache)
                }

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
                    db.userDao().getById(userId)
                }

                if (user == null) {
                    ToastManager.showToast(this@MainActivity, "Error loading user profile", Toast.LENGTH_SHORT)
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

                val dialog = androidx.appcompat.app.AlertDialog.Builder(this@MainActivity)
                    .setTitle("Edit Profile")
                    .setView(dialogView)
                    .setPositiveButton("Save", null) // Set to null. We'll override the listener later.
                    .setNeutralButton("Change Password") { _, _ ->
                        showChangePasswordDialog(user)
                    }
                    .setNegativeButton("Cancel", null)
                    .create()

                dialog.setOnShowListener {
                    val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    positiveButton.setOnClickListener {
                        val newUsername = etEditUsername.text?.toString()?.trim().orEmpty()
                        val newEmail = etEditEmail.text?.toString()?.trim().orEmpty()
                        val newFirstName = etEditFirstName.text?.toString()?.trim().orEmpty()
                        val newLastName = etEditLastName.text?.toString()?.trim().orEmpty()

                        // Validate inputs
                        if (newUsername.isEmpty()) {
                            etEditUsername.error = "Username cannot be empty"
                            return@setOnClickListener
                        }

                        if (!com.cheermateapp.util.InputValidationUtil.isValidUsername(newUsername)) {
                            etEditUsername.error = "Username must be 3-20 characters, letters, numbers, and underscore only"
                            return@setOnClickListener
                        }
                        
                        if (newEmail.isEmpty()) {
                            etEditEmail.error = "Email cannot be empty"
                            return@setOnClickListener
                        }

                        if (!com.cheermateapp.util.InputValidationUtil.isValidEmail(newEmail)) {
                            etEditEmail.error = "Invalid email format"
                            return@setOnClickListener
                        }
                        
                        if (newFirstName.isEmpty()) {
                            etEditFirstName.error = "First name cannot be empty"
                            return@setOnClickListener
                        }

                        if (newLastName.isEmpty()) {
                            etEditLastName.error = "Last name cannot be empty"
                            return@setOnClickListener
                        }

                        // Save changes
                        saveProfileChanges(user, newUsername, newEmail, newFirstName, newLastName)
                        dialog.dismiss() // Dismiss dialog only when validation passes
                    }
                }
                
                dialog.show()

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error showing profile edit dialog", e)
                ToastManager.showToast(this@MainActivity, "Error loading profile", Toast.LENGTH_SHORT)
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
        if (newFirstName.isBlank() || newLastName.isBlank()) {
            ToastManager.showToast(this, "First and last name cannot be empty", Toast.LENGTH_SHORT)
            return
        }

        uiScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val db = AppDb.get(this@MainActivity)

                    // Check if new username already exists (if changed)
                    if (newUsername != user.Username) {
                        val existingUser = db.userDao().findByUsername(newUsername)
                        if (existingUser != null && existingUser.User_ID != user.User_ID) {
                            withContext(Dispatchers.Main) {
                                ToastManager.showToast(
                                    this@MainActivity,
                                    "Username already exists",
                                    Toast.LENGTH_SHORT
                                )
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

                ToastManager.showToast(this@MainActivity, "Profile updated successfully", Toast.LENGTH_SHORT)
                
                // Reload settings fragment to show updated info (force DB refresh)
                loadSettingsFragmentData(forceRefresh = true)
                loadUserData()

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error saving profile changes", e)
                ToastManager.showToast(this@MainActivity, "Error updating profile: ${e.message}", Toast.LENGTH_LONG)
            }
        }
    }

    /**
     * Show dialog to change password
     */
    private fun showChangePasswordDialog(user: User) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etCurrentPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etCurrentPassword)
        val tilCurrentPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilCurrentPassword)
        val etNewPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNewPassword)
        val tilNewPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilNewPassword)
        val etConfirmPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etConfirmPassword)
        val tilConfirmPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilConfirmPassword)

        // Set initial state for transformation methods (password hidden)
        etCurrentPassword.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
        etNewPassword.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
        etConfirmPassword.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()

        // Hide the end icon initially
        tilCurrentPassword.isEndIconVisible = false
        tilNewPassword.isEndIconVisible = false
        tilConfirmPassword.isEndIconVisible = false

        // Handle end icon visibility based on text presence
        etCurrentPassword.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                tilCurrentPassword.isEndIconVisible = s?.isNotEmpty() == true
            }
        })

        tilCurrentPassword.setEndIconOnClickListener {
            if (etCurrentPassword.transformationMethod == null) {
                // Currently visible, hide it and set icon to off
                etCurrentPassword.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
                tilCurrentPassword.setEndIconDrawable(R.drawable.ic_visibility_off)
            } else {
                // Currently hidden, show it and set icon to on
                etCurrentPassword.transformationMethod = null
                tilCurrentPassword.setEndIconDrawable(R.drawable.ic_visibility_on)
            }
            // Move cursor to end
            etCurrentPassword.setSelection(etCurrentPassword.text?.length ?: 0)
        }

        etNewPassword.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                tilNewPassword.isEndIconVisible = s?.isNotEmpty() == true
            }
        })

        tilNewPassword.setEndIconOnClickListener {
            if (etNewPassword.transformationMethod == null) {
                etNewPassword.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
                tilNewPassword.setEndIconDrawable(R.drawable.ic_visibility_off)
            } else {
                etNewPassword.transformationMethod = null
                tilNewPassword.setEndIconDrawable(R.drawable.ic_visibility_on)
            }
            etNewPassword.setSelection(etNewPassword.text?.length ?: 0)
        }

        etConfirmPassword.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                tilConfirmPassword.isEndIconVisible = s?.isNotEmpty() == true
            }
        })

        tilConfirmPassword.setEndIconOnClickListener {
            if (etConfirmPassword.transformationMethod == null) {
                etConfirmPassword.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
                tilConfirmPassword.setEndIconDrawable(R.drawable.ic_visibility_off)
            } else {
                etConfirmPassword.transformationMethod = null
                tilConfirmPassword.setEndIconDrawable(R.drawable.ic_visibility_on)
            }
            etConfirmPassword.setSelection(etConfirmPassword.text?.length ?: 0)
        }


        val dialog = androidx.appcompat.app.AlertDialog.Builder(this@MainActivity)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val currentPassword = etCurrentPassword.text?.toString()?.trim().orEmpty()
                val newPassword = etNewPassword.text?.toString()?.trim().orEmpty()
                val confirmPassword = etConfirmPassword.text?.toString()?.trim().orEmpty()

                // Validate inputs
                if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    ToastManager.showToast(this@MainActivity, "Please fill in all fields", Toast.LENGTH_SHORT)
                    return@setOnClickListener
                }

                if (newPassword != confirmPassword) {
                    ToastManager.showToast(this@MainActivity, "New passwords do not match", Toast.LENGTH_SHORT)
                    return@setOnClickListener
                }

                if (!com.cheermateapp.util.InputValidationUtil.isValidPassword(newPassword)) {
                    ToastManager.showToast(this@MainActivity, "Password must be at least 6 characters", Toast.LENGTH_SHORT)
                    return@setOnClickListener
                }

                // Change password
                changePassword(user, currentPassword, newPassword)
                dialog.dismiss()
            }
        }
        dialog.show()
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
                    val isCurrentPasswordValid = com.cheermateapp.util.PasswordHashUtil.verifyPassword(
                        currentPassword,
                        user.PasswordHash
                    )

                    if (!isCurrentPasswordValid) {
                        return@withContext false
                    }

                    // Hash new password
                    val newHashedPassword = com.cheermateapp.util.PasswordHashUtil.hashPassword(newPassword)

                    // Update user
                    val updatedUser = user.copy(
                        PasswordHash = newHashedPassword,
                        UpdatedAt = System.currentTimeMillis()
                    )
                    db.userDao().update(updatedUser)
                    
                    return@withContext true
                }

                if (success) {
                    ToastManager.showToast(this@MainActivity, "Password changed successfully", Toast.LENGTH_SHORT)
                } else {
                    ToastManager.showToast(this@MainActivity, "Current password is incorrect", Toast.LENGTH_SHORT)
                }

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error changing password", e)
                ToastManager.showToast(this@MainActivity, "Error changing password: ${e.message}", Toast.LENGTH_LONG)
            }
        }
    }

    private fun showPersonalitySelectionDialog() {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)
                
                // Fetch personalities from database
                val availablePersonalities = withContext(Dispatchers.IO) {
                    db.personalityDao().getAllActive()
                }
                
                if (availablePersonalities.isEmpty()) {
                    ToastManager.showToast(this@MainActivity, "No personalities available", Toast.LENGTH_SHORT)
                    return@launch
                }
                
                // Get current user's personality ID
                val currentUser = withContext(Dispatchers.IO) {
                    db.userDao().getById(userId)
                }

                val personalityNames = availablePersonalities.map { it.Name }.toTypedArray()
                
                // Find the index of the current personality
                val checkedItem = if (currentUser?.Personality_ID != null) {
                    availablePersonalities.indexOfFirst { it.Personality_ID == currentUser.Personality_ID }
                } else {
                    -1  // No selection
                }
                
                // Track the selected personality
                var selectedPersonalityIndex: Int = checkedItem

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Choose Your Personality")
                    .setSingleChoiceItems(personalityNames, checkedItem) { _, which ->
                        selectedPersonalityIndex = which
                    }
                    .setPositiveButton("OK") { _, _ ->
                        if (selectedPersonalityIndex >= 0) {
                            val selected = availablePersonalities[selectedPersonalityIndex]
                            updateUserPersonality(selected.Personality_ID)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()

            } catch (e: Exception) {
                ToastManager.showToast(this@MainActivity, "Error loading personalities", Toast.LENGTH_SHORT)
            }
        }
    }

    private fun updateUserPersonality(personalityId: Int) {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)
                val personalityName = withContext(Dispatchers.IO) {
                    // Update User.Personality_ID to link the user to the personality
                    db.userDao().updatePersonality(userId, personalityId)
                    
                    // Get personality name for confirmation message
                    db.personalityDao().getById(personalityId)?.Name ?: "Unknown"
                }

                ToastManager.showToast(this@MainActivity, "✅ Personality updated to $personalityName!", Toast.LENGTH_SHORT)
                
                // ✅ Update both settings fragment AND home screen personality card
                loadSettingsFragmentData(forceRefresh = true)
                loadUserData() // This updates the personality card on home screen

            } catch (e: Exception) {
                ToastManager.showToast(this@MainActivity, "Error updating personality", Toast.LENGTH_SHORT)
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
            // ✅ Setup FAB for adding tasks
            findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddTask)?.apply {
                isEnabled = true
                isClickable = true
                bringToFront()
                setOnClickListener { showQuickAddTaskDialog() }
            }

            // Calendar date selection -> show tasks under calendar
            findViewById<CalendarView>(R.id.calendarView)?.let { cv ->
                ensureCalendarTasksContainer()
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val initialDateStr = sdf.format(java.util.Date(cv.date))
                android.util.Log.d("CalendarDebug", "Initial date=" + initialDateStr)
                selectedCalendarDateStr = initialDateStr
                updateCalendarSelectedTasks(initialDateStr)
                startObserveCalendarSelectedTasks(initialDateStr)
                cv.setOnDateChangeListener { _, year, month, dayOfMonth ->
                    val cal = java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.YEAR, year)
                        set(java.util.Calendar.MONTH, month)
                        set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }
                    val newDateStr = sdf.format(cal.time)
                    if (newDateStr == selectedCalendarDateStr) {
                        return@setOnDateChangeListener
                    }
                    android.util.Log.d("CalendarDebug", "Date changed to " + newDateStr)
                    selectedCalendarDateStr = newDateStr
                    updateCalendarSelectedTasks(newDateStr)
                    startObserveCalendarSelectedTasks(newDateStr)
                }
            }

            findViewById<View>(R.id.cardCalendar)?.setOnClickListener {
                ToastManager.showToast(this, "📅 Select a date to see tasks!", Toast.LENGTH_SHORT)
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

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error setting up home interactions", e)
        }
    }

    private fun ensureCalendarTasksContainer() {
        try {
            if (calendarTasksContainer == null) {
                val cardCalendar = findViewById<LinearLayout>(R.id.cardCalendar) ?: return
                val index = (0 until cardCalendar.childCount).firstOrNull { i ->
                    cardCalendar.getChildAt(i) is CalendarView
                } ?: return
                val container = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(20, 8, 20, 8)
                    gravity = android.view.Gravity.CENTER_HORIZONTAL
                }
                cardCalendar.addView(container, index + 1)
                calendarTasksContainer = container
            }
            if (calendarRow == null) {
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    lp.setMargins(0, (6 * resources.displayMetrics.density).toInt(), 0, 0)
                    layoutParams = lp
                    setPadding(0, (4 * resources.displayMetrics.density).toInt(), 0, 0)
                    gravity = android.view.Gravity.CENTER_VERTICAL
                }
                val icon = TextView(this).apply {
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    lp.rightMargin = (10 * resources.displayMetrics.density).toInt()
                    layoutParams = lp
                    textSize = 14f
                    visibility = View.GONE
                }
                val info = TextView(this).apply {
                    setTextColor(android.graphics.Color.WHITE)
                    setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12f)
                    setAllCaps(true)
                }
                row.addView(icon)
                row.addView(info)
                calendarTasksContainer?.addView(row)
                calendarRow = row
                calendarRowIcon = icon
                calendarRowInfo = info
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to add calendar tasks container", e)
        }
    }

    private fun summarize(tasks: List<Task>): CalendarSummary {
        if (tasks.isEmpty()) return CalendarSummary(0, null)
        val highest = when {
            tasks.any { it.Priority == Priority.High } -> Priority.High
            tasks.any { it.Priority == Priority.Medium } -> Priority.Medium
            else -> Priority.Low
        }
        return CalendarSummary(tasks.size, highest)
    }

    private fun updateCalendarRow(summary: CalendarSummary) {
        ensureCalendarTasksContainer()
        val icon = calendarRowIcon ?: return
        val info = calendarRowInfo ?: return
        when (summary.highest) {
            Priority.High -> { icon.text = "🔴"; icon.visibility = View.VISIBLE }
            Priority.Medium -> { icon.text = "🟡"; icon.visibility = View.VISIBLE }
            Priority.Low -> { icon.text = "🟢"; icon.visibility = View.VISIBLE }
            null -> { icon.text = ""; icon.visibility = View.GONE }
        }
        info.text = if (summary.count == 0) "no tasks in this date" else "${summary.count} task(s) - tap to view"
        calendarRow?.setOnClickListener(
            if (summary.count > 0) View.OnClickListener {
                findViewById<BottomNavigationView>(R.id.bottomNav)?.selectedItemId = R.id.nav_tasks
            } else null
        )
    }

    private fun updateCalendarSelectedTasks(dateStr: String) {
        calendarUpdateJob?.cancel()
        val seq = ++calendarUpdateSeq
        calendarUpdateJob = uiScope.launch {
            try {
                android.util.Log.d("CalendarDebug", "updateCalendarSelectedTasks start date=" + dateStr)
                ensureCalendarTasksContainer()

                // Always reset immediately to avoid any stale flash; render real data after fetch/flow
                android.util.Log.d("CalendarDebug", "FastNav immediate reset for " + dateStr)
                updateCalendarRow(CalendarSummary(0, null))

                // Refresh in background
                val db = AppDb.get(this@MainActivity)
                val tasksForDate = withContext(Dispatchers.IO) {
                    db.taskDao().getTodayTasks(userId, dateStr)
                }
                // Skip outdated results if user navigated away
                if (seq != calendarUpdateSeq || selectedCalendarDateStr != dateStr) {
                    android.util.Log.d("CalendarDebug", "FastNav discard outdated fetch for " + dateStr + " seq=" + seq + " currentSeq=" + calendarUpdateSeq + " selected=" + selectedCalendarDateStr)
                    return@launch
                }

                android.util.Log.d("CalendarDebug", "Tasks fetched for " + dateStr + ": " + tasksForDate.size)
                val newSummary = summarize(tasksForDate)
                calendarSummaryCache[dateStr] = newSummary
                updateCalendarRow(newSummary)
            } catch (e: Exception) {
                // Ignore cancellation noise
                if (e !is kotlinx.coroutines.CancellationException) {
                    android.util.Log.e("MainActivity", "Error updating calendar selected tasks", e)
                }
            }
        }
    }

    private fun startObserveCalendarSelectedTasks(dateStr: String) {
        calendarTasksObserverJob?.cancel()
        // Always reset immediately to avoid any stale flash; render real data via flow
        android.util.Log.d("CalendarDebug", "FastNav observe immediate reset for " + dateStr)
        updateCalendarRow(CalendarSummary(0, null))
        calendarTasksObserverJob = lifecycleScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)
                db.taskDao().getTodayTasksFlow(userId, dateStr).collect { tasks ->
                    if (selectedCalendarDateStr != dateStr) return@collect
                    val summary = summarize(tasks)
                    calendarSummaryCache[dateStr] = summary
                    updateCalendarRow(summary)
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Expected when switching dates; ignore
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error observing calendar tasks", e)
            }
        }
    }

    private fun stopObserveCalendarSelectedTasks() {
        calendarTasksObserverJob?.cancel()
        calendarTasksObserverJob = null
    }

    // ✅ ENHANCED ADD TASK DIALOG (NOW USING XML LAYOUT!)
    private fun showQuickAddTaskDialog() {
        android.util.Log.d("DIALOG_DEBUG", "===== MAIN ACTIVITY DIALOG CALLED =====")
        android.util.Log.d("DIALOG_DEBUG", "Method: showQuickAddTaskDialog() in MainActivity")
        android.util.Log.d("DIALOG_DEBUG", "NOW USING XML LAYOUT: dialog_add_task.xml")
        android.util.Log.d("DIALOG_DEBUG", "This will match the designer preview!")
        
        try {
            // Inflate the enhanced XML layout
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
            android.util.Log.d("DIALOG_DEBUG", "XML layout inflated successfully")
            
            // Get references to the XML components
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
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            etDueDate.setText(dateFormat.format(calendar.time))
            
            // Set up date and time pickers
            etDueDate.setOnClickListener { showDatePicker(etDueDate) }
            etDueTime.setOnClickListener { showTimePicker(etDueTime) }
            
            // Get references to buttons inside the custom layout
            val btnCreateTask = dialogView.findViewById<Button>(R.id.btnCreateTask)
            val btnCancelTask = dialogView.findViewById<Button>(R.id.btnCancelTask)

            // Create and show the dialog
            android.util.Log.d("DIALOG_DEBUG", "Creating AlertDialog with XML layout")
            val builder = AlertDialog.Builder(this)
            builder.setView(dialogView)
            // We no longer set positive/negative buttons here, as they are inside the layout.

            val dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)
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
                
                // Get selected values from spinners and convert to proper types
                val categoryString = com.cheermateapp.util.TaskDialogSpinnerHelper.getSelectedCategory(spinnerCategory)
                val priorityString = com.cheermateapp.util.TaskDialogSpinnerHelper.getSelectedPriority(spinnerPriority)
                
                val category = when (categoryString) {
                    "Work" -> com.cheermateapp.data.model.Category.Work
                    "Personal" -> com.cheermateapp.data.model.Category.Personal
                    "Shopping" -> com.cheermateapp.data.model.Category.Shopping
                    "Others" -> com.cheermateapp.data.model.Category.Others
                    else -> com.cheermateapp.data.model.Category.Work
                }
                val priority = when (priorityString) {
                    "High" -> Priority.High
                    "Medium" -> Priority.Medium
                    "Low" -> Priority.Low
                    else -> Priority.Medium
                }
                val reminderOption = com.cheermateapp.util.TaskDialogSpinnerHelper.getSelectedReminder(spinnerReminder)
                
                // Validation
                if (title.isEmpty()) {
                    etTaskTitle.error = "❌ Task title is required"
                    etTaskTitle.requestFocus()
                    return@setOnClickListener
                }
                
                if (dueDate.isEmpty()) {
                    etDueDate.error = "❌ Due date is required"
                    etDueDate.requestFocus()
                    return@setOnClickListener
                }
                
                // Validate reminder requires time
                if (reminderOption != "None" && dueTime.isEmpty()) {
                    ToastManager.showToast(this@MainActivity, "❌ Reminder requires a due time to be set", Toast.LENGTH_SHORT)
                    etDueTime.requestFocus()
                    return@setOnClickListener
                }
                
                // Create the task using the enhanced method
                createDetailedTask(
                    title = title,
                    description = if (description.isEmpty()) null else description,
                    dueDate = dueDate,
                    dueTime = if (dueTime.isEmpty()) null else dueTime,
                    category = category,
                    priority = priority,
                    reminderOption = reminderOption
                )
                
                android.util.Log.d("DIALOG_DEBUG", "Task created successfully, dismissing dialog")
                dialog.dismiss()
            }

            android.util.Log.d("DIALOG_DEBUG", "Showing enhanced XML dialog")
            dialog.show()
            
        } catch (e: Exception) {
            android.util.Log.e("DIALOG_DEBUG", "Error creating XML dialog", e)
            ToastManager.showToast(this, "Error creating dialog: ${e.message}", Toast.LENGTH_LONG)
        }
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
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
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

    // ✅ FIXED: TIME PICKER HELPER - 12-hour format with AM/PM
    private fun showTimePicker(editText: EditText) {
        val calendar = Calendar.getInstance()

        // Parse current time if exists
        if (editText.text.isNotEmpty()) {
            try {
                // Try parsing both 12-hour and 24-hour formats
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                val currentTime = timeFormat.parse(editText.text.toString())
                if (currentTime != null) {
                    calendar.time = currentTime
                } else {
                    // Fallback to 24-hour format for existing data
                    val fallbackFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val fallbackTime = fallbackFormat.parse(editText.text.toString())
                    if (fallbackTime != null) {
                        calendar.time = fallbackTime
                    }
                }
            } catch (e: Exception) {
                // Use current time if parsing fails
            }
        }

        val timePickerDialog = android.app.TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                // ✅ FIX: Use 12-hour format with AM/PM for Philippines
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                val selectedTime = Calendar.getInstance()
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedTime.set(Calendar.MINUTE, minute)
                editText.setText(timeFormat.format(selectedTime.time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false // ✅ FIX: 12-hour format with AM/PM
        )

        timePickerDialog.show()
    }

    // ✅ CREATE DETAILED TASK WITH ALL FIELDS
    private fun createDetailedTask(
        title: String,
        description: String?,
        dueDate: String,
        dueTime: String?,
        category: com.cheermateapp.data.model.Category,
        priority: Priority,
        reminderOption: String
    ) {
        android.util.Log.d("MainActivity", "Creating task with due date: '$dueDate'")
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
                    category = category,
                    priority = priority,
                    dueAt = dueDate,
                    dueTime = dueTime,
                    status = Status.Pending
                )

                withContext(Dispatchers.IO) {
                    db.taskDao().insert(newTask)
                }

                // Create reminder if requested
                if (reminderOption != "None" && dueTime != null) {
                    createTaskReminder(taskId, dueDate, dueTime, reminderOption)
                }

                // Refresh dashboard data
                loadTaskStatistics()
                loadRecentTasks()

                val timeText = if (dueTime != null) " at $dueTime" else ""
                val reminderText = if (reminderOption != "None") " (Reminder: $reminderOption)" else ""
                ToastManager.showToast(
                    this@MainActivity,
                    "✅ Task '$title' created for $dueDate$timeText$reminderText!",
                    Toast.LENGTH_LONG
                )

                android.util.Log.d("MainActivity", "✅ Created detailed task: $title with category: $category")

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error creating detailed task", e)
                ToastManager.showToast(this@MainActivity, "❌ Error creating task", Toast.LENGTH_SHORT)
            }
        }
    }

    // ✅ FIXED: CREATE TASK REMINDER with ReminderType
    private fun createTaskReminder(
        taskId: Int,
        dueDate: String,
        dueTime: String,
        reminderOption: String
    ) {
        uiScope.launch {
            try {
                val db = AppDb.get(this@MainActivity)

                // Parse due date and time
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val dueDateTime = dateFormat.parse("$dueDate $dueTime")

                if (dueDateTime != null) {
                    val dueTimeMillis = dueDateTime.time

                    // ✅ FIX: Map reminderOption to ReminderType enum
                    val reminderType = when (reminderOption) {
                        "10 minutes before" -> com.cheermateapp.data.model.ReminderType.TEN_MINUTES_BEFORE
                        "30 minutes before" -> com.cheermateapp.data.model.ReminderType.THIRTY_MINUTES_BEFORE
                        "At specific time" -> com.cheermateapp.data.model.ReminderType.AT_SPECIFIC_TIME
                        else -> null
                    }

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

                    // ✅ FIX: Include ReminderType in the reminder object
                    val reminder = com.cheermateapp.data.model.TaskReminder(
                        TaskReminder_ID = reminderId,
                        Task_ID = taskId,
                        User_ID = userId,
                        RemindAt = remindAtMillis,
                        ReminderType = reminderType,
                        IsActive = true
                    )

                    withContext(Dispatchers.IO) {
                        db.taskReminderDao().insert(reminder)
                    }

                    android.util.Log.d("MainActivity", "✅ Created reminder for task $taskId at $remindAtMillis with type: $reminderType")
                }

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error creating task reminder", e)
                // Don't show error to user - reminder is optional feature
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
            
            📈 Success Rate: ${actualStats["successRate"]}
            
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
                ToastManager.showToast(this@MainActivity, "Error loading statistics", Toast.LENGTH_SHORT)
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
            
            📊 Progress: ${actualProgress["percent"]}
            
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

                    // Get user's personality ID
                    val user = withContext(Dispatchers.IO) {
                        db.userDao().getById(userId)
                    }
                    
                    val personalityId = user?.Personality_ID
                    
                    if (personalityId != null) {
                        // Fetch motivation messages from database
                        val motivationMessages = withContext(Dispatchers.IO) {
                            db.messageTemplateDao().getByPersonalityAndCategory(personalityId, "motivation")
                        }
                        
                        if (motivationMessages.isNotEmpty()) {
                            val randomMessage = motivationMessages.random().MessageText
                            ToastManager.showToast(this@MainActivity, randomMessage, Toast.LENGTH_LONG)
                        } else {
                            // Fallback to personality's default motivation message
                            val personality = withContext(Dispatchers.IO) {
                                db.personalityDao().getById(personalityId)
                            }
                            val message = personality?.MotivationMessage ?: "💪 You've got this! Let's make today amazing!"
                            ToastManager.showToast(this@MainActivity, message, Toast.LENGTH_LONG)
                        }
                    } else {
                        ToastManager.showToast(this@MainActivity, "💪 You've got this! Let's make today amazing!", Toast.LENGTH_SHORT)
                    }

                } catch (e: Exception) {
                    ToastManager.showToast(this@MainActivity, "💪 You've got this! Let's make today amazing!", Toast.LENGTH_SHORT)
                }
            }
        } catch (e: Exception) {
            ToastManager.showToast(this, "💪 Stay motivated and keep pushing forward!", Toast.LENGTH_SHORT)
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
    
                    // Avoid flicker during theme toggle for username/persona
                    if (!isThemeApplying()) {
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
                    }
    
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Error loading user data", e)
                    findViewById<TextView>(R.id.tvUsername)?.text = "User"
                    ToastManager.showToast(this@MainActivity, "Error loading user data", Toast.LENGTH_SHORT)
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

                    // ✅ FIX: Count tasks DUE today (total tasks for today's progress)
                    val todayTasks = db.taskDao().getTodayTasksCount(userId, todayStr)
                    // ✅ FIX: Count tasks DUE today AND completed (matching numerator to denominator)
                    val todayCompletedTasks = db.taskDao().getCompletedTodayTasksCount(userId, todayStr)
                    val todayInProgressTasks = db.taskDao().getInProgressTodayTasksCount(userId, todayStr)
                    val pendingTasks = db.taskDao().getPendingTasksCount(userId)
                    val overdueTasks = db.taskDao().getOverdueTasksCount(userId)

                    android.util.Log.d("MainActivity", "✅ Dashboard statistics - Today: $todayStr")
                    android.util.Log.d(
                        "MainActivity",
                        "✅ Tasks due today: $todayTasks, Completed today (due today): $todayCompletedTasks, In Progress today: $todayInProgressTasks, Pending: $pendingTasks, Overdue: $overdueTasks"
                    )

                    mapOf(
                        "total" to totalTasks,
                        "completed" to completedTasks,
                        "today" to todayTasks,
                        "todayCompleted" to todayCompletedTasks,
                        "todayInProgress" to todayInProgressTasks,
                        "pending" to pendingTasks
                    )
                }

                updateStatisticsDisplay(stats)
                // ✅ Update progress bar with today's tasks progress
                // updateProgressDisplay(stats["todayCompleted"] ?: 0, stats["todayInProgress"] ?: 0, stats["today"] ?: 0)

                android.util.Log.d("MainActivity", "✅ Dashboard statistics loaded")

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error loading dashboard statistics", e)
                val fallbackStats: Map<String, Int> = mapOf("total" to 0, "completed" to 0, "today" to 0, "todayCompleted" to 0, "pending" to 0)
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
                            "pending" -> valueTextView?.text = stats["pending"].toString()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error updating grid statistics", e)
        }
    }

    private fun updateProgressDisplay(completed: Int, inProgress: Int, total: Int) {
        try {
            val percentage = if (total > 0) (completed * 100) / total else 0
            val inProgressPercentage = if (total > 0) (inProgress * 100) / total else 0

            var subtitle = "$completed of $total tasks completed today"
            if (inProgress > 0) {
                subtitle += " ($inProgress in progress)"
            }
            progressSubtitle.text = subtitle
            progressPercent.text = "$percentage%"

            // ✅ Update progress bar weight to reflect percentage
            (progressCompleted.layoutParams as? LinearLayout.LayoutParams)?.let { params ->
                params.weight = percentage.toFloat()
                progressCompleted.layoutParams = params
            }

            (progressInProgress.layoutParams as? LinearLayout.LayoutParams)?.let { params ->
                params.weight = inProgressPercentage.toFloat()
                progressInProgress.layoutParams = params
            }

            // Update the remainder weight
            val progressBar = progressCompleted.parent as? LinearLayout
            if (progressBar != null && progressBar.childCount > 2) {
                val remainder = progressBar.getChildAt(2)
                (remainder.layoutParams as? LinearLayout.LayoutParams)?.let { remParams ->
                    remParams.weight = (100 - percentage - inProgressPercentage).toFloat().coerceAtLeast(0f)
                    remainder.layoutParams = remParams
                }
                // ✅ Request layout update to force redraw
                progressBar.requestLayout()
            }

            android.util.Log.d("MainActivity", "✅ Progress updated: $percentage% completed, $inProgressPercentage% in progress ($completed/$inProgress/$total)")

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

    // ✅ ENHANCED RECENT TASKS DISPLAY - HYBRID TASK STACK WITH SWIPEABLE TASKS
    private fun updateRecentTasksDisplay(tasks: List<Task>) {
        try {
            val recentTasksContainer = findViewById<LinearLayout>(R.id.cardRecent)

            // Use white text for dark glass cards for better contrast
            val textColor = android.graphics.Color.WHITE
            android.util.Log.d("MainActivity", "🎨 Recent Tasks: Using white text color for contrast: #${Integer.toHexString(textColor)}")

            if (recentTasksContainer != null && recentTasksContainer.childCount > 1) {
                val contentArea = recentTasksContainer.getChildAt(1) as? LinearLayout
                contentArea?.removeAllViews()

                if (tasks.isEmpty()) {
                    val emptyText = TextView(this)
                    emptyText.text = "🎉 No pending tasks!\nTap + to create your first task!"
                    emptyText.textSize = 14f
                    emptyText.setTextColor(textColor)
                    emptyText.gravity = android.view.Gravity.CENTER
                    emptyText.setPadding(20, 30, 20, 30)
                    contentArea?.addView(emptyText)
                } else {
                    // Get active tasks (pending, in-progress, overdue) for swipeable view
                    val activeTasks = tasks.filter { task ->
                        task.Status == Status.Pending || 
                        task.Status == Status.InProgress ||
                        (task.Status == Status.Pending && isTaskOverdue(task, System.currentTimeMillis()))
                    }.sortedWith(compareBy<Task> { 
                        // Sort by overdue first, then priority
                        when {
                            isTaskOverdue(it, System.currentTimeMillis()) -> 0
                            else -> 1
                        }
                    }.thenBy { 
                        when (it.Priority) {
                            Priority.High -> 0
                            Priority.Medium -> 1
                            Priority.Low -> 2
                        }
                    })

                    if (activeTasks.isNotEmpty()) {
                        // Add swipeable task section header
                        val swipeHeader = TextView(this)
                        swipeHeader.text = "📋 Next Task (swipe to navigate)"
                        swipeHeader.textSize = 14f
                        swipeHeader.setTextColor(textColor)
                        swipeHeader.setTypeface(null, android.graphics.Typeface.BOLD)
                        swipeHeader.gravity = android.view.Gravity.CENTER
                        swipeHeader.setPadding(8, 8, 8, 8)
                        contentArea?.addView(swipeHeader)

                        // Create ViewPager2 for swipeable tasks
                        val viewPager = ViewPager2(this)
                        viewPager.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 8, 0, 8)
                        }
                        
                        // Set up adapter with callbacks
                        val adapter = TaskPagerAdapter(
                            activeTasks,
                            onCompleteClick = { task -> onTaskDone(task) },
                            onEditClick = { task -> showEditTaskDialog(task) },
                            onDeleteClick = { task -> deleteTask(task) }
                        )
                        viewPager.adapter = adapter
                        contentArea?.addView(viewPager)

                        // Add task counter
                        val counterText = TextView(this)
                        counterText.id = View.generateViewId()
                        counterText.text = "1 / ${activeTasks.size}"
                        counterText.textSize = 14f
                        counterText.setTextColor(textColor)
                        counterText.setTypeface(null, android.graphics.Typeface.BOLD)
                        counterText.gravity = android.view.Gravity.CENTER
                        counterText.setPadding(8, 8, 8, 8)
                        contentArea?.addView(counterText)

                        // Update counter when page changes
                        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                            override fun onPageSelected(position: Int) {
                                counterText.text = "${position + 1} / ${activeTasks.size}"
                            }
                        })
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error updating recent tasks display", e)
            ToastManager.showToast(this, "Error displaying tasks", Toast.LENGTH_SHORT)
        }
    }


    // ✅ ENHANCED - Use item_task.xml layout instead of programmatic views
    private fun createTaskCard(task: Task, container: LinearLayout?) {
        try {
            // Inflate the item_task.xml layout
            val inflater = android.view.LayoutInflater.from(this)
            val taskItemView = inflater.inflate(R.layout.item_task, container, false)

            // ✅ FIND ALL VIEWS FROM item_task.xml
            val layoutPriorityIndicator = taskItemView.findViewById<View>(R.id.layoutPriorityIndicator)
            val tvTaskTitle = taskItemView.findViewById<TextView>(R.id.tvTaskTitle)
            val tvTaskDescription = taskItemView.findViewById<TextView>(R.id.tvTaskDescription)
            val tvTaskPriority = taskItemView.findViewById<TextView>(R.id.tvTaskPriority)
            val tvTaskStatus = taskItemView.findViewById<TextView>(R.id.tvTaskStatus)
            val tvTaskCategory = taskItemView.findViewById<TextView>(R.id.tvTaskCategory)
            val tvTaskDueDate = taskItemView.findViewById<TextView>(R.id.tvTaskDueDate)
            val tvTaskProgress = taskItemView.findViewById<TextView>(R.id.tvTaskProgress)
            val progressBar = taskItemView.findViewById<android.widget.ProgressBar>(R.id.progressBar)
            val btnComplete = taskItemView.findViewById<TextView>(R.id.btnComplete)
            val btnEdit = taskItemView.findViewById<TextView>(R.id.btnEdit)
            val btnDelete = taskItemView.findViewById<TextView>(R.id.btnDelete)

            // ✅ POPULATE VIEWS WITH TASK DATA
            
            // 1. Priority Indicator Color
            layoutPriorityIndicator?.setBackgroundColor(task.getPriorityColor())

            // 2. Task Title
            tvTaskTitle.text = task.Title

            // 3. Task Description
            if (!task.Description.isNullOrBlank()) {
                tvTaskDescription.text = task.Description
                tvTaskDescription.visibility = View.VISIBLE
            } else {
                tvTaskDescription.visibility = View.GONE
            }

            // 4. Priority with emoji
            tvTaskPriority.text = when (task.Priority) {
                com.cheermateapp.data.model.Priority.High -> "🔴 High"
                com.cheermateapp.data.model.Priority.Medium -> "🟡 Medium"
                com.cheermateapp.data.model.Priority.Low -> "🟢 Low"
            }

            // 5. Status with emoji
            tvTaskStatus.text = when (task.Status) {
                com.cheermateapp.data.model.Status.Pending -> "⏳ Pending"
                com.cheermateapp.data.model.Status.InProgress -> "🔄 In Progress"
                com.cheermateapp.data.model.Status.Completed -> "✅ Completed"
                com.cheermateapp.data.model.Status.OverDue -> "🔴 Overdue"
                com.cheermateapp.data.model.Status.Cancelled -> "❌ Cancelled"
            }

            // 6. Category with icon
            tvTaskCategory.text = task.Category.getDisplayText()

            // 7. Progress
            if (tvTaskProgress != null && progressBar != null) {
                tvTaskProgress.text = "${task.TaskProgress} %"
                progressBar.progress = task.TaskProgress
                
                // Show progress bar for in-progress tasks
                if (task.Status == com.cheermateapp.data.model.Status.InProgress || task.TaskProgress > 0) {
                    tvTaskProgress.visibility = View.VISIBLE
                    progressBar.visibility = View.VISIBLE
                    progressBar.parent?.let { parent ->
                        (parent as? View)?.visibility = View.VISIBLE
                    }
                } else {
                    tvTaskProgress.visibility = View.GONE
                    progressBar.visibility = View.GONE
                    progressBar.parent?.let { parent ->
                        (parent as? View)?.visibility = View.GONE
                    }
                }
            }

            // 8. Due Date
            if (task.Status == com.cheermateapp.data.model.Status.Completed) {
                tvTaskDueDate.visibility = View.GONE
            } else {
                tvTaskDueDate.visibility = View.VISIBLE
                tvTaskDueDate.text = "📅 Due: ${task.getFormattedDueDateTime()}"
                tvTaskDueDate.setTextColor(0xFFE53E3E.toInt()) // Red
            }

            // 9. Button States based on Task Status
            when (task.Status) {
                com.cheermateapp.data.model.Status.Completed -> {
                    btnComplete.text = "✅ Completed"
                    btnComplete.isClickable = false
                    btnComplete.alpha = 0.6f
                }
                com.cheermateapp.data.model.Status.Pending -> {
                    btnComplete.text = "✅ Complete"
                    btnComplete.isClickable = true
                    btnComplete.alpha = 1.0f
                }
                com.cheermateapp.data.model.Status.InProgress -> {
                    btnComplete.text = "✅ Finish"
                    btnComplete.isClickable = true
                    btnComplete.alpha = 1.0f
                }
                com.cheermateapp.data.model.Status.OverDue -> {
                    btnComplete.text = "🔴 Complete"
                    btnComplete.isClickable = true
                    btnComplete.alpha = 1.0f
                }
                com.cheermateapp.data.model.Status.Cancelled -> {
                    btnComplete.text = "❌ Cancelled"
                    btnComplete.isClickable = false
                    btnComplete.alpha = 0.6f
                }
            }

            // ✅ SET UP CLICK LISTENERS FOR ACTION BUTTONS

            // Complete Button
            btnComplete.setOnClickListener {
                if (task.Status != com.cheermateapp.data.model.Status.Completed && 
                    task.Status != com.cheermateapp.data.model.Status.Cancelled) {
                    onTaskDone(task)
                }
            }

            // Edit Button
            btnEdit.setOnClickListener {
                showEditTaskDialog(task)
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
            container?.addView(taskItemView)

            android.util.Log.d("MainActivity", "✅ Successfully inflated and populated item_task.xml")

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error creating task card", e)
        }
    }

    // ✅ CREATE COMPACT TASK CARD FOR COLLAPSIBLE SECTION
    private fun createCompactTaskCard(task: Task, container: LinearLayout?) {
        try {
            val cardView = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(12, 8, 12, 8)
                setBackgroundColor(android.graphics.Color.parseColor("#1AFFFFFF"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 4, 0, 4)
                }
            }

            // Priority indicator
            val priorityDot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(8, 8).apply {
                    setMargins(0, 4, 8, 0)
                }
                setBackgroundColor(when (task.Priority) {
                    Priority.High -> android.graphics.Color.RED
                    Priority.Medium -> android.graphics.Color.YELLOW
                    Priority.Low -> android.graphics.Color.GREEN
                })
            }

            // Task info
            val taskInfo = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val titleText = TextView(this).apply {
                text = task.Title
                textSize = 13f
                setTextColor(android.graphics.Color.WHITE)
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val statusText = TextView(this).apply {
                text = when (task.Status) {
                    Status.Pending -> "⏳ Pending"
                    Status.InProgress -> "🔄 In Progress"
                    Status.Completed -> "✅ Completed"
                    Status.OverDue -> "🔴 Overdue"
                    Status.Cancelled -> "❌ Cancelled"
                }
                textSize = 11f
                setTextColor(android.graphics.Color.LTGRAY)
            }

            taskInfo.addView(titleText)
            taskInfo.addView(statusText)

            cardView.addView(priorityDot)
            cardView.addView(taskInfo)

            // Click to see details
            cardView.setOnClickListener {
                showTaskDetailsDialog(task)
            }

            container?.addView(cardView)

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error creating compact task card", e)
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
                onTaskDone(task)
            }
            .setNeutralButton("Edit") { _, _ ->
                navigateToTasks()
            }
            .setNegativeButton("Close", null)
            .show()
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

                        ToastManager.showToast(this@MainActivity, "🗑️ Task deleted!", Toast.LENGTH_SHORT)

                    } catch (e: Exception) {
                        ToastManager.showToast(this@MainActivity, "❌ Error deleting task", Toast.LENGTH_SHORT)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
        // Reload user data to reflect any personality changes
        loadUserData()
        
        // Refresh calendar info display for current selected date
        selectedCalendarDateStr?.let { dateStr ->
            updateCalendarSelectedTasks(dateStr)
        }
        
        // Restore Settings tab if theme toggle caused recreation
        kotlin.runCatching {
            val prefs = getSharedPreferences("cheermate_theme_prefs", MODE_PRIVATE)
            if (prefs.getBoolean("return_to_settings", false)) {
                findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)?.selectedItemId = R.id.nav_settings
                navigateToSettings()
                prefs.edit().putBoolean("return_to_settings", false).apply()
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    // ✅ OBSERVE TASK CHANGES FOR LIVE PROGRESS BAR UPDATES ON HOME SCREEN
        private var progressBarObserverJob: kotlinx.coroutines.Job? = null
        private fun startObserveTaskChangesForProgressBar() {
            progressBarObserverJob?.cancel()
            progressBarObserverJob = lifecycleScope.launch {
                try {
                    val db = AppDb.get(this@MainActivity)
    
                    val today = Calendar.getInstance()
                    val todayStr = dateToString(today.time)
    
                    // Observe today total, completed, and in-progress tasks
                    kotlinx.coroutines.flow.combine(
                        db.taskDao().getTodayTasksCountFlow(userId, todayStr),
                        db.taskDao().getCompletedTodayTasksCountFlow(userId, todayStr),
                        db.taskDao().getInProgressTodayTasksCountFlow(userId, todayStr)
                    ) { totalToday, completedToday, inProgressToday ->
                        Triple(totalToday, completedToday, inProgressToday)
                    }.collect { (totalToday, completedToday, inProgressToday) ->
                        // Update UI on main thread
                        withContext(Dispatchers.Main) {
                            updateProgressDisplay(completedToday, inProgressToday, totalToday)
                            android.util.Log.d(
                                "MainActivity",
                                "🔄 Live progress updated: completed=$completedToday, inProgress=$inProgressToday, total=$totalToday"
                            )
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Error observing task changes for progress bar", e)
                }
            }
        }
        private fun stopObserveTaskChangesForProgressBar() {
            progressBarObserverJob?.cancel()
            progressBarObserverJob = null
        }
    }