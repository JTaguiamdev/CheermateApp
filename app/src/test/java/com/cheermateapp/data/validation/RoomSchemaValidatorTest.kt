package com.cheermateapp.data.validation

import com.cheermateapp.data.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for RoomSchemaValidator
 */
class RoomSchemaValidatorTest {

    private lateinit var validator: RoomSchemaValidator

    @Before
    fun setup() {
        validator = RoomSchemaValidator()
    }

    @Test
    fun `test validate all entities returns result`() {
        // Given
        val entities = listOf(
            User::class,
            Task::class,
            SubTask::class
        )

        // When
        val result = validator.validate(entities)

        // Then
        assertNotNull(result)
        assertNotNull(result.issues)
        assertNotNull(result.fixSuggestions)
        assertNotNull(result.summary)
        assertTrue(result.summary.totalEntities > 0)
    }

    @Test
    fun `test validate Task entity has proper foreign keys`() {
        // Given
        val entities = listOf(User::class, Task::class)

        // When
        val issues = validator.validateEntity(Task::class, entities)

        // Then
        // Task should reference User correctly
        val foreignKeyIssues = issues.filter { 
            it.type == ValidationIssueType.INVALID_FOREIGN_KEY_REFERENCE 
        }
        assertTrue("Should not have invalid foreign key references", foreignKeyIssues.isEmpty())
    }

    @Test
    fun `test validate SubTask entity has proper composite foreign key`() {
        // Given
        val entities = listOf(User::class, Task::class, SubTask::class)

        // When
        val issues = validator.validateEntity(SubTask::class, entities)

        // Then
        // SubTask should correctly reference Task's composite key
        val criticalIssues = issues.filter { 
            it.severity == ValidationSeverity.CRITICAL 
        }
        assertTrue("SubTask should not have critical foreign key issues", criticalIssues.isEmpty())
    }

    @Test
    fun `test validate TaskDependency has correct multi-reference foreign keys`() {
        // Given
        val entities = listOf(User::class, Task::class, TaskDependency::class)

        // When
        val issues = validator.validateEntity(TaskDependency::class, entities)

        // Then
        // TaskDependency references Task twice, both should be valid
        val foreignKeyIssues = issues.filter {
            it.type == ValidationIssueType.INVALID_FOREIGN_KEY_REFERENCE &&
            it.severity == ValidationSeverity.CRITICAL
        }
        assertTrue("TaskDependency should have valid foreign keys", foreignKeyIssues.isEmpty())
    }

    @Test
    fun `test validate User entity has primary key`() {
        // Given
        val entities = listOf(User::class)

        // When
        val issues = validator.validateEntity(User::class, entities)

        // Then
        val primaryKeyIssues = issues.filter { 
            it.type == ValidationIssueType.MISSING_PRIMARY_KEY 
        }
        assertTrue("User should have a primary key defined", primaryKeyIssues.isEmpty())
    }

    @Test
    fun `test validate Task entity has composite primary key`() {
        // Given
        val entities = listOf(User::class, Task::class)

        // When
        val issues = validator.validateEntity(Task::class, entities)

        // Then
        val compositeKeyIssues = issues.filter { 
            it.type == ValidationIssueType.COMPOSITE_KEY_ISSUE 
        }
        assertTrue("Task composite primary key should be valid", compositeKeyIssues.isEmpty())
    }

    @Test
    fun `test validate foreign key type compatibility`() {
        // Given
        val entities = listOf(User::class, Task::class)

        // When
        val issues = validator.validateEntity(Task::class, entities)

        // Then
        // Task.User_ID should match User.User_ID type (both Int)
        val typeMismatchIssues = issues.filter { 
            it.type == ValidationIssueType.TYPE_MISMATCH &&
            it.fieldName == "User_ID"
        }
        assertTrue("Foreign key types should be compatible", typeMismatchIssues.isEmpty())
    }

    @Test
    fun `test validate indexes on foreign key columns`() {
        // Given
        val entities = listOf(User::class, Task::class)
        val config = ValidationConfig(checkMissingIndexes = true)

        // When
        val issues = validator.validateEntity(Task::class, entities, config)

        // Then
        // Task should have index on User_ID since it's a foreign key
        val missingIndexIssues = issues.filter {
            it.type == ValidationIssueType.MISSING_INDEX &&
            it.fieldName == "User_ID"
        }
        // Task already has Index("User_ID"), so should be empty
        assertTrue("Task should have index on foreign key", missingIndexIssues.isEmpty())
    }

    @Test
    fun `test validate cascade delete operations`() {
        // Given
        val entities = listOf(User::class, Task::class)
        val config = ValidationConfig(checkCascadeOperations = true)

        // When
        val issues = validator.validateEntity(Task::class, entities, config)

        // Then
        // Verify cascade operations are properly configured
        assertNotNull(issues)
        // Task uses CASCADE, so should not have issues
        val cascadeIssues = issues.filter { 
            it.type == ValidationIssueType.INVALID_CASCADE_OPERATION 
        }
        assertTrue("Cascade operations should be valid", cascadeIssues.isEmpty())
    }

