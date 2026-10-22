package com.example.tripbuddy.security;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SecurityTest {

    @Test
    public void hashVerifiesCorrectPasswordOnly() {
        String hash = PasswordHasher.hash("Correct horse 9".toCharArray());
        assertTrue(PasswordHasher.verify("Correct horse 9".toCharArray(), hash));
        assertFalse(PasswordHasher.verify("correct horse 9".toCharArray(), hash));
        assertFalse(PasswordHasher.verify("".toCharArray(), hash));
    }

    @Test
    public void samePasswordGetsDifferentSalt() {
        String a = PasswordHasher.hash("Same password 1".toCharArray());
        String b = PasswordHasher.hash("Same password 1".toCharArray());
        assertNotEquals(a, b);
    }

    @Test
    public void hashingWipesThePasswordArray() {
        char[] pw = "Wipe me 12345".toCharArray();
        PasswordHasher.hash(pw);
        assertArrayEquals(new char[13], pw);
    }

    @Test
    public void storedFormatIsSelfDescribingAndTamperSafe() {
        String hash = PasswordHasher.hash("Format check 1".toCharArray());
        assertTrue(hash.startsWith("pbkdf2_sha256$" + PasswordHasher.ITERATIONS + "$"));
        assertFalse(PasswordHasher.needsRehash(hash));
        assertTrue(PasswordHasher.needsRehash("pbkdf2_sha256$1000$aa$bb"));
        assertTrue(PasswordHasher.needsRehash("garbage"));
        assertFalse(PasswordHasher.verify("x".toCharArray(), "garbage"));
        assertFalse(PasswordHasher.verify("x".toCharArray(), null));
        assertFalse(PasswordHasher.verify("x".toCharArray(), "pbkdf2_sha256$-1$00$00"));
    }

    @Test
    public void usernameRules() {
        assertNull(InputValidator.usernameError("gift_seilane.1"));
        assertNotNull(InputValidator.usernameError("ab"));
        assertNotNull(InputValidator.usernameError("has space"));
        assertNotNull(InputValidator.usernameError("robert'); DROP TABLE users;--"));
        assertNotNull(InputValidator.usernameError(null));
    }

    @Test
    public void passwordRules() {
        assertNull(InputValidator.passwordError("abcdefg1".toCharArray(), "someone"));
        assertNotNull(InputValidator.passwordError("short1".toCharArray(), "someone"));
        assertNotNull(InputValidator.passwordError("allletters".toCharArray(), "someone"));
        assertNotNull(InputValidator.passwordError("12345678".toCharArray(), "someone"));
        assertNotNull(InputValidator.passwordError("Someone1".toCharArray(), "someone1"));
        assertNotNull(InputValidator.passwordError(new char[200], "someone"));
    }

    @Test
    public void sanitizeStripsControlCharactersButKeepsNewlines() {
        assertEquals("hello\nworld", InputValidator.sanitize("  hello\u0000\nwor\u0007ld  "));
        assertEquals("", InputValidator.sanitize(null));
        assertNotNull(InputValidator.requiredTextError("", 10, "Memory"));
        assertNotNull(InputValidator.requiredTextError("12345678901", 10, "Memory"));
        assertNull(InputValidator.requiredTextError("1234567890", 10, "Memory"));
        assertNull(InputValidator.optionalTextError("", 10, "Notes"));
    }
}
