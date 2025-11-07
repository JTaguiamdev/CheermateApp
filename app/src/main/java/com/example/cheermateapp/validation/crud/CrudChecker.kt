package com.example.cheermateapp.validation.crud

/**
 * Interface for CRUD validation operations
 * Implementations should test all CRUD operations for a given DAO
 */
interface CrudChecker {
    /**
     * Validate all CRUD operations for a specific DAO
     * 
     * @return List of test results for all operations
     */
    suspend fun validateDao(): List<CrudTestResult>
    
    /**
     * Get the name of the DAO being validated
     */
    fun getDaoName(): String
    
    /**
     * Generate a health summary for the DAO
     * 
     * @param results Test results to analyze
     * @return Health summary for the DAO
     */
    fun generateHealthSummary(results: List<CrudTestResult>): DaoHealthSummary
}
