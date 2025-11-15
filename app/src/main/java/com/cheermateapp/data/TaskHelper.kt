package com.cheermateapp.data

import com.cheermateapp.data.dao.TaskDao
import com.cheermateapp.data.model.Priority
import com.cheermateapp.data.model.Status
import java.text.SimpleDateFormat
import java.util.*

class TaskHelper(private val taskDao: TaskDao) {

    // ✅ EXTENSION FUNCTIONS FOR ANALYTICS
    suspend fun getTaskCountByPriority(userId: Int): Map<Priority, Int> {
        val priorities = taskDao.getAllPrioritiesForUser(userId)
        return priorities.groupingBy { it }.eachCount()
    }

    // ✅ FIXED: Return type changed to Map<String, Int> to match DAO return type
    suspend fun getTaskCountByStatus(userId: Int): Map<String, Int> {
        val statuses = taskDao.getAllStatusForUser(userId)
        return statuses.groupingBy { it }.eachCount()
    }

    // ✅ WRAPPER METHODS - Fixed parameter types
    suspend fun getCompletedTasksForUser(userId: Int): Int {
        return taskDao.getTasksCountByStatus(userId, "Completed")  // ✅ Use String
    }

    suspend fun getTodayTasksForUser(userId: Int): Int {
        val today = getCurrentDateString()
        return taskDao.getTodayTasksCount(userId, today)
    }

    suspend fun getOverdueTasksForUser(userId: Int): Int {
        val today = getCurrentDateString()
        return taskDao.getOverdueTasksCount(userId)
    }

    suspend fun getCompletedTodayTasksForUser(userId: Int): Int {
        val today = getCurrentDateString()
        return taskDao.getCompletedTodayTasksCount(userId, today)
    }

    // ✅ ADDITIONAL STATUS-BASED METHODS
    suspend fun getPendingTasksForUser(userId: Int): Int {
        return taskDao.getTasksCountByStatus(userId, "Pending")
    }

    suspend fun getInProgressTasksForUser(userId: Int): Int {
        return taskDao.getTasksCountByStatus(userId, "InProgress")
    }

    suspend fun getCancelledTasksForUser(userId: Int): Int {
        return taskDao.getTasksCountByStatus(userId, "Cancelled")
    }

    suspend fun getOverdueTasksCountByStatus(userId: Int): Int {
        return taskDao.getTasksCountByStatus(userId, "OverDue")
    }

    // ✅ ENHANCED ANALYTICS - Convert String status back to Status enum for external use
    suspend fun getTaskCountByStatusEnum(userId: Int): Map<Status, Int> {
        val stringStatusMap = getTaskCountByStatus(userId)
        return stringStatusMap.mapKeys { (statusString, _) ->
            try {
                Status.valueOf(statusString)
            } catch (e: IllegalArgumentException) {
                // Handle case where string doesn't match enum (fallback to Pending)
                Status.Pending
            }
        }
    }

    // ✅ COMPREHENSIVE TASK STATISTICS
    suspend fun getTaskStatsSummary(userId: Int): TaskStatsSummary {
        val allTasksCount = taskDao.getAllTasksCount(userId)
        val completedCount = getCompletedTasksForUser(userId)
        val pendingCount = getPendingTasksForUser(userId)
        val inProgressCount = getInProgressTasksForUser(userId)
        val overdueCount = getOverdueTasksCountByStatus(userId)
        val cancelledCount = getCancelledTasksForUser(userId)

        val completionRate = if (allTasksCount > 0) {
            (completedCount.toFloat() / allTasksCount.toFloat() * 100).toInt()
        } else 0

        return TaskStatsSummary(
            totalTasks = allTasksCount,
            completedTasks = completedCount,
            pendingTasks = pendingCount,
            inProgressTasks = inProgressCount,
            overdueTasks = overdueCount,
            cancelledTasks = cancelledCount,
            completionRate = completionRate
        )
    }

    // ✅ UTILITY METHODS
    private fun getCurrentDateString(): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(Date())
    }

    suspend fun getTaskProgressAverage(userId: Int): Float {
        val allTasks = taskDao.getAllTasksForUser(userId)
        return if (allTasks.isNotEmpty()) {
            allTasks.map { it.TaskProgress }.average().toFloat()
        } else 0f
    }

    suspend fun getTasksCompletedThisWeek(userId: Int): Int {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time

        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = calendar.time

        val startDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startDate)
        val endDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(endDate)

        return taskDao.getCompletedDaysInRange(userId, startDateStr, endDateStr)
    }

    // ✅ CONVENIENCE METHOD - Get status string from enum
    fun getStatusString(status: Status): String {
        return status.name
    }

    // ✅ CONVENIENCE METHOD - Get enum from status string
    fun getStatusEnum(statusString: String): Status {
        return try {
            Status.valueOf(statusString)
        } catch (e: IllegalArgumentException) {
            Status.Pending // Default fallback
        }
    }
}

// ✅ UPDATED DATA CLASS FOR TASK STATISTICS SUMMARY
data class TaskStatsSummary(
    val totalTasks: Int,
    val completedTasks: Int,
    val pendingTasks: Int,
    val inProgressTasks: Int,
    val overdueTasks: Int,
    val cancelledTasks: Int,
    val completionRate: Int
) {
    fun getActiveTasksCount(): Int {
        return pendingTasks + inProgressTasks + overdueTasks
    }

    fun getFinishedTasksCount(): Int {
        return completedTasks + cancelledTasks
    }
}
