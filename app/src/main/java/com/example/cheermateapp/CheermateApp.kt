package com.example.cheermateapp

import android.app.Application
import com.example.cheermateapp.data.db.AppDb
import com.example.cheermateapp.util.NotificationUtil
import com.example.cheermateapp.util.ThemeManager
import com.google.gson.Gson

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
        
        // Initialize theme
        ThemeManager.initializeTheme(this)
        
        // Create notification channel for reminders
        NotificationUtil.createNotificationChannel(this)
        
        android.util.Log.d("CheermateApp", "✅ Application initialized")
    }
}
