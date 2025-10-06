package com.example.cheermateapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cheermateapp.data.model.Priority
import com.example.cheermateapp.data.model.Status
import com.example.cheermateapp.data.model.Task

class TaskPagerAdapter(
    private var tasks: List<Task>,
    private val onCompleteClick: (Task) -> Unit,
    private val onEditClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskPagerAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTaskTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val tvTaskDescription: TextView = itemView.findViewById(R.id.tvTaskDescription)
        val tvTaskPriority: TextView = itemView.findViewById(R.id.tvTaskPriority)
        val tvTaskStatus: TextView = itemView.findViewById(R.id.tvTaskStatus)
        val tvTaskDueDate: TextView = itemView.findViewById(R.id.tvTaskDueDate)
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
        
        // Set priority with color-coded indicator
        when (task.Priority) {
            Priority.High -> {
                holder.tvTaskPriority.text = "🔴 High"
            }
            Priority.Medium -> {
                holder.tvTaskPriority.text = "🟡 Medium"
            }
            Priority.Low -> {
                holder.tvTaskPriority.text = "🟢 Low"
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
        
        // Set due date
        if (task.DueAt != null) {
            val dueText = "📅 Due: ${task.DueAt}"
            val timeText = if (!task.DueTime.isNullOrBlank()) " at ${task.DueTime}" else ""
            holder.tvTaskDueDate.text = "$dueText$timeText"
        } else {
            holder.tvTaskDueDate.text = "📅 No due date"
        }
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}
