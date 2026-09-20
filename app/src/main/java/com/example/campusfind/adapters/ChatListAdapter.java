package com.example.campusfind.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.campusfind.databinding.ItemChatListBinding;
import com.example.campusfind.models.Chat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    private final List<Chat> chats;
    private final OnChatClickListener onChatClick;

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    public ChatListAdapter(List<Chat> chats, OnChatClickListener onChatClick) {
        this.chats = chats;
        this.onChatClick = onChatClick;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChatListBinding binding = ItemChatListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ChatViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chats.get(position);
        ItemChatListBinding binding = holder.binding;

        binding.tvChatUserName.setText(chat.getOtherUserName() != null ? chat.getOtherUserName() : "User");
        binding.tvChatItemTitle.setText("Item: " + chat.getItemTitle());
        binding.tvLastMessage.setText(chat.getLastMessage());
        
        if (chat.getTimestamp() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
            binding.tvChatTime.setText(sdf.format(new Date(chat.getTimestamp())));
        } else {
            binding.tvChatTime.setText("");
        }

        binding.getRoot().setOnClickListener(v -> onChatClick.onChatClick(chat));
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        final ItemChatListBinding binding;

        public ChatViewHolder(ItemChatListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
