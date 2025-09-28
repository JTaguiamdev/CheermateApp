package com.example.cheermateapp.data.dao

import androidx.room.*
import com.example.cheermateapp.data.model.SubTask

@Dao
interface SubTaskDao {

    @Insert
    suspend fun insert(item: SubTask): Long

    @Update
    suspend fun update(subTask: SubTask)

    @Delete
    suspend fun delete(subTask: SubTask)

    @Query("SELECT * FROM SubTask")
    suspend fun getAllSubTasks(): List<SubTask>

    @Query("SELECT * FROM SubTask WHERE Task_ID = :taskId AND User_ID = :userId ORDER BY SortOrder ASC")
    suspend fun list(taskId: Int, userId: Int): List<SubTask>

    @Query("SELECT * FROM SubTask WHERE Task_ID = :taskId")
    suspend fun getSubTasksByTask(taskId: Int): List<SubTask>
}
