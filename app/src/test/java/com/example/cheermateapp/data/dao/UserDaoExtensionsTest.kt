package com.example.cheermateapp.data.dao

import com.example.cheermateapp.data.model.User
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for UserDao Personality_ID validation
 */
class UserDaoExtensionsTest {

    // Mock UserDao for testing
    private class MockUserDao : UserDao {
        var lastInsertedUser: User? = null
        var lastUpdatedUser: User? = null
        var lastUpdatePersonalityCall: Pair<Int, Int>? = null

        override suspend fun insert(user: User): Long {
            lastInsertedUser = user
            return 1L
        }

        override suspend fun update(user: User) {
            lastUpdatedUser = user
        }

        override suspend fun delete(user: User) {}
        override suspend fun deleteById(userId: Int): Int = 0
        override suspend fun getById(userId: Int): User? = null
        override suspend fun findByUsername(username: String): User? = null
        override suspend fun findByEmail(email: String): User? = null
        override suspend fun login(username: String, passwordHash: String): User? = null
        override suspend fun getAllUsers(): List<User> = emptyList()
        
        override suspend fun updatePersonality(userId: Int, personalityId: Int) {
            lastUpdatePersonalityCall = Pair(userId, personalityId)
        }
        
        override suspend fun updateUsername(userId: Int, username: String) {}
        override suspend fun getUserCount(): Int = 0
    }

    @Test
    fun `insertValidated should accept valid Personality_ID values 1-5`() = runBlocking {
        val dao = MockUserDao()
        
        for (personalityId in 1..5) {
            val user = User(
                User_ID = 0,
                Username = "test$personalityId",
                Email = "test$personalityId@example.com",
                PasswordHash = "hash",
                Personality_ID = personalityId
            )
            
            // Should not throw exception
            dao.insertValidated(user)
            assertEquals(user, dao.lastInsertedUser)
        }
    }

    @Test
    fun `insertValidated should accept null Personality_ID`() = runBlocking {
        val dao = MockUserDao()
        val user = User(
            User_ID = 0,
            Username = "test",
            Email = "test@example.com",
            PasswordHash = "hash",
            Personality_ID = null
        )
        
        // Should not throw exception
        dao.insertValidated(user)
        assertEquals(user, dao.lastInsertedUser)
    }

    @Test
    fun `insertValidated should reject Personality_ID less than 1`() = runBlocking {
        val dao = MockUserDao()
        val user = User(
            User_ID = 0,
            Username = "test",
            Email = "test@example.com",
            PasswordHash = "hash",
            Personality_ID = 0
        )
        
        try {
            dao.insertValidated(user)
            fail("Expected IllegalArgumentException for Personality_ID = 0")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Personality_ID must be between 1 and 5") == true)
        }
    }

    @Test
    fun `insertValidated should reject Personality_ID greater than 5`() = runBlocking {
        val dao = MockUserDao()
        
        for (invalidId in listOf(6, 13, 16, 100)) {
            val user = User(
                User_ID = 0,
                Username = "test$invalidId",
                Email = "test$invalidId@example.com",
                PasswordHash = "hash",
                Personality_ID = invalidId
            )
            
            try {
                dao.insertValidated(user)
                fail("Expected IllegalArgumentException for Personality_ID = $invalidId")
            } catch (e: IllegalArgumentException) {
                assertTrue(e.message?.contains("Personality_ID must be between 1 and 5") == true)
            }
        }
    }

    @Test
    fun `updateValidated should accept valid Personality_ID values 1-5`() = runBlocking {
        val dao = MockUserDao()
        
        for (personalityId in 1..5) {
            val user = User(
                User_ID = 1,
                Username = "test",
                Email = "test@example.com",
                PasswordHash = "hash",
                Personality_ID = personalityId
            )
            
            // Should not throw exception
            dao.updateValidated(user)
            assertEquals(user, dao.lastUpdatedUser)
        }
    }

    @Test
    fun `updateValidated should reject invalid Personality_ID values`() = runBlocking {
        val dao = MockUserDao()
        
        for (invalidId in listOf(0, 6, 13, 16)) {
            val user = User(
                User_ID = 1,
                Username = "test",
                Email = "test@example.com",
                PasswordHash = "hash",
                Personality_ID = invalidId
            )
            
            try {
                dao.updateValidated(user)
                fail("Expected IllegalArgumentException for Personality_ID = $invalidId")
            } catch (e: IllegalArgumentException) {
                assertTrue(e.message?.contains("Personality_ID must be between 1 and 5") == true)
            }
        }
    }

    @Test
    fun `updatePersonalityValidated should accept values 1-5`() = runBlocking {
        val dao = MockUserDao()
        
        for (personalityId in 1..5) {
            // Should not throw exception
            dao.updatePersonalityValidated(1, personalityId)
            assertEquals(Pair(1, personalityId), dao.lastUpdatePersonalityCall)
        }
    }

    @Test
    fun `updatePersonalityValidated should reject values outside 1-5 range`() = runBlocking {
        val dao = MockUserDao()
        
        for (invalidId in listOf(0, 6, 13, 16, -1, 100)) {
            try {
                dao.updatePersonalityValidated(1, invalidId)
                fail("Expected IllegalArgumentException for personalityId = $invalidId")
            } catch (e: IllegalArgumentException) {
                assertTrue(e.message?.contains("Personality_ID must be between 1 and 5") == true)
            }
        }
    }
}
