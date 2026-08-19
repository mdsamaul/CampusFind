package com.example.campusfind

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.campusfind.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSendResetLink.setOnClickListener {
            // TODO: Add Password Reset logic here
            Toast.makeText(this, "Reset Link Sent", Toast.LENGTH_SHORT).show()
        }

        binding.tvBackToLogin.setOnClickListener {
            finish()
        }
    }
}
