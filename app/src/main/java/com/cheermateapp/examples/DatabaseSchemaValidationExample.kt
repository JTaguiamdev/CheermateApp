package com.cheermateapp.examples

import com.cheermateapp.data.validation.*
import com.cheermateapp.data.model.*

/**
 * Example demonstrating how to use the Database Schema Validation System
 * 
 * This example shows various ways to validate your Room database schema
 * and handle the results.
 */
object DatabaseSchemaValidationExample {

    /**
     * Example 1: Basic Schema Validation
     */
    fun basicValidation() {
        println("=== Basic Schema Validation ===\n")
        
        // Validate the entire database schema
        val result = DatabaseSchemaValidationHelper.validateSchema()
        
        // Check if schema is valid
        if (result.isValid) {
            println("✓ Database schema is valid!")
        } else {
            println("✗ Database schema has issues")
            println("Total issues found: ${result.issues.size}")
        }
        
        // Print summary
        println("\nSummary:")
        println("  Entities: ${result.summary.totalEntities}")
        println("  Relationships: ${result.summary.totalRelationships}")
        println("  Critical: ${result.summary.criticalIssues}")
        println("  Errors: ${result.summary.errors}")
        println("  Warnings: ${result.summary.warnings}")
    }

    /**
     * Example 2: Quick Validation (Critical Issues Only)
     */
    fun quickValidation() {
        println("\n=== Quick Validation (Critical Only) ===\n")
        
        // Only check for critical issues that will cause crashes
        val result = DatabaseSchemaValidationHelper.quickValidate()
        
        if (result.hasCriticalIssues()) {
            println("⚠️ CRITICAL ISSUES FOUND!")
            println("These will cause runtime errors:\n")
            
            result.getIssuesBySeverity(ValidationSeverity.CRITICAL).forEach { issue ->
                println("❌ ${issue.entityName}: ${issue.message}")
                issue.suggestedFix?.let { fix ->
                    println("   Fix: $fix")
                }
                println()
            }
        } else {
            println("✓ No critical issues found")
        }
    }

    /**
     * Example 3: Comprehensive Validation with Full Report
     */
    fun comprehensiveValidation() {
        println("\n=== Comprehensive Validation ===\n")
        
        // Run all validation checks
        val result = DatabaseSchemaValidationHelper.comprehensiveValidate()
        
        // Print the full formatted report
        println(result.toReport())
    }

    /**
     * Example 4: Custom Validation Configuration
     */
    fun customValidation() {
        println("\n=== Custom Validation Configuration ===\n")
        
        // Create custom configuration
        val config = ValidationConfig(
            validatePrimaryKeys = true,
            validateForeignKeys = true,
            validateRelations = true,
            checkMissingIndexes = true,
            checkDuplicateIndexes = false,  // Skip duplicate index checks
            checkCascadeOperations = true,
            checkCircularDependencies = true,
            checkTypeCompatibility = true,
            minSeverity = ValidationSeverity.WARNING  // Only show warnings and above
        )
        
        val result = DatabaseSchemaValidationHelper.validateSchema(config)
        
        println("Custom validation completed")
        println("Issues (WARNING and above): ${result.issues.size}")
    }

    /**
     * Example 5: Validate Specific Entities
     */
    fun validateSpecificEntities() {
        println("\n=== Validate Specific Entities ===\n")
        
        // Validate only User and Task entities
        val result = DatabaseSchemaValidationHelper.validateEntities(
            User::class,
            Task::class,
            SubTask::class
        )
        
        println("Validated ${result.summary.totalEntities} entities")
        println("Found ${result.issues.size} issues")
        
        // Show issues grouped by entity
        listOf("User", "Task", "SubTask").forEach { entityName ->
            val entityIssues = result.getIssuesByEntity(entityName)
            if (entityIssues.isNotEmpty()) {
                println("\n$entityName:")
                entityIssues.forEach { issue ->
                    println("  - [${issue.severity}] ${issue.message}")
                }
            }
        }
    }

    /**
     * Example 6: Filter Results by Severity
     */
    fun filterBySeverity() {
        println("\n=== Filter Results by Severity ===\n")
        
        val result = DatabaseSchemaValidationHelper.validateSchema()
        
        // Get critical issues
        val critical = result.getIssuesBySeverity(ValidationSeverity.CRITICAL)
        println("Critical Issues: ${critical.size}")
        critical.forEach { println("  - ${it.entityName}: ${it.message}") }
        
        // Get errors
        val errors = result.getIssuesBySeverity(ValidationSeverity.ERROR)
        println("\nErrors: ${errors.size}")
        errors.forEach { println("  - ${it.entityName}: ${it.message}") }
        
        // Get warnings
        val warnings = result.getIssuesBySeverity(ValidationSeverity.WARNING)
        println("\nWarnings: ${warnings.size}")
        warnings.take(5).forEach { println("  - ${it.entityName}: ${it.message}") }
        if (warnings.size > 5) println("  ... and ${warnings.size - 5} more")
    }

    /**
     * Example 7: Get Fix Suggestions
     */
    fun getFixSuggestions() {
        println("\n=== Fix Suggestions ===\n")
        
        val result = DatabaseSchemaValidationHelper.comprehensiveValidate()
        
        if (result.fixSuggestions.isEmpty()) {
            println("No fix suggestions available (schema is perfect!)")
            return
        }
        
        println("Found ${result.fixSuggestions.size} fix suggestions:\n")
        
        result.fixSuggestions.take(10).forEach { fix ->
            println("Entity: ${fix.entityName}")
            println("Type: ${fix.fixType}")
            println("Description: ${fix.description}")
            println("Can auto-apply: ${if (fix.canAutoApply) "Yes" else "No"}")
            println("\nSuggested code:")
            println(fix.code.lines().joinToString("\n") { "  $it" })
            println("\n" + "-".repeat(60) + "\n")
        }
    }

