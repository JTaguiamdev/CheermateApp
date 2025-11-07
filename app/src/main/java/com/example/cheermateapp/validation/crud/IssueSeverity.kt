package com.example.cheermateapp.validation.crud

/**
 * Enum representing the severity level of CRUD validation issues
 */
enum class IssueSeverity {
    CRITICAL,    // Operation completely fails
    WARNING,     // Operation works but has issues
    INFO         // Informational, best practice suggestions
}
