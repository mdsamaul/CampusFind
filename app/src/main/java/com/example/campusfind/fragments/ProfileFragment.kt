package com.example.campusfind.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusfind.LoginActivity
import com.example.campusfind.adapters.ItemAdapter
import com.example.campusfind.databinding.FragmentProfileBinding
import com.example.campusfind.models.Item
import com.google.android.material.tabs.TabLayout

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserProfile()
        setupMyPostsRecyclerView()
        setupTabLayout()

        binding.fabEditPic.setOnClickListener {
            Toast.makeText(requireContext(), "Change Profile Picture", Toast.LENGTH_SHORT).show()
        }

        binding.btnEditProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Edit Profile Clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun setupUserProfile() {
        binding.tvProfileName.text = "John Doe"
        binding.tvProfileId.text = "ID: 202100123"
        binding.tvProfileEmail.text = "john.doe@university.edu"
    }

    private fun setupMyPostsRecyclerView() {
        val myDummyItems = listOf(
            Item("1", "Black Wallet", "Accessories", "Lost", "Near Cafeteria", "2 hours ago"),
            Item("5", "Scientific Calculator", "Electronics", "Lost", "Science Lab", "10 mins ago")
        )

        binding.rvMyPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ItemAdapter(myDummyItems)
        }
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.rvMyPosts.visibility = View.VISIBLE
                        binding.layoutSettings.visibility = View.GONE
                    }
                    1 -> {
                        binding.rvMyPosts.visibility = View.GONE
                        binding.layoutSettings.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
