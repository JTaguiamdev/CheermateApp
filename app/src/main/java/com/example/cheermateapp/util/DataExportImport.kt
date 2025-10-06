package com.example.cheermateapp.util

import android.content.Context
import com.example.cheermateapp.data.model.Task
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for exporting and importing tasks
 */
object DataExportImport {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    /**
     * Export tasks to JSON format
     */
    fun exportToJson(tasks: List<Task>): String {
        return gson.toJson(tasks)
    }

    /**
     * Import tasks from JSON format
     */
    fun importFromJson(json: String): List<Task>? {
        return try {
            val tasks = gson.fromJson(json, Array<Task>::class.java)
            tasks?.toList()
        } catch (e: Exception) {
            android.util.Log.e("DataExportImport", "Error importing JSON", e)
            null
        }
    }

    /**
     * Export tasks to CSV format
     */
    fun exportToCsv(tasks: List<Task>): String {
        val csv = StringBuilder()
        
        // CSV Header
        csv.append("Task_ID,User_ID,Title,Description,Priority,Status,DueAt,DueTime,TaskProgress,CreatedAt,UpdatedAt\n")
        
        // CSV Rows
        tasks.forEach { task ->
            csv.append("${task.Task_ID},")
            csv.append("${task.User_ID},")
            csv.append("\"${escapeCsv(task.Title)}\",")
            csv.append("\"${escapeCsv(task.Description ?: "")}\",")
            csv.append("${task.Priority},")
            csv.append("${task.Status},")
            csv.append("\"${task.DueAt ?: ""}\",")
            csv.append("\"${task.DueTime ?: ""}\",")
            csv.append("${task.TaskProgress},")
            csv.append("${task.CreatedAt},")
            csv.append("${task.UpdatedAt}\n")
        }
        
        return csv.toString()
    }

    /**
     * Escape CSV special characters
     */
    private fun escapeCsv(text: String): String {
        return text.replace("\"", "\"\"")
    }

    /**
     * Write data to file
     */
    fun writeToFile(context: Context, filename: String, data: String): File? {
        return try {
            val file = File(context.getExternalFilesDir(null), filename)
            FileWriter(file).use { writer ->
                writer.write(data)
            }
            file
        } catch (e: Exception) {
            android.util.Log.e("DataExportImport", "Error writing to file", e)
            null
        }
    }

    /**
     * Read data from file
     */
    fun readFromFile(file: File): String? {
        return try {
            file.readText()
        } catch (e: Exception) {
            android.util.Log.e("DataExportImport", "Error reading from file", e)
            null
        }
    }

    /**
     * Generate backup filename with timestamp
     */
    fun generateBackupFilename(format: String = "json"): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "cheermate_backup_$timestamp.$format"
    }

    /**
     * Create a backup of all tasks
     */
    suspend fun createBackup(context: Context, tasks: List<Task>, format: String = "json"): File? {
        val data = when (format.lowercase()) {
            "json" -> exportToJson(tasks)
            "csv" -> exportToCsv(tasks)
            else -> return null
        }
        
        val filename = generateBackupFilename(format)
        return writeToFile(context, filename, data)
    }

    /**
     * Restore tasks from backup file
     */
    fun restoreFromBackup(file: File, format: String = "json"): List<Task>? {
        val data = readFromFile(file) ?: return null
        
        return when (format.lowercase()) {
            "json" -> importFromJson(data)
            else -> null // CSV import not implemented for restore
        }
    }
}
