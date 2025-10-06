package com.example.cheermateapp.util

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for InputValidationUtil
 */
class InputValidationUtilTest {

    @Test
    fun `test valid username`() {
        assertTrue(InputValidationUtil.isValidUsername("john123"))
        assertTrue(InputValidationUtil.isValidUsername("user_name"))
        assertTrue(InputValidationUtil.isValidUsername("abc"))
        assertTrue(InputValidationUtil.isValidUsername("user123_test"))
    }

    @Test
    fun `test invalid username - too short`() {
        assertFalse(InputValidationUtil.isValidUsername("ab"))
        assertFalse(InputValidationUtil.isValidUsername("a"))
    }

    @Test
    fun `test invalid username - too long`() {
        assertFalse(InputValidationUtil.isValidUsername("a".repeat(21)))
    }

    @Test
    fun `test invalid username - special characters`() {
        assertFalse(InputValidationUtil.isValidUsername("user@name"))
        assertFalse(InputValidationUtil.isValidUsername("user name"))
        assertFalse(InputValidationUtil.isValidUsername("user-name"))
        assertFalse(InputValidationUtil.isValidUsername("user.name"))
    }

    @Test
    fun `test valid email`() {
        assertTrue(InputValidationUtil.isValidEmail("user@example.com"))
        assertTrue(InputValidationUtil.isValidEmail("test.user@domain.co.uk"))
        assertTrue(InputValidationUtil.isValidEmail("user+tag@example.com"))
    }

    @Test
    fun `test invalid email`() {
        assertFalse(InputValidationUtil.isValidEmail(""))
        assertFalse(InputValidationUtil.isValidEmail("notanemail"))
        assertFalse(InputValidationUtil.isValidEmail("@example.com"))
        assertFalse(InputValidationUtil.isValidEmail("user@"))
        assertFalse(InputValidationUtil.isValidEmail("user @example.com"))
    }

    @Test
    fun `test valid password`() {
        assertTrue(InputValidationUtil.isValidPassword("password123"))
        assertTrue(InputValidationUtil.isValidPassword("abcdef"))
        assertTrue(InputValidationUtil.isValidPassword("123456"))
    }

    @Test
    fun `test invalid password - too short`() {
        assertFalse(InputValidationUtil.isValidPassword("12345"))
        assertFalse(InputValidationUtil.isValidPassword("abc"))
        assertFalse(InputValidationUtil.isValidPassword(""))
    }

    @Test
    fun `test strong password`() {
        assertTrue(InputValidationUtil.isStrongPassword("Password123"))
        assertTrue(InputValidationUtil.isStrongPassword("MyP@ssw0rd"))
        assertTrue(InputValidationUtil.isStrongPassword("SecurePass1"))
    }

    @Test
    fun `test weak password - missing requirements`() {
        assertFalse(InputValidationUtil.isStrongPassword("password")) // No uppercase, no digit
        assertFalse(InputValidationUtil.isStrongPassword("PASSWORD")) // No lowercase, no digit
        assertFalse(InputValidationUtil.isStrongPassword("Password")) // No digit
        assertFalse(InputValidationUtil.isStrongPassword("Pass123")) // Too short
    }

    @Test
    fun `test password strength scoring`() {
        assertEquals(0, InputValidationUtil.getPasswordStrength(""))
        assertEquals(1, InputValidationUtil.getPasswordStrength("password"))
        assertEquals(2, InputValidationUtil.getPasswordStrength("Password"))
        assertEquals(3, InputValidationUtil.getPasswordStrength("Password1"))
        assertEquals(4, InputValidationUtil.getPasswordStrength("Password1!"))
    }

    @Test
    fun `test SQL injection pattern detection`() {
        assertTrue(InputValidationUtil.containsSQLInjectionPattern("' OR '1'='1"))
        assertTrue(InputValidationUtil.containsSQLInjectionPattern("admin'--"))
        assertTrue(InputValidationUtil.containsSQLInjectionPattern("1 UNION SELECT * FROM users"))
        assertTrue(InputValidationUtil.containsSQLInjectionPattern("'; DROP TABLE users--"))
        assertTrue(InputValidationUtil.containsSQLInjectionPattern("/* malicious comment */"))
    }

    @Test
    fun `test safe input - no SQL injection`() {
        assertFalse(InputValidationUtil.containsSQLInjectionPattern("john_doe"))
        assertFalse(InputValidationUtil.containsSQLInjectionPattern("user123"))
        assertFalse(InputValidationUtil.containsSQLInjectionPattern("normal text input"))
    }

    @Test
    fun `test sanitize input`() {
        assertEquals("test", InputValidationUtil.sanitizeInput("test"))
        assertEquals("testuser", InputValidationUtil.sanitizeInput("test<>user"))
        assertEquals("email.com", InputValidationUtil.sanitizeInput("email%@.com"))
        assertEquals("user_name", InputValidationUtil.sanitizeInput("user_name"))
    }
}
