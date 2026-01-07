package com.example.tripbuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText usernameInput;
    Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.usernameInput);
        loginBtn = findViewById(R.id.loginBtn);

        SharedPreferences prefs = getSharedPreferences("TripBuddyPrefs", MODE_PRIVATE);
        if (prefs.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(this, SaveMemoryActivity.class));
            finish();
        }

        loginBtn.setOnClickListener(v -> {
            String username = usernameInput.getText().toString();
            prefs.edit().putString("username", username)
                    .putBoolean("isLoggedIn", true)
                    .apply();
            startActivity(new Intent(this, SaveMemoryActivity.class));
            finish();
        });
    }
}