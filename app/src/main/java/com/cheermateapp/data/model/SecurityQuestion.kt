package com.cheermateapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SecurityQuestion")
data class SecurityQuestion(
    @PrimaryKey(autoGenerate = true)
    val Question_ID: Int = 0,
    val Prompt: String
)
