package com.example.cheermateapp.data.dao

import androidx.room.*
import com.example.cheermateapp.data.model.Personality

@Dao
interface PersonalityDao {

    // ✅ BASIC CRUD OPERATIONS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(personality: Personality): Long

    @Update
    suspend fun update(personality: Personality)

    @Delete
    suspend fun delete(personality: Personality)

    @Upsert
    suspend fun upsert(personality: Personality)

    // ✅ QUERY METHODS (MERGED FROM BOTH VERSIONS)
    @Query("SELECT * FROM Personality")
    suspend fun getAll(): List<Personality>

    @Query("SELECT * FROM Personality")
    suspend fun getAllPersonalities(): List<Personality>

    @Query("SELECT * FROM Personality WHERE Personality_ID = :personalityId")
    suspend fun getById(personalityId: Int): Personality?

    @Query("SELECT * FROM Personality WHERE Name = :name")
    suspend fun getByName(name: String): Personality?

    // ✅ USER-BASED QUERIES (HANDLES BOTH APPROACHES)
    @Query("SELECT * FROM Personality WHERE User_ID = :userId LIMIT 1")
    suspend fun getByUser(userId: String): Personality?

    @Query("SELECT * FROM Personality WHERE User_ID = :userId")
    suspend fun getPersonalityByUserId(userId: String): Personality?

    // ✅ JOIN QUERY FOR PERSONALITY VIA USER TABLE
    @Query("""
        SELECT p.* FROM Personality p 
        INNER JOIN User u ON p.Personality_ID = u.Personality_ID 
        WHERE u.User_ID = :userId
    """)
    suspend fun getPersonalityByUserIdFromUser(userId: String): Personality?

    @Query("SELECT COUNT(*) FROM Personality")
    suspend fun getCount(): Int
}
