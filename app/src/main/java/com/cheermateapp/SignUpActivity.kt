package com.cheermateapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cheermateapp.data.StaticDataRepository
import com.cheermateapp.databinding.ActivitySignUpBinding
import com.cheermateapp.data.db.AppDb
import com.cheermateapp.data.model.User
import com.cheermateapp.data.model.SecurityQuestion
import com.cheermateapp.data.model.UserSecurityAnswer
import com.cheermateapp.util.PasswordHashUtil
import com.cheermateapp.util.InputValidationUtil
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
    private lateinit var staticDataRepository: StaticDataRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.cheermateapp.util.ThemeManager.initializeTheme(this)

        try {
            binding = ActivitySignUpBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Set initial state
            binding.etPassword.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
            binding.tilPassword.isEndIconVisible = false

            binding.etPassword.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    binding.tilPassword.isEndIconVisible = s?.isNotEmpty() == true
                }
            })

            binding.tilPassword.setEndIconOnClickListener {
                if (binding.etPassword.transformationMethod == null) {
                    binding.etPassword.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
                    binding.tilPassword.setEndIconDrawable(R.drawable.ic_visibility_off)
                } else {
                    binding.etPassword.transformationMethod = null
                    binding.tilPassword.setEndIconDrawable(R.drawable.ic_visibility_on)
                }
                binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
            }

            // Initialize repository
            staticDataRepository = StaticDataRepository(this)

            setupSecurityQuestionDropdown()
            setupSignUpButton()
            setupNavigationLinks()
            setupKeyboardAwareScroll()

        } catch (e: Exception) {
            Toast.makeText(this, "Error loading sign up screen", Toast.LENGTH_LONG).show()
            android.util.Log.e("SignUpActivity", "Error in onCreate", e)
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
                        this@SignUpActivity,
                        "No security questions available. Please try again later.",
                        Toast.LENGTH_LONG
                    ).show()
                    android.util.Log.e("SignUpActivity", "No security questions found")
                    return@launch
                }
                
                val adapter = ArrayAdapter(
                    this@SignUpActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    securityQuestions
                )
                binding.etSecurityQuestion?.setAdapter(adapter)
                binding.etSecurityQuestion?.threshold = 0
                
                android.util.Log.d("SignUpActivity", "Loaded ${securityQuestions.size} security questions")
            } catch (e: Exception) {
                android.util.Log.e("SignUpActivity", "Error loading security questions", e)
                Toast.makeText(
                    this@SignUpActivity,
                    "Error loading security questions",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Set up the sign up button with validation and user creation
     */
    private fun setupSignUpButton() {
        binding.btnSignUp.setOnClickListener {
            val firstName = binding.etFirstName?.text?.toString()?.trim().orEmpty()
            val lastName = binding.etLastName?.text?.toString()?.trim().orEmpty()
            val username = binding.etUsername?.text?.toString()?.trim().orEmpty()
            val password = binding.etPassword?.text?.toString()?.trim().orEmpty()
            val securityQuestion = binding.etSecurityQuestion?.text?.toString()?.trim().orEmpty()
            val securityAnswer = binding.etSecurityAnswer?.text?.toString()?.trim().orEmpty()

            // Validate all inputs
            if (!validateInputs(firstName, lastName, username, password, securityQuestion, securityAnswer)) {
                return@setOnClickListener
            }

            // Show loading state
            binding.btnSignUp.isEnabled = false
            binding.btnSignUp.text = "Creating Account..."

            // Create user account
            createUserAccount(firstName, lastName, username, password, securityQuestion, securityAnswer)
        }
    }

    private fun setupKeyboardAwareScroll() {
        // Animate the ScrollView itself when the security answer field is focused/edited
        android.util.Log.d("SignUpActivity", "setupKeyboardAwareScroll called; etSecurityAnswer=${binding.etSecurityAnswer}")
        val securityAnswer = binding.etSecurityAnswer ?: return
        val scrollView = findViewById<android.widget.ScrollView>(R.id.signUpScrollView) ?: return

        fun animateScrollToField(trigger: String) {
            scrollView.post {
                val targetY = (securityAnswer.bottom + 80).coerceAtLeast(0)
                android.util.Log.d(
                    "SignUpActivity",
                    "animateScrollToField from=$trigger currentY=${scrollView.scrollY} targetY=$targetY"
                )
                android.animation.ObjectAnimator.ofInt(
                    scrollView,
                    "scrollY",
                    scrollView.scrollY,
                    targetY
                ).apply {
                    duration = 250
                    interpolator = android.view.animation.DecelerateInterpolator()
                }.start()
            }
        }

        securityAnswer.setOnFocusChangeListener { _, hasFocus ->
            android.util.Log.d("SignUpActivity", "etSecurityAnswer focus changed: hasFocus=$hasFocus")
            if (hasFocus) {
                animateScrollToField("focus")
            }
        }

        securityAnswer.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (securityAnswer.isFocused) {
                    android.util.Log.d("SignUpActivity", "afterTextChanged on etSecurityAnswer; length=${s?.length ?: 0}")
                    animateScrollToField("textChanged")
                }
            }
        })
    }

    /**
     * Validate all user inputs
     */
    private fun validateInputs(
        firstName: String,
        lastName: String,
        username: String,
        password: String,
        securityQuestion: String,
        securityAnswer: String
    ): Boolean {
        var hasError = false
        if (firstName.isEmpty()) {
            binding.etFirstName?.error = "Please enter your first name"
            hasError = true
        }

        if (lastName.isEmpty()) {
            binding.etLastName?.error = "Please enter your last name"
            hasError = true
        }

        if (username.isEmpty()) {
            binding.etUsername?.error = "Please enter a username"
            hasError = true
        } else if (!InputValidationUtil.isValidUsername(username)) {
            binding.etUsername?.error = "Username must be 3-20 characters, letters, numbers, and underscore only"
            hasError = true
        } else if (InputValidationUtil.containsSQLInjectionPattern(username)) {
            binding.etUsername?.error = "Invalid characters in username"
            hasError = true
        }

        if (!InputValidationUtil.isValidPassword(password)) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            hasError = true
        }

        if (securityQuestion.isEmpty()) {
            Toast.makeText(this, "Please select a security question", Toast.LENGTH_SHORT).show()
            hasError = true
        }

        if (securityAnswer.length < 2) {
            binding.etSecurityAnswer?.error = "Security answer must be at least 2 characters"
            hasError = true
        }

        return !hasError
    }

    /**
     * Create user account with hashed password and security answer
     */
    private fun createUserAccount(
        firstName: String,
        lastName: String,
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

                    // Hash the password using PBKDF2-HMAC-SHA256
                    val hashedPassword = PasswordHashUtil.hashPassword(password)

                    // Create user
                    val user = User(
                        User_ID = 0, // Auto-generated
                        Username = username,
                        Email = "$username@example.com", // Can be enhanced to accept real email
                        PasswordHash = hashedPassword,
                        FirstName = firstName,
                        LastName = lastName,
                        Personality_ID = null
                    )

                    val newUserId = db.userDao().insert(user)

                    // Save security answer
                    val questions = db.securityDao().getAllQuestions()
                    val questionId = questions.find { it.Prompt == securityQuestion }?.Question_ID

                    questionId?.let {
                        // Hash the security answer using PBKDF2-HMAC-SHA256 (same as password)
                        val hashedAnswer = PasswordHashUtil.hashPassword(securityAnswer)
                        val userAnswer = UserSecurityAnswer(
                            Answer_ID = 0,
                            User_ID = newUserId.toInt(),
                            Question_ID = it,
                            AnswerHash = hashedAnswer
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
