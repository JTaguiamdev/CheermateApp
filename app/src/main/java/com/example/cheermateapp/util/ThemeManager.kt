package com.example.cheermateapp.util

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

/**
 * Utility class for managing app theme/dark mode
 */
object ThemeManager {
    private const val PREFS_NAME = "cheermate_theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"
    
    // Theme mode constants
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    const val THEME_SYSTEM = "system"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Get current theme mode
     */
    fun getThemeMode(context: Context): String {
        return getPreferences(context).getString(KEY_THEME_MODE, THEME_SYSTEM) ?: THEME_SYSTEM
    }

    /**
     * Set theme mode and apply it
     */
    fun setThemeMode(context: Context, mode: String) {
        getPreferences(context).edit().putString(KEY_THEME_MODE, mode).apply()
        applyTheme(mode)
    }

    /**
     * Apply theme based on mode
     */
    fun applyTheme(mode: String) {
        when (mode) {
            THEME_LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            THEME_DARK -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            THEME_SYSTEM -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    /**
     * Initialize theme on app start
     */
    fun initializeTheme(context: Context) {
        val savedMode = getThemeMode(context)
        applyTheme(savedMode)
    }

    /**
     * Check if dark mode is currently active
     */
    fun isDarkModeActive(context: Context): Boolean {
        val mode = getThemeMode(context)
        return when (mode) {
            THEME_DARK -> true
            THEME_LIGHT -> false
            THEME_SYSTEM -> {
                val nightMode = context.resources.configuration.uiMode and 
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK
                nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
            else -> false
        }
    }

    /**
     * Toggle between light and dark mode
     */
    fun toggleDarkMode(context: Context) {
        val currentMode = getThemeMode(context)
        val newMode = if (currentMode == THEME_DARK) THEME_LIGHT else THEME_DARK
        setThemeMode(context, newMode)
    }
}
