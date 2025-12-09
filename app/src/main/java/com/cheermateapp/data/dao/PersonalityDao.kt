package com.cheermateapp.data.dao

import androidx.room.*
import com.cheermateapp.data.model.Personality

@Dao
interface PersonalityDao {

    // ✅ BASIC CRUD OPERATIONS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(personality: Personality): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(personalities: List<Personality>)

    @Update
    suspend fun update(personality: Personality)

    @Delete
    suspend fun delete(personality: Personality)

    @Upsert
    suspend fun upsert(personality: Personality)

    @Query("SELECT * FROM Personality ORDER BY Personality_ID")
    suspend fun getAll(): List<Personality>

    @Query("SELECT * FROM Personality WHERE Personality_ID = :personalityId")
    suspend fun getById(personalityId: Int): Personality?

    @Query("SELECT * FROM Personality WHERE Name = :name")
    suspend fun getByName(name: String): Personality?

    @Query("SELECT COUNT(*) FROM Personality")
    suspend fun getCount(): Int

    // ✅ USER-BASED QUERY - Get personality for a user via User.Personality_ID
    @Query("""
        SELECT p.* FROM Personality p 
        INNER JOIN User u ON p.Personality_ID = u.Personality_ID 
        WHERE u.User_ID = :userId
    """)
    suspend fun getByUser(userId: Int): Personality?
}
