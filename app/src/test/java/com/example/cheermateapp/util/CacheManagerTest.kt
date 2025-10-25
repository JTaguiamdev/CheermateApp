package com.example.cheermateapp.util

import android.content.Context
import com.google.gson.reflect.TypeToken
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

/**
 * Unit tests for CacheManager
 * Note: These tests require Robolectric or Mockito for Context mocking
 */
@RunWith(MockitoJUnitRunner::class)
class CacheManagerTest {

    @Mock
    private lateinit var mockContext: Context

    private lateinit var testCacheDir: File

    @Before
    fun setup() {
        // Create a temporary directory for testing
        testCacheDir = createTempDir("cache_test")
        val appCacheDir = File(testCacheDir, "app_cache")
        appCacheDir.mkdirs()

        // Mock the context to return our test cache directory
        `when`(mockContext.cacheDir).thenReturn(testCacheDir)
    }

    @Test
    fun testSaveAndGetCache() {
        val cacheKey = "test_data"
        val testData = listOf("item1", "item2", "item3")

        // Save cache
        val saved = CacheManager.saveCache(mockContext, cacheKey, testData)
        assertTrue("Cache should be saved successfully", saved)

        // Retrieve cache
        val typeToken = object : TypeToken<CacheManager.CachedData<List<String>>>() {}
        val retrieved = CacheManager.getCache(mockContext, cacheKey, typeToken)

        assertNotNull("Retrieved data should not be null", retrieved)
        assertEquals("Retrieved data should match saved data", testData, retrieved)
    }

    @Test
    fun testCacheMiss() {
        val cacheKey = "non_existent_key"
        val typeToken = object : TypeToken<CacheManager.CachedData<List<String>>>() {}

        val retrieved = CacheManager.getCache<List<String>>(mockContext, cacheKey, typeToken)
        assertNull("Retrieved data should be null for non-existent cache", retrieved)
    }

    @Test
    fun testCacheInvalidation() {
        val cacheKey = "test_invalidation"
        val testData = "test value"

        // Save cache
        CacheManager.saveCache(mockContext, cacheKey, testData)

        // Invalidate cache
        val invalidated = CacheManager.invalidateCache(mockContext, cacheKey)
        assertTrue("Cache should be invalidated successfully", invalidated)

        // Try to retrieve - should return null
        val typeToken = object : TypeToken<CacheManager.CachedData<String>>() {}
        val retrieved = CacheManager.getCache<String>(mockContext, cacheKey, typeToken)
        assertNull("Retrieved data should be null after invalidation", retrieved)
    }

    @Test
    fun testStaleCache() {
        val cacheKey = "test_stale"
        val testData = "old data"

        // Save cache
        CacheManager.saveCache(mockContext, cacheKey, testData)

        // Try to get with very short max age (1ms)
        Thread.sleep(10) // Wait 10ms to ensure cache is stale

        val typeToken = object : TypeToken<CacheManager.CachedData<String>>() {}
        val retrieved = CacheManager.getCache<String>(
            mockContext,
            cacheKey,
            typeToken,
            maxAgeMs = 1L // 1 millisecond
        )

        assertNull("Retrieved data should be null for stale cache", retrieved)
    }

    @Test
    fun testCacheValidity() {
        val cacheKey = "test_validity"
        val testData = "valid data"

        // No cache initially
        assertFalse(
            "Cache should not be valid initially",
            CacheManager.isCacheValid(mockContext, cacheKey)
        )

        // Save cache
        CacheManager.saveCache(mockContext, cacheKey, testData)

        // Check validity
        assertTrue(
            "Cache should be valid after saving",
            CacheManager.isCacheValid(mockContext, cacheKey)
        )
    }

    @Test
    fun testClearAllCache() {
        // Save multiple caches
        CacheManager.saveCache(mockContext, "cache1", "data1")
        CacheManager.saveCache(mockContext, "cache2", "data2")
        CacheManager.saveCache(mockContext, "cache3", "data3")

        // Clear all
        val cleared = CacheManager.clearAllCache(mockContext)
        assertTrue("All caches should be cleared", cleared)

        // Verify all caches are gone
        assertFalse(CacheManager.isCacheValid(mockContext, "cache1"))
        assertFalse(CacheManager.isCacheValid(mockContext, "cache2"))
        assertFalse(CacheManager.isCacheValid(mockContext, "cache3"))
    }

    @Test
    fun testComplexDataStructure() {
        data class TestData(val id: Int, val name: String, val items: List<String>)

        val cacheKey = "test_complex"
        val testData = TestData(1, "Test", listOf("a", "b", "c"))

        // Save
        CacheManager.saveCache(mockContext, cacheKey, testData)

        // Retrieve
        val typeToken = object : TypeToken<CacheManager.CachedData<TestData>>() {}
        val retrieved = CacheManager.getCache<TestData>(mockContext, cacheKey, typeToken)

        assertNotNull("Retrieved data should not be null", retrieved)
        assertEquals("ID should match", testData.id, retrieved?.id)
        assertEquals("Name should match", testData.name, retrieved?.name)
        assertEquals("Items should match", testData.items, retrieved?.items)
    }
}
