package com.cheermateapp.data

import com.cheermateapp.data.db.AppDb
import com.cheermateapp.data.model.User
import com.cheermateapp.util.PasswordHashUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for authentication operations
 */
class AuthRepository(private val db: AppDb) {

    /**
     * Validate user credentials with BCrypt password verification
     * @param username The username to validate
     * @param password The plain text password to verify
     * @param onResult Callback with the validated user or null if invalid
     */
    suspend fun validateCredentials(
        username: String,
        password: String,
        onResult: (User?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val user = db.userDao().findByUsername(username)
                
                if (user != null) {
                    // Verify password using BCrypt
                    val isValid = PasswordHashUtil.verifyPassword(password, user.PasswordHash)
                    onResult(if (isValid) user else null)
                } else {
                    onResult(null)
                }

            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Error validating credentials", e)
                onResult(null)
            }
        }
    }
}
