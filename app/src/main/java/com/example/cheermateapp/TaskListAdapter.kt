package com.example.cheermateapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cheermateapp.data.model.Task
import com.example.cheermateapp.data.model.getPriorityColor
import java.text.SimpleDateFormat
import java.util.*

class TaskListAdapter(
    private var tasks: List<Task>,
    private val onTaskClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskListAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val priorityIndicator: View = itemView.findViewById(R.id.priorityIndicator)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvTaskDescription)
        val tvPriority: TextView = itemView.findViewById(R.id.tvTaskPriority)
        val tvStatus: TextView = itemView.findViewById(R.id.tvTaskStatus)
        val tvDueDate: TextView = itemView.findViewById(R.id.tvTaskDueDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task_list, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        // Set priority indicator color
        holder.priorityIndicator.setBackgroundColor(task.getPriorityColor())

        // Set title
        holder.tvTitle.text = task.Title

        // Set description (show if available)
        if (!task.Description.isNullOrBlank()) {
            holder.tvDescription.text = task.Description
            holder.tvDescription.visibility = View.VISIBLE
        } else {
            holder.tvDescription.visibility = View.GONE
        }

        // Set priority with emoji
        holder.tvPriority.text = "🎯 ${task.Priority.name}"

        // Set status with emoji
        holder.tvStatus.text = "${task.getStatusEmoji()} ${task.Status.name}"

        // Set due date (shortened format for list)
        if (task.DueAt != null) {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = dateFormat.parse(task.DueAt)
                val shortFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                holder.tvDueDate.text = "📅 ${shortFormat.format(date)}"
                holder.tvDueDate.visibility = View.VISIBLE
            } catch (e: Exception) {
                holder.tvDueDate.text = "📅 ${task.DueAt}"
                holder.tvDueDate.visibility = View.VISIBLE
            }
        } else {
            holder.tvDueDate.visibility = View.GONE
        }

        // Set click listener
        holder.itemView.setOnClickListener {
            onTaskClick(task)
        }
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}
