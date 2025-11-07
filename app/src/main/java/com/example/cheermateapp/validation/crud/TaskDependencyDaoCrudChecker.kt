package com.example.cheermateapp.validation.crud

import com.example.cheermateapp.data.dao.TaskDependencyDao
import com.example.cheermateapp.data.model.TaskDependency

/**
 * CRUD validator for TaskDependencyDao
 */
class TaskDependencyDaoCrudChecker(private val taskDependencyDao: TaskDependencyDao) : BaseCrudChecker() {
    
    override fun getDaoName(): String = "TaskDependencyDao"
    
    override suspend fun validateDao(): List<CrudTestResult> {
        val results = mutableListOf<CrudTestResult>()
        
        results.add(testInsert())
        results.add(testDelete())
        results.add(testGetPrerequisiteTasks())
        results.add(testGetDependentTasks())
        results.add(testCountIncompletePrerequisites())
        results.add(testGetDependenciesForTask())
        results.add(testDeleteAllDependenciesForTask())
        results.add(testDeleteDependency())
        results.add(testCanTaskBeStarted())
        
        return results
    }
    
    private suspend fun testInsert() = executeTest(CrudOperation.CREATE, "insert") {
        val test = createTestDependency()
        taskDependencyDao.insert(test)
        
        // Cleanup
        taskDependencyDao.delete(test)
    }
    
    private suspend fun testDelete() = executeTest(CrudOperation.DELETE, "delete") {
        val test = createTestDependency()
        taskDependencyDao.insert(test)
        taskDependencyDao.delete(test)
    }
    
    private suspend fun testGetPrerequisiteTasks() = executeTest(CrudOperation.QUERY, "getPrerequisiteTasks") {
        taskDependencyDao.getPrerequisiteTasks(9999, 9999)
    }
    
    private suspend fun testGetDependentTasks() = executeTest(CrudOperation.QUERY, "getDependentTasks") {
        taskDependencyDao.getDependentTasks(9999, 9999)
    }
    
    private suspend fun testCountIncompletePrerequisites() = executeTest(CrudOperation.COUNT, "countIncompletePrerequisites") {
        val count = taskDependencyDao.countIncompletePrerequisites(9999, 9999)
        assert(count >= 0) { "Count should be non-negative" }
    }
    
    private suspend fun testGetDependenciesForTask() = executeTest(CrudOperation.QUERY, "getDependenciesForTask") {
        taskDependencyDao.getDependenciesForTask(9999, 9999)
    }
    
    private suspend fun testDeleteAllDependenciesForTask() = executeTest(CrudOperation.DELETE, "deleteAllDependenciesForTask") {
        val test = createTestDependency()
        taskDependencyDao.insert(test)
        taskDependencyDao.deleteAllDependenciesForTask(9999, 9999)
    }
    
    private suspend fun testDeleteDependency() = executeTest(CrudOperation.DELETE, "deleteDependency") {
        val test = createTestDependency()
        taskDependencyDao.insert(test)
        taskDependencyDao.deleteDependency(9999, 9999, 9999, 9998)
    }
    
    private suspend fun testCanTaskBeStarted() = executeTest(CrudOperation.QUERY, "canTaskBeStarted") {
        val result = taskDependencyDao.canTaskBeStarted(9999, 9999)
        // Should return boolean without throwing
    }
    
    private fun createTestDependency(): TaskDependency {
        return TaskDependency(
            User_ID = 9999,
            Task_ID = 9999,
            DependsOn_User_ID = 9999,
            DependsOn_Task_ID = 9998
        )
    }
}
