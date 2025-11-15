package com.cheermateapp.data.repository

import android.util.Log
import com.cheermateapp.data.dao.*
import com.cheermateapp.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Repository for Task operations following clean architecture principles
 * Provides a clean API for task CRUD operations with proper error handling
 */
class TaskRepository(
    private val taskDao: TaskDao,
    private val subTaskDao: SubTaskDao,
    private val taskReminderDao: TaskReminderDao,
    private val taskDependencyDao: TaskDependencyDao
) {

    companion object {
        private const val TAG = "TaskRepository"
    }

    // ==================== TASK CRUD OPERATIONS ====================

    /**
     * Insert a new task
     */
    suspend fun insertTask(task: Task): DataResult<Long> = withContext(Dispatchers.IO) {
        try {
            val taskId = taskDao.insert(task)
            Log.d(TAG, "Task inserted successfully: $taskId")
            DataResult.Success(taskId)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting task", e)
            DataResult.Error(e, "Failed to create task: ${e.message}")
        }
    }

    /**
     * Update an existing task
     */
    suspend fun updateTask(task: Task): DataResult<Unit> = withContext(Dispatchers.IO) {
        try {
            taskDao.update(task)
            Log.d(TAG, "Task updated successfully: ${task.Task_ID}")
            DataResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating task", e)
            DataResult.Error(e, "Failed to update task: ${e.message}")
        }
    }

    /**
     * Delete a task (hard delete)
     */
    suspend fun deleteTask(task: Task): DataResult<Unit> = withContext(Dispatchers.IO) {
        try {
            taskDao.delete(task)
            Log.d(TAG, "Task deleted successfully: ${task.Task_ID}")
            DataResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting task", e)
            DataResult.Error(e, "Failed to delete task: ${e.message}")
        }
    }

    /**
     * Soft delete a task (sets DeletedAt timestamp)
     */
    suspend fun softDeleteTask(userId: Int, taskId: Int): DataResult<Unit> = withContext(Dispatchers.IO) {
        try {
            taskDao.softDelete(userId, taskId)
            Log.d(TAG, "Task soft deleted: userId=$userId, taskId=$taskId")
            DataResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error soft deleting task", e)
            DataResult.Error(e, "Failed to delete task: ${e.message}")
        }
    }

    /**
     * Restore a soft-deleted task
     */
    suspend fun restoreTask(userId: Int, taskId: Int): DataResult<Unit> = withContext(Dispatchers.IO) {
        try {
            taskDao.restoreTask(userId, taskId)
            Log.d(TAG, "Task restored: userId=$userId, taskId=$taskId")
            DataResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring task", e)
            DataResult.Error(e, "Failed to restore task: ${e.message}")
        }
    }

    // ==================== TASK QUERIES ====================

    /**
     * Get all tasks for a user (non-deleted)
     */
    suspend fun getAllTasks(userId: Int): DataResult<List<Task>> = withContext(Dispatchers.IO) {
        try {
            val tasks = taskDao.getAllTasksForUser(userId)
            Log.d(TAG, "Retrieved ${tasks.size} tasks for user $userId")
            DataResult.Success(tasks)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all tasks", e)
            DataResult.Error(e, "Failed to load tasks: ${e.message}")
        }
    }

    /**
     * Get all tasks as Flow for reactive updates
     */
    fun getAllTasksFlow(userId: Int): Flow<List<Task>> {
        return taskDao.getAllTasksFlow(userId)
            .catch { e ->
                Log.e(TAG, "Error in tasks flow", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Get today's tasks
     */
    suspend fun getTodayTasks(userId: Int, date: String): DataResult<List<Task>> = withContext(Dispatchers.IO) {
        try {
            val tasks = taskDao.getTodayTasks(userId, date)
            Log.d(TAG, "Retrieved ${tasks.size} today tasks for user $userId")
            DataResult.Success(tasks)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting today tasks", e)
            DataResult.Error(e, "Failed to load today's tasks: ${e.message}")
        }
    }

    /**
     * Get today's tasks as Flow
     */
    fun getTodayTasksFlow(userId: Int, date: String): Flow<List<Task>> {
        return taskDao.getTodayTasksFlow(userId, date)
            .catch { e ->
                Log.e(TAG, "Error in today tasks flow", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Get pending tasks
     */
    suspend fun getPendingTasks(userId: Int): DataResult<List<Task>> = withContext(Dispatchers.IO) {
        try {
            val tasks = taskDao.getPendingTasks(userId)
            Log.d(TAG, "Retrieved ${tasks.size} pending tasks for user $userId")
            DataResult.Success(tasks)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting pending tasks", e)
            DataResult.Error(e, "Failed to load pending tasks: ${e.message}")
        }
    }

    /**
     * Get pending tasks as Flow
     */
    fun getPendingTasksFlow(userId: Int): Flow<List<Task>> {
        return taskDao.getPendingTasksFlow(userId)
            .catch { e ->
                Log.e(TAG, "Error in pending tasks flow", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Get completed tasks
     */
    suspend fun getCompletedTasks(userId: Int): DataResult<List<Task>> = withContext(Dispatchers.IO) {
        try {
            val tasks = taskDao.getCompletedTasks(userId)
            Log.d(TAG, "Retrieved ${tasks.size} completed tasks for user $userId")
            DataResult.Success(tasks)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting completed tasks", e)
            DataResult.Error(e, "Failed to load completed tasks: ${e.message}")
        }
    }

    /**
     * Get completed tasks as Flow
     */
    fun getCompletedTasksFlow(userId: Int): Flow<List<Task>> {
        return taskDao.getCompletedTasksFlow(userId)
            .catch { e ->
                Log.e(TAG, "Error in completed tasks flow", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Get task by ID
     */
    suspend fun getTaskById(userId: Int, taskId: Int): DataResult<Task?> = withContext(Dispatchers.IO) {
        try {
            val task = taskDao.getTaskByCompositeKey(userId, taskId)
            DataResult.Success(task)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting task by ID", e)
            DataResult.Error(e, "Failed to load task: ${e.message}")
        }
    }

    /**
     * Get task by ID as Flow
     */
    fun getTaskByIdFlow(userId: Int, taskId: Int): Flow<Task?> {
        return taskDao.getTaskByCompositeKeyFlow(userId, taskId)
            .catch { e ->
                Log.e(TAG, "Error in task flow", e)
                emit(null)
            }
            .flowOn(Dispatchers.IO)
    }

    // ==================== TASK STATUS OPERATIONS ====================

    /**
     * Mark task as completed
     */
    suspend fun markTaskCompleted(userId: Int, taskId: Int): DataResult<Unit> = withContext(Dispatchers.IO) {
        try {
            taskDao.markTaskCompleted(userId, taskId)
            Log.d(TAG, "Task marked completed: userId=$userId, taskId=$taskId")
            DataResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error marking task completed", e)
            DataResult.Error(e, "Failed to complete task: ${e.message}")
        }
    }

    /**
     * Update task progress
     */
    suspend fun updateTaskProgress(userId: Int, taskId: Int, progress: Int): DataResult<Unit> = withContext(Dispatchers.IO) {
        try {
            taskDao.updateTaskProgress(userId, taskId, progress)
            Log.d(TAG, "Task progress updated: userId=$userId, taskId=$taskId, progress=$progress")
            DataResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating task progress", e)
            DataResult.Error(e, "Failed to update progress: ${e.message}")
        }
    }

    /**
     * Update task status
     */
    suspend fun updateTaskStatus(userId: Int, taskId: Int, status: String): DataResult<Unit> = withContext(Dispatchers.IO) {
        try {
            taskDao.updateTaskStatus(userId, taskId, status)
            Log.d(TAG, "Task status updated: userId=$userId, taskId=$taskId, status=$status")
            DataResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating task status", e)
            DataResult.Error(e, "Failed to update status: ${e.message}")
        }
    }

    // ==================== SEARCH AND FILTER ====================

    /**
     * Search tasks by title
     */
    suspend fun searchTasks(userId: Int, query: String): DataResult<List<Task>> = withContext(Dispatchers.IO) {
        try {
            val tasks = taskDao.searchTasks(userId, "%$query%")
            Log.d(TAG, "Search found ${tasks.size} tasks")
            DataResult.Success(tasks)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching tasks", e)
            DataResult.Error(e, "Failed to search tasks: ${e.message}")
        }
    }

    /**
     * Get tasks by priority
     */
    suspend fun getTasksByPriority(userId: Int, priority: String): DataResult<List<Task>> = withContext(Dispatchers.IO) {
        try {
            val tasks = taskDao.getTasksByPriority(userId, priority)
            DataResult.Success(tasks)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting tasks by priority", e)
            DataResult.Error(e, "Failed to load tasks: ${e.message}")
        }
    }

    // ==================== SUBTASK OPERATIONS ====================

    /**
     * Get subtasks for a task as Flow
     */
    fun getSubTasksFlow(taskId: Int, userId: Int): Flow<List<SubTask>> {
        return subTaskDao.getSubTasksFlow(taskId, userId)
            .catch { e ->
                Log.e(TAG, "Error in subtasks flow", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Insert subtask
     */
    suspend fun insertSubTask(subTask: SubTask): DataResult<Long> = withContext(Dispatchers.IO) {
        try {
            val id = subTaskDao.insert(subTask)
            DataResult.Success(id)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting subtask", e)
            DataResult.Error(e, "Failed to create subtask: ${e.message}")
        }
    }

    /**
     * Update subtask
     */
    suspend fun updateSubTask(subTask: SubTask): DataResult<Unit> = withContext(Dispatchers.IO) {
        try {
            subTaskDao.update(subTask)
            DataResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating subtask", e)
            DataResult.Error(e, "Failed to update subtask: ${e.message}")
        }
    }

    // ==================== TASK STATISTICS ====================

    /**
     * Get task counts
     */
    suspend fun getTaskCounts(userId: Int, todayDate: String): DataResult<Map<String, Int>> = withContext(Dispatchers.IO) {
        try {
            val counts = mapOf(
                "all" to taskDao.getAllTasksCount(userId),
                "today" to taskDao.getTodayTasksCount(userId, todayDate),
                "pending" to taskDao.getPendingTasksCount(userId),
                "completed" to taskDao.getCompletedTasksCount(userId)
            )
            DataResult.Success(counts)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting task counts", e)
            DataResult.Error(e, "Failed to load statistics: ${e.message}")
        }
    }

    // ==================== BATCH OPERATIONS ====================

    /**
     * Insert multiple tasks
     */
    suspend fun insertTasks(tasks: List<Task>): DataResult<Unit> = withContext(Dispatchers.IO) {
        try {
            taskDao.insertAll(tasks)
            Log.d(TAG, "Inserted ${tasks.size} tasks")
            DataResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting multiple tasks", e)
            DataResult.Error(e, "Failed to create tasks: ${e.message}")
        }
    }

    /**
     * Soft delete multiple tasks
     */
    suspend fun softDeleteTasks(userId: Int, taskIds: List<Int>): DataResult<Unit> = withContext(Dispatchers.IO) {
        try {
            taskDao.softDeleteMultiple(userId, taskIds)
            Log.d(TAG, "Soft deleted ${taskIds.size} tasks")
            DataResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error soft deleting multiple tasks", e)
            DataResult.Error(e, "Failed to delete tasks: ${e.message}")
        }
    }

    /**
     * Mark multiple tasks as completed
     */
    suspend fun markTasksCompleted(userId: Int, taskIds: List<Int>): DataResult<Unit> = withContext(Dispatchers.IO) {
        try {
            taskDao.markMultipleCompleted(userId, taskIds)
            Log.d(TAG, "Marked ${taskIds.size} tasks completed")
            DataResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error marking multiple tasks completed", e)
            DataResult.Error(e, "Failed to complete tasks: ${e.message}")
        }
    }
}
