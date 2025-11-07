package com.example.cheermateapp.validation.crud

import com.example.cheermateapp.data.dao.SubTaskDao
import com.example.cheermateapp.data.model.SubTask

/**
 * CRUD validator for SubTaskDao
 */
class SubTaskDaoCrudChecker(private val subTaskDao: SubTaskDao) : BaseCrudChecker() {
    
    override fun getDaoName(): String = "SubTaskDao"
    
    override suspend fun validateDao(): List<CrudTestResult> {
        val results = mutableListOf<CrudTestResult>()
        
        results.add(testInsert())
        results.add(testUpdate())
        results.add(testDelete())
        results.add(testGetAllSubTasks())
        results.add(testList())
        results.add(testGetSubTasksByTask())
        results.add(testGetNextSubtaskId())
        
        return results
    }
    
    private suspend fun testInsert() = executeTest(CrudOperation.CREATE, "insert") {
        val testSubTask = createTestSubTask()
        val insertedId = subTaskDao.insert(testSubTask)
        assert(insertedId > 0) { "Insert should return positive ID" }
        
        // Cleanup
        subTaskDao.delete(testSubTask)
    }
    
    private suspend fun testUpdate() = executeTest(CrudOperation.UPDATE, "update") {
        val testSubTask = createTestSubTask()
        subTaskDao.insert(testSubTask)
        
        val updated = testSubTask.copy(Title = "Updated SubTask")
        subTaskDao.update(updated)
        
        // Cleanup
        subTaskDao.delete(updated)
    }
    
    private suspend fun testDelete() = executeTest(CrudOperation.DELETE, "delete") {
        val testSubTask = createTestSubTask()
        subTaskDao.insert(testSubTask)
        subTaskDao.delete(testSubTask)
    }
    
    private suspend fun testGetAllSubTasks() = executeTest(CrudOperation.READ, "getAllSubTasks") {
        subTaskDao.getAllSubTasks()
    }
    
    private suspend fun testList() = executeTest(CrudOperation.QUERY, "list") {
        subTaskDao.list(9999, 9999)
    }
    
    private suspend fun testGetSubTasksByTask() = executeTest(CrudOperation.QUERY, "getSubTasksByTask") {
        subTaskDao.getSubTasksByTask(9999)
    }
    
    private suspend fun testGetNextSubtaskId() = executeTest(CrudOperation.QUERY, "getNextSubtaskId") {
        val nextId = subTaskDao.getNextSubtaskId(9999, 9999)
        assert(nextId > 0) { "Next ID should be positive" }
    }
    
    private fun createTestSubTask(): SubTask {
        return SubTask(
            User_ID = 9999,
            Task_ID = 9999,
            Subtask_ID = 1,
            Title = "Test SubTask",
            IsCompleted = false,
            SortOrder = 0
        )
    }
}
