package com.example.campusfind.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.campusfind.ChatActivity;
import com.example.campusfind.adapters.ChatListAdapter;
import com.example.campusfind.databinding.FragmentChatListBinding;
import com.example.campusfind.models.Chat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {

    private FragmentChatListBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Chat> chatList;
    private ChatListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatListBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        chatList = new ArrayList<>();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        fetchChats();
    }

    private void setupRecyclerView() {
        adapter = new ChatListAdapter(chatList, chat -> {
            Intent intent = new Intent(requireContext(), ChatActivity.class);
            intent.putExtra("CHAT_ID", chat.getChatId());
            intent.putExtra("ITEM_ID", chat.getItemId());
            intent.putExtra("ITEM_TITLE", chat.getItemTitle());
            
            // Determine other user ID
            String otherUserId = "";
            for (String uid : chat.getParticipants()) {
                if (mAuth.getUid() != null && !uid.equals(mAuth.getUid())) {
                    otherUserId = uid;
                    break;
                }
            }
            intent.putExtra("RECEIVER_ID", otherUserId);
            intent.putExtra("USER_NAME", chat.getOtherUserName());
            startActivity(intent);
        });
        binding.rvChatList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvChatList.setAdapter(adapter);
    }

    private void fetchChats() {
        if (mAuth.getUid() == null) return;

        db.collection("chats")
                .whereArrayContains("participants", mAuth.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        chatList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            Chat chat = doc.toObject(Chat.class);
                            if (chat != null) {
                                chat.setChatId(doc.getId());
                                chatList.add(chat);
                                fetchOtherUserInfo(chat);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        
                        if (chatList.isEmpty()) {
                            binding.tvEmptyChats.setVisibility(View.VISIBLE);
                        } else {
                            binding.tvEmptyChats.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void fetchOtherUserInfo(Chat chat) {
        String otherUserId = "";
        for (String uid : chat.getParticipants()) {
            if (mAuth.getUid() != null && !uid.equals(mAuth.getUid())) {
                otherUserId = uid;
                break;
            }
        }

        if (otherUserId.isEmpty()) return;

        db.collection("users").document(otherUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        chat.setOtherUserName(name);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
