package com.example.paywise.managers;

import android.content.Context;
import com.example.paywise.database.TransactionDao;
import com.example.paywise.database.VaultDao;
import com.example.paywise.models.Transaction;
import com.example.paywise.models.Vault;
import com.example.paywise.utils.Constants;
import com.example.paywise.utils.DateUtils;

public class PaymentManager {
    private TransactionDao transactionDao;
    private VaultDao vaultDao;
    private Context context;

    public PaymentManager(Context context) {
        this.context = context;
        this.transactionDao = new TransactionDao(context);
        this.vaultDao = new VaultDao(context);
    }

    /**
     * Process payment
     * @param vaultId Vault ID
     * @param merchantName Merchant name
     * @param amount Payment amount
     * @param description Optional description
     * @return Transaction object with status
     */
    public Transaction processPayment(int vaultId, String merchantName, double amount, String description) {
        // Get vault details
        Vault vault = vaultDao.getVaultById(vaultId);

        if (vault == null || !vault.isActive()) {
            return createFailedTransaction(vaultId, merchantName, amount, description, "Vault not found or inactive");
        }

        // Check if vault has sufficient balance
        double remainingBalance = vault.getRemainingBalance();

        String status;
        if (remainingBalance >= amount) {
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
                description,
                currentDateTime,
                status
        );

        long transactionId = transactionDao.insertTransaction(transaction);
        transaction.setTransactionId((int) transactionId);

        return transaction;
    }

    /**
     * Validate payment before processing
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

        double remainingBalance = vault.getRemainingBalance();

        if (remainingBalance < amount) {
            return new PaymentValidationResult(false,
                    String.format("Insufficient balance. Available: ₹%.2f", remainingBalance));
        }

        return new PaymentValidationResult(true, "Payment can be processed");
    }

    /**
     * Create a failed transaction record
     */
    private Transaction createFailedTransaction(int vaultId, String merchantName,
                                                double amount, String description, String reason) {
        String currentDateTime = DateUtils.getCurrentDateTime();
        Transaction transaction = new Transaction(
                vaultId,
                merchantName,
                amount,
                Constants.TRANSACTION_TYPE_DEBIT,
                description != null ? description + " (Failed: " + reason + ")" : "Failed: " + reason,
                currentDateTime,
                Constants.TRANSACTION_STATUS_FAILED
        );

        long transactionId = transactionDao.insertTransaction(transaction);
        transaction.setTransactionId((int) transactionId);

        return transaction;
    }

    /**
     * Inner class for payment validation result
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