package com.cheermateapp.data.repository

import com.cheermateapp.data.dao.TaskDao
import com.cheermateapp.data.dao.SubTaskDao
import com.cheermateapp.data.dao.TaskReminderDao
import com.cheermateapp.data.dao.TaskDependencyDao
import com.cheermateapp.data.model.Task
import com.cheermateapp.data.model.Priority
import com.cheermateapp.data.model.Status
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * Unit tests for TaskRepository
 * 
 * Demonstrates:
 * - Testing repository with mocked DAOs
 * - Testing DataResult success/error cases
 * - Testing Flow-based operations
 * - Testing error handling
 * 
 * NOTE: These are example tests. You'll need to:
 * 1. Add test dependencies to build.gradle
 * 2. Setup proper test infrastructure
 * 3. Create actual test cases based on your needs
 */
class TaskRepositoryTest {

    @Mock
    private lateinit var taskDao: TaskDao
    
    @Mock
    private lateinit var subTaskDao: SubTaskDao
    
    @Mock
    private lateinit var taskReminderDao: TaskReminderDao
    
    @Mock
    private lateinit var taskDependencyDao: TaskDependencyDao
    
    private lateinit var repository: TaskRepository
    
    private val sampleTask = Task(
        Task_ID = 1,
        User_ID = 1,
        Title = "Test Task",
        Description = "Test Description",
        Priority = Priority.High,
        Status = Status.Pending,
        CreatedAt = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = TaskRepository(taskDao, subTaskDao, taskReminderDao, taskDependencyDao)
    }

    // ==================== INSERT TASK TESTS ====================

    @Test
    fun `insertTask returns Success when insert succeeds`() = runTest {
        // Given
        `when`(taskDao.insert(sampleTask)).thenReturn(1L)
        
        // When
        val result = repository.insertTask(sampleTask)
        
        // Then
        assertTrue(result is DataResult.Success)
        assertEquals(1L, (result as DataResult.Success).data)
        verify(taskDao).insert(sampleTask)
    }

    @Test
    fun `insertTask returns Error when insert fails`() = runTest {
        // Given
        val exception = Exception("Database error")
        `when`(taskDao.insert(sampleTask)).thenThrow(exception)
        
        // When
        val result = repository.insertTask(sampleTask)
        
        // Then
        assertTrue(result is DataResult.Error)
        assertEquals(exception, (result as DataResult.Error).exception)
        assertNotNull(result.message)
    }

    // ==================== UPDATE TASK TESTS ====================

    @Test
    fun `updateTask returns Success when update succeeds`() = runTest {
        // Given
        `when`(taskDao.update(sampleTask)).thenReturn(Unit)
        
        // When
        val result = repository.updateTask(sampleTask)
        
        // Then
        assertTrue(result is DataResult.Success)
        verify(taskDao).update(sampleTask)
    }

    @Test
    fun `updateTask returns Error when update fails`() = runTest {
        // Given
        val exception = Exception("Update failed")
        `when`(taskDao.update(sampleTask)).thenThrow(exception)
        
        // When
        val result = repository.updateTask(sampleTask)
        
        // Then
        assertTrue(result is DataResult.Error)
        assertEquals(exception, (result as DataResult.Error).exception)
    }

    // ==================== DELETE TASK TESTS ====================

    @Test
    fun `softDeleteTask returns Success when delete succeeds`() = runTest {
        // Given
        `when`(taskDao.softDelete(1, 1)).thenReturn(Unit)
        
        // When
        val result = repository.softDeleteTask(1, 1)
        
        // Then
        assertTrue(result is DataResult.Success)
        verify(taskDao).softDelete(eq(1), eq(1), any(), any())
    }

    // ==================== GET TASKS TESTS ====================

    @Test
    fun `getAllTasks returns Success with tasks when query succeeds`() = runTest {
        // Given
        val tasks = listOf(sampleTask)
        `when`(taskDao.getAllTasksForUser(1)).thenReturn(tasks)
        
        // When
        val result = repository.getAllTasks(1)
        
        // Then
        assertTrue(result is DataResult.Success)
        assertEquals(tasks, (result as DataResult.Success).data)
        verify(taskDao).getAllTasksForUser(1)
    }

