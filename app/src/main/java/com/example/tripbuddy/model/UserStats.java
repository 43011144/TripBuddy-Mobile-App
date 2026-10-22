package com.example.tripbuddy.model;

/** Counters shown on the home and dashboard screens. */
public final class UserStats {
    public final int confirmedTrips;
    public final int draftTrips;
    public final long confirmedTotalCents;
    public final int memories;

    public UserStats(int confirmedTrips, int draftTrips, long confirmedTotalCents, int memories) {
        this.confirmedTrips = confirmedTrips;
        this.draftTrips = draftTrips;
        this.confirmedTotalCents = confirmedTotalCents;
        this.memories = memories;
    }
}
