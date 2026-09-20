package com.example.campusfind;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.campusfind.databinding.ActivityPostItemBinding;
import com.example.campusfind.models.Item;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class PostItemActivity extends AppCompatActivity {

    private ActivityPostItemBinding binding;
    private Uri selectedImageUri;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth mAuth;
    
    private boolean isEditMode = false;
    private String existingItemId;
    private String existingImageUrl;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    binding.ivItemPreview.setImageURI(selectedImageUri);
                    binding.layoutUpload.setVisibility(View.GONE);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostItemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupCategoryDropdown();

        // Check for Edit Mode
        existingItemId = getIntent().getStringExtra("itemId");
        if (existingItemId != null) {
            isEditMode = true;
            loadItemData(existingItemId);
            binding.btnSubmit.setText("Update Post");
        }

        binding.cardImage.setOnClickListener(v -> openGallery());

        binding.btnSubmit.setOnClickListener(v -> validateAndSubmit());
    }

    private void loadItemData(String itemId) {
        db.collection("items").document(itemId).get().addOnSuccessListener(documentSnapshot -> {
            Item item = documentSnapshot.toObject(Item.class);
            if (item != null) {
                binding.etTitle.setText(item.getTitle());
                binding.autoCategory.setText(item.getCategory(), false);
                binding.etLocation.setText(item.getLocation());
                binding.etDescription.setText(item.getDescription());
                if ("Lost".equals(item.getType())) {
                    binding.btnLost.setChecked(true);
                } else {
                    binding.btnFound.setChecked(true);
                }
                
                existingImageUrl = item.getImageUrl();
                if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
                    Glide.with(this).load(existingImageUrl).into(binding.ivItemPreview);
                    binding.layoutUpload.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupCategoryDropdown() {
        String[] categories = {"Electronics", "ID Card", "Wallet", "Books", "Keys", "Others"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        binding.autoCategory.setAdapter(adapter);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void validateAndSubmit() {
        String title = binding.etTitle.getText().toString().trim();
        String category = binding.autoCategory.getText().toString().trim();
        String location = binding.etLocation.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String type = binding.btnLost.isChecked() ? "Lost" : "Found";

        if (title.isEmpty() || category.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEditMode && selectedImageUri == null) {
            // In Create mode, we still prefer an image, but user said default if it fails.
            // Let's allow proceed with a placeholder if they didn't pick any? 
            // Actually, let's just try to upload if selected, else use placeholder.
            saveItemToFirestore(title, category, location, description, type, "https://firebasestorage.googleapis.com/v0/b/campusfind-8a6c8.appspot.com/o/item_images%2Fplaceholder.png?alt=media");
            return;
        }

        if (selectedImageUri != null) {
            uploadImageAndPost(title, category, location, description, type);
        } else {
            saveItemToFirestore(title, category, location, description, type, existingImageUrl);
        }
    }

    private void uploadImageAndPost(String title, String category, String location, String description, String type) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSubmit.setEnabled(false);

        try {
            // Convert Image URI to Base64 String (Bits)
            android.graphics.Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            
            // Compress the image to stay under 1MB Firestore limit
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 50, baos); // 50% quality
            byte[] imageBytes = baos.toByteArray();
            String base64Image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
            
            // Save this string directly to Firestore
            saveItemToFirestore(title, category, location, description, type, "data:image/jpeg;base64," + base64Image);
            
        } catch (java.io.IOException e) {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
            binding.progressBar.setVisibility(View.GONE);
            binding.btnSubmit.setEnabled(true);
        }
    }

    private void saveItemToFirestore(String title, String category, String location, String description, String type, String imageUrl) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSubmit.setEnabled(false);

        String itemId = isEditMode ? existingItemId : db.collection("items").document().getId();
        String posterId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "anonymous";

        Item item = new Item(
                itemId,
                title,
                category,
                type,
                location,
                description,
                imageUrl,
                posterId,
                "Active", // Set to Active by default
                System.currentTimeMillis(),
                ""
        );

        db.collection("items").document(itemId)
                .set(item)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(PostItemActivity.this, isEditMode ? "Item updated!" : "Item posted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSubmit.setEnabled(true);
                    Toast.makeText(PostItemActivity.this, "Failed to save item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
