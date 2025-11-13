package com.example.cheermateapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cheermateapp.data.StaticDataRepository
import com.example.cheermateapp.data.db.AppDb
import com.example.cheermateapp.data.model.Personality
import com.example.cheermateapp.data.model.PersonalityType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PersonalityActivity : AppCompatActivity() {
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private lateinit var staticDataRepository: StaticDataRepository
    private var personalityTypes: List<PersonalityType> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personality)

        // Initialize repository
        staticDataRepository = StaticDataRepository(this)

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

        // Load personality types from database with cache
        loadPersonalityTypes(userId)
    }
    
    private fun loadPersonalityTypes(userId: Int) {
        uiScope.launch {
            try {
                // Fetch personality types from repository (with caching)
                personalityTypes = withContext(Dispatchers.IO) {
                    staticDataRepository.getPersonalityTypes()
                }
                
                if (personalityTypes.isEmpty()) {
                    Toast.makeText(this@PersonalityActivity, "No personality types available", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }
                
                Log.d("PersonalityActivity", "Loaded ${personalityTypes.size} personality types")
                
                // Set up click listeners for each personality type
                setupPersonalityButtons(userId)
                
            } catch (e: Exception) {
                Log.e("PersonalityActivity", "Error loading personality types", e)
                Toast.makeText(this@PersonalityActivity, "Error loading personality types", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    private fun setupPersonalityButtons(userId: Int) {
        val saveAndGo: (PersonalityType) -> Unit = { personalityType ->
            Log.d("PersonalityActivity", "Saving personality: ${personalityType.Name}, userId=$userId")

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
                            PersonalityType = personalityType.Type_ID,
                            Name = personalityType.Name,
                            Description = personalityType.Description
                        )
                        Log.d("PersonalityActivity", "Created personality object: $personality")

                        db.personalityDao().upsert(personality)
                        Log.d("PersonalityActivity", "Personality saved successfully")
                        
                        // Get the saved personality to get its ID
                        val savedPersonality = db.personalityDao().getByUser(userId)
                        if (savedPersonality != null) {
                            // Update User.Personality_ID to link the user to their personality
                            db.userDao().updatePersonality(userId, savedPersonality.Personality_ID)
                            Log.d("PersonalityActivity", "User.Personality_ID updated to ${savedPersonality.Personality_ID}")
                        }
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

        // Map personality types to buttons dynamically
        // Assuming Type_ID matches: 1=Kalog, 2=GenZ, 3=Softy, 4=Grey, 5=Flirty
        val buttonMap = mapOf(
            R.id.cardKalog to 1,
            R.id.cardGenZ to 2,
            R.id.cardSofty to 3,
            R.id.cardGrey to 4,
            R.id.cardFlirty to 5
        )
        
        buttonMap.forEach { (buttonId, typeId) ->
            val personalityType = personalityTypes.find { it.Type_ID == typeId }
            if (personalityType != null) {
                findViewById<View>(buttonId)?.setOnClickListener {
                    saveAndGo(personalityType)
                }
            } else {
                // Hide button if personality type not found
                findViewById<View>(buttonId)?.visibility = View.GONE
            }
        }
    }
}
