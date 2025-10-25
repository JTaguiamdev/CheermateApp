package com.example.cheermateapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.cheermateapp.data.model.Task
import com.example.cheermateapp.data.model.Status
import com.example.cheermateapp.data.model.Priority
import com.example.cheermateapp.data.model.getPriorityColor
import com.example.cheermateapp.data.model.isOverdue

import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onTaskClick: (Task) -> Unit,
    private val onTaskComplete: (Task) -> Unit,
    private val onTaskEdit: (Task) -> Unit,
    private val onTaskDelete: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvTaskDescription)
        val tvStatus: TextView = itemView.findViewById(R.id.tvTaskStatus)
        val tvPriority: TextView = itemView.findViewById(R.id.tvTaskPriority)
        val tvDueDate: TextView = itemView.findViewById(R.id.tvTaskDueDate)
        val tvProgress: TextView? = itemView.findViewById(R.id.tvTaskProgress)
        val progressBar: ProgressBar? = itemView.findViewById(R.id.progressBar)
        val btnComplete: View? = itemView.findViewById(R.id.btnComplete) // Can be TextView or ImageView
        val btnEdit: View? = itemView.findViewById(R.id.btnEdit)
        val btnDelete: View? = itemView.findViewById(R.id.btnDelete)
        val layoutPriorityIndicator: View? = itemView.findViewById(R.id.priorityIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task_list, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        // Set basic info
        holder.tvTitle.text = task.Title
        holder.tvDescription.text = if (task.Description.isNullOrBlank()) "No description" else task.Description
        holder.tvDescription.visibility = if (task.Description.isNullOrBlank()) View.GONE else View.VISIBLE

        // Set status with emoji
        holder.tvStatus.text = "${task.getStatusEmoji()} ${task.Status.name}"

        // Set priority with color
        holder.tvPriority.text = "🎯 ${task.Priority.name}"

        // Set priority indicator color if it exists
        holder.layoutPriorityIndicator?.setBackgroundColor(task.getPriorityColor())

        // Set due date - hide if task is completed
        if (task.DueAt != null && task.Status != Status.Completed) {
            val dueText = formatDueDate(task)
            holder.tvDueDate.text = dueText
            holder.tvDueDate.visibility = View.VISIBLE

            // Highlight overdue tasks
            if (task.isOverdue()) {
                holder.tvDueDate.setTextColor(0xFFE53E3E.toInt()) // Red
            } else if (task.isToday()) {
                holder.tvDueDate.setTextColor(0xFFED8936.toInt()) // Orange
            } else {
                holder.tvDueDate.setTextColor(0x99FFFFFF.toInt()) // Light gray
            }
        } else {
            holder.tvDueDate.visibility = View.GONE
        }

        // Set progress if progress views exist
        holder.tvProgress?.text = "${task.TaskProgress}%"
        holder.progressBar?.progress = task.TaskProgress

        // Set up click listeners
        holder.itemView.setOnClickListener { onTaskClick(task) }

        holder.btnComplete?.setOnClickListener {
            if (task.Status != Status.Completed) {
                onTaskComplete(task)
            }
        }

        holder.btnEdit?.setOnClickListener { onTaskEdit(task) }
        holder.btnDelete?.setOnClickListener { onTaskDelete(task) }

        // Update button states for completed tasks
        if (task.Status == Status.Completed) {
            holder.btnComplete?.alpha = 0.3f
            holder.btnComplete?.isEnabled = false
        } else {
            holder.btnComplete?.alpha = 1.0f
            holder.btnComplete?.isEnabled = true
        }
    }

    override fun getItemCount(): Int = tasks.size

    private fun formatDueDate(task: Task): String {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = dateFormat.parse(task.DueAt!!)

            val today = Calendar.getInstance()
            val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
            val taskDate = Calendar.getInstance().apply { time = date!! }

            when {
                isSameDay(today, taskDate) -> {
                    val timeText = if (task.DueTime != null) " at ${task.DueTime}" else ""
                    "📅 Due Today$timeText"
                }
                isSameDay(tomorrow, taskDate) -> {
                    val timeText = if (task.DueTime != null) " at ${task.DueTime}" else ""
                    "📅 Due Tomorrow$timeText"
                }
                taskDate.before(today) -> {
                    "🔴 Overdue"
                }
                else -> {
                    val displayFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                    val timeText = if (task.DueTime != null) " at ${task.DueTime}" else ""
                    "📅 ${displayFormat.format(date)}$timeText"
                }
            }
        } catch (e: Exception) {
            "📅 ${task.DueAt}"
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    // Method to update the task list
    fun updateTasks(newTasks: List<Task>) {
        android.util.Log.d("TaskAdapter", "🔄 updateTasks called with ${newTasks.size} tasks")

        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()

        android.util.Log.d("TaskAdapter", "✅ TaskAdapter updated, now has ${tasks.size} tasks")
    }

}
