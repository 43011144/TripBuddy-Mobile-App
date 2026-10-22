package com.example.tripbuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.tripbuddy.core.AppException;
import com.example.tripbuddy.core.Callback;

public class SettingsActivity extends BaseActivity {
    Button lightBtn, darkBtn, systemBtn, logoutBtn, deleteBtn;
    TextView accountView, versionView;

    @Override
    protected boolean requiresAuth() { return false; }  // theme works for guests too

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        lightBtn = findViewById(R.id.lightBtn);
        darkBtn = findViewById(R.id.darkBtn);
        systemBtn = findViewById(R.id.systemBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        accountView = findViewById(R.id.tvAccount);
        versionView = findViewById(R.id.tvVersion);

        SharedPreferences prefs = getSharedPreferences(TripBuddyApp.PREFS, MODE_PRIVATE);

        // The theme used to be saved but never applied. Now it is applied immediately and on every app start.
        lightBtn.setOnClickListener(v -> setTheme(prefs, TripBuddyApp.THEME_LIGHT));
        darkBtn.setOnClickListener(v -> setTheme(prefs, TripBuddyApp.THEME_DARK));
        systemBtn.setOnClickListener(v -> setTheme(prefs, TripBuddyApp.THEME_SYSTEM));

        boolean signedIn = session.isValid();
        accountView.setText(signedIn
                ? getString(R.string.tb_signed_in_as, session.username())
                : getString(R.string.tb_not_signed_in));
        logoutBtn.setVisibility(signedIn ? View.VISIBLE : View.GONE);
        deleteBtn.setVisibility(signedIn ? View.VISIBLE : View.GONE);

        logoutBtn.setOnClickListener(v -> signOut());
        deleteBtn.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(R.string.tb_delete_account_title)
                .setMessage(R.string.tb_delete_account_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.tb_delete, (d, w) -> deleteAccount())
                .show());

        versionView.setText(getString(R.string.tb_version, appVersion()));
    }

    private void setTheme(SharedPreferences prefs, String theme) {
        prefs.edit().putString(TripBuddyApp.KEY_THEME, theme).apply();
        TripBuddyApp.applyTheme(this);   // recreates visible screens when the mode actually changes
    }

    private void signOut() {
        session.end();
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    private void deleteAccount() {
        setEnabled(false, deleteBtn, logoutBtn);
        ServiceLocator.auth(this).deleteAccount(session.userId(), safe(new Callback<Boolean>() {
            @Override public void onSuccess(Boolean removed) { signOut(); }
            @Override public void onError(AppException e) {
                setEnabled(true, deleteBtn, logoutBtn);
                toast(e.getMessage());
            }
        }));
    }

    private String appVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0";
        }
    }
}