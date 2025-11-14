package com.example.cheermateapp.data.validation

import androidx.room.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Implementation of SchemaValidator that validates Room database entities
 */
class RoomSchemaValidator : SchemaValidator {

    override fun validate(
        entities: List<KClass<*>>,
        config: ValidationConfig
    ): ValidationResult {
        val issues = mutableListOf<ValidationIssue>()
        
        // Extract entity metadata
        val entityMetadataMap = entities.associateWith { extractEntityMetadata(it) }
        
        // Validate each entity
        entities.forEach { entity ->
            issues.addAll(validateEntity(entity, entities, config))
        }
        
        // Check for circular dependencies
        if (config.checkCircularDependencies) {
            issues.addAll(checkCircularDependencies(entityMetadataMap))
        }
        
        // Check for orphaned entities (entities that should have relations but don't)
        issues.addAll(checkOrphanedEntities(entityMetadataMap))
        
        // Generate fix suggestions
        val fixSuggestions = generateFixSuggestions(issues, entities)
        
        // Calculate summary
        val summary = ValidationSummary(
            totalEntities = entities.size,
            totalRelationships = entityMetadataMap.values.sumOf { it.foreignKeys.size + it.relations.size },
            criticalIssues = issues.count { it.severity == ValidationSeverity.CRITICAL },
            errors = issues.count { it.severity == ValidationSeverity.ERROR },
            warnings = issues.count { it.severity == ValidationSeverity.WARNING },
            infoMessages = issues.count { it.severity == ValidationSeverity.INFO }
        )
        
        // Filter by minimum severity
        val filteredIssues = issues.filter { it.severity.ordinal <= config.minSeverity.ordinal }
        
        return ValidationResult(
            isValid = filteredIssues.none { it.severity == ValidationSeverity.CRITICAL },
            issues = filteredIssues,
            fixSuggestions = fixSuggestions,
            summary = summary
        )
    }

    override fun validateEntity(
        entity: KClass<*>,
        allEntities: List<KClass<*>>,
        config: ValidationConfig
    ): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        val entityAnnotation = entity.findAnnotation<Entity>()
        
        if (entityAnnotation == null) {
            issues.add(
                ValidationIssue(
                    type = ValidationIssueType.MISSING_PRIMARY_KEY,
                    severity = ValidationSeverity.CRITICAL,
                    entityName = entity.simpleName ?: "Unknown",
                    message = "Class is not annotated with @Entity",
                    suggestedFix = "Add @Entity annotation to the class"
                )
            )
            return issues
        }
        
        val entityName = entity.simpleName ?: "Unknown"
        
        // Validate primary keys
        if (config.validatePrimaryKeys) {
            issues.addAll(validatePrimaryKeys(entity, entityAnnotation))
        }
        
        // Validate foreign keys
        if (config.validateForeignKeys) {
            issues.addAll(validateForeignKeys(entity, allEntities))
        }
        
        // Validate relations
        if (config.validateRelations) {
            issues.addAll(validateRelations(entity, allEntities))
        }
        
        // Check indexes
        if (config.checkMissingIndexes || config.checkDuplicateIndexes) {
            issues.addAll(validateIndexes(entity, entityAnnotation, config))
        }
        
        // Check cascade operations
        if (config.checkCascadeOperations) {
            issues.addAll(validateCascadeOperations(entity, entityAnnotation))
        }
        
