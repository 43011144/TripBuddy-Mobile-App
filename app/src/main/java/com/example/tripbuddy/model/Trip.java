package com.example.tripbuddy.model;

import java.util.Objects;

/** A planned or confirmed trip. Immutable; "version" is used for optimistic locking. */
public final class Trip {
    public static final String DRAFT = "DRAFT";
    public static final String CONFIRMED = "CONFIRMED";

    public final long id;
    public final String uuid;
    public final long userId;
    public final String destination;
    public final String notes;
    public final String activity;
    public final long activityCostCents;
    public final long customExpenseCents;
    public final long mealsExpenseCents;
    public final long discountCents;
    public final long totalCents;
    public final String status;
    public final int version;
    public final long createdAt;
    public final long updatedAt;

    public Trip(long id, String uuid, long userId, String destination, String notes, String activity,
                long activityCostCents, long customExpenseCents, long mealsExpenseCents,
                long discountCents, long totalCents, String status, int version,
                long createdAt, long updatedAt) {
        this.id = id;
        this.uuid = uuid;
        this.userId = userId;
        this.destination = destination;
        this.notes = notes;
        this.activity = activity;
        this.activityCostCents = activityCostCents;
        this.customExpenseCents = customExpenseCents;
        this.mealsExpenseCents = mealsExpenseCents;
        this.discountCents = discountCents;
        this.totalCents = totalCents;
        this.status = status;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public boolean isConfirmed() { return CONFIRMED.equals(status); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trip)) return false;
        Trip t = (Trip) o;
        return id == t.id && version == t.version && totalCents == t.totalCents
                && Objects.equals(status, t.status) && Objects.equals(destination, t.destination)
                && Objects.equals(notes, t.notes) && Objects.equals(activity, t.activity);
    }

    @Override
    public int hashCode() { return Objects.hash(id, version); }
}
