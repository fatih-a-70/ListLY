package com.example.myappp;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    public interface OnDataChanged {
        void onChanged();
    }

    private final Context context;
    private final List<CategoryItem> categories;
    private final OnDataChanged onDataChanged;
    private SortMode sortMode = SortMode.ALPHABETICAL;

    public CategoryAdapter(Context context, List<CategoryItem> categories, OnDataChanged onDataChanged) {
        this.context = context;
        this.categories = categories;
        this.onDataChanged = onDataChanged;
    }

    public void setSortMode(SortMode mode) {
        this.sortMode = mode;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory;
        RecyclerView rvRows;
        long lastClick = 0;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            rvRows = itemView.findViewById(R.id.rvRows);
        }
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.homebox, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.ViewHolder holder, int position) {
        CategoryItem category = categories.get(position);

        holder.tvCategory.setText(category.name);
        holder.tvCategory.setOnClickListener(v -> {
            long now = System.currentTimeMillis();
            if (now - holder.lastClick < 300) return;
            showCategoryOptions(category, position);
            holder.lastClick = now;
        });

        holder.rvRows.setLayoutManager(
                new LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        );
        holder.rvRows.setNestedScrollingEnabled(false);

        RowItem rowItem = new RowItem();
        rowItem.lists = category.lists;
        java.util.List<RowItem> rowList = new java.util.ArrayList<>();
        rowList.add(rowItem);

        RowAdapter rowAdapter = new RowAdapter(context, rowList, categories);
        rowAdapter.setSortMode(sortMode);
        holder.rvRows.setAdapter(rowAdapter);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    private void showCategoryOptions(CategoryItem cat, int pos) {
        String[] options = {"Edit Category Name", "Delete Category", "Duration"};
        new AlertDialog.Builder(context)
                .setTitle("Category Options")
                .setItems(options, (d, w) -> {
                    if (w == 0) editCategory(cat, pos);
                    else if (w == 1) deleteCategory(pos);
                    else if (w == 2) showCategoryDuration(cat);
                })
                .show();
    }

    private void showCategoryDuration(CategoryItem cat) {
        long createdAt = cat.createdAt != 0 ? cat.createdAt : System.currentTimeMillis();
        long now = System.currentTimeMillis();
        long duration = now - createdAt;

        android.util.Log.d("CategoryAdapter", "totalDurationMs=" + cat.totalDurationMs);

        String created = DateFormat.getDateTimeInstance().format(new Date(createdAt));
        String msg = "Created: " + created + "\n" +
                "Elapsed: " + formatDuration(duration);

        new AlertDialog.Builder(context)
                .setTitle("Category Info")
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }

    private String formatDuration(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        seconds = seconds % 60;
        minutes = minutes % 60;
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void editCategory(CategoryItem cat, int pos) {
        EditText input = new EditText(context);
        input.setText(cat.name);
        new AlertDialog.Builder(context)
                .setTitle("Edit Category")
                .setView(input)
                .setPositiveButton("Update", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        cat.name = name;
                        cat.updatedAt = System.currentTimeMillis();
                        notifyItemChanged(pos);
                        CategoryRepository.get(context).saveCategories(categories);
                        if (onDataChanged != null) onDataChanged.onChanged();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCategory(int pos) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Category?")
                .setMessage("All lists inside this category will be deleted.")
                .setPositiveButton("Delete", (d, w) -> {
                    categories.remove(pos);
                    notifyItemRemoved(pos);
                    CategoryRepository.get(context).saveCategories(categories);
                    if (onDataChanged != null) onDataChanged.onChanged();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
