package com.cheermateapp.util

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Utility class for password hashing and verification using PBKDF2-HMAC-SHA256
 */
object PasswordHashUtil {
    
    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 100000 // OWASP recommended minimum
    private const val MIN_ITERATIONS = 10000 // Minimum acceptable iterations
    private const val SALT_LENGTH = 16 // 128 bits
    private const val HASH_LENGTH = 32 // 256 bits
    
    /**
     * Hash a plain text password using PBKDF2-HMAC-SHA256
     * @param password The plain text password to hash
     * @return The hashed password in the format: iterations:salt:hash (Base64 encoded)
     */
    fun hashPassword(password: String): String {
        // Generate a random salt
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        
        // Hash the password
        val hash = pbkdf2Hash(password, salt, ITERATIONS, HASH_LENGTH)
        
        // Return the hash in the format: iterations:salt:hash (all Base64 encoded)
        val saltEncoded = Base64.getEncoder().encodeToString(salt)
        val hashEncoded = Base64.getEncoder().encodeToString(hash)
        
        return "$ITERATIONS:$saltEncoded:$hashEncoded"
    }
    
    /**
     * Verify a plain text password against a hashed password
     * @param password The plain text password to verify
     * @param hashedPassword The hashed password to verify against (format: iterations:salt:hash)
     * @return True if the password matches, false otherwise
     */
    fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return try {
            // Parse the stored hash
            val parts = hashedPassword.split(":")
            if (parts.size != 3) {
                return false
            }
            
            val iterations = parts[0].toIntOrNull() ?: return false
            
            // Validate minimum iteration count to prevent bypass attacks
            if (iterations < MIN_ITERATIONS) {
                return false
            }
            
            val salt = Base64.getDecoder().decode(parts[1])
            val storedHash = Base64.getDecoder().decode(parts[2])
            
            // Hash the provided password with the same salt and iterations
            val testHash = pbkdf2Hash(password, salt, iterations, storedHash.size)
            
            // Compare the hashes using constant-time comparison to prevent timing attacks
            return MessageDigest.isEqual(storedHash, testHash)
        } catch (e: Exception) {
            android.util.Log.e("PasswordHashUtil", "Error verifying password", e)
            false
        }
    }
    
    /**
     * Internal function to perform PBKDF2 hashing
     */
    private fun pbkdf2Hash(password: String, salt: ByteArray, iterations: Int, keyLength: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLength * 8)
        try {
            val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
            return factory.generateSecret(spec).encoded
        } finally {
            // Clear the password from memory to prevent exposure in memory dumps
            spec.clearPassword()
        }
    }
}
