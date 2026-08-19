package com.example.campusfind.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusfind.PostItemActivity
import com.example.campusfind.adapters.ItemAdapter
import com.example.campusfind.databinding.FragmentHomeBinding
import com.example.campusfind.models.Item

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        binding.fabPost.setOnClickListener {
            // In a real app with Navigation Component, we'd navigate to PostFragment.
            // For this dummy setup, we can communicate with MainActivity to switch tabs.
            (activity as? MainActivity)?.switchToPostTab()
        }
    }

    private fun setupRecyclerView() {
        val dummyItems = listOf(
            Item("1", "Black Wallet", "Accessories", "Lost", "Near Cafeteria", "2 hours ago"),
            Item("2", "iPhone 13", "Electronics", "Found", "Library Room 202", "5 hours ago"),
            Item("3", "Mathematics Book", "Books", "Lost", "Main Building", "1 day ago"),
            Item("4", "Blue Water Bottle", "Personal", "Found", "Gym", "3 hours ago"),
            Item("5", "Scientific Calculator", "Electronics", "Lost", "Science Lab", "10 mins ago")
        )

        binding.rvItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ItemAdapter(dummyItems)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
