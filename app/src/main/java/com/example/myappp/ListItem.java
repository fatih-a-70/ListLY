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
}
