package com.cheermateapp.util

import at.favre.lib.crypto.bcrypt.BCrypt

/**
 * Utility class for password hashing and verification using BCrypt
 */
object PasswordHashUtil {
    
    private const val BCRYPT_COST = 12 // Recommended cost factor for BCrypt
    
    /**
     * Hash a plain text password using BCrypt
     * @param password The plain text password to hash
     * @return The hashed password
     */
    fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray())
    }
    
    /**
     * Verify a plain text password against a hashed password
     * @param password The plain text password to verify
     * @param hashedPassword The hashed password to verify against
     * @return True if the password matches, false otherwise
     */
    fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return try {
            val result = BCrypt.verifyer().verify(password.toCharArray(), hashedPassword)
            result.verified
        } catch (e: Exception) {
            android.util.Log.e("PasswordHashUtil", "Error verifying password", e)
            false
        }
    }
}
