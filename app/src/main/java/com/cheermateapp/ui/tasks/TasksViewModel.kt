package com.cheermateapp.ui.tasks

import androidx.lifecycle.ViewModel
import com.cheermateapp.data.db.AppDb
import com.cheermateapp.data.model.Task
import com.cheermateapp.data.model.Status
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.*

class TasksViewModel(private val db: AppDb, private val userId: Int) : ViewModel() {

    private val _taskFilter = MutableStateFlow("ALL")
    val taskFilter: StateFlow<String> = _taskFilter

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // 1. Get a single source of truth from the DB
    private val allTasks: Flow<List<Task>> = db.taskDao().getAllTasksFlow(userId)

    // 2. Create the filtered and searched list
    @OptIn(kotlinx.coroutines.FlowPreview::class)
    val tasks: Flow<List<Task>> = combine(allTasks, taskFilter, searchQuery) { allTasksFromDb, filter, query ->
        var filteredList = allTasksFromDb

        // Apply tab filter first
        filteredList = when (filter) {
            "ALL" -> filteredList
            "TODAY" -> {
                val todayStr = dateToString(Date())
                val altTodayFormat = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val altTodayStr = altTodayFormat.format(Date())
                filteredList.filter { it.DueDate == todayStr || it.DueDate == altTodayStr }
            }
            "PENDING" -> filteredList.filter { it.Status == Status.Pending || it.Status == Status.InProgress }
            "COMPLETED" -> filteredList.filter { it.Status == Status.Completed }
            else -> filteredList
        }

        // Apply search query to the filtered list (title only, case-insensitive)
        if (query.isNotBlank()) {
            filteredList = filteredList.filter { task ->
                task.Title.contains(query, ignoreCase = true)
            }
        }
        filteredList
    }

    // 3. Use direct count flows for better real-time performance
    val allTasksCount: Flow<Int> = db.taskDao().getAllTasksCountFlow(userId)

    val todayTasksCount: Flow<Int> = db.taskDao().getTodayTasksCountFlow(userId, dateToString(Date()))

    val pendingTasksCount: Flow<Int> = db.taskDao().getPendingTasksCountFlow(userId)

    val completedTasksCount: Flow<Int> = db.taskDao().getCompletedTasksCountFlow(userId)

    fun setTaskFilter(filter: String) {
        _taskFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun dateToString(date: Date): String {
        val format = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(date)
    }
}
