package com.example.paywise.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager - Manages user session, auto-lock, and activity tracking
 *
 * Features:
 * - Track user login session
 * - Implement auto-lock after inactivity
 * - Manage failed login attempts
 * - Handle account lockout
 */
public class SessionManager {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // ============================================================
    // SESSION MANAGEMENT
    // ============================================================

    /**
     * Mark user as logged in and start session
     */
    public void createSession(int userId, String userName, String mobileNumber) {
        editor.putInt(Constants.PREF_USER_ID, userId);
        editor.putString(Constants.PREF_USER_NAME, userName);
        editor.putString(Constants.PREF_MOBILE_NUMBER, mobileNumber);
        editor.putBoolean(Constants.PREF_IS_LOGGED_IN, true);
        editor.putBoolean(Constants.PREF_SESSION_ACTIVE, true);
        editor.putLong(Constants.PREF_LAST_ACTIVITY_TIME, System.currentTimeMillis());
        editor.apply();
    }

    /**
     * Update last activity timestamp
     */
    public void updateActivity() {
        editor.putLong(Constants.PREF_LAST_ACTIVITY_TIME, System.currentTimeMillis());
        editor.apply();
    }

    /**
     * Check if session has timed out (5 minutes inactivity)
     *
     * @return true if session expired, false if still active
     */
    public boolean isSessionExpired() {
        if (!isLoggedIn()) {
            return true;
        }

        long lastActivity = sharedPreferences.getLong(Constants.PREF_LAST_ACTIVITY_TIME, 0);
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastActivity;

        return elapsed > Constants.SESSION_TIMEOUT_MS;
    }

    /**
     * Lock session (require PIN re-entry)
     */
    public void lockSession() {
        editor.putBoolean(Constants.PREF_SESSION_ACTIVE, false);
        editor.apply();
    }

    /**
     * Unlock session after successful PIN verification
     */
    public void unlockSession() {
        editor.putBoolean(Constants.PREF_SESSION_ACTIVE, true);
        editor.putLong(Constants.PREF_LAST_ACTIVITY_TIME, System.currentTimeMillis());
        editor.apply();
    }

    /**
     * Check if session is currently active (not locked)
     */
    public boolean isSessionActive() {
        return sharedPreferences.getBoolean(Constants.PREF_SESSION_ACTIVE, false);
    }

    /**
     * Logout user and clear all session data
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }

    // ============================================================
    // USER INFO GETTERS
    // ============================================================

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(Constants.PREF_IS_LOGGED_IN, false);
    }

    public int getUserId() {
        return sharedPreferences.getInt(Constants.PREF_USER_ID, -1);
    }

    public String getUserName() {
        return sharedPreferences.getString(Constants.PREF_USER_NAME, "");
    }

    public String getMobileNumber() {
        return sharedPreferences.getString(Constants.PREF_MOBILE_NUMBER, "");
    }

    public String getProfileImage() {
        return sharedPreferences.getString(Constants.PREF_PROFILE_IMAGE, "");
    }

    public void saveProfileImage(String imagePath) {
        editor.putString(Constants.PREF_PROFILE_IMAGE, imagePath);
        editor.apply();
    }

    // ============================================================
    // BANK ACCOUNT & VAULT INFO
    // ============================================================

    public boolean hasBankAccount() {
        return sharedPreferences.getBoolean(Constants.PREF_HAS_BANK_ACCOUNT, false);
    }

    public void setHasBankAccount(boolean hasBankAccount) {
        editor.putBoolean(Constants.PREF_HAS_BANK_ACCOUNT, hasBankAccount);
        editor.apply();
    }

    public int getEmergencyVaultId() {
        return sharedPreferences.getInt(Constants.PREF_EMERGENCY_VAULT_ID, -1);
    }

    public void setEmergencyVaultId(int vaultId) {
        editor.putInt(Constants.PREF_EMERGENCY_VAULT_ID, vaultId);
        editor.apply();
    }

    public int getDefaultInstantVaultId() {
        return sharedPreferences.getInt(Constants.PREF_DEFAULT_INSTANT_VAULT_ID, -1);
    }

    public void setDefaultInstantVaultId(int vaultId) {
        editor.putInt(Constants.PREF_DEFAULT_INSTANT_VAULT_ID, vaultId);
        editor.apply();
    }

    // ============================================================
    // LOCKOUT MANAGEMENT
    // ============================================================

    /**
     * Get time remaining for account lockout (in seconds)
     *
     * @return seconds remaining, or 0 if not locked
     */
    public long getLockoutTimeRemaining() {
        long lockoutUntil = sharedPreferences.getLong("lockout_until", 0);
        long currentTime = System.currentTimeMillis();

        if (currentTime < lockoutUntil) {
            return (lockoutUntil - currentTime) / 1000; // Convert to seconds
        }
        return 0;
    }

    /**
     * Set account lockout for specified duration
     */
    public void setLockout(long durationMs) {
        long lockoutUntil = System.currentTimeMillis() + durationMs;
        editor.putLong("lockout_until", lockoutUntil);
        editor.apply();
    }

    /**
     * Clear lockout
     */
    public void clearLockout() {
        editor.remove("lockout_until");
        editor.apply();
    }

    /**
     * Check if account is currently locked
     */
    public boolean isAccountLocked() {
        return getLockoutTimeRemaining() > 0;
    }
}