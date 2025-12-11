package com.cheermateapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cheermateapp.data.model.Priority
import com.cheermateapp.data.model.Status
import com.cheermateapp.data.model.Task
import com.cheermateapp.data.model.getDisplayText

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
        val tvTaskCategory: TextView = itemView.findViewById(R.id.tvTaskCategory)
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
        
        // Set category with icon
        holder.tvTaskCategory.text = task.Category.getDisplayText()
        
        // Set priority with color-coded indicator
        holder.tvTaskPriority.text = task.getPriorityText()
        
        // Set status
        when (task.Status) {
            Status.Pending -> holder.tvTaskStatus.text = "⏳ Pending"
            Status.InProgress -> holder.tvTaskStatus.text = "🔄 In Progress"
Status.Completed -> holder.tvTaskStatus.text = "✅ Completed"
            Status.Cancelled -> holder.tvTaskStatus.text = "❌ Cancelled"
            Status.OverDue -> holder.tvTaskStatus.text = "🔴 Overdue"
        }
        
        // Set due date with proper formatting
                    if (task.Status == Status.Completed) {            holder.tvTaskDueDate.visibility = View.GONE
        } else if (task.DueAt != null) {
            holder.tvTaskDueDate.visibility = View.VISIBLE
            val formattedDate = task.getFormattedDueDateTime()
            holder.tvTaskDueDate.text = if (formattedDate != null) {
                "📅 Due: $formattedDate"
            } else {
                "📅 Due: ${task.DueAt}"
            }
            holder.tvTaskDueDate.setTextColor(0xFFE53E3E.toInt()) // Red
        } else {
            holder.tvTaskDueDate.visibility = View.VISIBLE
            holder.tvTaskDueDate.text = "📅 No due date"
            holder.tvTaskDueDate.setTextColor(0xFFE53E3E.toInt()) // Red
        }
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}
