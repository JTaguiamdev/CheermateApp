package com.cheermateapp.util

import com.cheermateapp.data.dao.TaskDao
import com.cheermateapp.data.model.Priority
import com.cheermateapp.data.model.Status
import com.cheermateapp.data.model.Task

/**
 * Utility class for bulk task operations
 */
object BulkTaskOperations {

    /**
     * Result of a bulk operation
     */
    data class BulkOperationResult(
        val successCount: Int,
        val failureCount: Int,
        val errors: List<String> = emptyList()
    )

    /**
     * Bulk update task status
     */
    suspend fun bulkUpdateStatus(
        taskDao: TaskDao,
        userId: Int,
        taskIds: List<Int>,
        newStatus: Status
    ): BulkOperationResult {
        var successCount = 0
        var failureCount = 0
        val errors = mutableListOf<String>()

        for (taskId in taskIds) {
            try {
                val statusString = when (newStatus) {
                    Status.Pending -> "Pending"
                    Status.InProgress -> "InProgress"
                    Status.Done -> "Done"
                    Status.Cancelled -> "Cancelled"
                    Status.OverDue -> "OverDue"
                }
                taskDao.updateTaskStatus(userId, taskId, statusString)
                successCount++
            } catch (e: Exception) {
                failureCount++
                errors.add("Failed to update task $taskId: ${e.message}")
            }
        }

        return BulkOperationResult(successCount, failureCount, errors)
    }

    /**
     * Bulk update task priority
     */
    suspend fun bulkUpdatePriority(
        taskDao: TaskDao,
        userId: Int,
        taskIds: List<Int>,
        newPriority: Priority
    ): BulkOperationResult {
        var successCount = 0
        var failureCount = 0
        val errors = mutableListOf<String>()

        for (taskId in taskIds) {
            try {
                val task = taskDao.getTaskByCompositeKey(userId, taskId)
                if (task != null) {
                    val updatedTask = task.copy(
                        Priority = newPriority,
                        UpdatedAt = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp()
                    )
                    taskDao.update(updatedTask)
                    successCount++
                } else {
                    failureCount++
                    errors.add("Task $taskId not found")
                }
            } catch (e: Exception) {
                failureCount++
                errors.add("Failed to update task $taskId: ${e.message}")
            }
        }

        return BulkOperationResult(successCount, failureCount, errors)
    }

    /**
     * Bulk update task progress
     */
    suspend fun bulkUpdateProgress(
        taskDao: TaskDao,
        userId: Int,
        taskIds: List<Int>,
        newProgress: Int
    ): BulkOperationResult {
        var successCount = 0
        var failureCount = 0
        val errors = mutableListOf<String>()

        val clampedProgress = newProgress.coerceIn(0, 100)

        for (taskId in taskIds) {
            try {
                taskDao.updateTaskProgress(userId, taskId, clampedProgress)
                successCount++
            } catch (e: Exception) {
                failureCount++
                errors.add("Failed to update task $taskId: ${e.message}")
            }
        }

        return BulkOperationResult(successCount, failureCount, errors)
    }

    /**
     * Bulk mark tasks as completed
     */
    suspend fun bulkComplete(
        taskDao: TaskDao,
        userId: Int,
        taskIds: List<Int>
    ): BulkOperationResult {
        var successCount = 0
        var failureCount = 0
        val errors = mutableListOf<String>()

        for (taskId in taskIds) {
            try {
                taskDao.markTaskCompleted(userId, taskId)
                successCount++
            } catch (e: Exception) {
                failureCount++
                errors.add("Failed to complete task $taskId: ${e.message}")
            }
        }

        return BulkOperationResult(successCount, failureCount, errors)
    }

    /**
     * Bulk update due date
     */
    suspend fun bulkUpdateDueDate(
        taskDao: TaskDao,
        userId: Int,
        taskIds: List<Int>,
        newDueDate: String,
        newDueTime: String? = null
    ): BulkOperationResult {
        var successCount = 0
        var failureCount = 0
        val errors = mutableListOf<String>()

        for (taskId in taskIds) {
            try {
                val task = taskDao.getTaskByCompositeKey(userId, taskId)
                if (task != null) {
                    val updatedTask = task.copy(
                        DueAt = newDueDate,
                        DueTime = newDueTime ?: task.DueTime,
                        UpdatedAt = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp()
                    )
                    taskDao.update(updatedTask)
                    successCount++
                } else {
                    failureCount++
                    errors.add("Task $taskId not found")
                }
            } catch (e: Exception) {
                failureCount++
                errors.add("Failed to update task $taskId: ${e.message}")
            }
        }

        return BulkOperationResult(successCount, failureCount, errors)
    }

    /**
     * Filter tasks by criteria for bulk selection
     */
    fun filterTasksForBulkSelection(
        tasks: List<Task>,
        status: Status? = null,
        priority: Priority? = null,
        isOverdue: Boolean? = null
    ): List<Task> {
        var filtered = tasks

        if (status != null) {
            filtered = filtered.filter { it.Status == status }
        }

        if (priority != null) {
            filtered = filtered.filter { it.Priority == priority }
        }

        if (isOverdue != null) {
            filtered = filtered.filter { it.isOverdue() == isOverdue }
        }

        return filtered
    }
}
