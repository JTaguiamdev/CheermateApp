package com.cheermateapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cheermateapp.databinding.ActivityLoginBinding
import com.cheermateapp.data.db.AppDb
import com.cheermateapp.data.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.cheermateapp.data.model.Personality


import com.cheermateapp.util.PermissionManager
import androidx.activity.result.contract.ActivityResultContracts

class ActivityLogin : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val uiScope = CoroutineScope(Dispatchers.Main)

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Notification permission granted, now check for exact alarm permission.
                PermissionManager.checkAndRequestExactAlarmPermission(this)
            } else {
                Toast.makeText(this, "Notifications are recommended for reminders.", Toast.LENGTH_LONG).show()
            }
        }

    private fun requestPermissionsIfNecessary() {
        if (PermissionManager.checkAndRequestNotificationPermission(this, requestPermissionLauncher)) {
            // If notification permission is already granted, check for exact alarm permission too.
            PermissionManager.checkAndRequestExactAlarmPermission(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.cheermateapp.util.ThemeManager.initializeTheme(this)

        try {
            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            // Request permissions on launch
            requestPermissionsIfNecessary()

            // Set initial state
            binding.PasswordHash.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
            binding.passwordLayout.isEndIconVisible = false

            binding.PasswordHash.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    binding.passwordLayout.isEndIconVisible = s?.isNotEmpty() == true
                }
            })

            binding.passwordLayout.setEndIconOnClickListener {
                if (binding.PasswordHash.transformationMethod == null) {
                    binding.PasswordHash.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
                    binding.passwordLayout.setEndIconDrawable(R.drawable.ic_visibility_off)
                } else {
                    binding.PasswordHash.transformationMethod = null
                    binding.passwordLayout.setEndIconDrawable(R.drawable.ic_visibility_on)
                }
                binding.PasswordHash.setSelection(binding.PasswordHash.text?.length ?: 0)
            }

            // Login button click handler with database validation
            binding.btnLogin.setOnClickListener {
                val username = binding.UserID.text?.toString()?.trim().orEmpty()
                val password = binding.PasswordHash.text?.toString()?.trim().orEmpty()

                var hasError = false
                if (username.isEmpty()) {
                    binding.UserID.error = "Please enter username"
                    hasError = true
                }

                if (password.isEmpty()) {
                    Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                    hasError = true
                }

                if (hasError) {
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
                                            val personality: Personality? = withContext(Dispatchers.IO) {
                                                db.personalityDao().getByUser(user.User_ID)
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
                                                    putExtra("USER_ID", user.User_ID)
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
                try {
                    val intent = Intent(this, ForgotPasswordActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    android.util.Log.e("ActivityLogin", "Error opening ForgotPasswordActivity", e)
                    Toast.makeText(this, "Unable to open forgot password screen", Toast.LENGTH_SHORT).show()
                }
            }

            // Sign up TextView click handler
            binding.signUpLine.setOnClickListener {
                try {
                    val intent = Intent(this, SignUpActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    android.util.Log.e("ActivityLogin", "Error opening SignUpActivity", e)
                    Toast.makeText(this, "Unable to open sign up screen", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error loading login screen: ${e.message}", Toast.LENGTH_LONG).show()
            android.util.Log.e("ActivityLogin", "Error in onCreate", e)
        }
    }
}
