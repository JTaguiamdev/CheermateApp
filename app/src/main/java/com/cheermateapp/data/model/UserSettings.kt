package com.cheermateapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "UserSettings",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["User_ID"],
            childColumns = ["User_ID"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("User_ID")
    ]
)
data class UserSettings(
    @PrimaryKey(autoGenerate = true)
    val UserSettings_ID: Int = 0,
    val User_ID: Int,
    val Appearance: Appearance? = null,
    val Notification: NotificationPref? = null
)
