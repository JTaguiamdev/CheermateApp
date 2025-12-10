package com.cheermateapp.data.dao

import androidx.room.*
import com.cheermateapp.data.model.UserSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(s: UserSettings)

    @Insert
    suspend fun insert(userSettings: UserSettings): Long

    @Update
    suspend fun update(userSettings: UserSettings)

    @Query("SELECT * FROM UserSettings WHERE User_ID = :userId ORDER BY UserSettings_ID DESC LIMIT 1")
    suspend fun latest(userId: Int): UserSettings?

    @Query("SELECT * FROM UserSettings WHERE User_ID = :userId ORDER BY UserSettings_ID DESC LIMIT 1")
    suspend fun getSettingsByUser(userId: Int): UserSettings?

    @Query("SELECT * FROM UserSettings")
    suspend fun getAllSettings(): List<UserSettings>

    // ✅ FLOW METHODS FOR REACTIVE UPDATES
    @Query("SELECT * FROM UserSettings WHERE User_ID = :userId ORDER BY UserSettings_ID DESC LIMIT 1")
    fun getLatestSettingsFlow(userId: Int): Flow<UserSettings?>

    @Query("SELECT * FROM UserSettings WHERE User_ID = :userId LIMIT 1")
    fun getSettingsByUserFlow(userId: Int): Flow<UserSettings?>
}
