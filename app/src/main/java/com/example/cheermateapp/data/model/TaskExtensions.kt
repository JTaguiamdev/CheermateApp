package com.example.cheermateapp.data.model

import android.graphics.Color
import java.text.SimpleDateFormat
import java.util.*

// ✅ TASK EXTENSION METHODS FOR UI DISPLAY

/**
 * Get emoji representation of task status
 */
private fun Task.getStatusEmoji(): String {
    return when (this.Status) {
        com.example.cheermateapp.data.model.Status.Completed -> "✅"
        com.example.cheermateapp.data.model.Status.Pending -> "⏳"
        com.example.cheermateapp.data.model.Status.InProgress -> "🔄"
        com.example.cheermateapp.data.model.Status.Cancelled -> "❌"
        else -> "📝"
    }
}

/**
 * Get color for task status
 */
fun Task.getStatusColor(): Int {
    return when (this.Status) {
        com.example.cheermateapp.data.model.Status.Pending -> Color.parseColor("#FFA500") // Orange
        com.example.cheermateapp.data.model.Status.InProgress -> Color.BLUE
        com.example.cheermateapp.data.model.Status.Completed -> Color.GREEN
        com.example.cheermateapp.data.model.Status.Cancelled -> Color.GRAY
        com.example.cheermateapp.data.model.Status.OverDue -> Color.RED
    }
}
/**
 * Get color for task priority
 */
fun Task.getPriorityColor(): Int {
    return when (this.Priority) {
        com.example.cheermateapp.data.model.Priority.High -> Color.RED
        com.example.cheermateapp.data.model.Priority.Medium -> Color.parseColor("#FFA500") // Orange
        com.example.cheermateapp.data.model.Priority.Low -> Color.GREEN
    }
}

/**
 * Get formatted due date and time string
 */
fun Task.getFormattedDueDateTime(): String {
    val dueDate = this.DueAt ?: return "No due date"
    val dueTime = this.DueTime

    return if (dueTime.isNullOrBlank()) {
        dueDate
    } else {
        "$dueDate at $dueTime"
    }
}

/**
 * Check if task is overdue based on current date/time
 */
fun Task.isOverdue(): Boolean {
    return this.Status == com.example.cheermateapp.data.model.Status.OverDue ||
            (this.Status == com.example.cheermateapp.data.model.Status.Pending && isTaskOverdueByDate())
}

/**
 * Private helper function to check if task is overdue by date calculation
 */
private fun Task.isTaskOverdueByDate(): Boolean {
    val dueDate = this.DueAt ?: return false
    val dueTime = this.DueTime
    val currentTimeMillis = System.currentTimeMillis()

    try {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = dateFormat.parse(dueDate)

        if (parsedDate != null) {
            calendar.time = parsedDate

            // If has due time, parse and set it
            if (!dueTime.isNullOrBlank()) {
                try {
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val parsedTime = timeFormat.parse(dueTime)
                    if (parsedTime != null) {
                        val timeCalendar = Calendar.getInstance()
                        timeCalendar.time = parsedTime

                        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                    }
                } catch (e: Exception) {
                    // If time parsing fails, set to end of day
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                }
            } else {
                // No specific time, consider overdue at end of day
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
            }

            return currentTimeMillis > calendar.timeInMillis
        }
    } catch (e: Exception) {
        // Error parsing date, assume not overdue
        return false
    }

    return false
}

/**
 * Get priority text with emoji
 */
fun Task.getPriorityText(): String {
    return when (this.Priority) {
        com.example.cheermateapp.data.model.Priority.High -> "🔴 High"
        com.example.cheermateapp.data.model.Priority.Medium -> "🟡 Medium"
        com.example.cheermateapp.data.model.Priority.Low -> "🟢 Low"
    }
}

/**
 * Get status text with emoji
 */
