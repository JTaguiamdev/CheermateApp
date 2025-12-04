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
                            IsActive = true
                        ),
                        Personality(
                            Personality_ID = 2,
                            Name = "Gen Z",
                            Description = "Tech-savvy and trendy with the latest slang!",
                            IsActive = true
                        ),
                        Personality(
                            Personality_ID = 3,
                            Name = "Softy",
                            Description = "Gentle and caring with a warm heart!",
                            IsActive = true
                        ),
                        Personality(
                            Personality_ID = 4,
                            Name = "Grey",
                            Description = "Calm and balanced with steady wisdom!",
                            IsActive = true
                        ),
                        Personality(
                            Personality_ID = 5,
                            Name = "Flirty",
                            Description = "Playful and charming with a wink!",
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
                    // Multiple motivation messages for variety
                    messageTemplates.addAll(listOf(
                        MessageTemplate(Personality_ID = 1, Category = "motivation", TextTemplate = "Hala sige! Kaya mo yan, besh! 🎉"),
                        MessageTemplate(Personality_ID = 1, Category = "motivation", TextTemplate = "Ayos lang yan! Isa-isa lang! 😄"),
                        MessageTemplate(Personality_ID = 1, Category = "motivation", TextTemplate = "Go lang! Ikaw pa ba? 💪"),
                        MessageTemplate(Personality_ID = 1, Category = "motivation", TextTemplate = "Laban lang! Walang impossible! 🌟"),
                        MessageTemplate(Personality_ID = 1, Category = "motivation", TextTemplate = "Kaya mo yan! Easy-peasy! 🎊"),
                        MessageTemplate(Personality_ID = 1, Category = "task_work", TextTemplate = "Work mode ON! Get this bread! 💼😄"),
                        MessageTemplate(Personality_ID = 1, Category = "task_personal", TextTemplate = "Treat yourself after, besh! 🏆"),
                        MessageTemplate(Personality_ID = 1, Category = "task_shopping", TextTemplate = "Budget wisely ha! 🛍️😂"),
                        MessageTemplate(Personality_ID = 1, Category = "task_others", TextTemplate = "Busy bee ka today! Kaya mo! 🐝"),
                        MessageTemplate(Personality_ID = 1, Category = "task_completed", TextTemplate = "Yown! Bili na tayo ice cream! 🍦✅")
                    ))
                    
                    // Gen Z (ID=2) - Trendy and tech-savvy messages
                    messageTemplates.addAll(listOf(
                        MessageTemplate(Personality_ID = 2, Category = "motivation", TextTemplate = "No cap, you're slaying today! 💯"),
                        MessageTemplate(Personality_ID = 2, Category = "motivation", TextTemplate = "Main character energy! Let's go! ✨"),
                        MessageTemplate(Personality_ID = 2, Category = "motivation", TextTemplate = "You're so valid bestie! 🔥"),
                        MessageTemplate(Personality_ID = 2, Category = "motivation", TextTemplate = "Period! You got this! 💅"),
                        MessageTemplate(Personality_ID = 2, Category = "motivation", TextTemplate = "It's giving boss vibes! 👑"),
                        MessageTemplate(Personality_ID = 2, Category = "task_work", TextTemplate = "Work vibes! You're lit! 💼🔥"),
                        MessageTemplate(Personality_ID = 2, Category = "task_personal", TextTemplate = "Self-care check! Periodt! 💅"),
                        MessageTemplate(Personality_ID = 2, Category = "task_shopping", TextTemplate = "Living your best life! 🛍️💸"),
                        MessageTemplate(Personality_ID = 2, Category = "task_others", TextTemplate = "Another W incoming! 🔥"),
                        MessageTemplate(Personality_ID = 2, Category = "task_completed", TextTemplate = "So proud of you, babe! 💖✨")
                    ))
                    
                    // Softy (ID=3) - Gentle and caring messages
                    messageTemplates.addAll(listOf(
                        MessageTemplate(Personality_ID = 3, Category = "motivation", TextTemplate = "One gentle step at a time! 🌸"),
                        MessageTemplate(Personality_ID = 3, Category = "motivation", TextTemplate = "I believe in you, dear! 💕"),
                        MessageTemplate(Personality_ID = 3, Category = "motivation", TextTemplate = "You're doing amazing! 🌺"),
                        MessageTemplate(Personality_ID = 3, Category = "motivation", TextTemplate = "Take a deep breath, you got this! 🤗"),
                        MessageTemplate(Personality_ID = 3, Category = "motivation", TextTemplate = "So proud of your efforts! 💝"),
                        MessageTemplate(Personality_ID = 3, Category = "task_work", TextTemplate = "I'm here supporting you! 💼💕"),
                        MessageTemplate(Personality_ID = 3, Category = "task_personal", TextTemplate = "Self-care time! You deserve it! 🤗"),
                        MessageTemplate(Personality_ID = 3, Category = "task_shopping", TextTemplate = "Treat yourself kindly! 🛍️💝"),
                        MessageTemplate(Personality_ID = 3, Category = "task_others", TextTemplate = "Every task is a small victory! 🌺"),
                        MessageTemplate(Personality_ID = 3, Category = "task_completed", TextTemplate = "So proud of you! Amazing job! 💕✨")
                    ))
                    
                    // Grey (ID=4) - Professional and balanced messages
                    messageTemplates.addAll(listOf(
                        MessageTemplate(Personality_ID = 4, Category = "motivation", TextTemplate = "Steady progress. Focus. Execute. ⚖️"),
                        MessageTemplate(Personality_ID = 4, Category = "motivation", TextTemplate = "Discipline leads to success. 📊"),
                        MessageTemplate(Personality_ID = 4, Category = "motivation", TextTemplate = "One task at a time. 🎯"),
                        MessageTemplate(Personality_ID = 4, Category = "motivation", TextTemplate = "Your focus is commendable. 🧘"),
                        MessageTemplate(Personality_ID = 4, Category = "motivation", TextTemplate = "Execute efficiently. Time is key. ⏰"),
                        MessageTemplate(Personality_ID = 4, Category = "task_work", TextTemplate = "Work requires discipline. Focus. 💼"),
                        MessageTemplate(Personality_ID = 4, Category = "task_personal", TextTemplate = "Proceed thoughtfully. 📚"),
                        MessageTemplate(Personality_ID = 4, Category = "task_shopping", TextTemplate = "Balance necessity with desire. 🛍️"),
                        MessageTemplate(Personality_ID = 4, Category = "task_others", TextTemplate = "Execute efficiently. Time matters. ⏰"),
                        MessageTemplate(Personality_ID = 4, Category = "task_completed", TextTemplate = "Excellent work. Well done. ✅")
                    ))
                    
                    // Flirty (ID=5) - Playful and charming messages
                    messageTemplates.addAll(listOf(
                        MessageTemplate(Personality_ID = 5, Category = "motivation", TextTemplate = "Hey gorgeous! You got this! 😉"),
                        MessageTemplate(Personality_ID = 5, Category = "motivation", TextTemplate = "Looking good while crushing it! 💋"),
                        MessageTemplate(Personality_ID = 5, Category = "motivation", TextTemplate = "You're absolutely amazing! 😍"),
                        MessageTemplate(Personality_ID = 5, Category = "motivation", TextTemplate = "Charm those tasks, beautiful! ✨"),
                        MessageTemplate(Personality_ID = 5, Category = "motivation", TextTemplate = "You're irresistible! Keep going! 💕"),
                        MessageTemplate(Personality_ID = 5, Category = "task_work", TextTemplate = "You make productivity look sexy! 💼😍"),
                        MessageTemplate(Personality_ID = 5, Category = "task_personal", TextTemplate = "Self-love looks good on you! 💝"),
                        MessageTemplate(Personality_ID = 5, Category = "task_shopping", TextTemplate = "You'll look stunning! 🛍️✨"),
                        MessageTemplate(Personality_ID = 5, Category = "task_others", TextTemplate = "Handle it with grace! 😘"),
                        MessageTemplate(Personality_ID = 5, Category = "task_completed", TextTemplate = "Look at you go! Crushing it! 😍💋")
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
