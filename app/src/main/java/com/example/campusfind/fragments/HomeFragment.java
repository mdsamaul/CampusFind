package com.example.campusfind.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.campusfind.PostItemActivity;
import com.example.campusfind.R;
import com.example.campusfind.adapters.ItemAdapter;
import com.example.campusfind.databinding.FragmentHomeBinding;
import com.example.campusfind.models.Item;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ItemAdapter adapter;
    private FirebaseFirestore db;
    private final List<Item> allItemsList = new ArrayList<>();
    private final List<Item> displayedList = new ArrayList<>();
    private String currentTypeFilter = "All";
    private String currentSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        setupRecyclerView();
        fetchItemsFromFirestore();

        binding.swipeRefresh.setOnRefreshListener(this::fetchItemsFromFirestore);

        binding.fabPost.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), PostItemActivity.class));
        });

        setupFilters();
    }

    private void setupFilters() {
        // Search listener
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase().trim();
                applyFilterAndSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Chip group listener
        binding.chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipLost)) {
                currentTypeFilter = "Lost";
            } else if (checkedIds.contains(R.id.chipFound)) {
                currentTypeFilter = "Found";
            } else {
                currentTypeFilter = "All";
            }
            applyFilterAndSearch();
        });
    }

    private void setupRecyclerView() {
        adapter = new ItemAdapter(displayedList);
        binding.rvItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvItems.setAdapter(adapter);
    }

    private void fetchItemsFromFirestore() {
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        
        db.collection("items")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    if (error != null) {
                        android.util.Log.e("HomeFragment", "Firestore Error: " + error.getMessage());
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
                        applyFilterAndSearch();
                    }
                });
    }

    private void applyFilterAndSearch() {
        displayedList.clear();
        for (Item item : allItemsList) {
            // Show only Active (Admin Approved) items to regular users
            boolean isActive = "Active".equalsIgnoreCase(item.getStatus());
            
            boolean matchesFilter = currentTypeFilter.equals("All") || 
                                   (item.getType() != null && item.getType().equalsIgnoreCase(currentTypeFilter));
            
            boolean matchesSearch = currentSearchQuery.isEmpty() || 
                                   (item.getTitle() != null && item.getTitle().toLowerCase().contains(currentSearchQuery)) ||
                                   (item.getCategory() != null && item.getCategory().toLowerCase().contains(currentSearchQuery)) ||
                                   (item.getLocation() != null && item.getLocation().toLowerCase().contains(currentSearchQuery));

            if (isActive && matchesFilter && matchesSearch) {
                displayedList.add(item);
            }
        }
        
        adapter.updateList(displayedList);
        
        if (displayedList.isEmpty()) {
            binding.tvNoItems.setVisibility(View.VISIBLE);
        } else {
            binding.tvNoItems.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
