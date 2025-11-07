package com.example.cheermateapp.validation.crud

import com.example.cheermateapp.data.dao.TaskReminderDao
import com.example.cheermateapp.data.model.TaskReminder

/**
 * CRUD validator for TaskReminderDao
 */
class TaskReminderDaoCrudChecker(private val taskReminderDao: TaskReminderDao) : BaseCrudChecker() {
    
    override fun getDaoName(): String = "TaskReminderDao"
    
    override suspend fun validateDao(): List<CrudTestResult> {
        val results = mutableListOf<CrudTestResult>()
        
        results.add(testInsert())
        results.add(testUpdate())
        results.add(testDelete())
        results.add(testGetAllReminders())
        results.add(testActiveForTask())
        results.add(testGetRemindersByTask())
        
        return results
    }
    
    private suspend fun testInsert() = executeTest(CrudOperation.CREATE, "insert") {
        val test = createTestReminder()
        val insertedId = taskReminderDao.insert(test)
        assert(insertedId > 0) { "Insert should return positive ID" }
        
        // Cleanup
        taskReminderDao.delete(test)
    }
    
    private suspend fun testUpdate() = executeTest(CrudOperation.UPDATE, "update") {
        val test = createTestReminder()
        taskReminderDao.insert(test)
        
        val updated = test.copy(RemindAt = System.currentTimeMillis() + 10000)
        taskReminderDao.update(updated)
        
        // Cleanup
        taskReminderDao.delete(updated)
    }
    
    private suspend fun testDelete() = executeTest(CrudOperation.DELETE, "delete") {
        val test = createTestReminder()
        taskReminderDao.insert(test)
        taskReminderDao.delete(test)
    }
    
    private suspend fun testGetAllReminders() = executeTest(CrudOperation.READ, "getAllReminders") {
        taskReminderDao.getAllReminders()
    }
    
    private suspend fun testActiveForTask() = executeTest(CrudOperation.QUERY, "activeForTask") {
        taskReminderDao.activeForTask(9999, 9999)
    }
    
    private suspend fun testGetRemindersByTask() = executeTest(CrudOperation.QUERY, "getRemindersByTask") {
        taskReminderDao.getRemindersByTask(9999)
    }
    
    private fun createTestReminder(): TaskReminder {
        return TaskReminder(
            Reminder_ID = null,
            User_ID = 9999,
            Task_ID = 9999,
            RemindAt = System.currentTimeMillis() + 3600000,
            IsActive = true
        )
    }
}
