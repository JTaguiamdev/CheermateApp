package com.cheermateapp.data.validation

import kotlin.reflect.KClass

/**
 * Metadata about an entity in the database schema
 */
data class EntityMetadata(
    /** Entity class */
    val entityClass: KClass<*>,
    /** Entity name (table name) */
    val tableName: String,
    /** Primary key fields */
    val primaryKeys: List<FieldMetadata>,
    /** All fields in the entity */
    val fields: List<FieldMetadata>,
    /** Foreign key constraints */
    val foreignKeys: List<ForeignKeyMetadata>,
    /** Indexes defined on the entity */
    val indexes: List<IndexMetadata>,
    /** Relations to other entities */
    val relations: List<RelationMetadata>
)

/**
 * Metadata about a field in an entity
 */
data class FieldMetadata(
    /** Field name */
    val name: String,
    /** Column name in database */
    val columnName: String,
    /** Kotlin type */
    val type: KClass<*>,
    /** Whether field is nullable */
    val isNullable: Boolean,
    /** Whether field is a primary key */
    val isPrimaryKey: Boolean = false,
    /** Whether field is auto-generated */
    val isAutoGenerate: Boolean = false
)

/**
 * Metadata about a foreign key constraint
 */
data class ForeignKeyMetadata(
    /** Entity class that this foreign key references */
    val parentEntity: KClass<*>,
    /** Parent table columns */
    val parentColumns: List<String>,
    /** Child table columns */
    val childColumns: List<String>,
    /** On delete action */
    val onDelete: Int, // ForeignKey.CASCADE, SET_NULL, etc.
    /** On update action */
    val onUpdate: Int,
    /** Whether constraint is deferred */
    val deferred: Boolean = false
)

/**
 * Metadata about an index
 */
data class IndexMetadata(
    /** Index name (if specified) */
    val name: String?,
    /** Columns included in the index */
    val columns: List<String>,
    /** Whether index is unique */
    val unique: Boolean = false,
    /** Index orders (ASC or DESC) */
    val orders: List<String> = emptyList()
)

/**
 * Metadata about a relation between entities
 */
data class RelationMetadata(
    /** Parent entity class */
    val parentEntity: KClass<*>,
    /** Parent column */
    val parentColumn: String,
    /** Entity column that references parent */
    val entityColumn: String,
    /** Type of relation */
    val relationType: RelationType
)

/**
 * Type of relationship between entities
 */
enum class RelationType {
    ONE_TO_ONE,
    ONE_TO_MANY,
    MANY_TO_ONE,
    MANY_TO_MANY
}

/**
 * Configuration for schema validation
 */
data class ValidationConfig(
    /** Whether to validate primary keys */
    val validatePrimaryKeys: Boolean = true,
    /** Whether to validate foreign keys */
    val validateForeignKeys: Boolean = true,
    /** Whether to validate relations */
    val validateRelations: Boolean = true,
    /** Whether to check for missing indexes */
    val checkMissingIndexes: Boolean = true,
    /** Whether to check for duplicate indexes */
    val checkDuplicateIndexes: Boolean = true,
    /** Whether to check cascade operations */
    val checkCascadeOperations: Boolean = true,
    /** Whether to check for circular dependencies */
    val checkCircularDependencies: Boolean = true,
    /** Whether to check type compatibility */
    val checkTypeCompatibility: Boolean = true,
    /** Minimum severity level to include in results */
    val minSeverity: ValidationSeverity = ValidationSeverity.INFO
)
