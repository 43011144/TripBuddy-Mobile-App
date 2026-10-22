package com.example.tripbuddy.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Owns the SQLite database: schema, versioning and connection settings.
 * One shared instance per process (SQLiteOpenHelper is thread-safe and pools connections).
 *
 * Adding a feature later = bump DB_VERSION and add a block to onUpgrade. Never edit an
 * old block, so every installed version migrates the same way.
 */
public final class TripBuddyDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "tripbuddy.db";
    public static final int DB_VERSION = 1;

    private static volatile TripBuddyDbHelper instance;

    public static TripBuddyDbHelper get(Context context) {
        if (instance == null) {
            synchronized (TripBuddyDbHelper.class) {
                if (instance == null) instance = new TripBuddyDbHelper(context.getApplicationContext());
            }
        }
        return instance;
    }

    private TripBuddyDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);  // cascades and referential integrity
        db.enableWriteAheadLogging();              // readers no longer block the writer
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT NOT NULL COLLATE NOCASE UNIQUE,"
                + "password_hash TEXT NOT NULL,"
                + "failed_attempts INTEGER NOT NULL DEFAULT 0,"
                + "locked_until INTEGER NOT NULL DEFAULT 0,"
                + "created_at INTEGER NOT NULL)");

        db.execSQL("CREATE TABLE trips ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "uuid TEXT NOT NULL UNIQUE,"
                + "user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,"
                + "destination TEXT NOT NULL,"
                + "notes TEXT NOT NULL DEFAULT '',"
                + "activity TEXT NOT NULL,"
                + "activity_cost_cents INTEGER NOT NULL CHECK (activity_cost_cents >= 0),"
                + "custom_expense_cents INTEGER NOT NULL DEFAULT 0 CHECK (custom_expense_cents >= 0),"
                + "meals_expense_cents INTEGER NOT NULL DEFAULT 0 CHECK (meals_expense_cents >= 0),"
                + "discount_cents INTEGER NOT NULL DEFAULT 0 CHECK (discount_cents >= 0),"
                + "total_cents INTEGER NOT NULL CHECK (total_cents >= 0),"
                + "status TEXT NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','CONFIRMED')),"
                + "version INTEGER NOT NULL DEFAULT 1,"
                + "created_at INTEGER NOT NULL,"
                + "updated_at INTEGER NOT NULL)");
        // Serves the history list (newest first, per user) and the loyalty count.
        db.execSQL("CREATE INDEX idx_trips_user_id ON trips(user_id, id DESC)");
        db.execSQL("CREATE INDEX idx_trips_user_status ON trips(user_id, status)");

        db.execSQL("CREATE TABLE memories ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "uuid TEXT NOT NULL UNIQUE,"
                + "user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,"
                + "trip_id INTEGER REFERENCES trips(id) ON DELETE SET NULL,"
                + "content TEXT NOT NULL CHECK (length(content) BETWEEN 1 AND 2000),"
                + "created_at INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX idx_memories_user_id ON memories(user_id, id DESC)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Example for the future:
        // if (oldVersion < 2) { db.execSQL("ALTER TABLE trips ADD COLUMN start_date INTEGER"); }
    }
}
