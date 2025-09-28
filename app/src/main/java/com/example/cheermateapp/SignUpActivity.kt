package com.example.cheermateapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cheermateapp.databinding.ActivitySignUpBinding
import com.example.cheermateapp.data.db.AppDb
import com.example.cheermateapp.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivitySignUpBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Setup security question dropdown
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

            binding.btnSignUp.setOnClickListener {
                val fullName = binding.etFullName?.text?.toString()?.trim().orEmpty()
                val username = binding.etUsername?.text?.toString()?.trim().orEmpty()
                val password = binding.etPassword?.text?.toString()?.trim().orEmpty()
                val securityQuestion = binding.etSecurityQuestion?.text?.toString()?.trim().orEmpty()
                val securityAnswer = binding.etSecurityAnswer?.text?.toString()?.trim().orEmpty()

                // Validation checks
                if (fullName.isEmpty() || username.isEmpty() || password.length < 6) {
                    Toast.makeText(this, "Please complete all fields (password 6+ chars)", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (securityQuestion.isEmpty() || securityAnswer.isEmpty()) {
                    Toast.makeText(this, "Please select a security question and provide an answer", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (securityAnswer.length < 2) {
                    Toast.makeText(this, "Security answer must be at least 2 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Show loading state
                binding.btnSignUp.isEnabled = false
                binding.btnSignUp.text = "Creating Account..."

                uiScope.launch {
                    try {
                        val userId = withContext(Dispatchers.IO) {
                            val db = AppDb.get(this@SignUpActivity)

                            val firstName = fullName.substringBefore(' ').ifEmpty { fullName }
                            val lastName = fullName.substringAfter(' ', missingDelimiterValue = "")

                            val user = User(
                                User_ID = 0, // Auto-generated
                                Username = username,
                                Email = "$username@example.com",
                                PasswordHash = password,
                                FirstName = firstName,
                                LastName = lastName,
                                Birthdate = null,
                                Personality_ID = null
                            )

                            db.userDao().insert(user) // Returns Long (the actual ID)
                        }

                        Toast.makeText(this@SignUpActivity, "Account created successfully!", Toast.LENGTH_SHORT).show()

                        // FIXED: Pass the numeric User_ID, not username
                        val intent = Intent(this@SignUpActivity, PersonalityActivity::class.java).apply {
                            putExtra("USER_ID", userId.toInt()) // Use actual database ID
                        }
                        startActivity(intent)
                        finish()

                    } catch (e: Exception) {
                        binding.btnSignUp.isEnabled = true
                        binding.btnSignUp.text = "Sign Up"
                        Toast.makeText(this@SignUpActivity, "Sign up failed: ${e.message}", Toast.LENGTH_LONG).show()
                        android.util.Log.e("SignUpActivity", "Sign up error", e)
                    }
                }
            }

            // Forgot password link
            binding.tvForgot?.setOnClickListener {
                startActivity(Intent(this, ForgotPasswordActivity::class.java))
            }

            // Login link
            binding.tvLoginNow?.setOnClickListener {
                startActivity(Intent(this, ActivityLogin::class.java))
                finish()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "SignUpActivity - Coming Soon!", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
