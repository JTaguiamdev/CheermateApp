package com.example.cheermateapp.validation.crud

import com.example.cheermateapp.data.dao.PersonalityTypeDao
import com.example.cheermateapp.data.model.PersonalityType

/**
 * CRUD validator for PersonalityTypeDao
 */
class PersonalityTypeDaoCrudChecker(private val personalityTypeDao: PersonalityTypeDao) : BaseCrudChecker() {
    
    override fun getDaoName(): String = "PersonalityTypeDao"
    
    override suspend fun validateDao(): List<CrudTestResult> {
        val results = mutableListOf<CrudTestResult>()
        
        results.add(testInsert())
        results.add(testInsertAll())
        results.add(testUpdate())
        results.add(testDelete())
        results.add(testGetAllActive())
        results.add(testGetAll())
        results.add(testGetById())
        results.add(testGetByName())
        results.add(testGetCount())
        results.add(testGetLastModifiedTimestamp())
        
        return results
    }
    
    private suspend fun testInsert() = executeTest(CrudOperation.CREATE, "insert") {
        val test = createTestPersonalityType()
        val insertedId = personalityTypeDao.insert(test)
        assert(insertedId > 0) { "Insert should return positive ID" }
        
        // Cleanup
        personalityTypeDao.delete(test)
    }
    
    private suspend fun testInsertAll() = executeTest(CrudOperation.BULK_OPERATION, "insertAll") {
        val types = listOf(
            createTestPersonalityType(typeId = 99990),
            createTestPersonalityType(typeId = 99991)
        )
        personalityTypeDao.insertAll(types)
        
        // Cleanup
        types.forEach { personalityTypeDao.delete(it) }
    }
    
    private suspend fun testUpdate() = executeTest(CrudOperation.UPDATE, "update") {
        val test = createTestPersonalityType()
        personalityTypeDao.insert(test)
        
        val updated = test.copy(Name = "Updated Type")
        personalityTypeDao.update(updated)
        
        // Cleanup
        personalityTypeDao.delete(updated)
    }
    
    private suspend fun testDelete() = executeTest(CrudOperation.DELETE, "delete") {
        val test = createTestPersonalityType()
        personalityTypeDao.insert(test)
        personalityTypeDao.delete(test)
    }
    
    private suspend fun testGetAllActive() = executeTest(CrudOperation.READ, "getAllActive") {
        personalityTypeDao.getAllActive()
    }
    
    private suspend fun testGetAll() = executeTest(CrudOperation.READ, "getAll") {
        personalityTypeDao.getAll()
    }
    
    private suspend fun testGetById() = executeTest(CrudOperation.READ, "getById") {
        personalityTypeDao.getById(1)
    }
    
    private suspend fun testGetByName() = executeTest(CrudOperation.QUERY, "getByName") {
        personalityTypeDao.getByName("Test")
    }
    
    private suspend fun testGetCount() = executeTest(CrudOperation.COUNT, "getCount") {
        val count = personalityTypeDao.getCount()
        assert(count >= 0) { "Count should be non-negative" }
    }
    
    private suspend fun testGetLastModifiedTimestamp() = executeTest(CrudOperation.QUERY, "getLastModifiedTimestamp") {
        personalityTypeDao.getLastModifiedTimestamp()
    }
    
    private fun createTestPersonalityType(typeId: Int = 99999): PersonalityType {
        return PersonalityType(
            Type_ID = typeId,
            Name = "Test Type",
            Description = "Test Description",
            IsActive = true,
            CreatedAt = System.currentTimeMillis(),
            UpdatedAt = System.currentTimeMillis()
        )
    }
}
