package com.example.campusfind;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.campusfind.adapters.MessageAdapter;
import com.example.campusfind.databinding.ActivityChatBinding;
import com.example.campusfind.models.Chat;
import com.example.campusfind.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

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

    private final ActivityResultLauncher<Intent> chatImagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    Uri imageUri = result.getData().getData();
                    sendImageMessage(imageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();

        if (currentUserId == null) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get data from intent
        String passedChatId = getIntent().getStringExtra("CHAT_ID");
        receiverId = getIntent().getStringExtra("RECEIVER_ID");
        itemId = getIntent().getStringExtra("ITEM_ID");
        itemTitle = getIntent().getStringExtra("ITEM_TITLE");
        String otherUserName = getIntent().getStringExtra("USER_NAME");

        // Determine chatId
        if (passedChatId != null && !passedChatId.isEmpty()) {
            chatId = passedChatId;
        } else if (receiverId != null && !receiverId.isEmpty() && itemId != null && !itemId.isEmpty()) {
            List<String> participants = new ArrayList<>();
            participants.add(currentUserId);
            participants.add(receiverId);
            Collections.sort(participants);
            chatId = participants.get(0) + "_" + participants.get(1) + "_" + itemId;
        } else {
            Toast.makeText(this, "Error: Missing chat information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup UI Toolbar
        binding.tvChatTitle.setText(otherUserName != null && !otherUserName.isEmpty() ? otherUserName : "Chat");
        if (itemTitle != null && !itemTitle.isEmpty()) {
            binding.tvChatSubtitle.setText("Item: " + itemTitle);
            binding.tvChatSubtitle.setVisibility(View.VISIBLE);
        } else {
            binding.tvChatSubtitle.setVisibility(View.GONE);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // Fetch missing metadata (receiver name or chat details) if needed
        loadChatDetailsIfNeeded(otherUserName);

        messageList = new ArrayList<>();
        setupRecyclerView();
        listenForMessages();

        binding.btnSend.setOnClickListener(v -> sendMessage());
        binding.tilMessage.setEndIconOnClickListener(v -> openChatImagePicker());
    }

    private void openChatImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        chatImagePickerLauncher.launch(intent);
    }

    private void sendImageMessage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 35, baos); // Compress image for Firestore
            byte[] imageBytes = baos.toByteArray();
            String base64Image = "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.DEFAULT);

            saveMessageToFirestore(base64Image, "IMAGE", "📷 [Photo]");
        } catch (IOException e) {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadChatDetailsIfNeeded(String otherUserName) {
        db.collection("chats").document(chatId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Chat chat = documentSnapshot.toObject(Chat.class);
                if (chat != null) {
                    if (itemTitle == null || itemTitle.isEmpty()) {
                        itemTitle = chat.getItemTitle();
                        if (itemTitle != null && !itemTitle.isEmpty()) {
                            binding.tvChatSubtitle.setText("Item: " + itemTitle);
                            binding.tvChatSubtitle.setVisibility(View.VISIBLE);
                        }
                    }
                    if (itemId == null || itemId.isEmpty()) {
                        itemId = chat.getItemId();
                    }
                    if ((receiverId == null || receiverId.isEmpty()) && chat.getParticipants() != null) {
                        for (String uid : chat.getParticipants()) {
                            if (!uid.equals(currentUserId)) {
                                receiverId = uid;
                                break;
                            }
                        }
                    }
                    if ((otherUserName == null || otherUserName.isEmpty() || otherUserName.equals("Chat")) && receiverId != null) {
                        fetchReceiverName(receiverId);
                    }
                }
            } else if (receiverId != null && !receiverId.isEmpty()) {
                if (otherUserName == null || otherUserName.isEmpty() || otherUserName.equals("Chat")) {
                    fetchReceiverName(receiverId);
                }
            }
        });
    }

    private void fetchReceiverName(String targetUserId) {
        if (targetUserId == null || targetUserId.isEmpty()) return;
        db.collection("users").document(targetUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        if (name != null && !name.isEmpty()) {
                            binding.tvChatTitle.setText(name);
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

    private boolean isInitialLoad = true;

    private void listenForMessages() {
        db.collection("chats").document(chatId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening for messages", error);
                        return;
                    }
                    if (value != null) {
                        int previousSize = messageList.size();
                        messageList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Message msg = doc.toObject(Message.class);
                            if (msg != null) {
                                messageList.add(msg);

                                // Mark incoming message as seen in Firestore
                                if (currentUserId != null && !currentUserId.equals(msg.getSenderId()) && !msg.isSeen()) {
                                    db.collection("chats").document(chatId)
                                            .collection("messages").document(doc.getId())
                                            .update("seen", true);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if (!messageList.isEmpty()) {
                            binding.rvMessages.scrollToPosition(messageList.size() - 1);

                            // Trigger Push Notification for new incoming message
                            if (!isInitialLoad && messageList.size() > previousSize) {
                                Message lastMsg = messageList.get(messageList.size() - 1);
                                if (currentUserId != null && !currentUserId.equals(lastMsg.getSenderId())) {
                                    String previewText = "IMAGE".equalsIgnoreCase(lastMsg.getType()) ? "📷 Sent a photo" : lastMsg.getContent();
                                    com.example.campusfind.utils.NotificationHelper.showChatNotification(
                                            ChatActivity.this,
                                            binding.tvChatTitle.getText() != null ? binding.tvChatTitle.getText().toString() : "New Message",
                                            previewText,
                                            receiverId,
                                            itemId,
                                            itemTitle
                                    );
                                }
                            }
                        }
                        isInitialLoad = false;
                    }
                });
    }

    private void sendMessage() {
        if (binding.etMessage.getText() == null) return;
        String content = binding.etMessage.getText().toString().trim();
        if (content.isEmpty()) return;

        binding.etMessage.getText().clear();
        saveMessageToFirestore(content, "TEXT", content);
    }

    private void saveMessageToFirestore(String content, String type, String lastMessagePreview) {
        String messageId = db.collection("chats").document(chatId).collection("messages").document().getId();
        long timestamp = System.currentTimeMillis();

        Message message = new Message(messageId, chatId, currentUserId, content, timestamp);
        message.setType(type);

        // Chat metadata
        List<String> participants = new ArrayList<>();
        participants.add(currentUserId);
        if (receiverId != null && !receiverId.isEmpty()) {
            participants.add(receiverId);
        }
        Collections.sort(participants);

        Chat chatMeta = new Chat(chatId, participants, itemTitle != null ? itemTitle : "", itemId != null ? itemId : "");
        chatMeta.setLastMessage(lastMessagePreview);
        chatMeta.setTimestamp(timestamp);

        // Update both the chat list metadata and the message collection
        db.collection("chats").document(chatId).set(chatMeta, SetOptions.merge());
        db.collection("chats").document(chatId).collection("messages").document(messageId).set(message)
                .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
