package com.example.tripbuddy.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/** SQL for the users table. All queries are parameterised. Callers pass the database so they control transactions. */
public final class UserDao {

    /** Internal record that includes the password hash. Never exposed to the UI. */
    public static final class Credentials {
        public final long id;
        public final String username;
        public final String passwordHash;
        public final long lockedUntil;

        Credentials(long id, String username, String passwordHash, long lockedUntil) {
            this.id = id;
            this.username = username;
            this.passwordHash = passwordHash;
            this.lockedUntil = lockedUntil;
        }
    }

    /** Throws SQLiteConstraintException when the username already exists. */
    public long insert(SQLiteDatabase db, String username, String passwordHash, long now) {
        ContentValues v = new ContentValues();
        v.put("username", username);
        v.put("password_hash", passwordHash);
        v.put("created_at", now);
        return db.insertOrThrow("users", null, v);
    }

    public Credentials findByUsername(SQLiteDatabase db, String username) {
        try (Cursor c = db.query("users",
                new String[]{"id", "username", "password_hash", "locked_until"},
                "username = ?", new String[]{username}, null, null, null, "1")) {
            if (!c.moveToFirst()) return null;
            return new Credentials(c.getLong(0), c.getString(1), c.getString(2), c.getLong(3));
        }
    }

    public void resetFailures(SQLiteDatabase db, long id) {
        db.execSQL("UPDATE users SET failed_attempts = 0, locked_until = 0 WHERE id = ?", new Object[]{id});
    }

    /**
     * Atomic in one statement, so two simultaneous wrong guesses cannot both read the same
     * counter. When the limit is reached the account locks and the counter restarts.
     */
    public void recordFailure(SQLiteDatabase db, long id, int maxAttempts, long lockMs, long now) {
        db.execSQL("UPDATE users SET "
                        + "failed_attempts = CASE WHEN failed_attempts + 1 >= ? THEN 0 ELSE failed_attempts + 1 END, "
                        + "locked_until = CASE WHEN failed_attempts + 1 >= ? THEN ? ELSE locked_until END "
                        + "WHERE id = ?",
                new Object[]{maxAttempts, maxAttempts, now + lockMs, id});
    }

    public void updateHash(SQLiteDatabase db, long id, String passwordHash) {
        db.execSQL("UPDATE users SET password_hash = ? WHERE id = ?", new Object[]{passwordHash, id});
    }

    /** Foreign keys cascade, so this also removes the user's trips and memories. */
    public int delete(SQLiteDatabase db, long id) {
        return db.delete("users", "id = ?", new String[]{String.valueOf(id)});
    }
}
