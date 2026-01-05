package com.example.myappp;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for saving/loading categories from Firebase Firestore.
 * Adapters call saveCategories() and getCategories() as before.
 */
public class CategoryRepository {

    private static CategoryRepository instance;
    private final FirebaseFirestore db;
    private final String userId; // You should set this per user (e.g. FirebaseAuth UID)

    private CategoryRepository(Context context) {
        db = FirebaseFirestore.getInstance();
        // TODO: Replace with actual user ID from FirebaseAuth or your login system
        userId = "demoUser";
    }

    public static CategoryRepository get(Context context) {
        if (instance == null) {
            instance = new CategoryRepository(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Save all categories to Firestore.
     */
    public void saveCategories(List<CategoryItem> categories) {
        Map<String, Object> data = new HashMap<>();
        data.put("categories", categories);

        db.collection("users")
                .document(userId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid ->
                        Log.d("CategoryRepo", "Categories saved successfully"))
                .addOnFailureListener(e ->
                        Log.e("CategoryRepo", "Error saving categories", e));
    }

    /**
     * Load categories from Firestore.
     * You can call this asynchronously and update your adapters when data arrives.
     */
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

    public interface OnCategoriesLoaded {
        void onLoaded(List<CategoryItem> categories);
    }
}
