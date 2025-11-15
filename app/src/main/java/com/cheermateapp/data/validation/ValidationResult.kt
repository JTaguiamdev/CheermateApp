package com.cheermateapp.data.validation

/**
 * Severity level for validation issues
 */
enum class ValidationSeverity {
    /** Critical issue that will cause runtime errors */
    CRITICAL,
    /** Error that should be fixed but may not crash the app */
    ERROR,
    /** Warning about potential issues or bad practices */
    WARNING,
    /** Informational suggestion for optimization */
    INFO
}

/**
 * Type of validation issue
 */
enum class ValidationIssueType {
    MISSING_PRIMARY_KEY,
    MISSING_FOREIGN_KEY,
    INVALID_FOREIGN_KEY_REFERENCE,
    TYPE_MISMATCH,
    MISSING_INDEX,
    MISSING_CASCADE_DELETE,
    DUPLICATE_INDEX,
    COMPOSITE_KEY_ISSUE,
    RELATION_ANNOTATION_MISMATCH,
    ORPHANED_ENTITY,
    CIRCULAR_DEPENDENCY,
    INVALID_CASCADE_OPERATION
}

/**
 * Represents a single validation issue found in the database schema
 */
data class ValidationIssue(
    /** Type of the issue */
    val type: ValidationIssueType,
    /** Severity level */
    val severity: ValidationSeverity,
    /** Entity class name where the issue was found */
    val entityName: String,
    /** Field/column name if applicable */
    val fieldName: String? = null,
    /** Detailed description of the issue */
    val message: String,
    /** Line number in source code (if available) */
    val lineNumber: Int? = null,
    /** Suggested fix for the issue */
    val suggestedFix: String? = null,
    /** Code example showing the fix */
    val fixCodeExample: String? = null
)

/**
 * Represents a suggested fix for validation issues
 */
data class FixSuggestion(
    /** Entity class name to fix */
    val entityName: String,
    /** Type of fix to apply */
    val fixType: FixType,
    /** Description of what the fix does */
    val description: String,
    /** Kotlin code to apply the fix */
    val code: String,
    /** Whether this fix can be applied automatically */
    val canAutoApply: Boolean = false
)

/**
 * Type of fix suggestion
 */
enum class FixType {
    ADD_PRIMARY_KEY,
    ADD_FOREIGN_KEY,
    ADD_INDEX,
    FIX_TYPE_MISMATCH,
    ADD_CASCADE_DELETE,
    REMOVE_DUPLICATE_INDEX,
    FIX_RELATION_ANNOTATION,
    ADD_COMPOSITE_PRIMARY_KEY
}

/**
 * Result of database schema validation
 */
data class ValidationResult(
    /** Whether the entire schema is valid */
    val isValid: Boolean,
    /** List of all issues found */
    val issues: List<ValidationIssue>,
    /** List of suggested fixes */
    val fixSuggestions: List<FixSuggestion>,
    /** Summary statistics */
    val summary: ValidationSummary,
    /** Timestamp when validation was performed */
    val timestamp: Long = System.currentTimeMillis()
) {
    /** Get issues by severity */
    fun getIssuesBySeverity(severity: ValidationSeverity): List<ValidationIssue> {
        return issues.filter { it.severity == severity }
    }

    /** Get issues by entity name */
    fun getIssuesByEntity(entityName: String): List<ValidationIssue> {
        return issues.filter { it.entityName == entityName }
    }

    /** Get issues by type */
    fun getIssuesByType(type: ValidationIssueType): List<ValidationIssue> {
        return issues.filter { it.type == type }
    }

    /** Check if there are any critical issues */
    fun hasCriticalIssues(): Boolean {
        return issues.any { it.severity == ValidationSeverity.CRITICAL }
    }

    /** Get a formatted report */
    fun toReport(): String {
        val sb = StringBuilder()
        sb.appendLine("=".repeat(80))
        sb.appendLine("Database Schema Validation Report")
        sb.appendLine("=".repeat(80))
        sb.appendLine("Timestamp: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(timestamp))}")
        sb.appendLine("Overall Status: ${if (isValid) "✓ VALID" else "✗ INVALID"}")
        sb.appendLine()
        sb.appendLine(summary.toString())
        sb.appendLine()

        if (issues.isNotEmpty()) {
            sb.appendLine("Issues Found:")
            sb.appendLine("-".repeat(80))
            
            ValidationSeverity.values().forEach { severity ->
                val issuesOfSeverity = getIssuesBySeverity(severity)
                if (issuesOfSeverity.isNotEmpty()) {
                    sb.appendLine()
                    sb.appendLine("${severity.name} (${issuesOfSeverity.size}):")
                    issuesOfSeverity.forEachIndexed { index, issue ->
                        sb.appendLine("  ${index + 1}. [${issue.entityName}${issue.fieldName?.let { ".$it" } ?: ""}]")
                        sb.appendLine("     ${issue.message}")
                        issue.suggestedFix?.let {
                            sb.appendLine("     → Fix: $it")
                        }
                    }
                }
            }
        }

        if (fixSuggestions.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("Suggested Fixes:")
            sb.appendLine("-".repeat(80))
            fixSuggestions.forEachIndexed { index, fix ->
                sb.appendLine("  ${index + 1}. ${fix.entityName} - ${fix.description}")
                sb.appendLine("     ${fix.code.lines().joinToString("\n     ")}")
            }
        }

        sb.appendLine()
        sb.appendLine("=".repeat(80))
        return sb.toString()
    }
}

/**
 * Summary statistics for validation
 */
data class ValidationSummary(
    /** Total number of entities validated */
    val totalEntities: Int,
    /** Total number of relationships validated */
    val totalRelationships: Int,
    /** Number of critical issues */
    val criticalIssues: Int,
    /** Number of errors */
    val errors: Int,
    /** Number of warnings */
    val warnings: Int,
    /** Number of info messages */
    val infoMessages: Int
) {
    override fun toString(): String {
        return """
            |Summary:
            |  Total Entities: $totalEntities
            |  Total Relationships: $totalRelationships
            |  Critical Issues: $criticalIssues
            |  Errors: $errors
            |  Warnings: $warnings
            |  Info Messages: $infoMessages
        """.trimMargin()
    }
}
