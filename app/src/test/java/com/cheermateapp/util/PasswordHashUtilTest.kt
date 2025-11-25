package com.cheermateapp.util

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for PasswordHashUtil
 * Note: These tests use PBKDF2-HMAC-SHA256 for password hashing
 */
class PasswordHashUtilTest {

    @Test
    fun `test password hashing creates different hashes for same password`() {
        val password = "testPassword123"
        val hash1 = PasswordHashUtil.hashPassword(password)
        val hash2 = PasswordHashUtil.hashPassword(password)
        
        // Hashes should be different due to salt
        assertNotEquals(hash1, hash2)
        
        // But both should verify correctly
        assertTrue(PasswordHashUtil.verifyPassword(password, hash1))
        assertTrue(PasswordHashUtil.verifyPassword(password, hash2))
    }

    @Test
    fun `test password verification with correct password`() {
        val password = "correctPassword"
        val hash = PasswordHashUtil.hashPassword(password)
        
        assertTrue(PasswordHashUtil.verifyPassword(password, hash))
    }

    @Test
    fun `test password verification with incorrect password`() {
        val correctPassword = "correctPassword"
        val incorrectPassword = "wrongPassword"
        val hash = PasswordHashUtil.hashPassword(correctPassword)
        
        assertFalse(PasswordHashUtil.verifyPassword(incorrectPassword, hash))
    }

    @Test
    fun `test password verification is case sensitive`() {
        val password = "Password123"
        val hash = PasswordHashUtil.hashPassword(password)
        
        assertFalse(PasswordHashUtil.verifyPassword("password123", hash))
        assertFalse(PasswordHashUtil.verifyPassword("PASSWORD123", hash))
    }

    @Test
    fun `test hash is not empty`() {
        val password = "testPassword"
        val hash = PasswordHashUtil.hashPassword(password)
        
        assertTrue(hash.isNotEmpty())
        assertTrue(hash.length > 20) // PBKDF2 hashes in format iterations:salt:hash are typically longer
    }

    @Test
    fun `test empty password can be hashed`() {
        val password = ""
        val hash = PasswordHashUtil.hashPassword(password)
        
        assertTrue(hash.isNotEmpty())
        assertTrue(PasswordHashUtil.verifyPassword(password, hash))
    }

    @Test
    fun `test special characters in password`() {
        val password = "P@ssw0rd!#\$%^&*()"
        val hash = PasswordHashUtil.hashPassword(password)
        
        assertTrue(PasswordHashUtil.verifyPassword(password, hash))
    }

    @Test
    fun `test long password`() {
        val password = "a".repeat(100)
        val hash = PasswordHashUtil.hashPassword(password)
        
        assertTrue(PasswordHashUtil.verifyPassword(password, hash))
    }

    @Test
    fun `test verification with invalid hash returns false`() {
        val password = "testPassword"
        val invalidHash = "not_a_valid_pbkdf2_hash"
        
        assertFalse(PasswordHashUtil.verifyPassword(password, invalidHash))
    }
}
