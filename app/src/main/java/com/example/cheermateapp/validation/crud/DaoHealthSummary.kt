package com.example.cheermateapp.validation.crud

/**
 * Summary statistics for DAO CRUD operations
 * 
 * @property daoName Name of the DAO
 * @property totalOperations Total number of operations tested
 * @property successfulOperations Number of successful operations
 * @property failedOperations Number of failed operations
 * @property averageExecutionTimeMs Average execution time across all operations
 * @property criticalIssues Number of critical issues found
 * @property warnings Number of warnings found
 * @property recommendations List of recommendations for improvement
 */
data class DaoHealthSummary(
    val daoName: String,
    val totalOperations: Int,
    val successfulOperations: Int,
    val failedOperations: Int,
    val averageExecutionTimeMs: Double,
    val criticalIssues: Int,
    val warnings: Int,
    val recommendations: List<String>
) {
    /**
     * Health score as a percentage (0-100)
     */
    val healthScore: Double
        get() = if (totalOperations > 0) {
            (successfulOperations.toDouble() / totalOperations) * 100
        } else {
            0.0
        }
    
    /**
     * Whether this DAO is considered healthy (>= 95% success rate)
     */
    val isHealthy: Boolean
        get() = healthScore >= 95.0 && criticalIssues == 0
}
