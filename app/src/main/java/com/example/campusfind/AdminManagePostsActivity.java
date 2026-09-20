package com.example.campusfind;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.campusfind.adapters.ItemAdapter;
import com.example.campusfind.databinding.ActivityAdminManagePostsBinding;
import com.example.campusfind.models.Item;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AdminManagePostsActivity extends AppCompatActivity {

    private ActivityAdminManagePostsBinding binding;
    private FirebaseFirestore db;
    private List<Item> allItemsList = new ArrayList<>();
    private List<Item> filteredList = new ArrayList<>();
    private ItemAdapter adapter;
    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminManagePostsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        fetchAllItems();

        binding.chipGroupAdminFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipAdminLost)) currentFilter = "Lost";
            else if (checkedIds.contains(R.id.chipAdminFound)) currentFilter = "Found";
            else if (checkedIds.contains(R.id.chipAdminPending)) currentFilter = "Pending";
            else currentFilter = "All";
            
            applyFilter();
        });
    }

    private void setupRecyclerView() {
        adapter = new ItemAdapter(filteredList, true, this::showModerationOptions);
        binding.rvAdminPosts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAdminPosts.setAdapter(adapter);
    }

    private void fetchAllItems() {
        // Fetch everything ordered by time, and we will filter locally to avoid index issues
        db.collection("items")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        allItemsList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            Item item = doc.toObject(Item.class);
                            if (item != null) {
                                item.setId(doc.getId());
                                allItemsList.add(item);
                            }
                        }
                        applyFilter(); // Initial display
                    }
                });
    }

    private void applyFilter() {
        filteredList.clear();
        for (Item item : allItemsList) {
            if ("All".equals(currentFilter)) {
                filteredList.add(item);
            } else if ("Pending".equals(currentFilter)) {
                if ("Pending".equalsIgnoreCase(item.getStatus())) {
                    filteredList.add(item);
                }
            } else {
                // Filter by Type (Lost/Found)
                if (currentFilter.equalsIgnoreCase(item.getType())) {
                    filteredList.add(item);
                }
            }
        }
        adapter.updateList(filteredList);
    }

    private void showModerationOptions(Item item) {
        String[] options = {"Approve", "Reject/Delete", "Cancel"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("Moderation Options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        approvePost(item);
                    } else if (which == 1) {
                        showDeleteConfirmation(item);
                    }
                })
                .show();
    }

    private void approvePost(Item item) {
        db.collection("items").document(item.getId())
                .update("status", "Active")
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Post approved and live!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showDeleteConfirmation(Item item) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("items").document(item.getId()).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
