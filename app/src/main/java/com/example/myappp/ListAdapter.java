package com.example.myappp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private final Context context;
    private final List<ListItem> lists;
    private final List<CategoryItem> allCategories;

    int[] THEMES = {
            R.drawable.p10, R.drawable.p1, R.drawable.p2, R.drawable.p3,
            R.drawable.p4, R.drawable.p5, R.drawable.p6, R.drawable.p7,
            R.drawable.p8, R.drawable.p9, R.drawable.g0, R.drawable.g9
    };

    public ListAdapter(Context context,
                       List<ListItem> lists,
                       List<CategoryItem> allCategories) {
        this.context = context;
        this.lists = lists;
        this.allCategories = allCategories;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvListTitle;
        FrameLayout previewContainer;
        long lastClick = 0;

        ViewHolder(View v) {
            super(v);
            tvListTitle = v.findViewById(R.id.tvListTitle);
            previewContainer = v.findViewById(R.id.previewContainer);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_list, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        ListItem item = lists.get(position);

        h.tvListTitle.setText(item.title);
        h.tvListTitle.setTextColor(item.textColor);
        h.tvListTitle.setTextSize(item.fontSizeSp);
        if ("BOLD".equals(item.fontStyle)) {
            h.tvListTitle.setTypeface(Typeface.DEFAULT_BOLD);
        } else if ("ITALIC".equals(item.fontStyle)) {
            h.tvListTitle.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        } else {
            h.tvListTitle.setTypeface(Typeface.DEFAULT);
        }

        h.previewContainer.setBackgroundResource(item.themeRes);
        h.previewContainer.removeAllViews();

        View preview = LayoutInflater.from(context)
                .inflate(item.previewLayout, h.previewContainer, false);
        h.previewContainer.addView(preview);

        h.tvListTitle.setOnClickListener(v -> {
            long now = System.currentTimeMillis();
            if (now - h.lastClick < 300) showOptions(item, position);
            h.lastClick = now;
        });

        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, checkboxscreen.class);
            intent.putExtra("STYLE", item.style.name());
            intent.putExtra("PREF_KEY", item.prefKey);
            intent.putExtra("TITLE", item.title);
            intent.putExtra("THEME", item.themeRes);
            intent.putExtra("LIST_ID", item.id);
            context.startActivity(intent);
        });
    }

    private void showOptions(ListItem item, int pos) {
        String[] options = {"Edit Name", "Change Theme", "Text Color", "Font Style/Size", "Delete", "Duration"};

        new AlertDialog.Builder(context)
                .setTitle("List Options")
                .setItems(options, (d, i) -> {
                    if (i == 0) editName(item, pos);
                    if (i == 1) pickTheme(item, pos);
                    if (i == 2) pickTextColor(item, pos);
                    if (i == 3) pickFont(item, pos);
                    if (i == 4) deleteList(pos);
                    if (i == 5) showListDuration(item);
                })
                .show();
    }

    private void showListDuration(ListItem item) {
        long duration = item.totalDurationMs;
        String created = DateFormat.getDateTimeInstance().format(new Date(item.createdAt));
        String msg = "Created: " + created + "\nDuration: " + formatDuration(duration);
        new AlertDialog.Builder(context)
                .setTitle("List Info")
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }

    private String formatDuration(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        seconds %= 60;
        minutes %= 60;
        if (hours > 0) return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void editName(ListItem item, int pos) {
        EditText input = new EditText(context);
        input.setText(item.title);

        new AlertDialog.Builder(context)
                .setTitle("Edit List Name")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        item.title = name;
                        item.updatedAt = System.currentTimeMillis();
                        notifyItemChanged(pos);
                        saveAll();
                    }
                })
                .show();
    }

    private void pickTheme(ListItem item, int pos) {
        String[] names = new String[THEMES.length];
        for (int i = 0; i < names.length; i++) names[i] = "Theme " + i;

        new AlertDialog.Builder(context)
                .setTitle("Select Theme")
                .setItems(names, (d, i) -> {
                    item.themeRes = THEMES[i];
                    item.updatedAt = System.currentTimeMillis();
                    notifyItemChanged(pos);
                    saveAll();
                })
                .show();
    }

    private void pickTextColor(ListItem item, int pos) {
        String[] options = {"Default", "Red", "Blue", "Green"};
        int[] colors = {
                0xFF030320,
                0xFFFF0000,
                0xFF0000FF,
                0xFF008000
        };

        new AlertDialog.Builder(context)
                .setTitle("Select Text Color")
                .setItems(options, (d, i) -> {
                    item.textColor = colors[i];
                    item.updatedAt = System.currentTimeMillis();
                    notifyItemChanged(pos);
                    saveAll();
                })
                .show();
    }

    private void pickFont(ListItem item, int pos) {
        String[] styles = {"Normal", "Bold", "Italic"};

        new AlertDialog.Builder(context)
                .setTitle("Font Style")
                .setItems(styles, (d, styleIndex) -> {
                    String style;
                    if (styleIndex == 1) style = "BOLD";
                    else if (styleIndex == 2) style = "ITALIC";
                    else style = "NORMAL";

                    EditText sizeInput = new EditText(context);
                    sizeInput.setHint("Font size sp");
                    sizeInput.setText(String.valueOf(item.fontSizeSp));

                    new AlertDialog.Builder(context)
                            .setTitle("Font Size")
                            .setView(sizeInput)
                            .setPositiveButton("OK", (d2, w) -> {
                                float sz;
                                try {
                                    sz = Float.parseFloat(sizeInput.getText().toString().trim());
                                } catch (Exception e) {
                                    sz = 16;
                                }
                                item.fontStyle = style;
                                item.fontSizeSp = sz;
                                item.updatedAt = System.currentTimeMillis();
                                notifyItemChanged(pos);
                                saveAll();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                })
                .show();
    }

    private void deleteList(int pos) {
        lists.remove(pos);
        notifyItemRemoved(pos);
        saveAll();
    }

    private void saveAll() {
        CategoryRepository.get(context).saveCategories(allCategories);
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }
}
