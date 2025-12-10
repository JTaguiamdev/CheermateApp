package com.cheermateapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "SubTask",
    primaryKeys = ["Task_ID", "User_ID", "SubTask_ID"],
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
        Index("User_ID")
    ]
)
data class SubTask(
    val SubTask_ID: Int = 0,
    val Task_ID: Int,
    val User_ID: Int,
    val Name: String,
    val IsCompleted: Boolean = false,
    val CreatedAt: String = TimestampUtil.getCurrentTimestamp(),
    val UpdatedAt: String = TimestampUtil.getCurrentTimestamp()
)
