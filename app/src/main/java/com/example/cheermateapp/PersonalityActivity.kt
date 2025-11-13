package com.example.cheermateapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cheermateapp.data.db.AppDb
import com.example.cheermateapp.data.model.Personality
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PersonalityActivity : AppCompatActivity() {
    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personality)

        // FIXED: Get as Int, not String
        val userId = intent.getIntExtra("USER_ID", -1)
        Log.d("PersonalityActivity", "Received USER_ID: $userId")

        if (userId == -1) {
            Log.e("PersonalityActivity", "USER_ID is invalid!")
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Toast.makeText(this, "Select your personality", Toast.LENGTH_SHORT).show()

        val saveAndGo: (Int, String, String) -> Unit = { type, name, description ->
            Log.d("PersonalityActivity", "Saving personality: type=$type, name=$name, userId=$userId")

            // Disable buttons to prevent double-clicks
            findViewById<View>(R.id.cardKalog)?.isEnabled = false
            findViewById<View>(R.id.cardGenZ)?.isEnabled = false
            findViewById<View>(R.id.cardSofty)?.isEnabled = false
            findViewById<View>(R.id.cardGrey)?.isEnabled = false
            findViewById<View>(R.id.cardFlirty)?.isEnabled = false

            uiScope.launch {
                try {
                    Log.d("PersonalityActivity", "Starting database operation...")

                    withContext(Dispatchers.IO) {
                        val db = AppDb.get(this@PersonalityActivity)
                        Log.d("PersonalityActivity", "Got database instance")

                        // FIXED: Use Int userId, matching the foreign key type
                        val personality = Personality(
                            Personality_ID = 0,
                            User_ID = userId, // Use Int value, not String
                            PersonalityType = type,
                            Name = name,
                            Description = description
                        )
                        Log.d("PersonalityActivity", "Created personality object: $personality")

                        db.personalityDao().upsert(personality)
                        Log.d("PersonalityActivity", "Personality saved successfully")
                        
                        // Update User.Personality_ID with the PersonalityType (1-5)
                        db.userDao().updatePersonality(userId, type)
                        Log.d("PersonalityActivity", "User Personality_ID updated to: $type")
                    }

                    Log.d("PersonalityActivity", "Navigating to MainActivity...")
                    val intent = Intent(this@PersonalityActivity, MainActivity::class.java).apply {
                        putExtra(MainActivity.EXTRA_SHOW_DASHBOARD, true)
                        putExtra(MainActivity.EXTRA_USER_ID, userId.toString()) // Convert back to String for MainActivity
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                    startActivity(intent)
                    finish()

                } catch (e: Exception) {
                    Log.e("PersonalityActivity", "Error saving personality", e)
                    Toast.makeText(this@PersonalityActivity, "Error saving personality: ${e.message}", Toast.LENGTH_LONG).show()

                    // Re-enable buttons on error
                    findViewById<View>(R.id.cardKalog)?.isEnabled = true
                    findViewById<View>(R.id.cardGenZ)?.isEnabled = true
                    findViewById<View>(R.id.cardSofty)?.isEnabled = true
                    findViewById<View>(R.id.cardGrey)?.isEnabled = true
                    findViewById<View>(R.id.cardFlirty)?.isEnabled = true
                }
            }
        }

        // Personality selection buttons
        findViewById<View>(R.id.cardKalog)?.setOnClickListener {
            saveAndGo(1, "Kalog", "The funny friend who makes everything entertaining!")
        }
        findViewById<View>(R.id.cardGenZ)?.setOnClickListener {
            saveAndGo(2, "Gen Z", "Tech-savvy and trendy with the latest slang!")
        }
        findViewById<View>(R.id.cardSofty)?.setOnClickListener {
            saveAndGo(3, "Softy", "Gentle and caring with a warm heart!")
        }
        findViewById<View>(R.id.cardGrey)?.setOnClickListener {
            saveAndGo(4, "Grey", "Calm and balanced with steady wisdom!")
        }
        findViewById<View>(R.id.cardFlirty)?.setOnClickListener {
            saveAndGo(5, "Flirty", "Playful and charming with a wink!")
        }
    }
}
