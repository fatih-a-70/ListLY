package com.example.myappp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class checkboxscreen extends AppCompatActivity {

    private List<TaskItem> tasks;
    private TaskAdapter adapter;
    private String PREF_KEY;
    private ListStyle style;
    private FirebaseFirestore db;
    private String listId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String styleStr = getIntent().getStringExtra("STYLE");
        style = styleStr != null ? ListStyle.valueOf(styleStr) : ListStyle.CHECKBOX;

        int theme = getIntent().getIntExtra("THEME", R.drawable.p5);

        setContentView(getLayoutByStyle(style));

        RecyclerView rv = findViewById(R.id.recycler);
        rv.setBackgroundResource(theme);

        db = FirebaseFirestore.getInstance();

        PREF_KEY = getIntent().getStringExtra("PREF_KEY");
        listId = getIntent().getStringExtra("LIST_ID");
        setTitle(getIntent().getStringExtra("TITLE"));

        rv.setLayoutManager(new LinearLayoutManager(this));

        tasks = new ArrayList<>();
        adapter = new TaskAdapter(this, tasks, style, this::saveTasks);
        rv.setAdapter(adapter);

        loadTasks();

        ImageView add = findViewById(R.id.imagebutton6);
        add.setOnClickListener(v -> showAddDialog());
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
        db.collection("tasks")
                .document(PREF_KEY)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    List<Map<String, Object>> list =
                            (List<Map<String, Object>>) doc.get("tasks");

                    if (list == null) return;

                    tasks.clear();

                    for (Map<String, Object> m : list) {
                        TaskItem t = new TaskItem();
                        t.name = (String) m.get("name");
                        t.checked = Boolean.TRUE.equals(m.get("checked"));
                        t.startTime = m.get("startTime") == null ? 0 : (long) m.get("startTime");
                        t.endTime = m.get("endTime") == null ? 0 : (long) m.get("endTime");
                        tasks.add(t);
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void showAddDialog() {
        EditText input = new EditText(this);
        input.setHint(style == ListStyle.MEMO ? "Max 50 words" : "Text");

        new AlertDialog.Builder(this)
                .setTitle("Add Item")
                .setView(input)
                .setPositiveButton("Add", (d, w) -> {
                    String text = input.getText().toString().trim();

                    if (style == ListStyle.MEMO && text.split("\\s+").length > 50) return;

                    if (!TextUtils.isEmpty(text)) {
                        tasks.add(new TaskItem(text));
                        adapter.notifyItemInserted(tasks.size() - 1);
                        saveTasks();
                    }
                })
                .show();
    }

    private void saveTasks() {
        List<Map<String, Object>> save = new ArrayList<>();

        for (TaskItem t : tasks) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", t.name);
            m.put("checked", t.checked);
            m.put("startTime", t.startTime);
            m.put("endTime", t.endTime);
            save.add(m);
        }

        db.collection("tasks")
                .document(PREF_KEY)
                .set(Collections.singletonMap("tasks", save));

        updateListAndCategoryDurations();
    }

    private void updateListAndCategoryDurations() {
        db.collection("users")
                .document("demoUser")
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    List<?> catList = (List<?>) doc.get("categories");
                    if (catList == null) return;

                    boolean changed = false;
                    List<Map<String, Object>> cats = new java.util.ArrayList<>();

                    for (Object cObj : catList) {
                        Map<String, Object> c = (Map<String, Object>) cObj;
                        List<?> listList = (List<?>) c.get("lists");
                        long catTotal = 0;

                        if (listList != null) {
                            java.util.List<Map<String, Object>> newLists = new java.util.ArrayList<>();
                            for (Object lObj : listList) {
                                Map<String, Object> l = (Map<String, Object>) lObj;
                                String id = (String) l.get("id");

                                if (id != null && id.equals(listId)) {
                                    long listTotal = 0;
                                    long now = System.currentTimeMillis();
                                    for (TaskItem t : tasks) {
                                        if (t.startTime == 0) continue;
                                        if (t.checked && t.endTime > 0) {
                                            listTotal += (t.endTime - t.startTime);
                                        } else {
                                            listTotal += (now - t.startTime);
                                        }
                                    }
                                    l.put("totalDurationMs", Long.valueOf(listTotal));
                                    changed = true;
                                }

                                Object dObj = l.get("totalDurationMs");
                                if (dObj instanceof Number) {
                                    catTotal += ((Number) dObj).longValue();
                                }

                                newLists.add(l);
                            }
                            c.put("lists", newLists);
                        }

                        c.put("totalDurationMs", Long.valueOf(catTotal));
                        cats.add(c);
                    }

                    if (changed) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("categories", cats);
                        db.collection("users")
                                .document("demoUser")
                                .set(data);
                    }
                });
    }


}
