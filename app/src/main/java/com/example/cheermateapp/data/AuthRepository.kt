package com.example.cheermateapp.data

import com.example.cheermateapp.data.db.AppDb
import com.example.cheermateapp.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val db: AppDb) {

    suspend fun validateCredentials(
        username: String,
        password: String,
        onResult: (User?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val user = db.userDao().findByUsername(username)?.let { u ->
                    // Demo-only: compare plain; replace with proper hashing (e.g., BCrypt)
                    if (u.PasswordHash == password) u else null
                }
                onResult(user)

            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Error validating credentials", e)
                onResult(null)
            }
        }
    }

    // Removed createUser method since it's not being used
}
