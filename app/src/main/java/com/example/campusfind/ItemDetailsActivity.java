package com.example.campusfind;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.campusfind.adapters.MatchAdapter;
import com.example.campusfind.databinding.ActivityItemDetailsBinding;
import com.example.campusfind.models.Item;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ItemDetailsActivity extends AppCompatActivity {

    private ActivityItemDetailsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String itemId;
    private Item currentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        itemId = getIntent().getStringExtra("itemId");

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        if (itemId != null) {
            fetchItemDetails();
        } else {
            Toast.makeText(this, "Item ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.btnStartChat.setOnClickListener(v -> {
            if (currentItem != null) {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("RECEIVER_ID", currentItem.getPosterId());
                intent.putExtra("ITEM_ID", currentItem.getId());
                intent.putExtra("ITEM_TITLE", currentItem.getTitle());
                startActivity(intent);
            }
        });

        binding.btnFindMatches.setOnClickListener(v -> {
            if (currentItem != null) {
                Intent intent = new Intent(this, MatchResultsActivity.class);
                intent.putExtra("itemId", currentItem.getId());
                intent.putExtra("category", currentItem.getCategory());
                intent.putExtra("type", currentItem.getType());
                intent.putExtra("title", currentItem.getTitle());
                startActivity(intent);
            }
        });

        binding.btnMarkResolved.setOnClickListener(v -> {
            markAsResolved();
        });

        binding.btnViewOnMap.setOnClickListener(v -> {
            if (currentItem != null && currentItem.getLatitude() != 0) {
                Intent intent = new Intent(this, ItemMapActivity.class);
                intent.putExtra("lat", currentItem.getLatitude());
                intent.putExtra("lng", currentItem.getLongitude());
                intent.putExtra("title", currentItem.getTitle());
                startActivity(intent);
            }
        });

        binding.btnMoreActions.setOnClickListener(this::showOptionsMenu);

        setupPossibleMatches();
    }

    private void fetchItemDetails() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("items").document(itemId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    binding.progressBar.setVisibility(View.GONE);
                    currentItem = documentSnapshot.toObject(Item.class);
                    if (currentItem != null) {
                        currentItem.setId(documentSnapshot.getId());
                        displayItemData();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(ItemDetailsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayItemData() {
        binding.tvDetailTitle.setText(currentItem.getTitle());
        binding.tvDetailDescription.setText(currentItem.getDescription());
        binding.tvDetailLocation.setText(currentItem.getLocation());
        
        if (currentItem.getTimestamp() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            binding.tvDetailDate.setText("Posted on " + sdf.format(new Date(currentItem.getTimestamp())));
        }

        if (currentItem.getLatitude() != 0) {
            binding.btnViewOnMap.setVisibility(View.VISIBLE);
        } else {
            binding.btnViewOnMap.setVisibility(View.GONE);
        }

        if (currentItem.getImageUrl() != null && !currentItem.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentItem.getImageUrl())
                    .placeholder(R.drawable.ic_image)
                    .into(binding.ivDetailImage);
        }

        boolean isOwner = mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getUid().equals(currentItem.getPosterId());
        
        if (isOwner) {
            binding.btnStartChat.setVisibility(View.GONE);
            binding.layoutOwnerActions.setVisibility(View.VISIBLE);
        } else {
            binding.btnStartChat.setVisibility(View.VISIBLE);
            binding.layoutOwnerActions.setVisibility(View.GONE);
        }

        fetchPosterName(currentItem.getPosterId());
    }

    private void fetchPosterName(String posterId) {
        db.collection("users").document(posterId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        binding.tvPosterName.setText(documentSnapshot.getString("name"));
                    } else {
                        binding.tvPosterName.setText("Unknown User");
                    }
                });
    }

    private void markAsResolved() {
        db.collection("items").document(itemId)
                .update("status", "Resolved")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Item marked as Resolved", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void setupPossibleMatches() {
        List<Item> dummyMatches = new ArrayList<>();
        binding.rvPossibleMatches.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
        binding.rvPossibleMatches.setAdapter(new MatchAdapter(dummyMatches, true));
    }

    private void showOptionsMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenu().add("Edit");
        popupMenu.getMenu().add("Delete");

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Edit")) {
                Intent intent = new Intent(this, PostItemActivity.class);
                intent.putExtra("itemId", itemId);
                startActivity(intent);
            } else if (item.getTitle().equals("Delete")) {
                showDeleteConfirmation();
            }
            return true;
        });
        popupMenu.show();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("items").document(itemId).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
