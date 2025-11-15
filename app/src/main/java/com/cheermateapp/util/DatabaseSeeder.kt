package com.cheermateapp.util

import android.content.Context
import android.util.Log
import com.cheermateapp.data.db.AppDb
import com.cheermateapp.data.model.MessageTemplate
import com.cheermateapp.data.model.Personality
import com.cheermateapp.data.model.SecurityQuestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utility class for seeding the database with default static data
 */
object DatabaseSeeder {
    
    private const val TAG = "DatabaseSeeder"
    
    /**
     * Seeds the database with default personalities if not already present
     */
    suspend fun seedPersonalities(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val db = AppDb.get(context)
                val count = db.personalityDao().getCount()
                
                if (count == 0) {
                    Log.d(TAG, "Seeding personalities...")
                    val personalities = listOf(
                        Personality(
                            Personality_ID = 1,
                            Name = "Kalog",
                            Description = "The funny friend who makes everything entertaining!",
                            MotivationMessage = "Hala! Time to turn that task list into a comedy show! Kaya mo yan, besh! 🎉",
                            IsActive = true
                        ),
                        Personality(
                            Personality_ID = 2,
                            Name = "Gen Z",
                            Description = "Tech-savvy and trendy with the latest slang!",
                            MotivationMessage = "No cap, you're about to absolutely slay these tasks! It's giving main character energy! 💯",
                            IsActive = true
                        ),
                        Personality(
                            Personality_ID = 3,
                            Name = "Softy",
                            Description = "Gentle and caring with a warm heart!",
                            MotivationMessage = "You've got this, take it one gentle step at a time. I believe in you! 🌸",
                            IsActive = true
                        ),
                        Personality(
                            Personality_ID = 4,
                            Name = "Grey",
                            Description = "Calm and balanced with steady wisdom!",
                            MotivationMessage = "Steady progress leads to lasting success. Focus on what matters. 🧘",
                            IsActive = true
                        ),
                        Personality(
                            Personality_ID = 5,
                            Name = "Flirty",
                            Description = "Playful and charming with a wink!",
                            MotivationMessage = "Hey gorgeous, ready to charm those tasks into submission? You're irresistible! 😉",
                            IsActive = true
                        )
                    )
                    
                    db.personalityDao().insertAll(personalities)
                    Log.d(TAG, "Successfully seeded ${personalities.size} personalities")
                } else {
                    Log.d(TAG, "Personalities already seeded ($count records)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error seeding personalities", e)
            }
        }
    }
    
    /**
     * Seeds the database with default security questions if not already present
     */
    suspend fun seedSecurityQuestions(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val db = AppDb.get(context)
                val count = db.securityDao().getQuestionCount()
                
                if (count == 0) {
                    Log.d(TAG, "Seeding security questions...")
                    val questions = listOf(
                        SecurityQuestion(
                            Prompt = "What was your first pet's name?",
                            IsActive = true
                        ),
                        SecurityQuestion(
                            Prompt = "What city were you born in?",
                            IsActive = true
                        ),
                        SecurityQuestion(
                            Prompt = "What is your mother's maiden name?",
                            IsActive = true
                        ),
                        SecurityQuestion(
                            Prompt = "What was your first car?",
                            IsActive = true
                        ),
                        SecurityQuestion(
                            Prompt = "What elementary school did you attend?",
                            IsActive = true
                        ),
                        SecurityQuestion(
                            Prompt = "What is your favorite color?",
                            IsActive = true
                        ),
                        SecurityQuestion(
                            Prompt = "What was the name of your first boss?",
                            IsActive = true
                        ),
                        SecurityQuestion(
                            Prompt = "In what city did you meet your spouse/significant other?",
                            IsActive = true
                        )
                    )
                    
                    db.securityDao().insertAllQuestions(questions)
                    Log.d(TAG, "Successfully seeded ${questions.size} security questions")
                } else {
                    Log.d(TAG, "Security questions already seeded ($count records)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error seeding security questions", e)
            }
        }
    }
    
    /**
     * Seeds the database with default message templates for each personality
     */
    suspend fun seedMessageTemplates(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val db = AppDb.get(context)
                val count = db.messageTemplateDao().getCount()
                
                if (count == 0) {
                    Log.d(TAG, "Seeding message templates...")
                    val messageTemplates = mutableListOf<MessageTemplate>()
                    
                    // Kalog (ID=1) - Funny and entertaining messages
                    messageTemplates.addAll(listOf(
                        MessageTemplate(Personality_ID = 1, Category = "motivation", TextTemplate = "Hala sige! Time to turn that task list into a comedy show! 🎉"),
                        MessageTemplate(Personality_ID = 1, Category = "task_work", TextTemplate = "Work mode ON! Let's get this bread, besh! 💼😄"),
                        MessageTemplate(Personality_ID = 1, Category = "task_personal", TextTemplate = "Personal task? Sige na, treat yourself after! 🏆"),
                        MessageTemplate(Personality_ID = 1, Category = "task_shopping", TextTemplate = "Shopping time! Budget wisely ha, baka lugi ka! 🛍️😂"),
                        MessageTemplate(Personality_ID = 1, Category = "task_others", TextTemplate = "Another task? Hala, busy bee yarn! Kaya mo yan! 🐝"),
                        MessageTemplate(Personality_ID = 1, Category = "task_completed", TextTemplate = "Yown! Tapos na yan! Bili tayo ice cream! 🍦✅")
                    ))
                    
                    // Gen Z (ID=2) - Trendy and tech-savvy messages
                    messageTemplates.addAll(listOf(
                        MessageTemplate(Personality_ID = 2, Category = "motivation", TextTemplate = "No cap, you're about to absolutely slay these tasks! 💯✨"),
                        MessageTemplate(Personality_ID = 2, Category = "task_work", TextTemplate = "Work tasks? It's giving main character energy! Let's go! 💼🔥"),
                        MessageTemplate(Personality_ID = 2, Category = "task_personal", TextTemplate = "Personal growth vibes! Periodt! You're doing amazing! 💅"),
                        MessageTemplate(Personality_ID = 2, Category = "task_shopping", TextTemplate = "Shopping spree? Living your best life! Just don't go broke! 🛍️💸"),
                        MessageTemplate(Personality_ID = 2, Category = "task_others", TextTemplate = "Another W incoming! You're on fire today! 🔥"),
                        MessageTemplate(Personality_ID = 2, Category = "task_completed", TextTemplate = "Yayyy! So proud of you, babe! You're lit AF! 💖✨")
                    ))
                    
                    // Softy (ID=3) - Gentle and caring messages
                    messageTemplates.addAll(listOf(
                        MessageTemplate(Personality_ID = 3, Category = "motivation", TextTemplate = "You've got this! Take it one gentle step at a time. 🌸"),
                        MessageTemplate(Personality_ID = 3, Category = "task_work", TextTemplate = "Work tasks can be peaceful. I'm here supporting you! 💼💕"),
                        MessageTemplate(Personality_ID = 3, Category = "task_personal", TextTemplate = "Personal time is self-care time. You deserve this! 🤗"),
                        MessageTemplate(Personality_ID = 3, Category = "task_shopping", TextTemplate = "Shopping can be therapeutic! Treat yourself kindly! 🛍️💝"),
                        MessageTemplate(Personality_ID = 3, Category = "task_others", TextTemplate = "Every task is a small victory. I believe in you! 🌺"),
                        MessageTemplate(Personality_ID = 3, Category = "task_completed", TextTemplate = "I'm so proud of you! You did an amazing job! 💕✨")
                    ))
                    
                    // Grey (ID=4) - Professional and balanced messages
                    messageTemplates.addAll(listOf(
                        MessageTemplate(Personality_ID = 4, Category = "motivation", TextTemplate = "Steady progress leads to lasting success. Focus. Execute. ⚖️"),
                        MessageTemplate(Personality_ID = 4, Category = "task_work", TextTemplate = "Work requires discipline. Your focus is commendable. 💼"),
                        MessageTemplate(Personality_ID = 4, Category = "task_personal", TextTemplate = "Personal development demands attention. Proceed thoughtfully. 📚"),
                        MessageTemplate(Personality_ID = 4, Category = "task_shopping", TextTemplate = "Shopping: Balance necessity with desire. Choose wisely. 🛍️"),
                        MessageTemplate(Personality_ID = 4, Category = "task_others", TextTemplate = "Every task matters. Time is valuable. Execute efficiently. ⏰"),
                        MessageTemplate(Personality_ID = 4, Category = "task_completed", TextTemplate = "Excellent work. Your discipline is commendable. ✅")
                    ))
                    
                    // Flirty (ID=5) - Playful and charming messages
                    messageTemplates.addAll(listOf(
                        MessageTemplate(Personality_ID = 5, Category = "motivation", TextTemplate = "Hey gorgeous, ready to charm those tasks into submission? 😉💋"),
                        MessageTemplate(Personality_ID = 5, Category = "task_work", TextTemplate = "Work tasks? You make productivity look sexy! 💼😍"),
                        MessageTemplate(Personality_ID = 5, Category = "task_personal", TextTemplate = "Personal time? You deserve all the self-love, beautiful! 💝"),
                        MessageTemplate(Personality_ID = 5, Category = "task_shopping", TextTemplate = "Shopping spree? You're going to look absolutely stunning! 🛍️✨"),
                        MessageTemplate(Personality_ID = 5, Category = "task_others", TextTemplate = "Another task? You handle it with such grace! 😘"),
                        MessageTemplate(Personality_ID = 5, Category = "task_completed", TextTemplate = "Look at you go! You're absolutely crushing it! 😍💋")
                    ))
                    
                    db.messageTemplateDao().insertAll(messageTemplates)
                    Log.d(TAG, "Successfully seeded ${messageTemplates.size} message templates")
                } else {
                    Log.d(TAG, "Message templates already seeded ($count records)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error seeding message templates", e)
            }
        }
    }
    
    /**
     * Seeds all default data
     */
    suspend fun seedAll(context: Context) {
        seedPersonalities(context)
        seedSecurityQuestions(context)
        seedMessageTemplates(context)
    }
}
