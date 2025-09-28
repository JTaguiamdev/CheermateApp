package com.example.cheermateapp.data.dao

import androidx.room.*
import com.example.cheermateapp.data.model.Settings

@Dao
interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(s: Settings)

    @Insert
    suspend fun insert(settings: Settings): Long

    @Update
    suspend fun update(settings: Settings)

    @Query("SELECT * FROM Settings WHERE User_ID = :userId ORDER BY Settings_ID DESC LIMIT 1")
    suspend fun latest(userId: Int): Settings?

    @Query("SELECT * FROM Settings WHERE User_ID = :userId LIMIT 1")
    suspend fun getSettingsByUser(userId: Int): Settings?

    @Query("SELECT * FROM Settings")
    suspend fun getAllSettings(): List<Settings>
}
