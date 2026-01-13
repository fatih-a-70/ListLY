package com.example.myappp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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

    private SharedPreferences prefs;
    private static final String PREFS_NAME = "main_prefs";
    private static final String KEY_BG = "main_bg";
    private static final String KEY_USERNAME = "username";

    private final int[] THEMES = new int[]{
            R.drawable.p0, R.drawable.p1, R.drawable.p2, R.drawable.p3,
            R.drawable.p4, R.drawable.p5, R.drawable.p6, R.drawable.p7,
            R.drawable.p8, R.drawable.p9, R.drawable.g0, R.drawable.g9
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        setContentView(R.layout.activity_main);

        applySavedBackground();
        showWelcome();

        ImageView stopwatchBtn = findViewById(R.id.stopwatchBtn);
        stopwatchBtn.setOnClickListener(v ->
                startActivity(new Intent(this, StopwatchActivity.class)));

        ImageView rm = findViewById(R.id.rm);
        rm.setOnClickListener(v ->
                startActivity(new Intent(this, RemindersActivity.class)));

        ImageView moreBtn = findViewById(R.id.imageView2);
        moreBtn.setOnClickListener(v -> showMoreDialog());

        tvSort2 = findViewById(R.id.tvSort2);
        tvSort2.setOnClickListener(v -> showSortDialog());

        TextView tvHome = findViewById(R.id.tvHome);
        tvHome.setOnClickListener(v -> homeRecycler.smoothScrollToPosition(0));

        homeRecycler = findViewById(R.id.homeRecycler);
        homeRecycler.setLayoutManager(new LinearLayoutManager(this));
        categories = new ArrayList<>();
        adapter = new CategoryAdapter(this, categories, this::applySort);
        homeRecycler.setAdapter(adapter);

        loadCategories();

        ImageView addButton = findViewById(R.id.imagebutton66);
        addButton.setOnClickListener(v -> showAddListStyleDialog());
    }

    private void applySavedBackground() {
        int savedBg = prefs.getInt(KEY_BG, R.drawable.p6);
        findViewById(R.id.rootLayout).setBackgroundResource(savedBg);
    }

    private void showWelcome() {
        String name = prefs.getString(KEY_USERNAME, "");
        String msg;
        if (name != null && !name.isEmpty()) {
            msg = "Hello " + name + " !! Welcome to Listly !!";
        } else {
            msg = "Hello ! Welcome to Listly !!";
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void showMoreDialog() {
        String[] options = {"Change Background Theme", "Set User Name"};
        new AlertDialog.Builder(this)
                .setTitle("More Options")
                .setItems(options, (d, which) -> {
                    if (which == 0) showThemeDialog();
                    else showUsernameDialog();
                })
                .show();
    }

    private void showThemeDialog() {
        String[] names = new String[THEMES.length];
        for (int i = 0; i < THEMES.length; i++) {
            names[i] = "Theme " + (i + 1);
        }
        new AlertDialog.Builder(this)
                .setTitle("Select Background Theme")
                .setItems(names, (d, which) -> {
                    int res = THEMES[which];
                    prefs.edit().putInt(KEY_BG, res).apply();
                    applySavedBackground();
                })
                .show();
    }

    private void showUsernameDialog() {
        final EditText input = new EditText(this);
        input.setHint("Enter user name");
        String current = prefs.getString(KEY_USERNAME, "");
        input.setText(current);
        new AlertDialog.Builder(this)
                .setTitle("Set User Name")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    prefs.edit().putString(KEY_USERNAME, name).apply();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories();
    }

    private void loadCategories() {
        CategoryRepository.get(this).loadCategories(list -> {
            categories.clear();
            if (list != null) {
                for (CategoryItem c : list) {
                    long catTotal = 0;
                    if (c.lists != null) {
                        for (ListItem li : c.lists) {
                            catTotal += li.totalDurationMs;
                        }
                    }
                    c.totalDurationMs = catTotal;
                    categories.add(c);
                }
            }
            applySort();
            adapter.notifyDataSetChanged();
        });
    }

    private void showSortDialog() {
        String[] options = {"Alphabetical", "Recent", "Creation Time (Oldest First)", "List Style"};
        new AlertDialog.Builder(this)
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
        String[] options = {"Checkbox", "Wishlist", "Plain List", "Notes", "Memo"};
        new AlertDialog.Builder(this)
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
        for (CategoryItem c : categories) {
            names.add(c.name);
        }
        names.add("New Category");
        ArrayAdapter<String> adapterArr = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_item, names);
        new AlertDialog.Builder(this)
                .setTitle("Choose Category")
                .setAdapter(adapterArr, (d, i) -> {
                    if (i == names.size() - 1) {
                        showCreateCategoryDialog(style);
                    } else {
                        showCreateListDialog(categories.get(i), style);
                    }
                })
                .show();
    }

    private void showCreateCategoryDialog(ListStyle style) {
        EditText input = new EditText(this);
        input.setHint("Category name");
        new AlertDialog.Builder(this)
                .setTitle("New Category")
                .setView(input)
                .setPositiveButton("Next", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        long now = System.currentTimeMillis();
                        CategoryItem cat = new CategoryItem(UUID.randomUUID().toString(), name);
                        cat.createdAt = now;
                        cat.updatedAt = now;
                        categories.add(cat);
                        CategoryRepository.get(this).saveCategories(categories);
                        applySort();
                        adapter.notifyDataSetChanged();
                        showCreateListDialog(cat, style);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showCreateListDialog(CategoryItem category, ListStyle style) {
        EditText input = new EditText(this);
        input.setHint("List name");
        new AlertDialog.Builder(this)
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
                .setNegativeButton("Cancel", null)
                .show();
    }

    private int getLayoutByStyle(ListStyle style) {
        return R.layout.item_checkbox;
    }
}
