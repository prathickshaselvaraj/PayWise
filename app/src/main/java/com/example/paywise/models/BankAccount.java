package com.example.paywise.models;

/**
 * BankAccount Model
 * Represents a linked bank account for the user
 */
public class BankAccount {
    private int accountId;
    private int userId;
    private String bankName;
    private String accountNumber;
    private String accountHolderName;
    private String ifscCode;
    private boolean isPrimary;
    private double simulatedBalance;  // Simulated bank balance
    private String createdAt;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public BankAccount() {}

    public BankAccount(int userId, String bankName, String accountNumber,
                       String accountHolderName, String ifscCode, String createdAt) {
        this.userId = userId;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.ifscCode = ifscCode;
        this.isPrimary = true;
        this.simulatedBalance = 50000.0;  // Default simulated balance
        this.createdAt = createdAt;
    }

    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public double getSimulatedBalance() {
        return simulatedBalance;
    }

    public void setSimulatedBalance(double simulatedBalance) {
        this.simulatedBalance = simulatedBalance;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Get masked account number (e.g., ****3456)
     */
    public String getMaskedAccountNumber() {
        if (accountNumber != null && accountNumber.length() >= 4) {
            return "****" + accountNumber.substring(accountNumber.length() - 4);
        }
        return "****";
    }

    /**
     * Get formatted balance with currency symbol
     */
    public String getFormattedBalance() {
        return String.format("₹ %.2f", simulatedBalance);
    }
}