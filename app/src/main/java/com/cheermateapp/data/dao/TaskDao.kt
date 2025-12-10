package com.cheermateapp.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.cheermateapp.data.model.Task
import com.cheermateapp.data.model.Priority
import kotlinx.coroutines.flow.Flow

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

    // ✅ QUERY FOR USER TASKS
    @Query("SELECT * FROM Task WHERE User_ID = :userId ORDER BY CreatedAt DESC")
    suspend fun getTasksByUser(userId: Int): List<Task>

    @Query("SELECT * FROM Task WHERE Task_ID = :taskId")
    suspend fun getTaskById(taskId: Int): Task?

    // ✅ QUERIES USING COMPOSITE PRIMARY KEY (User_ID, Task_ID)
    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Task_ID = :taskId")
    suspend fun getTaskByCompositeKey(userId: Int, taskId: Int): Task?

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Task_ID = :taskId")
    fun getTaskByCompositeKeyLive(userId: Int, taskId: Int): LiveData<Task?>

    // ✅ TASK COMPLETION WITH COMPOSITE KEY
    @Query("UPDATE Task SET Status = 'Completed', TaskProgress = 100, UpdatedAt = :updatedAt WHERE User_ID = :userId AND Task_ID = :taskId")
    suspend fun markTaskCompleted(userId: Int, taskId: Int, updatedAt: String = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp())

    @Query("UPDATE Task SET TaskProgress = :progress, UpdatedAt = :updatedAt WHERE User_ID = :userId AND Task_ID = :taskId")
    suspend fun updateTaskProgress(userId: Int, taskId: Int, progress: Int, updatedAt: String = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp())

    // ✅ TASK STATUS UPDATE (also updates TaskProgress based on status)
    @Query("""
        UPDATE Task 
        SET Status = :status, 
            TaskProgress = CASE 
                WHEN :status = 'Completed' THEN 100 
                ELSE 0 
            END,
            UpdatedAt = :updatedAt 
        WHERE User_ID = :userId AND Task_ID = :taskId
    """)
    suspend fun updateTaskStatus(userId: Int, taskId: Int, status: String, updatedAt: String = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp())

    // ✅ **MISSING METHODS IMPLEMENTED FOR FragmentTaskActivity**

    // Basic filtered queries
    @Query("SELECT * FROM Task WHERE User_ID = :userId ORDER BY CreatedAt DESC")
    suspend fun getAllTasksForUser(userId: Int): List<Task>

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND DueAt = :date ORDER BY DueTime ASC")
    suspend fun getTodayTasks(userId: Int, date: String): List<Task>

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Status IN ('Pending', 'InProgress') ORDER BY DueAt ASC")
    suspend fun getPendingTasks(userId: Int): List<Task>

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Status = 'Completed' ORDER BY UpdatedAt DESC")
    suspend fun getCompletedTasks(userId: Int): List<Task>

    // Count queries for UI updates
    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId")
    suspend fun getAllTasksCount(userId: Int): Int

    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId AND DueAt = :date")
    suspend fun getTodayTasksCount(userId: Int, date: String): Int

    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId AND Status IN ('Pending', 'InProgress')")
    suspend fun getPendingTasksCount(userId: Int): Int

    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId AND Status = 'Completed'")
    suspend fun getCompletedTasksCount(userId: Int): Int

    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId AND Status IN ('Pending', 'InProgress')")
    fun getPendingTasksCountFlow(userId: Int): Flow<Int>


    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId AND Status = 'InProgress'")
    suspend fun getInProgressTasksCount(userId: Int): Int

    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId AND Status = 'InProgress'")
    fun getInProgressTasksCountFlow(userId: Int): Flow<Int>

    // ✅ FLOW COUNT QUERIES FOR LIVE UPDATES
    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId")
    fun getAllTasksCountFlow(userId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId AND DueAt = :date")
    fun getTodayTasksCountFlow(userId: Int, date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId AND Status = 'Completed'")
    fun getCompletedTasksCountFlow(userId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId AND DueAt = :todayStr AND Status = 'Completed'")
    fun getCompletedTodayTasksCountFlow(userId: Int, todayStr: String): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM Task 
        WHERE User_ID = :userId 
        AND Status = 'Completed' 
        AND date(UpdatedAt) = date('now', 'localtime')
    """)
    fun getTasksCompletedTodayByUpdateFlow(userId: Int): Flow<Int>

    // ✅ REMAINING EXISTING METHODS (PRESERVED)
    @Query("SELECT * FROM Task WHERE User_ID = :userId ORDER BY CreatedAt DESC")
    fun getAllTasksLive(userId: Int): LiveData<List<Task>>

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND DueAt = :todayStr ORDER BY DueTime ASC")
    fun getTodayTasksLive(userId: Int, todayStr: String): LiveData<List<Task>>

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Status IN ('Pending', 'InProgress') ORDER BY DueAt ASC")
    fun getPendingTasksLive(userId: Int): LiveData<List<Task>>

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Status = 'Completed' ORDER BY UpdatedAt DESC")
    fun getCompletedTasksLive(userId: Int): LiveData<List<Task>>

    // ✅ FLOW METHODS FOR REACTIVE UPDATES (MODERN APPROACH)
    @Query("SELECT * FROM Task WHERE User_ID = :userId ORDER BY CreatedAt DESC")
    fun getAllTasksFlow(userId: Int): Flow<List<Task>>

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND DueAt = :todayStr ORDER BY DueTime ASC")
    fun getTodayTasksFlow(userId: Int, todayStr: String): Flow<List<Task>>

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Status IN ('Pending', 'InProgress') ORDER BY DueAt ASC")
    fun getPendingTasksFlow(userId: Int): Flow<List<Task>>

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Status = 'Completed' ORDER BY UpdatedAt DESC")
    fun getCompletedTasksFlow(userId: Int): Flow<List<Task>>

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Task_ID = :taskId")
    fun getTaskByCompositeKeyFlow(userId: Int, taskId: Int): Flow<Task?>

    // ✅ COUNT QUERIES FOR ALL ACTIVITIES
    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId")
    suspend fun getTotalTasksForUser(userId: Int): Int

    // ✅ OVERDUE TASKS COUNT
    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId AND Status = 'OverDue'")
    suspend fun getOverdueTasksCount(userId: Int): Int

    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId AND DueAt = :todayStr AND Status = 'Completed'")
    suspend fun getCompletedTodayTasksCount(userId: Int, todayStr: String): Int

    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId AND DueAt = :todayStr AND Status = 'InProgress'")
    suspend fun getInProgressTodayTasksCount(userId: Int, todayStr: String): Int

    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId AND DueAt = :todayStr AND Status = 'InProgress'")
    fun getInProgressTodayTasksCountFlow(userId: Int, todayStr: String): Flow<Int>

    // ✅ NEW: Count tasks that were marked complete today (based on UpdatedAt timestamp)
    @Query("""
        SELECT COUNT(*) FROM Task 
        WHERE User_ID = :userId 
        AND Status = 'Completed' 
        AND date(UpdatedAt) = date('now', 'localtime')
    """)
    suspend fun getTasksCompletedTodayByUpdate(userId: Int): Int

    // ✅ FIXED: Use String instead of Status enum
    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId AND Status = :status")
    suspend fun getTasksCountByStatus(userId: Int, status: String): Int

    // ✅ TASK ID GENERATION FOR USER (COMPOSITE KEY SUPPORT)
    @Query("SELECT COALESCE(MAX(Task_ID), 0) + 1 FROM Task WHERE User_ID = :userId")
    suspend fun getNextTaskIdForUser(userId: Int): Int

    @Query("SELECT Task_ID FROM Task WHERE User_ID = :userId ORDER BY Task_ID ASC")
    suspend fun getAllTaskIdsForUser(userId: Int): List<Int>

    @Query("SELECT COUNT(*) FROM Task WHERE User_ID = :userId")
    suspend fun getTaskCountForUser(userId: Int): Int

    @Query("SELECT COUNT(*) FROM Task WHERE Task_ID = :taskId AND User_ID = :userId")
    suspend fun checkTaskExists(taskId: Int, userId: Int): Int

    // ✅ SEARCH AND FILTER QUERIES
    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Title LIKE :searchQuery ORDER BY CreatedAt DESC")
    suspend fun searchTasks(userId: Int, searchQuery: String): List<Task>

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Priority = :priority ORDER BY CreatedAt DESC")
    suspend fun getTasksByPriority(userId: Int, priority: String): List<Task>

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Status = :status ORDER BY CreatedAt DESC")
    suspend fun getTasksByStatus(userId: Int, status: String): List<Task>

    @Query("SELECT * FROM Task WHERE User_ID = :userId AND Status = :status ORDER BY DueAt ASC, DueTime ASC LIMIT :limit")
    suspend fun getTasksByStatusLimited(userId: Int, status: String, limit: Int): List<Task>

    // ✅ RECENT TASKS
    @Query("SELECT * FROM Task WHERE User_ID = :userId ORDER BY CreatedAt DESC LIMIT :limit")
    suspend fun getRecentTasksForUser(userId: Int, limit: Int): List<Task>

    // ✅ STATISTICS QUERIES
    @Query("""
        SELECT COUNT(DISTINCT DueAt) FROM Task 
        WHERE User_ID = :userId 
        AND Status = 'Completed'
        AND DueAt >= :startDateStr 
        AND DueAt <= :endDateStr 
        ORDER BY DueAt DESC
    """)
    suspend fun getCompletedDaysInRange(userId: Int, startDateStr: String, endDateStr: String): Int

    @Query("SELECT Priority FROM Task WHERE User_ID = :userId")
    suspend fun getAllPrioritiesForUser(userId: Int): List<Priority>

    @Query("SELECT Status FROM Task WHERE User_ID = :userId")
    suspend fun getAllStatusForUser(userId: Int): List<String>

    // ✅ BULK OPERATIONS (WITH @Transaction FOR ATOMICITY)
    @Transaction
    @Insert
    suspend fun insertAll(tasks: List<Task>)

    @Transaction
    @Query("UPDATE Task SET Status = 'Completed', TaskProgress = 100, UpdatedAt = :updatedAt WHERE User_ID = :userId AND Task_ID IN (:taskIds)")
    suspend fun markMultipleCompleted(userId: Int, taskIds: List<Int>, updatedAt: String = com.cheermateapp.data.model.TimestampUtil.getCurrentTimestamp())

    // ✅ DATA MANAGEMENT QUERIES (WITH @Transaction FOR SAFETY)
    @Transaction
    @Query("DELETE FROM Task WHERE User_ID = :userId")
    suspend fun deleteAllTasksForUser(userId: Int)

    @Transaction
    @Query("DELETE FROM Task")
    suspend fun deleteAllTasks()
}
