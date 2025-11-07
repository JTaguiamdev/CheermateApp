package com.example.cheermateapp.validation.crud

import com.example.cheermateapp.data.dao.TaskDao
import com.example.cheermateapp.data.model.Task
import com.example.cheermateapp.data.model.Priority

/**
 * CRUD validator for TaskDao
 * Tests all CRUD operations specific to Task entity
 */
class TaskDaoCrudChecker(private val taskDao: TaskDao) : BaseCrudChecker() {
    
    override fun getDaoName(): String = "TaskDao"
    
    override suspend fun validateDao(): List<CrudTestResult> {
        val results = mutableListOf<CrudTestResult>()
        
        // Test CREATE operations
        results.add(testInsert())
        results.add(testInsertOrReplace())
        results.add(testInsertAll())
        
        // Test READ operations
        results.add(testGetAllTasks())
        results.add(testGetTaskById())
        results.add(testGetTasksByUser())
        results.add(testGetTaskByCompositeKey())
        
        // Test UPDATE operations
        results.add(testUpdate())
        results.add(testUpdateTaskProgress())
        results.add(testUpdateTaskStatus())
        results.add(testMarkTaskCompleted())
        
        // Test DELETE operations
        results.add(testDelete())
        results.add(testSoftDelete())
        results.add(testRestoreTask())
        
        // Test QUERY operations
        results.add(testSearchTasks())
        results.add(testGetTasksByPriority())
        results.add(testGetTasksByStatus())
        results.add(testGetPendingTasks())
        results.add(testGetCompletedTasks())
        
        // Test COUNT operations
        results.add(testGetTaskCountForUser())
        results.add(testGetAllTasksCount())
        results.add(testGetPendingTasksCount())
        results.add(testGetCompletedTasksCount())
        
        // Test BULK operations
        results.add(testSoftDeleteMultiple())
        results.add(testMarkMultipleCompleted())
        
        return results
    }
    
    private suspend fun testInsert() = executeTest(CrudOperation.CREATE, "insert") {
        val testTask = createTestTask(userId = 9999, taskId = 1)
        val insertedId = taskDao.insert(testTask)
        assert(insertedId > 0) { "Insert should return positive ID" }
        
        // Cleanup
        taskDao.delete(testTask)
    }
    
    private suspend fun testInsertOrReplace() = executeTest(CrudOperation.CREATE, "insertOrReplace") {
        val testTask = createTestTask(userId = 9999, taskId = 2)
        val insertedId = taskDao.insertOrReplace(testTask)
        assert(insertedId > 0) { "InsertOrReplace should return positive ID" }
        
        // Test replace
        val updatedTask = testTask.copy(Title = "Updated Title")
        val replacedId = taskDao.insertOrReplace(updatedTask)
        assert(replacedId > 0) { "Replace should return positive ID" }
        
        // Cleanup
        taskDao.delete(updatedTask)
    }
    
    private suspend fun testInsertAll() = executeTest(CrudOperation.BULK_OPERATION, "insertAll") {
        val tasks = listOf(
            createTestTask(userId = 9999, taskId = 10),
            createTestTask(userId = 9999, taskId = 11),
            createTestTask(userId = 9999, taskId = 12)
        )
        taskDao.insertAll(tasks)
        
        // Verify inserted
        val count = taskDao.getTaskCountForUser(9999)
        assert(count >= 3) { "Should have at least 3 tasks for user 9999" }
        
        // Cleanup
        taskDao.deleteAllTasksForUser(9999)
    }
    
    private suspend fun testGetAllTasks() = executeTest(CrudOperation.READ, "getAllTasks") {
        val tasks = taskDao.getAllTasks()
        // Should not throw exception, result can be empty
    }
    
    private suspend fun testGetTaskById() = executeTest(CrudOperation.READ, "getTaskById") {
        val testTask = createTestTask(userId = 9999, taskId = 20)
        taskDao.insert(testTask)
        
        val retrieved = taskDao.getTaskById(20)
        assert(retrieved != null) { "Should retrieve inserted task" }
        
        // Cleanup
        taskDao.delete(testTask)
    }
    
