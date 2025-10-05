package com.example.cheermateapp

import android.app.DatePickerDialog
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

    // UI Elements
    private lateinit var overdueRow: LinearLayout
    private lateinit var overdueText: TextView
    private lateinit var postponeButton: Button
    private lateinit var taskCard: androidx.cardview.widget.CardView
    private lateinit var taskTitle: TextView
    private lateinit var taskDesc: TextView
    private lateinit var subtaskCard: androidx.cardview.widget.CardView

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
        loadTaskDetails()
    }

    private fun initializeViews() {
        overdueRow = findViewById(R.id.overdue_row)
        overdueText = findViewById(R.id.overdue_text)
        postponeButton = findViewById(R.id.postpone_button)
        taskCard = findViewById(R.id.task_card)
        taskTitle = findViewById(R.id.task_title)
        taskDesc = findViewById(R.id.task_desc)
        subtaskCard = findViewById(R.id.subtask_card)

        // Postpone button click handler
        postponeButton.setOnClickListener {
            currentTask?.let { showPostponeDialog(it) }
        }

        // Enable back navigation on task card click (go back)
        taskCard.setOnClickListener {
            finish()
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
        taskTitle.text = task.Title

        // Set description
        if (!task.Description.isNullOrBlank()) {
            taskDesc.text = task.Description
            taskDesc.visibility = View.VISIBLE
        } else {
            taskDesc.visibility = View.GONE
        }

        // Update category
        val categoryTextView = findViewById<TextView>(R.id.task_category)
        categoryTextView.text = when (task.Category) {
            Category.Work -> "📋 Work"
            Category.Personal -> "👤 Personal"
            Category.Shopping -> "🛒 Shopping"
            Category.Others -> "📌 Others"
        }

        // Update priority
        val priorityTextView = findViewById<TextView>(R.id.task_priority)
        priorityTextView.text = when (task.Priority) {
            Priority.High -> "🔴 High"
            Priority.Medium -> "🟡 Medium"
            Priority.Low -> "🟢 Low"
        }

        // Update due date
        val dueDateTextView = findViewById<TextView>(R.id.task_due_date)
        if (task.DueAt != null) {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = dateFormat.parse(task.DueAt)
                val shortFormat = SimpleDateFormat("📅 MMM dd, yyyy", Locale.getDefault())
                dueDateTextView.text = shortFormat.format(date)
                dueDateTextView.visibility = View.VISIBLE
            } catch (e: Exception) {
                dueDateTextView.text = "📅 ${task.DueAt}"
                dueDateTextView.visibility = View.VISIBLE
            }
        } else {
            dueDateTextView.visibility = View.GONE
        }

        // Check if task is overdue
        if (task.isOverdue()) {
            overdueRow.visibility = View.VISIBLE
            val overdueDays = calculateOverdueDays(task)
            overdueText.text = "⏰ Overdue by $overdueDays day${if (overdueDays != 1) "s" else ""}"
        } else {
            overdueRow.visibility = View.GONE
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

    private fun showPostponeDialog(task: Task) {
        val options = arrayOf("1 day", "3 days", "1 week", "Custom date")
        
        AlertDialog.Builder(this)
            .setTitle("Postpone Task")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> postponeTask(task, 1)
                    1 -> postponeTask(task, 3)
                    2 -> postponeTask(task, 7)
                    3 -> showCustomDatePicker(task)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun postponeTask(task: Task, days: Int) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val newDueDate = dateFormat.format(calendar.time)
        
        updateTaskDueDate(task, newDueDate)
    }

    private fun showCustomDatePicker(task: Task) {
        val calendar = Calendar.getInstance()
        
        // Parse current due date if available
        if (task.DueAt != null) {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                dateFormat.parse(task.DueAt)?.let { calendar.time = it }
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
                updateTaskDueDate(task, newDueDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        datePickerDialog.show()
    }

    private fun updateTaskDueDate(task: Task, newDueDate: String) {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentTaskExtensionActivity)
                val updatedTask = task.copy(
                    DueAt = newDueDate,
                    UpdatedAt = System.currentTimeMillis()
                )
                
                withContext(Dispatchers.IO) {
                    db.taskDao().updateTask(updatedTask)
                }
                
                currentTask = updatedTask
                displayTaskDetails(updatedTask)
                
                Toast.makeText(
                    this@FragmentTaskExtensionActivity,
                    "Task postponed successfully",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                android.util.Log.e("FragmentTaskExtensionActivity", "Error updating task", e)
                Toast.makeText(
                    this@FragmentTaskExtensionActivity,
                    "Error postponing task",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}