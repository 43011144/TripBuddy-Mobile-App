package com.example.tripbuddy.repo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.tripbuddy.core.AppException;
import com.example.tripbuddy.core.AppExecutors;
import com.example.tripbuddy.core.BudgetCalculator;
import com.example.tripbuddy.core.Callback;
import com.example.tripbuddy.core.Money;
import com.example.tripbuddy.data.MemoryDao;
import com.example.tripbuddy.data.TripBuddyDbHelper;
import com.example.tripbuddy.data.TripDao;
import com.example.tripbuddy.model.Trip;
import com.example.tripbuddy.model.TripInput;
import com.example.tripbuddy.model.UserStats;
import com.example.tripbuddy.security.InputValidator;

import java.util.List;
import java.util.UUID;

/**
 * Trip planning rules live here, not in the screens:
 *  - prices and discounts are recalculated from the database, never trusted from the UI
 *  - save + confirm happen in one transaction
 *  - optimistic locking (version column) stops two quick taps or two screens overwriting each other
 */
public final class TripRepository {

    public static final int PAGE_SIZE = 20;

    private final TripBuddyDbHelper helper;
    private final TripDao trips = new TripDao();
    private final MemoryDao memories = new MemoryDao();
    private final AppExecutors ex = AppExecutors.get();

    public TripRepository(Context context) {
        this.helper = TripBuddyDbHelper.get(context);
    }

    /** Creates a draft, or updates the given draft. */
    public void saveDraft(long userId, Trip existing, TripInput input, Callback<Trip> cb) {
        persist(userId, existing, input, false, cb);
    }

    /** Saves (if needed) and confirms the trip atomically. */
    public void confirm(long userId, Trip existing, TripInput input, Callback<Trip> cb) {
        persist(userId, existing, input, true, cb);
    }

    private void persist(long userId, Trip existing, TripInput input, boolean confirm, Callback<Trip> cb) {
        ex.write(() -> {
            SQLiteDatabase db = helper.getWritableDatabase();
            try {
                TripInput clean = validate(input);
                Trip saved;
                db.beginTransaction();
                try {
                    saved = upsert(db, userId, existing, clean, confirm);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                ex.deliver(cb, saved); // only after the commit succeeded
            } catch (AppException | RuntimeException e) {
                ex.fail(cb, e);
            }
        });
    }

    private Trip upsert(SQLiteDatabase db, long userId, Trip existing, TripInput in, boolean confirm)
            throws AppException {
        long activityCents = BudgetCalculator.priceOf(in.activity);
        int confirmedTrips = trips.countByStatus(db, userId, Trip.CONFIRMED);
        BudgetCalculator.Quote q = BudgetCalculator.quote(
                activityCents, in.customExpenseCents, in.mealsExpenseCents, confirmedTrips);
        long now = System.currentTimeMillis();

        long id;
        int version;
        if (existing == null) {
            id = trips.insert(db, userId, UUID.randomUUID().toString(), in.destination, in.notes,
                    in.activity, activityCents, in.customExpenseCents, in.mealsExpenseCents,
                    q.discountCents, q.totalCents, now);
            version = 1;
        } else {
            if (existing.isConfirmed()) {
                throw new AppException(AppException.Code.CONFLICT, "This trip is already confirmed");
            }
            int changed = trips.updateDraft(db, userId, existing.id, existing.version, in.destination,
                    in.notes, in.activity, activityCents, in.customExpenseCents, in.mealsExpenseCents,
                    q.discountCents, q.totalCents, now);
            if (changed == 0) {
                throw new AppException(AppException.Code.CONFLICT,
                        "This trip was changed somewhere else. Reopen it and try again.");
            }
            id = existing.id;
            version = existing.version + 1;
        }
        if (confirm) {
            int changed = trips.confirm(db, userId, id, version, q.discountCents, q.totalCents, now);
            if (changed == 0) {
                throw new AppException(AppException.Code.CONFLICT, "Could not confirm this trip. Please try again.");
            }
        }
        return trips.find(db, userId, id);
    }

    private static TripInput validate(TripInput in) throws AppException {
        String destination = InputValidator.sanitize(in.destination);
        String notes = InputValidator.sanitize(in.notes);
        String error = InputValidator.requiredTextError(destination, InputValidator.DESTINATION_MAX, "Destination");
        if (error == null) error = InputValidator.optionalTextError(notes, InputValidator.NOTES_MAX, "Notes");
        if (error == null && BudgetCalculator.priceOf(in.activity) == null) error = "Choose an activity";
        if (error == null && (in.customExpenseCents < 0 || in.customExpenseCents > Money.MAX_CENTS
                || in.mealsExpenseCents < 0 || in.mealsExpenseCents > Money.MAX_CENTS)) {
            error = "Amount is out of range";
        }
        if (error != null) throw new AppException(AppException.Code.VALIDATION, error);
        return new TripInput(destination, notes, in.activity, in.customExpenseCents, in.mealsExpenseCents);
    }

    /** Pass Long.MAX_VALUE for the first page, then the id of the last trip already shown. */
    public void loadPage(long userId, long beforeId, Callback<List<Trip>> cb) {
        ex.read(() -> {
            try {
                ex.deliver(cb, trips.page(helper.getReadableDatabase(), userId, beforeId, PAGE_SIZE));
            } catch (RuntimeException e) {
                ex.fail(cb, e);
            }
        });
    }

    public void confirmedCount(long userId, Callback<Integer> cb) {
        ex.read(() -> {
            try {
                ex.deliver(cb, trips.countByStatus(helper.getReadableDatabase(), userId, Trip.CONFIRMED));
            } catch (RuntimeException e) {
                ex.fail(cb, e);
            }
        });
    }

    public void delete(long userId, long tripId, Callback<Boolean> cb) {
        ex.write(() -> {
            try {
                ex.deliver(cb, trips.delete(helper.getWritableDatabase(), userId, tripId) > 0);
            } catch (RuntimeException e) {
                ex.fail(cb, e);
            }
        });
    }

    public void loadStats(long userId, Callback<UserStats> cb) {
        ex.read(() -> {
            try {
                SQLiteDatabase db = helper.getReadableDatabase();
                ex.deliver(cb, new UserStats(
                        trips.countByStatus(db, userId, Trip.CONFIRMED),
                        trips.countByStatus(db, userId, Trip.DRAFT),
                        trips.sumTotal(db, userId, Trip.CONFIRMED),
                        memories.count(db, userId)));
            } catch (RuntimeException e) {
                ex.fail(cb, e);
            }
        });
    }
}
