package com.example.cheermateapp.data.model

import androidx.room.*
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

// ✅ MAKE SURE THESE ENUMS ARE AT THE TOP AND PROPERLY DEFINED
enum class Priority {
    Low,
    Medium,
    High
}

enum class Status {
    Pending,
    @SerializedName("In Progress")
    InProgress,
    Completed,
    Cancelled,
    OverDue
}

enum class Category {
    Work,
    Personal,
    Shopping,
    Others
}

@Entity(
    tableName = "Task",
    primaryKeys = ["Task_ID", "User_ID"],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["User_ID"],
            childColumns = ["User_ID"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("User_ID")]
)
data class Task(
    @ColumnInfo(name = "Task_ID")
    val Task_ID: Int = 0,

    @ColumnInfo(name = "User_ID")
    val User_ID: Int,

    @ColumnInfo(name = "Title")
    val Title: String,

    @ColumnInfo(name = "Description")
    val Description: String? = null,

    // ✅ Category field
    @ColumnInfo(name = "Category")
    val Category: com.example.cheermateapp.data.model.Category = com.example.cheermateapp.data.model.Category.Work,

    // ✅ LINE 43: This should now work with the enum defined above
    @ColumnInfo(name = "Priority")
    val Priority: com.example.cheermateapp.data.model.Priority = com.example.cheermateapp.data.model.Priority.Medium,

    @ColumnInfo(name = "DueAt")
    val DueAt: String? = null,

    @ColumnInfo(name = "DueTime")
    val DueTime: String? = null,

    @ColumnInfo(name = "Status")
    val Status: com.example.cheermateapp.data.model.Status = com.example.cheermateapp.data.model.Status.Pending,

    @ColumnInfo(name = "TaskProgress")
    val TaskProgress: Int = 0,

    @ColumnInfo(name = "CreatedAt")
    val CreatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "UpdatedAt")
    val UpdatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "DeletedAt")
    val DeletedAt: Long? = null
) {
    companion object {
        fun create(
            taskId: Int = 0,
            userId: Int,
            title: String,
            description: String? = null,
            category: com.example.cheermateapp.data.model.Category = com.example.cheermateapp.data.model.Category.Work,
            priority: com.example.cheermateapp.data.model.Priority = com.example.cheermateapp.data.model.Priority.Medium,
            dueAt: String? = null,
            dueTime: String? = null,
            status: com.example.cheermateapp.data.model.Status = com.example.cheermateapp.data.model.Status.Pending,
            taskProgress: Int = 0,
            createdAt: Long = System.currentTimeMillis(),
            updatedAt: Long = System.currentTimeMillis(),
            deletedAt: Long? = null
        ) = Task(
            Task_ID = taskId,
            User_ID = userId,
            Title = title,
            Description = description,
            Category = category,
            Priority = priority,
            DueAt = dueAt,
            DueTime = dueTime,
            Status = status,
            TaskProgress = taskProgress,
            CreatedAt = createdAt,
            UpdatedAt = updatedAt,
            DeletedAt = deletedAt
        )

        fun dateToString(date: Date): String {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return format.format(date)
        }

        fun timeToString(date: Date): String {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            return format.format(date)
        }

        fun dateToTimeString(date: Date): String {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            return format.format(date)
        }

        fun stringToDate(dateStr: String, timeStr: String?): Date {
            return try {
                val dateTimeStr = if (timeStr != null) {
                    "$dateStr $timeStr"
                } else {
                    "$dateStr 00:00:00"
                }
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                format.parse(dateTimeStr) ?: Date()
            } catch (e: Exception) {
                Date()
            }
        }
    }

    fun getDueDate(): Date? {
        return if (DueAt != null) {
            Task.stringToDate(DueAt, DueTime)
        } else null
    }

    fun isOverdue(): Boolean {
        val dueDate = getDueDate()
        return dueDate != null && dueDate.before(Date()) && Status != com.example.cheermateapp.data.model.Status.Completed
    }

    fun isToday(): Boolean {
        val dueDate = getDueDate()
        if (dueDate == null) return false

        val today = Calendar.getInstance()
        val dueCalendar = Calendar.getInstance()
        dueCalendar.time = dueDate

        return today.get(Calendar.YEAR) == dueCalendar.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == dueCalendar.get(Calendar.DAY_OF_YEAR)
    }

    fun getFormattedDueDateTime(): String? {
        return if (DueAt != null) {
            val dueDate = getDueDate()
            if (dueDate != null) {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val formattedDate = dateFormat.format(dueDate)
                val formattedTime = if (DueTime != null) {
                    " at ${timeFormat.format(dueDate)}"
                } else ""
                "$formattedDate$formattedTime"
            } else null
        } else null
    }

    fun getStatusEmoji(): String {
        return when (Status) {
            com.example.cheermateapp.data.model.Status.Completed -> "✅"
            com.example.cheermateapp.data.model.Status.Pending -> "⏳"
            com.example.cheermateapp.data.model.Status.InProgress -> "🔄"
            com.example.cheermateapp.data.model.Status.Cancelled -> "❌"
            com.example.cheermateapp.data.model.Status.OverDue -> "🔴"
            else -> "⏳" // Default fallback
        }
    }

    fun com.example.cheermateapp.data.model.Task.getPriorityColor(): Int {
        return when (Priority) {
            com.example.cheermateapp.data.model.Priority.High -> 0xFFE53E3E.toInt() // Red
            com.example.cheermateapp.data.model.Priority.Medium -> 0xFFED8936.toInt() // Orange
            com.example.cheermateapp.data.model.Priority.Low -> 0xFF38A169.toInt() // Green
            else -> 0xFFED8936.toInt() // Default to Medium Orange
        }
    }
    fun getStatusColor(): Int {
        return when (Status) {
            com.example.cheermateapp.data.model.Status.Pending -> 0xFFFFA500.toInt()  // Orange
            com.example.cheermateapp.data.model.Status.InProgress -> 0xFF0066CC.toInt() // Blue
            com.example.cheermateapp.data.model.Status.Completed -> 0xFF38A169.toInt()  // Green
            com.example.cheermateapp.data.model.Status.Cancelled -> 0xFF808080.toInt()  // Gray
            com.example.cheermateapp.data.model.Status.OverDue -> 0xFFE53E3E.toInt()    // Red
        }
    }

    fun getPriorityText(): String {
        return when (Priority) {
            com.example.cheermateapp.data.model.Priority.High -> "🔴 High"
            com.example.cheermateapp.data.model.Priority.Medium -> "🟡 Medium"
            com.example.cheermateapp.data.model.Priority.Low -> "🟢 Low"
        }
    }

    fun getStatusText(): String {
        return when (Status) {
            com.example.cheermateapp.data.model.Status.Pending -> "⏳ Pending"
            com.example.cheermateapp.data.model.Status.InProgress -> "🔄 In Progress"
            com.example.cheermateapp.data.model.Status.Completed -> "✅ Completed"
            com.example.cheermateapp.data.model.Status.Cancelled -> "❌ Cancelled"
            com.example.cheermateapp.data.model.Status.OverDue -> "🔴 Overdue"
        }
    }
}