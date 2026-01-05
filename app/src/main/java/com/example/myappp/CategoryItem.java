package com.example.myappp;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class CategoryItem {

    public String id;
    public String name;
    public List<ListItem> lists = new ArrayList<>();
    public long createdAt;
    public long updatedAt;
    public long totalDurationMs;

    public CategoryItem() {
    }

    public CategoryItem(String id, String name) {
        this.id = id;
        this.name = name;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
        this.totalDurationMs = 0;
    }

    @Exclude
    public boolean isEmpty() {
        return name == null || name.isEmpty();
    }
}
