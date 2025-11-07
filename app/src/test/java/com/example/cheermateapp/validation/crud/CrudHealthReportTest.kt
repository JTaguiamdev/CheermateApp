package com.example.cheermateapp.validation.crud

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for CrudHealthReport
 */
class CrudHealthReportTest {
    
    @Test
    fun testGetDaosWithCriticalIssues() {
        val summaries = listOf(
            createHealthSummary("Dao1", criticalIssues = 2),
            createHealthSummary("Dao2", criticalIssues = 0),
            createHealthSummary("Dao3", criticalIssues = 1)
        )
        
        val report = createReport(summaries)
        val criticalDaos = report.getDaosWithCriticalIssues()
        
        assertEquals(2, criticalDaos.size)
        assertTrue(criticalDaos.any { it.daoName == "Dao1" })
        assertTrue(criticalDaos.any { it.daoName == "Dao3" })
    }
    
    @Test
    fun testGetUnhealthyDaos() {
        val summaries = listOf(
            createHealthSummary("Dao1", successfulOps = 10, totalOps = 10), // 100% healthy
            createHealthSummary("Dao2", successfulOps = 9, totalOps = 10),  // 90% unhealthy
            createHealthSummary("Dao3", successfulOps = 10, totalOps = 10)  // 100% healthy
        )
        
        val report = createReport(summaries)
        val unhealthyDaos = report.getUnhealthyDaos()
        
        assertEquals(1, unhealthyDaos.size)
        assertEquals("Dao2", unhealthyDaos[0].daoName)
    }
    
    @Test
    fun testGetFailedTests() {
        val results = listOf(
            CrudTestResult("Dao1", CrudOperation.CREATE, "insert", true),
            CrudTestResult("Dao1", CrudOperation.READ, "get", false, "Error"),
            CrudTestResult("Dao2", CrudOperation.UPDATE, "update", false, "Error2")
        )
        
        val report = createReport(emptyList(), results)
        val failedTests = report.getFailedTests()
        
        assertEquals(2, failedTests.size)
        assertTrue(failedTests.all { !it.success })
    }
    
    @Test
    fun testGetAllRecommendations() {
        val summaries = listOf(
            createHealthSummary("Dao1", recommendations = listOf("Rec1", "Rec2")),
            createHealthSummary("Dao2", recommendations = listOf("Rec2", "Rec3")),
            createHealthSummary("Dao3", recommendations = listOf("Rec1"))
        )
        
        val report = createReport(summaries)
        val recommendations = report.getAllRecommendations()
        
        assertEquals(3, recommendations.size)
        assertTrue(recommendations.contains("Rec1"))
        assertTrue(recommendations.contains("Rec2"))
        assertTrue(recommendations.contains("Rec3"))
    }
    
    @Test
    fun testToFormattedString_ContainsKeyInformation() {
        val summaries = listOf(
            createHealthSummary("TestDao", successfulOps = 8, totalOps = 10, criticalIssues = 1)
        )
        
        val report = createReport(summaries)
        val formatted = report.toFormattedString()
        
        assertTrue("Should contain title", formatted.contains("CRUD HEALTH REPORT"))
        assertTrue("Should contain overall score", formatted.contains("Overall Health Score"))
        assertTrue("Should contain total tests", formatted.contains("Total Tests"))
        assertTrue("Should contain DAO name", formatted.contains("TestDao"))
    }
    
    @Test
    fun testOverallHealthScore_Calculation() {
        val summaries = listOf(
            createHealthSummary("Dao1", successfulOps = 10, totalOps = 10), // 100%
            createHealthSummary("Dao2", successfulOps = 8, totalOps = 10),  // 80%
            createHealthSummary("Dao3", successfulOps = 9, totalOps = 10)   // 90%
        )
        
        val report = createReport(summaries)
        
        // Average: (100 + 80 + 90) / 3 = 90%
        assertEquals(90.0, report.overallHealthScore, 0.1)
    }
    
    @Test
    fun testReportMetrics() {
        val results = listOf(
            CrudTestResult("Dao1", CrudOperation.CREATE, "insert", true, executionTimeMs = 10),
            CrudTestResult("Dao1", CrudOperation.READ, "get", false, "Error", 
                executionTimeMs = 5, severity = IssueSeverity.CRITICAL),
            CrudTestResult("Dao2", CrudOperation.UPDATE, "update", true, executionTimeMs = 15)
        )
        
        val summaries = listOf(
            createHealthSummary("Dao1", successfulOps = 1, totalOps = 2, criticalIssues = 1),
            createHealthSummary("Dao2", successfulOps = 1, totalOps = 1)
        )
        
        val report = createReport(summaries, results)
        
        assertEquals(3, report.totalTests)
        assertEquals(2, report.passedTests)
        assertEquals(1, report.failedTests)
        assertEquals(30L, report.totalExecutionTimeMs)
        assertEquals(1, report.criticalIssuesCount)
        assertEquals(0, report.warningsCount)
    }
    
    private fun createHealthSummary(
        name: String,
        successfulOps: Int = 10,
        totalOps: Int = 10,
        criticalIssues: Int = 0,
        warnings: Int = 0,
        recommendations: List<String> = emptyList()
    ): DaoHealthSummary {
        return DaoHealthSummary(
            daoName = name,
            totalOperations = totalOps,
            successfulOperations = successfulOps,
            failedOperations = totalOps - successfulOps,
            averageExecutionTimeMs = 10.0,
            criticalIssues = criticalIssues,
            warnings = warnings,
            recommendations = recommendations
        )
    }
    
    private fun createReport(
        summaries: List<DaoHealthSummary>,
        results: List<CrudTestResult> = emptyList()
    ): CrudHealthReport {
        val totalTests = results.size
        val passedTests = results.count { it.success }
        val failedTests = results.count { !it.success }
        val totalExecutionTime = results.sumOf { it.executionTimeMs }
        val criticalIssues = results.count { it.severity == IssueSeverity.CRITICAL }
        val warnings = results.count { it.severity == IssueSeverity.WARNING }
        val overallScore = if (summaries.isNotEmpty()) {
            summaries.map { it.healthScore }.average()
        } else {
            0.0
        }
        
        return CrudHealthReport(
            daoSummaries = summaries,
            testResults = results,
            overallHealthScore = overallScore,
            totalTests = totalTests,
            passedTests = passedTests,
            failedTests = failedTests,
            totalExecutionTimeMs = totalExecutionTime,
            criticalIssuesCount = criticalIssues,
            warningsCount = warnings
        )
    }
}
