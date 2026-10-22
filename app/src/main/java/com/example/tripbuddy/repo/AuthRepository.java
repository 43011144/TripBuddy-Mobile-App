package com.example.tripbuddy.repo;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import com.example.tripbuddy.core.AppException;
import com.example.tripbuddy.core.AppExecutors;
import com.example.tripbuddy.core.Callback;
import com.example.tripbuddy.data.TripBuddyDbHelper;
import com.example.tripbuddy.data.UserDao;
import com.example.tripbuddy.model.User;
import com.example.tripbuddy.security.InputValidator;
import com.example.tripbuddy.security.PasswordHasher;

import java.util.Arrays;

/**
 * Accounts: register, sign in, delete.
 *
 * Threading: lookups run on the read pool, hashing on the crypto pool, writes on the single
 * writer thread. Each hand-off is a separate step so slow hashing never blocks other work.
 */
public final class AuthRepository {

    static final int MAX_FAILED_ATTEMPTS = 5;
    static final long LOCK_MS = 5L * 60 * 1000;
    private static final String GENERIC_FAILURE = "Incorrect username or password";

    private final TripBuddyDbHelper helper;
    private final UserDao users = new UserDao();
    private final AppExecutors ex = AppExecutors.get();

    public AuthRepository(Context context) {
        this.helper = TripBuddyDbHelper.get(context);
    }

    /** Wipes the password array after use. */
    public void register(String rawUsername, char[] password, Callback<User> cb) {
        final String username = InputValidator.sanitize(rawUsername);
        String error = InputValidator.usernameError(username);
        if (error == null) error = InputValidator.passwordError(password, username);
        if (error != null) {
            Arrays.fill(password, '\0');
            ex.fail(cb, new AppException(AppException.Code.VALIDATION, error));
            return;
        }
        ex.crypto(() -> {
            try {
                final String hash = PasswordHasher.hash(password);
                ex.write(() -> {
                    try {
                        long id = users.insert(helper.getWritableDatabase(), username, hash, System.currentTimeMillis());
                        ex.deliver(cb, new User(id, username));
                    } catch (SQLiteConstraintException e) {
                        ex.fail(cb, new AppException(AppException.Code.DUPLICATE, "That username is already taken"));
                    } catch (RuntimeException e) {
                        ex.fail(cb, e);
                    }
                });
            } catch (RuntimeException e) {
                ex.fail(cb, e);
            }
        });
    }

    /** Wipes the password array after use. */
    public void login(String rawUsername, char[] password, Callback<User> cb) {
        final String username = InputValidator.sanitize(rawUsername);
        if (username.isEmpty() || password.length == 0 || password.length > InputValidator.PASSWORD_MAX) {
            Arrays.fill(password, '\0');
            ex.fail(cb, new AppException(AppException.Code.VALIDATION, "Enter your username and password"));
            return;
        }
        ex.read(() -> {
            try {
                UserDao.Credentials cred = users.findByUsername(helper.getReadableDatabase(), username);
                continueLogin(cred, password, cb);
            } catch (RuntimeException e) {
                Arrays.fill(password, '\0');
                ex.fail(cb, e);
            }
        });
    }

    private void continueLogin(UserDao.Credentials cred, char[] password, Callback<User> cb) {
        if (cred == null) {
            ex.crypto(() -> {
                PasswordHasher.verifyAgainstDummy(password); // same cost as a real check
                ex.fail(cb, new AppException(AppException.Code.AUTH_FAILED, GENERIC_FAILURE));
            });
            return;
        }
        long now = System.currentTimeMillis();
        if (cred.lockedUntil > now) {
            Arrays.fill(password, '\0');
            long minutes = (cred.lockedUntil - now + 59_999) / 60_000;
            ex.fail(cb, new AppException(AppException.Code.LOCKED,
                    "Too many failed attempts. Try again in " + minutes + (minutes == 1 ? " minute" : " minutes")));
            return;
        }
        ex.crypto(() -> {
            try {
                char[] spare = password.clone();
                boolean ok = PasswordHasher.verify(password, cred.passwordHash);
                String upgraded = null;
                if (ok && PasswordHasher.needsRehash(cred.passwordHash)) {
                    upgraded = PasswordHasher.hash(spare);
                } else {
                    Arrays.fill(spare, '\0');
                }
                final String newHash = upgraded;
                ex.write(() -> {
                    try {
                        SQLiteDatabase db = helper.getWritableDatabase();
                        if (ok) {
                            users.resetFailures(db, cred.id);
                            if (newHash != null) users.updateHash(db, cred.id, newHash);
                            ex.deliver(cb, new User(cred.id, cred.username));
                        } else {
                            users.recordFailure(db, cred.id, MAX_FAILED_ATTEMPTS, LOCK_MS, System.currentTimeMillis());
                            ex.fail(cb, new AppException(AppException.Code.AUTH_FAILED, GENERIC_FAILURE));
                        }
                    } catch (RuntimeException e) {
                        ex.fail(cb, e);
                    }
                });
            } catch (RuntimeException e) {
                ex.fail(cb, e);
            }
        });
    }

    /** Deletes the account and, through foreign key cascades, all of its trips and memories. */
    public void deleteAccount(long userId, Callback<Boolean> cb) {
        ex.write(() -> {
            try {
                ex.deliver(cb, users.delete(helper.getWritableDatabase(), userId) > 0);
            } catch (RuntimeException e) {
                ex.fail(cb, e);
            }
        });
    }
}
