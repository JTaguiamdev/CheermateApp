package com.cheermateapp.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimestampUtil {
    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun getCurrentTimestamp(): String {
        return format.format(Date())
    }

    fun parseTimestamp(timestamp: String): Date? {
        return try {
            format.parse(timestamp)
        } catch (e: Exception) {
            null
        }
    }
}