    private suspend fun testGetTasksByUser() = executeTest(CrudOperation.QUERY, "getTasksByUser") {
        val tasks = taskDao.getTasksByUser(9999)
        // Should not throw exception, result can be empty
    }
    
    private suspend fun testGetTaskByCompositeKey() = executeTest(CrudOperation.READ, "getTaskByCompositeKey") {
        val testTask = createTestTask(userId = 9999, taskId = 21)
        taskDao.insert(testTask)
        
        val retrieved = taskDao.getTaskByCompositeKey(9999, 21)
        assert(retrieved != null) { "Should retrieve task by composite key" }
        
        // Cleanup
        taskDao.delete(testTask)
    }
    
    private suspend fun testUpdate() = executeTest(CrudOperation.UPDATE, "update") {
        val testTask = createTestTask(userId = 9999, taskId = 30)
        taskDao.insert(testTask)
        
        val updatedTask = testTask.copy(Title = "Updated Title")
        taskDao.update(updatedTask)
        
        val retrieved = taskDao.getTaskByCompositeKey(9999, 30)
        assert(retrieved?.Title == "Updated Title") { "Title should be updated" }
        
        // Cleanup
        taskDao.delete(updatedTask)
    }
    
    private suspend fun testUpdateTaskProgress() = executeTest(CrudOperation.UPDATE, "updateTaskProgress") {
        val testTask = createTestTask(userId = 9999, taskId = 31)
        taskDao.insert(testTask)
        
        taskDao.updateTaskProgress(9999, 31, 50)
        
        val retrieved = taskDao.getTaskByCompositeKey(9999, 31)
        assert(retrieved?.TaskProgress == 50) { "Task progress should be updated to 50" }
        
        // Cleanup
        taskDao.delete(testTask)
    }
    
    private suspend fun testUpdateTaskStatus() = executeTest(CrudOperation.UPDATE, "updateTaskStatus") {
        val testTask = createTestTask(userId = 9999, taskId = 32)
        taskDao.insert(testTask)
        
        taskDao.updateTaskStatus(9999, 32, "InProgress")
        
        val retrieved = taskDao.getTaskByCompositeKey(9999, 32)
        assert(retrieved?.Status == "InProgress") { "Status should be updated to InProgress" }
        
        // Cleanup
        taskDao.delete(testTask)
    }
    
    private suspend fun testMarkTaskCompleted() = executeTest(CrudOperation.UPDATE, "markTaskCompleted") {
        val testTask = createTestTask(userId = 9999, taskId = 33)
        taskDao.insert(testTask)
        
        taskDao.markTaskCompleted(9999, 33)
        
        val retrieved = taskDao.getTaskByCompositeKey(9999, 33)
        assert(retrieved?.Status == "Completed") { "Status should be Completed" }
        assert(retrieved?.TaskProgress == 100) { "Progress should be 100" }
        
        // Cleanup
        taskDao.delete(testTask)
    }
    
    private suspend fun testDelete() = executeTest(CrudOperation.DELETE, "delete") {
        val testTask = createTestTask(userId = 9999, taskId = 40)
        taskDao.insert(testTask)
        
        taskDao.delete(testTask)
        
        val retrieved = taskDao.getTaskByCompositeKey(9999, 40)
        assert(retrieved == null) { "Task should be deleted" }
    }
    
    private suspend fun testSoftDelete() = executeTest(CrudOperation.DELETE, "softDelete") {
        val testTask = createTestTask(userId = 9999, taskId = 41)
        taskDao.insert(testTask)
        
        taskDao.softDelete(9999, 41)
        
        val retrieved = taskDao.getTaskByCompositeKey(9999, 41)
        assert(retrieved?.DeletedAt != null) { "Task should be soft deleted" }
        
        // Cleanup
        taskDao.delete(testTask)
    }
    
    private suspend fun testRestoreTask() = executeTest(CrudOperation.UPDATE, "restoreTask") {
        val testTask = createTestTask(userId = 9999, taskId = 42)
        taskDao.insert(testTask)
        taskDao.softDelete(9999, 42)
        
        taskDao.restoreTask(9999, 42)
        
        val retrieved = taskDao.getTaskByCompositeKey(9999, 42)
        assert(retrieved?.DeletedAt == null) { "Task should be restored" }
        
        // Cleanup
        taskDao.delete(testTask)
    }
    
