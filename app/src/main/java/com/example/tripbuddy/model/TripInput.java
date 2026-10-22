package com.example.tripbuddy.model;

/** What the budget screen sends to the repository. Prices and totals are NOT included on purpose. */
public final class TripInput {
    public final String destination;
    public final String notes;
    public final String activity;
    public final long customExpenseCents;
    public final long mealsExpenseCents;

    public TripInput(String destination, String notes, String activity,
                     long customExpenseCents, long mealsExpenseCents) {
        this.destination = destination;
        this.notes = notes;
        this.activity = activity;
        this.customExpenseCents = customExpenseCents;
        this.mealsExpenseCents = mealsExpenseCents;
    }
}
