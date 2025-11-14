package com.example.cheermateapp.data

import android.content.Context
import android.util.Log
import com.example.cheermateapp.data.db.AppDb
import com.example.cheermateapp.data.model.Personality
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
        private const val CACHE_KEY_PERSONALITIES = "personalities"
        private const val CACHE_KEY_SECURITY_QUESTIONS = "security_questions"
        
        // Cache for 7 days since this is relatively static data
        private const val CACHE_MAX_AGE_MS = 7 * 24 * 60 * 60 * 1000L
    }
    
    /**
     * Get personalities with caching
     * First checks cache, then database if cache miss or stale
     */
    suspend fun getPersonalities(): List<Personality> {
        return withContext(Dispatchers.IO) {
            try {
                // Try to get from cache first
                val cachedData = CacheManager.getCache<List<Personality>>(context, CACHE_KEY_PERSONALITIES, CACHE_MAX_AGE_MS)
                
                if (cachedData != null) {
                    Log.d(TAG, "Personalities loaded from cache")
                    return@withContext cachedData
                }
                
                // Cache miss or stale, fetch from database
                Log.d(TAG, "Cache miss, fetching personalities from database")
                val data = db.personalityDao().getAllActive()
                
                if (data.isNotEmpty()) {
                    // Save to cache for next time
                    CacheManager.saveCache(context, CACHE_KEY_PERSONALITIES, data)
                    Log.d(TAG, "Personalities cached (${data.size} items)")
                }
                
                data
            } catch (e: Exception) {
                Log.e(TAG, "Error getting personalities", e)
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
     * Invalidate personalities cache
     * Call this when personalities are modified in the database
     */
    fun invalidatePersonalitiesCache() {
        CacheManager.invalidateCache(context, CACHE_KEY_PERSONALITIES)
        Log.d(TAG, "Personalities cache invalidated")
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
        invalidatePersonalitiesCache()
        invalidateSecurityQuestionsCache()
        Log.d(TAG, "All caches invalidated")
    }
}
