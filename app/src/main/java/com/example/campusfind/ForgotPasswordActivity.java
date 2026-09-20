package com.example.campusfind;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campusfind.databinding.ActivityForgotPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // Send Reset Link Button Click Listener
        binding.btnSendResetLink.setOnClickListener(v -> {
            String email = binding.etForgotEmail.getText().toString().trim();

            if (email.isEmpty()) {
                binding.etForgotEmail.setError("Email is required");
                binding.etForgotEmail.requestFocus();
                return;
            }

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnSendResetLink.setEnabled(false);

            mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSendResetLink.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Reset link sent to your email!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Error sending reset email.";
                        Toast.makeText(ForgotPasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
        });

        // Back to Login Click Listener
        binding.tvBackToLogin.setOnClickListener(v -> {
            // Simply close this activity to go back to LoginActivity
            finish();
        });
    }
}
