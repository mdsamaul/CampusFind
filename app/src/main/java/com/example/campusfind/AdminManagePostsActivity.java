package com.example.campusfind;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.campusfind.adapters.ItemAdapter;
import com.example.campusfind.databinding.ActivityAdminManagePostsBinding;
import com.example.campusfind.models.Item;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminManagePostsActivity extends AppCompatActivity {

    private ActivityAdminManagePostsBinding binding;
    private FirebaseFirestore db;
    private final List<Item> allItemsList = new ArrayList<>();
    private final List<Item> filteredList = new ArrayList<>();
    private ItemAdapter adapter;
    private String currentFilter = "All";
    private String currentSearchQuery = "";

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
        setupFilters();
    }

    private void setupFilters() {
        // Search listener
        binding.etAdminSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase().trim();
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Chip group listener
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
        db.collection("items")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        allItemsList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Item item = doc.toObject(Item.class);
                            if (item != null) {
                                item.setId(doc.getId());
                                allItemsList.add(item);
                            }
                        }
                        applyFilter();
                    }
                });
    }

    private void applyFilter() {
        filteredList.clear();
        for (Item item : allItemsList) {
            boolean matchesFilter = false;
            if ("All".equals(currentFilter)) {
                matchesFilter = true;
            } else if ("Pending".equals(currentFilter)) {
                if ("Pending".equalsIgnoreCase(item.getStatus())) {
                    matchesFilter = true;
                }
            } else {
                if (currentFilter.equalsIgnoreCase(item.getType())) {
                    matchesFilter = true;
                }
            }

            boolean matchesSearch = currentSearchQuery.isEmpty() || 
                                   (item.getTitle() != null && item.getTitle().toLowerCase().contains(currentSearchQuery)) ||
                                   (item.getCategory() != null && item.getCategory().toLowerCase().contains(currentSearchQuery)) ||
                                   (item.getLocation() != null && item.getLocation().toLowerCase().contains(currentSearchQuery));

            if (matchesFilter && matchesSearch) {
                filteredList.add(item);
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
