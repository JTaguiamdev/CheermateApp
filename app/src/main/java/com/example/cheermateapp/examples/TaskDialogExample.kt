package com.example.cheermateapp.examples

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cheermateapp.R
import com.example.cheermateapp.util.TaskDialogSpinnerHelper
import com.google.android.material.textfield.TextInputEditText
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Your activity setup
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
            "Work" -> com.example.cheermateapp.data.model.Category.Work
            "Personal" -> com.example.cheermateapp.data.model.Category.Personal
            "Shopping" -> com.example.cheermateapp.data.model.Category.Shopping
            "Others" -> com.example.cheermateapp.data.model.Category.Others
            else -> com.example.cheermateapp.data.model.Category.Work
        }
        
        val priorityEnum = when (priority) {
            "High" -> com.example.cheermateapp.data.model.Priority.High
            "Medium" -> com.example.cheermateapp.data.model.Priority.Medium
            "Low" -> com.example.cheermateapp.data.model.Priority.Low
            else -> com.example.cheermateapp.data.model.Priority.Medium
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
        
        // TODO: Actually save to database using your existing Task DAO
        // Example:
        // val task = Task.create(
        //     userId = currentUserId,
        //     taskId = getNextTaskId(),
        //     title = title,
        //     description = description,
        //     category = categoryEnum,
        //     priority = priorityEnum,
        //     dueAt = dueDate,
        //     dueTime = dueTime,
        //     status = Status.Pending
        // )
        // 
        // CoroutineScope(Dispatchers.IO).launch {
        //     database.taskDao().insert(task)
        //     if (reminder != "None") {
        //         createTaskReminder(task.Task_ID, dueDate, dueTime!!, reminder)
        //     }
        // }
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
