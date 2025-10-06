package com.example.cheermateapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.cheermateapp.data.model.Task
import com.example.cheermateapp.data.model.TaskDependency

@Dao
interface TaskDependencyDao {

    // ✅ BASIC CRUD OPERATIONS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(taskDependency: TaskDependency)

    @Delete
    suspend fun delete(taskDependency: TaskDependency)

    // ✅ QUERY OPERATIONS
    
    /**
     * Get all tasks that a specific task depends on (prerequisites)
     */
    @Query("""
        SELECT t.* FROM Task t
        INNER JOIN TaskDependency td ON t.Task_ID = td.DependsOn_Task_ID AND t.User_ID = td.DependsOn_User_ID
        WHERE td.Task_ID = :taskId AND td.User_ID = :userId AND t.DeletedAt IS NULL
    """)
    suspend fun getPrerequisiteTasks(userId: Int, taskId: Int): List<Task>

    /**
     * Get all tasks that depend on a specific task
     */
    @Query("""
        SELECT t.* FROM Task t
        INNER JOIN TaskDependency td ON t.Task_ID = td.Task_ID AND t.User_ID = td.User_ID
        WHERE td.DependsOn_Task_ID = :taskId AND td.DependsOn_User_ID = :userId AND t.DeletedAt IS NULL
    """)
    suspend fun getDependentTasks(userId: Int, taskId: Int): List<Task>

    /**
     * Check if a task has any incomplete prerequisites
     */
    @Query("""
        SELECT COUNT(*) FROM Task t
        INNER JOIN TaskDependency td ON t.Task_ID = td.DependsOn_Task_ID AND t.User_ID = td.DependsOn_User_ID
        WHERE td.Task_ID = :taskId AND td.User_ID = :userId 
        AND t.Status != 'Completed' AND t.DeletedAt IS NULL
    """)
    suspend fun countIncompletePrerequisites(userId: Int, taskId: Int): Int

    /**
     * Get all dependencies for a specific task
     */
    @Query("SELECT * FROM TaskDependency WHERE Task_ID = :taskId AND User_ID = :userId")
    suspend fun getDependenciesForTask(userId: Int, taskId: Int): List<TaskDependency>

    /**
     * Check if adding a dependency would create a circular dependency
     * Returns true if circular dependency would be created
     */
    suspend fun wouldCreateCircularDependency(userId: Int, taskId: Int, dependsOnTaskId: Int): Boolean {
        // If the task we want to depend on already depends on us (directly or indirectly), it's circular
        val dependentTasks = getDependentTasks(userId, dependsOnTaskId)
        
        if (dependentTasks.any { it.Task_ID == taskId }) {
            return true
        }
        
        // Check transitively
        for (dependentTask in dependentTasks) {
            if (wouldCreateCircularDependency(userId, taskId, dependentTask.Task_ID)) {
                return true
            }
        }
        
        return false
    }

    /**
     * Delete all dependencies for a task
     */
    @Query("DELETE FROM TaskDependency WHERE Task_ID = :taskId AND User_ID = :userId")
    suspend fun deleteAllDependenciesForTask(userId: Int, taskId: Int)

    /**
     * Delete specific dependency
     */
    @Query("DELETE FROM TaskDependency WHERE Task_ID = :taskId AND User_ID = :userId AND DependsOn_Task_ID = :dependsOnTaskId AND DependsOn_User_ID = :dependsOnUserId")
    suspend fun deleteDependency(userId: Int, taskId: Int, dependsOnUserId: Int, dependsOnTaskId: Int)

    /**
     * Check if a task can be started (all prerequisites are completed)
     */
    suspend fun canTaskBeStarted(userId: Int, taskId: Int): Boolean {
        return countIncompletePrerequisites(userId, taskId) == 0
    }
}
