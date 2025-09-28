package com.example.cheermateapp.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.cheermateapp.data.model.Priority
import com.example.cheermateapp.CheermateApp
import com.example.cheermateapp.data.model.User
import com.example.cheermateapp.data.model.Task

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
                PasswordHash = "<bcrypt>",
                FirstName = "Alice",
                LastName = "Lee",
                Birthdate = "2001-01-01",
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
                Status = com.example.cheermateapp.data.model.Status.Pending
            )
        )
    }
}
