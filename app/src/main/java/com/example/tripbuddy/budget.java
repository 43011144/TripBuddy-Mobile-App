package com.example.tripbuddy;

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

import com.example.tripbuddy.core.AppException;
import com.example.tripbuddy.core.BudgetCalculator;
import com.example.tripbuddy.core.Callback;
import com.example.tripbuddy.core.Money;
import com.example.tripbuddy.model.Trip;
import com.example.tripbuddy.model.TripInput;
import com.example.tripbuddy.repo.TripRepository;

import java.util.ArrayList;

public class budget extends BaseActivity {

    EditText etDestination, etNotes, etCustomExpense, etMealsExpense;
    Spinner spinnerActivities;
    TextView tvBudgetSummary;
    Button btnSave, btnConfirm;

    // All money is kept in whole cents (the old double values could drift, e.g. 0.1 + 0.2).
    long subtotal = 0;
    long discount = 0;
    long finalBudget = 0;

    private TripRepository repo;
    private int confirmedTrips = 0;   // drives the loyalty discount, read from the database
    private Trip currentTrip;         // the draft saved by "Save Trip", so a second tap updates it instead of duplicating it
    private boolean busy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);
        if (isFinishing()) return;

        repo = ServiceLocator.trips(this);

        // Init views
        etDestination = findViewById(R.id.etDestination);
        etNotes = findViewById(R.id.etNotes);
        etCustomExpense = findViewById(R.id.etCustomExpense);
        etMealsExpense = findViewById(R.id.etMealExpense);
        spinnerActivities = findViewById(R.id.spinnerActivities);
        tvBudgetSummary = findViewById(R.id.tvBudgetSummary);
        btnSave = findViewById(R.id.btnSave);
        btnConfirm = findViewById(R.id.btnConfirm);

        // Predefined activity costs live in BudgetCalculator (stable order, prices in cents)
        // Spinner setup
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>(BudgetCalculator.ACTIVITIES.keySet()));
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

        // Save Trip now stores a draft in the database; Confirm Trip saves and confirms in one transaction.
        btnSave.setOnClickListener(v -> save(false));
        btnConfirm.setOnClickListener(v -> save(true));

        loadLoyaltyCount();
    }

    private void loadLoyaltyCount() {
        repo.confirmedCount(session.userId(), safe(new Callback<Integer>() {
            @Override public void onSuccess(Integer count) {
                confirmedTrips = count;
                calculateBudget();
            }
            @Override public void onError(AppException e) { toast(e.getMessage()); }
        }));
    }

    private void save(boolean confirm) {
        if (busy) return;
        TripInput input;
        try {
            input = new TripInput(
                    etDestination.getText().toString(),
                    etNotes.getText().toString(),
                    String.valueOf(spinnerActivities.getSelectedItem()),
                    getCents(etCustomExpense),
                    getCents(etMealsExpense));
        } catch (IllegalArgumentException e) {
            toast(e.getMessage());
            return;
        }
        setBusy(true);
        Callback<Trip> callback = safe(new Callback<Trip>() {
            @Override public void onSuccess(Trip trip) {
                currentTrip = trip;
                setBusy(false);
                if (confirm) {
                    toast(getString(R.string.tb_trip_confirmed));
                    open(TripHistoryActivity.class);
                    finish();
                } else {
                    toast(getString(R.string.tb_trip_saved));
                }
            }
            @Override public void onError(AppException e) {
                setBusy(false);
                toast(e.getMessage());
            }
        });
        if (confirm) repo.confirm(session.userId(), currentTrip, input, callback);
        else repo.saveDraft(session.userId(), currentTrip, input, callback);
    }

    private void setBusy(boolean value) {
        busy = value;
        setEnabled(!value, btnSave, btnConfirm);
    }

    private void calculateBudget() {
        long custom, meals;
        try {
            custom = getCents(etCustomExpense);
            meals = getCents(etMealsExpense);
        } catch (IllegalArgumentException e) {
            return;   // the field already shows its error; keep the last good summary
        }

        // Activity cost comes from the catalogue, never from the screen.
        Object selected = spinnerActivities.getSelectedItem();
        Long activityCost = selected == null ? null : BudgetCalculator.priceOf(selected.toString());
        if (activityCost == null) return;

        // Apply loyalty discount if 3+ trips are confirmed (counted in the database)
        BudgetCalculator.Quote quote = BudgetCalculator.quote(activityCost, custom, meals, confirmedTrips);
        subtotal = quote.subtotalCents;
        discount = quote.discountCents;
        finalBudget = quote.totalCents;

        // Show budget summary
        tvBudgetSummary.setText(getString(R.string.tb_budget_summary,
                Money.format(subtotal), Money.format(discount), Money.format(finalBudget)));
    }

    /** Parses an amount field. Shows the problem on the field itself and rethrows so callers can stop. */
    private long getCents(EditText et) {
        try {
            long cents = Money.parseToCents(et.getText().toString());
            et.setError(null);
            return cents;
        } catch (IllegalArgumentException e) {
            et.setError(e.getMessage());
            throw e;
        }
    }

    // Simple watcher for live updates
    private class SimpleWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { calculateBudget(); }
        @Override public void afterTextChanged(Editable s) {}
    }
}