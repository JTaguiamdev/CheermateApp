package com.cheermateapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cheermateapp.data.StaticDataRepository
import com.cheermateapp.databinding.ActivityForgotPasswordBinding
import com.cheermateapp.data.db.AppDb
import com.cheermateapp.util.PasswordHashUtil
import com.cheermateapp.util.InputValidationUtil
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity for password recovery using security questions
 */
class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private lateinit var staticDataRepository: StaticDataRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Initialize repository
            staticDataRepository = StaticDataRepository(this)

            setupSecurityQuestionDropdown()
            setupVerifyButton()

        } catch (e: Exception) {
            Toast.makeText(this, "Error loading password recovery", Toast.LENGTH_LONG).show()
            android.util.Log.e("ForgotPasswordActivity", "Error in onCreate", e)
            finish()
        }
    }

    /**
     * Set up the security question dropdown with questions from database (cached)
     */
    private fun setupSecurityQuestionDropdown() {
        uiScope.launch {
            try {
                // Fetch security questions from repository (with caching)
                val securityQuestions = withContext(Dispatchers.IO) {
                    staticDataRepository.getSecurityQuestionPrompts()
                }
                
                if (securityQuestions.isEmpty()) {
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "No security questions available. Please contact support.",
                        Toast.LENGTH_LONG
                    ).show()
                    android.util.Log.e("ForgotPasswordActivity", "No security questions found")
                    return@launch
                }
                
                val adapter = ArrayAdapter(
                    this@ForgotPasswordActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    securityQuestions
                )
                binding.etSecurityQuestion?.setAdapter(adapter)
                binding.etSecurityQuestion?.threshold = 0
                
                android.util.Log.d("ForgotPasswordActivity", "Loaded ${securityQuestions.size} security questions")
            } catch (e: Exception) {
                android.util.Log.e("ForgotPasswordActivity", "Error loading security questions", e)
                Toast.makeText(
                    this@ForgotPasswordActivity,
                    "Error loading security questions",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Set up the verify button to handle password recovery
     */
    private fun setupVerifyButton() {
        binding.btnContinue?.setOnClickListener {
            val username = binding.etUsername?.text?.toString()?.trim().orEmpty()
            val securityQuestion = binding.etSecurityQuestion?.text?.toString()?.trim().orEmpty()
            val securityAnswer = binding.etSecurityAnswer?.text?.toString()?.trim().orEmpty()

            // Validate inputs
            if (!validateInputs(username, securityQuestion, securityAnswer)) {
                return@setOnClickListener
            }

            // Show loading state
            binding.btnContinue.isEnabled = false
            binding.btnContinue.text = "Verifying..."

            // Verify security answer and reset password
            verifyAndResetPassword(username, securityQuestion, securityAnswer)
        }
    }

    /**
     * Validate user inputs
     */
    private fun validateInputs(username: String, securityQuestion: String, securityAnswer: String): Boolean {
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter your username", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!InputValidationUtil.isValidUsername(username)) {
            Toast.makeText(this, "Invalid username format", Toast.LENGTH_SHORT).show()
            return false
        }

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
     * Verify security answer and prompt for new password
     */
    private fun verifyAndResetPassword(username: String, securityQuestion: String, securityAnswer: String) {
        uiScope.launch {
            try {
                val isValid = withContext(Dispatchers.IO) {
                    val db = AppDb.get(this@ForgotPasswordActivity)
                    val user = db.userDao().findByUsername(username)

                    if (user == null) {
                        return@withContext false
                    }

                    // Get all security questions
                    val questions = db.securityDao().getAllQuestions()
                    val questionId = questions.find { it.Prompt == securityQuestion }?.SecurityQuestion_ID

                    if (questionId == null) {
                        return@withContext false
                    }

                    // Get user's security answers
                    val userAnswers = db.securityDao().getUserAnswers(user.User_ID)
                    val savedAnswer = userAnswers.find { it.Question_ID == questionId }

                    if (savedAnswer == null) {
                        return@withContext false
                    }

                    // Verify the answer using PBKDF2-HMAC-SHA256 (same as password verification)
                    PasswordHashUtil.verifyPassword(securityAnswer, savedAnswer.AnswerHash)
                }

                binding.btnContinue.isEnabled = true
                binding.btnContinue.text = "Continue"

                if (isValid) {
                    // Show password reset dialog
                    showPasswordResetDialog(username)
                } else {
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "Invalid username, security question, or answer",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                binding.btnContinue.isEnabled = true
                binding.btnContinue.text = "Continue"
                Toast.makeText(
                    this@ForgotPasswordActivity,
                    "Error verifying security answer: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                android.util.Log.e("ForgotPasswordActivity", "Error verifying", e)
            }
        }
    }

    /**
     * Show dialog to enter new password
     */
    private fun showPasswordResetDialog(username: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reset_password, null)
        val etNewPassword = dialogView.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<TextInputEditText>(R.id.etConfirmPassword)

        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setMessage("Enter your new password")
            .setView(dialogView)
            .setPositiveButton("Reset") { _, _ ->
                val newPassword = etNewPassword.text?.toString()?.trim().orEmpty()
                val confirmPassword = etConfirmPassword.text?.toString()?.trim().orEmpty()

                if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(this, "Please fill in both password fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword != confirmPassword) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (!InputValidationUtil.isValidPassword(newPassword)) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Reset the password
                resetPassword(username, newPassword)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Reset the user's password in the database
     */
    private fun resetPassword(username: String, newPassword: String) {
        uiScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val db = AppDb.get(this@ForgotPasswordActivity)
                    val user = db.userDao().findByUsername(username)

                    if (user != null) {
                        // Hash the new password
                        val hashedPassword = PasswordHashUtil.hashPassword(newPassword)
                        
                        // Update user with new password
                        val updatedUser = user.copy(
                            PasswordHash = hashedPassword,
                            UpdatedAt = System.currentTimeMillis()
                        )
                        db.userDao().update(updatedUser)
                    }
                }

                Toast.makeText(this@ForgotPasswordActivity, "Password reset successfully!", Toast.LENGTH_SHORT).show()
                
                // Navigate to login
                startActivity(Intent(this@ForgotPasswordActivity, ActivityLogin::class.java))
                finish()

            } catch (e: Exception) {
                Toast.makeText(
                    this@ForgotPasswordActivity,
                    "Error resetting password: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                android.util.Log.e("ForgotPasswordActivity", "Error resetting password", e)
            }
        }
    }
}
