package com.cheermateapp.util

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.junit.Assert.*

/**
 * Unit tests for ThemeManager
 * Tests theme mode storage, retrieval, and application logic
 */
@RunWith(MockitoJUnitRunner::class)
class ThemeManagerTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    @Before
    fun setup() {
        // Setup SharedPreferences mock chain
        `when`(mockContext.getSharedPreferences(anyString(), anyInt()))
            .thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
    }

    @Test
    fun getThemeMode_defaultsToSystem() {
        // Given: No saved preference
        `when`(mockSharedPreferences.getString(anyString(), anyString()))
            .thenReturn(ThemeManager.THEME_SYSTEM)

        // When: Getting theme mode
        val result = ThemeManager.getThemeMode(mockContext)

        // Then: Should return system default
        assertEquals(ThemeManager.THEME_SYSTEM, result)
    }

    @Test
    fun getThemeMode_returnsSavedLightMode() {
        // Given: Light mode saved
        `when`(mockSharedPreferences.getString(anyString(), anyString()))
            .thenReturn(ThemeManager.THEME_LIGHT)

        // When: Getting theme mode
        val result = ThemeManager.getThemeMode(mockContext)

        // Then: Should return light mode
        assertEquals(ThemeManager.THEME_LIGHT, result)
    }

    @Test
    fun getThemeMode_returnsSavedDarkMode() {
        // Given: Dark mode saved
        `when`(mockSharedPreferences.getString(anyString(), anyString()))
            .thenReturn(ThemeManager.THEME_DARK)

        // When: Getting theme mode
        val result = ThemeManager.getThemeMode(mockContext)

        // Then: Should return dark mode
        assertEquals(ThemeManager.THEME_DARK, result)
    }

    @Test
    fun setThemeMode_savesLightMode() {
        // When: Setting light mode
        ThemeManager.setThemeMode(mockContext, ThemeManager.THEME_LIGHT)

        // Then: Should save to preferences
        verify(mockEditor).putString(anyString(), eq(ThemeManager.THEME_LIGHT))
        verify(mockEditor).apply()
    }

    @Test
    fun setThemeMode_savesDarkMode() {
        // When: Setting dark mode
        ThemeManager.setThemeMode(mockContext, ThemeManager.THEME_DARK)

        // Then: Should save to preferences
        verify(mockEditor).putString(anyString(), eq(ThemeManager.THEME_DARK))
        verify(mockEditor).apply()
    }

    @Test
    fun setThemeMode_savesSystemMode() {
        // When: Setting system mode
        ThemeManager.setThemeMode(mockContext, ThemeManager.THEME_SYSTEM)

        // Then: Should save to preferences
        verify(mockEditor).putString(anyString(), eq(ThemeManager.THEME_SYSTEM))
        verify(mockEditor).apply()
    }

    @Test
    fun themeConstants_haveCorrectValues() {
        // Verify theme constant values
        assertEquals("light", ThemeManager.THEME_LIGHT)
        assertEquals("dark", ThemeManager.THEME_DARK)
        assertEquals("system", ThemeManager.THEME_SYSTEM)
    }

    @Test
    fun toggleDarkMode_switchesFromDarkToLight() {
        // Given: Current mode is dark
        `when`(mockSharedPreferences.getString(anyString(), anyString()))
            .thenReturn(ThemeManager.THEME_DARK)

        // When: Toggling dark mode
        ThemeManager.toggleDarkMode(mockContext)

        // Then: Should switch to light mode
        verify(mockEditor).putString(anyString(), eq(ThemeManager.THEME_LIGHT))
    }

    @Test
    fun toggleDarkMode_switchesFromLightToDark() {
        // Given: Current mode is light
        `when`(mockSharedPreferences.getString(anyString(), anyString()))
            .thenReturn(ThemeManager.THEME_LIGHT)

        // When: Toggling dark mode
        ThemeManager.toggleDarkMode(mockContext)

        // Then: Should switch to dark mode
        verify(mockEditor).putString(anyString(), eq(ThemeManager.THEME_DARK))
    }

    @Test
    fun toggleDarkMode_switchesFromSystemToDark() {
        // Given: Current mode is system
        `when`(mockSharedPreferences.getString(anyString(), anyString()))
            .thenReturn(ThemeManager.THEME_SYSTEM)

        // When: Toggling dark mode
        ThemeManager.toggleDarkMode(mockContext)

        // Then: Should switch to dark mode
        verify(mockEditor).putString(anyString(), eq(ThemeManager.THEME_DARK))
    }
}
