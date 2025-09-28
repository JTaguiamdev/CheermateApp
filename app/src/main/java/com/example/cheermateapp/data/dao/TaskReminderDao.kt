package com.example.cheermateapp.data.dao

import androidx.room.*
import com.example.cheermateapp.data.model.TaskReminder

@Dao
interface TaskReminderDao {

    @Insert
    suspend fun insert(rem: TaskReminder): Long

    @Update
    suspend fun update(reminder: TaskReminder)

    @Delete
    suspend fun delete(reminder: TaskReminder)

    @Query("SELECT * FROM TaskReminder")
    suspend fun getAllReminders(): List<TaskReminder>

    @Query("SELECT * FROM TaskReminder WHERE Task_ID = :taskId AND User_ID = :userId AND IsActive = 1 ORDER BY RemindAt ASC")
    suspend fun activeForTask(taskId: Int, userId: Int): List<TaskReminder>

    @Query("SELECT * FROM TaskReminder WHERE Task_ID = :taskId")
    suspend fun getRemindersByTask(taskId: Int): List<TaskReminder>
}
