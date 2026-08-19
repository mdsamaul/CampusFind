package com.example.campusfind

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.campusfind.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            
            // TODO: Add Firebase Login logic here
            Toast.makeText(this, "Login Clicked", Toast.LENGTH_SHORT).show()
            
            // Navigate to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.tvForgotPassword.setOnClickListener {
            // TODO: Navigate to ForgotPasswordActivity
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.tvRegisterLink.setOnClickListener {
            // TODO: Navigate to RegisterActivity
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
