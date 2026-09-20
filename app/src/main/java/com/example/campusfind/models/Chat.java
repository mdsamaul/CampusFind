package com.example.campusfind.models;

import java.util.List;

public class Chat {
    private String chatId;
    private List<String> participants;
    private String lastMessage;
    private long timestamp;
    private String itemTitle;
    private String itemId;
    private String otherUserName;

    public Chat() {
        // Required for Firebase
    }

    public Chat(String chatId, List<String> participants, String itemTitle, String itemId) {
        this.chatId = chatId;
        this.participants = participants;
        this.itemTitle = itemTitle;
        this.itemId = itemId;
        this.lastMessage = "";
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }
    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getItemTitle() { return itemTitle; }
    public void setItemTitle(String itemTitle) { this.itemTitle = itemTitle; }
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getOtherUserName() { return otherUserName; }
    public void setOtherUserName(String otherUserName) { this.otherUserName = otherUserName; }
}
