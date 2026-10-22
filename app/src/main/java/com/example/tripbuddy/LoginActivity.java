package com.example.tripbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.tripbuddy.core.AppException;
import com.example.tripbuddy.core.Callback;
import com.example.tripbuddy.model.User;

public class LoginActivity extends BaseActivity {
    EditText usernameInput, passwordInput;
    Button loginBtn, registerBtn;
    TextView errorText;
    ProgressBar progress;

    @Override
    protected boolean requiresAuth() { return false; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);
        errorText = findViewById(R.id.errorText);
        progress = findViewById(R.id.loginProgress);

        // Already signed in (and the session has not expired): skip straight on.
        if (session.isValid()) {
            goNext();
            return;
        }

        loginBtn.setOnClickListener(v -> submit(false));
        registerBtn.setOnClickListener(v -> submit(true));
    }

    private void submit(boolean createAccount) {
        if (isDoubleClick()) return;

        // The password is read into a char[] and wiped by the repository after hashing,
        // so it never sits around as an immutable String.
        Editable typed = passwordInput.getText();
        char[] password = new char[typed.length()];
        typed.getChars(0, typed.length(), password, 0);
        typed.clear();

        String username = usernameInput.getText().toString();
        errorText.setText("");
        setBusy(true);

        Callback<User> callback = safe(new Callback<User>() {
            @Override public void onSuccess(User user) {
                session.start(user.id, user.username);
                goNext();
            }
            @Override public void onError(AppException e) {
                setBusy(false);
                errorText.setText(e.getMessage());
            }
        });

        if (createAccount) {
            ServiceLocator.auth(this).register(username, password, callback);
        } else {
            ServiceLocator.auth(this).login(username, password, callback);
        }
    }

    private void setBusy(boolean busy) {
        progress.setVisibility(busy ? View.VISIBLE : View.GONE);
        setEnabled(!busy, loginBtn, registerBtn, usernameInput, passwordInput);
    }

    /** Continues to the screen that sent the user here, otherwise to the original landing screen. */
    private void goNext() {
        startActivity(new Intent(this, resolveNext(getIntent().getStringExtra(EXTRA_NEXT))));
        finish();
    }

    /** Only our own BaseActivity screens are allowed as a destination (no arbitrary class names from extras). */
    private Class<?> resolveNext(String className) {
        String own = LoginActivity.class.getName();
        String pkg = own.substring(0, own.lastIndexOf('.') + 1);
        if (className != null && className.startsWith(pkg)) {
            try {
                Class<?> c = Class.forName(className);
                if (BaseActivity.class.isAssignableFrom(c) && c != LoginActivity.class) return c;
            } catch (ClassNotFoundException ignored) {
                // fall through to the default
            }
        }
        return SaveMemoryActivity.class;
    }
}