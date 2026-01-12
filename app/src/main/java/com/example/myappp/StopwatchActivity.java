package com.example.myappp;

import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StopwatchActivity extends AppCompatActivity {

    private TextView timeText;
    private TextView modeLabel;
    private Button startBtn;
    private Button stopBtn;
    private Button resetBtn;

    private Button focusModeBtn;
    private Button historyBtn;

    private Button tenMinBtn;
    private Button thirtyMinBtn;
    private Button oneHourBtn;
    private Button threeHourBtn;
    private Button customBtn;

    private Handler handler = new Handler();
    private long baseTimeMs = 0;
    private long displayMs = 0;
    private boolean running = false;

    private enum FocusMode {
        STOPWATCH,
        COUNTDOWN_FORWARD,
        COUNTDOWN_BACKWARD
    }

    private FocusMode mode = FocusMode.STOPWATCH;
    private long presetMs = 0;
    private long sessionStartMs = 0;

    private static final List<FocusSession> sessions = new ArrayList<>();

    private static class FocusSession {
        long durationPlanned;
        long durationDone;
        long dateMs;
        FocusMode mode;
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;

            long now = System.currentTimeMillis();
            long elapsed = now - baseTimeMs;

            if (mode == FocusMode.STOPWATCH) {
                displayMs = elapsed;
            } else if (mode == FocusMode.COUNTDOWN_FORWARD) {
                displayMs = elapsed;
                if (presetMs > 0 && elapsed >= presetMs) {
                    displayMs = presetMs;
                    timeText.setText(formatTime(displayMs));
                    stopRunning(true);
                    playBeep();
                    return;
                }
            } else if (mode == FocusMode.COUNTDOWN_BACKWARD) {
                long remain = presetMs - elapsed;
                if (remain <= 0) {
                    displayMs = 0;
                    timeText.setText(formatTime(displayMs));
                    stopRunning(true);
                    playBeep();
                    return;
                } else {
                    displayMs = remain;
                }
            }

            timeText.setText(formatTime(displayMs));
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stopwatch);

        timeText = findViewById(R.id.timeText);
        startBtn = findViewById(R.id.startBtn);
        stopBtn = findViewById(R.id.stopBtn);
        resetBtn = findViewById(R.id.resetBtn);

        focusModeBtn = findViewById(R.id.startBtn14);
        historyBtn = findViewById(R.id.startBtn9);

        tenMinBtn = findViewById(R.id.startBtn2);
        thirtyMinBtn = findViewById(R.id.startBtn4);
        oneHourBtn = findViewById(R.id.startBtn6);
        threeHourBtn = findViewById(R.id.startBtn5);
        customBtn = findViewById(R.id.startBtn3);

        modeLabel = findViewById(R.id.textView14);

        timeText.setText("00:00:00");
        modeLabel.setText("(not selected)");

        startBtn.setOnClickListener(v -> {
            if (!running) {
                startSession();
            }
        });

        stopBtn.setOnClickListener(v -> {
            if (running) {
                stopRunning(false);
            }
        });

        resetBtn.setOnClickListener(v -> {
            running = false;
            handler.removeCallbacks(runnable);
            baseTimeMs = 0;
            displayMs = 0;
            presetMs = 0;
            mode = FocusMode.STOPWATCH;
            modeLabel.setText("(not selected)");
            timeText.setText("00:00:00");
        });

        focusModeBtn.setOnClickListener(v -> showModeDialog());

        historyBtn.setOnClickListener(v -> showHistoryDialog());

        tenMinBtn.setOnClickListener(v -> applyPresetMinutes(getPresetFromButton(tenMinBtn, 10)));
        thirtyMinBtn.setOnClickListener(v -> applyPresetMinutes(getPresetFromButton(thirtyMinBtn, 30)));
        oneHourBtn.setOnClickListener(v -> applyPresetMinutes(getPresetFromButton(oneHourBtn, 60)));
        threeHourBtn.setOnClickListener(v -> applyPresetMinutes(getPresetFromButton(threeHourBtn, 180)));

        tenMinBtn.setOnLongClickListener(v -> {
            editPreset(tenMinBtn, 10);
            return true;
        });
        thirtyMinBtn.setOnLongClickListener(v -> {
            editPreset(thirtyMinBtn, 30);
            return true;
        });
        oneHourBtn.setOnLongClickListener(v -> {
            editPreset(oneHourBtn, 60);
            return true;
        });
        threeHourBtn.setOnLongClickListener(v -> {
            editPreset(threeHourBtn, 180);
            return true;
        });

        customBtn.setOnClickListener(v -> showCustomDialog());
    }

    private int getPresetFromButton(Button btn, int defaultMinutes) {
        String text = btn.getText().toString().trim();
        try {
            if (text.endsWith("min")) {
                String num = text.replace("min", "").trim();
                return Integer.parseInt(num);
            }
            if (text.endsWith("hr")) {
                String num = text.replace("hr", "").trim();
                return Integer.parseInt(num) * 60;
            }
            return Integer.parseInt(text);
        } catch (Exception e) {
            return defaultMinutes;
        }
    }

    private void startSession() {
        sessionStartMs = System.currentTimeMillis();
        baseTimeMs = System.currentTimeMillis();
        running = true;
        handler.post(runnable);
    }

    private void stopRunning(boolean autoFinished) {
        running = false;
        handler.removeCallbacks(runnable);

        long end = System.currentTimeMillis();
        long done = end - sessionStartMs;

        FocusSession session = new FocusSession();
        session.mode = mode;
        session.durationPlanned = presetMs;
        session.durationDone = done;
        session.dateMs = end;

        sessions.add(0, session);
    }

    private void showModeDialog() {
        String[] modes = {"Stopwatch", "Countdown Forward", "Countdown Backward"};
        new AlertDialog.Builder(this)
                .setTitle("Select Focus Mode")
                .setItems(modes, (d, i) -> {
                    if (i == 0) {
                        mode = FocusMode.STOPWATCH;
                        modeLabel.setText("stopwatch");
                    } else if (i == 1) {
                        mode = FocusMode.COUNTDOWN_FORWARD;
                        modeLabel.setText("countdown forward");
                    } else {
                        mode = FocusMode.COUNTDOWN_BACKWARD;
                        modeLabel.setText("countdown backward");
                    }
                })
                .show();
    }

    private void applyPresetMinutes(int minutes) {
        long ms = minutes * 60L * 1000L;
        presetMs = ms;

        if (mode == FocusMode.COUNTDOWN_FORWARD) {
            displayMs = 0;
        } else if (mode == FocusMode.COUNTDOWN_BACKWARD) {
            displayMs = presetMs;
        } else {
            displayMs = 0;
        }
        timeText.setText(formatTime(displayMs));
    }

    private void editPreset(Button btn, int defaultMinutes) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(defaultMinutes + " minutes");
        new AlertDialog.Builder(this)
                .setTitle("Edit Preset (minutes)")
                .setView(input)
                .setPositiveButton("Set", (d, w) -> {
                    String s = input.getText().toString().trim();
                    if (!s.isEmpty()) {
                        int m;
                        try {
                            m = Integer.parseInt(s);
                        } catch (Exception e) {
                            m = defaultMinutes;
                        }
                        if (m >= 60 && m % 60 == 0) {
                            int hours = m / 60;
                            btn.setText(hours + "hr");
                        } else {
                            btn.setText(m + "min");
                        }
                        applyPresetMinutes(m);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showCustomDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Minutes");
        new AlertDialog.Builder(this)
                .setTitle("Custom Minutes")
                .setView(input)
                .setPositiveButton("Set & Start", (d, w) -> {
                    String s = input.getText().toString().trim();
                    if (!s.isEmpty()) {
                        int m;
                        try {
                            m = Integer.parseInt(s);
                        } catch (Exception e) {
                            m = 0;
                        }
                        presetMs = m * 60L * 1000L;
                        if (mode == FocusMode.COUNTDOWN_BACKWARD) {
                            displayMs = presetMs;
                        } else {
                            displayMs = 0;
                        }
                        timeText.setText(formatTime(displayMs));
                        startSession();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showHistoryDialog() {
        if (sessions.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Focus Sessions History")
                    .setMessage("No sessions yet")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        int i = sessions.size();
        for (FocusSession s : sessions) {
            String modeStr;
            if (s.mode == FocusMode.COUNTDOWN_FORWARD) {
                modeStr = "countdown forward";
            } else if (s.mode == FocusMode.COUNTDOWN_BACKWARD) {
                modeStr = "countdown backward";
            } else {
                modeStr = "stopwatch";
            }

            String dateStr = DateFormat.getDateInstance().format(new Date(s.dateMs));
            String timeStr = DateFormat.getTimeInstance().format(new Date(s.dateMs));

            sb.append("Focus session ").append(i).append("\n");

            long durationToShow = (s.mode == FocusMode.STOPWATCH)
                    ? s.durationDone
                    : (s.durationPlanned > 0 ? s.durationPlanned : s.durationDone);

            sb.append("Duration : ").append(formatTime(durationToShow)).append("\n");
            sb.append("Date : ").append(dateStr).append("\n");
            sb.append("Time : ").append(timeStr).append("\n");

            String status;
            if (s.mode == FocusMode.STOPWATCH) {
                status = "completed(" + formatTime(s.durationDone) + ")";
            } else if (s.durationPlanned > 0 && s.durationDone >= s.durationPlanned) {
                status = "completed(" + formatTime(s.durationPlanned) + ")";
            } else if (s.durationPlanned > 0) {
                long remaining = s.durationPlanned - s.durationDone;
                if (remaining < 0) remaining = 0;
                status = "not completed(" + formatTime(remaining) + " remained)";
            } else {
                status = "completed(" + formatTime(s.durationDone) + ")";
            }

            sb.append("Status : ").append(status).append("\n");

            sb.append("Focus mode : ").append(modeStr).append("\n\n");
            i--;
        }

        new AlertDialog.Builder(this)
                .setTitle("Focus Sessions History")
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void playBeep() {
        try {
            MediaPlayer mp = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI);
            if (mp != null) {
                mp.start();
            }
        } catch (Exception ignored) {
        }
    }

    private String formatTime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        seconds = seconds % 60;
        minutes = minutes % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
