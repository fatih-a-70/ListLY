package com.example.myappp;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
public class ReminderSnoozeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String taskName = getIntent().getStringExtra("TASK_NAME");
        final String displayName = (taskName == null) ? "Task" : taskName;
        final String reminderKey = getIntent().getStringExtra("REMINDER_KEY");   // final
        final int requestCode = getIntent().getIntExtra("REQUEST_CODE", 0);      // final
        new AlertDialog.Builder(this)
                .setTitle("Reminder")
                .setMessage(displayName)
                .setPositiveButton("Stop", (d, w) -> {
                    if (reminderKey != null) {
                        cancelAlarm(reminderKey, requestCode);
                    }
                    finish();
                })
                .setNegativeButton("Snooze 5 min", (d, w) -> {
                    if (reminderKey != null) {
                        snoozeFiveMinutes(reminderKey, displayName, requestCode);
                    }
                    finish();
                })
                .show();
    }
    private void cancelAlarm(String key, int requestCode) {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (am == null) return;
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("REMINDER_KEY", key);
        intent.putExtra("REQUEST_CODE", requestCode);
        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        am.cancel(pi);
        SharedPreferences prefs = getSharedPreferences("reminders_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean(key + "_enabled", false).apply();
    }
    private void snoozeFiveMinutes(String key, String taskName, int requestCode) {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (am == null) return;
        long trigger = System.currentTimeMillis() + 5L * 60L * 1000L;
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("TASK_NAME", taskName);
        intent.putExtra("REMINDER_KEY", key);
        intent.putExtra("REQUEST_CODE", requestCode);
        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi);
        } catch (SecurityException ignored) {
        }
        SharedPreferences prefs = getSharedPreferences("reminders_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean(key + "_enabled", true).apply();
    }
}