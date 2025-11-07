package com.example.cheermateapp.validation.crud

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for DaoHealthSummary
 */
class DaoHealthSummaryTest {
    
    @Test
    fun testHealthScore_AllSuccessful() {
        val summary = DaoHealthSummary(
            daoName = "TestDao",
            totalOperations = 10,
            successfulOperations = 10,
            failedOperations = 0,
            averageExecutionTimeMs = 10.0,
            criticalIssues = 0,
            warnings = 0,
            recommendations = emptyList()
        )
        
        assertEquals(100.0, summary.healthScore, 0.01)
        assertTrue("Should be healthy", summary.isHealthy)
    }
    
    @Test
    fun testHealthScore_PartialSuccess() {
        val summary = DaoHealthSummary(
            daoName = "TestDao",
            totalOperations = 10,
            successfulOperations = 7,
            failedOperations = 3,
            averageExecutionTimeMs = 10.0,
            criticalIssues = 0,
            warnings = 0,
            recommendations = emptyList()
        )
        
        assertEquals(70.0, summary.healthScore, 0.01)
        assertFalse("Should not be healthy with 70% score", summary.isHealthy)
    }
    
    @Test
    fun testHealthScore_ZeroOperations() {
        val summary = DaoHealthSummary(
            daoName = "TestDao",
            totalOperations = 0,
            successfulOperations = 0,
            failedOperations = 0,
            averageExecutionTimeMs = 0.0,
            criticalIssues = 0,
            warnings = 0,
            recommendations = emptyList()
        )
        
        assertEquals(0.0, summary.healthScore, 0.01)
    }
    
    @Test
    fun testIsHealthy_WithCriticalIssues() {
        val summary = DaoHealthSummary(
            daoName = "TestDao",
            totalOperations = 10,
            successfulOperations = 10,
            failedOperations = 0,
            averageExecutionTimeMs = 10.0,
            criticalIssues = 1,
            warnings = 0,
            recommendations = emptyList()
        )
        
        assertEquals(100.0, summary.healthScore, 0.01)
        assertFalse("Should not be healthy with critical issues", summary.isHealthy)
    }
    
    @Test
    fun testIsHealthy_BoundaryCase() {
        // Test exactly 95% success rate
        val summary = DaoHealthSummary(
            daoName = "TestDao",
            totalOperations = 20,
            successfulOperations = 19,
            failedOperations = 1,
            averageExecutionTimeMs = 10.0,
            criticalIssues = 0,
            warnings = 0,
            recommendations = emptyList()
        )
        
        assertEquals(95.0, summary.healthScore, 0.01)
        assertTrue("Should be healthy at 95%", summary.isHealthy)
    }
    
    @Test
    fun testIsHealthy_JustBelowThreshold() {
        // Test just below 95% success rate
        val summary = DaoHealthSummary(
            daoName = "TestDao",
            totalOperations = 20,
            successfulOperations = 18,
            failedOperations = 2,
            averageExecutionTimeMs = 10.0,
            criticalIssues = 0,
            warnings = 0,
            recommendations = emptyList()
        )
        
        assertEquals(90.0, summary.healthScore, 0.01)
        assertFalse("Should not be healthy below 95%", summary.isHealthy)
    }
}
