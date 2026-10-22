package com.example.tripbuddy.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.example.tripbuddy.model.Trip;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL for the trips table. Every query is scoped by user_id, so one user can never read
 * or change another user's rows even if an id is guessed.
 */
public final class TripDao {

    private static final String[] COLUMNS = {
            "id", "uuid", "user_id", "destination", "notes", "activity",
            "activity_cost_cents", "custom_expense_cents", "meals_expense_cents",
            "discount_cents", "total_cents", "status", "version", "created_at", "updated_at"
    };

    public long insert(SQLiteDatabase db, long userId, String uuid, String destination, String notes,
                       String activity, long activityCents, long customCents, long mealsCents,
                       long discountCents, long totalCents, long now) {
        ContentValues v = new ContentValues();
        v.put("uuid", uuid);
        v.put("user_id", userId);
        v.put("destination", destination);
        v.put("notes", notes);
        v.put("activity", activity);
        v.put("activity_cost_cents", activityCents);
        v.put("custom_expense_cents", customCents);
        v.put("meals_expense_cents", mealsCents);
        v.put("discount_cents", discountCents);
        v.put("total_cents", totalCents);
        v.put("status", Trip.DRAFT);
        v.put("version", 1);
        v.put("created_at", now);
        v.put("updated_at", now);
        return db.insertOrThrow("trips", null, v);
    }

    /** Optimistic locking: only succeeds if the row still has the version the caller last saw. Returns rows changed (0 = conflict). */
    public int updateDraft(SQLiteDatabase db, long userId, long id, int expectedVersion,
                           String destination, String notes, String activity, long activityCents,
                           long customCents, long mealsCents, long discountCents, long totalCents, long now) {
        ContentValues v = new ContentValues();
        v.put("destination", destination);
        v.put("notes", notes);
        v.put("activity", activity);
        v.put("activity_cost_cents", activityCents);
        v.put("custom_expense_cents", customCents);
        v.put("meals_expense_cents", mealsCents);
        v.put("discount_cents", discountCents);
        v.put("total_cents", totalCents);
        v.put("version", expectedVersion + 1);
        v.put("updated_at", now);
        return db.update("trips", v, "id = ? AND user_id = ? AND version = ? AND status = 'DRAFT'",
                new String[]{String.valueOf(id), String.valueOf(userId), String.valueOf(expectedVersion)});
    }

    public int confirm(SQLiteDatabase db, long userId, long id, int expectedVersion,
                       long discountCents, long totalCents, long now) {
        ContentValues v = new ContentValues();
        v.put("status", Trip.CONFIRMED);
        v.put("discount_cents", discountCents);
        v.put("total_cents", totalCents);
        v.put("version", expectedVersion + 1);
        v.put("updated_at", now);
        return db.update("trips", v, "id = ? AND user_id = ? AND version = ? AND status = 'DRAFT'",
                new String[]{String.valueOf(id), String.valueOf(userId), String.valueOf(expectedVersion)});
    }

    public Trip find(SQLiteDatabase db, long userId, long id) {
        try (Cursor c = db.query("trips", COLUMNS, "id = ? AND user_id = ?",
                new String[]{String.valueOf(id), String.valueOf(userId)}, null, null, null, "1")) {
            return c.moveToFirst() ? map(c) : null;
        }
    }

    /** Keyset pagination: pass Long.MAX_VALUE for the first page, then the last id of the previous page. */
    public List<Trip> page(SQLiteDatabase db, long userId, long beforeId, int limit) {
        List<Trip> out = new ArrayList<>(limit);
        try (Cursor c = db.query("trips", COLUMNS, "user_id = ? AND id < ?",
                new String[]{String.valueOf(userId), String.valueOf(beforeId)},
                null, null, "id DESC", String.valueOf(limit))) {
            while (c.moveToNext()) out.add(map(c));
        }
        return out;
    }

    public int countByStatus(SQLiteDatabase db, long userId, String status) {
        return (int) DatabaseUtils.longForQuery(db,
                "SELECT COUNT(*) FROM trips WHERE user_id = ? AND status = ?",
                new String[]{String.valueOf(userId), status});
    }

    public long sumTotal(SQLiteDatabase db, long userId, String status) {
        return DatabaseUtils.longForQuery(db,
                "SELECT COALESCE(SUM(total_cents), 0) FROM trips WHERE user_id = ? AND status = ?",
                new String[]{String.valueOf(userId), status});
    }

    public int delete(SQLiteDatabase db, long userId, long id) {
        return db.delete("trips", "id = ? AND user_id = ?",
                new String[]{String.valueOf(id), String.valueOf(userId)});
    }

    private static Trip map(Cursor c) {
        return new Trip(c.getLong(0), c.getString(1), c.getLong(2), c.getString(3), c.getString(4),
                c.getString(5), c.getLong(6), c.getLong(7), c.getLong(8), c.getLong(9), c.getLong(10),
                c.getString(11), c.getInt(12), c.getLong(13), c.getLong(14));
    }
}
