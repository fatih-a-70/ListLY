package com.example.myappp;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StopwatchActivity extends AppCompatActivity {

    private TextView timeText;
    private Button startBtn, stopBtn, resetBtn;

    private Handler handler = new Handler();
    private long startTime = 0;
    private boolean running = false;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (running) {
                long millis = System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                timeText.setText(
                        String.format("%02d:%02d", minutes, seconds)
                );
                handler.postDelayed(this, 1000);
            }
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

        startBtn.setOnClickListener(v -> {
            if (!running) {
                startTime = System.currentTimeMillis();
                running = true;
                handler.post(runnable);
            }
        });

        stopBtn.setOnClickListener(v -> running = false);

        resetBtn.setOnClickListener(v -> {
            running = false;
            timeText.setText("00:00");
        });
    }
}
