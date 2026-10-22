package com.example.tripbuddy.repo;

import android.content.Context;

import com.example.tripbuddy.core.AppException;
import com.example.tripbuddy.core.AppExecutors;
import com.example.tripbuddy.core.Callback;
import com.example.tripbuddy.data.MemoryDao;
import com.example.tripbuddy.data.TripBuddyDbHelper;
import com.example.tripbuddy.model.Memory;
import com.example.tripbuddy.security.InputValidator;

import java.util.List;
import java.util.UUID;

public final class MemoryRepository {

    public static final int PAGE_SIZE = 20;

    private final TripBuddyDbHelper helper;
    private final MemoryDao memories = new MemoryDao();
    private final AppExecutors ex = AppExecutors.get();

    public MemoryRepository(Context context) {
        this.helper = TripBuddyDbHelper.get(context);
    }

    public void add(long userId, String rawText, Callback<Memory> cb) {
        final String text = InputValidator.sanitize(rawText);
        String error = InputValidator.requiredTextError(text, InputValidator.MEMORY_MAX, "Memory");
        if (error != null) {
            ex.fail(cb, new AppException(AppException.Code.VALIDATION, error));
            return;
        }
        ex.write(() -> {
            try {
                long now = System.currentTimeMillis();
                long id = memories.insert(helper.getWritableDatabase(), userId, UUID.randomUUID().toString(), text, now);
                ex.deliver(cb, new Memory(id, userId, text, now));
            } catch (RuntimeException e) {
                ex.fail(cb, e);
            }
        });
    }

    /** Pass Long.MAX_VALUE for the first page, then the id of the last memory already shown. */
    public void loadPage(long userId, long beforeId, Callback<List<Memory>> cb) {
        ex.read(() -> {
            try {
                ex.deliver(cb, memories.page(helper.getReadableDatabase(), userId, beforeId, PAGE_SIZE));
            } catch (RuntimeException e) {
                ex.fail(cb, e);
            }
        });
    }

    /** Delivers the newest memory, or null when the user has none yet. */
    public void latest(long userId, Callback<Memory> cb) {
        ex.read(() -> {
            try {
                List<Memory> one = memories.page(helper.getReadableDatabase(), userId, Long.MAX_VALUE, 1);
                ex.deliver(cb, one.isEmpty() ? null : one.get(0));
            } catch (RuntimeException e) {
                ex.fail(cb, e);
            }
        });
    }

    public void delete(long userId, long memoryId, Callback<Boolean> cb) {
        ex.write(() -> {
            try {
                ex.deliver(cb, memories.delete(helper.getWritableDatabase(), userId, memoryId) > 0);
            } catch (RuntimeException e) {
                ex.fail(cb, e);
            }
        });
    }
}
