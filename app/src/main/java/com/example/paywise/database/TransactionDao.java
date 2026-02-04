package com.example.paywise.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.paywise.models.Transaction;
import com.example.paywise.utils.Constants;
import java.util.ArrayList;
import java.util.List;

public class TransactionDao {
    private DatabaseHelper dbHelper;

    public TransactionDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Insert a new transaction
     * @param transaction Transaction object
     * @return transaction ID of inserted transaction, -1 if failed
     */
    public long insertTransaction(Transaction transaction) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("vault_id", transaction.getVaultId());
        values.put("merchant_name", transaction.getMerchantName());
        values.put("amount", transaction.getAmount());
        values.put("transaction_type", transaction.getTransactionType());
        values.put("description", transaction.getDescription());
        values.put("transaction_date", transaction.getTransactionDate());
        values.put("status", transaction.getStatus());

        long transactionId = db.insert(Constants.TABLE_TRANSACTIONS, null, values);
        db.close();
        return transactionId;
    }

    /**
     * Get all transactions for a vault
     * @param vaultId Vault ID
     * @return List of transactions
     */
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

    /**
     * Get all transactions for user (across all vaults)
     * @param userId User ID
     * @return List of transactions
     */
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

    /**
     * Get recent transactions (limit)
     * @param userId User ID
     * @param limit Number of transactions to fetch
     * @return List of recent transactions
     */
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

    /**
     * Get transaction by ID
     * @param transactionId Transaction ID
     * @return Transaction object or null
     */
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

    /**
     * Update transaction status
     * @param transactionId Transaction ID
     * @param status New status
     * @return number of rows affected
     */
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

    /**
     * Get total spent for a vault
     * @param vaultId Vault ID
     * @return Total spent amount
     */
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

    /**
     * Extract Transaction object from cursor
     */
    private Transaction extractTransactionFromCursor(Cursor cursor) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(cursor.getInt(cursor.getColumnIndexOrThrow("transaction_id")));
        transaction.setVaultId(cursor.getInt(cursor.getColumnIndexOrThrow("vault_id")));
        transaction.setMerchantName(cursor.getString(cursor.getColumnIndexOrThrow("merchant_name")));
        transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")));
        transaction.setTransactionType(cursor.getString(cursor.getColumnIndexOrThrow("transaction_type")));
        transaction.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        transaction.setTransactionDate(cursor.getString(cursor.getColumnIndexOrThrow("transaction_date")));
        transaction.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        return transaction;
    }

    /**
     * Insert service log
     * @param serviceName Name of the service
     * @param actionType Type of action performed
     * @param message Log message
     * @param timestamp Timestamp
     * @return log ID or -1 if failed
     */
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