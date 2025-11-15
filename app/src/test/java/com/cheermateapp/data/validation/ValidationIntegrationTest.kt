package com.cheermateapp.data.validation

import androidx.room.*
import kotlin.reflect.KClass
import org.junit.Test
import org.junit.Assert.*

/**
 * Integration test demonstrating real-world validation scenarios
 */
class ValidationIntegrationTest {

    /**
     * Test entity with valid schema
     */
    @Entity(
        tableName = "ValidEntity",
        indices = [Index("parentId")]
    )
    data class ValidEntity(
        @PrimaryKey(autoGenerate = true)
        val id: Int = 0,
        val name: String,
        val parentId: Int
    )

    /**
     * Test parent entity
     */
    @Entity(tableName = "ParentEntity")
    data class ParentEntity(
        @PrimaryKey(autoGenerate = true)
        val id: Int = 0,
        val name: String
    )

    /**
     * Test entity with missing primary key
     */
    @Entity(tableName = "NoPrimaryKey")
    data class NoPrimaryKeyEntity(
        val name: String,
        val value: Int
    )

    /**
     * Test entity with composite primary key
     */
    @Entity(
        tableName = "CompositeKeyEntity",
        primaryKeys = ["key1", "key2"]
    )
    data class CompositeKeyEntity(
        val key1: Int,
        val key2: Int,
        val data: String
    )

    /**
     * Test entity with valid foreign key
     */
    @Entity(
        tableName = "ChildEntity",
        foreignKeys = [
            ForeignKey(
                entity = ParentEntity::class,
                parentColumns = ["id"],
                childColumns = ["parentId"],
                onDelete = ForeignKey.CASCADE
            )
        ],
        indices = [Index("parentId")]
    )
    data class ChildEntity(
        @PrimaryKey(autoGenerate = true)
        val id: Int = 0,
        val parentId: Int,
        val name: String
    )

    /**
     * Test entity with type mismatch in foreign key
     */
    @Entity(
        tableName = "BadTypeEntity",
        foreignKeys = [
            ForeignKey(
                entity = ParentEntity::class,
                parentColumns = ["id"],
                childColumns = ["parentId"],
                onDelete = ForeignKey.CASCADE
            )
        ],
        indices = [Index("parentId")]
    )
    data class BadTypeEntity(
        @PrimaryKey(autoGenerate = true)
        val id: Int = 0,
        val parentId: Long,  // Type mismatch - parent is Int
        val name: String
    )

    /**
     * Test entity missing index on foreign key
     */
    @Entity(
        tableName = "NoIndexEntity",
        foreignKeys = [
            ForeignKey(
                entity = ParentEntity::class,
                parentColumns = ["id"],
                childColumns = ["parentId"],
                onDelete = ForeignKey.CASCADE
            )
        ]
        // Missing: indices = [Index("parentId")]
    )
    data class NoIndexEntity(
        @PrimaryKey(autoGenerate = true)
        val id: Int = 0,
        val parentId: Int,
        val name: String
    )

    @Test
    fun `test validate entity with no primary key`() {
        // Given
        val validator = RoomSchemaValidator()
        val entities = listOf(NoPrimaryKeyEntity::class)

        // When
        val result = validator.validate(entities)

        // Then
        assertTrue("Should find missing primary key issue", 
            result.issues.any { 
                it.type == ValidationIssueType.MISSING_PRIMARY_KEY &&
                it.entityName == "NoPrimaryKeyEntity"
            }
        )
        assertFalse("Schema should be invalid", result.isValid)
    }

    @Test
    fun `test validate entity with composite primary key`() {
        // Given
        val validator = RoomSchemaValidator()
        val entities = listOf(CompositeKeyEntity::class)

        // When
        val result = validator.validate(entities)

        // Then
        // Should not have primary key issues
        val pkIssues = result.issues.filter { 
            it.type == ValidationIssueType.MISSING_PRIMARY_KEY ||
            it.type == ValidationIssueType.COMPOSITE_KEY_ISSUE
        }
        assertTrue("Composite key should be valid", pkIssues.isEmpty())
    }

    @Test
    fun `test validate valid foreign key relationship`() {
        // Given
        val validator = RoomSchemaValidator()
        val entities = listOf(ParentEntity::class, ChildEntity::class)

        // When
        val result = validator.validate(entities)

        // Then
        val fkIssues = result.issues.filter { 
            it.entityName == "ChildEntity" &&
            it.severity == ValidationSeverity.CRITICAL &&
            it.type == ValidationIssueType.INVALID_FOREIGN_KEY_REFERENCE
        }
        assertTrue("Foreign key should be valid", fkIssues.isEmpty())
    }