    /**
     * Example 8: Validate Foreign Keys
     */
    fun validateForeignKeys() {
        println("\n=== Foreign Key Validation ===\n")
        
        val result = DatabaseSchemaValidationHelper.validateSchema()
        
        // Get all foreign key related issues
        val fkIssues = listOf(
            ValidationIssueType.INVALID_FOREIGN_KEY_REFERENCE,
            ValidationIssueType.TYPE_MISMATCH,
            ValidationIssueType.MISSING_INDEX
        ).flatMap { result.getIssuesByType(it) }
        
        println("Foreign key related issues: ${fkIssues.size}\n")
        
        fkIssues.forEach { issue ->
            println("[${issue.type}] ${issue.entityName}")
            println("  ${issue.message}")
            issue.suggestedFix?.let { println("  → $it") }
            println()
        }
    }

    /**
     * Example 9: Check for Circular Dependencies
     */
    fun checkCircularDependencies() {
        println("\n=== Circular Dependency Check ===\n")
        
        val config = ValidationConfig(
            validatePrimaryKeys = false,
            validateForeignKeys = false,
            checkCircularDependencies = true,
            minSeverity = ValidationSeverity.INFO
        )
        
        val result = DatabaseSchemaValidationHelper.validateSchema(config)
        val circularIssues = result.getIssuesByType(ValidationIssueType.CIRCULAR_DEPENDENCY)
        
        if (circularIssues.isEmpty()) {
            println("✓ No circular dependencies found")
        } else {
            println("⚠️ Found ${circularIssues.size} circular dependencies:")
            circularIssues.forEach { issue ->
                println("  - ${issue.message}")
            }
        }
    }

    /**
     * Example 10: Integration with Tests
     */
    fun integrationWithTests() {
        println("\n=== Integration with Tests Example ===\n")
        
        println("Example test code:")
        println("""
            @Test
            fun testDatabaseSchemaIsValid() {
                val result = DatabaseSchemaValidationHelper.quickValidate()
                
                if (result.hasCriticalIssues()) {
                    val report = result.toReport()
                    fail("Database schema has critical issues:\n${"$"}report")
                }
                
                assertTrue("Schema should be valid", result.isValid)
            }
        """.trimIndent())
    }

    /**
     * Example 11: Validation Stats
     */
    fun validationStats() {
        println("\n=== Validation Statistics ===\n")
        
        val stats = DatabaseSchemaValidationHelper.getValidationStats()
        println(stats)
    }

    /**
     * Example 12: Analyze Specific Issue Types
     */
    fun analyzeIssueTypes() {
        println("\n=== Analyze Issue Types ===\n")
        
        val result = DatabaseSchemaValidationHelper.comprehensiveValidate()
        
        // Count issues by type
        val issueTypeCounts = ValidationIssueType.values().associateWith { type ->
            result.getIssuesByType(type).size
        }.filter { it.value > 0 }
        
        println("Issues by type:")
        issueTypeCounts.forEach { (type, count) ->
            println("  ${type.name}: $count")
        }
    }

    /**
     * Example 13: Production Use - Background Validation
     */
    fun productionBackgroundValidation() {
        println("\n=== Production Background Validation ===\n")
        
        println("Example code for background validation:")
        println("""
            // In your Application class
            class CheermateApp : Application() {
                override fun onCreate() {
                    super.onCreate()
                    
                    if (BuildConfig.DEBUG) {
                        // Run validation in background thread
                        Thread {
                            val result = DatabaseSchemaValidationHelper.quickValidate()
                            
                            if (result.hasCriticalIssues()) {
                                Log.e("SchemaValidation", result.toReport())
                                
                                // Optionally show notification in debug builds
                                showDebugNotification(
                                    "Database Schema Issues",
                                    "${"$"}{result.summary.criticalIssues} critical issues found"
                                )
                            }
                        }.start()
                    }
                }
            }
        """.trimIndent())
    }

    /**
     * Example 14: Export Validation Report
     */
    fun exportValidationReport() {
        println("\n=== Export Validation Report ===\n")
        
        val result = DatabaseSchemaValidationHelper.comprehensiveValidate()
        val report = result.toReport()
        
        println("Example code to save report:")
        println("""
            val file = File(context.getExternalFilesDir(null), "schema_validation_report.txt")
            file.writeText(report)
            Log.i("SchemaValidation", "Report saved to: ${"$"}{file.absolutePath}")
        """.trimIndent())
        
        println("\nReport preview (first 20 lines):")
        report.lines().take(20).forEach { println(it) }
    }

    /**
     * Run all examples
     */
    fun runAllExamples() {
        basicValidation()
        quickValidation()
        customValidation()
        validateSpecificEntities()
        filterBySeverity()
        getFixSuggestions()
        validateForeignKeys()
        checkCircularDependencies()
        integrationWithTests()
        validationStats()
        analyzeIssueTypes()
        productionBackgroundValidation()
        exportValidationReport()
        
        println("\n" + "=".repeat(80))
        println("All examples completed!")
        println("=".repeat(80))
    }
}

/**
 * Usage in your code:
 * 
 * // Run a specific example
 * DatabaseSchemaValidationExample.basicValidation()
 * 
 * // Or run all examples
 * DatabaseSchemaValidationExample.runAllExamples()
 */
