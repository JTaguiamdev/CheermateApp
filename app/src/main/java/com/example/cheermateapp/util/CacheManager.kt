package com.example.cheermateapp.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException

/**
 * Generic cache manager for storing and retrieving data in JSON format
 * Implements a simple cache-aside pattern with timestamp-based invalidation
 */
object CacheManager {
    val gson = Gson()
    
    /**
     * Cache metadata for tracking modifications
     */
    data class CacheMetadata(
        val lastModified: Long,
        val version: Int = 1
    )
    
    /**
     * Wrapper for cached data with metadata
     */
    data class CachedData<T>(
        val data: T,
        val metadata: CacheMetadata
    )
    
    /**
     * Get cached data if it exists and is not stale
     * @param T Type parameter (inferred automatically using reified generics)
     * @param context Application context
     * @param cacheKey Unique key for this cache entry
     * @param maxAgeMs Maximum age of cache in milliseconds (default 24 hours)
     * @return Cached data or null if not found or stale
     */
    inline fun <reified T> getCache(
        context: Context,
        cacheKey: String,
        maxAgeMs: Long = 24 * 60 * 60 * 1000L // 24 hours
    ): T? {
        return try {
            val cacheFile = getCacheFile(context, cacheKey)
            
            if (!cacheFile.exists()) {
                return null
            }
            
            val json = cacheFile.readText()
            val type = object : TypeToken<CachedData<T>>() {}.type
            val cachedData: CachedData<T> = gson.fromJson(json, type)
            
            // Check if cache is stale
            val age = System.currentTimeMillis() - cachedData.metadata.lastModified
            if (age > maxAgeMs) {
                cacheFile.delete() // Clean up stale cache
                return null
            }
            
            cachedData.data
        } catch (e: Exception) {
            android.util.Log.e("CacheManager", "Error reading cache for $cacheKey", e)
            null
        }
    }
    
    /**
     * Save data to cache with current timestamp
     * @param context Application context
     * @param cacheKey Unique key for this cache entry
     * @param data Data to cache
     * @return true if successful, false otherwise
     */
    fun <T> saveCache(
        context: Context,
        cacheKey: String,
        data: T
    ): Boolean {
        return try {
            val cacheFile = getCacheFile(context, cacheKey)
            val cachedData = CachedData(
                data = data,
                metadata = CacheMetadata(
                    lastModified = System.currentTimeMillis()
                )
            )
            
            val json = gson.toJson(cachedData)
            cacheFile.writeText(json)
            true
        } catch (e: IOException) {
            android.util.Log.e("CacheManager", "Error writing cache for $cacheKey", e)
            false
        }
    }
    
    /**
     * Invalidate (delete) cached data
     * @param context Application context
     * @param cacheKey Unique key for this cache entry
     * @return true if cache was deleted or didn't exist
     */
    fun invalidateCache(context: Context, cacheKey: String): Boolean {
        return try {
            val cacheFile = getCacheFile(context, cacheKey)
            if (cacheFile.exists()) {
                cacheFile.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            android.util.Log.e("CacheManager", "Error invalidating cache for $cacheKey", e)
            false
        }
    }
    
    /**
     * Check if cache exists and is valid
     * @param context Application context
     * @param cacheKey Unique key for this cache entry
     * @param maxAgeMs Maximum age of cache in milliseconds
     * @return true if cache exists and is not stale
     */
    fun isCacheValid(
        context: Context,
        cacheKey: String,
        maxAgeMs: Long = 24 * 60 * 60 * 1000L
    ): Boolean {
        return try {
            val cacheFile = getCacheFile(context, cacheKey)
            
            if (!cacheFile.exists()) {
                return false
            }
            
            val json = cacheFile.readText()
            val type = object : TypeToken<CachedData<Any>>() {}.type
            val cachedData: CachedData<Any> = gson.fromJson(json, type)
            
            val age = System.currentTimeMillis() - cachedData.metadata.lastModified
            age <= maxAgeMs
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get the cache file for a given key
     */
    fun getCacheFile(context: Context, cacheKey: String): File {
        val cacheDir = File(context.cacheDir, "app_cache")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return File(cacheDir, "$cacheKey.json")
    }
    
    /**
     * Clear all cached data
     * @param context Application context
     * @return true if successful
     */
    fun clearAllCache(context: Context): Boolean {
        return try {
            val cacheDir = File(context.cacheDir, "app_cache")
            if (cacheDir.exists() && cacheDir.isDirectory) {
                cacheDir.listFiles()?.forEach { it.delete() }
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("CacheManager", "Error clearing all cache", e)
            false
        }
    }
}
