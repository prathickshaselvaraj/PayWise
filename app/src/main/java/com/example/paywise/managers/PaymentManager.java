package com.example.paywise.managers;

import android.content.Context;
import com.example.paywise.database.TransactionDao;
import com.example.paywise.database.VaultDao;
import com.example.paywise.database.VaultReassignmentDao;
import com.example.paywise.models.Transaction;
import com.example.paywise.models.Vault;
import com.example.paywise.models.VaultReassignment;
import com.example.paywise.utils.Constants;
import com.example.paywise.utils.DateUtils;

/**
 * PaymentManager - Business logic for payment operations
 *
 * Handles:
 * - Three payment methods (vault-based, instant pay, emergency)
 * - Payment validation
 * - Transaction processing
 * - Vault reassignment
 */
public class PaymentManager {

    private TransactionDao transactionDao;
    private VaultDao vaultDao;
    private VaultReassignmentDao reassignmentDao;
    private Context context;

    public PaymentManager(Context context) {
        this.context = context;
        this.transactionDao = new TransactionDao(context);
        this.vaultDao = new VaultDao(context);
        this.reassignmentDao = new VaultReassignmentDao(context);
    }

    // ============================================================
    // PAYMENT PROCESSING
    // ============================================================

    /**
     * Process vault-based payment
     * User selects vault before payment
     *
     * @param vaultId Selected vault ID
     * @param merchantName Merchant name
     * @param amount Payment amount
     * @param description Optional description
     * @return Transaction object with status
     */
    public Transaction processVaultBasedPayment(int vaultId, String merchantName,
                                                double amount, String description) {
        return processPayment(vaultId, merchantName, amount, description,
                Constants.PAYMENT_METHOD_VAULT_BASED);
    }

    /**
     * Process instant payment
     * Auto-deducts from default vault (Lifestyle), can be reassigned later
     *
     * @param userId User ID
     * @param merchantName Merchant name
     * @param amount Payment amount
     * @param description Optional description
     * @return Transaction object with status
     */
    public Transaction processInstantPayment(int userId, String merchantName,
                                             double amount, String description) {
        // Get default instant pay vault (Lifestyle)
        Vault defaultVault = vaultDao.getDefaultInstantPayVault(userId);

        if (defaultVault == null) {
            return createFailedTransaction(0, merchantName, amount, description,
                    Constants.PAYMENT_METHOD_INSTANT_PAY,
                    "No default vault found");
        }

        return processPayment(defaultVault.getVaultId(), merchantName, amount, description,
                Constants.PAYMENT_METHOD_INSTANT_PAY);
    }

    /**
     * Process emergency payment
     * Uses emergency vault (requires special PIN)
     *
     * @param vaultId Emergency vault ID
     * @param merchantName Merchant name
     * @param amount Payment amount
     * @param description Optional description
     * @return Transaction object with status
     */
    public Transaction processEmergencyPayment(int vaultId, String merchantName,
                                               double amount, String description) {
        // Verify it's actually an emergency vault
        Vault vault = vaultDao.getVaultById(vaultId);
        if (vault == null || !vault.isEmergency()) {
            return createFailedTransaction(vaultId, merchantName, amount, description,
                    Constants.PAYMENT_METHOD_EMERGENCY,
                    "Not an emergency vault");
        }

        return processPayment(vaultId, merchantName, amount, description,
                Constants.PAYMENT_METHOD_EMERGENCY);
    }

    /**
     * Core payment processing logic
     *
     * @param vaultId Vault ID
     * @param merchantName Merchant name
     * @param amount Payment amount
     * @param description Description
     * @param paymentMethod Payment method
     * @return Transaction object
     */
    private Transaction processPayment(int vaultId, String merchantName, double amount,
                                       String description, String paymentMethod) {
        // Get vault details
        Vault vault = vaultDao.getVaultById(vaultId);

        if (vault == null || !vault.isActive()) {
            return createFailedTransaction(vaultId, merchantName, amount, description,
                    paymentMethod, "Vault not found or inactive");
        }

        // Check if vault has sufficient balance
        String status;
        if (vault.canAffordPayment(amount)) {
            // Sufficient balance - process payment
            status = Constants.TRANSACTION_STATUS_SUCCESS;

            // Update vault spending
            double newSpent = vault.getCurrentSpent() + amount;
            vaultDao.updateVaultSpending(vaultId, newSpent);
        } else {
            // Insufficient balance - reject payment
            status = Constants.TRANSACTION_STATUS_FAILED;
        }

        // Create transaction record
        String currentDateTime = DateUtils.getCurrentDateTime();
        Transaction transaction = new Transaction(
                vaultId,
                merchantName,
                amount,
                Constants.TRANSACTION_TYPE_DEBIT,
                paymentMethod,
                description,
                currentDateTime,
                status
        );

        long transactionId = transactionDao.insertTransaction(transaction);
        transaction.setTransactionId((int) transactionId);

        return transaction;
    }

