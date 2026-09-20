package com.example.campusfind;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.campusfind.adapters.NotificationAdapter;
import com.example.campusfind.databinding.ActivityNotificationsBinding;
import com.example.campusfind.models.Notification;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private ActivityNotificationsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        List<Notification> dummyNotifs = new ArrayList<>();
        dummyNotifs.add(new Notification("1", "New message from Rafi", "Hi, I found your wallet!", "5 mins ago", 1));
        dummyNotifs.add(new Notification("2", "Matching item found!", "A new 'Black Wallet' was posted near your location.", "1 hour ago", 2));
        dummyNotifs.add(new Notification("3", "System Alert", "Your post 'Calculator' has been active for 30 days.", "Yesterday", 2));

        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotifications.setAdapter(new NotificationAdapter(dummyNotifs));
        
        // TODO: Connect to Firebase Cloud Messaging (FCM) to receive real notifications
    }
}
