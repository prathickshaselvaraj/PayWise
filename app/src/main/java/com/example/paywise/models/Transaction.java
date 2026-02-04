package com.example.paywise.models;

public class Transaction {
    private int transactionId;
    private int vaultId;
    private String merchantName;
    private double amount;
    private String transactionType; // debit or credit
    private String description;
    private String transactionDate;
    private String status; // success, failed, pending

    // Constructors
    public Transaction() {}

    public Transaction(int vaultId, String merchantName, double amount, String transactionType,
                       String description, String transactionDate, String status) {
        this.vaultId = vaultId;
        this.merchantName = merchantName;
        this.amount = amount;
        this.transactionType = transactionType;
        this.description = description;
        this.transactionDate = transactionDate;
        this.status = status;
    }

    // Getters and Setters
    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getVaultId() { return vaultId; }
    public void setVaultId(int vaultId) { this.vaultId = vaultId; }

    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTransactionDate() { return transactionDate; }
    public void setTransactionDate(String transactionDate) { this.transactionDate = transactionDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}