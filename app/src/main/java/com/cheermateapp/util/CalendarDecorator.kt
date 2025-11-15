package com.cheermateapp.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.CalendarView
import com.cheermateapp.data.model.Priority
import com.cheermateapp.data.model.Task
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class to store task information for calendar dates
 */
data class CalendarDateInfo(
    val date: String,
    val tasks: List<Task>,
    val highestPriority: Priority?
) {
    fun getPriorityColor(): Int {
        return when (highestPriority) {
            Priority.High -> android.graphics.Color.RED
            Priority.Medium -> android.graphics.Color.parseColor("#FFA500") // Orange
            Priority.Low -> android.graphics.Color.GREEN
            null -> android.graphics.Color.TRANSPARENT
        }
    }
}

/**
 * Utility class for calendar decorations and task date management
 */
object CalendarDecorator {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    /**
     * Get the highest priority from a list of tasks
     */
    fun getHighestPriority(tasks: List<Task>): Priority? {
        if (tasks.isEmpty()) return null
        
        // Priority order: High > Medium > Low
        return when {
            tasks.any { it.Priority == Priority.High } -> Priority.High
            tasks.any { it.Priority == Priority.Medium } -> Priority.Medium
            tasks.any { it.Priority == Priority.Low } -> Priority.Low
            else -> null
        }
    }
    
    /**
     * Get calendar date info with highest priority for each date
     */
    fun getCalendarDateInfoMap(tasks: List<Task>): Map<String, CalendarDateInfo> {
        val dateMap = mutableMapOf<String, MutableList<Task>>()
        
        // Group tasks by date
        tasks.forEach { task ->
            val date = task.DueAt ?: return@forEach
            if (!dateMap.containsKey(date)) {
                dateMap[date] = mutableListOf()
            }
            dateMap[date]?.add(task)
        }
        
        // Create CalendarDateInfo for each date with highest priority
        return dateMap.mapValues { (date, taskList) ->
            CalendarDateInfo(
                date = date,
                tasks = taskList,
                highestPriority = getHighestPriority(taskList)
            )
        }
    }
    
    /**
     * Format date to string (yyyy-MM-dd)
     */
    fun formatDate(year: Int, month: Int, day: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        return dateFormat.format(calendar.time)
    }
    
    /**
     * Parse date string to Calendar
     */
    fun parseDate(dateStr: String): Calendar? {
        return try {
            val date = dateFormat.parse(dateStr)
            val calendar = Calendar.getInstance()
            calendar.time = date ?: return null
            calendar
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get priority color for display
     */
    fun getPriorityColor(priority: Priority?): Int {
        return when (priority) {
            Priority.High -> android.graphics.Color.RED
            Priority.Medium -> android.graphics.Color.parseColor("#FFA500") // Orange
            Priority.Low -> android.graphics.Color.GREEN
            null -> android.graphics.Color.TRANSPARENT
        }
    }
    
    /**
     * Get priority dot indicator (emoji) for display
     */
    fun getPriorityDot(priority: Priority?): String {
        return when (priority) {
            Priority.High -> "🔴" // Red dot for high priority
            Priority.Medium -> "🟠" // Orange dot for medium priority
            Priority.Low -> "🟢" // Green dot for low priority
            null -> ""
        }
    }
}
