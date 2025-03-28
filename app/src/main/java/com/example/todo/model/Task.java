package com.example.todo.model;

import java.util.HashMap;
import java.util.Map;

public class Task {
    private String id;
    private String title;
    private String description;
    private int priority; // 0: Low, 1: Medium, 2: High
    private boolean completed;
    private long timestamp;
    private String category;
    private long dueDate; // timestamp for due date

    // Empty constructor needed for Firebase
    public Task() {
    }

    public Task(String id, String title, String description, int priority, boolean completed, String category, long dueDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.completed = completed;
        this.category = category != null ? category : "General";
        this.timestamp = System.currentTimeMillis();
        this.dueDate = dueDate;
    }

    // Convert Task to Map for Firebase
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("title", title);
        result.put("description", description);
        result.put("priority", priority);
        result.put("completed", completed);
        result.put("category", category);
        result.put("timestamp", timestamp);
        result.put("dueDate", dueDate);
        return result;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCategory() {
        return category != null ? category : "General";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }
} 