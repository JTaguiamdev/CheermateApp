# Database Schema Validation Quick Reference

## Quick Start

```kotlin
import com.example.cheermateapp.data.validation.DatabaseSchemaValidationHelper

// Validate entire schema
val result = DatabaseSchemaValidationHelper.validateSchema()
println(result.toReport())
```

## Common Commands

### Validate and Print Report
```kotlin
DatabaseSchemaValidationHelper.validateAndPrintReport()
```

### Quick Check (Critical Only)
```kotlin
val result = DatabaseSchemaValidationHelper.quickValidate()
if (result.hasCriticalIssues()) {
    println("⚠️ Critical issues found!")
}
```

### Comprehensive Check (All Issues)
```kotlin
val result = DatabaseSchemaValidationHelper.comprehensiveValidate()
```

### Validate Specific Entities
```kotlin
DatabaseSchemaValidationHelper.validateEntities(
    User::class,
    Task::class
)
```

## Filter Results

```kotlin
val result = DatabaseSchemaValidationHelper.validateSchema()

// By severity
val critical = result.getIssuesBySeverity(ValidationSeverity.CRITICAL)
val errors = result.getIssuesBySeverity(ValidationSeverity.ERROR)
val warnings = result.getIssuesBySeverity(ValidationSeverity.WARNING)

// By entity
val taskIssues = result.getIssuesByEntity("Task")

// By type
val fkIssues = result.getIssuesByType(ValidationIssueType.INVALID_FOREIGN_KEY_REFERENCE)
```

## Custom Configuration

```kotlin
val config = ValidationConfig(
    validatePrimaryKeys = true,
    validateForeignKeys = true,
    validateRelations = true,
    checkMissingIndexes = true,
    checkDuplicateIndexes = true,
    checkCascadeOperations = true,
    checkCircularDependencies = true,
    checkTypeCompatibility = true,
    minSeverity = ValidationSeverity.WARNING
)
DatabaseSchemaValidationHelper.validateSchema(config)
```

## Severity Levels

- **CRITICAL** - Will cause runtime errors (e.g., missing primary keys)
- **ERROR** - Should be fixed (e.g., type mismatches)
- **WARNING** - Best practice issues (e.g., missing indexes)
- **INFO** - Suggestions (e.g., orphaned entities)

## Issue Types

| Type | Description |
|------|-------------|
| `MISSING_PRIMARY_KEY` | Entity lacks a primary key |
| `INVALID_FOREIGN_KEY_REFERENCE` | FK references non-existent entity/column |
| `TYPE_MISMATCH` | Type incompatibility between related columns |
| `MISSING_INDEX` | Foreign key column lacks an index |
| `MISSING_CASCADE_DELETE` | FK lacks CASCADE delete |
| `DUPLICATE_INDEX` | Multiple indexes on same columns |
| `COMPOSITE_KEY_ISSUE` | Problem with composite primary keys |
| `RELATION_ANNOTATION_MISMATCH` | @Relation annotation inconsistency |
| `ORPHANED_ENTITY` | Entity not referenced by any FK |
| `CIRCULAR_DEPENDENCY` | Circular FK dependencies |
| `INVALID_CASCADE_OPERATION` | Invalid cascade (e.g., SET_NULL on non-nullable) |

## Fix Types

| Fix Type | Description |
|----------|-------------|
| `ADD_PRIMARY_KEY` | Add a primary key to entity |
| `ADD_FOREIGN_KEY` | Add foreign key constraint |
| `ADD_INDEX` | Add index on column |
| `FIX_TYPE_MISMATCH` | Change column type |
| `ADD_CASCADE_DELETE` | Add CASCADE to FK |
| `REMOVE_DUPLICATE_INDEX` | Remove duplicate index |
| `FIX_RELATION_ANNOTATION` | Fix @Relation annotation |
| `ADD_COMPOSITE_PRIMARY_KEY` | Add composite primary key |

## Integration Examples

### In Tests
```kotlin
@Test
fun validateDatabaseSchema() {
    val result = DatabaseSchemaValidationHelper.quickValidate()
    assertFalse("Schema has critical issues", result.hasCriticalIssues())
}
```

### In Application (Debug)
```kotlin
class CheermateApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Thread {
                val result = DatabaseSchemaValidationHelper.quickValidate()
                if (result.hasCriticalIssues()) {
                    Log.e("Schema", result.toReport())
                }
            }.start()
        }
    }
}
```

### Export Report
```kotlin
val result = DatabaseSchemaValidationHelper.comprehensiveValidate()
val file = File(context.filesDir, "validation_report.txt")
file.writeText(result.toReport())
```

## Validation Result API

```kotlin
data class ValidationResult(
    val isValid: Boolean,
    val issues: List<ValidationIssue>,
    val fixSuggestions: List<FixSuggestion>,
    val summary: ValidationSummary
)

// Methods
result.hasCriticalIssues(): Boolean
result.getIssuesBySeverity(severity): List<ValidationIssue>
result.getIssuesByEntity(name): List<ValidationIssue>
result.getIssuesByType(type): List<ValidationIssue>
result.toReport(): String
```

## CheermateApp Entities

Entities validated by the system:
- User
- Task (composite PK: Task_ID, User_ID)
- SubTask (composite PK: Subtask_ID, Task_ID, User_ID)
- TaskReminder (composite PK: TaskReminder_ID, Task_ID, User_ID)
- TaskDependency (composite PK: Task_ID, User_ID, DependsOn_Task_ID)
- TaskTemplate
- RecurringTask
- Settings
- Personality
- PersonalityType
- SecurityQuestion
- UserSecurityAnswer
- MessageTemplate

## Example Output

```
================================================================================
Database Schema Validation Report
================================================================================
Timestamp: 2024-01-15 10:30:45
Overall Status: ✓ VALID

Summary:
  Total Entities: 13
  Total Relationships: 18
  Critical Issues: 0
  Errors: 0
  Warnings: 2
  Info Messages: 1

Issues Found:
--------------------------------------------------------------------------------

WARNING (2):
  1. [TaskReminder.Task_ID]
     Foreign key column 'Task_ID' is not indexed, which may impact query performance
     → Fix: Add an index on 'Task_ID'

  2. [RecurringTask]
     Foreign key to User has NO_ACTION on delete. Consider using CASCADE
     → Fix: Change onDelete to ForeignKey.CASCADE

INFO (1):
  1. [TaskTemplate]
     Entity is not referenced by any foreign keys. Consider if relationships are missing.
     → Fix: Review if this entity should be related to other entities

================================================================================
```

## Performance

- **Fast**: <100ms for full validation
- **Non-blocking**: Run on background thread
- **Memory efficient**: Metadata extracted on-demand

## Documentation

- **Full Guide**: `DATABASE_SCHEMA_VALIDATION_GUIDE.md`
- **Examples**: `app/src/main/java/.../examples/DatabaseSchemaValidationExample.kt`
- **Tests**: `app/src/test/java/.../validation/`

## Support

For issues or questions, refer to:
1. Full documentation in `DATABASE_SCHEMA_VALIDATION_GUIDE.md`
2. Example code in `DatabaseSchemaValidationExample.kt`
3. Test cases for usage patterns
