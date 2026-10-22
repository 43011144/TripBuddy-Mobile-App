package com.example.tripbuddy;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.widget.TextView;

import com.example.tripbuddy.core.AppException;
import com.example.tripbuddy.core.Callback;
import com.example.tripbuddy.model.Memory;

public class CommentsActivity extends BaseActivity {
    TextView commentsView, commentsDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        if (isFinishing()) return;

        commentsView = findViewById(R.id.commentsView);
        commentsDate = findViewById(R.id.commentsDate);

        // Loaded off the main thread; the result is ignored if the screen was closed meanwhile.
        com.example.tripbuddy.MemoryDBHelper db = new com.example.tripbuddy.MemoryDBHelper(this);
        db.getLastMemory(safe(new Callback<Memory>() {
            @Override public void onSuccess(Memory m) {
                if (m == null) {
                    commentsView.setText(R.string.tb_no_memories_yet);
                    return;
                }
                commentsView.setText(m.content);
                commentsDate.setText(DateUtils.formatDateTime(CommentsActivity.this, m.createdAt,
                        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR));
            }
            @Override public void onError(AppException e) { commentsView.setText(e.getMessage()); }
        }));
    }
}