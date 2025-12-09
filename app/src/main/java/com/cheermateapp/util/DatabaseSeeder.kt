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
                            Description = "The funny friend who makes everything entertaining!"
                        ),
                        Personality(
                            Personality_ID = 2,
                            Name = "Gen Z",
                            Description = "Tech-savvy and trendy with the latest slang!"
                        ),
                        Personality(
                            Personality_ID = 3,
                            Name = "Softy",
                            Description = "Gentle and caring with a warm heart!"
                        ),
                        Personality(
                            Personality_ID = 4,
                            Name = "Grey",
                            Description = "Calm and balanced with steady wisdom!"
                        ),
                        Personality(
                            Personality_ID = 5,
                            Name = "Flirty",
                            Description = "Playful and charming with a wink!"
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
                            Prompt = "What was your first pet's name?"
                        ),
                        SecurityQuestion(
                            Prompt = "What city were you born in?"
                        ),
                        SecurityQuestion(
                            Prompt = "What is your mother's maiden name?"
                        ),
                        SecurityQuestion(
                            Prompt = "What was your first car?"
                        ),
                        SecurityQuestion(
                            Prompt = "What elementary school did you attend?"
                        ),
                        SecurityQuestion(
                            Prompt = "What is your favorite color?"
                        ),
                        SecurityQuestion(
                            Prompt = "What was the name of your first boss?"
                        ),
                        SecurityQuestion(
                            Prompt = "In what city did you meet your spouse/significant other?"
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
                    val messageTemplates = listOf(
                        // Kalog (ID 1)
                        MessageTemplate(Personality_ID = 1, Category = "motivation", MessageText = "Hala sige! Time to turn that task list into a comedy show! 🎉"),
                        MessageTemplate(Personality_ID = 1, Category = "motivation", MessageText = "Another day, another opportunity to shine!"),
                        MessageTemplate(Personality_ID = 1, Category = "motivation", MessageText = "You've got this!"),
                        MessageTemplate(Personality_ID = 1, Category = "motivation", MessageText = "Believe in yourself and all that you are."),
                        MessageTemplate(Personality_ID = 1, Category = "motivation", MessageText = "Let's get this bread... and maybe some cookies too. 🍪"),
                        MessageTemplate(Personality_ID = 1, Category = "motivation", MessageText = "Go on, show 'em what you're made of! (It's star stuff, probably.)"),
                        MessageTemplate(Personality_ID = 1, Category = "motivation", MessageText = "You're not just a snack, you're the whole picnic!"),
                        MessageTemplate(Personality_ID = 1, Category = "motivation", MessageText = "Time to make some magic happen, or at least a good sandwich."),
                        MessageTemplate(Personality_ID = 1, Category = "motivation", MessageText = "Is it a bird? Is it a plane? No, it's you, being awesome!"),
                        MessageTemplate(Personality_ID = 1, Category = "motivation", MessageText = "Keep your chin up, unless you're trying to find loose change."),
                        MessageTemplate(Personality_ID = 1, Category = "motivation", MessageText = "You're doing great, sweetie! - Kris Jenner, probably."),
                        MessageTemplate(Personality_ID = 1, Category = "motivation", MessageText = "Don't stop get it, get it!"),
                        MessageTemplate(Personality_ID = 1, Category = "motivation", MessageText = "Remember, even the smollest bean can make a big difference."),
                        MessageTemplate(Personality_ID = 1, Category = "motivation", MessageText = "You're on fire! (Metaphorically, please don't actually be on fire.)"),
                        MessageTemplate(Personality_ID = 1, Category = "motivation", MessageText = "It's a good day to have a good day!"),
                        MessageTemplate(Personality_ID = 1, Category = "motivation", MessageText = "Wacky waving inflatable arm-flailing tube man has nothing on your energy!"),
                        MessageTemplate(Personality_ID = 1, Category = "motivation", MessageText = "Let's do the thing!"),
                        MessageTemplate(Personality_ID = 1, Category = "motivation", MessageText = "Sending you good vibes and virtual high-fives!"),
                        MessageTemplate(Personality_ID = 1, Category = "motivation", MessageText = "You're a star! A slightly weird, but lovable star."),
                        MessageTemplate(Personality_ID = 1, Category = "motivation", MessageText = "Hakuna Matata! It means no worries, and more snacks!"),

                        // Gen Z (ID 2)
                        MessageTemplate(Personality_ID = 2, Category = "motivation", MessageText = "Vibe check: You're killing it!"),
                        MessageTemplate(Personality_ID = 2, Category = "motivation", MessageText = "Slay the day!"),
                        MessageTemplate(Personality_ID = 2, Category = "motivation", MessageText = "No cap, you're doing amazing."),
                        MessageTemplate(Personality_ID = 2, Category = "motivation", MessageText = "It's giving 'main character energy'. ✨"),
                        MessageTemplate(Personality_ID = 2, Category = "motivation", MessageText = "You understood the assignment. Period."),
                        MessageTemplate(Personality_ID = 2, Category = "motivation", MessageText = "Let's get this bread. 🍞"),
                        MessageTemplate(Personality_ID = 2, Category = "motivation", MessageText = "Manifesting success for you rn."),
                        MessageTemplate(Personality_ID = 2, Category = "motivation", MessageText = "That's the spirit. We move."),
                        MessageTemplate(Personality_ID = 2, Category = "motivation", MessageText = "Low-key, you're a legend."),
                        MessageTemplate(Personality_ID = 2, Category = "motivation", MessageText = "Bet. You got this."),
                        MessageTemplate(Personality_ID = 2, Category = "motivation", MessageText = "Keep that same energy."),
                        MessageTemplate(Personality_ID = 2, Category = "motivation", MessageText = "The glow up is real."),
                        MessageTemplate(Personality_ID = 2, Category = "motivation", MessageText = "Living for this focus!"),
                        MessageTemplate(Personality_ID = 2, Category = "motivation", MessageText = "Don't let anyone dull your sparkle. ✨"),
                        MessageTemplate(Personality_ID = 2, Category = "motivation", MessageText = "You're the CEO of getting things done."),
                        MessageTemplate(Personality_ID = 2, Category = "motivation", MessageText = "This is your world, we're just living in it."),
                        MessageTemplate(Personality_ID = 2, Category = "motivation", MessageText = "Let's see that progress, bestie."),
                        MessageTemplate(Personality_ID = 2, Category = "motivation", MessageText = "It's the dedication for me."),
                        MessageTemplate(Personality_ID = 2, Category = "motivation", MessageText = "You're literally crushing it."),
                        MessageTemplate(Personality_ID = 2, Category = "motivation", MessageText = "Say less, do more. You got it."),

                        // Softy (ID 3)
                        MessageTemplate(Personality_ID = 3, Category = "motivation", MessageText = "You are capable of amazing things."),
                        MessageTemplate(Personality_ID = 3, Category = "motivation", MessageText = "Your kindness is a superpower."),
                        MessageTemplate(Personality_ID = 3, Category = "motivation", MessageText = "Sending you a big virtual hug!"),
                        MessageTemplate(Personality_ID = 3, Category = "motivation", MessageText = "It's okay to take a break, little one. 🌸"),
                        MessageTemplate(Personality_ID = 3, Category = "motivation", MessageText = "Believe in your heart that you're meant to live a life full of passion."),
                        MessageTemplate(Personality_ID = 3, Category = "motivation", MessageText = "You are doing your best, and that is enough."),
                        MessageTemplate(Personality_ID = 3, Category = "motivation", MessageText = "Be gentle with yourself. You're doing great."),
                        MessageTemplate(Personality_ID = 3, Category = "motivation", MessageText = "There is a quiet strength in your gentle heart."),
                        MessageTemplate(Personality_ID = 3, Category = "motivation", MessageText = "You are a ray of sunshine on a cloudy day. ☀️"),
                        MessageTemplate(Personality_ID = 3, Category = "motivation", MessageText = "Don't forget to be kind to yourself today."),
                        MessageTemplate(Personality_ID = 3, Category = "motivation", MessageText = "Your efforts are blossoming beautifully. 🌷"),
                        MessageTemplate(Personality_ID = 3, Category = "motivation", MessageText = "Small steps every day lead to big results."),
                        MessageTemplate(Personality_ID = 3, Category = "motivation", MessageText = "You have a heart of gold. Don't ever forget it."),
                        MessageTemplate(Personality_ID = 3, Category = "motivation", MessageText = "Wishing you a day as lovely as you are."),
                        MessageTemplate(Personality_ID = 3, Category = "motivation", MessageText = "You're like a cozy blanket on a chilly day."),
                        MessageTemplate(Personality_ID = 3, Category = "motivation", MessageText = "Just a little reminder that you are loved."),
                        MessageTemplate(Personality_ID = 3, Category = "motivation", MessageText = "The world is a better place with you in it."),
                        MessageTemplate(Personality_ID = 3, Category = "motivation", MessageText = "Take a deep breath. You're going to be okay."),
                        MessageTemplate(Personality_ID = 3, Category = "motivation", MessageText = "Your gentle spirit can move mountains."),
                        MessageTemplate(Personality_ID = 3, Category = "motivation", MessageText = "You are enough, just as you are."),

                        // Grey (ID 4)
                        MessageTemplate(Personality_ID = 4, Category = "motivation", MessageText = "Just one step at a time."),
                        MessageTemplate(Personality_ID = 4, Category = "motivation", MessageText = "Progress, not perfection."),
                        MessageTemplate(Personality_ID = 4, Category = "motivation", MessageText = "It is what it is, but you can handle it."),
                        MessageTemplate(Personality_ID = 4, Category = "motivation", MessageText = "The task is before you. Proceed."),
                        MessageTemplate(Personality_ID = 4, Category = "motivation", MessageText = "Focus on the objective."),
                        MessageTemplate(Personality_ID = 4, Category = "motivation", MessageText = "Efficiency is key."),
                        MessageTemplate(Personality_ID = 4, Category = "motivation", MessageText = "Acknowledge the goal. Execute the plan."),
                        MessageTemplate(Personality_ID = 4, Category = "motivation", MessageText = "Emotion is a distraction. Focus."),
                        MessageTemplate(Personality_ID = 4, Category = "motivation", MessageText = "The path is clear. Walk it."),
                        MessageTemplate(Personality_ID = 4, Category = "motivation", MessageText = "Another task, another completion."),
                        MessageTemplate(Personality_ID = 4, Category = "motivation", MessageText = "Maintain equilibrium. Continue forward."),
                        MessageTemplate(Personality_ID = 4, Category = "motivation", MessageText = "It is logical to proceed."),
                        MessageTemplate(Personality_ID = 4, Category = "motivation", MessageText = "Your determination is a valuable asset."),
                        MessageTemplate(Personality_ID = 4, Category = "motivation", MessageText = "The outcome is dependent on your input."),
                        MessageTemplate(Personality_ID = 4, Category = "motivation", MessageText = "Execute with precision."),
                        MessageTemplate(Personality_ID = 4, Category = "motivation", MessageText = "The situation is under your control."),
                        MessageTemplate(Personality_ID = 4, Category = "motivation", MessageText = "Analyze, act, achieve."),
                        MessageTemplate(Personality_ID = 4, Category = "motivation", MessageText = "Your potential is a measurable quantity."),
                        MessageTemplate(Personality_ID = 4, Category = "motivation", MessageText = "Continue on your current trajectory."),
                        MessageTemplate(Personality_ID = 4, Category = "motivation", MessageText = "Task completion is the only logical goal."),

                        // Flirty (ID 5)
                        MessageTemplate(Personality_ID = 5, Category = "motivation", MessageText = "Are you a magician? Because whenever I look at your to-do list, everything else disappears!"),
                        MessageTemplate(Personality_ID = 5, Category = "motivation", MessageText = "You're looking extra productive today ;)"),
                        MessageTemplate(Personality_ID = 5, Category = "motivation", MessageText = "Go get 'em, tiger!"),
                        MessageTemplate(Personality_ID = 5, Category = "motivation", MessageText = "Are you a parking ticket? Because you've got 'fine' written all over you."),
                        MessageTemplate(Personality_ID = 5, Category = "motivation", MessageText = "If being productive was a crime, you'd be guilty as charged."),
                        MessageTemplate(Personality_ID = 5, Category = "motivation", MessageText = "You're hotter than my laptop after a long day of work."),
                        MessageTemplate(Personality_ID = 5, Category = "motivation", MessageText = "I must be a snowflake, because I've fallen for you and your work ethic."),
                        MessageTemplate(Personality_ID = 5, Category = "motivation", MessageText = "Let's make like a fabric softener and snuggle up to these tasks."),
                        MessageTemplate(Personality_ID = 5, Category = "motivation", MessageText = "You handle your tasks like you handle my heart... with care and expertise."),
                        MessageTemplate(Personality_ID = 5, Category = "motivation", MessageText = "You're so good at this, it's almost not fair to the tasks."),
                        MessageTemplate(Personality_ID = 5, Category = "motivation", MessageText = "I'm not a photographer, but I can picture us... finishing this list together."),
                        MessageTemplate(Personality_ID = 5, Category = "motivation", MessageText = "Is your name Google? Because you've got everything I've been searching for in a hard worker."),
                        MessageTemplate(Personality_ID = 5, Category = "motivation", MessageText = "You must be tired from running through my mind all day... and being so productive."),
                        MessageTemplate(Personality_ID = 5, Category = "motivation", MessageText = "That's a nice to-do list. It would look even better with some checkmarks on it, don't you think?"),
                        MessageTemplate(Personality_ID = 5, Category = "motivation", MessageText = "You're the apple of my i... Phone. I mean, eye."),
                        MessageTemplate(Personality_ID = 5, Category = "motivation", MessageText = "Keep up the great work, gorgeous."),
                        MessageTemplate(Personality_ID = 5, Category = "motivation", MessageText = "I'm not staring, I'm just impressed."),
                        MessageTemplate(Personality_ID = 5, Category = "motivation", MessageText = "You're making my heart... and my productivity chart... go up."),
                        MessageTemplate(Personality_ID = 5, Category = "motivation", MessageText = "Let's tackle these tasks together. It's a date!"),
                        MessageTemplate(Personality_ID = 5, Category = "motivation", MessageText = "I believe in you almost as much as I believe in us.")
                    )
                    
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
