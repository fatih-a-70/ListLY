package com.example.myappp;

import java.util.List;

public class RowItem {
    public String title;
    public List<ListItem> lists;

    public RowItem() {
    }

    public RowItem(String title, List<ListItem> lists) {
        this.title = title;
        this.lists = lists;
    }
}
