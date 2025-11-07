package com.example.cheermateapp.validation.crud

import com.example.cheermateapp.data.dao.UserDao
import com.example.cheermateapp.data.model.User

/**
 * CRUD validator for UserDao
 */
class UserDaoCrudChecker(private val userDao: UserDao) : BaseCrudChecker() {
    
    override fun getDaoName(): String = "UserDao"
    
    override suspend fun validateDao(): List<CrudTestResult> {
        val results = mutableListOf<CrudTestResult>()
        
        results.add(testInsert())
        results.add(testUpdate())
        results.add(testDelete())
        results.add(testGetById())
        results.add(testFindByUsername())
        results.add(testFindByEmail())
        results.add(testGetAllUsers())
        results.add(testGetUserCount())
        results.add(testUpdatePersonality())
        results.add(testUpdateUsername())
        
        return results
    }
    
    private suspend fun testInsert() = executeTest(CrudOperation.CREATE, "insert") {
        val testUser = createTestUser(userId = 99999, username = "test_user_99999")
        val insertedId = userDao.insert(testUser)
        assert(insertedId > 0) { "Insert should return positive ID" }
        
        // Cleanup
        userDao.deleteById(99999)
    }
    
    private suspend fun testUpdate() = executeTest(CrudOperation.UPDATE, "update") {
        val testUser = createTestUser(userId = 99998, username = "test_user_99998")
        userDao.insert(testUser)
        
        val updatedUser = testUser.copy(Email = "updated@test.com")
        userDao.update(updatedUser)
        
        val retrieved = userDao.getById(99998)
        assert(retrieved?.Email == "updated@test.com") { "Email should be updated" }
        
        // Cleanup
        userDao.deleteById(99998)
    }
    
    private suspend fun testDelete() = executeTest(CrudOperation.DELETE, "delete") {
        val testUser = createTestUser(userId = 99997, username = "test_user_99997")
        userDao.insert(testUser)
        
        userDao.delete(testUser)
        
        val retrieved = userDao.getById(99997)
        assert(retrieved == null) { "User should be deleted" }
    }
    
    private suspend fun testGetById() = executeTest(CrudOperation.READ, "getById") {
        val testUser = createTestUser(userId = 99996, username = "test_user_99996")
        userDao.insert(testUser)
        
        val retrieved = userDao.getById(99996)
        assert(retrieved != null) { "Should retrieve user by ID" }
        assert(retrieved?.Username == "test_user_99996") { "Username should match" }
        
        // Cleanup
        userDao.deleteById(99996)
    }
    
    private suspend fun testFindByUsername() = executeTest(CrudOperation.QUERY, "findByUsername") {
        val testUser = createTestUser(userId = 99995, username = "unique_username_99995")
        userDao.insert(testUser)
        
        val retrieved = userDao.findByUsername("unique_username_99995")
        assert(retrieved != null) { "Should find user by username" }
        
        // Cleanup
        userDao.deleteById(99995)
    }
    
    private suspend fun testFindByEmail() = executeTest(CrudOperation.QUERY, "findByEmail") {
        val testUser = createTestUser(userId = 99994, username = "test_user_99994", email = "unique99994@test.com")
        userDao.insert(testUser)
        
        val retrieved = userDao.findByEmail("unique99994@test.com")
        assert(retrieved != null) { "Should find user by email" }
        
        // Cleanup
        userDao.deleteById(99994)
    }
    
    private suspend fun testGetAllUsers() = executeTest(CrudOperation.READ, "getAllUsers") {
        val users = userDao.getAllUsers()
        // Should not throw exception
    }
    
    private suspend fun testGetUserCount() = executeTest(CrudOperation.COUNT, "getUserCount") {
        val count = userDao.getUserCount()
        assert(count >= 0) { "Count should be non-negative" }
    }
    
    private suspend fun testUpdatePersonality() = executeTest(CrudOperation.UPDATE, "updatePersonality") {
        val testUser = createTestUser(userId = 99993, username = "test_user_99993")
        userDao.insert(testUser)
        
        userDao.updatePersonality(99993, 5)
        
        val retrieved = userDao.getById(99993)
        assert(retrieved?.Personality_ID == 5) { "Personality should be updated" }
        
        // Cleanup
        userDao.deleteById(99993)
    }
    
    private suspend fun testUpdateUsername() = executeTest(CrudOperation.UPDATE, "updateUsername") {
        val testUser = createTestUser(userId = 99992, username = "test_user_99992")
        userDao.insert(testUser)
        
        userDao.updateUsername(99992, "new_username_99992")
        
        val retrieved = userDao.getById(99992)
        assert(retrieved?.Username == "new_username_99992") { "Username should be updated" }
        
        // Cleanup
        userDao.deleteById(99992)
    }
    
    private fun createTestUser(
        userId: Int,
        username: String,
        email: String = "$username@test.com"
    ): User {
        return User(
            User_ID = userId,
            Username = username,
            Email = email,
            PasswordHash = "test_hash",
            Personality_ID = null
        )
    }
}
