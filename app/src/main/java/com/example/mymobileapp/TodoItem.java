package com.example.mymobileapp;

import com.google.firebase.firestore.Exclude;

public class TodoItem {
    private String documentId;
    private String title;
    private String userId;
    private boolean isCompleted;

    // Required empty constructor for Firestore
    public TodoItem() {}

    public TodoItem(String title, String userId) {
        this.title = title;
        this.userId = userId;
        this.isCompleted = false;
    }

    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(boolean completed) {
        isCompleted = completed;
    }
    
    // Compatibility methods for existing adapter logic if needed
    @Exclude
    public String getTaskName() {
        return title;
    }

    public void setTaskName(String taskName) {
        this.title = taskName;
    }

    @Exclude
    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
