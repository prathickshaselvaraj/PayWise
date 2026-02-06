package com.example.paywise.models;

/**
 * Vault Model
 * Represents a budget vault with spending limits
 */
public class Vault {
    private int vaultId;
    private int userId;
    private String vaultName;
    private String vaultType;  // Food, Travel, Lifestyle, Business, Emergency, Custom
    private String customCategoryName;  // For custom vaults
    private String vaultIcon;  // Icon identifier or emoji
    private String vaultColor;
    private double monthlyLimit;
    private double currentSpent;
    private boolean isEmergency;
    private String emergencyPinHash;  // Special PIN for emergency vault
    private boolean isActive;
    private boolean isDefaultInstantPay;  // Default vault for instant pay
    private String createdAt;
    private String resetDate;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public Vault() {}

    public Vault(int userId, String vaultName, String vaultType, String vaultIcon,
                 String vaultColor, double monthlyLimit, String createdAt, String resetDate) {
        this.userId = userId;
        this.vaultName = vaultName;
        this.vaultType = vaultType;
        this.vaultIcon = vaultIcon;
        this.vaultColor = vaultColor;
        this.monthlyLimit = monthlyLimit;
        this.currentSpent = 0.0;
        this.isEmergency = false;
        this.isActive = true;
        this.isDefaultInstantPay = false;
        this.createdAt = createdAt;
        this.resetDate = resetDate;
    }

    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================

    public int getVaultId() {
        return vaultId;
    }

    public void setVaultId(int vaultId) {
        this.vaultId = vaultId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getVaultName() {
        return vaultName;
    }

    public void setVaultName(String vaultName) {
        this.vaultName = vaultName;
    }

    public String getVaultType() {
        return vaultType;
    }

    public void setVaultType(String vaultType) {
        this.vaultType = vaultType;
    }

    public String getCustomCategoryName() {
        return customCategoryName;
    }

    public void setCustomCategoryName(String customCategoryName) {
        this.customCategoryName = customCategoryName;
    }

    public String getVaultIcon() {
        return vaultIcon;
    }

    public void setVaultIcon(String vaultIcon) {
        this.vaultIcon = vaultIcon;
    }

    public String getVaultColor() {
        return vaultColor;
    }

    public void setVaultColor(String vaultColor) {
        this.vaultColor = vaultColor;
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(double monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public double getCurrentSpent() {
        return currentSpent;
    }

    public void setCurrentSpent(double currentSpent) {
        this.currentSpent = currentSpent;
    }

    public boolean isEmergency() {
        return isEmergency;
    }

    public void setEmergency(boolean emergency) {
        isEmergency = emergency;
    }

    public String getEmergencyPinHash() {
        return emergencyPinHash;
    }

    public void setEmergencyPinHash(String emergencyPinHash) {
        this.emergencyPinHash = emergencyPinHash;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isDefaultInstantPay() {
        return isDefaultInstantPay;
    }

    public void setDefaultInstantPay(boolean defaultInstantPay) {
        isDefaultInstantPay = defaultInstantPay;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getResetDate() {
        return resetDate;
    }

    public void setResetDate(String resetDate) {
        this.resetDate = resetDate;
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Get remaining balance in vault
     */
    public double getRemainingBalance() {
        return monthlyLimit - currentSpent;
    }

    /**
     * Get spending percentage (0-100)
     */
    public int getSpendingPercentage() {
        if (monthlyLimit == 0) return 0;
        return (int) ((currentSpent / monthlyLimit) * 100);
    }

    /**
     * Check if limit is exceeded
     */
    public boolean isLimitExceeded() {
        return currentSpent >= monthlyLimit;
    }

    /**
     * Check if balance is low (below 20% of limit)
     */
    public boolean isLowBalance() {
        double threshold = monthlyLimit * 0.2;
        return getRemainingBalance() <= threshold && getRemainingBalance() > 0;
    }

    /**
     * Get display name (custom category name if custom vault)
     */
    public String getDisplayName() {
        if (vaultType.equals("Custom") && customCategoryName != null) {
            return customCategoryName;
        }
        return vaultName;
    }

    /**
     * Check if payment amount can be made from this vault
     */
    public boolean canAffordPayment(double amount) {
        return getRemainingBalance() >= amount;
    }
}