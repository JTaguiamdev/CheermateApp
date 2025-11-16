package com.cheermateapp.data.model

/**
 * Notification preferences
 * @param enabled: Master notification toggle (1 = on, 0 = off). Default: 1 (enabled)
 * @param soundEnabled: Sound for notifications
 * @param vibrationEnabled: Vibration for notifications
 */
data class NotificationPref(
    val enabled: Int = 1,  // 1 = enabled (default), 0 = disabled
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)
