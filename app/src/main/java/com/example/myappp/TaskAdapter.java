package com.example.myappp;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.VH> {
    Context context;
    List<TaskItem> items;
    ListStyle style;
    Runnable saveCallback;

    public TaskAdapter(Context context,
                       List<TaskItem> items,
                       ListStyle style,
                       Runnable saveCallback) {
        this.context = context;
        this.items = items;
        this.style = style;
        this.saveCallback = saveCallback;
    }

    static class VH extends RecyclerView.ViewHolder {
        CheckBox cb;
        long lastClickTime = 0;

        VH(View v) {
            super(v);
            cb = v.findViewById(R.id.itemcheckbox);
        }
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_checkbox, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH h, int position) {
        TaskItem item = items.get(position);
        h.cb.setOnCheckedChangeListener(null);
        h.cb.setChecked(false);
        h.cb.setButtonDrawable(null);

        switch (style) {
            case CHECKBOX:
                h.cb.setButtonDrawable(
                        ContextCompat.getDrawable(
                                context,
                                android.R.drawable.checkbox_on_background
                        )
                );
                h.cb.setText(item.name);
                h.cb.setChecked(item.checked);

                // Apply saved text color and font style
                h.cb.setTextColor(item.textColor);
                if ("BOLD".equals(item.fontStyle)) {
                    h.cb.setTypeface(Typeface.DEFAULT_BOLD);
                } else if ("ITALIC".equals(item.fontStyle)) {
                    h.cb.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                } else {
                    h.cb.setTypeface(Typeface.DEFAULT);
                }
                // If you later add size field to TaskItem (e.g. float fontSizeSp), also do:
                // h.cb.setTextSize(item.fontSizeSp);

                h.cb.setOnCheckedChangeListener((b, isChecked) -> {
                    long now = System.currentTimeMillis();
                    item.checked = isChecked;
                    if (isChecked) {
                        if (item.startTime == 0L) {
                            item.startTime = now;
                        }
                        item.endTime = now;
                    } else {
                        item.endTime = now;
                    }
                    saveCallback.run();
                });

                h.cb.setOnClickListener(v -> {
                    long now = System.currentTimeMillis();
                    if (now - h.lastClickTime < 300) showTaskOptions(item, position);
                    h.lastClickTime = now;
                });
                break;

            case WISHLIST:
                h.cb.setText(" ✨ " + item.name);
                h.cb.setOnClickListener(v -> {
                    long now = System.currentTimeMillis();
                    if (now - h.lastClickTime < 300) showSimpleTaskOptions(position);
                    h.lastClickTime = now;
                });
                break;

            case PLAIN:
                h.cb.setText("• " + item.name);
                h.cb.setOnClickListener(v -> {
                    long now = System.currentTimeMillis();
                    if (now - h.lastClickTime < 300) showSimpleTaskOptions(position);
                    h.lastClickTime = now;
                });
                break;

            case NOTE:
                h.cb.setText(item.name);
                h.cb.setOnClickListener(v -> {
                    long now = System.currentTimeMillis();
                    if (now - h.lastClickTime < 300) showSimpleTaskOptions(position);
                    h.lastClickTime = now;
                });
                break;

            case MEMO:
                h.cb.setText(item.name);
                h.cb.setOnClickListener(v -> {
                    long now = System.currentTimeMillis();
                    if (now - h.lastClickTime < 300) showSimpleTaskOptions(position);
                    h.lastClickTime = now;
                });
                break;
        }
    }

    private void showTaskOptions(TaskItem item, int pos) {
        String[] options = {"Edit Task", "Delete Task", "Task Duration", "Text Color", "Font Style"};
        new AlertDialog.Builder(context)
                .setTitle("Task Options")
                .setItems(options, (d, i) -> {
                    if (i == 0) editTask(item, pos);
                    if (i == 1) deleteTask(pos);
                    if (i == 2) showTaskDuration(item);
                    if (i == 3) pickTextColor(item, pos);
                    if (i == 4) pickFontStyle(item, pos);
                })
                .show();
    }

    private void showSimpleTaskOptions(int pos) {
        TaskItem item = items.get(pos);
        EditText input = new EditText(context);
        input.setText(item.name);
        new AlertDialog.Builder(context)
                .setTitle("Edit or Delete")
                .setView(input)
                .setPositiveButton("Update", (d, w) -> {
                    item.name = input.getText().toString();
                    notifyItemChanged(pos);
                    saveCallback.run();
                })
                .setNegativeButton("Delete", (d, w) -> {
                    items.remove(pos);
                    notifyItemRemoved(pos);
                    saveCallback.run();
                })
                .show();
    }

    private void editTask(TaskItem item, int pos) {
        EditText input = new EditText(context);
        input.setText(item.name);
        new AlertDialog.Builder(context)
                .setTitle("Edit Task")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    item.name = input.getText().toString().trim();
                    notifyItemChanged(pos);
                    saveCallback.run();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTask(int pos) {
        items.remove(pos);
        notifyItemRemoved(pos);
        saveCallback.run();
    }

    private void showTaskDuration(TaskItem item) {
        long now = System.currentTimeMillis();
        long end = item.endTime > 0 ? item.endTime : now;
        long dur = end - item.startTime;
        if (dur < 0) dur = 0;
        String msg = "Duration: " + (dur / 1000) + " seconds";
        new AlertDialog.Builder(context)
                .setTitle("Task Duration")
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }

    private void pickTextColor(TaskItem item, int pos) {
        String[] options = {"White", "Red", "Blue", "Black"};
        int[] colors = {
                0xFFFFFFFF,
                0xFFFF0000,
                0xFF0000FF,
                0x00000000
        };
        new AlertDialog.Builder(context)
                .setTitle("Select Text Color")
                .setItems(options, (d, i) -> {
                    item.textColor = colors[i];
                    notifyItemChanged(pos);
                    saveCallback.run();
                })
                .show();
    }

    private void pickFontStyle(TaskItem item, int pos) {
        String[] styles = {"Normal", "Bold", "Italic"};
        new AlertDialog.Builder(context)
                .setTitle("Font Style")
                .setItems(styles, (d, i) -> {
                    if (i == 1) item.fontStyle = "BOLD";
                    else if (i == 2) item.fontStyle = "ITALIC";
                    else item.fontStyle = "NORMAL";
                    notifyItemChanged(pos);
                    saveCallback.run();
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
