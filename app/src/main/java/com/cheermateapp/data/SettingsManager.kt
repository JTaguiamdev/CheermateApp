package com.cheermateapp.data

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {

    private const val PREFERENCES_FILE_KEY = "com.cheermateapp.settings"
    private const val KEY_DARK_MODE = "dark_mode"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFERENCES_FILE_KEY, Context.MODE_PRIVATE)
    }

    fun isDarkMode(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkMode(context: Context, isDarkMode: Boolean) {
        with(getPreferences(context).edit()) {
            putBoolean(KEY_DARK_MODE, isDarkMode)
            apply()
        }
    }

    fun isNotificationsEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun setNotificationsEnabled(context: Context, isEnabled: Boolean) {
        with(getPreferences(context).edit()) {
            putBoolean(KEY_NOTIFICATIONS_ENABLED, isEnabled)
            apply()
        }
    }
}
