package com.cheermateapp.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "User",
    indices = [
        Index(value = ["Username"], unique = true),
        Index(value = ["Email"], unique = true),
        Index(value = ["Personality_ID"])
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val User_ID: Int = 0,  // INT type - this is crucial
    val Username: String,
    val Email: String,
    val PasswordHash: String,
    val FirstName: String = "",
    val LastName: String = "",
    val Personality_ID: Int? = null,
    val CreatedAt: String = TimestampUtil.getCurrentTimestamp(),
    val UpdatedAt: String = TimestampUtil.getCurrentTimestamp()
)
