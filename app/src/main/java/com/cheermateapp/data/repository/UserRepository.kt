package com.cheermateapp.data.repository

import android.util.Log
import com.cheermateapp.data.dao.UserDao
import com.cheermateapp.data.model.User
import com.cheermateapp.util.PasswordHashUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Repository for User operations
 * Handles user CRUD, authentication, and profile management
 */
class UserRepository(private val userDao: UserDao) {

    companion object {
        private const val TAG = "UserRepository"
    }

    // ==================== USER CRUD OPERATIONS ====================

    /**
     * Create a new user
     */
    suspend fun createUser(user: User): DataResult<Long> = withContext(Dispatchers.IO) {
        try {
            // Check if username already exists
            val existingUser = userDao.findByUsername(user.Username)
            if (existingUser != null) {
                return@withContext DataResult.Error(
                    Exception("Username already exists"),
                    "Username '${user.Username}' is already taken"
                )
            }

            // Check if email already exists
            val existingEmail = userDao.findByEmail(user.Email)
            if (existingEmail != null) {
                return@withContext DataResult.Error(
                    Exception("Email already exists"),
                    "Email '${user.Email}' is already registered"
                )
            }

            val userId = userDao.insert(user)
            Log.d(TAG, "User created successfully: $userId")
            DataResult.Success(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user", e)
            DataResult.Error(e, "Failed to create user: ${e.message}")
        }
    }

    /**
     * Update user profile
     */
    suspend fun updateUser(user: User): DataResult<Unit> = withContext(Dispatchers.IO) {
        try {
            userDao.update(user)
            Log.d(TAG, "User updated successfully: ${user.User_ID}")
            DataResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user", e)
            DataResult.Error(e, "Failed to update user: ${e.message}")
        }
    }

    /**
     * Delete user
     */
    suspend fun deleteUser(userId: Int): DataResult<Unit> = withContext(Dispatchers.IO) {
        try {
            userDao.deleteById(userId)
            Log.d(TAG, "User deleted: $userId")
            DataResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user", e)
            DataResult.Error(e, "Failed to delete user: ${e.message}")
        }
    }

    // ==================== USER QUERIES ====================

    /**
     * Get user by ID
     */
    suspend fun getUserById(userId: Int): DataResult<User?> = withContext(Dispatchers.IO) {
        try {
            val user = userDao.getById(userId)
            DataResult.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by ID", e)
            DataResult.Error(e, "Failed to load user: ${e.message}")
        }
    }

    /**
     * Get user by ID as Flow for reactive updates
     */
    fun getUserByIdFlow(userId: Int): Flow<User?> {
        return userDao.getUserByIdFlow(userId)
            .catch { e ->
                Log.e(TAG, "Error in user flow", e)
                emit(null)
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Find user by username
     */
    suspend fun findByUsername(username: String): DataResult<User?> = withContext(Dispatchers.IO) {
        try {
            val user = userDao.findByUsername(username)
            DataResult.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Error finding user by username", e)
            DataResult.Error(e, "Failed to find user: ${e.message}")
        }
    }

    /**
     * Find user by email
     */
    suspend fun findByEmail(email: String): DataResult<User?> = withContext(Dispatchers.IO) {
        try {
            val user = userDao.findByEmail(email)
            DataResult.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Error finding user by email", e)
            DataResult.Error(e, "Failed to find user: ${e.message}")
        }
    }

    // ==================== AUTHENTICATION ====================

    /**
     * Validate user credentials with proper password hashing
     */
    suspend fun validateCredentials(username: String, password: String): DataResult<User> = withContext(Dispatchers.IO) {
        try {
            val user = userDao.findByUsername(username)
            
            if (user == null) {
                return@withContext DataResult.Error(
                    Exception("User not found"),
                    "Invalid username or password"
                )
            }

            // Verify password using PBKDF2-HMAC-SHA256
            val isValid = PasswordHashUtil.verifyPassword(password, user.PasswordHash)
            
            if (!isValid) {
                return@withContext DataResult.Error(
                    Exception("Invalid password"),
                    "Invalid username or password"
                )
            }

            Log.d(TAG, "User authenticated successfully: ${user.Username}")
            DataResult.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Error validating credentials", e)
            DataResult.Error(e, "Authentication failed: ${e.message}")
        }
    }

    // ==================== PROFILE UPDATES ====================

    /**
     * Update username
     */
    suspend fun updateUsername(userId: Int, newUsername: String): DataResult<Unit> = withContext(Dispatchers.IO) {
        try {
            // Check if username is already taken
            val existingUser = userDao.findByUsername(newUsername)
            if (existingUser != null && existingUser.User_ID != userId) {
                return@withContext DataResult.Error(
                    Exception("Username already exists"),
                    "Username '$newUsername' is already taken"
                )
            }

            userDao.updateUsername(userId, newUsername)
            Log.d(TAG, "Username updated for user $userId")
            DataResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating username", e)
            DataResult.Error(e, "Failed to update username: ${e.message}")
        }
    }

    /**
     * Update personality
     */
    suspend fun updatePersonality(userId: Int, personalityId: Int): DataResult<Unit> = withContext(Dispatchers.IO) {
        try {
            userDao.updatePersonality(userId, personalityId)
            Log.d(TAG, "Personality updated for user $userId")
            DataResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating personality", e)
            DataResult.Error(e, "Failed to update personality: ${e.message}")
        }
    }

    // ==================== STATISTICS ====================

    /**
     * Get total user count
     */
    suspend fun getUserCount(): DataResult<Int> = withContext(Dispatchers.IO) {
        try {
            val count = userDao.getUserCount()
            DataResult.Success(count)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user count", e)
            DataResult.Error(e, "Failed to get user count: ${e.message}")
        }
    }
}
