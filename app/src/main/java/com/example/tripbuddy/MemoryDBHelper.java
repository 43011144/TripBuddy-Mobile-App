package com.example.tripbuddy;

import android.content.Context;

import com.example.tripbuddy.core.AppException;
import com.example.tripbuddy.core.Callback;
import com.example.tripbuddy.model.Memory;
import com.example.tripbuddy.security.SessionManager;

/**
 * Keeps the original MemoryDBHelper name and its two operations (addMemory, getLastMemory)
 * so existing screens read the same, but the work now goes through MemoryRepository:
 * asynchronous, per-user and validated.
 */
public class MemoryDBHelper {

    private final Context context;
    private final SessionManager session;

    public MemoryDBHelper(Context context) {
        this.context = context.getApplicationContext();
        this.session = new SessionManager(context);
    }

    public void addMemory(String text, Callback<Memory> cb) {
        if (!session.isValid()) {
            cb.onError(new AppException(AppException.Code.AUTH_FAILED, "Please sign in first"));
            return;
        }
        ServiceLocator.memories(context).add(session.userId(), text, cb);
    }

    /** Delivers null when there are no memories yet. */
    public void getLastMemory(Callback<Memory> cb) {
        if (!session.isValid()) {
            cb.onError(new AppException(AppException.Code.AUTH_FAILED, "Please sign in first"));
            return;
        }
        ServiceLocator.memories(context).latest(session.userId(), cb);
    }
}
