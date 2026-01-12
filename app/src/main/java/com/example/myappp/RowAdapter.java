package com.example.myappp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RowAdapter extends RecyclerView.Adapter<RowAdapter.ViewHolder> {

    private final Context context;
    private final List<RowItem> rows;
    private SortMode sortMode = SortMode.ALPHABETICAL;
    private final List<CategoryItem> allCategories;

    public RowAdapter(Context context, List<RowItem> rows, List<CategoryItem> allCategories) {
        this.context = context;
        this.rows = rows;
        this.allCategories = allCategories;
    }

    public void setSortMode(SortMode mode) {
        this.sortMode = mode;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerView rvHorizontalLists;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            rvHorizontalLists = itemView.findViewById(R.id.rvHorizontalLists);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RowItem row = rows.get(position);
        List<ListItem> lists = row.lists;

        if (lists == null) {
            lists = new java.util.ArrayList<>();
        }

        if (sortMode == SortMode.ALPHABETICAL) {
            ListSorter.sortListsAlphabetical(lists);
        } else if (sortMode == SortMode.RECENT) {
            ListSorter.sortListsNewest(lists);
        } else if (sortMode == SortMode.OLDEST) {
            ListSorter.sortListsOldest(lists);
        } else if (sortMode == SortMode.STYLE) {
            ListSorter.sortListsByStyle(lists);
        }

        LinearLayoutManager lm =
                new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false);
        holder.rvHorizontalLists.setLayoutManager(lm);
        holder.rvHorizontalLists.setNestedScrollingEnabled(false);
        holder.rvHorizontalLists.setHasFixedSize(false);
        holder.rvHorizontalLists.setFocusable(false);
        holder.rvHorizontalLists.setFocusableInTouchMode(false);

        ListAdapter listAdapter = new ListAdapter(context, lists, allCategories);
        holder.rvHorizontalLists.setAdapter(listAdapter);
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }
}
