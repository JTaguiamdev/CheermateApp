package com.example.cheermateapp.data.validation

import kotlin.reflect.KClass

/**
 * Interface for validating database schema
 */
interface SchemaValidator {
    /**
     * Validate the entire database schema
     * @param entities List of entity classes to validate
     * @param config Validation configuration
     * @return Validation result with issues and suggestions
     */
    fun validate(
        entities: List<KClass<*>>,
        config: ValidationConfig = ValidationConfig()
    ): ValidationResult

    /**
     * Validate a single entity
     * @param entity Entity class to validate
     * @param allEntities All entities in the database for reference checking
     * @param config Validation configuration
     * @return List of validation issues found
     */
    fun validateEntity(
        entity: KClass<*>,
        allEntities: List<KClass<*>>,
        config: ValidationConfig = ValidationConfig()
    ): List<ValidationIssue>

    /**
     * Validate foreign key relationships
     * @param entity Entity with foreign keys
     * @param allEntities All entities in the database
     * @return List of validation issues found
     */
    fun validateForeignKeys(
        entity: KClass<*>,
        allEntities: List<KClass<*>>
    ): List<ValidationIssue>

    /**
     * Validate relation annotations
     * @param entity Entity with relations
     * @param allEntities All entities in the database
     * @return List of validation issues found
     */
    fun validateRelations(
        entity: KClass<*>,
        allEntities: List<KClass<*>>
    ): List<ValidationIssue>

    /**
     * Generate fix suggestions for validation issues
     * @param issues List of validation issues
     * @param entities All entities in the database
     * @return List of suggested fixes
     */
    fun generateFixSuggestions(
        issues: List<ValidationIssue>,
        entities: List<KClass<*>>
    ): List<FixSuggestion>
}
