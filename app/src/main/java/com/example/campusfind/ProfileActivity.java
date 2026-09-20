package com.example.campusfind;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.campusfind.adapters.MyPostsAdapter;
import com.example.campusfind.databinding.ActivityProfileBinding;
import com.example.campusfind.models.Item;
import com.example.campusfind.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        fetchUserProfile();
        setupMyPostsRecyclerView();

        binding.btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });
        
        binding.btnChangePassword.setOnClickListener(v -> {
            Toast.makeText(this, "Change Password Clicked", Toast.LENGTH_SHORT).show();
        });

        binding.btnLogout.setOnClickListener(v -> showLogoutConfirmation());
        
        binding.fabEditPic.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });
    }

    private void fetchUserProfile() {
        if (mAuth.getUid() == null) return;

        // Fallback from Auth
        if (mAuth.getCurrentUser() != null) {
            binding.tvProfileName.setText("Campus User");
            binding.tvProfileEmail.setText(mAuth.getCurrentUser().getEmail());
        }

        db.collection("users").document(mAuth.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            binding.tvProfileName.setText(user.getName());
                            binding.tvProfileEmail.setText(user.getEmail());
                            binding.tvProfileId.setText("Student ID: " + user.getStudentId());
                            binding.tvProfilePhone.setText("Phone: " + user.getPhone());
                        }
                    }
                });
    }

    private void setupMyPostsRecyclerView() {
        List<Item> myItems = new ArrayList<>();
        // In real app, this should fetch from Firestore like ProfileFragment does
        binding.rvMyPosts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMyPosts.setAdapter(new MyPostsAdapter(myItems, new MyPostsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Item item) {
                Intent intent = new Intent(ProfileActivity.this, ItemDetailsActivity.class);
                intent.putExtra("itemId", item.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Item item) {
                showDeletePostConfirmation(item);
            }

            @Override
            public void onResolveClick(Item item) {
                markItemAsResolved(item);
            }

            @Override
            public void onEditClick(Item item) {
                Intent intent = new Intent(ProfileActivity.this, PostItemActivity.class);
                intent.putExtra("itemId", item.getId());
                startActivity(intent);
            }
        }));
    }

    private void showDeletePostConfirmation(Item item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("items").document(item.getId()).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void markItemAsResolved(Item item) {
        db.collection("items").document(item.getId())
                .update("status", "Resolved")
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Item marked as resolved!", Toast.LENGTH_SHORT).show());
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
