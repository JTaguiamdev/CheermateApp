package com.example.cheermateapp.data

import android.content.Context
import android.util.Log
import com.example.cheermateapp.data.db.AppDb
import com.example.cheermateapp.data.model.PersonalityType
import com.example.cheermateapp.data.model.SecurityQuestion
import com.example.cheermateapp.util.CacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing static reference data with caching
 */
class StaticDataRepository(private val context: Context) {
    
    private val db = AppDb.get(context)
    
    companion object {
        private const val TAG = "StaticDataRepository"
        private const val CACHE_KEY_PERSONALITY_TYPES = "personality_types"
        private const val CACHE_KEY_SECURITY_QUESTIONS = "security_questions"
        
        // Cache for 7 days since this is relatively static data
        private const val CACHE_MAX_AGE_MS = 7 * 24 * 60 * 60 * 1000L
    }
    
    /**
     * Get personality types with caching
     * First checks cache, then database if cache miss or stale
     */
    suspend fun getPersonalityTypes(): List<PersonalityType> {
        return withContext(Dispatchers.IO) {
            try {
                // Try to get from cache first
                val cachedData = CacheManager.getCache<List<PersonalityType>>(context, CACHE_KEY_PERSONALITY_TYPES, CACHE_MAX_AGE_MS)
                
                if (cachedData != null) {
                    Log.d(TAG, "Personality types loaded from cache")
                    return@withContext cachedData
                }
                
                // Cache miss or stale, fetch from database
                Log.d(TAG, "Cache miss, fetching personality types from database")
                val data = db.personalityTypeDao().getAllActive()
                
                if (data.isNotEmpty()) {
                    // Save to cache for next time
                    CacheManager.saveCache(context, CACHE_KEY_PERSONALITY_TYPES, data)
                    Log.d(TAG, "Personality types cached (${data.size} items)")
                }
                
                data
            } catch (e: Exception) {
                Log.e(TAG, "Error getting personality types", e)
                emptyList()
            }
        }
    }
    
    /**
     * Get security questions with caching
     * First checks cache, then database if cache miss or stale
     */
    suspend fun getSecurityQuestions(): List<SecurityQuestion> {
        return withContext(Dispatchers.IO) {
            try {
                // Try to get from cache first
                val cachedData = CacheManager.getCache<List<SecurityQuestion>>(context, CACHE_KEY_SECURITY_QUESTIONS, CACHE_MAX_AGE_MS)
                
                if (cachedData != null) {
                    Log.d(TAG, "Security questions loaded from cache")
                    return@withContext cachedData
                }
                
                // Cache miss or stale, fetch from database
                Log.d(TAG, "Cache miss, fetching security questions from database")
                val data = db.securityDao().getAllQuestions()
                    .filter { it.IsActive }
                
                if (data.isNotEmpty()) {
                    // Save to cache for next time
                    CacheManager.saveCache(context, CACHE_KEY_SECURITY_QUESTIONS, data)
                    Log.d(TAG, "Security questions cached (${data.size} items)")
                }
                
                data
            } catch (e: Exception) {
                Log.e(TAG, "Error getting security questions", e)
                emptyList()
            }
        }
    }
    
    /**
     * Get prompts (string) from security questions for UI display
     */
    suspend fun getSecurityQuestionPrompts(): List<String> {
        return getSecurityQuestions().map { it.Prompt }
    }
    
    /**
     * Invalidate personality types cache
     * Call this when personality types are modified in the database
     */
    fun invalidatePersonalityTypesCache() {
        CacheManager.invalidateCache(context, CACHE_KEY_PERSONALITY_TYPES)
        Log.d(TAG, "Personality types cache invalidated")
    }
    
    /**
     * Invalidate security questions cache
     * Call this when security questions are modified in the database
     */
    fun invalidateSecurityQuestionsCache() {
        CacheManager.invalidateCache(context, CACHE_KEY_SECURITY_QUESTIONS)
        Log.d(TAG, "Security questions cache invalidated")
    }
    
    /**
     * Invalidate all caches
     */
    fun invalidateAllCaches() {
        invalidatePersonalityTypesCache()
        invalidateSecurityQuestionsCache()
        Log.d(TAG, "All caches invalidated")
    }
}
