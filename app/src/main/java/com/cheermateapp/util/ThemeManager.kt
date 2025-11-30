package com.cheermateapp.util

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

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Get current theme mode
     */
        fun getThemeMode(context: Context): String {
            return getPreferences(context).getString(KEY_THEME_MODE, THEME_LIGHT) ?: THEME_LIGHT
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
                }
            }
        
            /**
             * Set theme mode and apply it
             */
            fun setThemeMode(context: Context, mode: String) {
                getPreferences(context).edit().putString(KEY_THEME_MODE, mode).apply()
                applyTheme(mode)
            }
        
                /**
                 * Initialize theme on app start
                 */
                fun initializeTheme(context: Context) {
                    // Always start in light mode, ignoring saved preference, to ensure toggle is off
                    applyTheme(THEME_LIGHT)
                }        /**
         * Check if dark mode is currently active
         */
        fun isDarkModeActive(context: Context): Boolean {
            val mode = getThemeMode(context)
            return when (mode) {
                THEME_DARK -> true
                THEME_LIGHT -> false
                else -> false // Should not happen with current setup, but for safety
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
