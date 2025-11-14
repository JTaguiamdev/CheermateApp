package com.example.cheermateapp.data.validation

import com.example.cheermateapp.data.model.*
import kotlin.reflect.KClass

/**
 * Helper object to validate the entire CheermateApp database schema
 */
object DatabaseSchemaValidationHelper {
    
    /**
     * Get all entity classes from the database
     */
    fun getAllEntities(): List<KClass<*>> {
        return listOf(
            User::class,
            Task::class,
            SubTask::class,
            TaskReminder::class,
            TaskDependency::class,
            TaskTemplate::class,
            RecurringTask::class,
            Settings::class,
            Personality::class,
            SecurityQuestion::class,
            UserSecurityAnswer::class,
            MessageTemplate::class
        )
    }
    
    /**
     * Validate the entire database schema with default configuration
     */
    fun validateSchema(): ValidationResult {
        val validator = RoomSchemaValidator()
        return validator.validate(getAllEntities())
    }
    
    /**
     * Validate the database schema with custom configuration
     */
    fun validateSchema(config: ValidationConfig): ValidationResult {
        val validator = RoomSchemaValidator()
        return validator.validate(getAllEntities(), config)
    }
    
    /**
     * Validate and print a formatted report to console
     */
    fun validateAndPrintReport(): ValidationResult {
        val result = validateSchema()
        println(result.toReport())
        return result
    }
    
    /**
     * Validate specific entities
     */
    fun validateEntities(vararg entities: KClass<*>): ValidationResult {
        val validator = RoomSchemaValidator()
        return validator.validate(entities.toList())
    }
    
    /**
     * Quick validation that only checks for critical issues
     */
    fun quickValidate(): ValidationResult {
        val config = ValidationConfig(
            validatePrimaryKeys = true,
            validateForeignKeys = true,
            validateRelations = true,
            checkMissingIndexes = false,
            checkDuplicateIndexes = false,
            checkCascadeOperations = false,
            checkCircularDependencies = false,
            checkTypeCompatibility = true,
            minSeverity = ValidationSeverity.CRITICAL
        )
        return validateSchema(config)
    }
    
    /**
     * Comprehensive validation with all checks enabled
     */
    fun comprehensiveValidate(): ValidationResult {
        val config = ValidationConfig(
            validatePrimaryKeys = true,
            validateForeignKeys = true,
            validateRelations = true,
            checkMissingIndexes = true,
            checkDuplicateIndexes = true,
            checkCascadeOperations = true,
            checkCircularDependencies = true,
            checkTypeCompatibility = true,
            minSeverity = ValidationSeverity.INFO
        )
        return validateSchema(config)
    }
    
    /**
     * Get validation statistics
     */
    fun getValidationStats(): String {
        val result = validateSchema()
        return """
            |Database Schema Validation Stats:
            |--------------------------------
            |Total Entities: ${result.summary.totalEntities}
            |Total Relationships: ${result.summary.totalRelationships}
            |Critical Issues: ${result.summary.criticalIssues}
            |Errors: ${result.summary.errors}
            |Warnings: ${result.summary.warnings}
            |Info Messages: ${result.summary.infoMessages}
            |Overall Status: ${if (result.isValid) "✓ VALID" else "✗ INVALID"}
        """.trimMargin()
    }
}
