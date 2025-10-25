package com.example.cheermateapp

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cheermateapp.data.model.Task
import com.example.cheermateapp.data.model.Priority
import com.example.cheermateapp.data.model.Category
import com.example.cheermateapp.data.model.getPriorityColor
import com.example.cheermateapp.data.model.getDisplayText
import java.text.SimpleDateFormat
import java.util.*

class TaskListAdapter(
    private var tasks: List<Task>,
    private val onTaskClick: (Task) -> Unit,
    private val onTaskUpdate: (Task, Category?, Priority?, String?) -> Unit
) : RecyclerView.Adapter<TaskListAdapter.TaskViewHolder>() {

    private var expandedPosition = -1

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Collapsed view elements
        val tvTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvTaskDescription)
        val tvCategory: TextView = itemView.findViewById(R.id.tvTaskCategory)
        val tvPriority: TextView = itemView.findViewById(R.id.tvTaskPriority)
        val tvStatus: TextView = itemView.findViewById(R.id.tvTaskStatus)
        val tvDueDate: TextView = itemView.findViewById(R.id.tvTaskDueDate)
        val layoutCollapsed: View = itemView.findViewById(R.id.layoutCollapsed)
        
        // Expanded view elements
        val layoutExpanded: View = itemView.findViewById(R.id.layoutExpanded)
        val btnCategoryWork: TextView = itemView.findViewById(R.id.btnCategoryWork)
        val btnCategoryPersonal: TextView = itemView.findViewById(R.id.btnCategoryPersonal)
        val btnCategoryShopping: TextView = itemView.findViewById(R.id.btnCategoryShopping)
        val btnCategoryOthers: TextView = itemView.findViewById(R.id.btnCategoryOthers)
        val btnPriorityLow: TextView = itemView.findViewById(R.id.btnPriorityLow)
        val btnPriorityMedium: TextView = itemView.findViewById(R.id.btnPriorityMedium)
        val btnPriorityHigh: TextView = itemView.findViewById(R.id.btnPriorityHigh)
        val btnDueDateToday: TextView = itemView.findViewById(R.id.btnDueDateToday)
        val btnDueDateTomorrow: TextView = itemView.findViewById(R.id.btnDueDateTomorrow)
        val btnDueDateCustom: TextView = itemView.findViewById(R.id.btnDueDateCustom)
        val btnSaveChanges: TextView = itemView.findViewById(R.id.btnSaveChanges)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task_list, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        val isExpanded = position == expandedPosition

        // Set title
        holder.tvTitle.text = task.Title

        // Set description (show if available)
        if (!task.Description.isNullOrBlank()) {
            holder.tvDescription.text = task.Description
            holder.tvDescription.visibility = View.VISIBLE
        } else {
            holder.tvDescription.visibility = View.GONE
        }

        // Set category with emoji
        holder.tvCategory.text = task.Category.getDisplayText()

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

        // Handle expand/collapse (disabled - no longer used)
        holder.layoutCollapsed.visibility = View.VISIBLE
        holder.layoutExpanded.visibility = View.GONE

        // Click on item view to open FragmentTaskExtensionActivity
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, FragmentTaskExtensionActivity::class.java).apply {
                putExtra(FragmentTaskExtensionActivity.EXTRA_TASK_ID, task.Task_ID)
                putExtra(FragmentTaskExtensionActivity.EXTRA_USER_ID, task.User_ID)
            }
            context.startActivity(intent)
        }
    }

    private fun highlightCategoryButton(holder: TaskViewHolder, category: Category) {
        val buttons = listOf(
            holder.btnCategoryWork to Category.Work,
            holder.btnCategoryPersonal to Category.Personal,
            holder.btnCategoryShopping to Category.Shopping,
            holder.btnCategoryOthers to Category.Others
        )
        
        buttons.forEach { (button, cat) ->
            if (cat == category) {
                button.setBackgroundColor(Color.parseColor("#FFA667C3"))
            } else {
                button.setBackgroundResource(R.drawable.bg_chip_glass)
            }
        }
    }

    private fun highlightPriorityButton(holder: TaskViewHolder, priority: Priority) {
        val buttons = listOf(
            holder.btnPriorityLow to Priority.Low,
            holder.btnPriorityMedium to Priority.Medium,
            holder.btnPriorityHigh to Priority.High
        )
        
        buttons.forEach { (button, pri) ->
            if (pri == priority) {
                button.setBackgroundColor(Color.parseColor("#FFA667C3"))
            } else {
                button.setBackgroundResource(R.drawable.bg_chip_glass)
            }
        }
    }

    private fun highlightDueDateButton(holder: TaskViewHolder, selected: String) {
        val allButtons = listOf(holder.btnDueDateToday, holder.btnDueDateTomorrow, holder.btnDueDateCustom)
        allButtons.forEach { it.setBackgroundResource(R.drawable.bg_chip_glass) }
        
        when (selected) {
            "today" -> holder.btnDueDateToday.setBackgroundColor(Color.parseColor("#FFA667C3"))
            "tomorrow" -> holder.btnDueDateTomorrow.setBackgroundColor(Color.parseColor("#FFA667C3"))
            "custom" -> holder.btnDueDateCustom.setBackgroundColor(Color.parseColor("#FFA667C3"))
        }
    }

    private fun showCustomDatePicker(holder: TaskViewHolder, task: Task, onDateSelected: (String) -> Unit) {
        val context = holder.itemView.context
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
            context,
            R.style.CustomDatePickerTheme,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                onDateSelected(dateFormat.format(selectedCalendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        datePickerDialog.show()
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        expandedPosition = -1
        notifyDataSetChanged()
    }
}
