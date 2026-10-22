package com.example.tripbuddy.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Central thread pools so no screen ever touches the database or runs password
 * hashing on the main thread.
 *
 *  - writer: one thread. Writes are serialised, which avoids SQLITE_BUSY and keeps ordering.
 *  - reader: small pool. WAL mode lets readers run while a write is in progress.
 *  - crypto: password hashing is CPU heavy, so it must not block the writer or readers.
 */
public final class AppExecutors {

    private static volatile AppExecutors instance;

    private final ExecutorService writer = Executors.newSingleThreadExecutor(factory("tb-db-write"));
    private final ThreadPoolExecutor reader = bounded(3, 128, "tb-db-read");
    private final ThreadPoolExecutor crypto = bounded(2, 32, "tb-crypto");
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private AppExecutors() { }

    public static AppExecutors get() {
        if (instance == null) {
            synchronized (AppExecutors.class) {
                if (instance == null) instance = new AppExecutors();
            }
        }
        return instance;
    }

    public void write(Runnable task) { submit(writer, task); }
    public void read(Runnable task) { submit(reader, task); }
    public void crypto(Runnable task) { submit(crypto, task); }

    public void main(Runnable task) {
        if (Looper.myLooper() == Looper.getMainLooper()) task.run();
        else mainHandler.post(task);
    }

    /** Delivers a success result on the main thread. */
    public <T> void deliver(Callback<T> cb, T value) {
        mainHandler.post(() -> cb.onSuccess(value));
    }

    /** Delivers a failure on the main thread. Unknown exceptions are mapped to a generic message. */
    public void fail(Callback<?> cb, Throwable t) {
        AppException e = (t instanceof AppException)
                ? (AppException) t
                : new AppException(AppException.Code.STORAGE, "Something went wrong. Please try again.", t);
        mainHandler.post(() -> cb.onError(e));
    }

    private static void submit(ExecutorService pool, Runnable task) {
        try {
            pool.execute(task);
        } catch (RejectedExecutionException e) {
            // Queue is full: fail loudly rather than silently dropping work or blocking the UI.
            throw new IllegalStateException("Executor saturated", e);
        }
    }

    private static ThreadPoolExecutor bounded(int threads, int queue, String name) {
        ThreadPoolExecutor ex = new ThreadPoolExecutor(threads, threads, 30, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queue), factory(name));
        ex.allowCoreThreadTimeOut(true);
        return ex;
    }

    private static ThreadFactory factory(String name) {
        AtomicInteger n = new AtomicInteger();
        return r -> new Thread(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            r.run();
        }, name + "-" + n.incrementAndGet());
    }
}
