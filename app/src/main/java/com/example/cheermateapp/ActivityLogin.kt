package com.example.cheermateapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cheermateapp.databinding.ActivityLoginBinding
import com.example.cheermateapp.data.db.AppDb
import com.example.cheermateapp.data.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivityLogin : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Login button click handler with database validation
            binding.btnLogin.setOnClickListener {
                val username = binding.UserID.text?.toString()?.trim().orEmpty()
                val password = binding.PasswordHash.text?.toString()?.trim().orEmpty()

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Show loading state
                binding.btnLogin.isEnabled = false
                binding.btnLogin.text = "Logging in..."

                uiScope.launch {
                    try {
                        val db = AppDb.get(this@ActivityLogin)
                        val repo = AuthRepository(db)

                        withContext(Dispatchers.IO) {
                            repo.validateCredentials(username, password) { user ->
                                uiScope.launch {
                                    // Reset button state
                                    binding.btnLogin.isEnabled = true
                                    binding.btnLogin.text = "Login"

                                    if (user != null) {
                                        // Check if user has selected a personality
                                        uiScope.launch {
                                            val personality = withContext(Dispatchers.IO) {
                                                db.personalityDao().getByUser(user.User_ID.toString())
                                            }

                                            if (personality != null) {
                                                // User has personality, go to dashboard
                                                Toast.makeText(this@ActivityLogin, "Welcome back!", Toast.LENGTH_SHORT).show()
                                                val intent = Intent(this@ActivityLogin, MainActivity::class.java).apply {
                                                    putExtra(MainActivity.EXTRA_SHOW_DASHBOARD, true)
                                                    putExtra(MainActivity.EXTRA_USER_ID, user.User_ID.toString())
                                                }
                                                startActivity(intent)
                                                finish()
                                            } else {
                                                // User needs to select personality first
                                                Toast.makeText(this@ActivityLogin, "Please select your personality", Toast.LENGTH_SHORT).show()
                                                val intent = Intent(this@ActivityLogin, PersonalityActivity::class.java).apply {
                                                    putExtra("USER_ID", user.User_ID.toString())
                                                }
                                                startActivity(intent)
                                                finish()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(this@ActivityLogin, "Invalid username or password", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Reset button state on error
                        binding.btnLogin.isEnabled = true
                        binding.btnLogin.text = "Login"

                        android.util.Log.e("ActivityLogin", "Database error during login", e)
                        Toast.makeText(this@ActivityLogin, "Login error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }

            // Forgot password TextView click handler
            binding.forgotPassword.setOnClickListener {
                Toast.makeText(this, "Forgot password clicked", Toast.LENGTH_SHORT).show()
                try {
                    val intent = Intent(this, ForgotPasswordActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Forgot password coming soon!", Toast.LENGTH_SHORT).show()
                }
            }

            // Sign up TextView click handler
            binding.signUpLine.setOnClickListener {
                Toast.makeText(this, "Sign up clicked", Toast.LENGTH_SHORT).show()
                try {
                    val intent = Intent(this, SignUpActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Sign up coming soon!", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error loading login screen: ${e.message}", Toast.LENGTH_LONG).show()
            android.util.Log.e("ActivityLogin", "Error in onCreate", e)
        }
    }
}
