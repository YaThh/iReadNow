package com.example.ungdungdocsach.Model;

public class Category {
    String category, uid;
    long timestamp;

    public Category() {
    }

    public Category(String category, String uid, long timestamp) {
        this.category = category;
        this.uid = uid;
        this.timestamp = timestamp;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
