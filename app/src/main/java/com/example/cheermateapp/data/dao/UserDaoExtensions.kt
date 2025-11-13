package com.example.cheermateapp.data.dao

import com.example.cheermateapp.data.model.User

/**
 * Extension functions for UserDao with validation
 */

/**
 * Insert user with validation for Personality_ID
 * Ensures Personality_ID is either null or in the range 1-5
 */
suspend fun UserDao.insertValidated(user: User): Long {
    // Validate Personality_ID is in valid range (1-5) or null
    user.Personality_ID?.let { personalityId ->
        require(personalityId in 1..5) {
            "Personality_ID must be between 1 and 5, got: $personalityId"
        }
    }
    return insert(user)
}

/**
 * Update user with validation for Personality_ID
 * Ensures Personality_ID is either null or in the range 1-5
 */
suspend fun UserDao.updateValidated(user: User) {
    // Validate Personality_ID is in valid range (1-5) or null
    user.Personality_ID?.let { personalityId ->
        require(personalityId in 1..5) {
            "Personality_ID must be between 1 and 5, got: $personalityId"
        }
    }
    update(user)
}

/**
 * Update personality with validation
 * Ensures personalityId is in the range 1-5
 */
suspend fun UserDao.updatePersonalityValidated(userId: Int, personalityId: Int) {
    require(personalityId in 1..5) {
        "Personality_ID must be between 1 and 5, got: $personalityId"
    }
    updatePersonality(userId, personalityId)
}