    private suspend fun testSearchTasks() = executeTest(CrudOperation.QUERY, "searchTasks") {
        val testTask = createTestTask(userId = 9999, taskId = 50, title = "Searchable Task")
        taskDao.insert(testTask)
        
        val results = taskDao.searchTasks(9999, "%Search%")
        assert(results.isNotEmpty()) { "Should find searchable task" }
        
        // Cleanup
        taskDao.delete(testTask)
    }
    
    private suspend fun testGetTasksByPriority() = executeTest(CrudOperation.QUERY, "getTasksByPriority") {
        val tasks = taskDao.getTasksByPriority(9999, "High")
        // Should not throw exception
    }
    
    private suspend fun testGetTasksByStatus() = executeTest(CrudOperation.QUERY, "getTasksByStatus") {
        val tasks = taskDao.getTasksByStatus(9999, "Pending")
        // Should not throw exception
    }
    
    private suspend fun testGetPendingTasks() = executeTest(CrudOperation.QUERY, "getPendingTasks") {
        val tasks = taskDao.getPendingTasks(9999)
        // Should not throw exception
    }
    
    private suspend fun testGetCompletedTasks() = executeTest(CrudOperation.QUERY, "getCompletedTasks") {
        val tasks = taskDao.getCompletedTasks(9999)
        // Should not throw exception
    }
    
    private suspend fun testGetTaskCountForUser() = executeTest(CrudOperation.COUNT, "getTaskCountForUser") {
        val count = taskDao.getTaskCountForUser(9999)
        assert(count >= 0) { "Count should be non-negative" }
    }
    
    private suspend fun testGetAllTasksCount() = executeTest(CrudOperation.COUNT, "getAllTasksCount") {
        val count = taskDao.getAllTasksCount(9999)
        assert(count >= 0) { "Count should be non-negative" }
    }
    
    private suspend fun testGetPendingTasksCount() = executeTest(CrudOperation.COUNT, "getPendingTasksCount") {
        val count = taskDao.getPendingTasksCount(9999)
        assert(count >= 0) { "Count should be non-negative" }
    }
    
    private suspend fun testGetCompletedTasksCount() = executeTest(CrudOperation.COUNT, "getCompletedTasksCount") {
        val count = taskDao.getCompletedTasksCount(9999)
        assert(count >= 0) { "Count should be non-negative" }
    }
    
    private suspend fun testSoftDeleteMultiple() = executeTest(CrudOperation.BULK_OPERATION, "softDeleteMultiple") {
        val tasks = listOf(
            createTestTask(userId = 9999, taskId = 60),
            createTestTask(userId = 9999, taskId = 61)
        )
        taskDao.insertAll(tasks)
        
        taskDao.softDeleteMultiple(9999, listOf(60, 61))
        
        // Cleanup
        taskDao.deleteAllTasksForUser(9999)
    }
    
    private suspend fun testMarkMultipleCompleted() = executeTest(CrudOperation.BULK_OPERATION, "markMultipleCompleted") {
        val tasks = listOf(
            createTestTask(userId = 9999, taskId = 70),
            createTestTask(userId = 9999, taskId = 71)
        )
        taskDao.insertAll(tasks)
        
        taskDao.markMultipleCompleted(9999, listOf(70, 71))
        
        // Cleanup
        taskDao.deleteAllTasksForUser(9999)
    }
    
    private fun createTestTask(
        userId: Int,
        taskId: Int,
        title: String = "Test Task $taskId"
    ): Task {
        return Task(
            User_ID = userId,
            Task_ID = taskId,
            Title = title,
            Description = "Test Description",
            Status = "Pending",
            Priority = Priority.MEDIUM,
            DueAt = "2024-12-31",
            DueTime = "23:59",
            TaskProgress = 0,
            CreatedAt = System.currentTimeMillis(),
            UpdatedAt = System.currentTimeMillis(),
            DeletedAt = null
        )
    }
}
