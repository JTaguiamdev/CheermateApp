package com.cheermateapp.ui.tasks

import androidx.lifecycle.ViewModel
import com.cheermateapp.data.db.AppDb
import com.cheermateapp.data.model.Task
import com.cheermateapp.data.model.Status
import kotlinx.coroutines.flow.*
import java.util.*

class TasksViewModel(private val db: AppDb, private val userId: Int) : ViewModel() {

    private val _taskFilter = MutableStateFlow("ALL")
    val taskFilter: StateFlow<String> = _taskFilter

    // 1. Get a single source of truth from the DB
    private val allTasks: Flow<List<Task>> = db.taskDao().getAllTasksFlow(userId)

    // 2. Create the filtered list based on the filter
    @OptIn(kotlinx.coroutines.FlowPreview::class)
    val tasks: Flow<List<Task>> = combine(allTasks, taskFilter) { tasks, filter ->
        when (filter) {
            "ALL" -> tasks
            "TODAY" -> {
                val todayStr = dateToString(Date())
                val altTodayFormat = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val altTodayStr = altTodayFormat.format(Date())
                tasks.filter { it.DueAt == todayStr || it.DueAt == altTodayStr }
            }
            "PENDING" -> tasks.filter { it.Status == Status.Pending || it.Status == Status.InProgress }
            "DONE" -> tasks.filter { it.Status == Status.Done }
            else -> tasks
        }
    }

    // 3. Derive counts from the main list
    val allTasksCount: Flow<Int> = allTasks.map { list ->
        val count = list.size
        android.util.Log.d("TasksViewModel", "Recalculating allTasksCount. Count: $count")
        count
    }

    val todayTasksCount: Flow<Int> = allTasks.map { list ->
        val todayStr = dateToString(Date())
        val altTodayFormat = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val altTodayStr = altTodayFormat.format(Date())
        val count = list.count { it.DueAt == todayStr || it.DueAt == altTodayStr }
        android.util.Log.d("TasksViewModel", "Recalculating todayTasksCount. Today is: $todayStr or $altTodayStr. Found $count tasks.")
        val dueDates = list.take(5).map { "'${it.DueAt}'" }.joinToString()
        android.util.Log.d("TasksViewModel", "Sample task due dates for today check: [$dueDates]")
        count
    }

    val pendingTasksCount: Flow<Int> = allTasks.map { list ->
        val count = list.count { it.Status == Status.Pending || it.Status == Status.InProgress }
        android.util.Log.d("TasksViewModel", "Recalculating pendingTasksCount. Count: $count")
        count
    }

    val completedTasksCount: Flow<Int> = allTasks.map { list ->
        val count = list.count { it.Status == Status.Done }
        android.util.Log.d("TasksViewModel", "Recalculating completedTasksCount. Count: $count")
        count
    }

    fun setTaskFilter(filter: String) {
        _taskFilter.value = filter
    }

    private fun dateToString(date: Date): String {
        val format = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(date)
    }
}
