package com.cheermateapp.data.dao

import androidx.room.*
import com.cheermateapp.data.model.SubTask
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM SubTask WHERE Task_ID = :taskId AND User_ID = :userId")
    suspend fun list(taskId: Int, userId: Int): List<SubTask>

    @Query("SELECT * FROM SubTask WHERE Task_ID = :taskId")
    suspend fun getSubTasksByTask(taskId: Int): List<SubTask>

    @Query("SELECT COALESCE(MAX(Subtask_ID), 0) + 1 FROM SubTask WHERE Task_ID = :taskId AND User_ID = :userId")
    suspend fun getNextSubtaskId(taskId: Int, userId: Int): Int

    // ✅ FLOW METHODS FOR REACTIVE UPDATES
    @Query("SELECT * FROM SubTask WHERE Task_ID = :taskId AND User_ID = :userId")
    fun getSubTasksFlow(taskId: Int, userId: Int): Flow<List<SubTask>>

    @Query("SELECT * FROM SubTask WHERE Task_ID = :taskId")
    fun getSubTasksByTaskFlow(taskId: Int): Flow<List<SubTask>>

    // ✅ BATCH OPERATIONS WITH @Transaction
    @Transaction
    @Insert
    suspend fun insertAll(subTasks: List<SubTask>)

    @Transaction
    @Query("DELETE FROM SubTask WHERE Task_ID = :taskId AND User_ID = :userId")
    suspend fun deleteAllForTask(taskId: Int, userId: Int)
}
