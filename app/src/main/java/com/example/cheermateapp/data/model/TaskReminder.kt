package com.example.cheermateapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

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
    val TaskReminder_ID: Int = 0,
    val Task_ID: Int,
    val User_ID: Int,
    val RemindAt: Long,
    val IsActive: Boolean = true,
    val CreatedAt: Long = System.currentTimeMillis(),
    val UpdatedAt: Long = System.currentTimeMillis()
)
