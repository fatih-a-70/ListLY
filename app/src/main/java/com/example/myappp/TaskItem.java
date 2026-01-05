package com.example.myappp;

import java.util.UUID;

public class TaskItem {
    public String id;        // unique id for each task
    public String name;      // task name
    public boolean checked;  // completion status
    public long startTime;   // task start timestamp
    public long endTime;     // task completion timestamp

    // Default constructor
    public TaskItem() {
        this.id = UUID.randomUUID().toString();
    }

    // Constructor with all fields
    public TaskItem(String name, boolean checked, long startTime, long endTime) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.checked = checked;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Optional constructor for new tasks
    public TaskItem(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.checked = false;
        this.startTime = System.currentTimeMillis();
        this.endTime = 0;
    }
}
