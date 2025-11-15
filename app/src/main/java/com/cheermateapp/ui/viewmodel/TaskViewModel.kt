package com.cheermateapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cheermateapp.CheermateApp
import com.cheermateapp.data.model.Task
import com.cheermateapp.data.repository.DataResult
import com.cheermateapp.data.repository.TaskRepository
import com.cheermateapp.data.repository.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for Task operations
 * Manages task data and provides UI state for Activities/Fragments
 * Follows MVVM pattern with clean architecture
 */
class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as CheermateApp).db
    private val taskRepository = TaskRepository(
        db.taskDao(),
        db.subTaskDao(),
        db.taskReminderDao(),
        db.taskDependencyDao()
    )

    // ==================== UI STATE FLOWS ====================

    private val _allTasksState = MutableStateFlow<UiState<List<Task>>>(UiState.Idle)
    val allTasksState: StateFlow<UiState<List<Task>>> = _allTasksState.asStateFlow()

    private val _todayTasksState = MutableStateFlow<UiState<List<Task>>>(UiState.Idle)
    val todayTasksState: StateFlow<UiState<List<Task>>> = _todayTasksState.asStateFlow()

    private val _pendingTasksState = MutableStateFlow<UiState<List<Task>>>(UiState.Idle)
    val pendingTasksState: StateFlow<UiState<List<Task>>> = _pendingTasksState.asStateFlow()

    private val _completedTasksState = MutableStateFlow<UiState<List<Task>>>(UiState.Idle)
    val completedTasksState: StateFlow<UiState<List<Task>>> = _completedTasksState.asStateFlow()

    private val _taskOperationState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val taskOperationState: StateFlow<UiState<String>> = _taskOperationState.asStateFlow()

    // ==================== LOAD TASKS ====================

    /**
     * Load all tasks for a user with realtime updates via Flow
     */
    fun loadAllTasks(userId: Int) {
        viewModelScope.launch {
            _allTasksState.value = UiState.Loading
            
            taskRepository.getAllTasksFlow(userId)
                .catch { e ->
                    _allTasksState.value = UiState.Error("Failed to load tasks: ${e.message}", e as? Exception)
                }
                .collect { tasks ->
                    _allTasksState.value = UiState.Success(tasks)
                }
        }
    }

    /**
     * Load today's tasks with realtime updates
     */
    fun loadTodayTasks(userId: Int, date: String) {
        viewModelScope.launch {
            _todayTasksState.value = UiState.Loading
            
            taskRepository.getTodayTasksFlow(userId, date)
                .catch { e ->
                    _todayTasksState.value = UiState.Error("Failed to load today's tasks: ${e.message}", e as? Exception)
                }
                .collect { tasks ->
                    _todayTasksState.value = UiState.Success(tasks)
                }
        }
    }

    /**
     * Load pending tasks with realtime updates
     */
    fun loadPendingTasks(userId: Int) {
        viewModelScope.launch {
            _pendingTasksState.value = UiState.Loading
            
            taskRepository.getPendingTasksFlow(userId)
                .catch { e ->
                    _pendingTasksState.value = UiState.Error("Failed to load pending tasks: ${e.message}", e as? Exception)
                }
                .collect { tasks ->
                    _pendingTasksState.value = UiState.Success(tasks)
                }
        }
    }

    /**
     * Load completed tasks with realtime updates
     */
    fun loadCompletedTasks(userId: Int) {
        viewModelScope.launch {
            _completedTasksState.value = UiState.Loading
            
            taskRepository.getCompletedTasksFlow(userId)
                .catch { e ->
                    _completedTasksState.value = UiState.Error("Failed to load completed tasks: ${e.message}", e as? Exception)
                }
                .collect { tasks ->
                    _completedTasksState.value = UiState.Success(tasks)
                }
        }
    }

    // ==================== TASK OPERATIONS ====================

    /**
     * Insert a new task
     */
    fun insertTask(task: Task) {
        viewModelScope.launch {
            _taskOperationState.value = UiState.Loading
            
            when (val result = taskRepository.insertTask(task)) {
                is DataResult.Success -> {
                    _taskOperationState.value = UiState.Success("Task created successfully")
                }
                is DataResult.Error -> {
                    _taskOperationState.value = UiState.Error(
                        result.message ?: "Failed to create task",
                        result.exception
                    )
                }
                is DataResult.Loading -> {
                    // Already in loading state
                }
            }
        }
    }

    /**
     * Update an existing task
     */
    fun updateTask(task: Task) {
        viewModelScope.launch {
            _taskOperationState.value = UiState.Loading
            
            when (val result = taskRepository.updateTask(task)) {
                is DataResult.Success -> {
                    _taskOperationState.value = UiState.Success("Task updated successfully")
                }
                is DataResult.Error -> {
                    _taskOperationState.value = UiState.Error(
                        result.message ?: "Failed to update task",
                        result.exception
                    )
                }
                is DataResult.Loading -> {
                    // Already in loading state
                }
            }
        }
    }

    /**
     * Delete a task (soft delete)
     */
    fun deleteTask(userId: Int, taskId: Int) {
        viewModelScope.launch {
            _taskOperationState.value = UiState.Loading
            
            when (val result = taskRepository.softDeleteTask(userId, taskId)) {
                is DataResult.Success -> {
                    _taskOperationState.value = UiState.Success("Task deleted successfully")
                }
                is DataResult.Error -> {
                    _taskOperationState.value = UiState.Error(
                        result.message ?: "Failed to delete task",
                        result.exception
                    )
                }
                is DataResult.Loading -> {
                    // Already in loading state
                }
            }
        }
    }

    /**
     * Mark task as completed
     */
    fun markTaskCompleted(userId: Int, taskId: Int) {
        viewModelScope.launch {
            _taskOperationState.value = UiState.Loading
            
            when (val result = taskRepository.markTaskCompleted(userId, taskId)) {
                is DataResult.Success -> {
                    _taskOperationState.value = UiState.Success("Task marked as completed")
                }
                is DataResult.Error -> {
                    _taskOperationState.value = UiState.Error(
                        result.message ?: "Failed to complete task",
                        result.exception
                    )
                }
                is DataResult.Loading -> {
                    // Already in loading state
                }
            }
        }
    }

    /**
     * Update task progress
     */
    fun updateTaskProgress(userId: Int, taskId: Int, progress: Int) {
        viewModelScope.launch {
            when (val result = taskRepository.updateTaskProgress(userId, taskId, progress)) {
                is DataResult.Success -> {
                    // Silent success - progress updates shouldn't show notifications
                }
                is DataResult.Error -> {
                    _taskOperationState.value = UiState.Error(
                        result.message ?: "Failed to update progress",
                        result.exception
                    )
                }
                is DataResult.Loading -> {
                    // Loading state
                }
            }
        }
    }

    /**
     * Update task status
     */
    fun updateTaskStatus(userId: Int, taskId: Int, status: String) {
        viewModelScope.launch {
            when (val result = taskRepository.updateTaskStatus(userId, taskId, status)) {
                is DataResult.Success -> {
                    _taskOperationState.value = UiState.Success("Task status updated")
                }
                is DataResult.Error -> {
                    _taskOperationState.value = UiState.Error(
                        result.message ?: "Failed to update status",
                        result.exception
                    )
                }
                is DataResult.Loading -> {
                    // Loading state
                }
            }
        }
    }

    // ==================== SEARCH AND FILTER ====================

    /**
     * Search tasks by query
     */
    fun searchTasks(userId: Int, query: String, onResult: (UiState<List<Task>>) -> Unit) {
        viewModelScope.launch {
            onResult(UiState.Loading)
            
            when (val result = taskRepository.searchTasks(userId, query)) {
                is DataResult.Success -> {
                    onResult(UiState.Success(result.data))
                }
                is DataResult.Error -> {
                    onResult(UiState.Error(
                        result.message ?: "Search failed",
                        result.exception
                    ))
                }
                is DataResult.Loading -> {
                    onResult(UiState.Loading)
                }
            }
        }
    }

    /**
     * Get tasks by priority
     */
    fun getTasksByPriority(userId: Int, priority: String, onResult: (UiState<List<Task>>) -> Unit) {
        viewModelScope.launch {
            onResult(UiState.Loading)
            
            when (val result = taskRepository.getTasksByPriority(userId, priority)) {
                is DataResult.Success -> {
                    onResult(UiState.Success(result.data))
                }
                is DataResult.Error -> {
                    onResult(UiState.Error(
                        result.message ?: "Failed to load tasks",
                        result.exception
                    ))
                }
                is DataResult.Loading -> {
                    onResult(UiState.Loading)
                }
            }
        }
    }

    // ==================== BATCH OPERATIONS ====================

    /**
     * Delete multiple tasks
     */
    fun deleteTasks(userId: Int, taskIds: List<Int>) {
        viewModelScope.launch {
            _taskOperationState.value = UiState.Loading
            
            when (val result = taskRepository.softDeleteTasks(userId, taskIds)) {
                is DataResult.Success -> {
                    _taskOperationState.value = UiState.Success("${taskIds.size} tasks deleted")
                }
                is DataResult.Error -> {
                    _taskOperationState.value = UiState.Error(
                        result.message ?: "Failed to delete tasks",
                        result.exception
                    )
                }
                is DataResult.Loading -> {
                    // Already in loading state
                }
            }
        }
    }

    /**
     * Mark multiple tasks as completed
     */
    fun markTasksCompleted(userId: Int, taskIds: List<Int>) {
        viewModelScope.launch {
            _taskOperationState.value = UiState.Loading
            
            when (val result = taskRepository.markTasksCompleted(userId, taskIds)) {
                is DataResult.Success -> {
                    _taskOperationState.value = UiState.Success("${taskIds.size} tasks completed")
                }
                is DataResult.Error -> {
                    _taskOperationState.value = UiState.Error(
                        result.message ?: "Failed to complete tasks",
                        result.exception
                    )
                }
                is DataResult.Loading -> {
                    // Already in loading state
                }
            }
        }
    }

    // ==================== RESET STATE ====================

    /**
     * Reset operation state (e.g., after showing a message)
     */
    fun resetOperationState() {
        _taskOperationState.value = UiState.Idle
    }
}
