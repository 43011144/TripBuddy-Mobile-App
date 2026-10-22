package com.example.tripbuddy.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.example.tripbuddy.model.Memory;

import java.util.ArrayList;
import java.util.List;

/** SQL for the memories table. Scoped by user_id on every query. */
public final class MemoryDao {

    private static final String[] COLUMNS = {"id", "user_id", "content", "created_at"};

    public long insert(SQLiteDatabase db, long userId, String uuid, String content, long now) {
        ContentValues v = new ContentValues();
        v.put("uuid", uuid);
        v.put("user_id", userId);
        v.put("content", content);
        v.put("created_at", now);
        return db.insertOrThrow("memories", null, v);
    }

    /** Keyset pagination, newest first. Pass Long.MAX_VALUE for the first page. */
    public List<Memory> page(SQLiteDatabase db, long userId, long beforeId, int limit) {
        List<Memory> out = new ArrayList<>(limit);
        try (Cursor c = db.query("memories", COLUMNS, "user_id = ? AND id < ?",
                new String[]{String.valueOf(userId), String.valueOf(beforeId)},
                null, null, "id DESC", String.valueOf(limit))) {
            while (c.moveToNext()) {
                out.add(new Memory(c.getLong(0), c.getLong(1), c.getString(2), c.getLong(3)));
            }
        }
        return out;
    }

    public int count(SQLiteDatabase db, long userId) {
        return (int) DatabaseUtils.longForQuery(db,
                "SELECT COUNT(*) FROM memories WHERE user_id = ?", new String[]{String.valueOf(userId)});
    }

    public int delete(SQLiteDatabase db, long userId, long id) {
        return db.delete("memories", "id = ? AND user_id = ?",
                new String[]{String.valueOf(id), String.valueOf(userId)});
    }
}
