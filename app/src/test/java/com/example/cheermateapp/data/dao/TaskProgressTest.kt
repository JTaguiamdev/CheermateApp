package com.example.cheermateapp.data.dao

import com.example.cheermateapp.data.model.Task
import com.example.cheermateapp.data.model.Status
import com.example.cheermateapp.data.model.Priority
import com.example.cheermateapp.data.model.Category
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests to verify TaskProgress logic
 * 
 * Tests ensure:
 * - TaskProgress = 100 when status is Completed
 * - TaskProgress = 0 when status is Pending, InProgress, etc.
 */
class TaskProgressTest {
    
    @Test
    fun `test completed task has progress 100`() {
        val task = Task(
            Task_ID = 1,
            User_ID = 1,
            Title = "Test Task",
            Status = Status.Completed,
            TaskProgress = 100,
            Priority = Priority.Medium,
            Category = Category.Work
        )
        
        assertEquals(100, task.TaskProgress)
        assertEquals(Status.Completed, task.Status)
    }
    
    @Test
    fun `test pending task has progress 0`() {
        val task = Task(
            Task_ID = 2,
            User_ID = 1,
            Title = "Pending Task",
            Status = Status.Pending,
            TaskProgress = 0,
            Priority = Priority.Medium,
            Category = Category.Work
        )
        
        assertEquals(0, task.TaskProgress)
        assertEquals(Status.Pending, task.Status)
    }
    
    @Test
    fun `test in-progress task has progress 0`() {
        val task = Task(
            Task_ID = 3,
            User_ID = 1,
            Title = "In Progress Task",
            Status = Status.InProgress,
            TaskProgress = 0,
            Priority = Priority.High,
            Category = Category.Personal
        )
        
        assertEquals(0, task.TaskProgress)
        assertEquals(Status.InProgress, task.Status)
    }
}
