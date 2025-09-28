package com.example.cheermateapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "MessageTemplate",
    foreignKeys = [
        ForeignKey(
            entity = Personality::class,
            parentColumns = ["Personality_ID"],
            childColumns = ["Personality_ID"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("Personality_ID")]
)
data class MessageTemplate(
    @PrimaryKey(autoGenerate = true)
    val Template_ID: Int = 0,
    val Personality_ID: Int? = null,
    val Category: String? = null,
    val TextTemplate: String? = null
)
