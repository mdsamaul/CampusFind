package com.example.campusfind.models;

public class Message {
    private String messageId;
    private String chatId;
    private String senderId;
    private String content;
    private long timestamp;
    private String type; // "TEXT", "IMAGE"
    private boolean seen; // true if message has been read by receiver

    public Message() {
        // Required for Firebase
    }

    public Message(String messageId, String chatId, String senderId, String content, long timestamp) {
        this.messageId = messageId;
        this.chatId = chatId;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
        this.type = "TEXT";
        this.seen = false;
    }

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }
}
