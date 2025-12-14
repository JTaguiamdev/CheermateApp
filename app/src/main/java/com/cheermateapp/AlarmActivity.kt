package com.cheermateapp

import android.app.KeyguardManager
import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cheermateapp.util.ReminderManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Full-screen alarm activity that appears when alarm triggers
 * Similar to Google Clock alarm experience
 */
class AlarmActivity : AppCompatActivity() {

    private lateinit var tvTaskTitle: TextView
    private lateinit var tvTaskDescription: TextView
    private lateinit var tvAlarmTime: TextView
    private lateinit var btnStop: Button
    private lateinit var btnSnooze: Button
    
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var taskId: Int = -1
    private var userId: Int = -1
    private var taskTitle: String = ""
    private var taskDescription: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 🚨 FULL-SCREEN ALARM SETUP (works even when locked)
        setupFullScreenAlarm()
        
        setContentView(R.layout.activity_alarm)
        
        // Get alarm data from intent
        taskId = intent.getIntExtra("TASK_ID", -1)
        userId = intent.getIntExtra("USER_ID", -1)
        taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Task Reminder"
        taskDescription = intent.getStringExtra("TASK_DESCRIPTION") ?: ""
        
        android.util.Log.d("AlarmActivity", "🚨 ALARM ACTIVITY STARTED")
        android.util.Log.d("AlarmActivity", "📋 Task: '$taskTitle' (ID: $taskId)")
        
        initializeViews()
        displayAlarmInfo()
        startAlarmSound()
        startVibration()
    }
    
    private fun setupFullScreenAlarm() {
        // ✅ Show over lock screen (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        
        // ✅ For older Android versions
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        
        // ✅ Disable keyguard
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        }
        
        // ✅ Full screen immersive mode
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
    }
    
    private fun initializeViews() {
        tvTaskTitle = findViewById(R.id.tv_alarm_task_title)
        tvTaskDescription = findViewById(R.id.tv_alarm_task_description)
        tvAlarmTime = findViewById(R.id.tv_alarm_time)
        btnStop = findViewById(R.id.btn_alarm_stop)
        btnSnooze = findViewById(R.id.btn_alarm_snooze)
        
        // ✅ Button click listeners
        btnStop.setOnClickListener {
            android.util.Log.d("AlarmActivity", "🛑 STOP button pressed")
            stopAlarm()
        }
        
        btnSnooze.setOnClickListener {
            android.util.Log.d("AlarmActivity", "😴 SNOOZE button pressed")
            snoozeAlarm()
        }
    }
    
    private fun displayAlarmInfo() {
        tvTaskTitle.text = taskTitle
        tvTaskDescription.text = if (taskDescription.isNotEmpty()) taskDescription else "No description"
        
        // Show current time
        val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        tvAlarmTime.text = currentTime
        
        android.util.Log.d("AlarmActivity", "📱 Display updated: '$taskTitle' at $currentTime")
    }
    
    private fun startAlarmSound() {
        try {
            // ✅ Use system alarm sound (same as clock apps)
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            
            mediaPlayer = MediaPlayer.create(this, alarmUri)
            mediaPlayer?.isLooping = true  // ✅ Continuous sound until dismissed
            mediaPlayer?.start()
            
            android.util.Log.d("AlarmActivity", "🔊 Alarm sound started (looping)")
        } catch (e: Exception) {
            android.util.Log.e("AlarmActivity", "❌ Error starting alarm sound", e)
        }
    }
    
    private fun startVibration() {
        try {
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // ✅ Modern vibration pattern (continuous)
                val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
                val effect = VibrationEffect.createWaveform(pattern, 0) // 0 = repeat from start
                vibrator?.vibrate(effect)
            } else {
                // ✅ Legacy vibration
                val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
                vibrator?.vibrate(pattern, 0) // 0 = repeat from start
            }
            
            android.util.Log.d("AlarmActivity", "📳 Vibration started (continuous)")
        } catch (e: Exception) {
            android.util.Log.e("AlarmActivity", "❌ Error starting vibration", e)
        }
    }
    
    private fun stopAlarm() {
        android.util.Log.d("AlarmActivity", "🛑 Stopping alarm completely")
        
        // ✅ Stop all alarm effects
        stopAlarmEffects()
        
        // ✅ Close alarm activity
        finish()
        
        android.util.Log.d("AlarmActivity", "✅ Alarm stopped and activity finished")
    }
    
    private fun snoozeAlarm() {
        android.util.Log.d("AlarmActivity", "😴 Snoozing alarm for 10 minutes")
        
        // ✅ Stop current alarm effects
        stopAlarmEffects()
        
        // ✅ Schedule new alarm in 10 minutes
        val snoozeTime = System.currentTimeMillis() + (10 * 60 * 1000) // 10 minutes
        
        ReminderManager.scheduleReminder(
            this,
            taskId,
            taskTitle,
            taskDescription,
            userId,
            snoozeTime
        )
        
        android.util.Log.d("AlarmActivity", "⏰ Snooze alarm scheduled for: ${Date(snoozeTime)}")
        
        // ✅ Close current alarm activity
        finish()
        
        android.util.Log.d("AlarmActivity", "✅ Alarm snoozed and activity finished")
    }
    
    private fun stopAlarmEffects() {
        // ✅ Stop sound
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            android.util.Log.d("AlarmActivity", "🔇 Alarm sound stopped")
        } catch (e: Exception) {
            android.util.Log.e("AlarmActivity", "❌ Error stopping sound", e)
        }
        
        // ✅ Stop vibration
        try {
            vibrator?.cancel()
            android.util.Log.d("AlarmActivity", "📴 Vibration stopped")
        } catch (e: Exception) {
            android.util.Log.e("AlarmActivity", "❌ Error stopping vibration", e)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopAlarmEffects()
        android.util.Log.d("AlarmActivity", "🧹 AlarmActivity destroyed")
    }
    
    override fun onBackPressed() {
        // ✅ Prevent back button from dismissing alarm
        android.util.Log.d("AlarmActivity", "⬅️ Back button pressed - ignoring")
        // Do nothing - force user to use Stop/Snooze buttons
    }
}