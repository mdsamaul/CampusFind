package com.example.campusfind;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.campusfind.adapters.ChatListAdapter;
import com.example.campusfind.databinding.ActivityChatListBinding;
import com.example.campusfind.models.Chat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private ActivityChatListBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Chat> chatList;
    private ChatListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        chatList = new ArrayList<>();

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Chats");
        }

        setupRecyclerView();
        fetchChats();
    }

    private void setupRecyclerView() {
        adapter = new ChatListAdapter(chatList, chat -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("CHAT_ID", chat.getChatId());
            intent.putExtra("ITEM_ID", chat.getItemId());
            intent.putExtra("ITEM_TITLE", chat.getItemTitle());
            
            // Determine other user ID
            String otherUserId = "";
            for (String uid : chat.getParticipants()) {
                if (!uid.equals(mAuth.getUid())) {
                    otherUserId = uid;
                    break;
                }
            }
            intent.putExtra("RECEIVER_ID", otherUserId);
            intent.putExtra("USER_NAME", chat.getOtherUserName());
            startActivity(intent);
        });
        binding.rvChatList.setLayoutManager(new LinearLayoutManager(this));
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
                                // We'll need to fetch the other user's name for each chat
                                // For now, we'll use a placeholder or previous name if available
                                chatList.add(chat);
                                fetchOtherUserInfo(chat);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void fetchOtherUserInfo(Chat chat) {
        String otherUserId = "";
        for (String uid : chat.getParticipants()) {
            if (!uid.equals(mAuth.getUid())) {
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
}
