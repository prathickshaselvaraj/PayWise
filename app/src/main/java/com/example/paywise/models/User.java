package com.example.paywise.models;

/**
 * User Model
 * Represents a user account with authentication details
 */
public class User {
    private int userId;
    private String fullName;
    private String mobileNumber;
    private String email;
    private String appPinHash;  // Hashed PIN
    private String profileImagePath;
    private boolean isLocked;
    private int failedAttempts;
    private String lockoutUntil;
    private String createdAt;
    private String updatedAt;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public User() {}

    public User(String fullName, String mobileNumber, String email, String appPinHash,
                String profileImagePath, String createdAt, String updatedAt) {
        this.fullName = fullName;
        this.mobileNumber = mobileNumber;
        this.email = email;
        this.appPinHash = appPinHash;
        this.profileImagePath = profileImagePath;
        this.isLocked = false;
        this.failedAttempts = 0;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAppPinHash() {
        return appPinHash;
    }

    public void setAppPinHash(String appPinHash) {
        this.appPinHash = appPinHash;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public String getLockoutUntil() {
        return lockoutUntil;
    }

    public void setLockoutUntil(String lockoutUntil) {
        this.lockoutUntil = lockoutUntil;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Get formatted mobile number (e.g., +91 98765 43210)
     */
    public String getFormattedMobileNumber() {
        if (mobileNumber != null && mobileNumber.length() == 10) {
            return "+91 " + mobileNumber.substring(0, 5) + " " + mobileNumber.substring(5);
        }
        return mobileNumber;
    }

    /**
     * Get masked mobile number (e.g., +91 98765 ***10)
     */
    public String getMaskedMobileNumber() {
        if (mobileNumber != null && mobileNumber.length() == 10) {
            return "+91 " + mobileNumber.substring(0, 5) + " ***" + mobileNumber.substring(8);
        }
        return mobileNumber;
    }
}