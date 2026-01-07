package com.example.tripbuddy;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    Button lightBtn, darkBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        lightBtn = findViewById(R.id.lightBtn);
        darkBtn = findViewById(R.id.darkBtn);

        SharedPreferences prefs = getSharedPreferences("TripBuddyPrefs", MODE_PRIVATE);

        lightBtn.setOnClickListener(v -> {
            prefs.edit().putString("theme", "light").apply();
            recreate();
        });

        darkBtn.setOnClickListener(v -> {
            prefs.edit().putString("theme", "dark").apply();
            recreate();
        });
    }
}