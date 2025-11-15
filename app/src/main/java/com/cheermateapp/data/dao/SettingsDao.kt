package com.cheermateapp.data.dao

import androidx.room.*
import com.cheermateapp.data.model.Settings
import kotlinx.coroutines.flow.Flow

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

    // ✅ FLOW METHODS FOR REACTIVE UPDATES
    @Query("SELECT * FROM Settings WHERE User_ID = :userId ORDER BY Settings_ID DESC LIMIT 1")
    fun getLatestSettingsFlow(userId: Int): Flow<Settings?>

    @Query("SELECT * FROM Settings WHERE User_ID = :userId LIMIT 1")
    fun getSettingsByUserFlow(userId: Int): Flow<Settings?>
}
