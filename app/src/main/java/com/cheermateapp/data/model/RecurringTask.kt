package com.cheermateapp.data.model

import androidx.room.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enum for recurring task frequency
 */
enum class RecurringFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

/**
 * RecurringTask model for managing recurring tasks
 */
@Entity(
    tableName = "RecurringTask",
    primaryKeys = ["RecurringTask_ID", "User_ID"],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["User_ID"],
            childColumns = ["User_ID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Task::class,
            parentColumns = ["Task_ID", "User_ID"],
            childColumns = ["Task_ID", "User_ID"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("User_ID"),
        Index(value = ["Task_ID", "User_ID"])
    ]
)
data class RecurringTask(
    @ColumnInfo(name = "RecurringTask_ID")
    val RecurringTask_ID: Int = 0,

    @ColumnInfo(name = "User_ID")
    val User_ID: Int,

    @ColumnInfo(name = "Task_ID")
    val Task_ID: Int? = null,

    @ColumnInfo(name = "Title")
    val Title: String,

    @ColumnInfo(name = "Description")
    val Description: String? = null,

    @ColumnInfo(name = "Priority")
    val Priority: Priority = com.cheermateapp.data.model.Priority.Medium,

    @ColumnInfo(name = "Frequency")
    val Frequency: RecurringFrequency = RecurringFrequency.DAILY,

    @ColumnInfo(name = "StartDate")
    val StartDate: String, // yyyy-MM-dd format

    @ColumnInfo(name = "EndDate")
    val EndDate: String? = null, // Optional end date

    @ColumnInfo(name = "TimeOfDay")
    val TimeOfDay: String? = null, // HH:mm format

    @ColumnInfo(name = "DayOfWeek")
    val DayOfWeek: Int? = null, // 1-7 for WEEKLY (1=Monday, 7=Sunday)

    @ColumnInfo(name = "DayOfMonth")
    val DayOfMonth: Int? = null, // 1-31 for MONTHLY

    @ColumnInfo(name = "IsActive")
    val IsActive: Boolean = true,

    @ColumnInfo(name = "LastGenerated")
    val LastGenerated: String? = null, // Last date a task was generated

    @ColumnInfo(name = "CreatedAt")
    val CreatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "UpdatedAt")
    val UpdatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun create(
            recurringTaskId: Int = 0,
            userId: Int,
            taskId: Int? = null,
            title: String,
            description: String? = null,
            priority: Priority = Priority.Medium,
            frequency: RecurringFrequency = RecurringFrequency.DAILY,
            startDate: String,
            endDate: String? = null,
            timeOfDay: String? = null,
            dayOfWeek: Int? = null,
            dayOfMonth: Int? = null,
            isActive: Boolean = true,
            lastGenerated: String? = null
        ) = RecurringTask(
            RecurringTask_ID = recurringTaskId,
            User_ID = userId,
            Task_ID = taskId,
            Title = title,
            Description = description,
            Priority = priority,
            Frequency = frequency,
            StartDate = startDate,
            EndDate = endDate,
            TimeOfDay = timeOfDay,
            DayOfWeek = dayOfWeek,
            DayOfMonth = dayOfMonth,
            IsActive = isActive,
            LastGenerated = lastGenerated
        )

        fun getCurrentDateString(): String {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return format.format(Date())
        }
    }

    /**
     * Calculate the next occurrence date for this recurring task
     */
    fun getNextOccurrence(): Date? {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDateParsed = try {
            format.parse(StartDate)
        } catch (e: Exception) {
            return null
        }

        val lastGenDate = if (LastGenerated != null) {
            try {
                format.parse(LastGenerated)
            } catch (e: Exception) {
                startDateParsed
            }
        } else {
            startDateParsed
        }

        val calendar = Calendar.getInstance()
        calendar.time = lastGenDate ?: Date()

        when (Frequency) {
            RecurringFrequency.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            RecurringFrequency.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            RecurringFrequency.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            RecurringFrequency.YEARLY -> calendar.add(Calendar.YEAR, 1)
        }

        val endDateParsed = if (EndDate != null) {
            try {
                format.parse(EndDate)
            } catch (e: Exception) {
                null
            }
        } else null

        return if (endDateParsed != null && calendar.time.after(endDateParsed)) {
            null // Past end date
        } else {
            calendar.time
        }
    }

    /**
     * Check if a task should be generated today
     */
    fun shouldGenerateToday(): Boolean {
        if (!IsActive) return false

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = format.format(Date())
        
        if (LastGenerated == today) return false // Already generated today

        val nextOccurrence = getNextOccurrence()
        if (nextOccurrence == null) return false

        val nextOccurrenceStr = format.format(nextOccurrence)
        return nextOccurrenceStr <= today
    }
}
