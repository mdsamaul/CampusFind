package com.example.campusfind.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.campusfind.EditProfileActivity;
import com.example.campusfind.ItemDetailsActivity;
import com.example.campusfind.LoginActivity;
import com.example.campusfind.PostItemActivity;
import com.example.campusfind.adapters.MyPostsAdapter;
import com.example.campusfind.databinding.FragmentProfileBinding;
import com.example.campusfind.models.Item;
import com.example.campusfind.models.User;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private boolean isAdmin = false;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Item> myItems;
    private MyPostsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        myItems = new ArrayList<>();
        
        if (getArguments() != null) {
            isAdmin = getArguments().getBoolean("IS_ADMIN", false);
        }
        
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fetchUserProfile();
        setupRecyclerView();
        fetchMyPosts();
        setupTabLayout();

        if (isAdmin) {
            binding.btnAdminDashboard.setVisibility(View.VISIBLE);
            binding.btnAdminDashboard.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), com.example.campusfind.AdminDashboardActivity.class);
                intent.putExtra("IS_ADMIN", true);
                startActivity(intent);
            });
        } else {
            binding.btnAdminDashboard.setVisibility(View.GONE);
        }

        binding.fabEditPic.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EditProfileActivity.class);
            intent.putExtra("IS_ADMIN", isAdmin);
            startActivity(intent);
        });

        binding.btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EditProfileActivity.class);
            intent.putExtra("IS_ADMIN", isAdmin);
            startActivity(intent);
        });

        binding.btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void fetchUserProfile() {
        if (mAuth.getUid() == null) return;

        db.collection("users").document(mAuth.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (binding == null) return;
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            binding.tvProfileName.setText(user.getName());
                            binding.tvProfileEmail.setText(user.getEmail());
                            binding.tvProfileId.setText("ID: " + (user.getStudentId() != null ? user.getStudentId() : "N/A"));
                            binding.tvProfilePhone.setText("Phone: " + (user.getPhone() != null ? user.getPhone() : "N/A"));
                        }
                    }
                });
    }

    private void setupRecyclerView() {
        adapter = new MyPostsAdapter(myItems, new MyPostsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Item item) {
                Intent intent = new Intent(requireContext(), ItemDetailsActivity.class);
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
                Intent intent = new Intent(requireContext(), PostItemActivity.class);
                intent.putExtra("itemId", item.getId());
                startActivity(intent);
            }
        });
        binding.rvMyPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMyPosts.setAdapter(adapter);
    }

    private void fetchMyPosts() {
        if (mAuth.getUid() == null) return;

        db.collection("items")
                .whereEqualTo("posterId", mAuth.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (binding == null) return;
                    if (error != null) return;
                    if (value != null) {
                        myItems.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            Item item = doc.toObject(Item.class);
                            if (item != null) {
                                item.setId(doc.getId());
                                myItems.add(item);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void showDeletePostConfirmation(Item item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("items").document(item.getId()).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void markItemAsResolved(Item item) {
        db.collection("items").document(item.getId())
                .update("status", "Resolved")
                .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Item marked as resolved!", Toast.LENGTH_SHORT).show());
    }

    private void setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (binding == null) return;
                if (tab.getPosition() == 0) {
                    binding.rvMyPosts.setVisibility(View.VISIBLE);
                    binding.layoutSettings.setVisibility(View.GONE);
                } else if (tab.getPosition() == 1) {
                    binding.rvMyPosts.setVisibility(View.GONE);
                    binding.layoutSettings.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
