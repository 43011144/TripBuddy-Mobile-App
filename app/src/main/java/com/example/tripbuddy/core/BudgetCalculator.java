package com.example.tripbuddy.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single source of truth for trip pricing. Used by the budget screen for the live
 * preview and by TripRepository when saving, so the stored total can never be
 * tampered with from the UI.
 */
public final class BudgetCalculator {

    public static final int LOYALTY_MIN_TRIPS = 3;
    public static final int LOYALTY_PERCENT = 10;

    /** Predefined activities in a stable order (the old HashMap shuffled the spinner). */
    public static final Map<String, Long> ACTIVITIES;

    static {
        Map<String, Long> m = new LinkedHashMap<>();
        m.put("Sightseeing", 50_000L);
        m.put("Hiking", 70_000L);
        m.put("Beach Day", 100_000L);
        m.put("Museum Visit", 40_000L);
        ACTIVITIES = Collections.unmodifiableMap(m);
    }

    private BudgetCalculator() { }

    public static final class Quote {
        public final long subtotalCents;
        public final long discountCents;
        public final long totalCents;

        Quote(long subtotal, long discount) {
            this.subtotalCents = subtotal;
            this.discountCents = discount;
            this.totalCents = subtotal - discount;
        }
    }

    public static Long priceOf(String activity) {
        return ACTIVITIES.get(activity);
    }

    public static Quote quote(long activityCents, long customCents, long mealsCents, int confirmedTrips) {
        long subtotal = Math.addExact(Math.addExact(activityCents, customCents), mealsCents);
        long discount = confirmedTrips >= LOYALTY_MIN_TRIPS ? Money.percentOf(subtotal, LOYALTY_PERCENT) : 0;
        return new Quote(subtotal, discount);
    }
}
