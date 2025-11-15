package com.example.cheermateapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Enum for reminder time options
 */
enum class ReminderType {
    TEN_MINUTES_BEFORE,
    THIRTY_MINUTES_BEFORE,
    AT_SPECIFIC_TIME
}

@Entity(
    tableName = "TaskReminder",
    primaryKeys = ["TaskReminder_ID", "Task_ID", "User_ID"],
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["Task_ID", "User_ID"],
            childColumns = ["Task_ID", "User_ID"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("Task_ID"),
        Index("User_ID"),
        Index(value = ["Task_ID", "User_ID"])
    ]
)
data class TaskReminder(
    @ColumnInfo(name = "TaskReminder_ID")
    val TaskReminder_ID: Int = 0,
    
    @ColumnInfo(name = "Task_ID")
    val Task_ID: Int,
    
    @ColumnInfo(name = "User_ID")
    val User_ID: Int,
    
    @ColumnInfo(name = "RemindAt")
    val RemindAt: Long,
    
    @ColumnInfo(name = "ReminderType")
    val ReminderType: ReminderType? = null,
    
    @ColumnInfo(name = "IsActive")
    val IsActive: Boolean = true,
    
    @ColumnInfo(name = "CreatedAt")
    val CreatedAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "UpdatedAt")
    val UpdatedAt: Long = System.currentTimeMillis()
)
