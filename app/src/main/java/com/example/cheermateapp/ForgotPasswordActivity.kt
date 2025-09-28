// Create: app/src/main/java/com/example/cheermateapp/ForgotPasswordActivity.kt
package com.example.cheermateapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cheermateapp.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
            setContentView(binding.root)

            binding.btnContinue?.setOnClickListener {
                Toast.makeText(this, "Password recovery coming soon", Toast.LENGTH_SHORT).show()

                // Navigate back to login
                startActivity(Intent(this, ActivityLogin::class.java))
                finish()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "ForgotPasswordActivity - Coming Soon!", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
