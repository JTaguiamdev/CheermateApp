package com.example.cheermateapp.validation.crud

import com.example.cheermateapp.data.dao.SettingsDao
import com.example.cheermateapp.data.model.Settings

/**
 * CRUD validator for SettingsDao
 */
class SettingsDaoCrudChecker(private val settingsDao: SettingsDao) : BaseCrudChecker() {
    
    override fun getDaoName(): String = "SettingsDao"
    
    override suspend fun validateDao(): List<CrudTestResult> {
        val results = mutableListOf<CrudTestResult>()
        
        results.add(testInsert())
        results.add(testUpdate())
        results.add(testUpsert())
        results.add(testLatest())
        results.add(testGetSettingsByUser())
        results.add(testGetAllSettings())
        
        return results
    }
    
    private suspend fun testInsert() = executeTest(CrudOperation.CREATE, "insert") {
        val testSettings = createTestSettings()
        val insertedId = settingsDao.insert(testSettings)
        assert(insertedId > 0) { "Insert should return positive ID" }
    }
    
    private suspend fun testUpdate() = executeTest(CrudOperation.UPDATE, "update") {
        val testSettings = createTestSettings()
        settingsDao.insert(testSettings)
        
        val updated = testSettings.copy(NotificationsEnabled = false)
        settingsDao.update(updated)
    }
    
    private suspend fun testUpsert() = executeTest(CrudOperation.CREATE, "upsert") {
        val testSettings = createTestSettings()
        settingsDao.upsert(testSettings)
    }
    
    private suspend fun testLatest() = executeTest(CrudOperation.READ, "latest") {
        settingsDao.latest(9999)
    }
    
    private suspend fun testGetSettingsByUser() = executeTest(CrudOperation.QUERY, "getSettingsByUser") {
        settingsDao.getSettingsByUser(9999)
    }
    
    private suspend fun testGetAllSettings() = executeTest(CrudOperation.READ, "getAllSettings") {
        settingsDao.getAllSettings()
    }
    
    private fun createTestSettings(): Settings {
        return Settings(
            Settings_ID = null,
            User_ID = 9999,
            NotificationsEnabled = true,
            ThemeMode = "light",
            SoundEnabled = true
        )
    }
}
