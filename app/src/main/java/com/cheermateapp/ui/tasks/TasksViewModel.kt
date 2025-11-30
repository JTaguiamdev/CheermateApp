package com.cheermateapp.ui.tasks

import androidx.lifecycle.ViewModel
import com.cheermateapp.data.db.AppDb
import com.cheermateapp.data.model.Task
import kotlinx.coroutines.flow.*
import java.util.*

class TasksViewModel(private val db: AppDb, private val userId: Int) : ViewModel() {

    private val _taskFilter = MutableStateFlow("ALL")
    val taskFilter: StateFlow<String> = _taskFilter

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    val tasks: Flow<List<Task>> = taskFilter.flatMapLatest { filter ->
        when (filter) {
            "ALL" -> db.taskDao().getAllTasksFlow(userId)
            "TODAY" -> {
                val todayStr = dateToString(Date())
                db.taskDao().getTodayTasksFlow(userId, todayStr)
            }
            "PENDING" -> db.taskDao().getPendingTasksFlow(userId)
            "DONE" -> db.taskDao().getCompletedTasksFlow(userId)
            else -> db.taskDao().getAllTasksFlow(userId)
        }
    }

    val allTasksCount: Flow<Int> = db.taskDao().getAllTasksCountFlow(userId)
    val todayTasksCount: Flow<Int> = db.taskDao().getTodayTasksCountFlow(userId, dateToString(Date()))
    val pendingTasksCount: Flow<Int> = db.taskDao().getPendingTasksCountFlow(userId)
    val completedTasksCount: Flow<Int> = db.taskDao().getCompletedTasksCountFlow(userId)
    
    fun setTaskFilter(filter: String) {
        _taskFilter.value = filter
    }

    private fun dateToString(date: Date): String {
        val format = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(date)
    }
}
