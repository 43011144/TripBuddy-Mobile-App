package com.example.tripbuddy.core;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Money helpers. Amounts are stored and calculated as whole cents (long) so the
 * app never suffers floating point rounding errors (the old code used double).
 */
public final class Money {

    /** Upper bound for a single amount: 10,000,000.00. Protects against overflow and typos. */
    public static final long MAX_CENTS = 1_000_000_000L;

    private static final Pattern AMOUNT = Pattern.compile("^\\d{1,8}([.,]\\d{1,2})?$");

    private Money() { }

    /**
     * Parses user input such as "12", "12.5" or "12,50" into cents.
     * Empty input counts as zero. Throws IllegalArgumentException on invalid input.
     */
    public static long parseToCents(String raw) {
        if (raw == null) return 0;
        String text = raw.trim();
        if (text.isEmpty()) return 0;
        if (!AMOUNT.matcher(text).matches()) {
            throw new IllegalArgumentException("Enter a valid amount, for example 250 or 250.50");
        }
        long cents = new BigDecimal(text.replace(',', '.')).movePointRight(2).longValueExact();
        if (cents > MAX_CENTS) {
            throw new IllegalArgumentException("Amount is too large");
        }
        return cents;
    }

    /** Percentage of an amount, rounded half up, using integer maths only. */
    public static long percentOf(long cents, int percent) {
        return (Math.multiplyExact(cents, (long) percent) + 50) / 100;
    }

    public static String format(long cents) {
        return NumberFormat.getCurrencyInstance(Locale.getDefault())
                .format(BigDecimal.valueOf(cents, 2));
    }
}
