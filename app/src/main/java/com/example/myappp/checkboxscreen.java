package com.example.myappp;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class checkboxscreen extends AppCompatActivity {

    RecyclerView recyclerView;
    TaskAdapter adapter;
    List<TaskItem> tasks;
    ListStyle style;
    String PREFKEY;
    String listId;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String styleStr = getIntent().getStringExtra("STYLE");
        style = styleStr != null ? ListStyle.valueOf(styleStr) : ListStyle.CHECKBOX;
        int theme = getIntent().getIntExtra("THEME", R.drawable.p5);

        setContentView(getLayoutByStyle(style));

        recyclerView = findViewById(R.id.recycler);
        recyclerView.setBackgroundResource(theme);

        db = FirebaseFirestore.getInstance();
        PREFKEY = getIntent().getStringExtra("PREFKEY");
        listId = getIntent().getStringExtra("LIST_ID");

        String title = getIntent().getStringExtra("TITLE");
        if (title != null) setTitle(title);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasks = new ArrayList<>();
        adapter = new TaskAdapter(this, tasks, style, this::saveTasks);
        recyclerView.setAdapter(adapter);

        loadTasks();

        ImageView add = findViewById(R.id.imagebutton6);
        if (add != null) {
            add.setOnClickListener(v -> showAddDialog());
        }
    }

    private int getLayoutByStyle(ListStyle style) {
        switch (style) {
            case NOTE:
                return R.layout.travel;
            case MEMO:
                return R.layout.deadline;
            case WISHLIST:
            case PLAIN:
            case CHECKBOX:
            default:
                return R.layout.shopping;
        }
    }

    private void loadTasks() {
        if (PREFKEY == null) {
            tasks.clear();
            adapter.notifyDataSetChanged();
            return;
        }
        db.collection("tasks")
                .document(PREFKEY)
                .get()
                .addOnSuccessListener(doc -> {
                    tasks.clear();
                    if (doc.exists()) {
                        List<?> arr = (List<?>) doc.get("items");
                        if (arr != null) {
                            for (Object o : arr) {
                                if (o instanceof java.util.Map) {
                                    java.util.Map<?, ?> m = (java.util.Map<?, ?>) o;
                                    TaskItem t = new TaskItem();
                                    t.id = m.get("id") != null ? m.get("id").toString() : null;
                                    t.name = m.get("name") != null ? m.get("name").toString() : "";
                                    Object c = m.get("checked");
                                    t.checked = c instanceof Boolean && (Boolean) c;
                                    Object st = m.get("startTime");
                                    Object et = m.get("endTime");
                                    t.startTime = st instanceof Number ? ((Number) st).longValue() : System.currentTimeMillis();
                                    t.endTime = et instanceof Number ? ((Number) et).longValue() : 0L;
                                    Object tc = m.get("textColor");
                                    t.textColor = tc instanceof Number ? ((Number) tc).intValue() : 0xFF030320;
                                    Object fs = m.get("fontStyle");
                                    t.fontStyle = fs != null ? fs.toString() : "NORMAL";
                                    tasks.add(t);
                                }
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    updateListDuration();
                });
    }

    private void saveTasks() {
        List<java.util.Map<String, Object>> arr = new ArrayList<>();
        for (TaskItem t : tasks) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", t.id);
            m.put("name", t.name);
            m.put("checked", t.checked);
            m.put("startTime", t.startTime);
            m.put("endTime", t.endTime);
            m.put("textColor", t.textColor);
            m.put("fontStyle", t.fontStyle);
            arr.add(m);
        }
        java.util.Map<String, Object> root = new java.util.HashMap<>();
        root.put("items", arr);
        db.collection("tasks")
                .document(PREFKEY)
                .set(root);

        updateListDuration();
    }

    private void showAddDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Task name");
        new android.app.AlertDialog.Builder(this)
                .setTitle("New Task")
                .setView(input)
                .setPositiveButton("Add", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        TaskItem t = new TaskItem(name);
                        tasks.add(t);
                        adapter.notifyItemInserted(tasks.size() - 1);
                        saveTasks();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void updateListDuration() {
        long total = 0;
        long now = System.currentTimeMillis();
        for (TaskItem t : tasks) {
            long end = t.endTime > 0 ? t.endTime : now;
            long dur = end - t.startTime;
            if (dur > 0) total += dur;
        }

        final long totalFinal = total;
        android.util.Log.d("checkboxscreen", "Computed list duration=" + totalFinal + "ms for listId=" + listId);

        CategoryRepository.get(this).loadCategories(list -> {
            if (list == null) {
                android.util.Log.d("checkboxscreen", "No categories loaded");
                return;
            }
            boolean changed = false;
            for (CategoryItem c : list) {
                if (c.lists == null) continue;
                for (ListItem li : c.lists) {
                    android.util.Log.d("checkboxscreen", "Checking li.id=" + li.id);
                    if (li.id != null && li.id.equals(listId)) {
                        android.util.Log.d("checkboxscreen", "MATCH, updating li.totalDurationMs");
                        li.totalDurationMs = totalFinal;
                        c.recalcDuration();
                        changed = true;
                        break;
                    }
                }
            }
            if (changed) {
                CategoryRepository.get(this).saveCategories(list);
                android.util.Log.d("checkboxscreen", "Saved categories with updated duration");
            } else {
                android.util.Log.d("checkboxscreen", "No matching list found for listId=" + listId);
            }
        });
    }


}
