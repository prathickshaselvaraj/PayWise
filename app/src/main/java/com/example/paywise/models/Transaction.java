package com.example.thinkpayai.models;

/**
 * Transaction Model
 * Represents a payment transaction with vault reassignment support
 */
public class Transaction {
    private int transactionId;
    private int vaultId;
    private int originalVaultId;  // Track original vault if changed
    private String merchantName;
    private double amount;
    private String transactionType;  // 'debit' or 'credit'
    private String paymentMethod;  // 'vault_based', 'instant_pay', 'emergency'
    private String description;
    private String transactionDate;
    private String status;  // 'success', 'failed', 'pending'
    private boolean vaultChanged;  // Was vault reassigned?
    private String vaultChangedAt;  // Timestamp of vault change

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public Transaction() {}

    public Transaction(int vaultId, String merchantName, double amount, String transactionType,
                       String paymentMethod, String description, String transactionDate, String status) {
        this.vaultId = vaultId;
        this.originalVaultId = vaultId;  // Initially same as current vault
        this.merchantName = merchantName;
        this.amount = amount;
        this.transactionType = transactionType;
        this.paymentMethod = paymentMethod;
        this.description = description;
        this.transactionDate = transactionDate;
        this.status = status;
        this.vaultChanged = false;
    }

    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getVaultId() { return vaultId; }
    public void setVaultId(int vaultId) { this.vaultId = vaultId; }

    public int getOriginalVaultId() { return originalVaultId; }
    public void setOriginalVaultId(int originalVaultId) { this.originalVaultId = originalVaultId; }

    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTransactionDate() { return transactionDate; }
    public void setTransactionDate(String transactionDate) { this.transactionDate = transactionDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isVaultChanged() { return vaultChanged; }
    public void setVaultChanged(boolean vaultChanged) { this.vaultChanged = vaultChanged; }

    public String getVaultChangedAt() { return vaultChangedAt; }
    public void setVaultChangedAt(String vaultChangedAt) { this.vaultChangedAt = vaultChangedAt; }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Get formatted amount with sign
     */
    public String getFormattedAmount() {
        String sign = "debit".equals(transactionType) ? "-" : "+";
        return String.format("%s ₹%.2f", sign, amount);
    }

    /**
     * Check if transaction was successful
     */
    public boolean isSuccessful() {
        return "success".equals(status);
    }

    /**
     * Check if transaction failed
     */
    public boolean isFailed() {
        return "failed".equals(status);
    }

    /**
     * Check if vault was reassigned
     */
    public boolean wasVaultReassigned() {
        return vaultChanged && originalVaultId != vaultId;
    }

    /**
     * Get payment method display name
     */
    public String getPaymentMethodDisplay() {
        switch (paymentMethod) {
            case "vault_based":
                return "Vault Payment";
            case "instant_pay":
                return "Instant Pay";
            case "emergency":
                return "Emergency Payment";
            default:
                return "Payment";
        }
    }
}