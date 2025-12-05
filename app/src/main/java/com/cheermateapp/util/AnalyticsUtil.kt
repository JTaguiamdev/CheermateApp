package com.cheermateapp.util

import com.cheermateapp.data.model.Task
import java.text.SimpleDateFormat
import java.util.*

/**
 * Analytics utility for calculating productivity metrics and trends
 */
object AnalyticsUtil {

    /**
     * Data class for productivity trends
     */
    data class ProductivityTrend(
        val period: String,
        val totalTasks: Int,
        val completedTasks: Int,
        val completionRate: Double,
        val averageCompletionTime: Long? = null // in milliseconds
    )

    /**
     * Data class for time-based analytics
     */
    data class TimeBasedAnalytics(
        val date: String,
        val tasksCreated: Int,
        val tasksCompleted: Int,
        val tasksPending: Int,
        val tasksOverdue: Int
    )

    /**
     * Data class for priority distribution
     */
    data class PriorityDistribution(
        val highPriority: Int,
        val mediumPriority: Int,
        val lowPriority: Int
    )

    /**
     * Calculate completion rate
     */
    fun calculateCompletionRate(totalTasks: Int, completedTasks: Int): Double {
        if (totalTasks == 0) return 0.0
        return (completedTasks.toDouble() / totalTasks.toDouble()) * 100.0
    }

    /**
     * Calculate average completion time for tasks
     */
    fun calculateAverageCompletionTime(tasks: List<Task>): Long? {
        val completedTasks = tasks.filter { 
            it.Status == com.cheermateapp.data.model.Status.Done 
        }
        
        if (completedTasks.isEmpty()) return null
        
        val completionTimes = completedTasks.mapNotNull { task ->
            if (task.UpdatedAt > task.CreatedAt) {
                task.UpdatedAt - task.CreatedAt
            } else null
        }
        
        return if (completionTimes.isNotEmpty()) {
            completionTimes.average().toLong()
        } else null
    }

    /**
     * Calculate productivity trend for a time period
     */
    fun calculateProductivityTrend(
        tasks: List<Task>,
        periodStart: Date,
        periodEnd: Date,
        periodName: String
    ): ProductivityTrend {
        val tasksInPeriod = tasks.filter { task ->
            val createdDate = Date(task.CreatedAt)
            createdDate.after(periodStart) && createdDate.before(periodEnd)
        }
        
        val completedTasks = tasksInPeriod.filter { 
            it.Status == com.cheermateapp.data.model.Status.Done 
        }
        
        val completionRate = calculateCompletionRate(tasksInPeriod.size, completedTasks.size)
        val avgCompletionTime = calculateAverageCompletionTime(tasksInPeriod)
        
        return ProductivityTrend(
            period = periodName,
            totalTasks = tasksInPeriod.size,
            completedTasks = completedTasks.size,
            completionRate = completionRate,
            averageCompletionTime = avgCompletionTime
        )
    }

    /**
     * Calculate weekly trends for the last N weeks
     */
    fun calculateWeeklyTrends(tasks: List<Task>, weeksCount: Int = 4): List<ProductivityTrend> {
        val trends = mutableListOf<ProductivityTrend>()
        val calendar = Calendar.getInstance()
        
        for (i in 0 until weeksCount) {
            val weekEnd = calendar.time
            calendar.add(Calendar.WEEK_OF_YEAR, -1)
            val weekStart = calendar.time
            
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val periodName = "${dateFormat.format(weekStart)} - ${dateFormat.format(weekEnd)}"
            
            val trend = calculateProductivityTrend(tasks, weekStart, weekEnd, periodName)
            trends.add(0, trend) // Add at beginning to maintain chronological order
        }
        
        // Reset calendar
        calendar.time = Date()
        
        return trends
    }

    /**
     * Calculate daily analytics for a date range
     */
    fun calculateDailyAnalytics(
        tasks: List<Task>,
        startDate: Date,
        endDate: Date
    ): List<TimeBasedAnalytics> {
        val analytics = mutableListOf<TimeBasedAnalytics>()
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        while (calendar.time.before(endDate) || calendar.time == endDate) {
            val currentDate = calendar.time
            val dateStr = dateFormat.format(currentDate)
            
            val dayStart = currentDate.time
            val dayEnd = currentDate.time + (24 * 60 * 60 * 1000) // Add 24 hours
            
            val tasksForDay = tasks.filter { task ->
                task.CreatedAt >= dayStart && task.CreatedAt < dayEnd
            }
            
            val completed = tasksForDay.count { 
                it.Status == com.cheermateapp.data.model.Status.Done 
            }
            val pending = tasksForDay.count { 
                it.Status == com.cheermateapp.data.model.Status.Pending ||
                it.Status == com.cheermateapp.data.model.Status.InProgress
            }
            val overdue = tasksForDay.count { it.isOverdue() }
            
            analytics.add(TimeBasedAnalytics(
                date = dateStr,
                tasksCreated = tasksForDay.size,
                tasksCompleted = completed,
                tasksPending = pending,
                tasksOverdue = overdue
            ))
            
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return analytics
    }

    /**
     * Calculate priority distribution
     */
    fun calculatePriorityDistribution(tasks: List<Task>): PriorityDistribution {
        val high = tasks.count { 
            it.Priority == com.cheermateapp.data.model.Priority.High 
        }
        val medium = tasks.count { 
            it.Priority == com.cheermateapp.data.model.Priority.Medium 
        }
        val low = tasks.count { 
            it.Priority == com.cheermateapp.data.model.Priority.Low 
        }
        
        return PriorityDistribution(
            highPriority = high,
            mediumPriority = medium,
            lowPriority = low
        )
    }

    /**
     * Calculate streak (consecutive days with completed tasks)
     */
    fun calculateCurrentStreak(tasks: List<Task>): Int {
        val completedTasks = tasks.filter { 
            it.Status == com.cheermateapp.data.model.Status.Done 
        }.sortedByDescending { it.UpdatedAt }
        
        if (completedTasks.isEmpty()) return 0
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        var streak = 0
        
        // Check if there's a completed task today
        val today = dateFormat.format(Date())
        val hasTaskToday = completedTasks.any { task ->
            dateFormat.format(Date(task.UpdatedAt)) == today
        }
        
        if (!hasTaskToday) return 0
        
        // Count consecutive days
        val uniqueDates = completedTasks.map { task ->
            dateFormat.format(Date(task.UpdatedAt))
        }.distinct().sorted().reversed()
        
        calendar.time = Date()
        
        for (date in uniqueDates) {
            val expectedDate = dateFormat.format(calendar.time)
            if (date == expectedDate) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        
        return streak
    }

    /**
     * Format duration in human-readable format
     */
    fun formatDuration(milliseconds: Long): String {
        val hours = milliseconds / (1000 * 60 * 60)
        val minutes = (milliseconds / (1000 * 60)) % 60
        val days = hours / 24
        
        return when {
            days > 0 -> "${days}d ${hours % 24}h"
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }
}
