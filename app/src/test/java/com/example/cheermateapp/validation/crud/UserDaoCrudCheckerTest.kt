package com.example.cheermateapp.validation.crud

import com.example.cheermateapp.data.dao.UserDao
import com.example.cheermateapp.data.model.User
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit tests for UserDaoCrudChecker
 * Demonstrates how to test the CRUD validation system
 */
@RunWith(MockitoJUnitRunner::class)
class UserDaoCrudCheckerTest {
    
    @Mock
    private lateinit var mockUserDao: UserDao
    
    private lateinit var checker: UserDaoCrudChecker
    
    @Before
    fun setup() {
        checker = UserDaoCrudChecker(mockUserDao)
    }
    
    @Test
    fun testGetDaoName() {
        assertEquals("UserDao", checker.getDaoName())
    }
    
    @Test
    fun testValidateDao_AllOperationsSuccessful() = runBlocking {
        // Mock successful operations
        val testUser = createTestUser()
        `when`(mockUserDao.insert(testUser)).thenReturn(1L)
        `when`(mockUserDao.getById(99996)).thenReturn(createTestUser(99996))
        `when`(mockUserDao.findByUsername("unique_username_99995")).thenReturn(createTestUser(99995))
        `when`(mockUserDao.findByEmail("unique99994@test.com")).thenReturn(createTestUser(99994))
        `when`(mockUserDao.getAllUsers()).thenReturn(listOf(createTestUser()))
        `when`(mockUserDao.getUserCount()).thenReturn(1)
        `when`(mockUserDao.deleteById(org.mockito.ArgumentMatchers.anyInt())).thenReturn(1)
        
        val results = checker.validateDao()
        
        // Verify all tests ran
        assertTrue("Should have multiple test results", results.size >= 8)
        
        // Check that some operations were tested
        val operations = results.map { it.operation }.toSet()
        assertTrue("Should test CREATE operations", operations.contains(CrudOperation.CREATE))
        assertTrue("Should test READ operations", operations.contains(CrudOperation.READ))
        assertTrue("Should test UPDATE operations", operations.contains(CrudOperation.UPDATE))
        assertTrue("Should test DELETE operations", operations.contains(CrudOperation.DELETE))
    }
    
    @Test
    fun testGenerateHealthSummary_AllSuccess() {
        val results = listOf(
            CrudTestResult("UserDao", CrudOperation.CREATE, "insert", true, executionTimeMs = 10),
            CrudTestResult("UserDao", CrudOperation.READ, "getById", true, executionTimeMs = 5),
            CrudTestResult("UserDao", CrudOperation.UPDATE, "update", true, executionTimeMs = 8)
        )
        
        val summary = checker.generateHealthSummary(results)
        
        assertEquals("UserDao", summary.daoName)
        assertEquals(3, summary.totalOperations)
        assertEquals(3, summary.successfulOperations)
        assertEquals(0, summary.failedOperations)
        assertEquals(100.0, summary.healthScore, 0.01)
        assertTrue("Should be healthy", summary.isHealthy)
        assertEquals(0, summary.criticalIssues)
    }
    
    @Test
    fun testGenerateHealthSummary_WithFailures() {
        val results = listOf(
            CrudTestResult("UserDao", CrudOperation.CREATE, "insert", true, executionTimeMs = 10),
            CrudTestResult("UserDao", CrudOperation.READ, "getById", false, 
                errorMessage = "Not found", severity = IssueSeverity.CRITICAL,
                recommendation = "Check database", executionTimeMs = 5),
            CrudTestResult("UserDao", CrudOperation.UPDATE, "update", true, executionTimeMs = 8)
        )
        
        val summary = checker.generateHealthSummary(results)
        
        assertEquals(3, summary.totalOperations)
        assertEquals(2, summary.successfulOperations)
        assertEquals(1, summary.failedOperations)
        assertEquals(66.67, summary.healthScore, 0.1)
        assertFalse("Should not be healthy with failures", summary.isHealthy)
        assertEquals(1, summary.criticalIssues)
        assertTrue("Should have recommendations", summary.recommendations.isNotEmpty())
    }
    
    @Test
    fun testGenerateHealthSummary_PerformanceWarning() {
        val results = listOf(
            CrudTestResult("UserDao", CrudOperation.CREATE, "insert", true, executionTimeMs = 150),
            CrudTestResult("UserDao", CrudOperation.READ, "getById", true, executionTimeMs = 120)
        )
        
        val summary = checker.generateHealthSummary(results)
        
        assertTrue("Should have performance recommendation", 
            summary.recommendations.any { it.contains("optimizing queries") })
    }
    
    private fun createTestUser(userId: Int = 1): User {
        return User(
            User_ID = userId,
            Username = "testuser$userId",
            Email = "test$userId@example.com",
            PasswordHash = "hash123",
            Personality_ID = null
        )
    }
}
