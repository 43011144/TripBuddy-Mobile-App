package com.example.tripbuddy.core;

/** Error type surfaced to the UI. The message is always safe to show to a user. */
public class AppException extends Exception {

    public enum Code { VALIDATION, AUTH_FAILED, LOCKED, DUPLICATE, CONFLICT, NOT_FOUND, STORAGE, BUSY }

    private final Code code;

    public AppException(Code code, String userMessage) {
        super(userMessage);
        this.code = code;
    }

    public AppException(Code code, String userMessage, Throwable cause) {
        super(userMessage, cause);
        this.code = code;
    }

    public Code getCode() { return code; }
}
