package com.example.cheermateapp.validation.crud

import com.example.cheermateapp.data.dao.PersonalityDao
import com.example.cheermateapp.data.model.Personality

/**
 * CRUD validator for PersonalityDao
 */
class PersonalityDaoCrudChecker(private val personalityDao: PersonalityDao) : BaseCrudChecker() {
    
    override fun getDaoName(): String = "PersonalityDao"
    
    override suspend fun validateDao(): List<CrudTestResult> {
        val results = mutableListOf<CrudTestResult>()
        
        results.add(testInsert())
        results.add(testUpdate())
        results.add(testDelete())
        results.add(testUpsert())
        results.add(testGetAll())
        results.add(testGetById())
        results.add(testGetByName())
        results.add(testGetByUser())
        results.add(testGetCount())
        
        return results
    }
    
    private suspend fun testInsert() = executeTest(CrudOperation.CREATE, "insert") {
        val test = createTestPersonality()
        val insertedId = personalityDao.insert(test)
        assert(insertedId > 0) { "Insert should return positive ID" }
        
        // Cleanup
        personalityDao.delete(test)
    }
    
    private suspend fun testUpdate() = executeTest(CrudOperation.UPDATE, "update") {
        val test = createTestPersonality()
        personalityDao.insert(test)
        
        val updated = test.copy(Name = "Updated Name")
        personalityDao.update(updated)
        
        // Cleanup
        personalityDao.delete(updated)
    }
    
    private suspend fun testDelete() = executeTest(CrudOperation.DELETE, "delete") {
        val test = createTestPersonality()
        personalityDao.insert(test)
        personalityDao.delete(test)
    }
    
    private suspend fun testUpsert() = executeTest(CrudOperation.CREATE, "upsert") {
        val test = createTestPersonality()
        personalityDao.upsert(test)
        
        // Cleanup
        personalityDao.delete(test)
    }
    
    private suspend fun testGetAll() = executeTest(CrudOperation.READ, "getAll") {
        personalityDao.getAll()
    }
    
    private suspend fun testGetById() = executeTest(CrudOperation.READ, "getById") {
        personalityDao.getById(1)
    }
    
    private suspend fun testGetByName() = executeTest(CrudOperation.QUERY, "getByName") {
        personalityDao.getByName("Test")
    }
    
    private suspend fun testGetByUser() = executeTest(CrudOperation.QUERY, "getByUser") {
        personalityDao.getByUser(9999)
    }
    
    private suspend fun testGetCount() = executeTest(CrudOperation.COUNT, "getCount") {
        val count = personalityDao.getCount()
        assert(count >= 0) { "Count should be non-negative" }
    }
    
    private fun createTestPersonality(): Personality {
        return Personality(
            Personality_ID = 99999,
            User_ID = 9999,
            Name = "Test Personality",
            Description = "Test Description"
        )
    }
}
