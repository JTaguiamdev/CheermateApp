package com.example.cheermateapp.examples

import android.content.Context
import android.util.Log
import com.example.cheermateapp.data.db.AppDb
import com.example.cheermateapp.data.repository.MessageTemplateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Example: How to use the Personality System in your activities
 * 
 * This example demonstrates:
 * 1. Getting the user's personality ID
 * 2. Retrieving personality-based messages
 * 3. Displaying messages in different scenarios
 */
object PersonalitySystemExample {
    
    private const val TAG = "PersonalityExample"
    
    /**
     * Example 1: Get and display a task creation message
     */
    fun showTaskCreatedMessage(context: Context, userId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Step 1: Get user's personality ID
                val db = AppDb.get(context)
                val user = db.userDao().getById(userId)
                val personalityId = user?.Personality_ID
                
                if (personalityId != null) {
                    // Step 2: Get message from repository
                    val messageRepo = MessageTemplateRepository(context)
                    val message = messageRepo.getTaskCreatedMessage(personalityId)
                    
                    // Step 3: Display message
                    Log.d(TAG, "Task Created Message: $message")
                    
                    // In a real app, you would show this in a Toast, Snackbar, or TextView
                    // Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                } else {
                    Log.w(TAG, "User has no personality selected")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting task created message", e)
            }
        }
    }
    
    /**
     * Example 2: Get and display a task completion message
     */
    fun showTaskCompletedMessage(context: Context, userId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val db = AppDb.get(context)
                val user = db.userDao().getById(userId)
                val personalityId = user?.Personality_ID
                
                if (personalityId != null) {
                    val messageRepo = MessageTemplateRepository(context)
                    val message = messageRepo.getTaskCompletedMessage(personalityId)
                    
                    Log.d(TAG, "Task Completed Message: $message")
                    // Display in your UI
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting task completed message", e)
            }
        }
    }
    
    /**
     * Example 3: Get a motivational message for the user
     */
    fun showMotivationalMessage(context: Context, userId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val db = AppDb.get(context)
                val user = db.userDao().getById(userId)
                val personalityId = user?.Personality_ID
                
                if (personalityId != null) {
                    val messageRepo = MessageTemplateRepository(context)
                    val message = messageRepo.getMotivationalMessage(personalityId)
                    
                    Log.d(TAG, "Motivational Message: $message")
                    // Display in your UI
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting motivational message", e)
            }
        }
    }
    
    /**
     * Example 4: Get all messages for a personality
     */
    fun getAllMessagesForPersonality(context: Context, personalityId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val messageRepo = MessageTemplateRepository(context)
                
                // Get all message types
                val taskCreated = messageRepo.getTaskCreatedMessage(personalityId)
                val taskCompleted = messageRepo.getTaskCompletedMessage(personalityId)
                val motivation = messageRepo.getMotivationalMessage(personalityId)
                val reminder = messageRepo.getReminderMessage(personalityId)
                
                Log.d(TAG, "=== Messages for Personality $personalityId ===")
                Log.d(TAG, "Task Created: $taskCreated")
                Log.d(TAG, "Task Completed: $taskCompleted")
                Log.d(TAG, "Motivation: $motivation")
                Log.d(TAG, "Reminder: $reminder")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error getting messages", e)
            }
        }
    }
    
    /**
     * Example 5: Get personality-specific message by category
     */
    fun getMessageByCategory(context: Context, userId: Int, category: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val db = AppDb.get(context)
                val user = db.userDao().getById(userId)
                val personalityId = user?.Personality_ID
                
                if (personalityId != null) {
                    val messageRepo = MessageTemplateRepository(context)
                    val message = messageRepo.getMessageText(personalityId, category)
                    
                    Log.d(TAG, "Message for category '$category': $message")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting message by category", e)
            }
        }
    }
    
    /**
     * Example 6: Demo all personalities and their message styles
     */
    fun demoAllPersonalities(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val messageRepo = MessageTemplateRepository(context)
                
                val personalities = mapOf(
                    1 to "Kalog Vibes",
                    2 to "GenZ Conyo",
                    3 to "Softy Bebe",
                    4 to "Mr. Grey",
                    5 to "Flirty Vibes"
                )
                
                personalities.forEach { (id, name) ->
                    Log.d(TAG, "\n=== $name (ID: $id) ===")
                    Log.d(TAG, "Task Created: ${messageRepo.getTaskCreatedMessage(id)}")
                    Log.d(TAG, "Task Completed: ${messageRepo.getTaskCompletedMessage(id)}")
                    Log.d(TAG, "Motivation: ${messageRepo.getMotivationalMessage(id)}")
                    Log.d(TAG, "Reminder: ${messageRepo.getReminderMessage(id)}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error demoing personalities", e)
            }
        }
    }
}

/**
 * Usage in an Activity:
 * 
 * class TaskActivity : AppCompatActivity() {
 *     private var userId: Int = -1
 *     
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContentView(R.layout.activity_task)
 *         
 *         userId = intent.getIntExtra("USER_ID", -1)
 *         
 *         // When user creates a task
 *         btnCreateTask.setOnClickListener {
 *             createTask()
 *             PersonalitySystemExample.showTaskCreatedMessage(this, userId)
 *         }
 *         
 *         // When user completes a task
 *         btnCompleteTask.setOnClickListener {
 *             completeTask()
 *             PersonalitySystemExample.showTaskCompletedMessage(this, userId)
 *         }
 *         
 *         // Show motivational message on app open
 *         PersonalitySystemExample.showMotivationalMessage(this, userId)
 *     }
 * }
 */
