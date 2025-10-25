package com.example.cheermateapp.examples

import android.content.Context
import com.example.cheermateapp.data.db.AppDb
import com.example.cheermateapp.util.CacheManager
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Example showing how to extend the caching system for other data types
 * This is a template for implementing caching for new static/reference data
 */
class CachingSystemExample(private val context: Context) {

    private val db = AppDb.get(context)

    companion object {
        // Define unique cache keys for each data type
        private const val CACHE_KEY_EXAMPLE_DATA = "example_data"
        
        // Define cache expiration (7 days for relatively static data)
        private const val CACHE_MAX_AGE_MS = 7 * 24 * 60 * 60 * 1000L
    }

    /**
     * Example 1: Caching a simple list of strings
     * Use case: App configuration, feature flags, etc.
     */
    suspend fun getConfigurationValues(): List<String> {
        return withContext(Dispatchers.IO) {
            // Try to get from cache first
            val typeToken = object : TypeToken<CacheManager.CachedData<List<String>>>() {}
            val cachedData = CacheManager.getCache(context, CACHE_KEY_EXAMPLE_DATA, typeToken, CACHE_MAX_AGE_MS)
            
            if (cachedData != null) {
                android.util.Log.d("CachingExample", "Data loaded from cache")
                return@withContext cachedData
            }
            
            // Cache miss - fetch from database or API
            android.util.Log.d("CachingExample", "Cache miss, fetching from source")
            val data = fetchConfigurationFromDatabase()
            
            // Save to cache for next time
            if (data.isNotEmpty()) {
                CacheManager.saveCache(context, CACHE_KEY_EXAMPLE_DATA, data)
            }
            
            data
        }
    }

    /**
     * Example 2: Caching complex data structures
     * Use case: Task categories with metadata
     */
    data class TaskCategory(
        val id: Int,
        val name: String,
        val icon: String,
        val color: String
    )

    suspend fun getTaskCategories(): List<TaskCategory> {
        return withContext(Dispatchers.IO) {
            val cacheKey = "task_categories"
            val typeToken = object : TypeToken<CacheManager.CachedData<List<TaskCategory>>>() {}
            val cachedData = CacheManager.getCache(context, cacheKey, typeToken, CACHE_MAX_AGE_MS)
            
            if (cachedData != null) {
                return@withContext cachedData
            }
            
            // Fetch from database
            val categories = listOf(
                TaskCategory(1, "Work", "💼", "#FF5722"),
                TaskCategory(2, "Personal", "👤", "#2196F3"),
                TaskCategory(3, "Shopping", "🛒", "#4CAF50"),
                TaskCategory(4, "Others", "📤", "#9E9E9E")
            )
            
            CacheManager.saveCache(context, cacheKey, categories)
            categories
        }
    }

    /**
     * Example 3: Invalidating cache when data changes
     */
    suspend fun updateCategory(category: TaskCategory) {
        withContext(Dispatchers.IO) {
            // Update in database
            // db.categoryDao().update(category)
            
            // Invalidate cache so next fetch gets fresh data
            CacheManager.invalidateCache(context, "task_categories")
        }
    }

    /**
     * Example 4: Smart cache refresh
     * Check if database has newer data and update cache accordingly
     */
    suspend fun smartRefreshCache() {
        withContext(Dispatchers.IO) {
            val cacheKey = "task_categories"
            
            // Check if cache exists and is valid
            if (CacheManager.isCacheValid(context, cacheKey, CACHE_MAX_AGE_MS)) {
                // Cache is valid, but check if database has updates
                // val dbLastModified = db.categoryDao().getLastModifiedTimestamp()
                // Compare with cache timestamp and update if needed
                android.util.Log.d("CachingExample", "Cache is valid, no refresh needed")
            } else {
                // Cache is stale or doesn't exist, fetch fresh data
                android.util.Log.d("CachingExample", "Cache is stale, refreshing...")
                getTaskCategories() // This will fetch and cache new data
            }
        }
    }

    /**
     * Example 5: Conditional caching based on data size
     * Only cache if data meets certain criteria
     */
    suspend fun getDataWithConditionalCaching(threshold: Int = 100): List<String> {
        return withContext(Dispatchers.IO) {
            val data = fetchConfigurationFromDatabase()
            
            // Only cache if data size is reasonable
            if (data.size <= threshold) {
                CacheManager.saveCache(context, CACHE_KEY_EXAMPLE_DATA, data)
                android.util.Log.d("CachingExample", "Data cached (size: ${data.size})")
            } else {
                android.util.Log.d("CachingExample", "Data too large to cache (size: ${data.size})")
            }
            
            data
        }
    }

    /**
     * Example 6: Cache warming (pre-loading cache)
     * Load frequently accessed data into cache during app startup or idle time
     */
    suspend fun warmUpCache() {
        withContext(Dispatchers.IO) {
            android.util.Log.d("CachingExample", "Warming up cache...")
            
            // Pre-load all static data
            getTaskCategories()
            getConfigurationValues()
            
            android.util.Log.d("CachingExample", "Cache warmed up successfully")
        }
    }

    /**
     * Example 7: Batch cache operations
     * Invalidate multiple caches at once (e.g., after data import)
     */
    fun invalidateAllRelatedCaches() {
        CacheManager.invalidateCache(context, "task_categories")
        CacheManager.invalidateCache(context, CACHE_KEY_EXAMPLE_DATA)
        CacheManager.invalidateCache(context, "security_questions")
        CacheManager.invalidateCache(context, "personality_types")
        
        android.util.Log.d("CachingExample", "All related caches invalidated")
    }

    // Private helper method (simulates database fetch)
    private fun fetchConfigurationFromDatabase(): List<String> {
        // In real implementation, this would query the database
        return listOf("Config1", "Config2", "Config3")
    }
}

/**
 * Usage Example in an Activity:
 * 
 * class MyActivity : AppCompatActivity() {
 *     private lateinit var cachingExample: CachingSystemExample
 *     
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         cachingExample = CachingSystemExample(this)
 *         
 *         lifecycleScope.launch {
 *             // Get cached data (will fetch from DB on first call)
 *             val categories = cachingExample.getTaskCategories()
 *             
 *             // Update data (cache will be invalidated)
 *             cachingExample.updateCategory(categories[0])
 *             
 *             // Warm up cache in background
 *             cachingExample.warmUpCache()
 *         }
 *     }
 * }
 */
