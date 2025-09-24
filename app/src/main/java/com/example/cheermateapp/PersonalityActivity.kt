package com.example.cheermateapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class PersonalityActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personality)

        val goMain: (Int) -> Unit = {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_SHOW_DASHBOARD, true)
                // Start fresh so Back won't go to login/selector
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            // no need to finish(); stack is cleared
        }

        findViewById<View>(R.id.cardKalog)?.setOnClickListener { goMain(1) }
        findViewById<View>(R.id.cardGenZ)?.setOnClickListener { goMain(2) }
        findViewById<View>(R.id.cardSofty)?.setOnClickListener { goMain(3) }
        findViewById<View>(R.id.cardGrey)?.setOnClickListener { goMain(4) }
        findViewById<View>(R.id.cardFlirty)?.setOnClickListener { goMain(5) }
    }
}
