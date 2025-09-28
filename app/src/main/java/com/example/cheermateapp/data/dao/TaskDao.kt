package com.example.cheermateapp.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.example.cheermateapp.data.model.Task
import com.example.cheermateapp.data.model.Priority

@Dao
interface TaskDao {

    // ✅ BASIC CRUD OPERATIONS
    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(task: Task): Long

    // ✅ YOUR EXISTING METHODS (PRESERVED)
    @Query("SELECT * FROM Task")
    suspend fun getAllTasks(): List<Task>

    @Query("SELECT * FROM Task WHERE User_ID = :userId ORDER BY CreatedAt DESC")
    suspend fun listForUser(userId: Int): List<Task>

    @Query("SELECT * FROM Task WHERE User_ID = :userId")
    suspend fun getTasksByUser(userId: Int): List<Task>

    @Query("SELECT * FROM Task WHERE Task_ID = :taskId")
    suspend fun getTaskById(taskId: Int): Task?

    // ✅ QUERIES USING COMPOSITE PRIMARY KEY (User_ID, Task_ID)
    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Task_ID = :taskId")
    suspend fun getTaskByCompositeKey(userId: Int, taskId: Int): Task?

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Task_ID = :taskId")
    fun getTaskByCompositeKeyLive(userId: Int, taskId: Int): LiveData<Task?>

    // ✅ SOFT DELETE WITH COMPOSITE KEY
    @Query("UPDATE Task SET DeletedAt = :deletedAt, UpdatedAt = :updatedAt WHERE User_ID = :userId AND Task_ID = :taskId")
    suspend fun softDelete(userId: Int, taskId: Int, deletedAt: Long = System.currentTimeMillis(), updatedAt: Long = System.currentTimeMillis())

