package com.example.campusfind;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.campusfind.adapters.MessageAdapter;
import com.example.campusfind.databinding.ActivityChatBinding;
import com.example.campusfind.models.Chat;
import com.example.campusfind.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String receiverId;
    private String itemId;
    private String itemTitle;
    private String chatId;
    private List<Message> messageList;
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();

        receiverId = getIntent().getStringExtra("RECEIVER_ID");
        itemId = getIntent().getStringExtra("ITEM_ID");
        itemTitle = getIntent().getStringExtra("ITEM_TITLE");
        String otherUserName = getIntent().getStringExtra("USER_NAME");

        if (currentUserId == null || receiverId == null || itemId == null) {
            Toast.makeText(this, "Error: Missing chat information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Generate consistent chatId
        List<String> participants = new ArrayList<>();
        participants.add(currentUserId);
        participants.add(receiverId);
        Collections.sort(participants);
        chatId = participants.get(0) + "_" + participants.get(1) + "_" + itemId;

        binding.toolbar.setTitle(otherUserName != null ? otherUserName : "Chat");
        binding.toolbar.setSubtitle("Item: " + itemTitle);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        messageList = new ArrayList<>();
        setupRecyclerView();
        listenForMessages();

        binding.btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(messageList);
        binding.rvMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMessages.setAdapter(adapter);
    }

    private void listenForMessages() {
        db.collection("chats").document(chatId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        messageList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            Message msg = doc.toObject(Message.class);
                            if (msg != null) {
                                messageList.add(msg);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if (!messageList.isEmpty()) {
                            binding.rvMessages.scrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }

    private void sendMessage() {
        String content = binding.etMessage.getText().toString().trim();
        if (content.isEmpty()) return;

        binding.etMessage.getText().clear();

        String messageId = db.collection("chats").document(chatId).collection("messages").document().getId();
        long timestamp = System.currentTimeMillis();

        Message message = new Message(messageId, chatId, currentUserId, content, timestamp);

        // Update chat meta-data
        List<String> participants = new ArrayList<>();
        participants.add(currentUserId);
        participants.add(receiverId);
        
        Chat chatMeta = new Chat(chatId, participants, itemTitle, itemId);
        chatMeta.setLastMessage(content);
        chatMeta.setTimestamp(timestamp);

        // Batch write or sequential write
        db.collection("chats").document(chatId).set(chatMeta);
        db.collection("chats").document(chatId).collection("messages").document(messageId).set(message);
    }
}
