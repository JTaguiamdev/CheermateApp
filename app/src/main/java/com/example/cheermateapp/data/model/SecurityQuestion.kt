package com.example.cheermateapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SecurityQuestion")
data class SecurityQuestion(
    @PrimaryKey(autoGenerate = true)
    val SecurityQuestion_ID: Int = 0,
    val Prompt: String
)
