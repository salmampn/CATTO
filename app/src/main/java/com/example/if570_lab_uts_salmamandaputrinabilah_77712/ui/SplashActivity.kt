package com.example.if570_lab_uts_salmamandaputrinabilah_77712.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.if570_lab_uts_salmamandaputrinabilah_77712.R
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var buttonLogin: Button
    private lateinit var buttonSignUp: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        auth = FirebaseAuth.getInstance()

        buttonLogin = findViewById(R.id.buttonLogin)
        buttonSignUp = findViewById(R.id.buttonSignUp)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            // If user is signed in, redirect to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        // Handle button clicks
        buttonLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        buttonSignUp.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}
