package com.example.cheermateapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.cheermateapp.data.model.TaskTemplate

@Dao
interface TaskTemplateDao {

    // ✅ BASIC CRUD OPERATIONS
    @Insert
    suspend fun insert(template: TaskTemplate): Long

    @Update
    suspend fun update(template: TaskTemplate)

    @Delete
    suspend fun delete(template: TaskTemplate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(template: TaskTemplate): Long

    // ✅ QUERY OPERATIONS
    @Query("SELECT * FROM TaskTemplate WHERE User_ID = :userId ORDER BY UsageCount DESC, CreatedAt DESC")
    suspend fun getAllTemplates(userId: Int): List<TaskTemplate>

    @Query("SELECT * FROM TaskTemplate WHERE User_ID = :userId ORDER BY UsageCount DESC, CreatedAt DESC")
    fun getAllTemplatesLive(userId: Int): LiveData<List<TaskTemplate>>

    @Query("SELECT * FROM TaskTemplate WHERE User_ID = :userId AND Template_ID = :templateId")
    suspend fun getTemplateById(userId: Int, templateId: Int): TaskTemplate?

    @Query("SELECT * FROM TaskTemplate WHERE User_ID = :userId AND Category = :category ORDER BY UsageCount DESC")
    suspend fun getTemplatesByCategory(userId: Int, category: String): List<TaskTemplate>

    @Query("SELECT * FROM TaskTemplate WHERE User_ID = :userId AND Name LIKE '%' || :searchQuery || '%' ORDER BY UsageCount DESC")
    suspend fun searchTemplates(userId: Int, searchQuery: String): List<TaskTemplate>

    // ✅ UPDATE OPERATIONS
    @Query("UPDATE TaskTemplate SET UsageCount = UsageCount + 1, UpdatedAt = :updatedAt WHERE User_ID = :userId AND Template_ID = :templateId")
    suspend fun incrementUsageCount(userId: Int, templateId: Int, updatedAt: Long = System.currentTimeMillis())

    // ✅ COUNT OPERATIONS
    @Query("SELECT COUNT(*) FROM TaskTemplate WHERE User_ID = :userId")
    suspend fun getTemplateCount(userId: Int): Int

    @Query("SELECT COUNT(*) FROM TaskTemplate WHERE User_ID = :userId AND Category = :category")
    suspend fun getTemplateCountByCategory(userId: Int, category: String): Int

    @Query("SELECT DISTINCT Category FROM TaskTemplate WHERE User_ID = :userId AND Category IS NOT NULL ORDER BY Category")
    suspend fun getAllCategories(userId: Int): List<String>

    // ✅ DELETE OPERATIONS
    @Query("DELETE FROM TaskTemplate WHERE User_ID = :userId AND Template_ID = :templateId")
    suspend fun deleteById(userId: Int, templateId: Int)

    // ✅ GET NEXT ID
    @Query("SELECT COALESCE(MAX(Template_ID), 0) + 1 FROM TaskTemplate WHERE User_ID = :userId")
    suspend fun getNextTemplateId(userId: Int): Int

    // ✅ BATCH OPERATIONS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<TaskTemplate>)
}
