package com.cheermateapp.data.db

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.cheermateapp.data.model.Appearance
import com.cheermateapp.data.model.Priority
import com.cheermateapp.data.model.Status
import com.cheermateapp.data.model.Category  // ✅ ADD THIS IMPORT
import com.google.gson.Gson
import java.util.Date

@ProvidedTypeConverter
class AppTypeConverters(private val gson: Gson = Gson()) {

    // ✅ DATE/TIME CONVERTERS
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // ✅ PRIORITY ENUM CONVERTERS
    @TypeConverter
    fun fromTaskPriority(priority: Priority?): String? {
        return priority?.name
    }

    @TypeConverter
    fun toTaskPriority(priority: String?): Priority? {
        return try {
            priority?.let { Priority.valueOf(it) }
        } catch (e: IllegalArgumentException) {
            Priority.Medium // Default fallback
        }
    }

    // ✅ STATUS ENUM CONVERTERS (FIXED VERSION)
    @TypeConverter
    fun fromTaskStatus(status: Status?): String? {
        return status?.name
    }

    @TypeConverter
    fun toTaskStatus(status: String?): Status? {
        return try {
            status?.let { Status.valueOf(it) }
        } catch (e: IllegalArgumentException) {
            Status.Pending // Default fallback
        }
    }

    // ✅ CATEGORY ENUM CONVERTERS
    @TypeConverter
    fun fromTaskCategory(category: Category?): String? {
        return category?.name
    }

    @TypeConverter
    fun toTaskCategory(category: String?): Category? {
        return try {
            category?.let { Category.valueOf(it) }
        } catch (e: IllegalArgumentException) {
            Category.Work // Default fallback
        }
    }
}
