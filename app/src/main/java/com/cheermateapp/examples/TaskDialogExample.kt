package com.cheermateapp.examples

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cheermateapp.R
import com.cheermateapp.data.db.AppDb
import com.cheermateapp.data.model.Task
import com.cheermateapp.data.model.TaskReminder
import com.cheermateapp.data.model.Status
import com.cheermateapp.util.TaskDialogSpinnerHelper
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Example implementation showing how to use the updated dialog_add_task.xml
 * with icon-enabled spinners.
 * 
 * This is a REFERENCE IMPLEMENTATION. Copy the relevant methods to your
 * MainActivity or FragmentTaskActivity to use the new dialog layout.
 */
class TaskDialogExample : AppCompatActivity() {

    // Example user ID - in a real app, get this from login session
    private var currentUserId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Your activity setup
        
        // Get user ID from intent or shared preferences
        currentUserId = intent.getIntExtra("USER_ID", 1)
    }

    /**
     * Example method showing how to use the dialog_add_task.xml layout
     * with the new icon-enabled spinners
     */
    private fun showAddTaskDialogWithIcons() {
        // Inflate the XML layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        
        // Get references to all input fields
        val etTaskTitle = dialogView.findViewById<TextInputEditText>(R.id.etTaskTitle)
        val etTaskDescription = dialogView.findViewById<TextInputEditText>(R.id.etTaskDescription)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinnerPriority)
        val etDueDate = dialogView.findViewById<TextInputEditText>(R.id.etDueDate)
        val etDueTime = dialogView.findViewById<TextInputEditText>(R.id.etDueTime)
        val spinnerReminder = dialogView.findViewById<Spinner>(R.id.spinnerReminder)
        
        // Set up spinners with icons using the helper
        TaskDialogSpinnerHelper.setupCategorySpinner(this, spinnerCategory)
        TaskDialogSpinnerHelper.setupPrioritySpinner(this, spinnerPriority)
        TaskDialogSpinnerHelper.setupReminderSpinner(this, spinnerReminder)
        
        // Set default due date to today
        val today = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        etDueDate.setText(dateFormat.format(today.time))
        
        // Set up date and time pickers
        etDueDate.setOnClickListener {
            showDatePicker { year, month, day ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day)
                etDueDate.setText(dateFormat.format(calendar.time))
            }
        }
        
        etDueTime.setOnClickListener {
            showTimePicker { hour, minute ->
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                etDueTime.setText(timeFormat.format(calendar.time))
            }
        }
        
        // Create and show the dialog
        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Task")
            .setView(dialogView)
            .setPositiveButton("Create Task", null) // Set to null initially
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.setOnShowListener {
            // Override positive button to prevent auto-dismiss on validation errors
            val createButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            createButton.setOnClickListener {
                // Validate inputs
                val title = etTaskTitle.text.toString().trim()
                val description = etTaskDescription.text.toString().trim()
                val dueDate = etDueDate.text.toString().trim()
                val dueTime = etDueTime.text.toString().trim()
                
                // Get selected values from spinners
                val category = TaskDialogSpinnerHelper.getSelectedCategory(spinnerCategory)
                val priority = TaskDialogSpinnerHelper.getSelectedPriority(spinnerPriority)
                val reminder = TaskDialogSpinnerHelper.getSelectedReminder(spinnerReminder)
                
                // Validation
                if (title.isEmpty()) {
                    Toast.makeText(this, "❌ Task title is required", Toast.LENGTH_SHORT).show()
                    etTaskTitle.error = "Required"
                    etTaskTitle.requestFocus()
                    return@setOnClickListener
                }
                
                if (dueDate.isEmpty()) {
                    Toast.makeText(this, "❌ Due date is required", Toast.LENGTH_SHORT).show()
                    etDueDate.error = "Required"
                    return@setOnClickListener
                }
                
                // Validate reminder requires time
                if (reminder != "None" && dueTime.isEmpty()) {
                    Toast.makeText(
                        this,
                        "❌ Reminder requires a due time to be set",
                        Toast.LENGTH_SHORT
                    ).show()
                    etDueTime.error = "Required for reminder"
                    return@setOnClickListener
                }
                
                // All validations passed - create the task
                createTask(
                    title = title,
                    description = if (description.isEmpty()) null else description,
                    category = category,
                    priority = priority,
                    dueDate = dueDate,
                    dueTime = if (dueTime.isEmpty()) null else dueTime,
                    reminder = reminder
                )
                
                dialog.dismiss()
            }
        }
        
        dialog.show()
    }
    
    /**
     * Example method to create a task with the collected data
     */
    private fun createTask(
        title: String,
        description: String?,
        category: String,
        priority: String,
        dueDate: String,
        dueTime: String?,
        reminder: String
    ) {
        // Convert string values to enum types
        val categoryEnum = when (category) {
            "Work" -> com.cheermateapp.data.model.Category.Work
            "Personal" -> com.cheermateapp.data.model.Category.Personal
            "Shopping" -> com.cheermateapp.data.model.Category.Shopping
            "Others" -> com.cheermateapp.data.model.Category.Others
            else -> com.cheermateapp.data.model.Category.Work
        }
        
        val priorityEnum = when (priority) {
            "High" -> com.cheermateapp.data.model.Priority.High
            "Medium" -> com.cheermateapp.data.model.Priority.Medium
            "Low" -> com.cheermateapp.data.model.Priority.Low
            else -> com.cheermateapp.data.model.Priority.Medium
        }
        
        // Build display message
        val timeText = if (dueTime != null) " at $dueTime" else ""
        val reminderText = if (reminder != "None") "\nReminder: $reminder" else ""
        
        val message = """
            ✅ Task Created!
            
            📝 Title: $title
            📂 Category: $category
            🎯 Priority: $priority
            📅 Due: $dueDate$timeText$reminderText
        """.trimIndent()
        
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        
        // Save to database using Task DAO
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@TaskDialogExample)
                
                // Get next task ID for this user
                val taskId = withContext(Dispatchers.IO) {
                    db.taskDao().getNextTaskIdForUser(currentUserId)
                }
                
                // Create the task object
                val task = Task(
                    Task_ID = taskId,
                    User_ID = currentUserId,
                    Title = title,
                    Description = description,
                    Category = categoryEnum,
                    Priority = priorityEnum,
                    Status = Status.Pending,
                    TaskProgress = 0,
                    DueAt = dueDate,
                    DueTime = dueTime,
                    CreatedAt = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp(),
                    UpdatedAt = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp()
                )
                
                // Insert task into database
                withContext(Dispatchers.IO) {
                    db.taskDao().insert(task)
                }
                
                // Create reminder if requested
                if (reminder != "None" && dueTime != null) {
                    createTaskReminder(taskId, dueDate, dueTime, reminder)
                }
                
                android.util.Log.d("TaskDialogExample", "Task created successfully: $title")
                
            } catch (e: Exception) {
                android.util.Log.e("TaskDialogExample", "Error creating task", e)
                Toast.makeText(
                    this@TaskDialogExample,
                    "Failed to create task: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    /**
     * Create a task reminder
     */
    private suspend fun createTaskReminder(taskId: Int, dueDate: String, dueTime: String, reminderOption: String) {
        try {
            val db = AppDb.get(this@TaskDialogExample)
            
            // Parse due date and time
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val dueDateTime = dateFormat.parse("$dueDate $dueTime")
            
            if (dueDateTime != null) {
                val dueTimeMillis = dueDateTime.time
                
                // Calculate reminder time based on option
                val remindAtMillis = when (reminderOption) {
                    "5 minutes before" -> dueTimeMillis - (5 * 60 * 1000)
                    "15 minutes before" -> dueTimeMillis - (15 * 60 * 1000)
                    "30 minutes before" -> dueTimeMillis - (30 * 60 * 1000)
                    "1 hour before" -> dueTimeMillis - (60 * 60 * 1000)
                    "1 day before" -> dueTimeMillis - (24 * 60 * 60 * 1000)
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
                    User_ID = currentUserId,
                    RemindAt = remindAtMillis,
                    IsActive = true
                )
                
                withContext(Dispatchers.IO) {
                    db.taskReminderDao().insert(reminder)
                }
                
                android.util.Log.d("TaskDialogExample", "✅ Created reminder for task $taskId at $remindAtMillis")
            }
        } catch (e: Exception) {
            android.util.Log.e("TaskDialogExample", "Error creating reminder", e)
        }
    }
    
    /**
     * Show date picker dialog
     */
    private fun showDatePicker(onDateSelected: (year: Int, month: Int, day: Int) -> Unit) {
        val calendar = Calendar.getInstance()
        android.app.DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                onDateSelected(year, month, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis() // Disable past dates
            show()
        }
    }
    
    /**
     * Show time picker dialog
     */
    private fun showTimePicker(onTimeSelected: (hour: Int, minute: Int) -> Unit) {
        val calendar = Calendar.getInstance()
        android.app.TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                onTimeSelected(hourOfDay, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // 24-hour format
        ).show()
    }
}
