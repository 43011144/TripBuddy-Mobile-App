package com.example.tripbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.tripbuddy.core.AppException;
import com.example.tripbuddy.core.Callback;
import com.example.tripbuddy.model.Memory;
import com.example.tripbuddy.security.InputValidator;

public class SaveMemoryActivity extends BaseActivity {
    EditText memoryInput;
    Button saveMemoryBtn;
    TextView memoryCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_memory);
        if (isFinishing()) return;

        memoryInput = findViewById(R.id.memoryInput);
        saveMemoryBtn = findViewById(R.id.saveMemoryBtn);
        memoryCounter = findViewById(R.id.memoryCounter);

        memoryCounter.setText(getString(R.string.tb_counter, 0, InputValidator.MEMORY_MAX));
        memoryInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                memoryCounter.setText(getString(R.string.tb_counter, s.length(), InputValidator.MEMORY_MAX));
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        saveMemoryBtn.setOnClickListener(v -> {
            if (isDoubleClick()) return;
            String memoryText = memoryInput.getText().toString();
            setEnabled(false, saveMemoryBtn);
            com.example.tripbuddy.MemoryDBHelper db = new com.example.tripbuddy.MemoryDBHelper(this);
            db.addMemory(memoryText, safe(new Callback<Memory>() {
                @Override public void onSuccess(Memory m) {
                    startActivity(new Intent(SaveMemoryActivity.this, DashboardActivity.class));
                    finish();
                }
                @Override public void onError(AppException e) {
                    setEnabled(true, saveMemoryBtn);
                    memoryInput.setError(e.getMessage());
                }
            }));
        });
    }
}