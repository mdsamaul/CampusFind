package com.example.campusfind;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.campusfind.databinding.ActivityMainBinding;
import com.example.campusfind.fragments.ChatListFragment;
import com.example.campusfind.fragments.HomeFragment;
import com.example.campusfind.fragments.PostFragment;
import com.example.campusfind.fragments.ProfileFragment;
import com.google.android.material.badge.BadgeDrawable;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isAdmin = getIntent().getBooleanExtra("IS_ADMIN", false);

        // Set default fragment
        HomeFragment homeFragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putBoolean("IS_ADMIN", isAdmin);
        homeFragment.setArguments(args);
        loadFragment(homeFragment);

        setupBottomNavigation();
        setupBadges();
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Bundle args = new Bundle();
            args.putBoolean("IS_ADMIN", isAdmin);

            if (itemId == R.id.nav_home) {
                HomeFragment fragment = new HomeFragment();
                fragment.setArguments(args);
                loadFragment(fragment);
                return true;
            } else if (itemId == R.id.nav_post) {
                PostFragment fragment = new PostFragment();
                fragment.setArguments(args);
                loadFragment(fragment);
                return true;
            } else if (itemId == R.id.nav_chats) {
                ChatListFragment fragment = new ChatListFragment();
                fragment.setArguments(args);
                loadFragment(fragment);
                // Remove badge when user clicks on chats
                BadgeDrawable badge = binding.bottomNavigation.getBadge(R.id.nav_chats);
                if (badge != null) {
                    badge.setVisible(false);
                    badge.clearNumber();
                }
                return true;
            } else if (itemId == R.id.nav_profile) {
                ProfileFragment fragment = new ProfileFragment();
                fragment.setArguments(args);
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void setupBadges() {
        // UI Badge for Chats icon (Dummy count)
        int unreadCount = 3;
        if (unreadCount > 0) {
            BadgeDrawable badge = binding.bottomNavigation.getOrCreateBadge(R.id.nav_chats);
            badge.setVisible(true);
            badge.setNumber(unreadCount);
            badge.setBackgroundColor(getResources().getColor(R.color.lost_red));
        }
    }

    public void switchToPostTab() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_post);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commit();
    }
}
