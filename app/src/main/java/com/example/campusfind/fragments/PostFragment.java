package com.example.campusfind.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.campusfind.databinding.FragmentPostBinding;
import com.example.campusfind.models.Item;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class PostFragment extends Fragment {

    private FragmentPostBinding binding;
    private Uri selectedImageUri;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth mAuth;
    private double selectedLat = 0;
    private double selectedLng = 0;
    
    private static final String DEFAULT_IMAGE = "https://firebasestorage.googleapis.com/v0/b/campusfind-8a6c8.appspot.com/o/item_images%2Fplaceholder.png?alt=media";

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

    private final ActivityResultLauncher<Intent> mapPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedLat = result.getData().getDoubleExtra("lat", 0);
                    selectedLng = result.getData().getDoubleExtra("lng", 0);
                    binding.tvSelectedCoords.setText(String.format(java.util.Locale.getDefault(), "Pinned: %.4f, %.4f", selectedLat, selectedLng));
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPostBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupCategoryDropdown();

        binding.cardImage.setOnClickListener(v -> openGallery());
        
        binding.btnPickLocation.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.example.campusfind.MapPickerActivity.class);
            mapPickerLauncher.launch(intent);
        });

        binding.btnSubmit.setOnClickListener(v -> validateAndSubmit());
    }

    private void setupCategoryDropdown() {
        String[] categories = {"Electronics", "ID Card", "Wallet", "Books", "Keys", "Others"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
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
            Toast.makeText(requireContext(), "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            saveItemToFirestore(title, category, location, description, type, DEFAULT_IMAGE);
            return;
        }

        uploadImageAndPost(title, category, location, description, type);
    }

    private void uploadImageAndPost(String title, String category, String location, String description, String type) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSubmit.setEnabled(false);

        String imageId = UUID.randomUUID().toString();
        StorageReference storageRef = storage.getReference().child("item_images/" + imageId);

        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveItemToFirestore(title, category, location, description, type, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Image upload failed, using default.", Toast.LENGTH_SHORT).show();
                    saveItemToFirestore(title, category, location, description, type, DEFAULT_IMAGE);
                });
    }

    private void saveItemToFirestore(String title, String category, String location, String description, String type, String imageUrl) {
        binding.progressBar.setVisibility(View.VISIBLE);
        String itemId = db.collection("items").document().getId();
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
                "Pending",
                System.currentTimeMillis(),
                ""
        );
        item.setLatitude(selectedLat);
        item.setLongitude(selectedLng);

        db.collection("items").document(itemId)
                .set(item)
                .addOnSuccessListener(aVoid -> {
                    if (binding != null) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSubmit.setEnabled(true);
                        Toast.makeText(requireContext(), "Item posted successfully!", Toast.LENGTH_SHORT).show();
                        resetFields();
                    }
                })
                .addOnFailureListener(e -> {
                    if (binding != null) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSubmit.setEnabled(true);
                        Toast.makeText(requireContext(), "Failed to save item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetFields() {
        binding.etTitle.getText().clear();
        binding.etLocation.getText().clear();
        binding.etDescription.getText().clear();
        binding.ivItemPreview.setImageResource(0);
        binding.layoutUpload.setVisibility(View.VISIBLE);
        selectedImageUri = null;
        selectedLat = 0;
        selectedLng = 0;
        binding.tvSelectedCoords.setText("No location pinned");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
