package com.example.campusfind.models;

public class Notification {
    private String id;
    private String title;
    private String message;
    private String time;
    private int type; // 1 for message, 2 for match, etc.

    public Notification(String id, String title, String message, String time, int type) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.time = time;
        this.type = type;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public int getType() { return type; }
}
