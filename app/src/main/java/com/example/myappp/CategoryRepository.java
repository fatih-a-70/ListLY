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
                        List<CategoryItem> categories =
                                (List<CategoryItem>) doc.get("categories");
                        listener.onLoaded(categories);
                    } else {
                        listener.onLoaded(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CategoryRepo", "Error loading categories", e);
                    listener.onLoaded(null);
                });
    }
}
