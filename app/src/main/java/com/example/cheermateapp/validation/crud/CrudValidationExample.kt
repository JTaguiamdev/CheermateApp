package com.example.cheermateapp.validation.crud

import android.content.Context
import com.example.cheermateapp.data.db.AppDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Example usage of the CRUD Validation System
 * 
 * This file demonstrates how to use the automated CRUD checker module
 * to validate all DAO operations in the application.
 */
object CrudValidationExample {
    
    /**
     * Example 1: Run full validation for all DAOs
     */
    fun runFullValidation(context: Context) {
        val database = AppDb.get(context)
        val orchestrator = CrudValidationOrchestrator(database)
        
        CoroutineScope(Dispatchers.IO).launch {
            // Run comprehensive validation
            val report = orchestrator.runFullValidation()
            
            // Print formatted report
            println(report.toFormattedString())
            
            // Check overall health
            if (report.overallHealthScore >= 95.0) {
                println("✅ All DAOs are healthy!")
            } else {
                println("⚠️ Some DAOs need attention")
                
                // Get unhealthy DAOs
                report.getUnhealthyDaos().forEach { dao ->
                    println("Unhealthy DAO: ${dao.daoName}")
                    println("  Health Score: ${dao.healthScore}%")
                    println("  Recommendations:")
                    dao.recommendations.forEach { rec ->
                        println("    - $rec")
                    }
                }
            }
            
            // Check for critical issues
            if (report.criticalIssuesCount > 0) {
                println("🚨 Critical issues found: ${report.criticalIssuesCount}")
                report.getDaosWithCriticalIssues().forEach { dao ->
                    println("  ${dao.daoName}: ${dao.criticalIssues} critical issue(s)")
                }
            }
        }
    }
    
    /**
     * Example 2: Validate a specific DAO
     */
    fun validateSpecificDao(context: Context, daoName: String) {
        val database = AppDb.get(context)
        val orchestrator = CrudValidationOrchestrator(database)
        
        CoroutineScope(Dispatchers.IO).launch {
            val summary = orchestrator.validateSpecificDao(daoName)
            
            if (summary != null) {
                println("DAO: ${summary.daoName}")
                println("Health Score: ${summary.healthScore}%")
                println("Operations: ${summary.successfulOperations}/${summary.totalOperations} successful")
                println("Average Execution Time: ${summary.averageExecutionTimeMs}ms")
                
                if (summary.recommendations.isNotEmpty()) {
                    println("Recommendations:")
                    summary.recommendations.forEach { rec ->
                        println("  - $rec")
                    }
                }
            } else {
                println("DAO not found: $daoName")
            }
        }
    }
    
    /**
     * Example 3: Get only failed tests
     */
    fun analyzeFailures(context: Context) {
        val database = AppDb.get(context)
        val orchestrator = CrudValidationOrchestrator(database)
        
        CoroutineScope(Dispatchers.IO).launch {
            val report = orchestrator.runFullValidation()
            
            val failedTests = report.getFailedTests()
            
            if (failedTests.isEmpty()) {
                println("✅ No failed tests!")
            } else {
                println("Failed Tests Summary:")
                failedTests.forEach { test ->
                    println("\n❌ ${test.daoName}.${test.methodName}")
                    println("   Operation: ${test.operation}")
                    println("   Error: ${test.errorMessage}")
                    println("   Severity: ${test.severity}")
                    test.recommendation?.let { rec ->
                        println("   Recommendation: $rec")
                    }
                }
            }
        }
    }
    
    /**
     * Example 4: Performance analysis
     */
    fun analyzePerformance(context: Context) {
        val database = AppDb.get(context)
        val orchestrator = CrudValidationOrchestrator(database)
        
        CoroutineScope(Dispatchers.IO).launch {
            val report = orchestrator.runFullValidation()
            
            println("Performance Analysis:")
            println("Total Execution Time: ${report.totalExecutionTimeMs}ms")
            println("Total Tests: ${report.totalTests}")
            println("Average Time Per Test: ${report.totalExecutionTimeMs / report.totalTests}ms")
            
            // Find slowest DAOs
            val slowestDaos = report.daoSummaries
                .sortedByDescending { it.averageExecutionTimeMs }
                .take(3)
            
            println("\nSlowest DAOs:")
            slowestDaos.forEach { dao ->
                println("  ${dao.daoName}: ${dao.averageExecutionTimeMs}ms average")
            }
        }
    }
    
    /**
     * Example 5: Integration with CI/CD
     * Returns exit code 0 if healthy, 1 if there are issues
     */
    suspend fun validateForCI(context: Context): Int {
        val database = AppDb.get(context)
        val orchestrator = CrudValidationOrchestrator(database)
        
        val report = orchestrator.runFullValidation()
        
        // Log the full report
        println(report.toFormattedString())
        
        // Determine exit code
        return when {
            report.criticalIssuesCount > 0 -> {
                System.err.println("❌ CI VALIDATION FAILED: ${report.criticalIssuesCount} critical issues")
                1
            }
            report.overallHealthScore < 95.0 -> {
                System.err.println("⚠️ CI VALIDATION WARNING: Health score below 95%")
                1
            }
            else -> {
                println("✅ CI VALIDATION PASSED: All DAOs healthy")
                0
            }
        }
    }
}
