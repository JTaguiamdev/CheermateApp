package com.cheermateapp.data.dao

import androidx.room.*
import com.cheermateapp.data.model.SecurityQuestion
import com.cheermateapp.data.model.UserSecurityAnswer

@Dao
interface SecurityDao {

    @Insert
    suspend fun addQuestion(q: SecurityQuestion): Long

    @Insert
    suspend fun insertQuestion(question: SecurityQuestion): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAnswer(a: UserSecurityAnswer)

    @Insert
    suspend fun insertAnswer(answer: UserSecurityAnswer): Long

    @Query("SELECT * FROM SecurityQuestion")
    suspend fun getAllQuestions(): List<SecurityQuestion>

    @Query("SELECT * FROM UserSecurityAnswer WHERE User_ID = :userId")
    suspend fun answers(userId: Int): List<UserSecurityAnswer>

    @Query("SELECT * FROM UserSecurityAnswer WHERE User_ID = :userId")
    suspend fun getUserAnswers(userId: Int): List<UserSecurityAnswer>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllQuestions(questions: List<SecurityQuestion>)
    
    @Query("SELECT COUNT(*) FROM SecurityQuestion")
    suspend fun getQuestionCount(): Int
    
    @Update
    suspend fun updateQuestion(question: SecurityQuestion)
    
    @Delete
    suspend fun deleteQuestion(question: SecurityQuestion)
}
