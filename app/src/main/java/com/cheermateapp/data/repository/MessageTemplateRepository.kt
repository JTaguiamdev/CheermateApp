package com.cheermateapp.data.repository

import android.content.Context
import com.cheermateapp.data.db.AppDb
import com.cheermateapp.data.model.MessageTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing message templates
 * Provides methods to retrieve personalized messages based on user's personality
 */
class MessageTemplateRepository(context: Context) {
    
    private val db = AppDb.get(context)
    
    /**
     * Get all message templates for a specific personality
     * @param personalityId The ID of the personality type (1-5)
     * @return List of message templates for that personality
     */
    suspend fun getMessagesByPersonality(personalityId: Int): List<MessageTemplate> {
        return withContext(Dispatchers.IO) {
            db.messageTemplateDao().getByPersonalityId(personalityId)
        }
    }
    
    /**
     * Get a message template for a specific personality and category
     * @param personalityId The ID of the personality type (1-5)
     * @param category The message category (e.g., "task_created", "task_completed", "motivation", "reminder")
     * @return A random message template from the matching templates, or null if none found
     */
    suspend fun getMessageByPersonalityAndCategory(
        personalityId: Int,
        category: String
    ): MessageTemplate? {
        return withContext(Dispatchers.IO) {
            val templates = db.messageTemplateDao().getByPersonalityAndCategory(personalityId, category)
            templates.randomOrNull()
        }
    }
    
    /**
     * Get the text of a message for a specific personality and category
     * @param personalityId The ID of the personality type (1-5)
     * @param category The message category
     * @return The message text, or a default message if none found
     */
    suspend fun getMessageText(personalityId: Int, category: String): String {
        return getMessageByPersonalityAndCategory(personalityId, category)?.TextTemplate
            ?: getDefaultMessage(category)
    }
    
    /**
     * Get all message templates for a specific category across all personalities
     * @param category The message category
     * @return List of message templates for that category
     */
    suspend fun getMessagesByCategory(category: String): List<MessageTemplate> {
        return withContext(Dispatchers.IO) {
            db.messageTemplateDao().getByCategory(category)
        }
    }
    
    /**
     * Get a random motivational message for the given personality
     * @param personalityId The ID of the personality type (1-5)
     * @return A motivational message text
     */
    suspend fun getMotivationalMessage(personalityId: Int): String {
        return getMessageText(personalityId, "motivation")
    }
    
    /**
     * Get a task created message for the given personality
     * @param personalityId The ID of the personality type (1-5)
     * @return A task created message text
     */
    suspend fun getTaskCreatedMessage(personalityId: Int): String {
        return getMessageText(personalityId, "task_created")
    }
    
    /**
     * Get a task completed message for the given personality
     * @param personalityId The ID of the personality type (1-5)
     * @return A task completed message text
     */
    suspend fun getTaskCompletedMessage(personalityId: Int): String {
        return getMessageText(personalityId, "task_completed")
    }
    
    /**
     * Get a reminder message for the given personality
     * @param personalityId The ID of the personality type (1-5)
     * @return A reminder message text
     */
    suspend fun getReminderMessage(personalityId: Int): String {
        return getMessageText(personalityId, "reminder")
    }
    
    /**
     * Get default messages when no personality-specific message is found
     */
    private fun getDefaultMessage(category: String): String {
        return when (category) {
            "task_created" -> "Task created successfully!"
            "task_completed" -> "Great job! Task completed!"
            "motivation" -> "You can do it! Keep going!"
            "reminder" -> "Don't forget about your task!"
            else -> "Keep up the good work!"
        }
    }
}
