package com.example.paywise.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * PreferenceManager - Handles SharedPreferences for app settings
 * Updated with new preferences for ThinkPay AI
 */
public class PreferenceManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public PreferenceManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // ============================================================
    // USER PREFERENCES
    // ============================================================

    public void saveUserId(int userId) {
        editor.putInt(Constants.PREF_USER_ID, userId);
        editor.apply();
    }

    public int getUserId() {
        return sharedPreferences.getInt(Constants.PREF_USER_ID, -1);
    }

    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(Constants.PREF_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(Constants.PREF_IS_LOGGED_IN, false);
    }

    public void saveUserName(String userName) {
        editor.putString(Constants.PREF_USER_NAME, userName);
        editor.apply();
    }

    public String getUserName() {
        return sharedPreferences.getString(Constants.PREF_USER_NAME, "");
    }

    public void saveMobileNumber(String mobileNumber) {
        editor.putString(Constants.PREF_MOBILE_NUMBER, mobileNumber);
        editor.apply();
    }

    public String getMobileNumber() {
        return sharedPreferences.getString(Constants.PREF_MOBILE_NUMBER, "");
    }

    public void saveProfileImage(String imagePath) {
        editor.putString(Constants.PREF_PROFILE_IMAGE, imagePath);
        editor.apply();
    }

    public String getProfileImage() {
        return sharedPreferences.getString(Constants.PREF_PROFILE_IMAGE, "");
    }

    // ============================================================
    // BANK ACCOUNT PREFERENCES (NEW)
    // ============================================================

    public void setHasBankAccount(boolean hasBankAccount) {
        editor.putBoolean(Constants.PREF_HAS_BANK_ACCOUNT, hasBankAccount);
        editor.apply();
    }

    public boolean hasBankAccount() {
        return sharedPreferences.getBoolean(Constants.PREF_HAS_BANK_ACCOUNT, false);
    }

    // ============================================================
    // VAULT PREFERENCES (NEW)
    // ============================================================

    public void saveEmergencyVaultId(int vaultId) {
        editor.putInt(Constants.PREF_EMERGENCY_VAULT_ID, vaultId);
        editor.apply();
    }

    public int getEmergencyVaultId() {
        return sharedPreferences.getInt(Constants.PREF_EMERGENCY_VAULT_ID, -1);
    }

    public void saveDefaultInstantVaultId(int vaultId) {
        editor.putInt(Constants.PREF_DEFAULT_INSTANT_VAULT_ID, vaultId);
        editor.apply();
    }

    public int getDefaultInstantVaultId() {
        return sharedPreferences.getInt(Constants.PREF_DEFAULT_INSTANT_VAULT_ID, -1);
    }

    // ============================================================
    // SESSION MANAGEMENT (NEW)
    // ============================================================

    public void saveSessionActive(boolean isActive) {
        editor.putBoolean(Constants.PREF_SESSION_ACTIVE, isActive);
        editor.apply();
    }

    public boolean isSessionActive() {
        return sharedPreferences.getBoolean(Constants.PREF_SESSION_ACTIVE, false);
    }

    public void saveLastActivityTime(long timestamp) {
        editor.putLong(Constants.PREF_LAST_ACTIVITY_TIME, timestamp);
        editor.apply();
    }

    public long getLastActivityTime() {
        return sharedPreferences.getLong(Constants.PREF_LAST_ACTIVITY_TIME, 0);
    }

    // ============================================================
    // CLEAR ALL PREFERENCES (LOGOUT)
    // ============================================================

    public void clearPreferences() {
        editor.clear();
        editor.apply();
    }

    /**
     * Clear only session data (keep user data)
     */
    public void clearSession() {
        editor.remove(Constants.PREF_SESSION_ACTIVE);
        editor.remove(Constants.PREF_LAST_ACTIVITY_TIME);
        editor.apply();
    }
}