package com.example.cheermateapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.cheermateapp.data.StaticDataRepository
import com.example.cheermateapp.data.db.AppDb
import com.example.cheermateapp.data.model.Personality
import com.example.cheermateapp.util.ThemeManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentSettingsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USER_ID = "USER_ID"
    }

    private var userId: Int = 0
    private lateinit var staticDataRepository: StaticDataRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_settings)

        // Get userId from intent
        userId = intent?.getStringExtra(EXTRA_USER_ID)?.toIntOrNull() ?: 0
        
        // Initialize repository
        staticDataRepository = StaticDataRepository(this)

        setupToolbar()
        setupBottomNavigation()
        loadSettingsUserData()
        setupSettingsInteractions()
    }

    private fun setupToolbar() {
        // Your layout doesn't have a toolbar, skip this or implement if needed
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    navigateToMain()
                    true
                }
                R.id.nav_tasks -> {
                    navigateToTasks()
                    true
                }
                R.id.nav_settings -> {
                    // Already in settings screen
                    true
                }
                else -> false
            }
        }
        bottomNav?.selectedItemId = R.id.nav_settings
    }

    private fun loadSettingsUserData() {
        if (userId != 0) {
            lifecycleScope.launch {
                try {
                    val db = AppDb.get(this@FragmentSettingsActivity)

                    val user = withContext(Dispatchers.IO) {
                        db.userDao().getById(userId)
                    }

                    val personality: Personality? = withContext(Dispatchers.IO) {
                        db.personalityDao().getByUser(userId)
                    }

                    // ✅ USE YOUR ACTUAL XML IDs
                    val tvProfileName = findViewById<TextView>(R.id.tvProfileName)
                    val tvProfileEmail = findViewById<TextView>(R.id.tvProfileEmail)
                    val tvCurrentPersona = findViewById<TextView>(R.id.tvCurrentPersona)
                    val chipPersona = findViewById<TextView>(R.id.chipPersona)

                    if (user != null) {
                        val displayName = when {
                            !user.FirstName.isNullOrBlank() && !user.LastName.isNullOrBlank() ->
                                "${user.FirstName} ${user.LastName}"
                            !user.FirstName.isNullOrBlank() -> user.FirstName
                            else -> user.Username
                        }

                        tvProfileName?.text = displayName ?: "User"
                        tvProfileEmail?.text = user.Email ?: "user@example.com"

                    } else {
                        tvProfileName?.text = "User"
                        tvProfileEmail?.text = "user@example.com"
                    }

                    if (personality != null) {
                        tvCurrentPersona?.text = personality.Name
                        chipPersona?.text = "${personality.Name} Personality"
                    } else {
                        tvCurrentPersona?.text = "Your Personality"
                        chipPersona?.text = "No Personality Selected"
                    }

                    // ✅ UPDATE TASK STATISTICS
                    loadUserTaskStatistics()

                } catch (e: Exception) {
                    android.util.Log.e("FragmentSettingsActivity", "Error loading user data", e)
                    Toast.makeText(this@FragmentSettingsActivity, "Error loading profile data", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            findViewById<TextView>(R.id.tvProfileName)?.text = "Guest"
            findViewById<TextView>(R.id.tvProfileEmail)?.text = "guest@example.com"
            findViewById<TextView>(R.id.tvCurrentPersona)?.text = "Guest Mode"
            findViewById<TextView>(R.id.chipPersona)?.text = "Guest User"
        }
    }

    private fun loadUserTaskStatistics() {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentSettingsActivity)

                val stats = withContext(Dispatchers.IO) {
                    val totalTasks = db.taskDao().getAllTasksCount(userId)
                    val completedTasks = db.taskDao().getCompletedTasksCount(userId)

                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        .format(java.util.Date())
                    val todayTasks = db.taskDao().getTodayTasksCount(userId, today)
                    val pendingTasks = db.taskDao().getPendingTasksCount(userId)

                    mapOf(
                        "total" to totalTasks,
                        "completed" to completedTasks,
                        "today" to todayTasks,
                        "pending" to pendingTasks
                    )
                }

                // ✅ UPDATE STATISTICS DISPLAY
                updateSettingsStatistics(stats)

            } catch (e: Exception) {
                android.util.Log.e("FragmentSettingsActivity", "Error loading task statistics", e)
            }
        }
    }

    // ✅ FIXED - MATCHES YOUR ACTUAL XML LAYOUT
    private fun updateSettingsStatistics(stats: Map<String, Int>) {
        try {
            // ✅ USE YOUR ACTUAL XML IDs FROM THE LAYOUT YOU SHOWED
            val tvStatTotal = findViewById<TextView>(R.id.tvStatTotal)
            val tvStatCompleted = findViewById<TextView>(R.id.tvStatCompleted)
            val tvStatSuccess = findViewById<TextView>(R.id.tvStatSuccess)

            var updatedCount = 0

            // Update Total Tasks
            if (tvStatTotal != null) {
                tvStatTotal.text = "${stats["total"]}"
                updatedCount++
                android.util.Log.d("FragmentSettingsActivity", "✅ Updated tvStatTotal: ${stats["total"]}")
            }

            // Update Completed Tasks
            if (tvStatCompleted != null) {
                tvStatCompleted.text = "${stats["completed"]}"
                updatedCount++
                android.util.Log.d("FragmentSettingsActivity", "✅ Updated tvStatCompleted: ${stats["completed"]}")
            }

            // Calculate and Update Success Rate
            if (tvStatSuccess != null) {
                val successRate = if (stats["total"]!! > 0) {
                    ((stats["completed"]!! * 100) / stats["total"]!!)
                } else {
                    0
                }
                tvStatSuccess.text = "$successRate%"
                updatedCount++
                android.util.Log.d("FragmentSettingsActivity", "✅ Updated tvStatSuccess: $successRate%")
            }

            // ✅ SHOW ADDITIONAL STATS IN TOAST
            showDetailedStatisticsToast(stats)

            android.util.Log.d("FragmentSettingsActivity", "✅ Updated $updatedCount statistics TextViews")

        } catch (e: Exception) {
            android.util.Log.e("FragmentSettingsActivity", "Error updating statistics display", e)
            showStatisticsInDialog(stats)
        }
    }

    // ✅ SHOW ADDITIONAL STATISTICS IN TOAST
    private fun showDetailedStatisticsToast(stats: Map<String, Int>) {
        val successRate = if (stats["total"]!! > 0) {
            ((stats["completed"]!! * 100) / stats["total"]!!)
        } else {
            0
        }

        val message = "📊 Total: ${stats["total"]} • Done: ${stats["completed"]} • Today: ${stats["today"]} • Pending: ${stats["pending"]} • Success: $successRate%"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun setupSettingsInteractions() {
        try {
            // ✅ USE YOUR ACTUAL XML IDs

            // Profile Card Click
            findViewById<LinearLayout>(R.id.cardProfile)?.setOnClickListener {
                showProfileEditDialog()
            }

            // AI Personality Row Click
            findViewById<LinearLayout>(R.id.rowPersonality)?.setOnClickListener {
                showPersonalitySelectionDialog()
            }

            // Stats Card Click
            findViewById<LinearLayout>(R.id.cardStats)?.setOnClickListener {
                showDetailedStatisticsDialog()
            }

            // Dark Mode Switch - Now with functional implementation
            val switchDarkMode = findViewById<Switch>(R.id.switchDarkMode)
            
            // Set initial state based on current theme
            switchDarkMode?.isChecked = ThemeManager.isDarkModeActive(this)
            
            switchDarkMode?.setOnCheckedChangeListener { _, isChecked ->
                val newMode = if (isChecked) ThemeManager.THEME_DARK else ThemeManager.THEME_LIGHT
                ThemeManager.setThemeMode(this, newMode)
                Toast.makeText(
                    this, 
                    if (isChecked) "🌙 Dark mode enabled" else "☀️ Light mode enabled", 
                    Toast.LENGTH_SHORT
                ).show()
                
                // Recreate activity to apply theme
                recreate()
            }

            // Notifications Row and Switch
            findViewById<LinearLayout>(R.id.rowNotifications)?.setOnClickListener {
                showNotificationSettings()
            }

            findViewById<Switch>(R.id.switchNotifications)?.setOnCheckedChangeListener { _, isChecked ->
                Toast.makeText(this, if (isChecked) "🔔 Notifications enabled" else "🔕 Notifications disabled", Toast.LENGTH_SHORT).show()
            }

            // Sign Out
            findViewById<LinearLayout>(R.id.cardSignOut)?.setOnClickListener {
                showLogoutConfirmation()
            }

            findViewById<TextView>(R.id.btnSignOut)?.setOnClickListener {
                showLogoutConfirmation()
            }

        } catch (e: Exception) {
            android.util.Log.e("FragmentSettingsActivity", "Error setting up interactions", e)
        }
    }

    private fun showDetailedStatisticsDialog() {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentSettingsActivity)

                val detailedStats = withContext(Dispatchers.IO) {
                    val totalTasks = db.taskDao().getAllTasksCount(userId)
                    val completedTasks = db.taskDao().getCompletedTasksCount(userId)

                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        .format(java.util.Date())
                    val todayTasks = db.taskDao().getTodayTasksCount(userId, today)
                    val pendingTasks = db.taskDao().getPendingTasksCount(userId)
                    val overdueTasks = db.taskDao().getOverdueTasksCount(userId)

                    mapOf(
                        "total" to totalTasks,
                        "completed" to completedTasks,
                        "today" to todayTasks,
                        "pending" to pendingTasks,
                        "overdue" to overdueTasks
                    )
                }

                val successRate = if (detailedStats["total"]!! > 0) {
                    ((detailedStats["completed"]!! * 100) / detailedStats["total"]!!)
                } else {
                    0
                }

                val message = """
                    📊 Detailed Task Statistics
                    
                    📋 Total Tasks: ${detailedStats["total"]}
                    ✅ Completed: ${detailedStats["completed"]} 
                    📅 Due Today: ${detailedStats["today"]}
                    ⏳ Pending: ${detailedStats["pending"]}
                    🔴 Overdue: ${detailedStats["overdue"]}
                    📈 Success Rate: $successRate%
                    
                    Keep up the great work! 💪
                """.trimIndent()

                android.app.AlertDialog.Builder(this@FragmentSettingsActivity)
                    .setTitle("Task Statistics")
                    .setMessage(message)
                    .setPositiveButton("Manage Tasks") { _, _ ->
                        navigateToTasks()
                    }
                    .setNegativeButton("Close", null)
                    .show()

            } catch (e: Exception) {
                Toast.makeText(this@FragmentSettingsActivity, "Error loading detailed statistics", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showProfileEditDialog() {
        val options = arrayOf("Change Profile Picture", "Edit Name", "Cancel")
        
        android.app.AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showProfilePictureOptions()
                    1 -> showEditNameDialog()
                    2 -> {} // Cancel - do nothing
                }
            }
            .show()
    }
    
    private fun showProfilePictureOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Remove Picture", "Cancel")
        
        android.app.AlertDialog.Builder(this)
            .setTitle("Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // Take photo functionality - inform user about permissions
                        Toast.makeText(this, "📸 Camera feature requires camera and storage permissions. Please grant permissions in app settings if needed.", Toast.LENGTH_LONG).show()
                        // Note: Full camera implementation would require camera permission handling,
                        // file provider setup, and camera intent. Keeping it simple for now.
                    }
                    1 -> {
                        // Choose from gallery functionality - inform user about permissions
                        Toast.makeText(this, "🖼️ Gallery picker requires storage permissions. Please grant permissions in app settings if needed.", Toast.LENGTH_LONG).show()
                        // Note: Full gallery implementation would require read external storage permission
                        // and gallery intent. Keeping it simple for now.
                    }
                    2 -> {
                        // Remove picture - set to default
                        com.example.cheermateapp.util.ProfileImageManager.deleteProfileImage(this, userId)
                        Toast.makeText(this, "✅ Profile picture reset to default", Toast.LENGTH_SHORT).show()
                        loadSettingsUserData() // Reload to show default
                    }
                    3 -> {} // Cancel
                }
            }
            .show()
    }
    
    private fun showEditNameDialog() {
        val editText = android.widget.EditText(this)
        editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS
        editText.hint = "Enter new name"
        
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentSettingsActivity)
                val currentUser = withContext(Dispatchers.IO) {
                    db.userDao().getById(userId)
                }
                editText.setText(currentUser?.Username ?: "")
            } catch (e: Exception) {
                android.util.Log.e("FragmentSettingsActivity", "Error loading current username", e)
            }
        }
        
        android.app.AlertDialog.Builder(this)
            .setTitle("Edit Name")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    updateUserName(newName)
                } else {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun updateUserName(newName: String) {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentSettingsActivity)
                withContext(Dispatchers.IO) {
                    db.userDao().updateUsername(userId, newName)
                }
                
                Toast.makeText(this@FragmentSettingsActivity, "✅ Name updated!", Toast.LENGTH_SHORT).show()
                loadSettingsUserData()
                
            } catch (e: Exception) {
                Toast.makeText(this@FragmentSettingsActivity, "❌ Error updating name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPersonalitySelectionDialog() {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentSettingsActivity)
                
                // Fetch personalities from repository (with caching)
                val personalities = withContext(Dispatchers.IO) {
                    staticDataRepository.getPersonalities()
                }
                
                if (personalities.isEmpty()) {
                    Toast.makeText(this@FragmentSettingsActivity, "No personalities available", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                // Get current user's personality ID
                val currentUser = withContext(Dispatchers.IO) {
                    db.userDao().getById(userId)
                }

                val personalityNames = personalities.map { it.Name }.toTypedArray()
                
                // Find the index of the current personality
                val checkedItem = if (currentUser?.Personality_ID != null) {
                    personalities.indexOfFirst { it.Personality_ID == currentUser.Personality_ID }
                } else {
                    -1  // No selection
                }
                
                // Track the selected personality
                var selectedPersonalityIndex: Int = checkedItem

                android.app.AlertDialog.Builder(this@FragmentSettingsActivity)
                    .setTitle("Choose Your Personality")
                    .setSingleChoiceItems(personalityNames, checkedItem) { _, which ->
                        selectedPersonalityIndex = which
                    }
                    .setPositiveButton("OK") { _, _ ->
                        if (selectedPersonalityIndex >= 0) {
                            val selected = personalities[selectedPersonalityIndex]
                            updateUserPersonality(selected.Personality_ID)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()

            } catch (e: Exception) {
                Toast.makeText(this@FragmentSettingsActivity, "Error loading personalities", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUserPersonality(personalityId: Int) {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentSettingsActivity)
                val personalityName = withContext(Dispatchers.IO) {
                    // Update User.Personality_ID to link the user to the personality
                    db.userDao().updatePersonality(userId, personalityId)
                    
                    // Get personality name for confirmation message
                    db.personalityDao().getById(personalityId)?.Name ?: "Unknown"
                }

                Toast.makeText(this@FragmentSettingsActivity, "✅ Personality updated to $personalityName!", Toast.LENGTH_SHORT).show()
                loadSettingsUserData()

            } catch (e: Exception) {
                Toast.makeText(this@FragmentSettingsActivity, "Error updating personality", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showNotificationSettings() {
        val options = arrayOf(
            "✅ Task Due Notifications",
            "⏰ Daily Reminders",
            "📊 Weekly Progress",
            "🏆 Achievement Alerts"
        )

        val checkedItems = booleanArrayOf(true, true, false, true)

        android.app.AlertDialog.Builder(this)
            .setTitle("Notification Settings")
            .setMultiChoiceItems(options, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Save") { _, _ ->
                saveNotificationSettings(checkedItems)
                Toast.makeText(this, "🔔 Notification settings saved", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveNotificationSettings(settings: BooleanArray) {
        val prefs = getSharedPreferences("CheerMate_Settings", MODE_PRIVATE)
        prefs.edit()
            .putBoolean("task_due_notifications", settings[0])
            .putBoolean("daily_reminders", settings[1])
            .putBoolean("weekly_progress", settings[2])
            .putBoolean("achievement_alerts", settings[3])
            .apply()
    }

    private fun showDataManagementOptions() {
        val options = arrayOf(
            "📤 Export All Data",
            "📥 Import Data",
            "🗑️ Clear All Tasks",
            "🔄 Sync with Cloud"
        )

        android.app.AlertDialog.Builder(this)
            .setTitle("Data Management")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> exportUserData()
                    1 -> importUserData()
                    2 -> showClearAllTasksConfirmation()
                    3 -> syncWithCloud()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportUserData() {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentSettingsActivity)
                val allTasks = withContext(Dispatchers.IO) {
                    db.taskDao().getAllTasksForUser(userId)
                }

                val csvData = StringBuilder()
                csvData.append("Title,Description,Priority,Status,Due Date,Due Time\n")

                allTasks.forEach { task ->
                    csvData.append("${task.Title},${task.Description ?: ""},${task.Priority},${task.Status},${task.DueAt ?: ""},${task.DueTime ?: ""}\n")
                }

                Toast.makeText(this@FragmentSettingsActivity, "📤 Data exported! (${allTasks.size} tasks)", Toast.LENGTH_LONG).show()
                android.util.Log.d("FragmentSettingsActivity", "Exported data:\n$csvData")

            } catch (e: Exception) {
                android.util.Log.e("FragmentSettingsActivity", "Error exporting data", e)
                Toast.makeText(this@FragmentSettingsActivity, "Error exporting data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun importUserData() {
        // Simple implementation: inform user about import functionality
        android.app.AlertDialog.Builder(this)
            .setTitle("📥 Import Data")
            .setMessage("Data import functionality allows you to restore your tasks from a backup file.\n\nTo use this feature:\n1. Ensure you have a backup file (.json)\n2. Grant file access permissions\n3. Select the backup file to import\n\nNote: This will merge with existing data. Use with caution.")
            .setPositiveButton("OK") { _, _ ->
                Toast.makeText(this, "Import feature: Please ensure you have a valid backup file and necessary permissions.", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showClearAllTasksConfirmation() {
        android.app.AlertDialog.Builder(this)
            .setTitle("⚠️ Clear All Tasks")
            .setMessage("This will permanently delete all your tasks. This action cannot be undone.")
            .setPositiveButton("DELETE ALL") { _, _ ->
                clearAllUserTasks()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearAllUserTasks() {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentSettingsActivity)
                withContext(Dispatchers.IO) {
                    db.taskDao().deleteAllTasksForUser(userId)
                }

                Toast.makeText(this@FragmentSettingsActivity, "🗑️ All tasks cleared!", Toast.LENGTH_SHORT).show()
                loadUserTaskStatistics()

            } catch (e: Exception) {
                Toast.makeText(this@FragmentSettingsActivity, "Error clearing tasks", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun syncWithCloud() {
        android.app.AlertDialog.Builder(this)
            .setTitle("🔄 Cloud Sync")
            .setMessage("Cloud synchronization allows you to:\n• Backup your data to the cloud\n• Access your tasks from multiple devices\n• Automatically sync changes\n\nTo enable cloud sync:\n1. Sign in with a cloud provider (Google Drive, Dropbox, etc.)\n2. Grant necessary permissions\n3. Enable auto-sync in settings\n\nNote: Cloud sync requires an internet connection and appropriate cloud storage permissions.")
            .setPositiveButton("Learn More") { _, _ ->
                Toast.makeText(this, "Cloud sync: Ensure you have a cloud storage account and internet connection.", Toast.LENGTH_LONG).show()
            }
            .setNeutralButton("Settings") { _, _ ->
                Toast.makeText(this, "Cloud sync settings: Configure your cloud provider in app settings.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAboutDialog() {
        val message = """
            🎉 CheerMate Task Manager
            
            Version 1.0.0
            
            A personality-driven task management app that helps you stay organized and motivated!
            
            Features:
            ✅ Smart Task Management
            📅 Calendar Integration
            📊 Progress Tracking
            🎭 Personality-Based Motivation
            🔔 Smart Notifications
            
            Made with ❤️ for productivity enthusiasts
        """.trimIndent()

        android.app.AlertDialog.Builder(this)
            .setTitle("About CheerMate")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showStatisticsInDialog(stats: Map<String, Int>) {
        val successRate = if (stats["total"]!! > 0) {
            ((stats["completed"]!! * 100) / stats["total"]!!)
        } else {
            0
        }

        val message = """
            📊 Your Task Statistics
            
            📋 Total Tasks: ${stats["total"]}
            ✅ Completed: ${stats["completed"]} 
            📅 Due Today: ${stats["today"]}
            ⏳ Pending: ${stats["pending"]}
            📈 Success Rate: $successRate%
            
            Statistics loaded successfully!
        """.trimIndent()

        android.app.AlertDialog.Builder(this)
            .setTitle("Task Statistics")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showLogoutConfirmation() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Sign Out") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        val prefs = getSharedPreferences("CheerMate_Login", MODE_PRIVATE)
        prefs.edit().clear().apply()

        val intent = Intent(this, ActivityLogin::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showDeleteAccountConfirmation() {
        android.app.AlertDialog.Builder(this)
            .setTitle("⚠️ Delete Account")
            .setMessage("This will permanently delete your account and all data. This action cannot be undone.")
            .setPositiveButton("DELETE ACCOUNT") { _, _ ->
                deleteUserAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteUserAccount() {
        lifecycleScope.launch {
            try {
                val db = AppDb.get(this@FragmentSettingsActivity)
                withContext(Dispatchers.IO) {
                    db.taskDao().deleteAllTasksForUser(userId)
                    db.userDao().deleteById(userId)
                }

                Toast.makeText(this@FragmentSettingsActivity, "Account deleted successfully", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@FragmentSettingsActivity, ActivityLogin::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()

            } catch (e: Exception) {
                Toast.makeText(this@FragmentSettingsActivity, "Error deleting account", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_SHOW_DASHBOARD, true)
        intent.putExtra(MainActivity.EXTRA_USER_ID, userId.toString())
        startActivity(intent)
        finish()
    }

    private fun navigateToTasks() {
        val intent = Intent(this, FragmentTaskActivity::class.java)
        intent.putExtra(FragmentTaskActivity.EXTRA_USER_ID, userId.toString())
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        navigateToMain()
        return true
    }

    override fun onResume() {
        super.onResume()
        loadSettingsUserData()
    }
}
