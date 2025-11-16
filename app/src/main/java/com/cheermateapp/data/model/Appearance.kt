// KEEP: app/src/main/java/com/example/cheermateapp/data/model/Appearance.kt
package com.cheermateapp.data.model

/**
 * Appearance settings for the app
 * @param theme: 0 = light mode, 1 = dark mode (default: 0 for light)
 * @param fontSize: Font size preference
 * @param colorScheme: Color scheme preference
 */
data class Appearance(
    val theme: Int = 0,  // 0 = light mode (default), 1 = dark mode
    val fontSize: String = "medium",
    val colorScheme: String = "default"
)
