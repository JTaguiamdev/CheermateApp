package com.cheermateapp.util

import android.util.Patterns

/**
 * Utility class for input validation
 */
object InputValidationUtil {
    
    /**
     * Validate username format
     * Rules: 3-20 characters, alphanumeric and underscore only
     */
    fun isValidUsername(username: String): Boolean {
        val usernamePattern = "^[a-zA-Z0-9_]{3,20}$".toRegex()
        return username.matches(usernamePattern)
    }
    
    /**
     * Validate email format
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Validate password strength
     * Rules: At least 6 characters (can be enhanced later)
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
    
    /**
     * Enhanced password validation with strength requirements
     * Rules: At least 8 characters, one uppercase, one lowercase, one number
     */
    fun isStrongPassword(password: String): Boolean {
        val hasMinLength = password.length >= 8
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        
        return hasMinLength && hasUpperCase && hasLowerCase && hasDigit
    }
    
    /**
     * Get password strength score (0-4)
     */
    fun getPasswordStrength(password: String): Int {
        var strength = 0
        if (password.length >= 8) strength++
        if (password.any { it.isUpperCase() }) strength++
        if (password.any { it.isLowerCase() }) strength++
        if (password.any { it.isDigit() }) strength++
        if (password.any { !it.isLetterOrDigit() }) strength++
        return strength.coerceIn(0, 4)
    }
    
    /**
     * Sanitize input to prevent SQL injection
     * Note: Room/SQLite parameterized queries already prevent SQL injection,
     * but this provides an extra layer of validation
     */
    fun sanitizeInput(input: String): String {
        // Remove potentially dangerous characters
        return input.replace(Regex("[<>\"'%;()&+]"), "")
    }
    
    /**
     * Validate that input doesn't contain SQL injection patterns
     */
    fun containsSQLInjectionPattern(input: String): Boolean {
        val sqlPatterns = listOf(
            "(?i).*\\bor\\b.*=.*",
            "(?i).*\\band\\b.*=.*",
            "(?i).*\\bdrop\\b.*",
            "(?i).*\\bdelete\\b.*",
            "(?i).*\\binsert\\b.*",
            "(?i).*\\bupdate\\b.*",
            "(?i).*\\bselect\\b.*from.*",
            "(?i).*\\bexec\\b.*",
            "(?i).*\\bunion\\b.*",
            ".*--.*",
            ".*/\\*.*\\*/.*",
            ".*';.*"
        )
        
        return sqlPatterns.any { pattern ->
            input.matches(pattern.toRegex())
        }
    }
}
