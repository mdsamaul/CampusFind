package com.example.campusfind.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusfind.ChatActivity
import com.example.campusfind.adapters.ChatListAdapter
import com.example.campusfind.databinding.FragmentChatListBinding
import com.example.campusfind.models.Chat

class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val dummyChats = listOf(
            Chat("1", "John Doe", "Black Wallet", "Is this still available?", "12:30 PM"),
            Chat("2", "Jane Smith", "iPhone 13", "I found your phone!", "Yesterday"),
            Chat("3", "Mike Ross", "Scientific Calculator", "Can we meet today?", "Monday")
        )

        binding.rvChatList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ChatListAdapter(dummyChats) { chat ->
                val intent = Intent(requireContext(), ChatActivity::class.java)
                intent.putExtra("USER_NAME", chat.otherUserName)
                intent.putExtra("ITEM_TITLE", chat.itemTitle)
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
