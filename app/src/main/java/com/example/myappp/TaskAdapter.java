package com.example.myappp;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;
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

                h.cb.setOnCheckedChangeListener((b, isChecked) -> {
                    item.checked = isChecked;
                    if (isChecked) {
                        item.endTime = System.currentTimeMillis();
                    } else {
                        item.endTime = 0;
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
                h.cb.setText("❤️ " + item.name);
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
        String[] options = {"Edit Task", "Delete Task", "Task Duration"};

        new AlertDialog.Builder(context)
                .setTitle("Task Options")
                .setItems(options, (d, i) -> {
                    if (i == 0) editTask(item, pos);
                    if (i == 1) deleteTask(pos);
                    if (i == 2) showTaskDuration(item);
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
                .setPositiveButton("Update", (d, w) -> {
                    item.name = input.getText().toString();
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
        String status = item.checked ? "completed" : "not completed";
        String created = DateFormat.getDateTimeInstance().format(new Date(item.startTime));
        long durationMs;
        if (item.checked && item.endTime > 0) {
            durationMs = item.endTime - item.startTime;
        } else {
            durationMs = System.currentTimeMillis() - item.startTime;
        }
        String msg = "Task status: " + status +
                "\nTask created time: " + created +
                "\nTask duration: " + formatDuration(durationMs);

        new AlertDialog.Builder(context)
                .setTitle("Task Info")
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

    @Override
    public int getItemCount() {
        return items.size();
    }
}
