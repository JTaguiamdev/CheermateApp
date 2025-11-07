package com.example.cheermateapp.validation.crud

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

/**
 * Base class for DAO CRUD validators
 * Provides common functionality for testing CRUD operations
 */
abstract class BaseCrudChecker : CrudChecker {
    
    /**
     * Execute a test and capture the result
     * 
     * @param operation The CRUD operation being tested
     * @param methodName The name of the method being tested
     * @param testBlock The test logic to execute
     * @return Test result
     */
    protected suspend fun executeTest(
        operation: CrudOperation,
        methodName: String,
        testBlock: suspend () -> Unit
    ): CrudTestResult {
        return withContext(Dispatchers.IO) {
            var success = false
            var errorMessage: String? = null
            var severity: IssueSeverity? = null
            var recommendation: String? = null
            
            val executionTime = measureTimeMillis {
                try {
                    testBlock()
                    success = true
                } catch (e: Exception) {
                    success = false
                    errorMessage = e.message ?: "Unknown error"
                    severity = IssueSeverity.CRITICAL
                    recommendation = generateRecommendation(operation, e)
                }
            }
            
            CrudTestResult(
                daoName = getDaoName(),
                operation = operation,
                methodName = methodName,
                success = success,
                errorMessage = errorMessage,
                executionTimeMs = executionTime,
                severity = severity,
                recommendation = recommendation
            )
        }
    }
    
    /**
     * Generate a recommendation based on the operation and error
     */
    private fun generateRecommendation(operation: CrudOperation, error: Exception): String {
        return when {
            error.message?.contains("constraint", ignoreCase = true) == true ->
                "Check database constraints and ensure proper foreign key relationships"
            error.message?.contains("null", ignoreCase = true) == true ->
                "Ensure all required fields are properly initialized and non-null"
            error.message?.contains("unique", ignoreCase = true) == true ->
                "Check for duplicate entries or unique constraint violations"
            operation == CrudOperation.CREATE ->
                "Verify that insert operation has proper conflict handling strategy"
            operation == CrudOperation.UPDATE ->
                "Ensure entity exists before update and all fields are valid"
            operation == CrudOperation.DELETE ->
                "Verify entity exists before deletion and check cascade rules"
            operation == CrudOperation.READ ->
                "Check query syntax and ensure table/column names are correct"
            else ->
                "Review the method implementation and error logs for more details"
        }
    }
    
    override fun generateHealthSummary(results: List<CrudTestResult>): DaoHealthSummary {
        val totalOperations = results.size
        val successfulOperations = results.count { it.success }
        val failedOperations = results.count { !it.success }
        val avgExecutionTime = if (results.isNotEmpty()) {
            results.map { it.executionTimeMs }.average()
        } else {
            0.0
        }
        
        val criticalIssues = results.count { it.severity == IssueSeverity.CRITICAL }
        val warnings = results.count { it.severity == IssueSeverity.WARNING }
        
        val recommendations = results
            .mapNotNull { it.recommendation }
            .distinct()
            .toMutableList()
        
        // Add performance recommendations
        if (avgExecutionTime > 100) {
            recommendations.add("Consider optimizing queries - average execution time is ${avgExecutionTime.toInt()}ms")
        }
        
        // Add general recommendations based on failures
        if (failedOperations > 0) {
            recommendations.add("$failedOperations operation(s) failed - review error messages and fix issues")
        }
        
        return DaoHealthSummary(
            daoName = getDaoName(),
            totalOperations = totalOperations,
            successfulOperations = successfulOperations,
            failedOperations = failedOperations,
            averageExecutionTimeMs = avgExecutionTime,
            criticalIssues = criticalIssues,
            warnings = warnings,
            recommendations = recommendations
        )
    }
}
