package com.example.tripbuddy.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class MoneyAndBudgetTest {

    @Test
    public void parsesWholeAndDecimalAmounts() {
        assertEquals(0, Money.parseToCents(""));
        assertEquals(0, Money.parseToCents(null));
        assertEquals(25000, Money.parseToCents("250"));
        assertEquals(25050, Money.parseToCents("250.50"));
        assertEquals(25050, Money.parseToCents("250,5"));
        assertEquals(10, Money.parseToCents("0.1"));
    }

    @Test
    public void rejectsBadAmounts() {
        for (String bad : new String[]{"abc", "-5", "1.234", "1e5", "12345678901", "1..2"}) {
            try {
                Money.parseToCents(bad);
                fail("should reject " + bad);
            } catch (IllegalArgumentException expected) {
                assertNotNull(expected.getMessage());
            }
        }
    }

    @Test
    public void percentRoundsHalfUp() {
        assertEquals(100, Money.percentOf(1000, 10));
        assertEquals(1, Money.percentOf(5, 10));   // 0.5 rounds up
        assertEquals(0, Money.percentOf(4, 10));
    }

    @Test
    public void noFloatingPointDrift() {
        // 0.1 + 0.2 style errors are impossible with cents
        long total = Money.parseToCents("0.10") + Money.parseToCents("0.20");
        assertEquals(30, total);
    }

    @Test
    public void loyaltyDiscountStartsAtThreeConfirmedTrips() {
        long activity = BudgetCalculator.priceOf("Hiking");
        assertEquals(70_000, activity);

        BudgetCalculator.Quote none = BudgetCalculator.quote(activity, 10_000, 5_000, 2);
        assertEquals(85_000, none.subtotalCents);
        assertEquals(0, none.discountCents);
        assertEquals(85_000, none.totalCents);

        BudgetCalculator.Quote loyal = BudgetCalculator.quote(activity, 10_000, 5_000, 3);
        assertEquals(8_500, loyal.discountCents);
        assertEquals(76_500, loyal.totalCents);
    }

    @Test
    public void activityCatalogueKeepsStableOrderAndRejectsUnknown() {
        assertEquals(Arrays.asList("Sightseeing", "Hiking", "Beach Day", "Museum Visit"),
                new ArrayList<>(BudgetCalculator.ACTIVITIES.keySet()));
        assertNull(BudgetCalculator.priceOf("Skydiving"));
    }
}
