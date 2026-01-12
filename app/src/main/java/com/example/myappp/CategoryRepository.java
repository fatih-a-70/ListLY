package com.example.myappp;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryRepository {

    private static CategoryRepository instance;
    private final FirebaseFirestore db;
    private final String userId;

    private CategoryRepository(Context context) {
        db = FirebaseFirestore.getInstance();
        userId = "demoUser";
    }

    public static CategoryRepository get(Context context) {
        if (instance == null) {
            instance = new CategoryRepository(context.getApplicationContext());
        }
        return instance;
    }

    public void saveCategories(List<CategoryItem> categories) {
        Map<String, Object> data = new HashMap<>();
        data.put("categories", categories);

        db.collection("users")
                .document(userId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("CategoryRepo", "Categories saved successfully"))
                .addOnFailureListener(e -> Log.e("CategoryRepo", "Error saving categories", e));
    }

    public interface OnCategoriesLoaded {
        void onLoaded(List<CategoryItem> categories);
    }

    public void loadCategories(OnCategoriesLoaded listener) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<?> raw = (List<?>) doc.get("categories");
                        if (raw == null) {
                            listener.onLoaded(null);
                            return;
                        }

                        List<CategoryItem> result = new java.util.ArrayList<>();
                        for (Object obj : raw) {
                            if (!(obj instanceof java.util.Map)) continue;
                            java.util.Map<?,?> cm = (java.util.Map<?,?>) obj;

                            CategoryItem cat = new CategoryItem();
                            cat.id = cm.get("id") != null ? cm.get("id").toString() : null;
                            cat.name = cm.get("name") != null ? cm.get("name").toString() : "";
                            Object ca = cm.get("createdAt");
                            cat.createdAt = ca instanceof Number ? ((Number) ca).longValue() : 0L;
                            Object ua = cm.get("updatedAt");
                            cat.updatedAt = ua instanceof Number ? ((Number) ua).longValue() : 0L;

                            cat.lists = new java.util.ArrayList<>();
                            Object listsObj = cm.get("lists");
                            if (listsObj instanceof java.util.List) {
                                for (Object lo : (java.util.List<?>) listsObj) {
                                    if (!(lo instanceof java.util.Map)) continue;
                                    java.util.Map<?,?> lm = (java.util.Map<?,?>) lo;
                                    ListItem li = new ListItem();
                                    li.id = lm.get("id") != null ? lm.get("id").toString() : null;
                                    li.title = lm.get("title") != null ? lm.get("title").toString() : "";
                                    li.prefKey = lm.get("prefKey") != null ? lm.get("prefKey").toString() : null;
                                    Object cr = lm.get("createdAt");
                                    li.createdAt = cr instanceof Number ? ((Number) cr).longValue() : 0L;
                                    Object up = lm.get("updatedAt");
                                    li.updatedAt = up instanceof Number ? ((Number) up).longValue() : 0L;
                                    Object td = lm.get("totalDurationMs");
                                    li.totalDurationMs = td instanceof Number ? ((Number) td).longValue() : 0L;
                                    Object theme = lm.get("themeRes");
                                    li.themeRes = theme instanceof Number ? ((Number) theme).intValue() : R.drawable.p5;
                                    Object color = lm.get("textColor");
                                    li.textColor = color instanceof Number ? ((Number) color).intValue() : 0xFFFFFFFF;
                                    Object fs = lm.get("fontSizeSp");
                                    li.fontSizeSp = fs instanceof Number ? ((Number) fs).floatValue() : 16f;
                                    Object ff = lm.get("fontStyle");
                                    li.fontStyle = ff != null ? ff.toString() : "NORMAL";
                                    Object style = lm.get("style");
                                    if (style != null) {
                                        try { li.style = ListStyle.valueOf(style.toString()); }
                                        catch (Exception ignored) { li.style = ListStyle.CHECKBOX; }
                                    } else {
                                        li.style = ListStyle.CHECKBOX;
                                    }
                                    Object pl = lm.get("previewLayout");
                                    li.previewLayout = pl instanceof Number ? ((Number) pl).intValue() : R.layout.item_checkbox;

                                    cat.lists.add(li);
                                }
                            }

                            result.add(cat);
                        }
                        listener.onLoaded(result);
                    } else {
                        listener.onLoaded(null);
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("CategoryRepo", "Error loading categories", e);
                    listener.onLoaded(null);
                });
    }

}
