package com.example.myappp;

import java.util.ArrayList;
import java.util.List;


    public class CategoryItem {
        public String id;
        public String name;
        public List<ListItem> lists = new ArrayList<>();

        public long createdAt;
        public long updatedAt;

        public long totalDurationMs;

        public CategoryItem() {
            // needed for manual creation / Firestore
        }

        public CategoryItem(String id, String name) {
            this.id = id;
            this.name = name;
            this.createdAt = System.currentTimeMillis();
            this.updatedAt = this.createdAt;
            this.totalDurationMs = 0;
        }

        public void recalcDuration() {
            long sum = 0;
            if (lists != null) {
                for (ListItem li : lists) {
                    sum += li.totalDurationMs;
                }
            }
            totalDurationMs = sum;
        }}


