package com.example.paywise.utils;

import android.content.Context;

/**
 * SessionManager - Manages user session and auto-lock functionality
 * Tracks user activity and enforces session timeout
 */
public class SessionManager {
    private PreferenceManager preferenceManager;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        this.preferenceManager = new PreferenceManager(context);
    }

    /**
     * Start a new session
     */
    public void startSession() {
        preferenceManager.saveSessionActive(true);
        updateLastActivityTime();
    }

    /**
     * End current session
     */
    public void endSession() {
        preferenceManager.saveSessionActive(false);
    }

    /**
     * Update last activity timestamp
     */
    public void updateLastActivityTime() {
        long currentTime = System.currentTimeMillis();
        preferenceManager.saveLastActivityTime(currentTime);
    }

    /**
     * Check if session is active
     * @return true if session is active and not timed out
     */
    public boolean isSessionActive() {
        if (!preferenceManager.isSessionActive()) {
            return false;
        }

        long lastActivityTime = preferenceManager.getLastActivityTime();
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastActivityTime;

        // Check if session has timed out (5 minutes of inactivity)
        if (elapsedTime > Constants.SESSION_TIMEOUT_MS) {
            endSession();
            return false;
        }

        return true;
    }

    /**
     * Get remaining session time in milliseconds
     */
    public long getRemainingSessionTime() {
        if (!preferenceManager.isSessionActive()) {
            return 0;
        }

        long lastActivityTime = preferenceManager.getLastActivityTime();
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastActivityTime;
        long remainingTime = Constants.SESSION_TIMEOUT_MS - elapsedTime;

        return remainingTime > 0 ? remainingTime : 0;
    }

    /**
     * Check if session requires re-authentication
     */
    public boolean requiresReAuthentication() {
        return !isSessionActive();
    }
}