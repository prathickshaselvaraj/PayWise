package com.example.paywise.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.paywise.models.BankAccount;
import com.example.paywise.utils.Constants;

/**
 * BankAccountDao - Data Access Object for bank_accounts table
 *
 * Handles all database operations for bank accounts:
 * - Insert new bank account
 * - Get bank account by user
 * - Update bank account details
 * - Update simulated balance
 */
public class BankAccountDao {

    private DatabaseHelper dbHelper;

    public BankAccountDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Insert a new bank account
     *
     * @param bankAccount BankAccount object
     * @return account ID of inserted account, -1 if failed
     */
    public long insertBankAccount(BankAccount bankAccount) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("user_id", bankAccount.getUserId());
        values.put("bank_name", bankAccount.getBankName());
        values.put("account_number", bankAccount.getAccountNumber());
        values.put("account_holder_name", bankAccount.getAccountHolderName());
        values.put("ifsc_code", bankAccount.getIfscCode());
        values.put("is_primary", bankAccount.isPrimary() ? 1 : 0);
        values.put("simulated_balance", bankAccount.getSimulatedBalance());
        values.put("created_at", bankAccount.getCreatedAt());

        long accountId = db.insert(Constants.TABLE_BANK_ACCOUNTS, null, values);
        db.close();
        return accountId;
    }

    /**
     * Get primary bank account for a user
     *
     * @param userId User ID
     * @return BankAccount object or null if not found
     */
    public BankAccount getPrimaryBankAccount(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        BankAccount bankAccount = null;

        Cursor cursor = db.query(Constants.TABLE_BANK_ACCOUNTS,
                null,
                "user_id = ? AND is_primary = 1",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            bankAccount = extractBankAccountFromCursor(cursor);
            cursor.close();
        }

        db.close();
        return bankAccount;
    }

    /**
     * Check if user has a bank account
     *
     * @param userId User ID
     * @return true if bank account exists, false otherwise
     */
    public boolean hasBankAccount(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(Constants.TABLE_BANK_ACCOUNTS,
                new String[]{"account_id"},
                "user_id = ?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        db.close();
        return exists;
    }

    /**
     * Update simulated bank balance
     *
     * @param accountId Account ID
     * @param newBalance New balance amount
     * @return number of rows affected
     */
    public int updateSimulatedBalance(int accountId, double newBalance) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("simulated_balance", newBalance);

        int rowsAffected = db.update(Constants.TABLE_BANK_ACCOUNTS,
                values,
                "account_id = ?",
                new String[]{String.valueOf(accountId)});

        db.close();
        return rowsAffected;
    }

    /**
     * Update bank account details
     *
     * @param bankAccount BankAccount object with updated information
     * @return number of rows affected
     */
    public int updateBankAccount(BankAccount bankAccount) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("bank_name", bankAccount.getBankName());
        values.put("account_number", bankAccount.getAccountNumber());
        values.put("account_holder_name", bankAccount.getAccountHolderName());
        values.put("ifsc_code", bankAccount.getIfscCode());
        values.put("simulated_balance", bankAccount.getSimulatedBalance());

        int rowsAffected = db.update(Constants.TABLE_BANK_ACCOUNTS,
                values,
                "account_id = ?",
                new String[]{String.valueOf(bankAccount.getAccountId())});

        db.close();
        return rowsAffected;
    }

    /**
     * Get bank account by ID
     *
     * @param accountId Account ID
     * @return BankAccount object or null
     */
    public BankAccount getBankAccountById(int accountId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        BankAccount bankAccount = null;

        Cursor cursor = db.query(Constants.TABLE_BANK_ACCOUNTS,
                null,
                "account_id = ?",
                new String[]{String.valueOf(accountId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            bankAccount = extractBankAccountFromCursor(cursor);
            cursor.close();
        }

        db.close();
        return bankAccount;
    }

    /**
     * Extract BankAccount object from cursor
     */
    private BankAccount extractBankAccountFromCursor(Cursor cursor) {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setAccountId(cursor.getInt(cursor.getColumnIndexOrThrow("account_id")));
        bankAccount.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        bankAccount.setBankName(cursor.getString(cursor.getColumnIndexOrThrow("bank_name")));
        bankAccount.setAccountNumber(cursor.getString(cursor.getColumnIndexOrThrow("account_number")));
        bankAccount.setAccountHolderName(cursor.getString(cursor.getColumnIndexOrThrow("account_holder_name")));
        bankAccount.setIfscCode(cursor.getString(cursor.getColumnIndexOrThrow("ifsc_code")));
        bankAccount.setPrimary(cursor.getInt(cursor.getColumnIndexOrThrow("is_primary")) == 1);
        bankAccount.setSimulatedBalance(cursor.getDouble(cursor.getColumnIndexOrThrow("simulated_balance")));
        bankAccount.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return bankAccount;
    }
}