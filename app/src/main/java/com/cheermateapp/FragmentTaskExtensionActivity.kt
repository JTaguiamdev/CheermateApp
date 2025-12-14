package com.cheermateapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.cheermateapp.data.db.AppDb
import com.cheermateapp.data.model.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

import com.cheermateapp.util.ReminderManager

class FragmentTaskExtensionActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TASK_ID = "TASK_ID"
        const val EXTRA_USER_ID = "USER_ID"
    }

    private var taskId: Int = 0
    private var userId: Int = 0
    private var currentTask: Task? = null
    private val subtasks = mutableListOf<SubTask>()

    // UI Elements
    private lateinit var toolbar: Toolbar
    private lateinit var overdueRow: LinearLayout
    private lateinit var overdueText: TextView
    private lateinit var taskCard: LinearLayout
    private lateinit var etTaskTitle: EditText
    private lateinit var etTaskDescription: EditText
    private lateinit var tvTaskStatus: TextView
    private lateinit var btnTaskCategory: TextView
    private lateinit var btnTaskPriority: TextView
    private lateinit var btnTaskDueDate: TextView
    private lateinit var btnTaskDueTime: TextView
    private lateinit var btnTaskReminder: TextView
    private lateinit var subtaskCard: LinearLayout
    private lateinit var etSubtaskInput: EditText
    private lateinit var btnAddSubtask: EditText
    private lateinit var subtasksContainer: androidx.recyclerview.widget.RecyclerView
    private lateinit var subtaskAdapter: SubTaskAdapter
    private lateinit var tvNoSubtasks: TextView
    private lateinit var tvItemsCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.cheermateapp.util.ThemeManager.initializeTheme(this)
        setContentView(R.layout.fragment_tasks_extension)

        // Get data from intent
        taskId = intent.getIntExtra(EXTRA_TASK_ID, 0)
        userId = intent.getIntExtra(EXTRA_USER_ID, 0)

        if (taskId == 0) {
            Toast.makeText(this, "Invalid task", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        setupListeners()
        loadTaskDetails()
        loadSubtasks()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        overdueRow = findViewById(R.id.overdue_row)
        overdueText = findViewById(R.id.overdue_text)
        taskCard = findViewById(R.id.task_card)
        etTaskTitle = findViewById(R.id.et_task_title)
        etTaskDescription = findViewById(R.id.et_task_description)
        tvTaskStatus = findViewById(R.id.tvTaskStatus)
        btnTaskCategory = findViewById(R.id.btn_task_category)
        btnTaskPriority = findViewById(R.id.btn_task_priority)
        btnTaskDueDate = findViewById(R.id.btn_task_due_date)
        btnTaskDueTime = findViewById(R.id.btn_task_due_time)
        btnTaskReminder = findViewById(R.id.btn_task_reminder)
        subtaskCard = findViewById(R.id.subtask_card)
        etSubtaskInput = findViewById(R.id.et_subtask_input)
        btnAddSubtask = findViewById(R.id.btn_add_subtask)
        subtasksContainer = findViewById(R.id.subtasks_container)
        tvNoSubtasks = findViewById(R.id.tv_no_subtasks)
        tvItemsCount = findViewById(R.id.tv_items_count)

                // Initialize subtask adapter
                subtaskAdapter = SubTaskAdapter(
                    subtasks,
                    onSubTaskToggle = { subtask ->
                        val updatedSubtask = subtask.copy(
                            IsCompleted = if (subtask.IsCompleted == "Yes") "No" else "Yes"
                        )
                        updateSubtask(updatedSubtask)
                    },
                    onSubTaskDelete = { subtask ->
                        showDeleteSubtaskConfirmation(subtask)
                    }
                )
                subtasksContainer.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
                subtasksContainer.adapter = subtaskAdapter
        
                // Setup toolbar
                setSupportActionBar(toolbar)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.setDisplayShowHomeEnabled(true)
                
                // Set navigation icon color based on theme
                val iconColor = resources.getColor(R.color.toolbar_icon, theme)
                toolbar.navigationIcon?.setTint(iconColor)
                
                toolbar.setNavigationOnClickListener { 
                    setResult(RESULT_OK)
                    finish() 
                }
            }
        
            private fun setupListeners() {
                // Category button click
                btnTaskCategory.setOnClickListener {
                    showCategoryDialog()
                }
        
                // Priority button click
                btnTaskPriority.setOnClickListener {
                    showPriorityDialog()
                }
        
                // Due Date button click
                btnTaskDueDate.setOnClickListener {
                    showDueDateDialog()
                }
                
                // Due Time button click
                btnTaskDueTime.setOnClickListener {
                    showDueTimeDialog()
                }
        
                // Reminder button click
                btnTaskReminder.setOnClickListener {
                    showReminderDialog()
                }
        
                // Subtask input text watcher
                etSubtaskInput.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        btnAddSubtask.visibility = if (s?.isNotEmpty() == true) View.VISIBLE else View.GONE
                    }
                    override fun afterTextChanged(s: Editable?) {}
                })
        
                // Add subtask button click
                btnAddSubtask.setOnClickListener {
                    addSubtask()
                }
        
                // Save task changes when title or description changes
                etTaskTitle.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) saveTaskChanges()
                }
        
                etTaskDescription.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) saveTaskChanges()
                }
            }
        
        
            private fun loadTaskDetails() {
                lifecycleScope.launch {
                    try {
                        val db = AppDb.get(this@FragmentTaskExtensionActivity)
                        val task = withContext(Dispatchers.IO) {
                            db.taskDao().getTaskById(taskId)
                        }
        
                        if (task != null) {
                            currentTask = task
                            displayTaskDetails(task)
                        } else {
                            Toast.makeText(
                                this@FragmentTaskExtensionActivity,
                                "Task not found",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("FragmentTaskExtensionActivity", "Error loading task", e)
                        Toast.makeText(
                            this@FragmentTaskExtensionActivity,
                            "Error loading task details",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        
            private fun displayTaskDetails(task: Task) {
                // Set task title
                etTaskTitle.setText(task.Title)
        
                // Set description
                if (!task.Description.isNullOrBlank()) {
                    etTaskDescription.setText(task.Description)
                }
        
                // Update status
                updateTaskStatus(task.Status)
        
                // Update category button
                updateCategoryButton(task.Category)
        
                // Update priority button
                updatePriorityButton(task.Priority)
        
                // Update due date button
                updateDueDateButton(task.DueDate)
                
                // Update due time button
                updateDueTimeButton(task.DueTime)
        
                // ✅ Load and display reminder if exists
                loadTaskReminder(task)
        
                // Check if task is overdue
                if (task.isOverdue()) {
                    overdueRow.visibility = View.VISIBLE
                    val overdueDays = calculateOverdueDays(task)
                    overdueText.text = "⏰ Overdue by $overdueDays day${if (overdueDays != 1) "s" else ""}"
                } else {
                    overdueRow.visibility = View.GONE
                }
            }
        
            private fun updateCategoryButton(category: Category) {
                btnTaskCategory.text = when (category) {
                    Category.Work -> "💼 Work"
                    Category.Personal -> "👤 Personal"
                    Category.Shopping -> "🛒 Shopping"
                    Category.Others -> "📤 Others"
                }
            }
        
            private fun updatePriorityButton(priority: Priority) {
                btnTaskPriority.text = when (priority) {
                    Priority.High -> "🔴 High"
                    Priority.Medium -> "🟡 Medium"
                    Priority.Low -> "🟢 Low"
                }
            }
        
            private fun updateTaskStatus(status: Status) {
                tvTaskStatus.text = when (status) {
                    Status.Pending -> "⏳ Pending"
                    Status.InProgress -> "🔄 In Progress"
                    Status.Completed -> "✅ Completed"
                    Status.Cancelled -> "❌ Cancelled"
                    Status.OverDue -> "🔴 Overdue"
                }
            }
        
            private fun updateDueDateButton(dueDate: String?) {
                if (dueDate != null) {
                    try {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val date = dateFormat.parse(dueDate)
                        val displayFormat = SimpleDateFormat("📅 M/d/yyyy", Locale.getDefault())
                        btnTaskDueDate.text = displayFormat.format(date!!)
                    } catch (e: Exception) {
                        btnTaskDueDate.text = "📅 $dueDate"
                    }
                } else {
                    btnTaskDueDate.text = "📅 Select Date"
                }
            }
            
            private fun updateDueTimeButton(dueTime: String?) {
                android.util.Log.d("FragmentTaskExtension", "🕐 Updating due time button with: $dueTime")
                
                if (dueTime != null && dueTime != "12:00 PM") {
                    try {
                        // Parse the stored time (should be in HH:mm format)
                        val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val time = inputFormat.parse(dueTime)
                        
                        // Format to 12-hour format with AM/PM
                        val displayFormat = SimpleDateFormat("🕐 h:mm a", Locale.getDefault())
                        btnTaskDueTime.text = displayFormat.format(time!!)
                        
                        android.util.Log.d("FragmentTaskExtension", "✅ Due time button updated to: ${btnTaskDueTime.text}")
                    } catch (e: Exception) {
                        android.util.Log.e("FragmentTaskExtension", "❌ Error parsing due time: $dueTime", e)
                        btnTaskDueTime.text = "🕐 $dueTime"
                    }
                } else {
                    btnTaskDueTime.text = "🕐 12:00 PM"
                    android.util.Log.d("FragmentTaskExtension", "📭 Due time is null/default, showing default time")
                }
            }
        
            private fun showCategoryDialog() {
                val categories = arrayOf("💼 Work", "👤 Personal", "🛒 Shopping", "📤 Others")
                val categoryValues = arrayOf(Category.Work, Category.Personal, Category.Shopping, Category.Others)
        
                AlertDialog.Builder(this)
                    .setTitle("Select Category")
                    .setItems(categories) { _, which ->
                        currentTask?.let { task ->
                            android.util.Log.d("FragmentTaskExtension", "🏷️ Setting category to: ${categoryValues[which]}")
                            
                            val updatedTask = task.copy(
                                Category = categoryValues[which], // ✅ FIXED: Include Category field!
                                UpdatedAt = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp()
                            )
                            // Update currentTask immediately to ensure it's saved in onPause
                            currentTask = updatedTask
                            saveTask(updatedTask)
                            updateCategoryButton(categoryValues[which])
                            
                            android.util.Log.d("FragmentTaskExtension", "✅ Category updated to: ${categoryValues[which]}")
                        }
                    }
                    .show()
            }
        
            private fun showPriorityDialog() {
                val priorities = arrayOf("🔴 High", "🟡 Medium", "🟢 Low")
                val priorityValues = arrayOf(Priority.High, Priority.Medium, Priority.Low)
        
                AlertDialog.Builder(this)
                    .setTitle("Select Priority")
                    .setItems(priorities) { _, which ->
                        currentTask?.let { task ->
                            android.util.Log.d("FragmentTaskExtension", "🔥 Setting priority to: ${priorityValues[which]}")
                            
                            val updatedTask = task.copy(
                                Priority = priorityValues[which], // ✅ FIXED: Include Priority field!
                                UpdatedAt = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp()
                            )
                            // Update currentTask immediately to ensure it's saved in onPause
                            currentTask = updatedTask
                            saveTask(updatedTask)
                            updatePriorityButton(priorityValues[which])
                            
                            android.util.Log.d("FragmentTaskExtension", "✅ Priority updated to: ${priorityValues[which]}")
                        }
                    }
                    .show()
            }
        
            private fun showDueDateDialog() {
                val options = arrayOf("📅 Today", "📅 Tomorrow", "📅 Custom Date")
        
                AlertDialog.Builder(this)
                    .setTitle("Select Due Date")
                    .setItems(options) { _, which ->
                        when (which) {
                            0 -> setDueDateToday()
                            1 -> setDueDateTomorrow()
                            2 -> showCustomDatePicker()
                        }
                    }
                    .show()
            }
        
            private fun setDueDateToday() {
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val newDueDate = dateFormat.format(calendar.time)
                updateTaskDueDate(newDueDate)
            }
        
            private fun setDueDateTomorrow() {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val newDueDate = dateFormat.format(calendar.time)
                updateTaskDueDate(newDueDate)
            }
        
            private fun showCustomDatePicker() {
                val calendar = Calendar.getInstance()
                
                // Parse current due date if available
                currentTask?.DueDate?.let { dueAt ->
                    try {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        dateFormat.parse(dueAt)?.let { calendar.time = it }
                    } catch (e: Exception) {
                        // Use current date
                    }
                }
        
                val datePickerDialog = DatePickerDialog(
                    this,
                    { _, year, month, dayOfMonth ->
                        val selectedCalendar = Calendar.getInstance()
                        selectedCalendar.set(year, month, dayOfMonth)
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val newDueDate = dateFormat.format(selectedCalendar.time)
                        updateTaskDueDate(newDueDate)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                
                datePickerDialog.show()
            }
        
            private fun updateTaskDueDate(newDueDate: String) {
                currentTask?.let { task ->
                    val updatedTask = task.copy(
                        DueDate = newDueDate,
                        UpdatedAt = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp()
                    )
                    // Update currentTask immediately to ensure it's saved in onPause
                    currentTask = updatedTask
                    saveTask(updatedTask)
                    updateDueDateButton(newDueDate)
                    
                    // Update overdue status
                    if (updatedTask.isOverdue()) {
                        overdueRow.visibility = View.VISIBLE
                        val overdueDays = calculateOverdueDays(updatedTask)
                        overdueText.text = "⏰ Overdue by $overdueDays day${if (overdueDays != 1) "s" else ""}"
                    } else {
                        overdueRow.visibility = View.GONE
                    }
                }
            }
            
            private fun showDueTimeDialog() {
                val calendar = Calendar.getInstance()
                
                // Parse current due time if available
                currentTask?.DueTime?.let { dueTime ->
                    try {
                        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        timeFormat.parse(dueTime)?.let { 
                            calendar.time = it 
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("FragmentTaskExtension", "Error parsing current due time: $dueTime", e)
                    }
                }
                
                val timePickerDialog = TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        android.util.Log.d("FragmentTaskExtension", "🕐 Time selected: $hourOfDay:$minute")
                        
                        // Format time as HH:mm for database storage
                        val timeFormat24 = SimpleDateFormat("HH:mm", Locale.getDefault())
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        val newDueTime = timeFormat24.format(calendar.time)
                        
                        // Update task with new due time
                        updateTaskDueTime(newDueTime)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false // Use 12-hour format
                )
                
                timePickerDialog.show()
            }
            
            private fun updateTaskDueTime(newDueTime: String) {
                currentTask?.let { task ->
                    android.util.Log.d("FragmentTaskExtension", "🕐 Updating task due time to: $newDueTime")
                    
                    val updatedTask = task.copy(
                        DueTime = newDueTime,
                        UpdatedAt = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp()
                    )
                    // Update currentTask immediately to ensure it's saved
                    currentTask = updatedTask
                    saveTask(updatedTask)
                    updateDueTimeButton(newDueTime)
                    
                    android.util.Log.d("FragmentTaskExtension", "✅ Due time updated to: $newDueTime")
                }
            }
        
            private fun showReminderDialog() {
                val options = arrayOf("⏰ 10 minutes before", "⏰ 30 minutes before", "⏰ At specific time")
        
                AlertDialog.Builder(this)
                    .setTitle("Set Reminder")
                    .setItems(options) { _, which ->
                        when (which) {
                            0 -> setReminder("10 minutes before")
                            1 -> setReminder("30 minutes before")
                            2 -> showSpecificTimeReminder()
                        }
                    }
                    .show()
            }
        
            private fun setReminder(reminderText: String) {
                android.util.Log.d("FragmentTaskExtension", "🎯 Setting reminder: $reminderText")
                
                // ✅ Save reminder to database (UI will be updated in callback)
                currentTask?.let { task ->
                    val reminderType = when (reminderText) {
                        "10 minutes before" -> ReminderType.TEN_MINUTES_BEFORE
                        "30 minutes before" -> ReminderType.THIRTY_MINUTES_BEFORE
                        else -> null
                    }
                    
                    if (reminderType != null) {
                        saveReminderToDatabase(task, reminderType)
                    }
                }
                
                Toast.makeText(this, "Reminder set: $reminderText", Toast.LENGTH_SHORT).show()
            }
        
            private fun showSpecificTimeReminder() {
                val calendar = Calendar.getInstance()
                
                val timePickerDialog = TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                        
                        // ✅ FIX: Create reminder calendar with correct date logic
                        val reminderCalendar = Calendar.getInstance()
                        reminderCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        reminderCalendar.set(Calendar.MINUTE, minute)
                        reminderCalendar.set(Calendar.SECOND, 0)
                        reminderCalendar.set(Calendar.MILLISECOND, 0)
                        
                        // ✅ FIX: If selected time has passed today, schedule for tomorrow
                        val now = Calendar.getInstance()
                        if (reminderCalendar.timeInMillis <= now.timeInMillis) {
                            android.util.Log.d("FragmentTaskExtension", "⚠️ Selected time has passed, scheduling for tomorrow")
                            reminderCalendar.add(Calendar.DAY_OF_MONTH, 1)
                        }
                        
                        val timeString = timeFormat.format(reminderCalendar.time)
                        android.util.Log.d("FragmentTaskExtension", "🎯 Setting specific time reminder: $timeString")
                        android.util.Log.d("FragmentTaskExtension", "📅 Reminder timestamp: ${reminderCalendar.timeInMillis}")
                        
                        // ✅ Save reminder to database with corrected timestamp
                        currentTask?.let { task ->
                            saveReminderToDatabase(task, ReminderType.AT_SPECIFIC_TIME, reminderCalendar.timeInMillis)
                            
                            val displayText = if (reminderCalendar.get(Calendar.DAY_OF_YEAR) > now.get(Calendar.DAY_OF_YEAR)) {
                                "Reminder set for tomorrow at $timeString"
                            } else {
                                "Reminder set at $timeString"
                            }
                            Toast.makeText(this, displayText, Toast.LENGTH_SHORT).show()
                        }
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                )
                
                timePickerDialog.show()
            }
        
            // ✅ Prevent rapid duplicate calls
            private var isReminderSaving = false
            
            // ✅ FIXED: Save or update reminder in TaskReminder table (prevents duplication)
            private fun saveReminderToDatabase(task: Task, reminderType: ReminderType, specificTime: Long? = null) {
                // ✅ Debounce: Prevent multiple simultaneous saves
                if (isReminderSaving) {
                    android.util.Log.d("FragmentTaskExtension", "⏭️ Skipping duplicate reminder save call")
                    return
                }
                
                isReminderSaving = true
                
                // Show immediate feedback
                btnTaskReminder.text = "⏰ Saving..."
                
                lifecycleScope.launch {
                    try {
                        val db = AppDb.get(this@FragmentTaskExtensionActivity)
                        
                        withContext(Dispatchers.IO) {
                            android.util.Log.d("FragmentTaskExtension", "🔧 Starting reminder save for Task_ID: ${task.Task_ID}")
                                
                            // Calculate reminder time based on type
                            val dueDate = task.getParsedDueDate()
                            val dueTime = task.DueTime
                                
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                val dueDateTime = dateFormat.parse("${task.DueDate} ${task.DueTime}")

                                if (dueDateTime == null) {
                                    android.util.Log.e("AlarmDebug", "Failed to parse due date/time for task ${task.Task_ID}")
                                    return@withContext
                                }
                                val dueTimeMillis = dueDateTime.time

                                val remindAt = when (reminderType) {
                                    ReminderType.TEN_MINUTES_BEFORE -> {
                                        dueTimeMillis - (10 * 60 * 1000)
                                    }
                                    ReminderType.THIRTY_MINUTES_BEFORE -> {
                                        dueTimeMillis - (30 * 60 * 1000)
                                    }
                                    ReminderType.AT_SPECIFIC_TIME -> {
                                        specificTime ?: dueTimeMillis
                                    }
                                }                    
                                
                                // ✅ DEBUG: Check all reminders first, then active ones
                                val allRemindersForTask = db.taskReminderDao().getRemindersByTask(task.Task_ID)
                                android.util.Log.d("FragmentTaskExtension", "🔍 DEBUG: Found ${allRemindersForTask.size} total reminders for Task_ID: ${task.Task_ID}")
                                allRemindersForTask.forEach { reminder ->
                                    android.util.Log.d("FragmentTaskExtension", "  📝 Reminder ID: ${reminder.TaskReminder_ID}, IsActive: ${reminder.IsActive}, Type: ${reminder.ReminderType}")
                                }
                                
                                val existingReminders = db.taskReminderDao().activeForTask(task.Task_ID, task.User_ID)
                                android.util.Log.d("FragmentTaskExtension", "🔍 Found ${existingReminders.size} ACTIVE reminders for Task_ID: ${task.Task_ID}")
                                
                                // ✅ SIMPLE FIX: Delete ALL existing reminders first, then create ONE new one
                                // This guarantees exactly one reminder per task
                                android.util.Log.d("FragmentTaskExtension", "🗑️ Deleting ALL existing reminders for Task_ID: ${task.Task_ID}")
                                db.taskReminderDao().deleteAllForTask(task.Task_ID, task.User_ID)
                                
                                // ✅ Always create ONE new reminder (guaranteed no duplicates)
                                val userId = task.User_ID
                                val fixedReminderId = task.Task_ID  // Use Task_ID as Reminder_ID for 1:1 relationship
                                
                                val newReminder = TaskReminder(
                                    TaskReminder_ID = fixedReminderId,
                                    Task_ID = task.Task_ID,
                                    User_ID = task.User_ID,
                                    RemindAt = TaskReminder.timestampToReadableString(remindAt),
                                    ReminderType = reminderType,
                                    IsActive = true,
                                    CreatedAt = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp(),
                                    UpdatedAt = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp()
                                )
                                
                                db.taskReminderDao().insert(newReminder)
                                android.util.Log.d("FragmentTaskExtension", "✅ SINGLE reminder CREATED: $reminderType with ID: $fixedReminderId for Task_ID: ${task.Task_ID}")
                                
                                
                                // Schedule alarm within transaction
                                ReminderManager.scheduleReminder(
                                    applicationContext,
                                    task.Task_ID,
                                    task.Title,
                                    task.Description,
                                    task.User_ID,
                                    remindAt
                                )
                        }
                        
                        // ✅ Update UI immediately with the saved reminder type
                        withContext(Dispatchers.Main) {
                            when (reminderType) {
                                ReminderType.TEN_MINUTES_BEFORE -> btnTaskReminder.text = "⏰ 10 minutes before"
                                ReminderType.THIRTY_MINUTES_BEFORE -> btnTaskReminder.text = "⏰ 30 minutes before"
                                ReminderType.AT_SPECIFIC_TIME -> {
                                    val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
                                    val timeString = formatter.format(Date(specificTime ?: System.currentTimeMillis()))
                                    btnTaskReminder.text = "⏰ At $timeString"
                                }
                            }
                        }
                        
                        android.util.Log.d("FragmentTaskExtension", "🔧 Transaction completed successfully")
                        
                    } catch (e: Exception) {
                        android.util.Log.e("FragmentTaskExtension", "Error saving reminder", e)
                        Toast.makeText(
                            this@FragmentTaskExtensionActivity,
                            "Error saving reminder: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        // Reset button text on error
                        btnTaskReminder.text = "⏰ Reminder"
                    } finally {
                        // ✅ Always reset the saving flag
                        isReminderSaving = false
                    }
                }
            }
        
            // ✅ NEW: Load and display existing reminder for the task
            private fun loadTaskReminder(task: Task) {
                lifecycleScope.launch {
                    try {
                        android.util.Log.d("FragmentTaskExtension", "🔍 Loading reminders for Task_ID: ${task.Task_ID}, User_ID: ${task.User_ID}")
                        
                        val db = AppDb.get(this@FragmentTaskExtensionActivity)
                        
                        val reminders = withContext(Dispatchers.IO) {
                            db.taskReminderDao().activeForTask(task.Task_ID, task.User_ID)
                        }
                        
                        android.util.Log.d("FragmentTaskExtension", "📋 Found ${reminders.size} reminder(s)")
                        
                        if (reminders.isNotEmpty()) {
                            val reminder = reminders.first()
                            
                            android.util.Log.d("FragmentTaskExtension", "📝 Reminder Type: ${reminder.ReminderType}")
                            android.util.Log.d("FragmentTaskExtension", "📝 Reminder Time: ${reminder.RemindAt}")
                            
                            // Display reminder based on type
                            val reminderText = when (reminder.ReminderType) {
                                ReminderType.TEN_MINUTES_BEFORE -> "⏰ 10 minutes before"
                                ReminderType.THIRTY_MINUTES_BEFORE -> "⏰ 30 minutes before"
                                ReminderType.AT_SPECIFIC_TIME -> {
                                    try {
                                        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                                        val time = Date(reminder.remindAtTimestamp)
                                        "⏰ At ${timeFormat.format(time)}"
                                    } catch (e: Exception) {
                                        "⏰ At specific time"
                                    }
                                }
                                null -> "⏰ Reminder"
                            }
                            
                            btnTaskReminder.text = reminderText
                            
                            android.util.Log.d("FragmentTaskExtension", "✅ Loaded reminder: $reminderText")
                        } else {
                            btnTaskReminder.text = "⏰ Reminder"
                            android.util.Log.d("FragmentTaskExtension", "📭 No reminders found - showing default text")
                        }
                        
                    } catch (e: Exception) {
                        android.util.Log.e("FragmentTaskExtension", "Error loading reminder", e)
                        btnTaskReminder.text = "⏰ Reminder"
                    }
                }
            }
        
        
            private fun calculateOverdueDays(task: Task): Int {
                val dueDate = task.getParsedDueDate()
                if (dueDate != null) {
                    val currentDate = Date()
                    val diffInMillis = currentDate.time - dueDate.time
                    val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)
                    return diffInDays.toInt()
                }
                return 0
            }
        
            private fun saveTaskChanges() {
                currentTask?.let { task ->
                    val title = etTaskTitle.text.toString().trim()
                    val description = etTaskDescription.text.toString().trim()
                    
                    if (title.isNotEmpty()) {
                        val updatedTask = task.copy(
                            Title = title,
                            Description = description.ifEmpty { null },
                            UpdatedAt = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp()
                        )
                        saveTask(updatedTask)
                    }
                }
            }
        
            private fun saveTask(updatedTask: Task) {
                lifecycleScope.launch {
                    try {
                        val db = AppDb.get(this@FragmentTaskExtensionActivity)
                        withContext(Dispatchers.IO) {
                            db.taskDao().update(updatedTask)
                        }
                        currentTask = updatedTask
                    } catch (e: Exception) {
                        android.util.Log.e("FragmentTaskExtensionActivity", "Error saving task", e)
                        Toast.makeText(
                            this@FragmentTaskExtensionActivity,
                            "Error saving changes",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        
            // Subtask management functions
            private fun loadSubtasks() {
                lifecycleScope.launch {
                    try {
                        val db = AppDb.get(this@FragmentTaskExtensionActivity)
                        val loadedSubtasks = withContext(Dispatchers.IO) {
                            db.subTaskDao().list(taskId, userId)
                        }
                        
                        subtasks.clear()
                        subtasks.addAll(loadedSubtasks)
                        displaySubtasks()
                    } catch (e: Exception) {
                        android.util.Log.e("FragmentTaskExtensionActivity", "Error loading subtasks", e)
                    }
                }
            }
        
                private fun displaySubtasks() {
                    // Update items count
                    val completedCount = subtasks.count { it.IsCompleted == "Yes" }
                    tvItemsCount.text = "$completedCount/${subtasks.size} items"
                    
                    if (subtasks.isEmpty()) {
                        tvNoSubtasks.visibility = View.VISIBLE
                        subtasksContainer.visibility = View.GONE
                    } else {
                        tvNoSubtasks.visibility = View.GONE
                        subtasksContainer.visibility = View.VISIBLE
                        subtaskAdapter.notifyDataSetChanged()
                    }
                }
            
                private fun addSubtask() {
                    val subtaskName = etSubtaskInput.text.toString().trim()
                    
                    if (subtaskName.isEmpty()) {
                        Toast.makeText(this, "Please enter a subtask name", Toast.LENGTH_SHORT).show()
                        return
                    }
                    
                    lifecycleScope.launch {
                        try {
                            val db = AppDb.get(this@FragmentTaskExtensionActivity)
                            
                            // Get the next available SubTask_ID for this task and user
                            val nextSubtaskId = withContext(Dispatchers.IO) {
                                db.subTaskDao().getNextSubtaskId(taskId, userId)
                            }
                            
                            val newSubtask = SubTask(
                                SubTask_ID = nextSubtaskId,
                                Task_ID = taskId,
                                User_ID = userId,
                                Name = subtaskName,
                                IsCompleted = "No",
                                CreatedAt = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp()
                            )
                            
                            withContext(Dispatchers.IO) {
                                db.subTaskDao().insert(newSubtask)
                            }
                            
                            // Clear input
                            etSubtaskInput.text.clear()
                            btnAddSubtask.visibility = View.GONE
                            
                            // Reload subtasks
                            loadSubtasks()
                            
                            Toast.makeText(
                                this@FragmentTaskExtensionActivity,
                                "Subtask added",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            android.util.Log.e("FragmentTaskExtensionActivity", "Error adding subtask", e)
                            Toast.makeText(
                                this@FragmentTaskExtensionActivity,
                                "Error adding subtask",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            
                private fun updateSubtask(subtask: SubTask) {
                    lifecycleScope.launch {
                        try {
                            val db = AppDb.get(this@FragmentTaskExtensionActivity)
                            withContext(Dispatchers.IO) {
                                db.subTaskDao().update(subtask)
                            }
                            
                            // Update local list
                            val index = subtasks.indexOfFirst { it.SubTask_ID == subtask.SubTask_ID }
                            if (index >= 0) {
                                subtasks[index] = subtask
                                subtaskAdapter.notifyItemChanged(index) // Notify adapter for specific item change
                            }
                            
                            // Update items count display
                            val completedCount = subtasks.count { it.IsCompleted == "Yes" }
                            tvItemsCount.text = "$completedCount/${subtasks.size} items"                    } catch (e: Exception) {
                        android.util.Log.e("FragmentTaskExtensionActivity", "Error updating subtask", e)
                    }
                }
            }
        
            private fun showDeleteSubtaskConfirmation(subtask: SubTask) {
                AlertDialog.Builder(this)
                    .setTitle("Delete Subtask")
                    .setMessage("Are you sure you want to delete this subtask?")
                    .setPositiveButton("Delete") { _, _ ->
                        deleteSubtask(subtask)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        
            private fun deleteSubtask(subtask: SubTask) {
                lifecycleScope.launch {
                    try {
                        val db = AppDb.get(this@FragmentTaskExtensionActivity)
                        withContext(Dispatchers.IO) {
                            db.subTaskDao().delete(subtask)
                        }
                        
                        // Remove from local list
                        subtasks.remove(subtask)
                        displaySubtasks()
                        
                        Toast.makeText(
                            this@FragmentTaskExtensionActivity,
                            "Subtask deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        android.util.Log.e("FragmentTaskExtensionActivity", "Error deleting subtask", e)
                        Toast.makeText(
                            this@FragmentTaskExtensionActivity,
                            "Error deleting subtask",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        
            override fun onPause() {
                super.onPause()
                // Save task changes synchronously to ensure data is persisted before returning to task list
                runBlocking {
                    saveTaskChangesSynchronously()
                }
                // Set result to indicate task was modified
                setResult(RESULT_OK)
            }
            
            private suspend fun saveTaskChangesSynchronously() {
                currentTask?.let { task ->
                    val title = etTaskTitle.text.toString().trim()
                    val description = etTaskDescription.text.toString().trim()
                    
                    if (title.isNotEmpty()) {
                        // Merge current task (which may have category/priority/date changes)
                        // with any title/description edits
                        val updatedTask = task.copy(
                            Title = title,
                            Description = description.ifEmpty { null },
                            UpdatedAt = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp()
                        )
                        saveTaskSynchronously(updatedTask)
                    }
                }
            }
            
            private suspend fun saveTaskSynchronously(updatedTask: Task) {
                try {
                    val db = AppDb.get(this@FragmentTaskExtensionActivity)
                    withContext(Dispatchers.IO) {
                        db.taskDao().update(updatedTask)
                    }
                    currentTask = updatedTask
                } catch (e: Exception) {
                    android.util.Log.e("FragmentTaskExtensionActivity", "Error saving task", e)
                }
            }
        
            override fun onSupportNavigateUp(): Boolean {
                setResult(RESULT_OK)
                finish()
                return true
            }
        
            override fun onCreateOptionsMenu(menu: Menu?): Boolean {
                menuInflater.inflate(R.menu.menu_task_extension, menu)
                return true
            }
        
            override fun onOptionsItemSelected(item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.action_more -> {
                        showTaskActionsBottomSheet()
                        true
                    }
                    else -> super.onOptionsItemSelected(item)
                }
            }
        
            private fun showTaskActionsBottomSheet() {
                val bottomSheetDialog = BottomSheetDialog(this)
                val view = layoutInflater.inflate(R.layout.bottom_sheet_task_actions, null)
                
                // Set up click listeners for each action
                view.findViewById<LinearLayout>(R.id.action_mark_completed).setOnClickListener {
                    bottomSheetDialog.dismiss()
                    markTaskAsCompleted()
                }
                
                view.findViewById<LinearLayout>(R.id.action_wont_do).setOnClickListener {
                    bottomSheetDialog.dismiss()
                    markTaskAsWontDo()
                }
                
                view.findViewById<LinearLayout>(R.id.action_delete_task).setOnClickListener {
                    bottomSheetDialog.dismiss()
                    deleteTask()
                }
                
                bottomSheetDialog.setContentView(view)
                bottomSheetDialog.show()
            }
        
            private fun deleteTask() {
                currentTask?.let { task ->
                    AlertDialog.Builder(this)
                        .setTitle("Delete Task")
                        .setMessage("Are you sure you want to delete '${task.Title}'? This action cannot be undone.")
                        .setPositiveButton("Delete") { _, _ ->
                            lifecycleScope.launch {
                                try {
                                    val db = AppDb.get(this@FragmentTaskExtensionActivity)
                                    withContext(Dispatchers.IO) {
                                        db.taskDao().delete(task)
                                    }
                                    
                                    Toast.makeText(
                                        this@FragmentTaskExtensionActivity,
                                        "Task deleted successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    
                                    setResult(RESULT_OK)
                                    finish()
                                } catch (e: Exception) {
                                    android.util.Log.e("FragmentTaskExtensionActivity", "Error deleting task", e)
                                    Toast.makeText(
                                        this@FragmentTaskExtensionActivity,
                                        "Error deleting task",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        
            private fun markTaskAsCompleted() {
                currentTask?.let { task ->
                    AlertDialog.Builder(this)
                        .setTitle("Mark as Done")
                        .setMessage("Are you sure you want to mark '${task.Title}' as Done?")
                        .setPositiveButton("Yes") { _, _ ->
                            lifecycleScope.launch {
                                try {
                                    val db = AppDb.get(this@FragmentTaskExtensionActivity)
                                    val updatedAt = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp()
                                    withContext(Dispatchers.IO) {
                                        db.taskDao().updateTaskStatus(
                                            task.User_ID,
                                            task.Task_ID,
                                            "Completed",
                                            updatedAt
                                        )
                                        db.taskDao().updateTaskProgress(task.User_ID, task.Task_ID, 100, updatedAt)
                                    }
                                    
                                    // Update the current task status
                                    currentTask = task.copy(
                                        Status = Status.Completed,
                                        TaskProgress = 100,
                                        UpdatedAt = updatedAt
                                    )
                                    
                                    // Update the status display
                                    updateTaskStatus(Status.Completed)
                                    
                                    Toast.makeText(
                                        this@FragmentTaskExtensionActivity,
                                        "✅ Task marked as Completed!",
                                        Toast.LENGTH_SHORT
                                    ).show()                            
                                    
                                    // Trigger global statistics broadcast
                                    lifecycleScope.launch {
                                        val db = com.cheermateapp.data.db.AppDb.get(this@FragmentTaskExtensionActivity)
                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                            com.cheermateapp.util.StatisticsBroadcaster.refreshFromDatabase(db, task.User_ID)
                                        }
                                    }
                                    
                                    // Set result to notify caller that task was modified
                                    setResult(RESULT_OK)
                                } catch (e: Exception) {
                                    android.util.Log.e("FragmentTaskExtensionActivity", "Error marking task as completed", e)
                                    Toast.makeText(
                                        this@FragmentTaskExtensionActivity,
                                        "Error updating task",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        
            private fun showSnoozeDialog() {
                val options = arrayOf("⏰ 1 hour", "⏰ 1 day", "⏰ 3 days", "⏰ 1 week", "⏰ Custom")
                
                AlertDialog.Builder(this)
                    .setTitle("Snooze Task")
                    .setItems(options) { _, which ->
                        when (which) {
                            0 -> snoozeTask(1, Calendar.HOUR_OF_DAY)
                            1 -> snoozeTask(1, Calendar.DAY_OF_YEAR)
                            2 -> snoozeTask(3, Calendar.DAY_OF_YEAR)
                            3 -> snoozeTask(7, Calendar.DAY_OF_YEAR)
                            4 -> showCustomSnoozeDialog()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        
            private fun snoozeTask(amount: Int, field: Int) {
                currentTask?.let { task ->
                    val calendar = Calendar.getInstance()
                    calendar.add(field, amount)
                    
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val newDueDate = dateFormat.format(calendar.time)
                    
                    val updatedTask = task.copy(
                        DueDate = newDueDate,
                        UpdatedAt = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp()
                    )
                    
                    // Update currentTask immediately to ensure it's saved in onPause
                    currentTask = updatedTask
                    
                    lifecycleScope.launch {
                        try {
                            val db = AppDb.get(this@FragmentTaskExtensionActivity)
                            
                            withContext(Dispatchers.IO) {
                                db.taskDao().update(updatedTask)
                            }
                            
                            updateDueDateButton(newDueDate)
                            
                            val timeText = when (field) {
                                Calendar.HOUR_OF_DAY -> "$amount hour${if (amount != 1) "s" else ""}"
                                else -> "$amount day${if (amount != 1) "s" else ""}"
                            }
                            
                            Toast.makeText(
                                this@FragmentTaskExtensionActivity,
                                "⏰ Task snoozed for $timeText",
                                Toast.LENGTH_SHORT
                            ).show()
                            
                            // Update overdue status
                            if (updatedTask.isOverdue()) {
                                overdueRow.visibility = View.VISIBLE
                                val overdueDays = calculateOverdueDays(updatedTask)
                                overdueText.text = "⏰ Overdue by $overdueDays day${if (overdueDays != 1) "s" else ""}"
                            } else {
                                overdueRow.visibility = View.GONE
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("FragmentTaskExtensionActivity", "Error snoozing task", e)
                            Toast.makeText(
                                this@FragmentTaskExtensionActivity,
                                "Error updating task",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        
            private fun showCustomSnoozeDialog() {
                val calendar = Calendar.getInstance()
                
                val datePickerDialog = DatePickerDialog(
                    this,
                    { _, year, month, dayOfMonth ->
                        val selectedCalendar = Calendar.getInstance()
                        selectedCalendar.set(year, month, dayOfMonth)
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val newDueDate = dateFormat.format(selectedCalendar.time)
                        
                        currentTask?.let { task ->
                            val updatedTask = task.copy(
                                DueDate = newDueDate,
                                UpdatedAt = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp()
                            )
                            
                            // Update currentTask immediately to ensure it's saved in onPause
                            currentTask = updatedTask
                            
                            lifecycleScope.launch {
                                try {
                                    val db = AppDb.get(this@FragmentTaskExtensionActivity)
                                    
                                    withContext(Dispatchers.IO) {
                                        db.taskDao().update(updatedTask)
                                    }
                                    
                                    updateDueDateButton(newDueDate)
                                    
                                    Toast.makeText(
                                        this@FragmentTaskExtensionActivity,
                                        "⏰ Task snoozed until $newDueDate",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    
                                    // Update overdue status
                                    if (updatedTask.isOverdue()) {
                                        overdueRow.visibility = View.VISIBLE
                                        val overdueDays = calculateOverdueDays(updatedTask)
                                        overdueText.text = "⏰ Overdue by $overdueDays day${if (overdueDays != 1) "s" else ""}"
                                    } else {
                                        overdueRow.visibility = View.GONE
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("FragmentTaskExtensionActivity", "Error snoozing task", e)
                                    Toast.makeText(
                                        this@FragmentTaskExtensionActivity,
                                        "Error updating task",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                
                // Set minimum date to today
                datePickerDialog.datePicker.minDate = Date().time
                datePickerDialog.show()
            }
        
            private fun markTaskAsWontDo() {
                currentTask?.let { task ->
                    AlertDialog.Builder(this)
                        .setTitle("Mark as Won't Do")
                        .setMessage("Are you sure you want to mark '${task.Title}' as Won't Do? This will cancel the task.")
                        .setPositiveButton("Yes") { _, _ ->
                            lifecycleScope.launch {
                                try {
                                    val db = AppDb.get(this@FragmentTaskExtensionActivity)
                                    withContext(Dispatchers.IO) {
                                        db.taskDao().updateTaskStatus(
                                            task.User_ID,
                                            task.Task_ID,
                                            "Cancelled"
                                        )
                                    }
                                    
                                    // Update the current task status
                                    currentTask = task.copy(
                                        Status = Status.Cancelled,
                                        UpdatedAt = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp()
                                    )
                                    
                                    // Update the status display
                                    updateTaskStatus(Status.Cancelled)
                                    
                                    Toast.makeText(
                                        this@FragmentTaskExtensionActivity,
                                        "❌ Task marked as Won't Do",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    
                                    // Set result to notify caller that task was modified
                                    setResult(RESULT_OK)
                                } catch (e: Exception) {
                                    android.util.Log.e("FragmentTaskExtensionActivity", "Error marking task as won't do", e)
                                    Toast.makeText(
                                        this@FragmentTaskExtensionActivity,
                                        "Error updating task",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        }
