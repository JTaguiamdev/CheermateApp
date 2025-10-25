package com.example.cheermateapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cheermateapp.data.model.Priority
import com.example.cheermateapp.data.model.Status
import com.example.cheermateapp.data.model.Task
import java.text.SimpleDateFormat
import java.util.*

class TaskPagerAdapter(
    private var tasks: List<Task>,
    private val onCompleteClick: (Task) -> Unit,
    private val onEditClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskPagerAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutPriorityIndicator: View = itemView.findViewById(R.id.layoutPriorityIndicator)
        val tvTaskTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val tvTaskDescription: TextView = itemView.findViewById(R.id.tvTaskDescription)
        val tvTaskCategory: TextView = itemView.findViewById(R.id.tvTaskCategory)
        val tvTaskPriority: TextView = itemView.findViewById(R.id.tvTaskPriority)
        val tvTaskStatus: TextView = itemView.findViewById(R.id.tvTaskStatus)
        val tvTaskDueDate: TextView = itemView.findViewById(R.id.tvTaskDueDate)
        val tvTaskProgress: TextView = itemView.findViewById(R.id.tvTaskProgress)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val layoutProgress: View = itemView.findViewById(R.id.layoutProgress)
        val btnComplete: TextView = itemView.findViewById(R.id.btnComplete)
        val btnEdit: TextView = itemView.findViewById(R.id.btnEdit)
        val btnDelete: TextView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task_swipeable, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        
        // Set task details
        holder.tvTaskTitle.text = task.Title
        holder.tvTaskDescription.text = task.Description ?: "No description"
        
        // Set category with emoji
        holder.tvTaskCategory.text = when (task.Category) {
            com.example.cheermateapp.data.model.Category.Work -> "📋 Work"
            com.example.cheermateapp.data.model.Category.Personal -> "👤 Personal"
            com.example.cheermateapp.data.model.Category.Shopping -> "🛒 Shopping"
            com.example.cheermateapp.data.model.Category.Others -> "📌 Others"
        }
        
        // Set priority with color-coded indicator
        when (task.Priority) {
            Priority.High -> {
                holder.tvTaskPriority.text = "🔴 High"
                holder.layoutPriorityIndicator.setBackgroundColor(android.graphics.Color.RED)
            }
            Priority.Medium -> {
                holder.tvTaskPriority.text = "🟡 Medium"
                holder.layoutPriorityIndicator.setBackgroundColor(android.graphics.Color.YELLOW)
            }
            Priority.Low -> {
                holder.tvTaskPriority.text = "🟢 Low"
                holder.layoutPriorityIndicator.setBackgroundColor(android.graphics.Color.GREEN)
            }
        }
        
        // Set status
        when (task.Status) {
            Status.Pending -> holder.tvTaskStatus.text = "⏳ Pending"
            Status.InProgress -> holder.tvTaskStatus.text = "🔄 In Progress"
            Status.Completed -> holder.tvTaskStatus.text = "✅ Completed"
            Status.Cancelled -> holder.tvTaskStatus.text = "❌ Cancelled"
            Status.OverDue -> holder.tvTaskStatus.text = "🔴 Overdue"
        }
        
        // Set progress
        holder.progressBar.progress = task.TaskProgress
        holder.tvTaskProgress.text = "${task.TaskProgress}%"
        
        // Show/hide progress bar based on progress value
        if (task.TaskProgress > 0) {
            holder.layoutProgress.visibility = View.VISIBLE
        } else {
            holder.layoutProgress.visibility = View.GONE
        }
        
        // Set due date - hide if task is completed
        if (task.DueAt != null && task.Status != Status.Completed) {
            val dueText = "📅 Due: ${task.DueAt}"
            val timeText = if (!task.DueTime.isNullOrBlank()) " at ${task.DueTime}" else ""
            holder.tvTaskDueDate.text = "$dueText$timeText"
            holder.tvTaskDueDate.visibility = View.VISIBLE
        } else {
            holder.tvTaskDueDate.visibility = View.GONE
        }
        
        // Configure action buttons
        when (task.Status) {
            Status.Completed -> {
                holder.btnComplete.text = "✅ Completed"
                holder.btnComplete.isClickable = false
                holder.btnComplete.alpha = 0.6f
            }
            Status.Cancelled -> {
                holder.btnComplete.text = "❌ Cancelled"
                holder.btnComplete.isClickable = false
                holder.btnComplete.alpha = 0.6f
            }
            else -> {
                holder.btnComplete.text = "✅ Complete"
                holder.btnComplete.isClickable = true
                holder.btnComplete.alpha = 1.0f
            }
        }
        
        // Set click listeners
        holder.btnComplete.setOnClickListener {
            if (task.Status != Status.Completed && task.Status != Status.Cancelled) {
                onCompleteClick(task)
            }
        }
        
        holder.btnEdit.setOnClickListener {
            onEditClick(task)
        }
        
        holder.btnDelete.setOnClickListener {
            onDeleteClick(task)
        }
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}
