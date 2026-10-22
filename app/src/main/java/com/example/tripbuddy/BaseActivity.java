package com.example.tripbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tripbuddy.core.AppException;
import com.example.tripbuddy.core.Callback;
import com.example.tripbuddy.security.SessionManager;

/**
 * Shared behaviour for every screen:
 *  - sign-in guard (screens that need an account redirect to LoginActivity)
 *  - safe callbacks (results are dropped if the screen was closed meanwhile, so no crashes or leaks)
 *  - click debouncing (stops double taps from starting two saves or two screens)
 *  - system bar padding so content is never hidden behind the status or navigation bar
 */
public abstract class BaseActivity extends AppCompatActivity {

    public static final String EXTRA_NEXT = "next_screen";
    private static final long DEBOUNCE_MS = 600;

    protected SessionManager session;
    private long lastClickAt;

    /** Override and return false for screens that work without an account. */
    protected boolean requiresAuth() { return true; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (requiresAuth() && !session.isValid()) {
            Intent login = new Intent(this, LoginActivity.class);
            login.putExtra(EXTRA_NEXT, getClass().getName());
            startActivity(login);
            finish();
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        View content = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    protected boolean isAlive() { return !isFinishing() && !isDestroyed(); }

    /** Wraps a callback so it only runs while this screen is still showing. */
    protected <T> Callback<T> safe(final Callback<T> inner) {
        return new Callback<T>() {
            @Override public void onSuccess(T value) { if (isAlive()) inner.onSuccess(value); }
            @Override public void onError(AppException error) { if (isAlive()) inner.onError(error); }
        };
    }

    /** True when this click should be ignored because the previous one was moments ago. */
    protected boolean isDoubleClick() {
        long now = SystemClock.elapsedRealtime();
        if (now - lastClickAt < DEBOUNCE_MS) return true;
        lastClickAt = now;
        return false;
    }

    protected void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void setEnabled(boolean enabled, View... views) {
        for (View v : views) {
            if (v != null) v.setEnabled(enabled);
        }
    }

    protected void open(Class<?> target) {
        startActivity(new Intent(this, target));
    }
}
