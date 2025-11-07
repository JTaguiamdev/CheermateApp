package com.example.cheermateapp.validation.crud

/**
 * Comprehensive health report for all DAOs in the application
 * 
 * @property daoSummaries Health summaries for each DAO
 * @property testResults Detailed test results for all operations
 * @property overallHealthScore Overall health score across all DAOs
 * @property totalTests Total number of tests executed
 * @property passedTests Number of tests that passed
 * @property failedTests Number of tests that failed
 * @property totalExecutionTimeMs Total execution time for all tests
 * @property criticalIssuesCount Total number of critical issues
 * @property warningsCount Total number of warnings
 * @property timestamp When the report was generated
 */
data class CrudHealthReport(
    val daoSummaries: List<DaoHealthSummary>,
    val testResults: List<CrudTestResult>,
    val overallHealthScore: Double,
    val totalTests: Int,
    val passedTests: Int,
    val failedTests: Int,
    val totalExecutionTimeMs: Long,
    val criticalIssuesCount: Int,
    val warningsCount: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        private const val REPORT_WIDTH = 80
    }
    
    /**
     * Get all DAOs with critical issues
     */
    fun getDaosWithCriticalIssues(): List<DaoHealthSummary> {
        return daoSummaries.filter { it.criticalIssues > 0 }
    }
    
    /**
     * Get all DAOs that are unhealthy
     */
    fun getUnhealthyDaos(): List<DaoHealthSummary> {
        return daoSummaries.filter { !it.isHealthy }
    }
    
    /**
     * Get failed test results
     */
    fun getFailedTests(): List<CrudTestResult> {
        return testResults.filter { !it.success }
    }
    
    /**
     * Get all recommendations across all DAOs
     */
    fun getAllRecommendations(): List<String> {
        return daoSummaries.flatMap { it.recommendations }.distinct()
    }
    
    /**
     * Generate a formatted text report
     */
    fun toFormattedString(): String {
        val sb = StringBuilder()
        sb.appendLine("=".repeat(REPORT_WIDTH))
        sb.appendLine("CRUD HEALTH REPORT")
        sb.appendLine("=".repeat(REPORT_WIDTH))
        sb.appendLine("Generated: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timestamp)}")
        sb.appendLine()
        sb.appendLine("OVERALL STATISTICS:")
        sb.appendLine("  Overall Health Score: %.2f%%".format(overallHealthScore))
        sb.appendLine("  Total Tests: $totalTests")
        sb.appendLine("  Passed: $passedTests")
        sb.appendLine("  Failed: $failedTests")
        sb.appendLine("  Total Execution Time: ${totalExecutionTimeMs}ms")
        sb.appendLine("  Critical Issues: $criticalIssuesCount")
        sb.appendLine("  Warnings: $warningsCount")
        sb.appendLine()
        
        if (getDaosWithCriticalIssues().isNotEmpty()) {
            sb.appendLine("⚠️  DAOS WITH CRITICAL ISSUES:")
            getDaosWithCriticalIssues().forEach { dao ->
                sb.appendLine("  - ${dao.daoName}: ${dao.criticalIssues} critical issue(s)")
            }
            sb.appendLine()
        }
        
        sb.appendLine("DAO HEALTH SUMMARIES:")
        daoSummaries.forEach { dao ->
            val status = if (dao.isHealthy) "✅" else "❌"
            sb.appendLine("  $status ${dao.daoName}")
            sb.appendLine("     Health Score: %.2f%%".format(dao.healthScore))
            sb.appendLine("     Operations: ${dao.successfulOperations}/${dao.totalOperations} successful")
            sb.appendLine("     Avg Execution Time: %.2fms".format(dao.averageExecutionTimeMs))
            if (dao.criticalIssues > 0 || dao.warnings > 0) {
                sb.appendLine("     Issues: ${dao.criticalIssues} critical, ${dao.warnings} warnings")
            }
            if (dao.recommendations.isNotEmpty()) {
                sb.appendLine("     Recommendations:")
                dao.recommendations.forEach { rec ->
                    sb.appendLine("       - $rec")
                }
            }
            sb.appendLine()
        }
        
        if (getFailedTests().isNotEmpty()) {
            sb.appendLine("FAILED TESTS:")
            getFailedTests().forEach { test ->
                sb.appendLine("  ❌ ${test.daoName}.${test.methodName} (${test.operation})")
                sb.appendLine("     Error: ${test.errorMessage}")
                test.recommendation?.let { rec ->
                    sb.appendLine("     Recommendation: $rec")
                }
            }
        }
        
        sb.appendLine("=".repeat(REPORT_WIDTH))
        return sb.toString()
    }
}