    @Test
    fun `test detect type mismatch in foreign key`() {
        // Given
        val validator = RoomSchemaValidator()
        val entities = listOf(ParentEntity::class, BadTypeEntity::class)

        // When
        val result = validator.validate(entities)

        // Then
        // Should detect type mismatch between Int and Long
        val typeMismatch = result.issues.any { 
            it.type == ValidationIssueType.TYPE_MISMATCH &&
            it.entityName == "BadTypeEntity" &&
            it.fieldName == "parentId"
        }
        assertTrue("Should detect type mismatch", typeMismatch)
    }

    @Test
    fun `test detect missing index on foreign key`() {
        // Given
        val validator = RoomSchemaValidator()
        val entities = listOf(ParentEntity::class, NoIndexEntity::class)
        val config = ValidationConfig(checkMissingIndexes = true)

        // When
        val result = validator.validate(entities, config)

        // Then
        val missingIndex = result.issues.any { 
            it.type == ValidationIssueType.MISSING_INDEX &&
            it.entityName == "NoIndexEntity" &&
            it.fieldName == "parentId"
        }
        assertTrue("Should detect missing index", missingIndex)
    }

    @Test
    fun `test fix suggestions are generated`() {
        // Given
        val validator = RoomSchemaValidator()
        val entities = listOf(NoPrimaryKeyEntity::class)

        // When
        val result = validator.validate(entities)

        // Then
        assertTrue("Should have fix suggestions", result.fixSuggestions.isNotEmpty())
        val pkFixSuggestion = result.fixSuggestions.any { 
            it.entityName == "NoPrimaryKeyEntity" &&
            it.fixType == FixType.ADD_PRIMARY_KEY
        }
        assertTrue("Should suggest adding primary key", pkFixSuggestion)
    }

    @Test
    fun `test validation summary is accurate`() {
        // Given
        val validator = RoomSchemaValidator()
        val entities = listOf(
            ParentEntity::class,
            ChildEntity::class,
            NoPrimaryKeyEntity::class
        )

        // When
        val result = validator.validate(entities)

        // Then
        assertEquals("Should validate 3 entities", 3, result.summary.totalEntities)
        assertTrue("Should have at least one relationship", result.summary.totalRelationships > 0)
        assertTrue("Should have at least one critical issue", result.summary.criticalIssues > 0)
    }

    @Test
    fun `test filtering issues by severity works`() {
        // Given
        val validator = RoomSchemaValidator()
        val entities = listOf(
            ParentEntity::class,
            NoIndexEntity::class,
            NoPrimaryKeyEntity::class
        )

        // When
        val result = validator.validate(entities)

        // Then
        val critical = result.getIssuesBySeverity(ValidationSeverity.CRITICAL)
        val warnings = result.getIssuesBySeverity(ValidationSeverity.WARNING)
        
        assertTrue("Should have critical issues", critical.isNotEmpty())
        assertTrue("Critical issues should only be CRITICAL severity", 
            critical.all { it.severity == ValidationSeverity.CRITICAL })
        assertTrue("Warning issues should only be WARNING severity",
            warnings.all { it.severity == ValidationSeverity.WARNING })
    }

    @Test
    fun `test validation report generation`() {
        // Given
        val validator = RoomSchemaValidator()
        val entities = listOf(ParentEntity::class, ChildEntity::class)

        // When
        val result = validator.validate(entities)
        val report = result.toReport()

        // Then
        assertNotNull("Report should not be null", report)
        assertTrue("Report should contain header", 
            report.contains("Database Schema Validation Report"))
        assertTrue("Report should contain summary", 
            report.contains("Summary:"))
        assertTrue("Report should contain status", 
            report.contains("Overall Status:"))
    }

    @Test
    fun `test cascade operation validation`() {
        // Given
        val validator = RoomSchemaValidator()
        val entities = listOf(ParentEntity::class, ChildEntity::class)
        val config = ValidationConfig(checkCascadeOperations = true)

        // When
        val result = validator.validate(entities, config)

        // Then
        // ChildEntity uses CASCADE, should not have issues
        val cascadeIssues = result.issues.filter { 
            it.entityName == "ChildEntity" &&
            it.type == ValidationIssueType.INVALID_CASCADE_OPERATION
        }
        assertTrue("CASCADE operation should be valid", cascadeIssues.isEmpty())
    }

    @Test
    fun `test real schema validation`() {
        // Given
        val entities = DatabaseSchemaValidationHelper.getAllEntities()
        val validator = RoomSchemaValidator()

        // When
        val result = validator.validate(entities)

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("Should validate all entities", 
            entities.size, result.summary.totalEntities)
        assertTrue("Should have some relationships",
            result.summary.totalRelationships > 0)
        
        // Print summary for manual inspection
        println("\n=== Real Schema Validation Summary ===")
        println(result.summary)
        println("\nTotal Issues: ${result.issues.size}")
        println("Critical: ${result.summary.criticalIssues}")
        println("Errors: ${result.summary.errors}")
        println("Warnings: ${result.summary.warnings}")
        println("Info: ${result.summary.infoMessages}")
    }
}
