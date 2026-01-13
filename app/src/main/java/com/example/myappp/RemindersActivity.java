package com.example.myappp;
import android.app.AlertDialog;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
public class RemindersActivity extends AppCompatActivity {
    private TextView task1, task2, task3, task4;
    private TextView time1, time2, time3, time4;
    private Switch sw1, sw2, sw3, sw4;
    private ReminderData r1 = new ReminderData();
    private ReminderData r2 = new ReminderData();
    private ReminderData r3 = new ReminderData();
    private ReminderData r4 = new ReminderData();
    private static class ReminderData {
        String taskName = "";
        String timeText = "";
        boolean enabled = false;
        int hour = -1;
        int minute = -1;
        int requestCode = 0;
    }
    private SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reminders);
        prefs = getSharedPreferences("reminders_prefs", MODE_PRIVATE);
        task1 = findViewById(R.id.textView12);
        task2 = findViewById(R.id.textView20);
        task3 = findViewById(R.id.textView21);
        task4 = findViewById(R.id.textView27);
        time1 = findViewById(R.id.textClock2);
        time2 = findViewById(R.id.textClock3);
        time3 = findViewById(R.id.textClock4);
        time4 = findViewById(R.id.textClock5);
        sw1 = findViewById(R.id.switch3);
        sw2 = findViewById(R.id.switch1);
        sw3 = findViewById(R.id.switch4);
        sw4 = findViewById(R.id.switch5);
        r1.requestCode = 101;
        r2.requestCode = 102;
        r3.requestCode = 103;
        r4.requestCode = 104;
        loadReminder(r1, task1, time1, "r1");
        loadReminder(r2, task2, time2, "r2");
        loadReminder(r3, task3, time3, "r3");
        loadReminder(r4, task4, time4, "r4");
        sw1.setChecked(r1.enabled);
        sw2.setChecked(r2.enabled);
        sw3.setChecked(r3.enabled);
        sw4.setChecked(r4.enabled);
        task1.setOnClickListener(v -> editTaskName(task1, r1, "r1"));
        task2.setOnClickListener(v -> editTaskName(task2, r2, "r2"));
        task3.setOnClickListener(v -> editTaskName(task3, r3, "r3"));
        task4.setOnClickListener(v -> editTaskName(task4, r4, "r4"));
        time1.setOnClickListener(v -> editTime(r1, time1, "r1"));
        time2.setOnClickListener(v -> editTime(r2, time2, "r2"));
        time3.setOnClickListener(v -> editTime(r3, time3, "r3"));
        time4.setOnClickListener(v -> editTime(r4, time4, "r4"));
        sw1.setOnCheckedChangeListener((b, c) -> toggleReminder(r1, c, "r1"));
        sw2.setOnCheckedChangeListener((b, c) -> toggleReminder(r2, c, "r2"));
        sw3.setOnCheckedChangeListener((b, c) -> toggleReminder(r3, c, "r3"));
        sw4.setOnCheckedChangeListener((b, c) -> toggleReminder(r4, c, "r4"));
    }
    private void loadReminder(ReminderData data, TextView taskView, TextView timeView, String key) {
        data.taskName = prefs.getString(key + "_task", taskView.getText().toString());
        data.hour = prefs.getInt(key + "_hour", -1);
        data.minute = prefs.getInt(key + "_minute", -1);
        data.enabled = prefs.getBoolean(key + "_enabled", false);
        if (data.hour >= 0 && data.minute >= 0) {
            data.timeText = String.format("%02d:%02d", data.hour, data.minute);
            timeView.setText(data.timeText);
        }
        taskView.setText(data.taskName);
    }
    private void saveReminder(ReminderData data, String key) {
        SharedPreferences.Editor e = prefs.edit();
        e.putString(key + "_task", data.taskName);
        e.putInt(key + "_hour", data.hour);
        e.putInt(key + "_minute", data.minute);
        e.putBoolean(key + "_enabled", data.enabled);
        e.apply();
    }
    private void editTaskName(TextView tv, ReminderData data, String key) {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setText(tv.getText().toString());
        new AlertDialog.Builder(this)
                .setTitle("Edit task name")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String t = input.getText().toString().trim();
                    if (!t.isEmpty()) {
                        tv.setText(t);
                        data.taskName = t;
                        saveReminder(data, key);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void editTime(ReminderData data, TextView timeView, String key) {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(InputType.TYPE_CLASS_DATETIME);
        input.setHint("HH:MM (24h)");
        new AlertDialog.Builder(this)
                .setTitle("Edit time")
                .setView(input)
                .setPositiveButton("Set", (d, w) -> {
                    String t = input.getText().toString().trim();
                    if (!t.isEmpty() && t.contains(":")) {
                        String[] parts = t.split(":");
                        try {
                            int h = Integer.parseInt(parts[0]);
                            int m = Integer.parseInt(parts[1]);
                            data.hour = h;
                            data.minute = m;
                            data.timeText = String.format("%02d:%02d", h, m);
                            timeView.setText(data.timeText);
                            saveReminder(data, key);
                        } catch (Exception ignored) {
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void toggleReminder(ReminderData data, boolean enable, String key) {
        data.enabled = enable;
        saveReminder(data, key);
        if (!enable) {
            cancelAlarm(data, key);
            return;
        }
        if (data.hour < 0 || data.minute < 0 || data.taskName.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Set task name and time first")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, data.hour);
        c.set(Calendar.MINUTE, data.minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long trigger = c.getTimeInMillis();
        if (trigger < System.currentTimeMillis()) {
            trigger += 24L * 60L * 60L * 1000L;
        }
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (am == null) {
            return;
        }
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("TASK_NAME", data.taskName);
        intent.putExtra("REMINDER_KEY", key);
        intent.putExtra("REQUEST_CODE", data.requestCode);
        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                data.requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi);
        } catch (SecurityException e) {
            new AlertDialog.Builder(this)
                    .setTitle("Exact alarm not allowed")
                    .setMessage("Please allow exact alarms for this app in system settings.")
                    .setPositiveButton("OK", null)
                    .show();
        }
        String msg = "Reminder: " + data.taskName + " at " + data.timeText;
        new AlertDialog.Builder(this)
                .setTitle("Reminder On")
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }
    private void cancelAlarm(ReminderData data, String key) {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (am == null) return;
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("REMINDER_KEY", key);
        intent.putExtra("REQUEST_CODE", data.requestCode);
        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                data.requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        am.cancel(pi);
    }
}