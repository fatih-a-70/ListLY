package com.example.myappp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    RecyclerView homeRecycler;
    CategoryAdapter adapter;
    List<CategoryItem> categories;
    TextView tvSort2;
    SortMode sortMode = SortMode.ALPHABETICAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView stopwatchBtn = findViewById(R.id.stopwatchBtn);
        stopwatchBtn.setOnClickListener(v ->
                startActivity(new Intent(this, StopwatchActivity.class))
        );

        tvSort2 = findViewById(R.id.tvSort2);
        tvSort2.setOnClickListener(v -> showSortDialog());

        TextView tvHome = findViewById(R.id.tvHome);
        tvHome.setOnClickListener(v -> homeRecycler.smoothScrollToPosition(0));

        homeRecycler = findViewById(R.id.homeRecycler);
        homeRecycler.setLayoutManager(new LinearLayoutManager(this));

        categories = new ArrayList<>();
        adapter = new CategoryAdapter(this, categories, () -> applySort());
        homeRecycler.setAdapter(adapter);

        loadCategories();

        ImageView addButton = findViewById(R.id.imagebutton66);
        addButton.setOnClickListener(v -> showAddListStyleDialog());
    }

    private void loadCategories() {
        CategoryRepository.get(this).loadCategories(list -> {
            categories.clear();
            if (list != null) {
                categories.addAll(list);
            }
            applySort();
            adapter.notifyDataSetChanged();
        });
    }

    private void showSortDialog() {
        String[] options = {
                "Alphabetical",
                "Recent",
                "Creation Time (Oldest First)",
                "List Style"
        };

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Sort Lists")
                .setItems(options, (d, i) -> {
                    if (i == 0) sortMode = SortMode.ALPHABETICAL;
                    if (i == 1) sortMode = SortMode.RECENT;
                    if (i == 2) sortMode = SortMode.OLDEST;
                    if (i == 3) sortMode = SortMode.STYLE;
                    tvSort2.setText(options[i]);
                    applySort();
                    adapter.setSortMode(sortMode);
                })
                .show();
    }

    private void applySort() {
        if (sortMode == SortMode.ALPHABETICAL) {
            Collections.sort(categories, (a, b) -> a.name.toLowerCase().compareTo(b.name.toLowerCase()));
            for (CategoryItem c : categories) {
                ListSorter.sortListsAlphabetical(c.lists);
            }
        } else if (sortMode == SortMode.RECENT) {
            Collections.sort(categories, (a, b) -> Long.compare(b.createdAt, a.createdAt));
            for (CategoryItem c : categories) {
                ListSorter.sortListsNewest(c.lists);
            }
        } else if (sortMode == SortMode.OLDEST) {
            Collections.sort(categories, (a, b) -> Long.compare(a.createdAt, b.createdAt));
            for (CategoryItem c : categories) {
                ListSorter.sortListsOldest(c.lists);
            }
        } else if (sortMode == SortMode.STYLE) {
            Collections.sort(categories, (a, b) -> a.name.toLowerCase().compareTo(b.name.toLowerCase()));
            for (CategoryItem c : categories) {
                ListSorter.sortListsByStyle(c.lists);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showAddListStyleDialog() {
        String[] options = {
                "Checkbox",
                "Wishlist ❤️",
                "Plain List •",
                "Notes 📝",
                "Memo ✍️"
        };

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select List Type")
                .setItems(options, (d, i) -> {
                    ListStyle selected;
                    if (i == 0) selected = ListStyle.CHECKBOX;
                    else if (i == 1) selected = ListStyle.WISHLIST;
                    else if (i == 2) selected = ListStyle.PLAIN;
                    else if (i == 3) selected = ListStyle.NOTE;
                    else selected = ListStyle.MEMO;
                    showCategoryChoiceDialog(selected);
                })
                .show();
    }

    private void showCategoryChoiceDialog(ListStyle style) {
        List<String> names = new ArrayList<>();
        for (CategoryItem c : categories) names.add(c.name);
        names.add("+ New Category");

        ArrayAdapter<String> adapterArr =
                new ArrayAdapter<>(this, android.R.layout.select_dialog_item, names);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Choose Category")
                .setAdapter(adapterArr, (d, i) -> {
                    if (i == names.size() - 1) showCreateCategoryDialog(style);
                    else showCreateListDialog(categories.get(i), style);
                })
                .show();
    }

    private void showCreateCategoryDialog(ListStyle style) {
        EditText input = new EditText(this);
        input.setHint("Category name");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("New Category")
                .setView(input)
                .setPositiveButton("Next", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        long now = System.currentTimeMillis();
                        CategoryItem cat = new CategoryItem(
                                UUID.randomUUID().toString(),
                                name
                        );
                        cat.createdAt = now;
                        cat.updatedAt = now;
                        categories.add(cat);
                        CategoryRepository.get(this).saveCategories(categories);
                        applySort();
                        adapter.notifyDataSetChanged();
                        showCreateListDialog(cat, style);
                    }
                })
                .show();
    }

    private void showCreateListDialog(CategoryItem category, ListStyle style) {
        EditText input = new EditText(this);
        input.setHint("List name");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("New " + style.name() + " List")
                .setView(input)
                .setPositiveButton("Create", (d, w) -> {
                    String title = input.getText().toString().trim();
                    if (!title.isEmpty()) {
                        long now = System.currentTimeMillis();
                        ListItem item = new ListItem();
                        item.id = UUID.randomUUID().toString();
                        item.title = title;
                        item.previewLayout = getLayoutByStyle(style);
                        item.prefKey = "list_" + UUID.randomUUID();
                        item.style = style;
                        item.themeRes = R.drawable.p5;
                        item.textColor = 0xFF030320;
                        item.fontSizeSp = 16;
                        item.fontStyle = "NORMAL";
                        item.createdAt = now;
                        item.updatedAt = now;
                        item.totalDurationMs = 0;
                        category.lists.add(item);
                        category.updatedAt = now;
                        CategoryRepository.get(this).saveCategories(categories);
                        applySort();
                        adapter.notifyDataSetChanged();
                    }
                })
                .show();
    }

    private int getLayoutByStyle(ListStyle style) {
        return R.layout.item_checkbox;
    }
}
