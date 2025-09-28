package com.example.cheermateapp.data.db

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.example.cheermateapp.data.model.Appearance
import com.example.cheermateapp.data.model.DataManagement
import com.example.cheermateapp.data.model.NotificationPref
import com.example.cheermateapp.data.model.StatisticsPref
import com.example.cheermateapp.data.model.Priority
import com.example.cheermateapp.data.model.Status  // ✅ ADD THIS IMPORT
import com.google.gson.Gson
import java.util.Date

@ProvidedTypeConverter
class AppTypeConverters(private val gson: Gson) {

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

    // ✅ JSON-BACKED PREFERENCES CONVERTERS (EXISTING)
    @TypeConverter
    fun appearanceToJson(v: Appearance?): String? = v?.let { gson.toJson(it) }

    @TypeConverter
    fun jsonToAppearance(s: String?): Appearance? =
        s?.let {
            try {
                gson.fromJson(it, Appearance::class.java)
            } catch (e: Exception) {
                null // Return null if JSON parsing fails
            }
        }

    @TypeConverter
    fun notificationToJson(v: NotificationPref?): String? = v?.let { gson.toJson(it) }

    @TypeConverter
    fun jsonToNotification(s: String?): NotificationPref? =
        s?.let {
            try {
                gson.fromJson(it, NotificationPref::class.java)
            } catch (e: Exception) {
                null
            }
        }

    @TypeConverter
    fun dataMgmtToJson(v: DataManagement?): String? = v?.let { gson.toJson(it) }

    @TypeConverter
    fun jsonToDataMgmt(s: String?): DataManagement? =
        s?.let {
            try {
                gson.fromJson(it, DataManagement::class.java)
            } catch (e: Exception) {
                null
            }
        }

    @TypeConverter
    fun statsToJson(v: StatisticsPref?): String? = v?.let { gson.toJson(it) }

    @TypeConverter
    fun jsonToStats(s: String?): StatisticsPref? =
        s?.let {
            try {
                gson.fromJson(it, StatisticsPref::class.java)
            } catch (e: Exception) {
                null
            }
        }
}
