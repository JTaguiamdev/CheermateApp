package com.example.cheermateapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Stores message templates for each personality type and category.
 * Personality_ID references the Personality entity (1-5).
 * Category can be: "motivation", "task_work", "task_personal", "task_shopping", "task_others", etc.
 */
@Entity(
    tableName = "MessageTemplate",
    foreignKeys = [
        ForeignKey(
            entity = Personality::class,
            parentColumns = ["Personality_ID"],
            childColumns = ["Personality_ID"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("Personality_ID"), Index("Category")]
)
data class MessageTemplate(
    @PrimaryKey(autoGenerate = true)
    val Template_ID: Int = 0,
    val Personality_ID: Int, // 1=Kalog, 2=Gen Z, 3=Softy, 4=Grey, 5=Flirty
    val Category: String, // "motivation", "task_work", "task_personal", "task_shopping", "task_others"
    val TextTemplate: String
)
