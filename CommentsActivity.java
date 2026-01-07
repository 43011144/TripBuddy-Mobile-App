package com.example.tripbuddy;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class CommentsActivity extends AppCompatActivity {
    TextView commentsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        commentsView = findViewById(R.id.commentsView);

        com.example.tripbuddy.MemoryDBHelper db = new com.example.tripbuddy.MemoryDBHelper(this);
        commentsView.setText(db.getLastMemory());
    }
}