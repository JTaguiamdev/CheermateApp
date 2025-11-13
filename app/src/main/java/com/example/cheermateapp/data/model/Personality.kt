package com.example.cheermateapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.cheermateapp.data.model.Personality

@Entity(
    tableName = "Personality",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["User_ID"],
            childColumns = ["User_ID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PersonalityType::class,
            parentColumns = ["Type_ID"],
            childColumns = ["PersonalityType_ID"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("User_ID"), Index("PersonalityType_ID")]
)
data class Personality(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Personality_ID")
    val Personality_ID: Int = 0,

    @ColumnInfo(name = "User_ID")
    val User_ID: Int,  // FIXED: Changed from String to Int to match User.User_ID

    @ColumnInfo(name = "PersonalityType_ID")
    val PersonalityType_ID: Int,

    @ColumnInfo(name = "Name")
    val Name: String,

    @ColumnInfo(name = "Description")
    val Description: String? = null
)
