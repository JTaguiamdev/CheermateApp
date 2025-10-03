package com.example.cheermateapp.data.model

import androidx.room.*
import com.example.cheermateapp.data.model.Priority
import com.example.cheermateapp.data.model.Status

/**
 * TaskTemplate model for reusable task templates
 */
@Entity(
    tableName = "TaskTemplate",
    primaryKeys = ["Template_ID", "User_ID"],
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
data class TaskTemplate(
    @ColumnInfo(name = "Template_ID")
    val Template_ID: Int = 0,

    @ColumnInfo(name = "User_ID")
    val User_ID: Int,

    @ColumnInfo(name = "Name")
    val Name: String,

    @ColumnInfo(name = "Description")
    val Description: String? = null,

    @ColumnInfo(name = "Category")
    val Category: String? = null, // e.g., "Work", "Personal", "Study"

    @ColumnInfo(name = "Title")
    val Title: String,

    @ColumnInfo(name = "TaskDescription")
    val TaskDescription: String? = null,

    @ColumnInfo(name = "Priority")
    val Priority: Priority = Priority.Medium,

    @ColumnInfo(name = "EstimatedDuration")
    val EstimatedDuration: Int? = null, // Duration in minutes

    @ColumnInfo(name = "DefaultDueInDays")
    val DefaultDueInDays: Int? = null, // Default due date offset from creation

    @ColumnInfo(name = "IsShared")
    val IsShared: Boolean = false, // Future: share templates with other users

    @ColumnInfo(name = "UsageCount")
    val UsageCount: Int = 0,

    @ColumnInfo(name = "CreatedAt")
    val CreatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "UpdatedAt")
    val UpdatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun create(
            templateId: Int = 0,
            userId: Int,
            name: String,
            description: String? = null,
            category: String? = null,
            title: String,
            taskDescription: String? = null,
            priority: Priority = Priority.Medium,
            estimatedDuration: Int? = null,
            defaultDueInDays: Int? = null,
            isShared: Boolean = false,
            usageCount: Int = 0
        ) = TaskTemplate(
            Template_ID = templateId,
            User_ID = userId,
            Name = name,
            Description = description,
            Category = category,
            Title = title,
            TaskDescription = taskDescription,
            Priority = priority,
            EstimatedDuration = estimatedDuration,
            DefaultDueInDays = defaultDueInDays,
            IsShared = isShared,
            UsageCount = usageCount
        )

        /**
         * Predefined templates for common tasks
         */
        fun getDefaultTemplates(userId: Int): List<TaskTemplate> {
            return listOf(
                create(
                    userId = userId,
                    name = "Daily Standup",
                    category = "Work",
                    title = "Daily Standup Meeting",
                    taskDescription = "Attend daily standup meeting with team",
                    priority = Priority.High,
                    estimatedDuration = 15,
                    defaultDueInDays = 0
                ),
                create(
                    userId = userId,
                    name = "Code Review",
                    category = "Work",
                    title = "Review Pull Request",
                    taskDescription = "Review and provide feedback on team's pull request",
                    priority = Priority.Medium,
                    estimatedDuration = 30,
                    defaultDueInDays = 1
                ),
                create(
                    userId = userId,
                    name = "Weekly Report",
                    category = "Work",
                    title = "Prepare Weekly Report",
                    taskDescription = "Compile and submit weekly progress report",
                    priority = Priority.High,
                    estimatedDuration = 60,
                    defaultDueInDays = 7
                ),
                create(
                    userId = userId,
                    name = "Study Session",
                    category = "Study",
                    title = "Study Session",
                    taskDescription = "Dedicated study time for course materials",
                    priority = Priority.Medium,
                    estimatedDuration = 90,
                    defaultDueInDays = 0
                ),
                create(
                    userId = userId,
                    name = "Exercise",
                    category = "Personal",
                    title = "Daily Exercise",
                    taskDescription = "Complete daily exercise routine",
                    priority = Priority.Medium,
                    estimatedDuration = 30,
                    defaultDueInDays = 0
                )
            )
        }
    }

    /**
     * Create a task from this template
     */
    fun toTask(taskId: Int, dueDate: String? = null, dueTime: String? = null): Task {
        val finalDueDate = dueDate ?: run {
            if (DefaultDueInDays != null) {
                val calendar = java.util.Calendar.getInstance()
                calendar.add(java.util.Calendar.DAY_OF_YEAR, DefaultDueInDays)
                Task.dateToString(calendar.time)
            } else {
                null
            }
        }

        return Task.create(
            taskId = taskId,
            userId = User_ID,
            title = Title,
            description = TaskDescription,
            priority = Priority,
            dueAt = finalDueDate,
            dueTime = dueTime,
            status = Status.Pending
        )
    }
}
