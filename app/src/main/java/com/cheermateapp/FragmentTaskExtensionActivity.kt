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
    private lateinit var btnTaskReminder: TextView
    private lateinit var subtaskCard: LinearLayout
    private lateinit var etSubtaskInput: EditText
    private lateinit var btnAddSubtask: Button
    private lateinit var subtasksContainer: androidx.recyclerview.widget.RecyclerView
    private lateinit var subtaskAdapter: SubTaskAdapter
    private lateinit var tvNoSubtasks: TextView
    private lateinit var tvItemsCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    IsCompleted = !subtask.IsCompleted,
                    UpdatedAt = System.currentTimeMillis()
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
        updateDueDateButton(task.DueAt)

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

    private fun showCategoryDialog() {
        val categories = arrayOf("💼 Work", "👤 Personal", "🛒 Shopping", "📤 Others")
        val categoryValues = arrayOf(Category.Work, Category.Personal, Category.Shopping, Category.Others)

        AlertDialog.Builder(this)
            .setTitle("Select Category")
            .setItems(categories) { _, which ->
                currentTask?.let { task ->
                    val updatedTask = task.copy(
                        Category = categoryValues[which],
                        UpdatedAt = System.currentTimeMillis()
                    )
                    // Update currentTask immediately to ensure it's saved in onPause
                    currentTask = updatedTask
                    saveTask(updatedTask)
                    updateCategoryButton(categoryValues[which])
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
                    val updatedTask = task.copy(
                        Priority = priorityValues[which],
                        UpdatedAt = System.currentTimeMillis()
                    )
                    // Update currentTask immediately to ensure it's saved in onPause
                    currentTask = updatedTask
                    saveTask(updatedTask)
                    updatePriorityButton(priorityValues[which])
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
        currentTask?.DueAt?.let { dueAt ->
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
                DueAt = newDueDate,
                UpdatedAt = System.currentTimeMillis()
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
        btnTaskReminder.text = "⏰ $reminderText"
        
        // ✅ Save reminder to database
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
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                val timeString = timeFormat.format(calendar.time)
                btnTaskReminder.text = "⏰ At $timeString"
                
                // ✅ Save reminder to database
                currentTask?.let { task ->
                    saveReminderToDatabase(task, ReminderType.AT_SPECIFIC_TIME, calendar.timeInMillis)
                }
                
                Toast.makeText(this, "Reminder set at $timeString", Toast.LENGTH_SHORT).show()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        
        timePickerDialog.show()
    }

    // ✅ FIXED: Save or update reminder in TaskReminder table (prevents duplication)
    private fun saveReminderToDatabase(task: Task, reminderType: ReminderType, specificTime: Long? = null) {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentTaskExtensionActivity)
                
                withContext(Dispatchers.IO) {
                    // Calculate reminder time based on type
                    val remindAt = when (reminderType) {
                        ReminderType.TEN_MINUTES_BEFORE -> {
                            task.getDueDate()?.time?.minus(10 * 60 * 1000) ?: System.currentTimeMillis()
                        }
                        ReminderType.THIRTY_MINUTES_BEFORE -> {
                            task.getDueDate()?.time?.minus(30 * 60 * 1000) ?: System.currentTimeMillis()
                        }
                        ReminderType.AT_SPECIFIC_TIME -> {
                            specificTime ?: System.currentTimeMillis()
                        }
                    }
                    
                    // ✅ FIX: Check for existing active reminders first
                    val existingReminders = db.taskReminderDao().activeForTask(task.Task_ID, task.User_ID)
                    
                    if (existingReminders.isNotEmpty()) {
                        // Update the first existing reminder instead of creating a new one
                        val existingReminder = existingReminders.first()
                        val updatedReminder = existingReminder.copy(
                            RemindAt = remindAt,
                            ReminderType = reminderType,
                            UpdatedAt = System.currentTimeMillis()
                        )
                        db.taskReminderDao().update(updatedReminder)
                        android.util.Log.d("FragmentTaskExtension", "✅ Reminder updated: $reminderType at $remindAt")
                    } else {
                        // No existing reminder, create a new one
                        val nextId = db.taskReminderDao().getNextReminderIdForUser(userId)
                        
                        val reminder = TaskReminder(
                            TaskReminder_ID = nextId,
                            Task_ID = task.Task_ID,
                            User_ID = task.User_ID,
                            RemindAt = remindAt,
                            ReminderType = reminderType,
                            IsActive = true,
                            CreatedAt = System.currentTimeMillis(),
                            UpdatedAt = System.currentTimeMillis()
                        )
                        
                        db.taskReminderDao().insert(reminder)
                        android.util.Log.d("FragmentTaskExtension", "✅ Reminder created: $reminderType at $remindAt")
                    }
                }
                
            } catch (e: Exception) {
                android.util.Log.e("FragmentTaskExtension", "Error saving reminder", e)
                Toast.makeText(
                    this@FragmentTaskExtensionActivity,
                    "Error saving reminder: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ✅ NEW: Load and display existing reminder for the task
    private fun loadTaskReminder(task: Task) {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentTaskExtensionActivity)
                
                val reminders = withContext(Dispatchers.IO) {
                    db.taskReminderDao().activeForTask(task.Task_ID, task.User_ID)
                }
                
                if (reminders.isNotEmpty()) {
                    val reminder = reminders.first()
                    
                    // Display reminder based on type
                    val reminderText = when (reminder.ReminderType) {
                        ReminderType.TEN_MINUTES_BEFORE -> "⏰ 10 minutes before"
                        ReminderType.THIRTY_MINUTES_BEFORE -> "⏰ 30 minutes before"
                        ReminderType.AT_SPECIFIC_TIME -> {
                            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                            val time = Date(reminder.RemindAt)
                            "⏰ At ${timeFormat.format(time)}"
                        }
                        null -> "⏰ Reminder"
                    }
                    
                    btnTaskReminder.text = reminderText
                    
                    android.util.Log.d("FragmentTaskExtension", "✅ Loaded reminder: $reminderText")
                } else {
                    btnTaskReminder.text = "⏰ Reminder"
                }
                
            } catch (e: Exception) {
                android.util.Log.e("FragmentTaskExtension", "Error loading reminder", e)
                btnTaskReminder.text = "⏰ Reminder"
            }
        }
    }


    private fun calculateOverdueDays(task: Task): Int {
        val dueDate = task.getDueDate()
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
                    UpdatedAt = System.currentTimeMillis()
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
        val completedCount = subtasks.count { it.IsCompleted }
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
                
                // Get the next available Subtask_ID for this task and user
                val nextSubtaskId = withContext(Dispatchers.IO) {
                    db.subTaskDao().getNextSubtaskId(taskId, userId)
                }
                
                val newSubtask = SubTask(
                    Subtask_ID = nextSubtaskId,
                    Task_ID = taskId,
                    User_ID = userId,
                    Name = subtaskName,
                    IsCompleted = false,
                    SortOrder = subtasks.size,
                    CreatedAt = System.currentTimeMillis(),
                    UpdatedAt = System.currentTimeMillis()
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
                val index = subtasks.indexOfFirst { it.Subtask_ID == subtask.Subtask_ID }
                if (index >= 0) {
                    subtasks[index] = subtask
                }
                
                // Update items count display
                val completedCount = subtasks.count { it.IsCompleted }
                tvItemsCount.text = "$completedCount/${subtasks.size} items"
            } catch (e: Exception) {
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
                    UpdatedAt = System.currentTimeMillis()
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
        
        view.findViewById<LinearLayout>(R.id.action_snooze).setOnClickListener {
            bottomSheetDialog.dismiss()
            showSnoozeDialog()
        }
        
        view.findViewById<LinearLayout>(R.id.action_wont_do).setOnClickListener {
            bottomSheetDialog.dismiss()
            markTaskAsWontDo()
        }
        
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    private fun markTaskAsCompleted() {
        currentTask?.let { task ->
            AlertDialog.Builder(this)
                .setTitle("Mark as Completed")
                .setMessage("Are you sure you want to mark '${task.Title}' as completed?")
                .setPositiveButton("Yes") { _, _ ->
                    lifecycleScope.launch {
                        try {
                            val db = AppDb.get(this@FragmentTaskExtensionActivity)
                            val updatedAt = System.currentTimeMillis()
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
                                "✅ Task marked as completed!",
                                Toast.LENGTH_SHORT
                            ).show()
                            
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
                DueAt = newDueDate,
                UpdatedAt = System.currentTimeMillis()
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
                        DueAt = newDueDate,
                        UpdatedAt = System.currentTimeMillis()
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
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
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
                                UpdatedAt = System.currentTimeMillis()
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