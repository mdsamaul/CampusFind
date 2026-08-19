package com.example.campusfind

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.campusfind.databinding.ActivityMainBinding
import com.example.campusfind.fragments.ChatListFragment
import com.example.campusfind.fragments.HomeFragment
import com.example.campusfind.fragments.PostFragment
import com.example.campusfind.fragments.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set default fragment
        loadFragment(HomeFragment())

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_post -> {
                    loadFragment(PostFragment())
                    true
                }
                R.id.nav_chats -> {
                    loadFragment(ChatListFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    fun switchToPostTab() {
        binding.bottomNavigation.selectedItemId = R.id.nav_post
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}
