package com.example.campusfind.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campusfind.databinding.ItemChatListBinding
import com.example.campusfind.models.Chat

class ChatListAdapter(
    private val chats: List<Chat>,
    private val onChatClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    class ChatViewHolder(val binding: ItemChatListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        with(holder.binding) {
            tvChatUserName.text = chat.otherUserName
            tvChatItemTitle.text = "Item: ${chat.itemTitle}"
            tvLastMessage.text = chat.lastMessage
            tvChatTime.text = chat.time
            
            root.setOnClickListener { onChatClick(chat) }
        }
    }

    override fun getItemCount() = chats.size
}
