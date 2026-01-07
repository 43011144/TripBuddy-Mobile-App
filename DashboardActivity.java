package com.example.tripbuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {
    EditText memoryText;
    Button saveBtn, trackTripsBtn, settingsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        memoryText = findViewById(R.id.memoryTextt);
        saveBtn = findViewById(R.id.saveBtnn);
        trackTripsBtn = findViewById(R.id.trackTripsBtnn);
        settingsBtn = findViewById(R.id.settingsBtnn);

        saveBtn.setOnClickListener(v -> {
            com.example.tripbuddy.MemoryDBHelper db = new com.example.tripbuddy.MemoryDBHelper(this);
            db.addMemory(memoryText.getText().toString());
            startActivity(new Intent(this, CommentsActivity.class));
        });

        trackTripsBtn.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("TripBuddyPrefs", MODE_PRIVATE);
            int trips = prefs.getInt("tripCount", 0) + 1;
            prefs.edit().putInt("tripCount", trips).apply();
        });

        settingsBtn.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }
}