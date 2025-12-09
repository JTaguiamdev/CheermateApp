package com.cheermateapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.cheermateapp.data.model.SubTask

/**
 * Adapter for displaying and managing subtasks in a RecyclerView
 */
class SubTaskAdapter(
    private val subTasks: MutableList<SubTask>,
    private val onSubTaskToggle: (SubTask) -> Unit,
    private val onSubTaskDelete: (SubTask) -> Unit
) : RecyclerView.Adapter<SubTaskAdapter.SubTaskViewHolder>() {

    class SubTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.cbSubTask)
        val tvSubTaskName: TextView = itemView.findViewById(R.id.tvSubTaskName)
        val btnDeleteSubTask: ImageView = itemView.findViewById(R.id.btnDeleteSubTask)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubTaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subtask, parent, false)
        return SubTaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubTaskViewHolder, position: Int) {
        val subTask = subTasks[position]

        // Set subtask name and completion state
        holder.tvSubTaskName.text = subTask.Name
        holder.checkBox.isChecked = subTask.IsCompleted

        // Apply strikethrough if completed
        if (subTask.IsCompleted) {
            holder.tvSubTaskName.paintFlags = holder.tvSubTaskName.paintFlags or
                    android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvSubTaskName.alpha = 0.6f
        } else {
            holder.tvSubTaskName.paintFlags = holder.tvSubTaskName.paintFlags and
                    android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvSubTaskName.alpha = 1.0f
        }

        // Set up click listeners
        holder.checkBox.setOnClickListener {
            onSubTaskToggle(subTask)
        }

        holder.btnDeleteSubTask.setOnClickListener {
            onSubTaskDelete(subTask)
        }
    }

    override fun getItemCount(): Int = subTasks.size

    /**
     * Update the subtask list
     */
    fun updateSubTasks(newSubTasks: List<SubTask>) {
        subTasks.clear()
        subTasks.addAll(newSubTasks)
        notifyDataSetChanged()
    }

    /**
     * Add a new subtask
     */
    fun addSubTask(subTask: SubTask) {
        subTasks.add(subTask)
        notifyItemInserted(subTasks.size - 1)
    }

    /**
     * Remove a subtask
     */
    fun removeSubTask(subTask: SubTask) {
        val position = subTasks.indexOf(subTask)
        if (position != -1) {
            subTasks.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    /**
     * Update a subtask
     */
    fun updateSubTask(subTask: SubTask) {
        val position = subTasks.indexOfFirst { it.SubTask_ID == subTask.SubTask_ID }
        if (position != -1) {
            subTasks[position] = subTask
            notifyItemChanged(position)
        }
    }

    /**
     * Get completion percentage
     */
    fun getCompletionPercentage(): Int {
        if (subTasks.isEmpty()) return 0
        val completed = subTasks.count { it.IsCompleted }
        return (completed * 100) / subTasks.size
    }
}
