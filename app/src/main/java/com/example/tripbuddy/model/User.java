package com.example.tripbuddy.model;

/** Public view of an account. The password hash never leaves the data layer. */
public final class User {
    public final long id;
    public final String username;

    public User(long id, String username) {
        this.id = id;
        this.username = username;
    }
}
