package com.example.tripbuddy.core;

/** Result of an asynchronous operation. Always invoked on the main thread. */
public interface Callback<T> {
    void onSuccess(T value);
    void onError(AppException error);
}
