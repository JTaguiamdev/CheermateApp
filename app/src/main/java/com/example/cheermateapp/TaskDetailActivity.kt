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
                            db.taskDao().updateTaskStatus(task.Task_ID, Status.Completed)
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
        // Show a simple toast for now - full edit functionality can be added later
        Toast.makeText(this, "✏️ Edit task functionality - coming soon!", Toast.LENGTH_SHORT).show()
        
        // In a full implementation, you would open an edit dialog or activity here
        // For minimal changes, we'll keep this simple
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
