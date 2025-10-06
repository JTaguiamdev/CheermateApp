package com.example.cheermateapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.cheermateapp.data.db.AppDb
import com.example.cheermateapp.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private lateinit var btnTaskCategory: TextView
    private lateinit var btnTaskPriority: TextView
    private lateinit var btnTaskDueDate: TextView
    private lateinit var btnTaskReminder: TextView
    private lateinit var subtaskCard: LinearLayout
    private lateinit var etSubtaskInput: EditText
    private lateinit var btnAddSubtask: Button
    private lateinit var subtasksContainer: LinearLayout
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

        // Setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
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

        // Update category button
        updateCategoryButton(task.Category)

        // Update priority button
        updatePriorityButton(task.Priority)

        // Update due date button
        updateDueDateButton(task.DueAt)

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
                Toast.makeText(this, "Reminder set at $timeString", Toast.LENGTH_SHORT).show()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        
        timePickerDialog.show()
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
        subtasksContainer.removeAllViews()
        
        // Update items count
        val completedCount = subtasks.count { it.IsCompleted }
        tvItemsCount.text = "$completedCount/${subtasks.size} items"
        
        if (subtasks.isEmpty()) {
            tvNoSubtasks.visibility = View.VISIBLE
            subtasksContainer.visibility = View.GONE
        } else {
            tvNoSubtasks.visibility = View.GONE
            subtasksContainer.visibility = View.VISIBLE
            
            subtasks.forEach { subtask ->
                val subtaskView = createSubtaskView(subtask)
                subtasksContainer.addView(subtaskView)
            }
        }
    }

    private fun createSubtaskView(subtask: SubTask): View {
        val inflater = LayoutInflater.from(this)
        val subtaskView = inflater.inflate(R.layout.item_subtask, null, false)
        
        val checkbox = subtaskView.findViewById<CheckBox>(R.id.cbSubTask)
        val textView = subtaskView.findViewById<TextView>(R.id.tvSubTaskName)
        val deleteButton = subtaskView.findViewById<ImageView>(R.id.btnDeleteSubTask)
        
        // Set subtask data
        textView.text = subtask.Name
        checkbox.isChecked = subtask.IsCompleted
        
        // Apply strikethrough if completed
        if (subtask.IsCompleted) {
            textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            textView.alpha = 0.6f
        } else {
            textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            textView.alpha = 1.0f
        }
        
        // Checkbox click listener
        checkbox.setOnCheckedChangeListener { _, isChecked ->
            val updatedSubtask = subtask.copy(
                IsCompleted = isChecked,
                UpdatedAt = System.currentTimeMillis()
            )
            updateSubtask(updatedSubtask)
            
            // Apply/remove strikethrough
            if (isChecked) {
                textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                textView.alpha = 0.6f
            } else {
                textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                textView.alpha = 1.0f
            }
        }
        
        // Delete button click listener
        deleteButton.setOnClickListener {
            showDeleteSubtaskConfirmation(subtask)
        }
        
        return subtaskView
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
        saveTaskChanges()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}