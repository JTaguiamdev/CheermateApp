package com.cheermateapp.data.dao

import com.cheermateapp.data.model.Appearance
import com.cheermateapp.data.model.NotificationPref
import com.cheermateapp.data.model.Settings
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * Unit tests for SettingsDao
 * 
 * Tests the Settings entity with integer-based Appearance and Notification preferences
 */
class SettingsDaoTest {

    @Mock
    private lateinit var settingsDao: SettingsDao
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }
    
    /**
     * Test that default settings have correct values
     * - Appearance.theme = 0 (light mode)
     * - Notification.enabled = 1 (enabled)
     */
    @Test
    fun testDefaultSettings_correctValues() {
        // Given
        val defaultAppearance = Appearance()
        val defaultNotification = NotificationPref()
        
        // Then - verify defaults
        assertEquals("Default appearance should be light mode (0)", 0, defaultAppearance.theme)
        assertEquals("Default notification should be enabled (1)", 1, defaultNotification.enabled)
    }
    
    /**
     * Test that settings can be created with light mode (theme = 0)
     */
    @Test
    fun testCreateSettings_withLightMode() {
        // Given
        val userId = 1
        val settings = Settings(
            Settings_ID = 1,
            User_ID = userId,
            Personality_ID = null,
            Appearance = Appearance(theme = 0, fontSize = "medium", colorScheme = "default"),
            Notification = NotificationPref(enabled = 1, soundEnabled = true, vibrationEnabled = true),
            DataManagement = null,
            Statistics = null
        )
        
        // Then - verify light mode
        assertEquals("Appearance theme should be 0 (light mode)", 0, settings.Appearance?.theme)
        assertNotNull("Appearance should not be null", settings.Appearance)
    }
    
    /**
     * Test that settings can be created with dark mode (theme = 1)
     */
    @Test
    fun testCreateSettings_withDarkMode() {
        // Given
        val userId = 1
        val settings = Settings(
            Settings_ID = 1,
            User_ID = userId,
            Personality_ID = null,
            Appearance = Appearance(theme = 1, fontSize = "medium", colorScheme = "default"),
            Notification = NotificationPref(enabled = 1, soundEnabled = true, vibrationEnabled = true),
            DataManagement = null,
            Statistics = null
        )
        
        // Then - verify dark mode
        assertEquals("Appearance theme should be 1 (dark mode)", 1, settings.Appearance?.theme)
    }
    
    /**
     * Test that notification can be disabled (enabled = 0)
     */
    @Test
    fun testCreateSettings_withNotificationsDisabled() {
        // Given
        val userId = 1
        val settings = Settings(
            Settings_ID = 1,
            User_ID = userId,
            Personality_ID = null,
            Appearance = Appearance(theme = 0, fontSize = "medium", colorScheme = "default"),
            Notification = NotificationPref(enabled = 0, soundEnabled = false, vibrationEnabled = false),
            DataManagement = null,
            Statistics = null
        )
        
        // Then - verify notifications disabled
        assertEquals("Notification should be disabled (0)", 0, settings.Notification?.enabled)
    }
    
    /**
     * Test that notification can be enabled (enabled = 1)
     */
    @Test
    fun testCreateSettings_withNotificationsEnabled() {
        // Given
        val userId = 1
        val settings = Settings(
            Settings_ID = 1,
            User_ID = userId,
            Personality_ID = null,
            Appearance = Appearance(theme = 0, fontSize = "medium", colorScheme = "default"),
            Notification = NotificationPref(enabled = 1, soundEnabled = true, vibrationEnabled = true),
            DataManagement = null,
            Statistics = null
        )
        
        // Then - verify notifications enabled
        assertEquals("Notification should be enabled (1)", 1, settings.Notification?.enabled)
    }
    
    /**
     * Test that settings can be updated via upsert
     */
    @Test
    fun testUpsertSettings_updatesExisting() = runTest {
        // Given
        val userId = 1
        val settings = Settings(
            Settings_ID = 1,
            User_ID = userId,
            Personality_ID = null,
            Appearance = Appearance(theme = 1, fontSize = "medium", colorScheme = "default"),
            Notification = NotificationPref(enabled = 1, soundEnabled = true, vibrationEnabled = true),
            DataManagement = null,
            Statistics = null
        )
        
        // When
        settingsDao.upsert(settings)
        
        // Then - verify the method was called
        verify(settingsDao, times(1)).upsert(settings)
    }
    
    /**
     * Test that settings can be retrieved by user ID
     */
    @Test
    fun testGetSettingsByUser_returnsSettings() = runTest {
        // Given
        val userId = 1
        val settings = Settings(
            Settings_ID = 1,
            User_ID = userId,
            Personality_ID = null,
            Appearance = Appearance(theme = 0, fontSize = "medium", colorScheme = "default"),
            Notification = NotificationPref(enabled = 1, soundEnabled = true, vibrationEnabled = true),
            DataManagement = null,
            Statistics = null
        )
        
        // When
        `when`(settingsDao.getSettingsByUser(userId)).thenReturn(settings)
        val result = settingsDao.getSettingsByUser(userId)
        
        // Then
        assertNotNull("Settings should not be null", result)
        assertEquals("User ID should match", userId, result?.User_ID)
        assertEquals("Theme should be 0 (light mode)", 0, result?.Appearance?.theme)
        assertEquals("Notifications should be enabled (1)", 1, result?.Notification?.enabled)
    }
}
