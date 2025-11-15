package com.cheermateapp

import android.app.Application
import com.cheermateapp.data.db.AppDb
import com.cheermateapp.util.DatabaseSeeder
import com.cheermateapp.util.NotificationUtil
import com.cheermateapp.util.ThemeManager
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Application class for CheermateApp
 * Initializes database and notification channels
 */
class CheermateApp : Application() {
    
    lateinit var db: AppDb
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize database
        db = AppDb.get(this)
        
        // Seed database with default static data in background
        CoroutineScope(Dispatchers.IO).launch {
            DatabaseSeeder.seedAll(this@CheermateApp)
        }
        
        // Initialize theme
        ThemeManager.initializeTheme(this)
        
        // Create notification channel for reminders
        NotificationUtil.createNotificationChannel(this)
        
        android.util.Log.d("CheermateApp", "✅ Application initialized")
    }
}
