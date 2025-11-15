package com.cheermateapp.data.dao

import androidx.room.*
import com.cheermateapp.data.model.MessageTemplate

@Dao
interface MessageTemplateDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(messageTemplate: MessageTemplate): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messageTemplates: List<MessageTemplate>)
    
    @Update
    suspend fun update(messageTemplate: MessageTemplate)
    
    @Delete
    suspend fun delete(messageTemplate: MessageTemplate)
    
    @Query("SELECT * FROM MessageTemplate ORDER BY Template_ID")
    suspend fun getAll(): List<MessageTemplate>
    
    @Query("SELECT * FROM MessageTemplate WHERE Template_ID = :templateId")
    suspend fun getById(templateId: Int): MessageTemplate?
    
    @Query("SELECT * FROM MessageTemplate WHERE Personality_ID = :personalityId")
    suspend fun getByPersonalityId(personalityId: Int): List<MessageTemplate>
    
    @Query("SELECT * FROM MessageTemplate WHERE Personality_ID = :personalityId AND Category = :category")
    suspend fun getByPersonalityAndCategory(personalityId: Int, category: String): List<MessageTemplate>
    
    @Query("SELECT * FROM MessageTemplate WHERE Category = :category")
    suspend fun getByCategory(category: String): List<MessageTemplate>
    
    @Query("SELECT COUNT(*) FROM MessageTemplate")
    suspend fun getCount(): Int
    
    @Query("DELETE FROM MessageTemplate WHERE Personality_ID = :personalityId")
    suspend fun deleteByPersonalityId(personalityId: Int)
}
