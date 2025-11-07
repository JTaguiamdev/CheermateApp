package com.example.cheermateapp.validation.crud

import com.example.cheermateapp.data.dao.RecurringTaskDao
import com.example.cheermateapp.data.model.RecurringTask
import com.example.cheermateapp.data.model.RecurringFrequency
import com.example.cheermateapp.data.model.Priority

/**
 * CRUD validator for RecurringTaskDao
 */
class RecurringTaskDaoCrudChecker(private val recurringTaskDao: RecurringTaskDao) : BaseCrudChecker() {
    
    companion object {
        private const val TEST_DATE = "2099-12-31"
    }
    
    override fun getDaoName(): String = "RecurringTaskDao"
    
    override suspend fun validateDao(): List<CrudTestResult> {
        val results = mutableListOf<CrudTestResult>()
        
        results.add(testInsert())
        results.add(testUpdate())
        results.add(testDelete())
        results.add(testInsertOrReplace())
        results.add(testGetAllRecurringTasks())
        results.add(testGetRecurringTaskById())
        results.add(testGetActiveRecurringTasks())
        results.add(testToggleActive())
        results.add(testUpdateLastGenerated())
        results.add(testGetRecurringTaskCount())
        results.add(testDeleteById())
        results.add(testGetNextRecurringTaskId())
        
        return results
    }
    
    private suspend fun testInsert() = executeTest(CrudOperation.CREATE, "insert") {
        val test = createTestRecurringTask()
        val insertedId = recurringTaskDao.insert(test)
        assert(insertedId > 0) { "Insert should return positive ID" }
        
        // Cleanup
        recurringTaskDao.deleteById(9999, 99999)
    }
    
    private suspend fun testUpdate() = executeTest(CrudOperation.UPDATE, "update") {
        val test = createTestRecurringTask()
        recurringTaskDao.insert(test)
        
        val updated = test.copy(Title = "Updated Title")
        recurringTaskDao.update(updated)
        
        // Cleanup
        recurringTaskDao.deleteById(9999, 99999)
    }
    
    private suspend fun testDelete() = executeTest(CrudOperation.DELETE, "delete") {
        val test = createTestRecurringTask()
        recurringTaskDao.insert(test)
        recurringTaskDao.delete(test)
    }
    
    private suspend fun testInsertOrReplace() = executeTest(CrudOperation.CREATE, "insertOrReplace") {
        val test = createTestRecurringTask()
        val insertedId = recurringTaskDao.insertOrReplace(test)
        assert(insertedId > 0) { "InsertOrReplace should return positive ID" }
        
        // Cleanup
        recurringTaskDao.deleteById(9999, 99999)
    }
    
    private suspend fun testGetAllRecurringTasks() = executeTest(CrudOperation.READ, "getAllRecurringTasks") {
        recurringTaskDao.getAllRecurringTasks(9999)
    }
    
    private suspend fun testGetRecurringTaskById() = executeTest(CrudOperation.READ, "getRecurringTaskById") {
        recurringTaskDao.getRecurringTaskById(9999, 1)
    }
    
    private suspend fun testGetActiveRecurringTasks() = executeTest(CrudOperation.QUERY, "getActiveRecurringTasks") {
        recurringTaskDao.getActiveRecurringTasks(9999)
    }
    
    private suspend fun testToggleActive() = executeTest(CrudOperation.UPDATE, "toggleActive") {
        val test = createTestRecurringTask()
        recurringTaskDao.insert(test)
        
        recurringTaskDao.toggleActive(9999, 99999, false)
        
        // Cleanup
        recurringTaskDao.deleteById(9999, 99999)
    }
    
    private suspend fun testUpdateLastGenerated() = executeTest(CrudOperation.UPDATE, "updateLastGenerated") {
        val test = createTestRecurringTask()
        recurringTaskDao.insert(test)
        
        recurringTaskDao.updateLastGenerated(9999, 99999, TEST_DATE)
        
        // Cleanup
        recurringTaskDao.deleteById(9999, 99999)
    }
    
    private suspend fun testGetRecurringTaskCount() = executeTest(CrudOperation.COUNT, "getRecurringTaskCount") {
        val count = recurringTaskDao.getRecurringTaskCount(9999)
        assert(count >= 0) { "Count should be non-negative" }
    }
    
    private suspend fun testDeleteById() = executeTest(CrudOperation.DELETE, "deleteById") {
        val test = createTestRecurringTask()
        recurringTaskDao.insert(test)
        recurringTaskDao.deleteById(9999, 99999)
    }
    
    private suspend fun testGetNextRecurringTaskId() = executeTest(CrudOperation.QUERY, "getNextRecurringTaskId") {
        val nextId = recurringTaskDao.getNextRecurringTaskId(9999)
        assert(nextId > 0) { "Next ID should be positive" }
    }
    
    private fun createTestRecurringTask(): RecurringTask {
        return RecurringTask(
            User_ID = 9999,
            RecurringTask_ID = 99999,
            Title = "Test Recurring Task",
            Description = "Test Description",
            Frequency = RecurringFrequency.DAILY,
            Priority = Priority.MEDIUM,
            IsActive = true,
            LastGenerated = null,
            CreatedAt = System.currentTimeMillis(),
            UpdatedAt = System.currentTimeMillis()
        )
    }
}
