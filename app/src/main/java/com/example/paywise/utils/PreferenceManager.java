package com.example.paywise.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public PreferenceManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Save user ID
    public void saveUserId(int userId) {
        editor.putInt(Constants.PREF_USER_ID, userId);
        editor.apply();
    }

    // Get user ID
    public int getUserId() {
        return sharedPreferences.getInt(Constants.PREF_USER_ID, -1);
    }

    // Save login status
    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(Constants.PREF_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(Constants.PREF_IS_LOGGED_IN, false);
    }

    // Save user name
    public void saveUserName(String userName) {
        editor.putString(Constants.PREF_USER_NAME, userName);
        editor.apply();
    }

    // Get user name
    public String getUserName() {
        return sharedPreferences.getString(Constants.PREF_USER_NAME, "");
    }

    // Save profile image path
    public void saveProfileImage(String imagePath) {
        editor.putString(Constants.PREF_PROFILE_IMAGE, imagePath);
        editor.apply();
    }

    // Get profile image path
    public String getProfileImage() {
        return sharedPreferences.getString(Constants.PREF_PROFILE_IMAGE, "");
    }

    // Clear all preferences (logout)
    public void clearPreferences() {
        editor.clear();
        editor.apply();
    }
}