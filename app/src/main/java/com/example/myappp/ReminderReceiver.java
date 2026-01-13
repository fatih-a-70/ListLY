package com.example.myappp;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String taskName = intent.getStringExtra("TASK_NAME");
        if (taskName == null) taskName = "Task";
        String key = intent.getStringExtra("REMINDER_KEY");
        int requestCode = intent.getIntExtra("REQUEST_CODE", 0);
        String channelId = "reminder_channel";
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    channelId,
                    "Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            nm.createNotificationChannel(ch);
        }
        Intent fullIntent = new Intent(context, ReminderSnoozeActivity.class);
        fullIntent.putExtra("TASK_NAME", taskName);
        fullIntent.putExtra("REMINDER_KEY", key);
        fullIntent.putExtra("REQUEST_CODE", requestCode);
        PendingIntent pi = PendingIntent.getActivity(
                context,
                200,
                fullIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        NotificationCompat.Builder b = new NotificationCompat.Builder(context, channelId)
                .setContentTitle("Reminder")
                .setContentText(taskName)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                .setContentIntent(pi)
                .setAutoCancel(true);
        nm.notify((int) System.currentTimeMillis(), b.build());
    }
}