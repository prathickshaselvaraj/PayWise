package com.example.paywise.models;

public class Vault {
    private int vaultId;
    private int userId;
    private String vaultName;
    private String vaultType; // Food, Travel, Lifestyle, Business, Emergency
    private double monthlyLimit;
    private double currentSpent;
    private String vaultColor;
    private boolean isActive;
    private String createdAt;
    private String resetDate;

    // Constructors
    public Vault() {}

    public Vault(int userId, String vaultName, String vaultType, double monthlyLimit,
                 String vaultColor, String createdAt, String resetDate) {
        this.userId = userId;
        this.vaultName = vaultName;
        this.vaultType = vaultType;
        this.monthlyLimit = monthlyLimit;
        this.currentSpent = 0.0;
        this.vaultColor = vaultColor;
        this.isActive = true;
        this.createdAt = createdAt;
        this.resetDate = resetDate;
    }

    // Getters and Setters
    public int getVaultId() { return vaultId; }
    public void setVaultId(int vaultId) { this.vaultId = vaultId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getVaultName() { return vaultName; }
    public void setVaultName(String vaultName) { this.vaultName = vaultName; }

    public String getVaultType() { return vaultType; }
    public void setVaultType(String vaultType) { this.vaultType = vaultType; }

    public double getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(double monthlyLimit) { this.monthlyLimit = monthlyLimit; }

    public double getCurrentSpent() { return currentSpent; }
    public void setCurrentSpent(double currentSpent) { this.currentSpent = currentSpent; }

    public String getVaultColor() { return vaultColor; }
    public void setVaultColor(String vaultColor) { this.vaultColor = vaultColor; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getResetDate() { return resetDate; }
    public void setResetDate(String resetDate) { this.resetDate = resetDate; }

    // Helper method to get remaining balance
    public double getRemainingBalance() {
        return monthlyLimit - currentSpent;
    }

    // Helper method to get spending percentage
    public int getSpendingPercentage() {
        if (monthlyLimit == 0) return 0;
        return (int) ((currentSpent / monthlyLimit) * 100);
    }

    // Helper method to check if limit is exceeded
    public boolean isLimitExceeded() {
        return currentSpent >= monthlyLimit;
    }
}