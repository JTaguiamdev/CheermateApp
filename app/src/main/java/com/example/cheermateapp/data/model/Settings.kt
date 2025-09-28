package com.example.cheermateapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "Settings",
    primaryKeys = ["User_ID", "Settings_ID"],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["User_ID"],
            childColumns = ["User_ID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Personality::class,
            parentColumns = ["Personality_ID"],
            childColumns = ["Personality_ID"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("User_ID"),
        Index("Personality_ID")
    ]
)
data class Settings(
    val Settings_ID: Int = 0,
    val User_ID: Int,
    val Personality_ID: Int? = null,
    val Appearance: Appearance? = null,
    val Notification: NotificationPref? = null,
    val DataManagement: DataManagement? = null,
    val Statistics: StatisticsPref? = null
)
