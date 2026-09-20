package com.example.campusfind;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.campusfind.adapters.AdminUserAdapter;
import com.example.campusfind.databinding.ActivityAdminManageUsersBinding;
import com.example.campusfind.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class AdminManageUsersActivity extends AppCompatActivity {

    private ActivityAdminManageUsersBinding binding;
    private FirebaseFirestore db;
    private List<User> userList;
    private AdminUserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminManageUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        fetchUsers();
    }

    private void setupRecyclerView() {
        adapter = new AdminUserAdapter(userList, user -> {
            showDeleteConfirmation(user);
        });
        binding.rvAdminUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAdminUsers.setAdapter(adapter);
    }

    private void fetchUsers() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("users").get().addOnCompleteListener(task -> {
            binding.progressBar.setVisibility(View.GONE);
            if (task.isSuccessful() && task.getResult() != null) {
                userList.clear();
                for (com.google.firebase.firestore.DocumentSnapshot doc : task.getResult()) {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        user.setUserId(doc.getId());
                        userList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Failed to fetch users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmation(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete user: " + user.getName() + "? This will not delete their Firebase Auth account, only their Firestore profile.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("users").document(user.getUserId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "User profile deleted", Toast.LENGTH_SHORT).show();
                                fetchUsers(); // Refresh list
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
