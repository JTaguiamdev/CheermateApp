package com.example.cheermateapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a personality type with associated messages and characteristics.
 * This entity stores the personality types (1-5) that users can choose from.
 * Each personality has unique motivation messages for different contexts.
 */
@Entity(tableName = "Personality")
data class Personality(
    @PrimaryKey
    @ColumnInfo(name = "Personality_ID")
    val Personality_ID: Int, // Values: 1=Kalog, 2=Gen Z, 3=Softy, 4=Grey, 5=Flirty

    @ColumnInfo(name = "Name")
    val Name: String,

    @ColumnInfo(name = "Description")
    val Description: String,

    @ColumnInfo(name = "MotivationMessage")
    val MotivationMessage: String? = null,

    @ColumnInfo(name = "IsActive")
    val IsActive: Boolean = true,

    @ColumnInfo(name = "CreatedAt")
    val CreatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "UpdatedAt")
    val UpdatedAt: Long = System.currentTimeMillis()
)
