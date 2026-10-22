package com.example.tripbuddy;

import android.content.Context;

import com.example.tripbuddy.repo.AuthRepository;
import com.example.tripbuddy.repo.MemoryRepository;
import com.example.tripbuddy.repo.TripRepository;

/**
 * Tiny dependency provider: one shared repository per type. Screens ask for what they need
 * instead of building database objects themselves, which keeps them easy to test or swap
 * for a network-backed version later.
 */
public final class ServiceLocator {

    private static AuthRepository auth;
    private static TripRepository trips;
    private static MemoryRepository memories;

    private ServiceLocator() { }

    public static synchronized AuthRepository auth(Context c) {
        if (auth == null) auth = new AuthRepository(c.getApplicationContext());
        return auth;
    }

    public static synchronized TripRepository trips(Context c) {
        if (trips == null) trips = new TripRepository(c.getApplicationContext());
        return trips;
    }

    public static synchronized MemoryRepository memories(Context c) {
        if (memories == null) memories = new MemoryRepository(c.getApplicationContext());
        return memories;
    }
}