    // Legacy soft delete method (preserved for compatibility)
    @Query("UPDATE Task SET DeletedAt = :deletedAt WHERE Task_ID = :taskId AND User_ID = :userId")
    suspend fun softDelete(taskId: Int, userId: Int, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE Task SET DeletedAt = NULL, UpdatedAt = :updatedAt WHERE User_ID = :userId AND Task_ID = :taskId")
    suspend fun restoreTask(userId: Int, taskId: Int, updatedAt: Long = System.currentTimeMillis())

    // ✅ TASK COMPLETION WITH COMPOSITE KEY
    @Query("UPDATE Task SET Status = 'Completed', TaskProgress = 100, UpdatedAt = :updatedAt WHERE User_ID = :userId AND Task_ID = :taskId")
    suspend fun markTaskCompleted(userId: Int, taskId: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE Task SET TaskProgress = :progress, UpdatedAt = :updatedAt WHERE User_ID = :userId AND Task_ID = :taskId")
    suspend fun updateTaskProgress(userId: Int, taskId: Int, progress: Int, updatedAt: Long = System.currentTimeMillis())

    // ✅ FIXED: Use String instead of Status enum in parameters
    @Query("UPDATE Task SET Status = :status, UpdatedAt = :updatedAt WHERE Task_ID = :taskId AND User_ID = :userId")
    suspend fun updateTaskStatus(taskId: Int, userId: Int, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE Task SET Status = :status, UpdatedAt = :updatedAt WHERE User_ID = :userId AND Task_ID = :taskId")
    suspend fun updateTaskStatusByCompositeKey(userId: Int, taskId: Int, status: String, updatedAt: Long = System.currentTimeMillis())

    // ✅ **MISSING METHODS IMPLEMENTED FOR FragmentTaskActivity**

    // Basic filtered queries
    @Query("SELECT * FROM Task WHERE User_ID = :userId AND DeletedAt IS NULL ORDER BY CreatedAt DESC")
    suspend fun getAllTasksForUser(userId: Int): List<Task>

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND DueAt = :date AND DeletedAt IS NULL ORDER BY DueTime ASC")
    suspend fun getTodayTasks(userId: Int, date: String): List<Task>

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Status IN ('Pending', 'InProgress') AND DeletedAt IS NULL ORDER BY DueAt ASC")
    suspend fun getPendingTasks(userId: Int): List<Task>

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Status = 'Completed' AND DeletedAt IS NULL ORDER BY UpdatedAt DESC")
    suspend fun getCompletedTasks(userId: Int): List<Task>

    // Count queries for UI updates
    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId AND DeletedAt IS NULL")
    suspend fun getAllTasksCount(userId: Int): Int

    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId AND DueAt = :date AND DeletedAt IS NULL")
    suspend fun getTodayTasksCount(userId: Int, date: String): Int

    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId AND Status IN ('Pending', 'InProgress') AND DeletedAt IS NULL")
    suspend fun getPendingTasksCount(userId: Int): Int

    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId AND Status = 'Completed' AND DeletedAt IS NULL")
    suspend fun getCompletedTasksCount(userId: Int): Int

    // ✅ REMAINING EXISTING METHODS (PRESERVED)
    @Query("SELECT * FROM Task WHERE User_ID = :userId AND DeletedAt IS NULL ORDER BY CreatedAt DESC")
    fun getAllTasksLive(userId: Int): LiveData<List<Task>>

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND DueAt = :todayStr AND DeletedAt IS NULL ORDER BY DueTime ASC")
    fun getTodayTasksLive(userId: Int, todayStr: String): LiveData<List<Task>>

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Status IN ('Pending', 'InProgress') AND DeletedAt IS NULL ORDER BY DueAt ASC")
    suspend fun getAllPendingTasks(userId: Int): List<Task>

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Status IN ('Pending', 'InProgress') AND DeletedAt IS NULL ORDER BY DueAt ASC")
    fun getPendingTasksLive(userId: Int): LiveData<List<Task>>

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Status = 'Completed' AND DeletedAt IS NULL ORDER BY UpdatedAt DESC")
    fun getCompletedTasksLive(userId: Int): LiveData<List<Task>>

    // ✅ COUNT QUERIES FOR ALL ACTIVITIES
    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId")
    suspend fun getTotalTasksForUser(userId: Int): Int

    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId AND DueAt < :currentDateStr AND Status != 'Completed' AND DeletedAt IS NULL")
    suspend fun getOverdueTasksCount(userId: Int, currentDateStr: String): Int

    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId AND Status = 'OverDue' AND DeletedAt IS NULL")
    suspend fun getOverdueTasksCount(userId: Int): Int

    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId AND DueAt = :todayStr AND Status = 'Completed' AND DeletedAt IS NULL")
    suspend fun getCompletedTodayTasksCount(userId: Int, todayStr: String): Int

    // ✅ FIXED: Use String instead of Status enum
    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId AND Status = :status AND DeletedAt IS NULL")
    suspend fun getTasksCountByStatus(userId: Int, status: String): Int

    // ✅ TASK ID GENERATION FOR USER (COMPOSITE KEY SUPPORT)
    @Query("SELECT COALESCE(MAX(Task_ID), 0) + 1 FROM Task WHERE User_ID = :userId")
    suspend fun getNextTaskIdForUser(userId: Int): Int

    @Query("SELECT Task_ID FROM Task WHERE User_ID = :userId ORDER BY Task_ID ASC")
    suspend fun getAllTaskIdsForUser(userId: Int): List<Int>

    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId")
    suspend fun getTaskCountForUser(userId: Int): Int

    @Query("SELECT COUNT(*) FROM Task WHERE Task_ID = :taskId AND User_ID = :userId AND DeletedAt IS NULL")
    suspend fun checkTaskExists(taskId: Int, userId: Int): Int

    // ✅ SEARCH AND FILTER QUERIES
    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Title LIKE :searchQuery AND DeletedAt IS NULL ORDER BY CreatedAt DESC")
    suspend fun searchTasks(userId: Int, searchQuery: String): List<Task>

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Priority = :priority AND DeletedAt IS NULL ORDER BY CreatedAt DESC")
    suspend fun getTasksByPriority(userId: Int, priority: String): List<Task>

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Status = :status AND DeletedAt IS NULL ORDER BY CreatedAt DESC")
    suspend fun getTasksByStatus(userId: Int, status: String): List<Task>

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Status = :status AND DeletedAt IS NULL ORDER BY DueAt ASC, DueTime ASC LIMIT :limit")
    suspend fun getTasksByStatusLimited(userId: Int, status: String, limit: Int): List<Task>

    // ✅ RECENT TASKS
    @Query("SELECT * FROM Task WHERE User_ID = :userId AND DeletedAt IS NULL ORDER BY CreatedAt DESC LIMIT :limit")
    suspend fun getRecentTasksForUser(userId: Int, limit: Int): List<Task>

    // ✅ STATISTICS QUERIES
    @Query("""
        SELECT COUNT(DISTINCT DueAt) FROM Task 
        WHERE User_ID = :userId 
        AND Status = 'Completed'
        AND DeletedAt IS NULL 
        AND DueAt >= :startDateStr 
        AND DueAt <= :endDateStr 
        ORDER BY DueAt DESC
    """)
    suspend fun getCompletedDaysInRange(userId: Int, startDateStr: String, endDateStr: String): Int

    @Query("SELECT Priority FROM Task WHERE User_ID = :userId AND DeletedAt IS NULL")
    suspend fun getAllPrioritiesForUser(userId: Int): List<Priority>

    @Query("SELECT Status FROM Task WHERE User_ID = :userId AND DeletedAt IS NULL")
    suspend fun getAllStatusForUser(userId: Int): List<String>

    // ✅ BULK OPERATIONS
    @Insert
    suspend fun insertAll(tasks: List<Task>)

    @Query("UPDATE Task SET DeletedAt = :deletedAt WHERE User_ID = :userId AND Task_ID IN (:taskIds)")
    suspend fun softDeleteMultiple(userId: Int, taskIds: List<Int>, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE Task SET Status = 'Completed', TaskProgress = 100, UpdatedAt = :updatedAt WHERE User_ID = :userId AND Task_ID IN (:taskIds)")
    suspend fun markMultipleCompleted(userId: Int, taskIds: List<Int>, updatedAt: Long = System.currentTimeMillis())

    // ✅ DATA MANAGEMENT QUERIES
    @Query("DELETE FROM Task WHERE User_ID = :userId")
    suspend fun deleteAllTasksForUser(userId: Int)

    @Query("DELETE FROM Task")
    suspend fun deleteAllTasks()

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND DeletedAt IS NULL ORDER BY CreatedAt DESC")
    suspend fun debugGetAllTasksForUser(userId: Int): List<Task>
}
