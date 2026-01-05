package com.example.myappp;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListSorter {

    public static void sortListsAlphabetical(List<ListItem> lists) {
        Collections.sort(lists, Comparator.comparing(l -> l.title.toLowerCase()));
    }

    public static void sortListsNewest(List<ListItem> lists) {
        Collections.sort(lists, (a, b) -> Long.compare(b.createdAt, a.createdAt));
    }

    public static void sortListsOldest(List<ListItem> lists) {
        Collections.sort(lists, (a, b) -> Long.compare(a.createdAt, b.createdAt));
    }

    public static void sortListsByStyle(List<ListItem> lists) {
        Collections.sort(lists, (a, b) -> {
            int s = a.style.ordinal() - b.style.ordinal();
            if (s != 0) return s;
            return a.title.toLowerCase().compareTo(b.title.toLowerCase());
        });
    }
}
