# Database Schema Validation System

A comprehensive, production-ready database schema validation system for Room Database in the CheermateApp. This system validates entity relationships, foreign keys, primary keys, indexes, and provides actionable fix suggestions.

## Features

### Core Validation Capabilities

1. **Entity Validation**
   - Primary key validation (single and composite)
   - Entity annotation verification
   - Field type checking

2. **Foreign Key Validation**
   - Parent entity existence verification
   - Parent-child column type compatibility
   - Foreign key constraint consistency
   - Missing index detection on FK columns

3. **Relationship Validation**
   - @Relation annotation consistency
   - Parent-child column mapping verification
   - Relationship type validation

4. **Index Optimization**
   - Missing index detection
   - Duplicate index identification
   - Index performance suggestions

5. **Cascade Operation Validation**
   - CASCADE delete verification
   - SET_NULL with nullable columns
   - Referential integrity checks

6. **Advanced Checks**
   - Circular dependency detection
   - Orphaned entity identification
   - Composite key validation
   - Type compatibility across relationships

## Usage

### Basic Validation

```kotlin
import com.example.cheermateapp.data.validation.DatabaseSchemaValidationHelper

// Validate entire database schema
val result = DatabaseSchemaValidationHelper.validateSchema()

if (result.isValid) {
    println("✓ Database schema is valid!")
} else {
    println("✗ Found ${result.issues.size} issues")
    result.issues.forEach { issue ->
        println("${issue.severity}: ${issue.message}")
    }
}
```

### Print Validation Report

```kotlin
// Validate and print formatted report
DatabaseSchemaValidationHelper.validateAndPrintReport()
```

### Quick Validation (Critical Issues Only)

```kotlin
// Only check for critical issues
val result = DatabaseSchemaValidationHelper.quickValidate()

if (result.hasCriticalIssues()) {
    println("Critical issues found that will cause runtime errors!")
}
```

### Comprehensive Validation (All Checks)

```kotlin
// Run all validation checks
val result = DatabaseSchemaValidationHelper.comprehensiveValidate()
println(result.toReport())
```

### Custom Validation Configuration

```kotlin
import com.example.cheermateapp.data.validation.ValidationConfig
import com.example.cheermateapp.data.validation.ValidationSeverity

val config = ValidationConfig(
    validatePrimaryKeys = true,
    validateForeignKeys = true,
    validateRelations = true,
    checkMissingIndexes = true,
    checkDuplicateIndexes = true,
    checkCascadeOperations = true,
    checkCircularDependencies = true,
    checkTypeCompatibility = true,
    minSeverity = ValidationSeverity.WARNING  // Only show WARNING and above
)

val result = DatabaseSchemaValidationHelper.validateSchema(config)
```

### Validate Specific Entities

```kotlin
import com.example.cheermateapp.data.model.User
import com.example.cheermateapp.data.model.Task

// Validate only specific entities
val result = DatabaseSchemaValidationHelper.validateEntities(
    User::class,
    Task::class
)
```

### Filter Results

```kotlin
val result = DatabaseSchemaValidationHelper.validateSchema()

// Get only critical issues
val criticalIssues = result.getIssuesBySeverity(ValidationSeverity.CRITICAL)

// Get issues for specific entity
val taskIssues = result.getIssuesByEntity("Task")

// Get specific type of issues
val foreignKeyIssues = result.getIssuesByType(ValidationIssueType.INVALID_FOREIGN_KEY_REFERENCE)
```

### Get Fix Suggestions

```kotlin
val result = DatabaseSchemaValidationHelper.comprehensiveValidate()

result.fixSuggestions.forEach { fix ->
    println("Entity: ${fix.entityName}")
    println("Fix: ${fix.description}")
    println("Code:")
    println(fix.code)
    println()
}
```

## Validation Result Structure

```kotlin
data class ValidationResult(
    val isValid: Boolean,                    // Overall validation status
    val issues: List<ValidationIssue>,       // All issues found
    val fixSuggestions: List<FixSuggestion>, // Suggested fixes
    val summary: ValidationSummary,          // Summary statistics
    val timestamp: Long                      // When validation was performed
)
```

## Validation Issues

### Issue Severity Levels

- **CRITICAL**: Will cause runtime errors (missing primary keys, invalid foreign keys)
- **ERROR**: Should be fixed but may not crash (type mismatches, missing relations)
- **WARNING**: Potential issues or bad practices (missing indexes, no cascade delete)
- **INFO**: Informational suggestions (orphaned entities, optimization tips)

### Issue Types

- `MISSING_PRIMARY_KEY`: Entity lacks a primary key
- `MISSING_FOREIGN_KEY`: Expected foreign key is missing
- `INVALID_FOREIGN_KEY_REFERENCE`: Foreign key references non-existent entity/column
- `TYPE_MISMATCH`: Type incompatibility between related columns
- `MISSING_INDEX`: Foreign key column lacks an index
- `MISSING_CASCADE_DELETE`: Foreign key lacks CASCADE delete
- `DUPLICATE_INDEX`: Multiple indexes on same columns
- `COMPOSITE_KEY_ISSUE`: Problem with composite primary keys
- `RELATION_ANNOTATION_MISMATCH`: @Relation annotation inconsistency
- `ORPHANED_ENTITY`: Entity not referenced by any foreign key
- `CIRCULAR_DEPENDENCY`: Circular foreign key dependencies
- `INVALID_CASCADE_OPERATION`: Invalid cascade operation (e.g., SET_NULL on non-nullable)

