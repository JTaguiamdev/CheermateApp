package com.cheermateapp.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.cheermateapp.data.model.Priority
import com.cheermateapp.CheermateApp
import com.cheermateapp.data.model.User
import com.cheermateapp.data.model.Task

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val db = (app as CheermateApp).db

    private val userDao = db.userDao()
    private val taskDao = db.taskDao()

    fun createSampleUser() = viewModelScope.launch {
        userDao.insert(
            User(
                User_ID = 0, // Auto-generated
                Username = "alice",
                Email = "alice@example.com",
                PasswordHash = "<pbkdf2>",
                FirstName = "Alice",
                LastName = "Lee",
                Personality_ID = null
            )
        )
    }

    fun addSampleTask(userId: Int) = viewModelScope.launch {
        taskDao.insert(
            Task(
                Task_ID = 0, // Auto-generated
                User_ID = userId,
                Title = "Finish report",
                Priority = Priority.Medium,
                Status = com.cheermateapp.data.model.Status.Pending
            )
        )
    }
}
