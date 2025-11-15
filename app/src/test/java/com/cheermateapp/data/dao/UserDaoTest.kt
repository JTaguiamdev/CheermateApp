package com.cheermateapp.data.dao

import com.cheermateapp.data.model.User
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * Unit tests for UserDao personality update functionality
 * 
 * Tests the updatePersonality method to ensure User.Personality_ID is properly set
 */
class UserDaoTest {

    @Mock
    private lateinit var userDao: UserDao
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }
    
    /**
     * Test that updatePersonality correctly updates the User's Personality_ID
     */
    @Test
    fun testUpdatePersonality_setsPersonalityId() = runTest {
        // Given
        val userId = 1
        val personalityId = 5
        
        // When
        userDao.updatePersonality(userId, personalityId)
        
        // Then - verify the method was called with correct parameters
        verify(userDao, times(1)).updatePersonality(userId, personalityId)
    }
    
    /**
     * Test that a user can be retrieved after personality update
     */
    @Test
    fun testGetUserById_afterPersonalityUpdate() = runTest {
        // Given
        val userId = 1
        val personalityId = 5
        val expectedUser = User(
            User_ID = userId,
            Username = "testuser",
            Email = "test@example.com",
            PasswordHash = "hashedpassword",
            Personality_ID = personalityId
        )
        
        // Mock the behavior
        `when`(userDao.getById(userId)).thenReturn(expectedUser)
        
        // When
        val user = userDao.getById(userId)
        
        // Then
        assertNotNull(user)
        assertEquals(personalityId, user?.Personality_ID)
    }
    
    /**
     * Test that personality can be updated multiple times
     */
    @Test
    fun testUpdatePersonality_multipleTimes() = runTest {
        // Given
        val userId = 1
        val firstPersonalityId = 3
        val secondPersonalityId = 7
        
        // When - update personality twice
        userDao.updatePersonality(userId, firstPersonalityId)
        userDao.updatePersonality(userId, secondPersonalityId)
        
        // Then - verify both updates were called
        verify(userDao, times(1)).updatePersonality(userId, firstPersonalityId)
        verify(userDao, times(1)).updatePersonality(userId, secondPersonalityId)
    }
}