        return issues
    }

    override fun validateForeignKeys(
        entity: KClass<*>,
        allEntities: List<KClass<*>>
    ): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        val entityAnnotation = entity.findAnnotation<Entity>() ?: return issues
        val entityName = entity.simpleName ?: "Unknown"
        val foreignKeys = entityAnnotation.foreignKeys
        
        foreignKeys.forEach { foreignKey ->
            val parentEntity = foreignKey.entity
            val parentName = parentEntity.simpleName ?: "Unknown"
            
            // Check if parent entity exists in the database
            if (!allEntities.contains(parentEntity)) {
                issues.add(
                    ValidationIssue(
                        type = ValidationIssueType.INVALID_FOREIGN_KEY_REFERENCE,
                        severity = ValidationSeverity.CRITICAL,
                        entityName = entityName,
                        message = "Foreign key references non-existent entity: $parentName",
                        suggestedFix = "Ensure $parentName is included in the database entities list"
                    )
                )
                return@forEach
            }
            
            // Validate parent columns exist
            val parentMetadata = extractEntityMetadata(parentEntity)
            val parentPrimaryKeys = parentMetadata.primaryKeys.map { it.columnName }
            
            foreignKey.parentColumns.forEach { parentColumn ->
                if (!parentPrimaryKeys.contains(parentColumn)) {
                    issues.add(
                        ValidationIssue(
                            type = ValidationIssueType.INVALID_FOREIGN_KEY_REFERENCE,
                            severity = ValidationSeverity.CRITICAL,
                            entityName = entityName,
                            message = "Foreign key references non-existent or non-primary key column '$parentColumn' in $parentName",
                            suggestedFix = "Ensure '$parentColumn' is a primary key in $parentName",
                            fixCodeExample = """
                                @Entity
                                data class $parentName(
                                    @PrimaryKey val $parentColumn: Type
                                )
                            """.trimIndent()
                        )
                    )
                }
            }
            
            // Validate child columns exist
            val childMetadata = extractEntityMetadata(entity)
            val childFields = childMetadata.fields.map { it.columnName }
            
            foreignKey.childColumns.forEach { childColumn ->
                if (!childFields.contains(childColumn)) {
                    issues.add(
                        ValidationIssue(
                            type = ValidationIssueType.INVALID_FOREIGN_KEY_REFERENCE,
                            severity = ValidationSeverity.CRITICAL,
                            entityName = entityName,
                            fieldName = childColumn,
                            message = "Foreign key references non-existent column '$childColumn' in $entityName",
                            suggestedFix = "Add field '$childColumn' to $entityName or fix the foreign key definition"
                        )
                    )
                }
            }
            
            // Check type compatibility between parent and child columns
            if (foreignKey.parentColumns.size == foreignKey.childColumns.size) {
                foreignKey.parentColumns.zip(foreignKey.childColumns).forEach { (parentCol, childCol) ->
                    val parentField = parentMetadata.fields.find { it.columnName == parentCol }
                    val childField = childMetadata.fields.find { it.columnName == childCol }
                    
                    if (parentField != null && childField != null) {
                        if (!areTypesCompatible(parentField.type, childField.type)) {
                            issues.add(
                                ValidationIssue(
                                    type = ValidationIssueType.TYPE_MISMATCH,
                                    severity = ValidationSeverity.ERROR,
                                    entityName = entityName,
                                    fieldName = childCol,
                                    message = "Type mismatch: '$childCol' (${childField.type.simpleName}) in $entityName " +
                                            "does not match '$parentCol' (${parentField.type.simpleName}) in $parentName",
                                    suggestedFix = "Change type of '$childCol' to ${parentField.type.simpleName}",
                                    fixCodeExample = "val $childCol: ${parentField.type.simpleName}"
                                )
                            )
                        }
                    }
                }
            }
            
            // Check if foreign key columns have indexes
            val indexes = entityAnnotation.indices.toList()
            val indexedColumns = indexes.flatMap { it.value.toList() }
            
            foreignKey.childColumns.forEach { childColumn ->
                if (!indexedColumns.contains(childColumn)) {
                    issues.add(
                        ValidationIssue(
                            type = ValidationIssueType.MISSING_INDEX,
                            severity = ValidationSeverity.WARNING,
                            entityName = entityName,
                            fieldName = childColumn,
                            message = "Foreign key column '$childColumn' is not indexed, which may impact query performance",
                            suggestedFix = "Add an index on '$childColumn'",
                            fixCodeExample = """
                                @Entity(
                                    indices = [Index("$childColumn")]
                                )
                            """.trimIndent()
                        )
                    )
                }
            }
        }
        
        return issues
    }

    override fun validateRelations(
        entity: KClass<*>,
        allEntities: List<KClass<*>>
    ): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        val entityName = entity.simpleName ?: "Unknown"
        
        // Check for @Relation annotations in the entity's properties
        entity.memberProperties.forEach { property ->
            val relationAnnotation = property.annotations.find { it is Relation } as? Relation
            
            if (relationAnnotation != null) {
                val parentColumn = relationAnnotation.parentColumn
                val entityColumn = relationAnnotation.entityColumn
                
                // Validate that the parent column exists in this entity
                val metadata = extractEntityMetadata(entity)
                val hasParentColumn = metadata.fields.any { it.columnName == parentColumn }
                
                if (!hasParentColumn) {
                    issues.add(
                        ValidationIssue(
                            type = ValidationIssueType.RELATION_ANNOTATION_MISMATCH,
                            severity = ValidationSeverity.ERROR,
                            entityName = entityName,
                            fieldName = property.name,
                            message = "@Relation annotation references non-existent parent column '$parentColumn' in $entityName",
                            suggestedFix = "Add field '$parentColumn' to $entityName or fix the @Relation annotation"
                        )
                    )
                }
                
                // Note: Further validation would require inspecting the related entity type
                // which would need more complex reflection or annotation processing
            }
        }
        
        return issues
    }

    override fun generateFixSuggestions(
        issues: List<ValidationIssue>,
        entities: List<KClass<*>>
    ): List<FixSuggestion> {
        val suggestions = mutableListOf<FixSuggestion>()
        
        issues.forEach { issue ->
            when (issue.type) {
                ValidationIssueType.MISSING_INDEX -> {
                    issue.fieldName?.let { fieldName ->
                        suggestions.add(
                            FixSuggestion(
                                entityName = issue.entityName,
                                fixType = FixType.ADD_INDEX,
                                description = "Add index on column '$fieldName'",
                                code = """
                                    @Entity(
                                        indices = [Index("$fieldName")]
                                    )
                                """.trimIndent(),
                                canAutoApply = false
                            )
                        )
                    }
                }
                
                ValidationIssueType.TYPE_MISMATCH -> {
                    issue.fixCodeExample?.let { code ->
                        suggestions.add(
                            FixSuggestion(
                                entityName = issue.entityName,
                                fixType = FixType.FIX_TYPE_MISMATCH,
                                description = "Fix type mismatch for field '${issue.fieldName}'",
                                code = code,
                                canAutoApply = false
                            )
                        )
                    }
                }
                
                ValidationIssueType.MISSING_CASCADE_DELETE -> {
                    suggestions.add(
                        FixSuggestion(
                            entityName = issue.entityName,
                            fixType = FixType.ADD_CASCADE_DELETE,
                            description = "Add CASCADE delete to foreign key",
                            code = """
                                ForeignKey(
                                    entity = ParentEntity::class,
                                    parentColumns = ["parent_id"],
                                    childColumns = ["${issue.fieldName}"],
                                    onDelete = ForeignKey.CASCADE
                                )
                            """.trimIndent(),
                            canAutoApply = false
                        )
                    )
                }
                
                ValidationIssueType.MISSING_PRIMARY_KEY -> {
                    suggestions.add(
                        FixSuggestion(
                            entityName = issue.entityName,
                            fixType = FixType.ADD_PRIMARY_KEY,
                            description = "Add primary key to entity",
                            code = """
                                @Entity
                                data class ${issue.entityName}(
                                    @PrimaryKey(autoGenerate = true)
                                    val id: Int = 0
                                )
                            """.trimIndent(),
                            canAutoApply = false
                        )
                    )
                }
                
                else -> {
                    // For other types, use the suggested fix from the issue
                    issue.suggestedFix?.let { fix ->
                        suggestions.add(
                            FixSuggestion(
                                entityName = issue.entityName,
                                fixType = when (issue.type) {
                                    ValidationIssueType.DUPLICATE_INDEX -> FixType.REMOVE_DUPLICATE_INDEX
                                    ValidationIssueType.RELATION_ANNOTATION_MISMATCH -> FixType.FIX_RELATION_ANNOTATION
                                    else -> FixType.ADD_INDEX
                                },
                                description = fix,
                                code = issue.fixCodeExample ?: "",
                                canAutoApply = false
                            )
                        )
                    }
                }
            }
        }
        
        return suggestions.distinctBy { "${it.entityName}-${it.fixType}-${it.description}" }
    }

    // Private helper methods

    private fun validatePrimaryKeys(entity: KClass<*>, entityAnnotation: Entity): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        val entityName = entity.simpleName ?: "Unknown"
        val primaryKeyAnnotation = entityAnnotation.primaryKeys
        
        // Check if entity has primary keys defined
        val hasPrimaryKeys = primaryKeyAnnotation.isNotEmpty()
        
        // Check for @PrimaryKey annotation on fields
        val primaryKeyFields = entity.memberProperties.filter { property ->
            property.annotations.any { it is PrimaryKey }
        }
        
        if (!hasPrimaryKeys && primaryKeyFields.isEmpty()) {
            issues.add(
                ValidationIssue(
                    type = ValidationIssueType.MISSING_PRIMARY_KEY,
                    severity = ValidationSeverity.CRITICAL,
                    entityName = entityName,
                    message = "Entity does not have a primary key defined",
                    suggestedFix = "Add @PrimaryKey annotation to a field or define primaryKeys in @Entity annotation",
                    fixCodeExample = """
                        @Entity
                        data class $entityName(
                            @PrimaryKey(autoGenerate = true)
                            val id: Int = 0
                        )
                    """.trimIndent()
                )
            )
        }
        
        // Check for composite primary keys
        if (primaryKeyAnnotation.size > 1) {
            // Validate all composite key fields exist
            val metadata = extractEntityMetadata(entity)
            val fieldNames = metadata.fields.map { it.columnName }
            
            primaryKeyAnnotation.forEach { keyColumn ->
                if (!fieldNames.contains(keyColumn)) {
                    issues.add(
                        ValidationIssue(
                            type = ValidationIssueType.COMPOSITE_KEY_ISSUE,
                            severity = ValidationSeverity.CRITICAL,
                            entityName = entityName,
                            fieldName = keyColumn,
                            message = "Composite primary key references non-existent column '$keyColumn'",
                            suggestedFix = "Add field '$keyColumn' to the entity or remove it from primaryKeys"
                        )
                    )
                }
            }
        }
        
        return issues
    }

    private fun validateIndexes(
        entity: KClass<*>,
        entityAnnotation: Entity,
        config: ValidationConfig
    ): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        val entityName = entity.simpleName ?: "Unknown"
        val indexes = entityAnnotation.indices.toList()
        
        // Check for duplicate indexes
        if (config.checkDuplicateIndexes) {
            val indexColumns = indexes.map { it.value.toList().sorted() }
            val duplicates = indexColumns.groupBy { it }.filter { it.value.size > 1 }
            
            duplicates.forEach { (columns, _) ->
                issues.add(
                    ValidationIssue(
                        type = ValidationIssueType.DUPLICATE_INDEX,
                        severity = ValidationSeverity.WARNING,
                        entityName = entityName,
                        message = "Duplicate indexes found on columns: ${columns.joinToString(", ")}",
                        suggestedFix = "Remove duplicate index definitions"
                    )
                )
            }
        }
        
        return issues
    }

    private fun validateCascadeOperations(
        entity: KClass<*>,
        entityAnnotation: Entity
    ): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        val entityName = entity.simpleName ?: "Unknown"
        val foreignKeys = entityAnnotation.foreignKeys
        
        foreignKeys.forEach { foreignKey ->
            // Check if cascade delete is appropriate
            val onDelete = foreignKey.onDelete
            
            when (onDelete) {
                ForeignKey.NO_ACTION -> {
                    issues.add(
                        ValidationIssue(
                            type = ValidationIssueType.MISSING_CASCADE_DELETE,
                            severity = ValidationSeverity.WARNING,
                            entityName = entityName,
                            message = "Foreign key to ${foreignKey.entity.simpleName} has NO_ACTION on delete. " +
                                    "Consider using CASCADE to maintain referential integrity",
                            suggestedFix = "Change onDelete to ForeignKey.CASCADE if child records should be deleted with parent"
                        )
                    )
                }
                
                ForeignKey.SET_NULL -> {
                    // Validate that child columns are nullable
                    val metadata = extractEntityMetadata(entity)
                    foreignKey.childColumns.forEach { childColumn ->
                        val field = metadata.fields.find { it.columnName == childColumn }
                        if (field != null && !field.isNullable) {
                            issues.add(
                                ValidationIssue(
                                    type = ValidationIssueType.INVALID_CASCADE_OPERATION,
                                    severity = ValidationSeverity.ERROR,
                                    entityName = entityName,
                                    fieldName = childColumn,
                                    message = "Foreign key with SET_NULL references non-nullable column '$childColumn'",
                                    suggestedFix = "Make '$childColumn' nullable or change onDelete action",
                                    fixCodeExample = "val $childColumn: Type? = null"
                                )
                            )
                        }
                    }
                }
            }
        }
        
        return issues
    }

    private fun checkCircularDependencies(
        entityMetadataMap: Map<KClass<*>, EntityMetadata>
    ): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        
        // Build dependency graph
        val dependencyGraph = entityMetadataMap.mapValues { (_, metadata) ->
            metadata.foreignKeys.map { it.parentEntity }
        }
        
        // Check for circular dependencies using DFS
        dependencyGraph.keys.forEach { entity ->
            val visited = mutableSetOf<KClass<*>>()
            val recursionStack = mutableSetOf<KClass<*>>()
            
            if (hasCycle(entity, dependencyGraph, visited, recursionStack)) {
                issues.add(
                    ValidationIssue(
                        type = ValidationIssueType.CIRCULAR_DEPENDENCY,
                        severity = ValidationSeverity.WARNING,
                        entityName = entity.simpleName ?: "Unknown",
                        message = "Circular dependency detected involving entity ${entity.simpleName}",
                        suggestedFix = "Review foreign key relationships to break the circular dependency"
                    )
                )
            }
        }
        
        return issues.distinctBy { it.entityName }
    }

    private fun hasCycle(
        entity: KClass<*>,
        graph: Map<KClass<*>, List<KClass<*>>>,
        visited: MutableSet<KClass<*>>,
        recursionStack: MutableSet<KClass<*>>
    ): Boolean {
        visited.add(entity)
        recursionStack.add(entity)
        
        graph[entity]?.forEach { dependency ->
            if (!visited.contains(dependency)) {
                if (hasCycle(dependency, graph, visited, recursionStack)) {
                    return true
                }
            } else if (recursionStack.contains(dependency)) {
                return true
            }
        }
        
        recursionStack.remove(entity)
        return false
    }

    private fun checkOrphanedEntities(
        entityMetadataMap: Map<KClass<*>, EntityMetadata>
    ): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        
        // Find entities that are never referenced by foreign keys
        val referencedEntities = entityMetadataMap.values
            .flatMap { it.foreignKeys }
            .map { it.parentEntity }
            .toSet()
        
        val allEntities = entityMetadataMap.keys
        val orphanedEntities = allEntities - referencedEntities
        
        // Filter out entities that are intentionally standalone (like User, Settings, etc.)
        val intentionallyStandalone = setOf("User", "Settings", "Personality", "SecurityQuestion")
        
        orphanedEntities.forEach { entity ->
            val entityName = entity.simpleName ?: "Unknown"
            if (!intentionallyStandalone.contains(entityName)) {
                issues.add(
                    ValidationIssue(
                        type = ValidationIssueType.ORPHANED_ENTITY,
                        severity = ValidationSeverity.INFO,
                        entityName = entityName,
                        message = "Entity is not referenced by any foreign keys. Consider if relationships are missing.",
                        suggestedFix = "Review if this entity should be related to other entities"
                    )
                )
            }
        }
        
        return issues
    }

    private fun extractEntityMetadata(entity: KClass<*>): EntityMetadata {
        val entityAnnotation = entity.findAnnotation<Entity>()
            ?: throw IllegalArgumentException("Class ${entity.simpleName} is not annotated with @Entity")
        
        val tableName = entityAnnotation.tableName.ifEmpty { entity.simpleName ?: "" }
        
        // Extract fields from constructor parameters
        val constructor = entity.primaryConstructor
        val fields = constructor?.parameters?.mapNotNull { param ->
            val columnInfo = param.annotations.find { it is ColumnInfo } as? ColumnInfo
            val primaryKey = param.annotations.find { it is PrimaryKey } as? PrimaryKey
            val type = param.type.classifier as? KClass<*> ?: return@mapNotNull null
            
            FieldMetadata(
                name = param.name ?: "",
                columnName = columnInfo?.name?.ifEmpty { param.name ?: "" } ?: param.name ?: "",
                type = type,
                isNullable = param.type.isMarkedNullable,
                isPrimaryKey = primaryKey != null,
                isAutoGenerate = primaryKey?.autoGenerate ?: false
            )
        } ?: emptyList()
        
        // Extract primary keys
        val primaryKeys = if (entityAnnotation.primaryKeys.isNotEmpty()) {
            fields.filter { entityAnnotation.primaryKeys.contains(it.columnName) }
        } else {
            fields.filter { it.isPrimaryKey }
        }
        
        // Extract foreign keys
        val foreignKeys = entityAnnotation.foreignKeys.map { fk ->
            ForeignKeyMetadata(
                parentEntity = fk.entity,
                parentColumns = fk.parentColumns.toList(),
                childColumns = fk.childColumns.toList(),
                onDelete = fk.onDelete,
                onUpdate = fk.onUpdate,
                deferred = fk.deferred
            )
        }
        
        // Extract indexes
        val indexes = entityAnnotation.indices.map { index ->
            IndexMetadata(
                name = index.name.ifEmpty { null },
                columns = index.value.toList(),
                unique = index.unique,
                orders = index.orders.toList().map { it.name }
            )
        }
        
        return EntityMetadata(
            entityClass = entity,
            tableName = tableName,
            primaryKeys = primaryKeys,
            fields = fields,
            foreignKeys = foreignKeys,
            indexes = indexes,
            relations = emptyList() // Relations would need more complex extraction
        )
    }

    private fun areTypesCompatible(type1: KClass<*>, type2: KClass<*>): Boolean {
        // Direct match
        if (type1 == type2) return true
        
        // Check for numeric type compatibility
        val numericTypes = setOf(
            Int::class, Long::class, Short::class, Byte::class,
            Float::class, Double::class
        )
        
        if (type1 in numericTypes && type2 in numericTypes) {
            // Allow compatible numeric types
            return true
        }
        
        // Check for string compatibility
        if ((type1 == String::class && type2 == String::class)) {
            return true
        }
        
        return false
    }
}
