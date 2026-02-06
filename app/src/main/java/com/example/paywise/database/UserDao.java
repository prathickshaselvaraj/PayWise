package com.example.paywise.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.paywise.models.User;
import com.example.paywise.utils.Constants;

/**
 * UserDao - Data Access Object for users table
 *
 * Handles all database operations for users:
 * - Insert new user
 * - Get user by ID or mobile number
 * - Update user information
 * - Handle failed login attempts and lockout
 */
public class UserDao {

    private DatabaseHelper dbHelper;

    public UserDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Insert a new user
     *
     * @param user User object
     * @return user ID of inserted user, -1 if failed
     */
    public long insertUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("full_name", user.getFullName());
        values.put("mobile_number", user.getMobileNumber());
        values.put("email", user.getEmail());
        values.put("app_pin_hash", user.getAppPinHash());
        values.put("profile_image_path", user.getProfileImagePath());
        values.put("is_locked", user.isLocked() ? 1 : 0);
        values.put("failed_attempts", user.getFailedAttempts());
        values.put("lockout_until", user.getLockoutUntil());
        values.put("created_at", user.getCreatedAt());
        values.put("updated_at", user.getUpdatedAt());

        long userId = db.insert(Constants.TABLE_USERS, null, values);
        db.close();
        return userId;
    }

    /**
     * Get user by ID
     *
     * @param userId User ID
     * @return User object or null
     */
    public User getUserById(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        Cursor cursor = db.query(Constants.TABLE_USERS,
                null,
                "user_id = ?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            user = extractUserFromCursor(cursor);
            cursor.close();
        }

        db.close();
        return user;
    }

    /**
     * Get user by mobile number
     *
     * @param mobileNumber Mobile number (10 digits)
     * @return User object or null
     */
    public User getUserByMobileNumber(String mobileNumber) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        Cursor cursor = db.query(Constants.TABLE_USERS,
                null,
                "mobile_number = ?",
                new String[]{mobileNumber},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            user = extractUserFromCursor(cursor);
            cursor.close();
        }

        db.close();
        return user;
    }

    /**
     * Update user information
     *
     * @param user User object with updated information
     * @return number of rows affected
     */
    public int updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("full_name", user.getFullName());
        values.put("email", user.getEmail());
        values.put("profile_image_path", user.getProfileImagePath());
        values.put("updated_at", user.getUpdatedAt());

        int rowsAffected = db.update(Constants.TABLE_USERS,
                values,
                "user_id = ?",
                new String[]{String.valueOf(user.getUserId())});

        db.close();
        return rowsAffected;
    }

    /**
     * Update user PIN
     *
     * @param userId User ID
     * @param newPinHash New hashed PIN
     * @param updatedAt Timestamp
     * @return number of rows affected
     */
    public int updateUserPin(int userId, String newPinHash, String updatedAt) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("app_pin_hash", newPinHash);
        values.put("updated_at", updatedAt);

        int rowsAffected = db.update(Constants.TABLE_USERS,
                values,
                "user_id = ?",
                new String[]{String.valueOf(userId)});

        db.close();
        return rowsAffected;
    }

    /**
     * Increment failed login attempts
     *
     * @param userId User ID
     * @return number of rows affected
     */
    public int incrementFailedAttempts(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Get current failed attempts
        User user = getUserById(userId);
        if (user == null) return 0;

        int newAttempts = user.getFailedAttempts() + 1;

        ContentValues values = new ContentValues();
        values.put("failed_attempts", newAttempts);

        // Lock account after max attempts
        if (newAttempts >= Constants.MAX_LOGIN_ATTEMPTS) {
            values.put("is_locked", 1);

            // Set lockout time
            long lockoutUntil = System.currentTimeMillis() + Constants.LOCKOUT_DURATION_MS;
            values.put("lockout_until", String.valueOf(lockoutUntil));
        }

        int rowsAffected = db.update(Constants.TABLE_USERS,
                values,
                "user_id = ?",
                new String[]{String.valueOf(userId)});

        db.close();
        return rowsAffected;
    }

    /**
     * Reset failed attempts after successful login
     *
     * @param userId User ID
     * @return number of rows affected
     */
    public int resetFailedAttempts(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("failed_attempts", 0);
        values.put("is_locked", 0);
        values.putNull("lockout_until");

        int rowsAffected = db.update(Constants.TABLE_USERS,
                values,
                "user_id = ?",
                new String[]{String.valueOf(userId)});

        db.close();
        return rowsAffected;
    }

    /**
     * Check if user account is locked
     *
     * @param userId User ID
     * @return true if locked, false otherwise
     */
    public boolean isAccountLocked(int userId) {
        User user = getUserById(userId);
        if (user == null) return false;

        // Check if lockout time has passed
        if (user.getLockoutUntil() != null) {
            try {
                long lockoutTime = Long.parseLong(user.getLockoutUntil());
                if (System.currentTimeMillis() > lockoutTime) {
                    // Lockout expired, reset
                    resetFailedAttempts(userId);
                    return false;
                }
            } catch (NumberFormatException e) {
                return user.isLocked();
            }
        }

        return user.isLocked();
    }

    /**
     * Check if any user exists in database
     *
     * @return true if user exists
     */
    public boolean isUserExists() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + Constants.TABLE_USERS, null);

        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        db.close();
        return exists;
    }

    /**
     * Get first user (assuming single user app)
     *
     * @return User object or null
     */
    public User getFirstUser() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        Cursor cursor = db.query(Constants.TABLE_USERS,
                null, null, null, null, null,
                "user_id ASC", "1");

        if (cursor != null && cursor.moveToFirst()) {
            user = extractUserFromCursor(cursor);
            cursor.close();
        }

        db.close();
        return user;
    }

    /**
     * Check if mobile number already exists
     *
     * @param mobileNumber Mobile number to check
     * @return true if exists, false otherwise
     */
    public boolean mobileNumberExists(String mobileNumber) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(Constants.TABLE_USERS,
                new String[]{"user_id"},
                "mobile_number = ?",
                new String[]{mobileNumber},
                null, null, null);

        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        db.close();
        return exists;
    }

    /**
     * Extract User object from cursor
     */
    private User extractUserFromCursor(Cursor cursor) {
        User user = new User();
        user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
        user.setMobileNumber(cursor.getString(cursor.getColumnIndexOrThrow("mobile_number")));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
        user.setAppPinHash(cursor.getString(cursor.getColumnIndexOrThrow("app_pin_hash")));
        user.setProfileImagePath(cursor.getString(cursor.getColumnIndexOrThrow("profile_image_path")));
        user.setLocked(cursor.getInt(cursor.getColumnIndexOrThrow("is_locked")) == 1);
        user.setFailedAttempts(cursor.getInt(cursor.getColumnIndexOrThrow("failed_attempts")));
        user.setLockoutUntil(cursor.getString(cursor.getColumnIndexOrThrow("lockout_until")));
        user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        user.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
        return user;
    }
}