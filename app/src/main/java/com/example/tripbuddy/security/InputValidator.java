package com.example.tripbuddy.security;

import java.util.regex.Pattern;

/**
 * Server-style validation for everything the user can type. Every method returns
 * null when the value is fine, otherwise a message that is safe to show on screen.
 * The database layer also uses parameterised queries, so validation is defence in depth,
 * not the only protection against injection.
 */
public final class InputValidator {

    public static final int USERNAME_MIN = 3;
    public static final int USERNAME_MAX = 30;
    public static final int PASSWORD_MIN = 8;
    public static final int PASSWORD_MAX = 128;
    public static final int MEMORY_MAX = 2000;
    public static final int DESTINATION_MAX = 100;
    public static final int NOTES_MAX = 500;

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_.]+$");

    private InputValidator() { }

    /** Trims, drops control characters (keeps newline and tab) and collapses null to "". */
    public static String sanitize(String raw) {
        if (raw == null) return "";
        StringBuilder sb = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (Character.isISOControl(c) && c != '\n' && c != '\t') continue;
            sb.append(c);
        }
        return sb.toString().trim();
    }

    public static String usernameError(String username) {
        if (username == null || username.length() < USERNAME_MIN || username.length() > USERNAME_MAX) {
            return "Username must be " + USERNAME_MIN + " to " + USERNAME_MAX + " characters";
        }
        if (!USERNAME.matcher(username).matches()) {
            return "Use only letters, numbers, dots and underscores";
        }
        return null;
    }

    public static String passwordError(char[] password, String username) {
        if (password == null || password.length < PASSWORD_MIN) {
            return "Password must be at least " + PASSWORD_MIN + " characters";
        }
        if (password.length > PASSWORD_MAX) {
            return "Password must be at most " + PASSWORD_MAX + " characters";
        }
        boolean letter = false, digit = false;
        for (char c : password) {
            if (Character.isLetter(c)) letter = true;
            if (Character.isDigit(c)) digit = true;
        }
        if (!letter || !digit) return "Password needs at least one letter and one number";
        if (username != null && new String(password).equalsIgnoreCase(username)) {
            return "Password must not be the same as the username";
        }
        return null;
    }

    public static String requiredTextError(String clean, int max, String label) {
        if (clean == null || clean.isEmpty()) return label + " cannot be empty";
        if (clean.length() > max) return label + " must be at most " + max + " characters";
        return null;
    }

    public static String optionalTextError(String clean, int max, String label) {
        if (clean != null && clean.length() > max) return label + " must be at most " + max + " characters";
        return null;
    }
}
