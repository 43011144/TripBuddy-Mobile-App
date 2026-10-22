package com.example.tripbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.tripbuddy.core.AppException;
import com.example.tripbuddy.core.Callback;
import com.example.tripbuddy.core.Money;
import com.example.tripbuddy.model.UserStats;


public class home extends BaseActivity {

    Button button;
    TextView statsView;

    @Override
    protected boolean requiresAuth() { return false; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        button= findViewById(R.id.button);
        statsView = findViewById(R.id.tvHomeStats);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDoubleClick()) return;
                Intent intent= new Intent(home.this, registration.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!session.isValid()) {
            statsView.setText(R.string.tb_sign_in_to_see_stats);
            return;
        }
        ServiceLocator.trips(this).loadStats(session.userId(), safe(new Callback<UserStats>() {
            @Override public void onSuccess(UserStats s) {
                statsView.setText(getString(R.string.tb_home_stats, session.username(),
                        s.confirmedTrips, Money.format(s.confirmedTotalCents), s.memories));
            }
            @Override public void onError(AppException e) { statsView.setText(e.getMessage()); }
        }));
    }
}