## Validation Report Example

```
================================================================================
Database Schema Validation Report
================================================================================
Timestamp: 2024-01-15 10:30:45
Overall Status: ✗ INVALID

Summary:
  Total Entities: 13
  Total Relationships: 18
  Critical Issues: 0
  Errors: 2
  Warnings: 5
  Info Messages: 3

Issues Found:
--------------------------------------------------------------------------------

ERROR (2):
  1. [Task.User_ID]
     Type mismatch: 'User_ID' (Int) in Task does not match 'User_ID' (Long) in User
     → Fix: Change type of 'User_ID' to Long

  2. [SubTask]
     @Relation annotation references non-existent parent column 'taskId' in SubTask
     → Fix: Add field 'taskId' to SubTask or fix the @Relation annotation

WARNING (5):
  1. [TaskReminder.Task_ID]
     Foreign key column 'Task_ID' is not indexed, which may impact query performance
     → Fix: Add an index on 'Task_ID'

  ...

Suggested Fixes:
--------------------------------------------------------------------------------
  1. Task - Add index on column 'User_ID'
     @Entity(
         indices = [Index("User_ID")]
     )

  2. SubTask - Fix type mismatch for field 'Task_ID'
     val Task_ID: Int

================================================================================
```

## Integration into Build Process

### Run validation before tests

Add to your test suite:

```kotlin
class DatabaseSchemaValidationTest {
    @Test
    fun validateDatabaseSchema() {
        val result = DatabaseSchemaValidationHelper.quickValidate()
        
        if (result.hasCriticalIssues()) {
            val report = result.toReport()
            fail("Database schema has critical issues:\n$report")
        }
    }
}
```

### Run validation on app startup (Debug builds only)

```kotlin
class CheermateApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        if (BuildConfig.DEBUG) {
            // Validate schema on debug builds
            Thread {
                val result = DatabaseSchemaValidationHelper.quickValidate()
                if (result.hasCriticalIssues()) {
                    Log.e("SchemaValidation", result.toReport())
                }
            }.start()
        }
    }
}
```

## Architecture

### Class Hierarchy

```
SchemaValidator (interface)
    └── RoomSchemaValidator (implementation)

DatabaseSchemaValidationHelper (helper)
    └── Uses RoomSchemaValidator

ValidationResult (data classes)
    ├── ValidationIssue
    ├── FixSuggestion
    └── ValidationSummary

EntityMetadata (metadata classes)
    ├── FieldMetadata
    ├── ForeignKeyMetadata
    ├── IndexMetadata
    └── RelationMetadata
```

### Extensibility

The system is designed to be extensible:

1. **Custom Validators**: Implement `SchemaValidator` interface
2. **Custom Validation Rules**: Extend `RoomSchemaValidator`
3. **Custom Issue Types**: Add to `ValidationIssueType` enum
4. **Custom Fix Generators**: Override `generateFixSuggestions()`

## Performance

- **Fast**: Validation uses Kotlin reflection, typically completes in <100ms
- **Non-blocking**: Can be run on background thread
- **Memory efficient**: Metadata is extracted on-demand
- **Cacheable**: Results can be cached and reused

## Best Practices

1. **Run in CI/CD**: Include validation in your continuous integration pipeline
2. **Pre-deployment**: Validate schema before deploying to production
3. **After schema changes**: Run comprehensive validation after modifying entities
4. **Code reviews**: Include validation report in code review process
5. **Documentation**: Keep validation results as documentation of schema state

## Troubleshooting

### "Entity is not annotated with @Entity"
Ensure all data classes used in the database have the `@Entity` annotation.

### "Foreign key references non-existent entity"
Make sure all referenced entities are included in the `entities` list of your `@Database` annotation.

### "Type mismatch" errors
Foreign key columns must have the same type as the primary key they reference.

### "Missing index" warnings
Add `indices = [Index("column_name")]` to the `@Entity` annotation for better query performance.

## Migration Script Generation

The system provides suggestions for migration scripts:

```kotlin
val result = DatabaseSchemaValidationHelper.comprehensiveValidate()

// Generate migration suggestions
result.fixSuggestions.forEach { fix ->
    when (fix.fixType) {
        FixType.ADD_INDEX -> {
            println("ALTER TABLE ${fix.entityName} ADD INDEX ...")
        }
        FixType.ADD_FOREIGN_KEY -> {
            println("ALTER TABLE ${fix.entityName} ADD FOREIGN KEY ...")
        }
        // ... other fix types
    }
}
```

## Contributing

To add new validation rules:

1. Add new `ValidationIssueType` if needed
2. Implement validation logic in `RoomSchemaValidator`
3. Add corresponding `FixType` and fix generation logic
4. Add unit tests for the new validation
5. Update this documentation

## License

Part of the CheermateApp project.