    @Test
    fun `test validate circular dependencies detection`() {
        // Given
        val entities = listOf(User::class, Task::class, TaskDependency::class)
        val config = ValidationConfig(checkCircularDependencies = true)

        // When
        val result = validator.validate(entities, config)

        // Then
        // TaskDependency can create circular dependencies
        assertNotNull(result)
        // May or may not have circular dependencies depending on data
    }

    @Test
    fun `test validation result summary`() {
        // Given
        val entities = DatabaseSchemaValidationHelper.getAllEntities()

        // When
        val result = validator.validate(entities)

        // Then
        assertEquals(entities.size, result.summary.totalEntities)
        assertTrue(result.summary.totalRelationships > 0)
        
        // Verify counts
        val totalIssues = result.summary.criticalIssues + 
                         result.summary.errors + 
                         result.summary.warnings + 
                         result.summary.infoMessages
        assertEquals(totalIssues, result.issues.size)
    }

    @Test
    fun `test validation result filtering by severity`() {
        // Given
        val entities = DatabaseSchemaValidationHelper.getAllEntities()

        // When
        val result = validator.validate(entities)

        // Then
        val criticalIssues = result.getIssuesBySeverity(ValidationSeverity.CRITICAL)
        val errorIssues = result.getIssuesBySeverity(ValidationSeverity.ERROR)
        val warningIssues = result.getIssuesBySeverity(ValidationSeverity.WARNING)
        
        assertTrue(criticalIssues.all { it.severity == ValidationSeverity.CRITICAL })
        assertTrue(errorIssues.all { it.severity == ValidationSeverity.ERROR })
        assertTrue(warningIssues.all { it.severity == ValidationSeverity.WARNING })
    }

    @Test
    fun `test validation result filtering by entity`() {
        // Given
        val entities = listOf(User::class, Task::class, SubTask::class)

        // When
        val result = validator.validate(entities)

        // Then
        val taskIssues = result.getIssuesByEntity("Task")
        assertTrue(taskIssues.all { it.entityName == "Task" })
    }

    @Test
    fun `test validation result has critical issues check`() {
        // Given
        val entities = DatabaseSchemaValidationHelper.getAllEntities()

        // When
        val result = validator.validate(entities)

        // Then
        val hasCritical = result.hasCriticalIssues()
        assertEquals(result.summary.criticalIssues > 0, hasCritical)
    }

    @Test
    fun `test generate fix suggestions for missing indexes`() {
        // Given
        val entities = listOf(User::class, Task::class)
        val config = ValidationConfig(checkMissingIndexes = true)
        val result = validator.validate(entities, config)

        // When
        val suggestions = result.fixSuggestions

        // Then
        assertNotNull(suggestions)
        // Verify suggestions have proper structure
        suggestions.forEach { suggestion ->
            assertNotNull(suggestion.entityName)
            assertNotNull(suggestion.fixType)
            assertNotNull(suggestion.description)
            assertNotNull(suggestion.code)
        }
    }

    @Test
    fun `test validation report generation`() {
        // Given
        val entities = listOf(User::class, Task::class)

        // When
        val result = validator.validate(entities)
        val report = result.toReport()

        // Then
        assertNotNull(report)
        assertTrue(report.contains("Database Schema Validation Report"))
        assertTrue(report.contains("Overall Status:"))
        assertTrue(report.contains("Summary:"))
    }

    @Test
    fun `test UserSecurityAnswer entity validation`() {
        // Given - UserSecurityAnswer doesn't have foreign key constraints but should
        val entities = listOf(User::class, SecurityQuestion::class, UserSecurityAnswer::class)

        // When
        val issues = validator.validateEntity(UserSecurityAnswer::class, entities)

        // Then
        // UserSecurityAnswer references User_ID and Question_ID but has no FK constraints
        assertNotNull(issues)
        // This is informational - the entity works but lacks FK constraints
    }

    @Test
    fun `test MessageTemplate foreign key with SET_NULL`() {
        // Given
        val entities = listOf(Personality::class, MessageTemplate::class)

        // When
        val issues = validator.validateEntity(MessageTemplate::class, entities)

        // Then
        // MessageTemplate uses SET_NULL on Personality_ID which is nullable
        val cascadeIssues = issues.filter { 
            it.type == ValidationIssueType.INVALID_CASCADE_OPERATION 
        }
        assertTrue("SET_NULL should work with nullable column", cascadeIssues.isEmpty())
    }

    @Test
    fun `test RecurringTask foreign key validation`() {
        // Given
        val entities = listOf(User::class, RecurringTask::class)

        // When
        val issues = validator.validateEntity(RecurringTask::class, entities)

        // Then
        val criticalIssues = issues.filter { 
            it.severity == ValidationSeverity.CRITICAL 
        }
        assertTrue("RecurringTask should have valid foreign key", criticalIssues.isEmpty())
    }

    @Test
    fun `test comprehensive validation with all checks`() {
        // Given
        val entities = DatabaseSchemaValidationHelper.getAllEntities()
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

        // When
        val result = validator.validate(entities, config)

        // Then
        assertNotNull(result)
        assertTrue(result.summary.totalEntities == entities.size)
        
        // Should complete without exceptions
        val report = result.toReport()
        assertTrue(report.isNotEmpty())
    }
}
