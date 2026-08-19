package com.example.campusfind

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusfind.adapters.MessageAdapter
import com.example.campusfind.databinding.ActivityChatBinding
import com.example.campusfind.models.Message

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userName = intent.getStringExtra("USER_NAME") ?: "Chat"
        val itemTitle = intent.getStringExtra("ITEM_TITLE") ?: "Unknown Item"

        binding.toolbar.title = userName
        binding.toolbar.subtitle = "Item: $itemTitle"
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString()
            if (text.isNotEmpty()) {
                // TODO: Send message to Firebase
                binding.etMessage.text?.clear()
            }
        }
    }

    private fun setupRecyclerView() {
        val dummyMessages = listOf(
            Message("1", "Hi, is this still available?", "10:00 AM", false),
            Message("2", "Yes, I still have it.", "10:05 AM", true),
            Message("3", "Great! Where can we meet?", "10:10 AM", false),
            Message("4", "I'm near the library right now.", "10:15 AM", true)
        )

        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = MessageAdapter(dummyMessages)
            scrollToPosition(dummyMessages.size - 1)
        }
    }
}
