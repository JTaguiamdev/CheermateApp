package com.cheermateapp.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cheermateapp.data.db.AppDb

class TasksViewModelFactory(private val db: AppDb, private val userId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TasksViewModel(db, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
