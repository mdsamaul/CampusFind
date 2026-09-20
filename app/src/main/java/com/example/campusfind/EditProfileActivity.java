package com.example.campusfind;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.campusfind.databinding.ActivityEditProfileBinding;
import com.example.campusfind.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private Uri selectedImageUri;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String currentUserId;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    binding.ivEditProfilePic.setImageURI(selectedImageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUserId = mAuth.getUid();

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // Load current data
        loadUserData();

        binding.btnChangePic.setOnClickListener(v -> openGallery());

        binding.btnSaveChanges.setOnClickListener(v -> validateAndSave());
    }

    private void loadUserData() {
        if (currentUserId == null) {
            Toast.makeText(this, "User session expired. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            binding.etFullName.setText(user.getName());
                            binding.etEmail.setText(user.getEmail());
                            binding.etEmail.setEnabled(false); // Email usually not editable
                            binding.etStudentId.setText(user.getStudentId());
                            binding.etPhone.setText(user.getPhone());
                            
                            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                                Glide.with(this)
                                        .load(user.getProfileImageUrl())
                                        .placeholder(R.drawable.ic_profile)
                                        .into(binding.ivEditProfilePic);
                            }
                        }
                    } else {
                        // Document doesn't exist
                        if (mAuth.getCurrentUser() != null) {
                            binding.etEmail.setText(mAuth.getCurrentUser().getEmail());
                            binding.etEmail.setEnabled(false);
                            Toast.makeText(this, "Profile not found. Please create one.", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void validateAndSave() {
        String name = binding.etFullName.getText().toString().trim();
        String studentId = binding.etStudentId.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            binding.etFullName.setError("Name is required");
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSaveChanges.setEnabled(false);

        if (selectedImageUri != null) {
            uploadImageAndSave(name, studentId, phone);
        } else {
            saveToFirestore(name, studentId, phone, null);
        }
    }

    private void uploadImageAndSave(String name, String studentId, String phone) {
        String fileName = UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child("profile_pics/" + currentUserId + "/" + fileName);

        ref.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveToFirestore(name, studentId, phone, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSaveChanges.setEnabled(true);
                    Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveToFirestore(String name, String studentId, String phone, String imageUrl) {
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("name", name);
        updates.put("studentId", studentId);
        updates.put("phone", phone);
        updates.put("userId", currentUserId); // Ensure UID is there
        updates.put("email", binding.etEmail.getText().toString().trim());

        if (imageUrl != null) {
            updates.put("profileImageUrl", imageUrl);
        }

        // Use SET instead of UPDATE so it creates the document if it doesn't exist
        db.collection("users").document(currentUserId).set(updates, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSaveChanges.setEnabled(true);
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }
}
