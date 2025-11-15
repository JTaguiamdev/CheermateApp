package com.cheermateapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SecurityQuestion")
data class SecurityQuestion(
    @PrimaryKey(autoGenerate = true)
    val SecurityQuestion_ID: Int = 0,
    val Prompt: String,
    
    @ColumnInfo(name = "IsActive")
    val IsActive: Boolean = true,
    
    @ColumnInfo(name = "CreatedAt")
    val CreatedAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "UpdatedAt")
    val UpdatedAt: Long = System.currentTimeMillis()
)
