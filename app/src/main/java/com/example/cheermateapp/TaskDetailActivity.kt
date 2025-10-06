package com.example.cheermateapp

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cheermateapp.data.db.AppDb
import com.example.cheermateapp.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.cheermateapp.data.model.Status

class TaskDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TASK_ID = "TASK_ID"
        const val EXTRA_USER_ID = "USER_ID"
    }

    private var taskId: Int = 0
    private var userId: Int = 0
    private var currentTask: Task? = null

    // UI Elements
    private lateinit var btnBack: TextView
    private lateinit var layoutPriorityIndicator: View
    private lateinit var tvTaskTitle: TextView
    private lateinit var tvTaskDescription: TextView
    private lateinit var tvTaskCategory: TextView
    private lateinit var tvTaskPriority: TextView
    private lateinit var tvTaskStatus: TextView
    private lateinit var tvTaskDueDate: TextView
    private lateinit var tvTaskProgress: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressBarLayout: LinearLayout
    private lateinit var btnComplete: TextView
    private lateinit var btnEdit: TextView
    private lateinit var btnDelete: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        // Get data from intent
        taskId = intent.getIntExtra(EXTRA_TASK_ID, 0)
        userId = intent.getIntExtra(EXTRA_USER_ID, 0)

        if (taskId == 0) {
            Toast.makeText(this, "Invalid task", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        loadTaskDetails()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btnBack)
        layoutPriorityIndicator = findViewById(R.id.layoutPriorityIndicator)
        tvTaskTitle = findViewById(R.id.tvTaskTitle)
        tvTaskDescription = findViewById(R.id.tvTaskDescription)
        tvTaskCategory = findViewById(R.id.tvTaskCategory)
        tvTaskPriority = findViewById(R.id.tvTaskPriority)
        tvTaskStatus = findViewById(R.id.tvTaskStatus)
        tvTaskDueDate = findViewById(R.id.tvTaskDueDate)
        tvTaskProgress = findViewById(R.id.tvTaskProgress)
        progressBar = findViewById(R.id.progressBar)
        progressBarLayout = findViewById(R.id.progressBarLayout)
        btnComplete = findViewById(R.id.btnComplete)
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)

        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Action buttons (will be set up after loading task)
        btnComplete.setOnClickListener {
            currentTask?.let { markTaskAsDone(it) }
        }

        btnEdit.setOnClickListener {
            currentTask?.let { showEditTaskDialog(it) }
        }

        btnDelete.setOnClickListener {
            currentTask?.let { showDeleteConfirmation(it) }
        }
    }

    private fun loadTaskDetails() {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@TaskDetailActivity)
                val task = withContext(Dispatchers.IO) {
                    db.taskDao().getTaskById(taskId)
                }

                if (task != null) {
                    currentTask = task
                    displayTaskDetails(task)
                } else {
                    Toast.makeText(this@TaskDetailActivity, "Task not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                android.util.Log.e("TaskDetailActivity", "Error loading task", e)
                Toast.makeText(this@TaskDetailActivity, "Error loading task details", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayTaskDetails(task: Task) {
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

        // Set category with emoji
        tvTaskCategory.text = when (task.Category) {
            Category.Work -> "📋 Work"
            Category.Personal -> "👤 Personal"
            Category.Shopping -> "🛒 Shopping"
            Category.Others -> "📌 Others"
        }

        // Set priority
        tvTaskPriority.text = "🎯 ${task.Priority.name}"

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

        // Update button state based on task status
        when (task.Status) {
            Status.Completed -> {
                btnComplete.text = "✅ Completed"
                btnComplete.alpha = 0.5f
                btnComplete.isEnabled = false
            }
            Status.Cancelled -> {
                btnComplete.text = "❌ Cancelled"
                btnComplete.alpha = 0.5f
                btnComplete.isEnabled = false
            }
            else -> {
                btnComplete.text = "✅ Complete"
                btnComplete.alpha = 1.0f
                btnComplete.isEnabled = true
            }
        }
    }

    private fun markTaskAsDone(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Complete Task")
            .setMessage("Mark '${task.Title}' as completed?")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val db = AppDb.get(this@TaskDetailActivity)
                        withContext(Dispatchers.IO) {
                            db.taskDao().updateTaskStatus(task.User_ID, task.Task_ID, "Completed")
                        }
                        Toast.makeText(this@TaskDetailActivity, "✅ Task marked as done!", Toast.LENGTH_SHORT).show()
                        
                        // Reload task details
                        loadTaskDetails()
                    } catch (e: Exception) {
                        android.util.Log.e("TaskDetailActivity", "Error completing task", e)
                        Toast.makeText(this@TaskDetailActivity, "Error completing task", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
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

            val dateInput = EditText(this)
            dateInput.hint = "YYYY-MM-DD"
            dateInput.setText(task.DueAt ?: "")
            dateInput.setPadding(16, 16, 16, 16)
            dateInput.isFocusable = false
            dateInput.isClickable = true
            dateInput.setOnClickListener {
                showDatePickerDialog(dateInput)
            }
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
            timeInput.setOnClickListener {
                showTimePickerDialog(timeInput)
            }
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
                    Toast.makeText(this@TaskDetailActivity, "⚠️ Please enter a task title", Toast.LENGTH_SHORT).show()
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
            android.util.Log.e("TaskDetailActivity", "Error showing edit task dialog", e)
            Toast.makeText(this, "❌ Edit task dialog error", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showDatePickerDialog(editText: EditText) {
        val calendar = java.util.Calendar.getInstance()

        // Parse current date if exists
        if (editText.text.isNotEmpty()) {
            try {
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
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
                val selectedDate = java.util.Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                editText.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun showTimePickerDialog(editText: EditText) {
        val calendar = java.util.Calendar.getInstance()

        // Parse current time if exists
        if (editText.text.isNotEmpty()) {
            try {
                val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
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
                val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                val selectedTime = java.util.Calendar.getInstance()
                selectedTime.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
                selectedTime.set(java.util.Calendar.MINUTE, minute)
                editText.setText(timeFormat.format(selectedTime.time))
            },
            calendar.get(java.util.Calendar.HOUR_OF_DAY),
            calendar.get(java.util.Calendar.MINUTE),
            true
        )

        timePickerDialog.show()
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
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@TaskDetailActivity)

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

                Toast.makeText(
                    this@TaskDetailActivity,
                    "✅ Task '$title' updated successfully!",
                    Toast.LENGTH_LONG
                ).show()

                // Reload the task to show updated data
                loadTaskDetails()

            } catch (e: Exception) {
                android.util.Log.e("TaskDetailActivity", "Error updating task", e)
                Toast.makeText(this@TaskDetailActivity, "❌ Update Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteConfirmation(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete '${task.Title}'?")
            .setPositiveButton("Delete") { _, _ ->
                deleteTask(task)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTask(task: Task) {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@TaskDetailActivity)
                withContext(Dispatchers.IO) {
                    db.taskDao().softDelete(task.Task_ID, task.User_ID)
                }
                Toast.makeText(this@TaskDetailActivity, "🗑️ Task deleted", Toast.LENGTH_SHORT).show()
                
                // Close the activity and return to task list
                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                android.util.Log.e("TaskDetailActivity", "Error deleting task", e)
                Toast.makeText(this@TaskDetailActivity, "Error deleting task", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