fun Task.getStatusText(): String {
    return when (this.Status) {
        com.example.cheermateapp.data.model.Status.Pending -> "⏳ Pending"
        com.example.cheermateapp.data.model.Status.InProgress -> "🔄 In Progress"
        com.example.cheermateapp.data.model.Status.Completed -> "✅ Completed"
        com.example.cheermateapp.data.model.Status.Cancelled -> "❌ Cancelled"
        com.example.cheermateapp.data.model.Status.OverDue -> "🔴 Overdue"
    }
}

/**
 * Get relative due date text (Today, Tomorrow, etc.)
 */
fun Task.getRelativeDueText(): String {
    val dueDate = this.DueAt ?: return "No due date"

    try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = dateFormat.parse(dueDate)

        if (parsedDate != null) {
            val today = Calendar.getInstance()
            val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            val dueCalendar = Calendar.getInstance().apply { time = parsedDate }

            return when {
                isSameDay(today, dueCalendar) -> "📅 Due Today"
                isSameDay(tomorrow, dueCalendar) -> "📅 Due Tomorrow"
                isSameDay(yesterday, dueCalendar) -> "📅 Was Due Yesterday"
                dueCalendar.before(today) -> "📅 Overdue"
                else -> {
                    val displayFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                    "📅 Due ${displayFormat.format(parsedDate)}"
                }
            }
        }
    } catch (e: Exception) {
        return "📅 $dueDate"
    }

    return "📅 $dueDate"
}

/**
 * Helper function to check if two calendar instances represent the same day
 */
private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

/**
 * Get task completion percentage
 */
fun Task.getCompletionPercentage(): Int {
    return when (this.Status) {
        com.example.cheermateapp.data.model.Status.Completed -> 100
        com.example.cheermateapp.data.model.Status.Pending -> this.TaskProgress
        com.example.cheermateapp.data.model.Status.InProgress -> this.TaskProgress
        com.example.cheermateapp.data.model.Status.OverDue -> this.TaskProgress
        com.example.cheermateapp.data.model.Status.Cancelled -> 0
    }
}

/**
 * Check if task is completed
 */
fun Task.isCompleted(): Boolean {
    return this.Status == com.example.cheermateapp.data.model.Status.Completed
}

/**
 * Check if task is active (not completed or cancelled)
 */
fun Task.isActive(): Boolean {
    return this.Status != com.example.cheermateapp.data.model.Status.Completed && this.Status != com.example.cheermateapp.data.model.Status.Cancelled
}

/**
 * Check if task is actionable (pending or in progress)
 */
fun Task.isActionable(): Boolean {
    return this.Status == com.example.cheermateapp.data.model.Status.Pending || this.Status == com.example.cheermateapp.data.model.Status.InProgress
}

/**
 * Get task urgency level (1=highest, 5=lowest)
 */
fun Task.getUrgencyLevel(): Int {
    return when {
        this.Status == com.example.cheermateapp.data.model.Status.OverDue -> 1
        this.Status == com.example.cheermateapp.data.model.Status.Pending && this.Priority == com.example.cheermateapp.data.model.Priority.High -> 2
        this.Status == com.example.cheermateapp.data.model.Status.InProgress -> 3
        this.Status == com.example.cheermateapp.data.model.Status.Pending && this.Priority == com.example.cheermateapp.data.model.Priority.Medium -> 4
        else -> 5
    }
}

/**
 * Get progress percentage as formatted string
 */
fun Task.getProgressText(): String {
    return "${this.TaskProgress}%"
}

/**
 * Get task summary for display
 */
fun Task.getSummary(): String {
    val statusEmoji = this.getStatusEmoji()
    val priorityEmoji = when (this.Priority) {
        com.example.cheermateapp.data.model.Priority.High -> "🔴"
        com.example.cheermateapp.data.model.Priority.Medium -> "🟡"
        com.example.cheermateapp.data.model.Priority.Low -> "🟢"
    }

    return "$statusEmoji $priorityEmoji ${this.Title}"
}

/**
 * Check if task is due today
 */
fun Task.isDueToday(): Boolean {
    return this.isToday()
}
