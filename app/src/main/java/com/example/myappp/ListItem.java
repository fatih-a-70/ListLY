package com.example.myappp;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class ListItem {

    public String id;
    public String title;
    public String prefKey;
    public ListStyle style;
    public int themeRes;
    public int previewLayout;
    public int textColor;
    public float fontSizeSp;
    public String fontStyle;
    public long createdAt;
    public long updatedAt;
    public long totalDurationMs;

    public ListItem() {
    }

    public ListItem(String id,
                    String title,
                    int previewLayout,
                    String prefKey,
                    ListStyle style,
                    int themeRes) {
        this.id = id;
        this.title = title;
        this.previewLayout = previewLayout;
        this.prefKey = prefKey;
        this.style = style;
        this.themeRes = themeRes;
        this.textColor = 0xFF030320;
        this.fontSizeSp = 16;
        this.fontStyle = "NORMAL";
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
        this.totalDurationMs = 0;
    }
}
