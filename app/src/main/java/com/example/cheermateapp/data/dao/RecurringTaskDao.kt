package com.example.cheermateapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.cheermateapp.data.model.RecurringTask
import com.example.cheermateapp.data.model.RecurringFrequency

@Dao
interface RecurringTaskDao {

    // ✅ BASIC CRUD OPERATIONS
    @Insert
    suspend fun insert(recurringTask: RecurringTask): Long

    @Update
    suspend fun update(recurringTask: RecurringTask)

    @Delete
    suspend fun delete(recurringTask: RecurringTask)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(recurringTask: RecurringTask): Long

    // ✅ QUERY OPERATIONS
    @Query("SELECT * FROM RecurringTask WHERE User_ID = :userId ORDER BY CreatedAt DESC")
    suspend fun getAllRecurringTasks(userId: Int): List<RecurringTask>

    @Query("SELECT * FROM RecurringTask WHERE User_ID = :userId ORDER BY CreatedAt DESC")
    fun getAllRecurringTasksLive(userId: Int): LiveData<List<RecurringTask>>

    @Query("SELECT * FROM RecurringTask WHERE User_ID = :userId AND RecurringTask_ID = :recurringTaskId")
    suspend fun getRecurringTaskById(userId: Int, recurringTaskId: Int): RecurringTask?

    @Query("SELECT * FROM RecurringTask WHERE User_ID = :userId AND IsActive = 1 ORDER BY CreatedAt DESC")
    suspend fun getActiveRecurringTasks(userId: Int): List<RecurringTask>

    @Query("SELECT * FROM RecurringTask WHERE User_ID = :userId AND Frequency = :frequency AND IsActive = 1")
    suspend fun getRecurringTasksByFrequency(userId: Int, frequency: RecurringFrequency): List<RecurringTask>

    // ✅ UPDATE OPERATIONS
    @Query("UPDATE RecurringTask SET IsActive = :isActive, UpdatedAt = :updatedAt WHERE User_ID = :userId AND RecurringTask_ID = :recurringTaskId")
    suspend fun toggleActive(userId: Int, recurringTaskId: Int, isActive: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE RecurringTask SET LastGenerated = :lastGenerated, UpdatedAt = :updatedAt WHERE User_ID = :userId AND RecurringTask_ID = :recurringTaskId")
    suspend fun updateLastGenerated(userId: Int, recurringTaskId: Int, lastGenerated: String, updatedAt: Long = System.currentTimeMillis())

    // ✅ COUNT OPERATIONS
    @Query("SELECT COUNT(*) FROM RecurringTask WHERE User_ID = :userId")
    suspend fun getRecurringTaskCount(userId: Int): Int

    @Query("SELECT COUNT(*) FROM RecurringTask WHERE User_ID = :userId AND IsActive = 1")
    suspend fun getActiveRecurringTaskCount(userId: Int): Int

    // ✅ DELETE OPERATIONS
    @Query("DELETE FROM RecurringTask WHERE User_ID = :userId AND RecurringTask_ID = :recurringTaskId")
    suspend fun deleteById(userId: Int, recurringTaskId: Int)

    // ✅ GET NEXT ID
    @Query("SELECT COALESCE(MAX(RecurringTask_ID), 0) + 1 FROM RecurringTask WHERE User_ID = :userId")
    suspend fun getNextRecurringTaskId(userId: Int): Int
}
