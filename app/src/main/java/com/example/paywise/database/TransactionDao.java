package com.example.paywise.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.paywise.models.Transaction;
import com.example.paywise.models.Vault;
import com.example.paywise.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * TransactionDao - Data Access Object for transactions table
 *
 * Handles all database operations for transactions:
 * - Insert new transaction
 * - Get transactions by vault/user
 * - Update transaction status
 * - Reassign transaction to different vault
 * - Track vault changes
 */
public class TransactionDao {

    private final DatabaseHelper dbHelper;
    private final Context context;

    public TransactionDao(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = new DatabaseHelper(this.context);
    }

    public long insertTransaction(Transaction transaction) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("vault_id", transaction.getVaultId());
        values.put("original_vault_id", transaction.getOriginalVaultId());
        values.put("merchant_name", transaction.getMerchantName());
        values.put("amount", transaction.getAmount());
        values.put("transaction_type", transaction.getTransactionType());
        values.put("payment_method", transaction.getPaymentMethod());
        values.put("description", transaction.getDescription());
        values.put("transaction_date", transaction.getTransactionDate());
        values.put("status", transaction.getStatus());
        values.put("vault_changed", transaction.isVaultChanged() ? 1 : 0);
        values.put("vault_changed_at", transaction.getVaultChangedAt());

