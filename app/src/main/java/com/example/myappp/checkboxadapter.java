package com.example.myappp;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class checkboxadapter extends RecyclerView.Adapter<checkboxadapter.ViewHolder> {

    private List<TaskItem> tasks;
    private Runnable saveCallback;
    private Context context;

    public checkboxadapter(List<TaskItem> tasks, Runnable saveCallback, Context context) {
        this.tasks = tasks;
        this.saveCallback = saveCallback;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkbox;
        long lastClickTime = 0;

        public ViewHolder(View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.itemcheckbox);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkbox, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TaskItem task = tasks.get(position);
        holder.checkbox.setText(task.name);
        holder.checkbox.setChecked(task.checked);

        holder.checkbox.setOnCheckedChangeListener((b, isChecked) -> {
            task.checked = isChecked;
            if (isChecked) task.endTime = System.currentTimeMillis();
            else task.endTime = 0L;
            saveCallback.run();
        });

        holder.checkbox.setOnClickListener(v -> {
            long clickTime = System.currentTimeMillis();
            if (clickTime - holder.lastClickTime < 300) {
                showEditDeleteDialog(position);
            }
            holder.lastClickTime = clickTime;
        });
    }

    private void showEditDeleteDialog(int position) {
        TaskItem task = tasks.get(position);
        EditText input = new EditText(context);
        input.setText(task.name);

        new AlertDialog.Builder(context)
                .setTitle("Edit or Delete")
                .setView(input)
                .setPositiveButton("Update", (d, w) -> {
                    task.name = input.getText().toString();
                    notifyItemChanged(position);
                    saveCallback.run();
                })
                .setNegativeButton("Delete", (d, w) -> {
                    tasks.remove(position);
                    notifyItemRemoved(position);
                    saveCallback.run();
                })
                .setNeutralButton("Show Duration", (d, w) -> {
                    long duration = task.endTime > 0 ? task.endTime - task.startTime : System.currentTimeMillis() - task.startTime;
                    new AlertDialog.Builder(context)
                            .setTitle("Task Duration")
                            .setMessage("Duration: " + (duration/1000) + " seconds")
                            .setPositiveButton("OK", null)
                            .show();
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }
}
