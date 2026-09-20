package com.example.campusfind;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.campusfind.databinding.ActivityAdminDashboardBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Dashboard");
        }

        // Role Check
        boolean isAdmin = getIntent().getBooleanExtra("IS_ADMIN", true);
        if (!isAdmin) {
            Toast.makeText(this, "Access Denied: Admin only", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchStats();

        binding.btnManagePosts.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminManagePostsActivity.class));
        });

        binding.btnManageUsers.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminManageUsersActivity.class));
        });

        binding.btnUserMode.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("IS_ADMIN", true);
            startActivity(intent);
        });

        binding.btnLogout.setOnClickListener(v -> {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void fetchStats() {
        // Fetch User Count
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                binding.tvTotalUsers.setText(String.valueOf(task.getResult().size()));
            }
        });

        // Fetch Item Stats
        db.collection("items").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                QuerySnapshot items = task.getResult();
                binding.tvTotalItems.setText(String.valueOf(items.size()));

                int lostCount = 0;
                int foundCount = 0;
                for (com.google.firebase.firestore.DocumentSnapshot doc : items.getDocuments()) {
                    String type = doc.getString("type");
                    if ("Lost".equalsIgnoreCase(type)) lostCount++;
                    else if ("Found".equalsIgnoreCase(type)) foundCount++;
                }
                binding.tvLostCount.setText(String.valueOf(lostCount));
                binding.tvFoundCount.setText(String.valueOf(foundCount));
            }
        });
    }
}
