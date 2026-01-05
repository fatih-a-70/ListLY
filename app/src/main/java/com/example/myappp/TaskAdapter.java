package com.example.myappp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

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
                        item.endTime = 0; // reopened task
                    }

                    saveCallback.run();
                });
                break;


            case WISHLIST:
                h.cb.setText("❤️ " + item.name);
                break;


            case PLAIN:
                h.cb.setText("• " + item.name);
                break;

            case NOTE:

                h.cb.setText(item.name);
                break;


            case MEMO:

                h.cb.setText(item.name);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
