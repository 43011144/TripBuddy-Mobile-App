package com.example.tripbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class SaveMemoryActivity extends AppCompatActivity {
    EditText memoryInput;
    Button saveMemoryBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_memory);

        memoryInput = findViewById(R.id.memoryInput);
        saveMemoryBtn = findViewById(R.id.saveMemoryBtn);

        saveMemoryBtn.setOnClickListener(v -> {
            String memoryText = memoryInput.getText().toString();
            com.example.tripbuddy.MemoryDBHelper db = new com.example.tripbuddy.MemoryDBHelper(this);
            db.addMemory(memoryText);
            startActivity(new Intent(this, DashboardActivity.class));
        });
    }
}