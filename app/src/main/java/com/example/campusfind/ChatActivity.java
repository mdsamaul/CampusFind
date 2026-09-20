package com.example.campusfind;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.campusfind.adapters.MessageAdapter;
import com.example.campusfind.databinding.ActivityChatBinding;
import com.example.campusfind.models.Chat;
import com.example.campusfind.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

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

        // Get data from intent
        receiverId = getIntent().getStringExtra("RECEIVER_ID");
        itemId = getIntent().getStringExtra("ITEM_ID");
        itemTitle = getIntent().getStringExtra("ITEM_TITLE");
        String otherUserName = getIntent().getStringExtra("USER_NAME");

        if (currentUserId == null || receiverId == null || itemId == null) {
            Toast.makeText(this, "Error: Missing chat information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Generate consistent chatId: smallerID_largerID_itemID
        List<String> participants = new ArrayList<>();
        participants.add(currentUserId);
        participants.add(receiverId);
        Collections.sort(participants);
        chatId = participants.get(0) + "_" + participants.get(1) + "_" + itemId;

        // Setup UI
        binding.toolbar.setTitle(otherUserName != null ? otherUserName : "Chat");
        binding.toolbar.setSubtitle("Item: " + itemTitle);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // If name is missing or "Chat", try fetching it from Firestore
        if (otherUserName == null || otherUserName.equals("Chat") || otherUserName.isEmpty()) {
            fetchReceiverName();
        }

        messageList = new ArrayList<>();
        setupRecyclerView();
        listenForMessages();

        binding.btnSend.setOnClickListener(v -> sendMessage());
    }

    private void fetchReceiverName() {
        db.collection("users").document(receiverId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        if (name != null) {
                            binding.toolbar.setTitle(name);
                        }
                    }
                });
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Display messages from bottom
        binding.rvMessages.setLayoutManager(layoutManager);
        binding.rvMessages.setAdapter(adapter);

        // Scroll to bottom when keyboard appears
        binding.rvMessages.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom && !messageList.isEmpty()) {
                binding.rvMessages.postDelayed(() -> 
                    binding.rvMessages.smoothScrollToPosition(messageList.size() - 1), 100);
            }
        });
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
                        for (DocumentSnapshot doc : value.getDocuments()) {
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

        // Chat metadata
        List<String> participants = new ArrayList<>();
        participants.add(currentUserId);
        participants.add(receiverId);
        Collections.sort(participants);

        Chat chatMeta = new Chat(chatId, participants, itemTitle, itemId);
        chatMeta.setLastMessage(content);
        chatMeta.setTimestamp(timestamp);

        // Update both the chat list metadata and the message collection
        db.collection("chats").document(chatId).set(chatMeta);
        db.collection("chats").document(chatId).collection("messages").document(messageId).set(message)
                .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show());
    }
}
