package com.example.tripbuddy;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.StrictMode;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.tripbuddy.core.AppExecutors;
import com.example.tripbuddy.data.TripBuddyDbHelper;

/** Runs once per process: applies the saved theme, warms up the database, enables debug checks. */
public class TripBuddyApp extends Application {

    public static final String PREFS = "TripBuddyPrefs";
    public static final String KEY_THEME = "theme";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_SYSTEM = "system";

    @Override
    public void onCreate() {
        super.onCreate();
        applyTheme(this);

        if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            // Debug builds only: log any database or disk work that sneaks onto the main thread.
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedClosableObjects().detectLeakedSqlLiteObjects().penaltyLog().build());
        }

        // Open (and if needed create or migrate) the database off the main thread so the first screen is fast.
        AppExecutors.get().read(() -> TripBuddyDbHelper.get(this).getReadableDatabase());
    }

    public static void applyTheme(Context context) {
        String theme = context.getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_THEME, THEME_SYSTEM);
        if (THEME_DARK.equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (THEME_LIGHT.equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }
}
