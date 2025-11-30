package com.cheermateapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cheermateapp.data.DailyProgress // Import your data class
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _dailyProgress = MutableLiveData<DailyProgress>()
    val dailyProgress: LiveData<DailyProgress> = _dailyProgress

    init {
        fetchDailyProgress()
    }

    private fun fetchDailyProgress() {
        viewModelScope.launch {
            // TODO: Replace with actual data fetching logic from your repository/database
            // For demonstration, let's use some dummy data
            val total = 10
            val completed = 3
            val inProgress = 2
            _dailyProgress.postValue(DailyProgress(total, completed, inProgress))
        }
    }

    // You might add functions here to update tasks, which would then trigger a new fetch
    fun taskCompleted() {
        // Logic to update task status in DB, then call fetchDailyProgress()
        fetchDailyProgress()
    }

    fun taskStarted() {
        // Logic to update task status in DB, then call fetchDailyProgress()
        fetchDailyProgress()
    }
}
