package com.cheermateapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cheermateapp.data.model.SecurityQuestion

@Entity(
    tableName = "UserSecurityAnswer",
    foreignKeys = [
        ForeignKey(
            entity = SecurityQuestion::class,
            parentColumns = ["Question_ID"],
            childColumns = ["Question_ID"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("User_ID"),
        Index("Question_ID")
    ]
)
data class UserSecurityAnswer(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Answer_ID")
    val Answer_ID: Int = 0,

    @ColumnInfo(name = "User_ID")
    val User_ID: Int,

    @ColumnInfo(name = "Question_ID")
    val Question_ID: Int,

    @ColumnInfo(name = "AnswerHash")
    val AnswerHash: String
)
