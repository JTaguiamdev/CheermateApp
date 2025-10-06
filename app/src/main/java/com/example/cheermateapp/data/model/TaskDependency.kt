package com.example.cheermateapp.data.model

import androidx.room.*

/**
 * TaskDependency model for managing task dependencies (prerequisite tasks)
 * Represents a relationship where one task depends on another task's completion
 */
@Entity(
    tableName = "TaskDependency",
    primaryKeys = ["Task_ID", "User_ID", "DependsOn_Task_ID"],
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["Task_ID", "User_ID"],
            childColumns = ["Task_ID", "User_ID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Task::class,
            parentColumns = ["Task_ID", "User_ID"],
            childColumns = ["DependsOn_Task_ID", "DependsOn_User_ID"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("Task_ID", "User_ID"),
        Index("DependsOn_Task_ID", "DependsOn_User_ID")
    ]
)
data class TaskDependency(
    @ColumnInfo(name = "Task_ID")
    val Task_ID: Int,

    @ColumnInfo(name = "User_ID")
    val User_ID: Int,

    @ColumnInfo(name = "DependsOn_Task_ID")
    val DependsOn_Task_ID: Int,

    @ColumnInfo(name = "DependsOn_User_ID")
    val DependsOn_User_ID: Int,

    @ColumnInfo(name = "CreatedAt")
    val CreatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun create(
            taskId: Int,
            userId: Int,
            dependsOnTaskId: Int,
            dependsOnUserId: Int
        ) = TaskDependency(
            Task_ID = taskId,
            User_ID = userId,
            DependsOn_Task_ID = dependsOnTaskId,
            DependsOn_User_ID = dependsOnUserId
        )
    }
}
