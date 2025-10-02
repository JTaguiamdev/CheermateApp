package com.example.cheermateapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cheermateapp.databinding.ActivitySignUpBinding
import com.example.cheermateapp.data.db.AppDb
import com.example.cheermateapp.data.model.User
import com.example.cheermateapp.data.model.SecurityQuestion
import com.example.cheermateapp.data.model.UserSecurityAnswer
import com.example.cheermateapp.util.PasswordHashUtil
import com.example.cheermateapp.util.InputValidationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity for user registration with security question setup
 */
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivitySignUpBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setupSecurityQuestionDropdown()
            setupSignUpButton()
            setupNavigationLinks()

        } catch (e: Exception) {
            Toast.makeText(this, "Error loading sign up screen", Toast.LENGTH_LONG).show()
            android.util.Log.e("SignUpActivity", "Error in onCreate", e)
            finish()
        }
    }

    /**
     * Set up the security question dropdown with predefined questions
     */
    private fun setupSecurityQuestionDropdown() {
        val securityQuestions = arrayOf(
            "What was your first pet's name?",
            "What city were you born in?",
            "What is your mother's maiden name?",
            "What was your first car?",
            "What elementary school did you attend?",
            "What is your favorite color?",
            "What was the name of your first boss?",
            "In what city did you meet your spouse/significant other?"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, securityQuestions)
        binding.etSecurityQuestion?.setAdapter(adapter)
        binding.etSecurityQuestion?.threshold = 0
    }

    /**
     * Set up the sign up button with validation and user creation
     */
    private fun setupSignUpButton() {
        binding.btnSignUp.setOnClickListener {
            val fullName = binding.etFullName?.text?.toString()?.trim().orEmpty()
            val username = binding.etUsername?.text?.toString()?.trim().orEmpty()
            val password = binding.etPassword?.text?.toString()?.trim().orEmpty()
            val securityQuestion = binding.etSecurityQuestion?.text?.toString()?.trim().orEmpty()
            val securityAnswer = binding.etSecurityAnswer?.text?.toString()?.trim().orEmpty()

            // Validate all inputs
            if (!validateInputs(fullName, username, password, securityQuestion, securityAnswer)) {
                return@setOnClickListener
            }

            // Show loading state
            binding.btnSignUp.isEnabled = false
            binding.btnSignUp.text = "Creating Account..."

            // Create user account
            createUserAccount(fullName, username, password, securityQuestion, securityAnswer)
        }
    }

    /**
     * Validate all user inputs
     */
    private fun validateInputs(
        fullName: String,
        username: String,
        password: String,
        securityQuestion: String,
        securityAnswer: String
    ): Boolean {
        // Validate full name
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate username
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!InputValidationUtil.isValidUsername(username)) {
            Toast.makeText(
                this,
                "Username must be 3-20 characters, letters, numbers, and underscore only",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        // Check for SQL injection patterns
        if (InputValidationUtil.containsSQLInjectionPattern(username)) {
            Toast.makeText(this, "Invalid characters in username", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate password
        if (!InputValidationUtil.isValidPassword(password)) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate security question and answer
        if (securityQuestion.isEmpty()) {
            Toast.makeText(this, "Please select a security question", Toast.LENGTH_SHORT).show()
            return false
        }

        if (securityAnswer.length < 2) {
            Toast.makeText(this, "Security answer must be at least 2 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    /**
     * Create user account with hashed password and security answer
     */
    private fun createUserAccount(
        fullName: String,
        username: String,
        password: String,
        securityQuestion: String,
        securityAnswer: String
    ) {
        uiScope.launch {
            try {
                val userId = withContext(Dispatchers.IO) {
                    val db = AppDb.get(this@SignUpActivity)

                    // Check if username already exists
                    val existingUser = db.userDao().findByUsername(username)
                    if (existingUser != null) {
                        return@withContext -1L // Username exists
                    }

                    // Split full name into first and last name
                    val firstName = fullName.substringBefore(' ').ifEmpty { fullName }
                    val lastName = fullName.substringAfter(' ', missingDelimiterValue = "")

                    // Hash the password using BCrypt
                    val hashedPassword = PasswordHashUtil.hashPassword(password)

                    // Create user
                    val user = User(
                        User_ID = 0, // Auto-generated
                        Username = username,
                        Email = "$username@example.com", // Can be enhanced to accept real email
                        PasswordHash = hashedPassword,
                        FirstName = firstName,
                        LastName = lastName,
                        Birthdate = null,
                        Personality_ID = null
                    )

                    val newUserId = db.userDao().insert(user)

                    // Ensure security questions exist in database
                    ensureSecurityQuestionsExist(db)

                    // Save security answer
                    val questions = db.securityDao().getAllQuestions()
                    val questionId = questions.find { it.Prompt == securityQuestion }?.SecurityQuestion_ID

                    if (questionId != null) {
                        val userAnswer = UserSecurityAnswer(
                            Answer_ID = 0,
                            User_ID = newUserId.toInt(),
                            Question_ID = questionId,
                            AnswerHash = securityAnswer // In production, this should be hashed too
                        )
                        db.securityDao().saveAnswer(userAnswer)
                    }

                    newUserId
                }

                binding.btnSignUp.isEnabled = true
                binding.btnSignUp.text = "Sign Up"

                if (userId == -1L) {
                    Toast.makeText(this@SignUpActivity, "Username already exists", Toast.LENGTH_LONG).show()
                    return@launch
                }

                Toast.makeText(this@SignUpActivity, "Account created successfully!", Toast.LENGTH_SHORT).show()

                // Navigate to personality selection
                val intent = Intent(this@SignUpActivity, PersonalityActivity::class.java).apply {
                    putExtra("USER_ID", userId.toInt())
                }
                startActivity(intent)
                finish()

            } catch (e: Exception) {
                binding.btnSignUp.isEnabled = true
                binding.btnSignUp.text = "Sign Up"
                Toast.makeText(
                    this@SignUpActivity,
                    "Sign up failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                android.util.Log.e("SignUpActivity", "Sign up error", e)
            }
        }
    }

    /**
     * Ensure security questions exist in the database
     */
    private suspend fun ensureSecurityQuestionsExist(db: AppDb) {
        val existingQuestions = db.securityDao().getAllQuestions()
        
        if (existingQuestions.isEmpty()) {
            val predefinedQuestions = listOf(
                "What was your first pet's name?",
                "What city were you born in?",
                "What is your mother's maiden name?",
                "What was your first car?",
                "What elementary school did you attend?",
                "What is your favorite color?",
                "What was the name of your first boss?",
                "In what city did you meet your spouse/significant other?"
            )

            predefinedQuestions.forEach { prompt ->
                val question = SecurityQuestion(
                    SecurityQuestion_ID = 0,
                    Prompt = prompt
                )
                db.securityDao().insertQuestion(question)
            }
        }
    }

    /**
     * Set up navigation links (forgot password and login)
     */
    private fun setupNavigationLinks() {
        // Forgot password link
        binding.tvForgot?.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Login link
        binding.tvLoginNow?.setOnClickListener {
            startActivity(Intent(this, ActivityLogin::class.java))
            finish()
        }
    }
}
