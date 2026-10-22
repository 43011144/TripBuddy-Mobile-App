package com.example.tripbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.tripbuddy.core.AppException;
import com.example.tripbuddy.core.Callback;
import com.example.tripbuddy.core.Money;
import com.example.tripbuddy.model.Memory;
import com.example.tripbuddy.model.UserStats;

public class DashboardActivity extends BaseActivity {
    EditText memoryText;
    Button saveBtn, trackTripsBtn, settingsBtn;
    TextView summaryView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        if (isFinishing()) return;

        summaryView = findViewById(R.id.tvDashboardSummary);
        memoryText = findViewById(R.id.memoryTextt);
        saveBtn = findViewById(R.id.saveBtnn);
        trackTripsBtn = findViewById(R.id.trackTripsBtnn);
        settingsBtn = findViewById(R.id.settingsBtnn);

        saveBtn.setOnClickListener(v -> {
            if (isDoubleClick()) return;
            setEnabled(false, saveBtn);   // no second save while the first is running
            com.example.tripbuddy.MemoryDBHelper db = new com.example.tripbuddy.MemoryDBHelper(this);
            db.addMemory(memoryText.getText().toString(), safe(new Callback<Memory>() {
                @Override public void onSuccess(Memory m) {
                    setEnabled(true, saveBtn);
                    memoryText.setText("");
                    startActivity(new Intent(DashboardActivity.this, CommentsActivity.class));
                }
                @Override public void onError(AppException e) {
                    setEnabled(true, saveBtn);
                    memoryText.setError(e.getMessage());
                }
            }));
        });

        // Tracking trips now shows the real trip history stored in the database.
        trackTripsBtn.setOnClickListener(v -> {
            if (!isDoubleClick()) open(TripHistoryActivity.class);
        });

        settingsBtn.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (session == null || !session.isValid()) return;
        ServiceLocator.trips(this).loadStats(session.userId(), safe(new Callback<UserStats>() {
            @Override public void onSuccess(UserStats s) {
                summaryView.setText(getString(R.string.tb_dashboard_summary,
                        s.confirmedTrips, s.draftTrips, Money.format(s.confirmedTotalCents), s.memories));
            }
            @Override public void onError(AppException e) { summaryView.setText(e.getMessage()); }
        }));
    }
}