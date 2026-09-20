package com.example.campusfind.models;

import java.util.HashMap;
import java.util.Map;

public class Item {
    private String id;
    private String title;
    private String category;
    private String type; // "Lost" or "Found"
    private String location;
    private String description;
    private String imageUrl;
    private String posterId;
    private String status; // "Active", "Resolved"
    private long timestamp;
    private String contactInfo;
    private double latitude;
    private double longitude;

    // Default constructor for Firebase
    public Item() {
    }

    // Constructor for dummy data/convenience
    public Item(String id, String title, String category, String type, String location, String description) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.type = type;
        this.location = location;
        this.description = description;
    }

    // Full constructor
    public Item(String id, String title, String category, String type, String location, String description, String imageUrl, String posterId, String status, long timestamp, String contactInfo) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.type = type;
        this.location = location;
        this.description = description;
        this.imageUrl = imageUrl;
        this.posterId = posterId;
        this.status = status;
        this.timestamp = timestamp;
        this.contactInfo = contactInfo;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getPosterId() { return posterId; }
    public void setPosterId(String posterId) { this.posterId = posterId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("title", title);
        map.put("category", category);
        map.put("type", type);
        map.put("location", location);
        map.put("description", description);
        map.put("imageUrl", imageUrl);
        map.put("posterId", posterId);
        map.put("status", status);
        map.put("timestamp", timestamp);
        map.put("contactInfo", contactInfo);
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        return map;
    }
}
