package com.example.tripbuddy.security;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Remembers who is signed in. Nothing secret is stored here (no password, no hash):
 * only the user id, the username and an expiry. Sessions expire after SESSION_TTL_MS
 * and the file is excluded from backups in the manifest.
 */
public final class SessionManager {

    private static final String FILE = "TripBuddySession";
    private static final String KEY_ID = "user_id";
    private static final String KEY_NAME = "username";
    private static final String KEY_EXPIRES = "expires_at";
    private static final long SESSION_TTL_MS = 7L * 24 * 60 * 60 * 1000;

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public void start(long userId, String username) {
        prefs.edit()
                .putLong(KEY_ID, userId)
                .putString(KEY_NAME, username)
                .putLong(KEY_EXPIRES, System.currentTimeMillis() + SESSION_TTL_MS)
                .apply();
    }

    public boolean isValid() {
        long id = prefs.getLong(KEY_ID, 0);
        long expires = prefs.getLong(KEY_EXPIRES, 0);
        if (id <= 0 || expires <= System.currentTimeMillis()) {
            if (id != 0) end();
            return false;
        }
        return true;
    }

    public long userId() { return isValid() ? prefs.getLong(KEY_ID, 0) : 0; }

    public String username() { return isValid() ? prefs.getString(KEY_NAME, "") : ""; }

    public void end() { prefs.edit().clear().apply(); }
}
