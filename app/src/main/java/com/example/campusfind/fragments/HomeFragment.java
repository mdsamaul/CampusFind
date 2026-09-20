package com.example.campusfind.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.campusfind.PostItemActivity;
import com.example.campusfind.adapters.ItemAdapter;
import com.example.campusfind.databinding.FragmentHomeBinding;
import com.example.campusfind.models.Item;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ItemAdapter adapter;
    private FirebaseFirestore db;
    private List<Item> itemList;

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
        itemList = new ArrayList<>();
        setupRecyclerView();
        fetchItemsFromFirestore();

        binding.swipeRefresh.setOnRefreshListener(this::fetchItemsFromFirestore);

        binding.fabPost.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), PostItemActivity.class));
        });
    }

    private void setupRecyclerView() {
        adapter = new ItemAdapter(itemList);
        binding.rvItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvItems.setAdapter(adapter);
    }

    private void fetchItemsFromFirestore() {
        if (!binding.swipeRefresh.isRefreshing()) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        
        db.collection("items")
                .whereEqualTo("status", "Active")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    if (error != null) {
                        return;
                    }

                    if (value != null) {
                        itemList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            Item item = doc.toObject(Item.class);
                            if (item != null) {
                                item.setId(doc.getId());
                                itemList.add(item);
                            }
                        }
                        adapter.updateList(itemList);
                        
                        if (itemList.isEmpty()) {
                            binding.tvNoItems.setVisibility(View.VISIBLE);
                        } else {
                            binding.tvNoItems.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
