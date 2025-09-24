package com.example.cheermateapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SHOW_DASHBOARD = "SHOW_DASHBOARD"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val showDashboard = intent?.getBooleanExtra(EXTRA_SHOW_DASHBOARD, false) == true
        if (showDashboard) {
            // Display dashboard UI in this same Activity
            setContentView(R.layout.activity_main)
            return
        }

        // Default: show login UI
        setContentView(R.layout.activity_login)

        val username = findViewById<TextInputEditText>(R.id.User_ID)
        val password = findViewById<TextInputEditText>(R.id.PasswordHash)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val u = username.text?.toString()?.trim().orEmpty()
            val p = password.text?.toString()?.trim().orEmpty()

            if (u == "jasper" && p == "123") {
                // Go to personality selector; finish to remove login from back stack
                startActivity(Intent(this, PersonalityActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
