package com.example.tripbuddy.security;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Salted PBKDF2 password hashing (no extra libraries needed).
 *
 * Stored format:  scheme$iterations$saltHex$hashHex
 * The scheme and iteration count live inside the string, so the cost can be raised
 * later and old hashes are upgraded the next time the user signs in (see needsRehash).
 *
 * Hashing is deliberately slow: always call it from AppExecutors.crypto(), never the main thread.
 */
public final class PasswordHasher {

    static final String SCHEME_SHA256 = "pbkdf2_sha256";
    static final String SCHEME_SHA1 = "pbkdf2_sha1"; // fallback for very old devices only
    static final int ITERATIONS = 310_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static String dummyHash;

    private PasswordHasher() { }

    /** Hashes the password and wipes the supplied array. */
    public static String hash(char[] password) {
        try {
            byte[] salt = new byte[SALT_BYTES];
            RANDOM.nextBytes(salt);
            String scheme = SCHEME_SHA256;
            byte[] out;
            try {
                out = derive(scheme, password, salt, ITERATIONS);
            } catch (java.security.NoSuchAlgorithmException e) {
                scheme = SCHEME_SHA1;
                out = derive(scheme, password, salt, ITERATIONS);
            }
            return scheme + "$" + ITERATIONS + "$" + toHex(salt) + "$" + toHex(out);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing unavailable", e);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    /** Constant-time verification. Wipes the supplied array. */
    public static boolean verify(char[] password, String stored) {
        try {
            if (stored == null) return false;
            String[] p = stored.split("\\$");
            if (p.length != 4) return false;
            int iterations = Integer.parseInt(p[1]);
            if (iterations < 1 || iterations > 5_000_000) return false;
            byte[] salt = fromHex(p[2]);
            byte[] expected = fromHex(p[3]);
            byte[] actual = derive(p[0], password, salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (GeneralSecurityException | RuntimeException e) {
            return false;
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    public static boolean needsRehash(String stored) {
        if (stored == null) return true;
        String[] p = stored.split("\\$");
        if (p.length != 4) return true;
        try {
            return !SCHEME_SHA256.equals(p[0]) || Integer.parseInt(p[1]) < ITERATIONS;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    /**
     * Burns the same CPU time as a real check. Used when the username does not exist so
     * response timing does not reveal which usernames are registered.
     */
    public static void verifyAgainstDummy(char[] password) {
        String h;
        synchronized (PasswordHasher.class) {
            if (dummyHash == null) dummyHash = hash("tripbuddy-dummy-password".toCharArray());
            h = dummyHash;
        }
        verify(password, h);
    }

    private static byte[] derive(String scheme, char[] password, byte[] salt, int iterations)
            throws GeneralSecurityException {
        String algorithm;
        if (SCHEME_SHA256.equals(scheme)) algorithm = "PBKDF2WithHmacSHA256";
        else if (SCHEME_SHA1.equals(scheme)) algorithm = "PBKDF2WithHmacSHA1";
        else throw new GeneralSecurityException("Unknown scheme");
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance(algorithm).generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    static String toHex(byte[] bytes) {
        char[] hex = "0123456789abcdef".toCharArray();
        char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            out[i * 2] = hex[(bytes[i] >> 4) & 0xF];
            out[i * 2 + 1] = hex[bytes[i] & 0xF];
        }
        return new String(out);
    }

    static byte[] fromHex(String s) {
        if (s.length() % 2 != 0) throw new IllegalArgumentException("bad hex");
        byte[] out = new byte[s.length() / 2];
        for (int i = 0; i < out.length; i++) {
            int hi = Character.digit(s.charAt(i * 2), 16);
            int lo = Character.digit(s.charAt(i * 2 + 1), 16);
            if (hi < 0 || lo < 0) throw new IllegalArgumentException("bad hex");
            out[i] = (byte) ((hi << 4) | lo);
        }
        return out;
    }
}