        long transactionId = db.insert(Constants.TABLE_TRANSACTIONS, null, values);
        db.close();
        return transactionId;
    }

    public List<Transaction> getTransactionsByVault(int vaultId) {
        List<Transaction> transactionList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(Constants.TABLE_TRANSACTIONS,
                null,
                "vault_id = ?",
                new String[]{String.valueOf(vaultId)},
                null, null,
                "transaction_date DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Transaction transaction = extractTransactionFromCursor(cursor);
                transactionList.add(transaction);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return transactionList;
    }

    public List<Transaction> getAllTransactionsByUser(int userId) {
        List<Transaction> transactionList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT t.* FROM " + Constants.TABLE_TRANSACTIONS + " t " +
                "INNER JOIN " + Constants.TABLE_VAULTS + " v ON t.vault_id = v.vault_id " +
                "WHERE v.user_id = ? " +
                "ORDER BY t.transaction_date DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Transaction transaction = extractTransactionFromCursor(cursor);
                transactionList.add(transaction);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return transactionList;
    }

    public List<Transaction> getRecentTransactions(int userId, int limit) {
        List<Transaction> transactionList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT t.* FROM " + Constants.TABLE_TRANSACTIONS + " t " +
                "INNER JOIN " + Constants.TABLE_VAULTS + " v ON t.vault_id = v.vault_id " +
                "WHERE v.user_id = ? " +
                "ORDER BY t.transaction_date DESC LIMIT ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(limit)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Transaction transaction = extractTransactionFromCursor(cursor);
                transactionList.add(transaction);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return transactionList;
    }

    public Transaction getTransactionById(int transactionId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Transaction transaction = null;

        Cursor cursor = db.query(Constants.TABLE_TRANSACTIONS,
                null,
                "transaction_id = ?",
                new String[]{String.valueOf(transactionId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            transaction = extractTransactionFromCursor(cursor);
            cursor.close();
        }

        db.close();
        return transaction;
    }

    public int updateTransactionStatus(int transactionId, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);

        int rowsAffected = db.update(Constants.TABLE_TRANSACTIONS,
                values,
                "transaction_id = ?",
                new String[]{String.valueOf(transactionId)});

        db.close();
        return rowsAffected;
    }

    public boolean reassignTransactionVault(int transactionId, int newVaultId, String changeTimestamp) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            // Get transaction details
            Transaction transaction = getTransactionById(transactionId);
            if (transaction == null || !transaction.isSuccessful()) {
                return false;
            }

            int oldVaultId = transaction.getVaultId();
            double amount = transaction.getAmount();

            // ✅ Use stored context (SQLiteDatabase has no getContext())
            VaultDao vaultDao = new VaultDao(context);

            // Restore old vault spending
            Vault oldVault = vaultDao.getVaultById(oldVaultId);
            if (oldVault != null) {
                double newOldSpent = oldVault.getCurrentSpent() - amount;
                vaultDao.updateVaultSpending(oldVaultId, Math.max(0, newOldSpent));
            }

            // Add to new vault spending
            Vault newVault = vaultDao.getVaultById(newVaultId);
            if (newVault != null) {
                double newVaultSpent = newVault.getCurrentSpent() + amount;
                vaultDao.updateVaultSpending(newVaultId, newVaultSpent);
            }

            // Update transaction row
            ContentValues values = new ContentValues();
            values.put("vault_id", newVaultId);

            if (transaction.getOriginalVaultId() == 0) {
                values.put("original_vault_id", oldVaultId);
            }

            values.put("vault_changed", 1);
            values.put("vault_changed_at", changeTimestamp);

            int rowsAffected = db.update(Constants.TABLE_TRANSACTIONS,
                    values,
                    "transaction_id = ?",
                    new String[]{String.valueOf(transactionId)});

            if (rowsAffected > 0) {
                db.setTransactionSuccessful();
                return true;
            }
            return false;

        } catch (Exception e) {
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public double getTotalSpentByVault(int vaultId) {
        double totalSpent = 0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT SUM(amount) as total FROM " + Constants.TABLE_TRANSACTIONS +
                        " WHERE vault_id = ? AND transaction_type = ? AND status = ?",
                new String[]{String.valueOf(vaultId), Constants.TRANSACTION_TYPE_DEBIT, Constants.TRANSACTION_STATUS_SUCCESS});

        if (cursor != null && cursor.moveToFirst()) {
            totalSpent = cursor.getDouble(0);
            cursor.close();
        }

        db.close();
        return totalSpent;
    }

    public List<Transaction> getTransactionsByPaymentMethod(int userId, String paymentMethod) {
        List<Transaction> transactionList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT t.* FROM " + Constants.TABLE_TRANSACTIONS + " t " +
                "INNER JOIN " + Constants.TABLE_VAULTS + " v ON t.vault_id = v.vault_id " +
                "WHERE v.user_id = ? AND t.payment_method = ? " +
                "ORDER BY t.transaction_date DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), paymentMethod});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Transaction transaction = extractTransactionFromCursor(cursor);
                transactionList.add(transaction);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return transactionList;
    }

    public List<Transaction> getReassignedTransactions(int userId) {
        List<Transaction> transactionList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT t.* FROM " + Constants.TABLE_TRANSACTIONS + " t " +
                "INNER JOIN " + Constants.TABLE_VAULTS + " v ON t.vault_id = v.vault_id " +
                "WHERE v.user_id = ? AND t.vault_changed = 1 " +
                "ORDER BY t.vault_changed_at DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Transaction transaction = extractTransactionFromCursor(cursor);
                transactionList.add(transaction);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return transactionList;
    }

    private Transaction extractTransactionFromCursor(Cursor cursor) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(cursor.getInt(cursor.getColumnIndexOrThrow("transaction_id")));
        transaction.setVaultId(cursor.getInt(cursor.getColumnIndexOrThrow("vault_id")));

        int originalVaultIdIndex = cursor.getColumnIndex("original_vault_id");
        if (originalVaultIdIndex != -1 && !cursor.isNull(originalVaultIdIndex)) {
            transaction.setOriginalVaultId(cursor.getInt(originalVaultIdIndex));
        }

        transaction.setMerchantName(cursor.getString(cursor.getColumnIndexOrThrow("merchant_name")));
        transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")));
        transaction.setTransactionType(cursor.getString(cursor.getColumnIndexOrThrow("transaction_type")));
        transaction.setPaymentMethod(cursor.getString(cursor.getColumnIndexOrThrow("payment_method")));
        transaction.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        transaction.setTransactionDate(cursor.getString(cursor.getColumnIndexOrThrow("transaction_date")));
        transaction.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        transaction.setVaultChanged(cursor.getInt(cursor.getColumnIndexOrThrow("vault_changed")) == 1);
        transaction.setVaultChangedAt(cursor.getString(cursor.getColumnIndexOrThrow("vault_changed_at")));
        return transaction;
    }

    public long insertServiceLog(String serviceName, String actionType, String message, String timestamp) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("service_name", serviceName);
        values.put("action_type", actionType);
        values.put("message", message);
        values.put("timestamp", timestamp);

        long logId = db.insert(Constants.TABLE_SERVICE_LOGS, null, values);
        db.close();
        return logId;
    }
}
