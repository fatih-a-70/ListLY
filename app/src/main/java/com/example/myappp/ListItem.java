package com.example.myappp;

import java.util.ArrayList;
import java.util.List;

public class ListItem {
    public String id;
    public String title;
    public int previewLayout;
    public String prefKey;
    public ListStyle style;
    public int themeRes;
    public int textColor;
    public float fontSizeSp;
    public String fontStyle;
    public long createdAt;
    public long updatedAt;
    public long totalDurationMs;
    public List<TaskItem> tasks = new ArrayList<>();
}
