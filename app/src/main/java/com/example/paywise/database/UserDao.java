package com.example.paywise.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.paywise.models.User;
import com.example.paywise.utils.Constants;

public class UserDao {
    private DatabaseHelper dbHelper;

    public UserDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Insert a new user
     * @param user User object
     * @return user ID of inserted user, -1 if failed
     */
    public long insertUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("full_name", user.getFullName());
        values.put("email", user.getEmail());
        values.put("phone", user.getPhone());
        values.put("profile_image_path", user.getProfileImagePath());
        values.put("created_at", user.getCreatedAt());
        values.put("updated_at", user.getUpdatedAt());

        long userId = db.insert(Constants.TABLE_USERS, null, values);
        return userId;
    }

    /**
     * Get user by ID
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
            user = new User();
            user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
            user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            user.setProfileImagePath(cursor.getString(cursor.getColumnIndexOrThrow("profile_image_path")));
            user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
            user.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
            cursor.close();
        }

        return user;
    }

    /**
     * Update user information
     * @param user User object with updated information
     * @return number of rows affected
     */
    public int updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("full_name", user.getFullName());
        values.put("email", user.getEmail());
        values.put("phone", user.getPhone());
        values.put("profile_image_path", user.getProfileImagePath());
        values.put("updated_at", user.getUpdatedAt());

        int rowsAffected = db.update(Constants.TABLE_USERS,
                values,
                "user_id = ?",
                new String[]{String.valueOf(user.getUserId())});

        return rowsAffected;
    }

    /**
     * Check if any user exists in database
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
        return exists;
    }

    /**
     * Get first user (assuming single user app)
     * @return User object or null
     */
    public User getFirstUser() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        Cursor cursor = db.query(Constants.TABLE_USERS,
                null, null, null, null, null,
                "user_id ASC", "1");

        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
            user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            user.setProfileImagePath(cursor.getString(cursor.getColumnIndexOrThrow("profile_image_path")));
            user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
            user.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
            cursor.close();
        }

        return user;
    }
}