    @Test
    fun `getAllTasks returns Error when query fails`() = runTest {
        // Given
        val exception = Exception("Query failed")
        `when`(taskDao.getAllTasksForUser(1)).thenThrow(exception)
        
        // When
        val result = repository.getAllTasks(1)
        
        // Then
        assertTrue(result is DataResult.Error)
        assertEquals(exception, (result as DataResult.Error).exception)
    }

    // ==================== FLOW TESTS ====================

    @Test
    fun `getAllTasksFlow emits tasks from DAO`() = runTest {
        // Given
        val tasks = listOf(sampleTask)
        val flow = flowOf(tasks)
        `when`(taskDao.getAllTasksFlow(1)).thenReturn(flow)
        
        // When/Then
        repository.getAllTasksFlow(1).collect { emittedTasks ->
            assertEquals(tasks, emittedTasks)
        }
        
        verify(taskDao).getAllTasksFlow(1)
    }

    @Test
    fun `getAllTasksFlow emits empty list on error`() = runTest {
        // Given
        val flow = flowOf<List<Task>>().also {
            throw Exception("Flow error")
        }
        `when`(taskDao.getAllTasksFlow(1)).thenReturn(flow)
        
        // When/Then
        // Flow should catch error and emit empty list
        repository.getAllTasksFlow(1).collect { emittedTasks ->
            // In case of error, repository returns empty list
            assertTrue(emittedTasks.isEmpty())
        }
    }

    // ==================== MARK COMPLETED TESTS ====================

    @Test
    fun `markTaskCompleted returns Success when operation succeeds`() = runTest {
        // Given
        `when`(taskDao.markTaskCompleted(1, 1)).thenReturn(Unit)
        
        // When
        val result = repository.markTaskCompleted(1, 1)
        
        // Then
        assertTrue(result is DataResult.Success)
        verify(taskDao).markTaskCompleted(eq(1), eq(1), any())
    }

    // ==================== SEARCH TESTS ====================

    @Test
    fun `searchTasks returns Success with matching tasks`() = runTest {
        // Given
        val query = "Test"
        val matchingTasks = listOf(sampleTask)
        `when`(taskDao.searchTasks(1, "%$query%")).thenReturn(matchingTasks)
        
        // When
        val result = repository.searchTasks(1, query)
        
        // Then
        assertTrue(result is DataResult.Success)
        assertEquals(matchingTasks, (result as DataResult.Success).data)
    }

    // ==================== BATCH OPERATIONS TESTS ====================

    @Test
    fun `insertTasks returns Success when batch insert succeeds`() = runTest {
        // Given
        val tasks = listOf(sampleTask, sampleTask.copy(Task_ID = 2))
        `when`(taskDao.insertAll(tasks)).thenReturn(Unit)
        
        // When
        val result = repository.insertTasks(tasks)
        
        // Then
        assertTrue(result is DataResult.Success)
        verify(taskDao).insertAll(tasks)
    }

    @Test
    fun `softDeleteTasks returns Success when batch delete succeeds`() = runTest {
        // Given
        val taskIds = listOf(1, 2, 3)
        `when`(taskDao.softDeleteMultiple(1, taskIds)).thenReturn(Unit)
        
        // When
        val result = repository.softDeleteTasks(1, taskIds)
        
        // Then
        assertTrue(result is DataResult.Success)
        verify(taskDao).softDeleteMultiple(eq(1), eq(taskIds), any())
    }

    @Test
    fun `markTasksCompleted returns Success when batch complete succeeds`() = runTest {
        // Given
        val taskIds = listOf(1, 2, 3)
        `when`(taskDao.markMultipleCompleted(1, taskIds)).thenReturn(Unit)
        
        // When
        val result = repository.markTasksCompleted(1, taskIds)
        
        // Then
        assertTrue(result is DataResult.Success)
        verify(taskDao).markMultipleCompleted(eq(1), eq(taskIds), any())
    }
}

/**
 * To run these tests, add the following to app/build.gradle.kts:
 * 
 * dependencies {
 *     // Testing
 *     testImplementation("junit:junit:4.13.2")
 *     testImplementation("org.mockito:mockito-core:5.7.0")
 *     testImplementation("org.mockito:mockito-inline:5.2.0")
 *     testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
 *     testImplementation("androidx.arch.core:core-testing:2.2.0")
 * }
 */
