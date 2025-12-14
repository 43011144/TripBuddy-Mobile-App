package com.example.tripbuddy;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class budget extends AppCompatActivity {

    EditText etDestination, etNotes, etCustomExpense, etMealsExpense;
    Spinner spinnerActivities;
    TextView tvBudgetSummary;
    Button btnSave, btnConfirm;

    double subtotal = 0;
    double discount = 0;
    double finalBudget = 0;

    Map<String, Double> activityCosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        // Init views
        etDestination = findViewById(R.id.etDestination);
        etNotes = findViewById(R.id.etNotes);
        etCustomExpense = findViewById(R.id.etCustomExpense);
        etMealsExpense = findViewById(R.id.etMealExpense);
        spinnerActivities = findViewById(R.id.spinnerActivities);
        tvBudgetSummary = findViewById(R.id.tvBudgetSummary);
        btnSave = findViewById(R.id.btnSave);
        btnConfirm = findViewById(R.id.btnConfirm);

        // Predefined activity costs
        activityCosts = new HashMap<>();
        activityCosts.put("Sightseeing", 500.0);
        activityCosts.put("Hiking", 700.0);
        activityCosts.put("Beach Day", 1000.0);
        activityCosts.put("Museum Visit", 400.0);

        // Spinner setup
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>(activityCosts.keySet()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivities.setAdapter(adapter);

        // Trigger budget calculation when spinner changes
        spinnerActivities.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculateBudget();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Watcher for live updates
        etCustomExpense.addTextChangedListener(new SimpleWatcher());
        etMealsExpense.addTextChangedListener(new SimpleWatcher());

        btnSave.setOnClickListener(v -> calculateBudget());

        btnConfirm.setOnClickListener(v -> {
            saveTripHistory();
            Toast.makeText(this, "Trip Confirmed!", Toast.LENGTH_SHORT).show();
        });
    }

    private void calculateBudget() {
        subtotal = 0;

        // Add activity cost
        String selectedActivity = spinnerActivities.getSelectedItem().toString();
        subtotal += activityCosts.get(selectedActivity);

        // Add custom expenses
        subtotal += getNumber(etCustomExpense);
        subtotal += getNumber(etMealsExpense);

        // Apply loyalty discount if 3+ trips saved
        SharedPreferences prefs = getSharedPreferences("TripHistory", MODE_PRIVATE);
        int tripCount = prefs.getInt("tripCount", 0);
        discount = (tripCount >= 3) ? subtotal * 0.1 : 0;

        finalBudget = subtotal - discount;

        // Show budget summary
        tvBudgetSummary.setText("    Subtotal: " + subtotal +
                " | Discount: " + discount +
                " | Final Budget: " + finalBudget);
    }

    private void saveTripHistory() {
        SharedPreferences prefs = getSharedPreferences("TripHistory", MODE_PRIVATE);
        int tripCount = prefs.getInt("tripCount", 0);

        prefs.edit().putInt("tripCount", tripCount + 1).apply();
    }

    private double getNumber(EditText et) {
        String text = et.getText().toString().trim();
        if (text.isEmpty()) return 0;
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Simple watcher for live updates
    private class SimpleWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { calculateBudget(); }
        @Override public void afterTextChanged(Editable s) {}
    }
}