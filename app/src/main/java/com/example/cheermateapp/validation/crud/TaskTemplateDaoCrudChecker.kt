package com.example.cheermateapp.validation.crud

import com.example.cheermateapp.data.dao.TaskTemplateDao
import com.example.cheermateapp.data.model.TaskTemplate
import com.example.cheermateapp.data.model.Priority

/**
 * CRUD validator for TaskTemplateDao
 */
class TaskTemplateDaoCrudChecker(private val taskTemplateDao: TaskTemplateDao) : BaseCrudChecker() {
    
    override fun getDaoName(): String = "TaskTemplateDao"
    
    override suspend fun validateDao(): List<CrudTestResult> {
        val results = mutableListOf<CrudTestResult>()
        
        results.add(testInsert())
        results.add(testUpdate())
        results.add(testDelete())
        results.add(testInsertOrReplace())
        results.add(testGetAllTemplates())
        results.add(testGetTemplateById())
        results.add(testGetTemplatesByCategory())
        results.add(testSearchTemplates())
        results.add(testIncrementUsageCount())
        results.add(testGetTemplateCount())
        results.add(testDeleteById())
        results.add(testGetNextTemplateId())
        results.add(testInsertAll())
        
        return results
    }
    
    private suspend fun testInsert() = executeTest(CrudOperation.CREATE, "insert") {
        val test = createTestTemplate()
        val insertedId = taskTemplateDao.insert(test)
        assert(insertedId > 0) { "Insert should return positive ID" }
        
        // Cleanup
        taskTemplateDao.deleteById(9999, 99999)
    }
    
    private suspend fun testUpdate() = executeTest(CrudOperation.UPDATE, "update") {
        val test = createTestTemplate()
        taskTemplateDao.insert(test)
        
        val updated = test.copy(Name = "Updated Template")
        taskTemplateDao.update(updated)
        
        // Cleanup
        taskTemplateDao.deleteById(9999, 99999)
    }
    
    private suspend fun testDelete() = executeTest(CrudOperation.DELETE, "delete") {
        val test = createTestTemplate()
        taskTemplateDao.insert(test)
        taskTemplateDao.delete(test)
    }
    
    private suspend fun testInsertOrReplace() = executeTest(CrudOperation.CREATE, "insertOrReplace") {
        val test = createTestTemplate()
        val insertedId = taskTemplateDao.insertOrReplace(test)
        assert(insertedId > 0) { "InsertOrReplace should return positive ID" }
        
        // Cleanup
        taskTemplateDao.deleteById(9999, 99999)
    }
    
    private suspend fun testGetAllTemplates() = executeTest(CrudOperation.READ, "getAllTemplates") {
        taskTemplateDao.getAllTemplates(9999)
    }
    
    private suspend fun testGetTemplateById() = executeTest(CrudOperation.READ, "getTemplateById") {
        taskTemplateDao.getTemplateById(9999, 1)
    }
    
    private suspend fun testGetTemplatesByCategory() = executeTest(CrudOperation.QUERY, "getTemplatesByCategory") {
        taskTemplateDao.getTemplatesByCategory(9999, "Work")
    }
    
    private suspend fun testSearchTemplates() = executeTest(CrudOperation.QUERY, "searchTemplates") {
        taskTemplateDao.searchTemplates(9999, "test")
    }
    
    private suspend fun testIncrementUsageCount() = executeTest(CrudOperation.UPDATE, "incrementUsageCount") {
        val test = createTestTemplate()
        taskTemplateDao.insert(test)
        
        taskTemplateDao.incrementUsageCount(9999, 99999)
        
        // Cleanup
        taskTemplateDao.deleteById(9999, 99999)
    }
    
    private suspend fun testGetTemplateCount() = executeTest(CrudOperation.COUNT, "getTemplateCount") {
        val count = taskTemplateDao.getTemplateCount(9999)
        assert(count >= 0) { "Count should be non-negative" }
    }
    
    private suspend fun testDeleteById() = executeTest(CrudOperation.DELETE, "deleteById") {
        val test = createTestTemplate()
        taskTemplateDao.insert(test)
        taskTemplateDao.deleteById(9999, 99999)
    }
    
    private suspend fun testGetNextTemplateId() = executeTest(CrudOperation.QUERY, "getNextTemplateId") {
        val nextId = taskTemplateDao.getNextTemplateId(9999)
        assert(nextId > 0) { "Next ID should be positive" }
    }
    
    private suspend fun testInsertAll() = executeTest(CrudOperation.BULK_OPERATION, "insertAll") {
        val templates = listOf(
            createTestTemplate(templateId = 99990),
            createTestTemplate(templateId = 99991)
        )
        taskTemplateDao.insertAll(templates)
        
        // Cleanup
        taskTemplateDao.deleteById(9999, 99990)
        taskTemplateDao.deleteById(9999, 99991)
    }
    
    private fun createTestTemplate(templateId: Int = 99999): TaskTemplate {
        return TaskTemplate(
            User_ID = 9999,
            Template_ID = templateId,
            Name = "Test Template",
            Description = "Test Description",
            Priority = Priority.MEDIUM,
            Category = "Test",
            UsageCount = 0,
            CreatedAt = System.currentTimeMillis(),
            UpdatedAt = System.currentTimeMillis()
        )
    }
}
