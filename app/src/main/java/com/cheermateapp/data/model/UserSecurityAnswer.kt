// Replace: app/src/main/java/com/example/cheermateapp/data/model/UserSecurityAnswer.kt
package com.cheermateapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "UserSecurityAnswer")
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
