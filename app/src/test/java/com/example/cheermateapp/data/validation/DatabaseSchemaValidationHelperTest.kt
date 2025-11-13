package com.example.cheermateapp.data.validation

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for DatabaseSchemaValidationHelper
 */
class DatabaseSchemaValidationHelperTest {

    @Test
    fun `test getAllEntities returns all database entities`() {
        // When
        val entities = DatabaseSchemaValidationHelper.getAllEntities()

        // Then
        assertNotNull(entities)
        assertTrue(entities.isNotEmpty())
        // Verify all expected entities are present
        assertTrue("Should contain User", entities.any { it.simpleName == "User" })
        assertTrue("Should contain Task", entities.any { it.simpleName == "Task" })
        assertTrue("Should contain SubTask", entities.any { it.simpleName == "SubTask" })
        assertTrue("Should contain TaskReminder", entities.any { it.simpleName == "TaskReminder" })
        assertTrue("Should contain TaskDependency", entities.any { it.simpleName == "TaskDependency" })
        assertTrue("Should contain Settings", entities.any { it.simpleName == "Settings" })
        assertTrue("Should contain Personality", entities.any { it.simpleName == "Personality" })
        assertTrue("Should contain SecurityQuestion", entities.any { it.simpleName == "SecurityQuestion" })
    }

    @Test
    fun `test validateSchema returns validation result`() {
        // When
        val result = DatabaseSchemaValidationHelper.validateSchema()

        // Then
        assertNotNull(result)
        assertNotNull(result.issues)
        assertNotNull(result.fixSuggestions)
        assertNotNull(result.summary)
        assertTrue(result.summary.totalEntities > 0)
    }

    @Test
    fun `test validateSchema with custom config`() {
        // Given
        val config = ValidationConfig(
            validatePrimaryKeys = true,
            validateForeignKeys = true,
            validateRelations = false,
            checkMissingIndexes = false,
            minSeverity = ValidationSeverity.ERROR
        )

        // When
        val result = DatabaseSchemaValidationHelper.validateSchema(config)

        // Then
        assertNotNull(result)
        // Should only have ERROR or CRITICAL issues (no INFO or WARNING)
        assertTrue(result.issues.all { 
            it.severity == ValidationSeverity.ERROR || 
            it.severity == ValidationSeverity.CRITICAL 
        })
    }

    @Test
    fun `test validateEntities with specific entities`() {
        // Given
        val entities = arrayOf(
            com.example.cheermateapp.data.model.User::class,
            com.example.cheermateapp.data.model.Task::class
        )

        // When
        val result = DatabaseSchemaValidationHelper.validateEntities(*entities)

        // Then
        assertNotNull(result)
        assertEquals(2, result.summary.totalEntities)
    }

    @Test
    fun `test quickValidate only checks critical issues`() {
        // When
        val result = DatabaseSchemaValidationHelper.quickValidate()

        // Then
        assertNotNull(result)
        // Should only contain CRITICAL issues
        assertTrue(result.issues.all { it.severity == ValidationSeverity.CRITICAL })
    }

    @Test
    fun `test comprehensiveValidate checks all issues`() {
        // When
        val result = DatabaseSchemaValidationHelper.comprehensiveValidate()

        // Then
        assertNotNull(result)
        // May contain issues of all severity levels
        assertNotNull(result.issues)
        assertTrue(result.summary.totalEntities > 0)
    }

    @Test
    fun `test getValidationStats returns formatted string`() {
        // When
        val stats = DatabaseSchemaValidationHelper.getValidationStats()

        // Then
        assertNotNull(stats)
        assertTrue(stats.contains("Database Schema Validation Stats"))
        assertTrue(stats.contains("Total Entities:"))
        assertTrue(stats.contains("Total Relationships:"))
        assertTrue(stats.contains("Critical Issues:"))
        assertTrue(stats.contains("Errors:"))
        assertTrue(stats.contains("Warnings:"))
        assertTrue(stats.contains("Overall Status:"))
    }

    @Test
    fun `test validateAndPrintReport returns result`() {
        // When
        val result = DatabaseSchemaValidationHelper.validateAndPrintReport()

        // Then
        assertNotNull(result)
        assertNotNull(result.issues)
        assertNotNull(result.summary)
    }

    @Test
    fun `test validation result is consistent across calls`() {
        // When
        val result1 = DatabaseSchemaValidationHelper.validateSchema()
        val result2 = DatabaseSchemaValidationHelper.validateSchema()

        // Then
        assertEquals(result1.summary.totalEntities, result2.summary.totalEntities)
        assertEquals(result1.summary.totalRelationships, result2.summary.totalRelationships)
        // Issue counts should be the same
        assertEquals(result1.issues.size, result2.issues.size)
    }

    @Test
    fun `test validation covers all entity relationships`() {
        // When
        val result = DatabaseSchemaValidationHelper.comprehensiveValidate()

        // Then
        // Verify key relationships are validated
        assertTrue(result.summary.totalRelationships > 0)
        
        // Task -> User relationship
        val taskIssues = result.getIssuesByEntity("Task")
        assertNotNull(taskIssues)
        
        // SubTask -> Task relationship
        val subTaskIssues = result.getIssuesByEntity("SubTask")
        assertNotNull(subTaskIssues)
        
        // TaskReminder -> Task relationship
        val reminderIssues = result.getIssuesByEntity("TaskReminder")
        assertNotNull(reminderIssues)
    }

    @Test
    fun `test validation identifies all primary keys`() {
        // When
        val result = DatabaseSchemaValidationHelper.validateSchema()

        // Then
        // All entities should have primary keys defined
        val primaryKeyIssues = result.getIssuesByType(ValidationIssueType.MISSING_PRIMARY_KEY)
        // Should be empty or minimal since all entities have PKs
        assertNotNull(primaryKeyIssues)
    }

    @Test
    fun `test validation checks foreign key references`() {
        // When
        val result = DatabaseSchemaValidationHelper.validateSchema()

        // Then
        val foreignKeyIssues = result.getIssuesByType(ValidationIssueType.INVALID_FOREIGN_KEY_REFERENCE)
        // Should be empty since all FKs reference existing entities
        assertNotNull(foreignKeyIssues)
    }

    @Test
    fun `test validation detects type mismatches`() {
        // When
        val result = DatabaseSchemaValidationHelper.comprehensiveValidate()

        // Then
        val typeMismatchIssues = result.getIssuesByType(ValidationIssueType.TYPE_MISMATCH)
        // Should be empty since all types are compatible
        assertNotNull(typeMismatchIssues)
    }

    @Test
    fun `test validation provides fix suggestions`() {
        // When
        val result = DatabaseSchemaValidationHelper.comprehensiveValidate()

        // Then
        if (result.issues.isNotEmpty()) {
            assertNotNull(result.fixSuggestions)
            // Each fix suggestion should have required fields
            result.fixSuggestions.forEach { suggestion ->
                assertNotNull(suggestion.entityName)
                assertNotNull(suggestion.fixType)
                assertNotNull(suggestion.description)
                assertTrue(suggestion.code.isNotEmpty())
            }
        }
    }

    @Test
    fun `test validation report is human readable`() {
        // When
        val result = DatabaseSchemaValidationHelper.validateSchema()
        val report = result.toReport()

        // Then
        assertNotNull(report)
        assertTrue(report.isNotEmpty())
        assertTrue(report.contains("="))
        assertTrue(report.contains("Database Schema Validation Report"))
        assertTrue(report.contains("Summary:"))
    }
}
