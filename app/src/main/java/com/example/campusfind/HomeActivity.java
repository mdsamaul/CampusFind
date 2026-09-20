package com.example.campusfind;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.campusfind.adapters.ItemAdapter;
import com.example.campusfind.databinding.ActivityHomeBinding;
import com.example.campusfind.models.Item;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();

        binding.fabPost.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, PostItemActivity.class));
        });

        binding.btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, NotificationsActivity.class));
            // Hide badge dot when clicked
            binding.notifBadge.setVisibility(View.GONE);
        });

        // Set up Bottom Navigation (if being used in standalone Activity mode)
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            // Logic for switching
            return true;
        });

        // Dummy Notification Logic: show badge if there are new alerts
        boolean hasNewAlerts = true; 
        binding.notifBadge.setVisibility(hasNewAlerts ? View.VISIBLE : View.GONE);
    }

    private void setupRecyclerView() {
        List<Item> dummyItems = new ArrayList<>();
        dummyItems.add(new Item("1", "Black Wallet", "Accessories", "Lost", "Near Cafeteria", "2 hours ago"));
        dummyItems.add(new Item("2", "iPhone 13", "Electronics", "Found", "Library Room 202", "5 hours ago"));
        dummyItems.add(new Item("3", "Mathematics Book", "Books", "Lost", "Main Building", "1 day ago"));

        binding.rvItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvItems.setAdapter(new ItemAdapter(dummyItems));
    }
}