    /**
     * Create a failed transaction record
     */
    private Transaction createFailedTransaction(int vaultId, String merchantName,
                                                double amount, String description,
                                                String paymentMethod, String reason) {
        String currentDateTime = DateUtils.getCurrentDateTime();
        String failedDescription = description != null ?
                description + " (Failed: " + reason + ")" :
                "Failed: " + reason;

        Transaction transaction = new Transaction(
                vaultId,
                merchantName,
                amount,
                Constants.TRANSACTION_TYPE_DEBIT,
                paymentMethod,
                failedDescription,
                currentDateTime,
                Constants.TRANSACTION_STATUS_FAILED
        );

        long transactionId = transactionDao.insertTransaction(transaction);
        transaction.setTransactionId((int) transactionId);

        return transaction;
    }

    // ============================================================
    // PAYMENT VALIDATION
    // ============================================================

    /**
     * Validate payment before processing
     *
     * @param vaultId Vault ID
     * @param amount Payment amount
     * @return Validation result with message
     */
    public PaymentValidationResult validatePayment(int vaultId, double amount) {
        Vault vault = vaultDao.getVaultById(vaultId);

        if (vault == null) {
            return new PaymentValidationResult(false, "Vault not found");
        }

        if (!vault.isActive()) {
            return new PaymentValidationResult(false, "Vault is inactive");
        }

        if (amount <= 0) {
            return new PaymentValidationResult(false, "Invalid amount");
        }

        if (!vault.canAffordPayment(amount)) {
            double remaining = vault.getRemainingBalance();
            return new PaymentValidationResult(false,
                    String.format("Insufficient balance. Available: ₹%.2f", remaining));
        }

        return new PaymentValidationResult(true, "Payment can be processed");
    }

    /**
     * Check if emergency vault can process payment
     * (Additional check beyond balance)
     */
    public PaymentValidationResult validateEmergencyPayment(int vaultId, double amount) {
        Vault vault = vaultDao.getVaultById(vaultId);

        if (vault == null || !vault.isEmergency()) {
            return new PaymentValidationResult(false, "Not an emergency vault");
        }

        return validatePayment(vaultId, amount);
    }

    // ============================================================
    // VAULT REASSIGNMENT (FOR INSTANT PAY)
    // ============================================================

    /**
     * Reassign transaction to a different vault
     * Used primarily for instant pay transactions
     *
     * @param transactionId Transaction ID
     * @param newVaultId New vault ID
     * @param userId User ID (for logging)
     * @return true if successful
     */
    public boolean reassignTransactionVault(int transactionId, int newVaultId, int userId) {
        Transaction transaction = transactionDao.getTransactionById(transactionId);

        if (transaction == null || !transaction.isSuccessful()) {
            return false; // Can't reassign failed or non-existent transactions
        }

        int oldVaultId = transaction.getVaultId();

        // Don't reassign if it's the same vault
        if (oldVaultId == newVaultId) {
            return false;
        }

        String changeTimestamp = DateUtils.getCurrentDateTime();

        // Reassign transaction (updates vault spending)
        boolean success = transactionDao.reassignTransactionVault(transactionId, newVaultId, changeTimestamp);

        if (success) {
            // Log reassignment
            VaultReassignment reassignment = new VaultReassignment(
                    transactionId,
                    oldVaultId,
                    newVaultId,
                    userId,
                    changeTimestamp
            );
            reassignmentDao.insertVaultReassignment(reassignment);
        }

        return success;
    }

    /**
     * Get reassignment history for a transaction
     */
    public java.util.List<VaultReassignment> getReassignmentHistory(int transactionId) {
        return reassignmentDao.getReassignmentsByTransaction(transactionId);
    }

    // ============================================================
    // TRANSACTION RETRIEVAL
    // ============================================================

    /**
     * Get all transactions for a user
     */
    public java.util.List<Transaction> getAllTransactions(int userId) {
        return transactionDao.getAllTransactionsByUser(userId);
    }

    /**
     * Get recent transactions (limited)
     */
    public java.util.List<Transaction> getRecentTransactions(int userId, int limit) {
        return transactionDao.getRecentTransactions(userId, limit);
    }

    /**
     * Get transactions by payment method
     */
    public java.util.List<Transaction> getTransactionsByPaymentMethod(int userId, String paymentMethod) {
        return transactionDao.getTransactionsByPaymentMethod(userId, paymentMethod);
    }

    /**
     * Get transactions that were reassigned
     */
    public java.util.List<Transaction> getReassignedTransactions(int userId) {
        return transactionDao.getReassignedTransactions(userId);
    }

    /**
     * Get transaction by ID
     */
    public Transaction getTransaction(int transactionId) {
        return transactionDao.getTransactionById(transactionId);
    }

    // ============================================================
    // INNER CLASS: PAYMENT VALIDATION RESULT
    // ============================================================

    /**
     * Payment validation result wrapper
     */
    public static class PaymentValidationResult {
        private boolean isValid;
        private String message;

        public PaymentValidationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }

        public boolean isValid() {
            return isValid;
        }

        public String getMessage() {
            return message;
        }
    }
}