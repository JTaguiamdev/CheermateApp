package com.example.cheermateapp.data.dao

import androidx.room.*
import com.example.cheermateapp.data.model.User

@Dao
interface UserDao {

    // ✅ BASIC CRUD OPERATIONS
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    // ✅ QUERY METHODS (MERGED FROM BOTH VERSIONS)
    @Query("SELECT * FROM `User` WHERE User_ID = :id")
    suspend fun getById(id: Int): User?

    @Query("SELECT * FROM `User` WHERE User_ID = :userId LIMIT 1")
    suspend fun findById(userId: Int): User?

    @Query("SELECT * FROM `User` WHERE Username = :username LIMIT 1")
    suspend fun findByUsername(username: String): User?

    @Query("SELECT * FROM User WHERE Email = :email")
    suspend fun findByEmail(email: String): User?

    // ✅ AUTHENTICATION METHODS
    @Query("SELECT * FROM `User` WHERE Username = :username AND PasswordHash = :passwordHash LIMIT 1")
    suspend fun login(username: String, passwordHash: String): User?

    @Query("SELECT * FROM `User` WHERE Username = :username AND PasswordHash = :password LIMIT 1")
    suspend fun findByCredentials(username: String, password: String): User?

    // ✅ ADDITIONAL METHODS FOR SETTINGS AND NOTIFICATIONS
    @Query("SELECT * FROM User")
    suspend fun getAllUsers(): List<User>

    @Query("UPDATE User SET Personality_ID = :personalityId WHERE User_ID = :userId")
    suspend fun updatePersonality(userId: Int, personalityId: Int)

    @Query("DELETE FROM User WHERE User_ID = :userId")
    suspend fun delete(userId: Int)

    @Query("SELECT COUNT(*) FROM User")
    suspend fun getUserCount(): Int
}
