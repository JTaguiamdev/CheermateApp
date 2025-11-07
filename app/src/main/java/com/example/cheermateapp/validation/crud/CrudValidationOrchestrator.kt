package com.example.cheermateapp.validation.crud

import com.example.cheermateapp.data.db.AppDb
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Main orchestrator for CRUD validation across all DAOs
 * Coordinates validation of all DAO CRUD operations and generates comprehensive health report
 * 
 * @property database The Room database instance
 */
class CrudValidationOrchestrator(private val database: AppDb) {
    
    /**
     * Run comprehensive CRUD validation for all DAOs
     * 
     * @return Complete health report for all DAOs
     */
    suspend fun runFullValidation(): CrudHealthReport = coroutineScope {
        val checkers = createCheckers()
        
        // Run all validations in parallel for efficiency
        val daoResults = checkers.map { checker ->
            async {
                val results = checker.validateDao()
                val summary = checker.generateHealthSummary(results)
                Pair(summary, results)
            }
        }.awaitAll()
        
        // Aggregate results
        val allSummaries = daoResults.map { it.first }
        val allResults = daoResults.flatMap { it.second }
        
        generateHealthReport(allSummaries, allResults)
    }
    
    /**
     * Validate a specific DAO by name
     * 
     * @param daoName Name of the DAO to validate
     * @return Health summary for the specific DAO, or null if not found
     */
    suspend fun validateSpecificDao(daoName: String): DaoHealthSummary? {
        val checker = createCheckers().find { it.getDaoName() == daoName } ?: return null
        val results = checker.validateDao()
        return checker.generateHealthSummary(results)
    }
    
    /**
     * Create all DAO checkers
     */
    private fun createCheckers(): List<CrudChecker> {
        return listOf(
            TaskDaoCrudChecker(database.taskDao()),
            UserDaoCrudChecker(database.userDao()),
            SubTaskDaoCrudChecker(database.subTaskDao()),
            SettingsDaoCrudChecker(database.settingsDao()),
            PersonalityDaoCrudChecker(database.personalityDao()),
            RecurringTaskDaoCrudChecker(database.recurringTaskDao()),
            TaskTemplateDaoCrudChecker(database.taskTemplateDao()),
            TaskReminderDaoCrudChecker(database.taskReminderDao()),
            TaskDependencyDaoCrudChecker(database.taskDependencyDao()),
            SecurityDaoCrudChecker(database.securityDao()),
            PersonalityTypeDaoCrudChecker(database.personalityTypeDao())
        )
    }
    
    /**
     * Generate comprehensive health report from summaries and results
     */
    private fun generateHealthReport(
        summaries: List<DaoHealthSummary>,
        results: List<CrudTestResult>
    ): CrudHealthReport {
        val totalTests = results.size
        val passedTests = results.count { it.success }
        val failedTests = results.count { !it.success }
        val totalExecutionTime = results.sumOf { it.executionTimeMs }
        val criticalIssues = results.count { it.severity == IssueSeverity.CRITICAL }
        val warnings = results.count { it.severity == IssueSeverity.WARNING }
        
        // Calculate overall health score (weighted by DAO importance if needed)
        val overallHealthScore = if (summaries.isNotEmpty()) {
            summaries.map { it.healthScore }.average()
        } else {
            0.0
        }
        
        return CrudHealthReport(
            daoSummaries = summaries,
            testResults = results,
            overallHealthScore = overallHealthScore,
            totalTests = totalTests,
            passedTests = passedTests,
            failedTests = failedTests,
            totalExecutionTimeMs = totalExecutionTime,
            criticalIssuesCount = criticalIssues,
            warningsCount = warnings
        )
    }
}
