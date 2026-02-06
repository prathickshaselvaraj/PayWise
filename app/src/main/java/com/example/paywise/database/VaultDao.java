package com.example.paywise.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.paywise.models.Vault;
import com.example.paywise.utils.Constants;
import java.util.ArrayList;
import java.util.List;

/**
 * VaultDao - Data Access Object for vaults table
 *
 * Handles all database operations for vaults:
 * - Insert new vault
 * - Get vaults by user
 * - Update vault spending
 * - Reset monthly vaults
 * - Manage emergency vault
 * - Manage default instant pay vault
 */
public class VaultDao {

    private DatabaseHelper dbHelper;

    public VaultDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Insert a new vault
     *
     * @param vault Vault object
     * @return vault ID of inserted vault, -1 if failed
     */
    public long insertVault(Vault vault) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("user_id", vault.getUserId());
        values.put("vault_name", vault.getVaultName());
        values.put("vault_type", vault.getVaultType());
        values.put("custom_category_name", vault.getCustomCategoryName());
        values.put("vault_icon", vault.getVaultIcon());
        values.put("vault_color", vault.getVaultColor());
        values.put("monthly_limit", vault.getMonthlyLimit());
        values.put("current_spent", vault.getCurrentSpent());
        values.put("is_emergency", vault.isEmergency() ? 1 : 0);
        values.put("emergency_pin_hash", vault.getEmergencyPinHash());
        values.put("is_active", vault.isActive() ? 1 : 0);
        values.put("is_default_instant_pay", vault.isDefaultInstantPay() ? 1 : 0);
        values.put("created_at", vault.getCreatedAt());
        values.put("reset_date", vault.getResetDate());

        long vaultId = db.insert(Constants.TABLE_VAULTS, null, values);
        db.close();
        return vaultId;
    }

    /**
     * Get all vaults for a user
     *
     * @param userId User ID
     * @return List of vaults
     */
    public List<Vault> getAllVaultsByUser(int userId) {
        List<Vault> vaultList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(Constants.TABLE_VAULTS,
                null,
                "user_id = ? AND is_active = 1",
                new String[]{String.valueOf(userId)},
                null, null,
                "vault_id ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Vault vault = extractVaultFromCursor(cursor);
                vaultList.add(vault);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return vaultList;
    }

    /**
     * Get vault by ID
     *
     * @param vaultId Vault ID
     * @return Vault object or null
     */
    public Vault getVaultById(int vaultId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Vault vault = null;

        Cursor cursor = db.query(Constants.TABLE_VAULTS,
                null,
                "vault_id = ?",
                new String[]{String.valueOf(vaultId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            vault = extractVaultFromCursor(cursor);
            cursor.close();
        }

        db.close();
        return vault;
    }

    /**
     * Get emergency vault for user
     *
     * @param userId User ID
     * @return Emergency Vault object or null
     */
    public Vault getEmergencyVault(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Vault vault = null;

        Cursor cursor = db.query(Constants.TABLE_VAULTS,
                null,
                "user_id = ? AND is_emergency = 1 AND is_active = 1",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            vault = extractVaultFromCursor(cursor);
            cursor.close();
        }

        db.close();
        return vault;
    }

    /**
     * Get default instant pay vault for user
     *
     * @param userId User ID
     * @return Default instant pay vault or null
     */
    public Vault getDefaultInstantPayVault(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Vault vault = null;

        Cursor cursor = db.query(Constants.TABLE_VAULTS,
                null,
                "user_id = ? AND is_default_instant_pay = 1 AND is_active = 1",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            vault = extractVaultFromCursor(cursor);
            cursor.close();
        }

        // If no default set, get Lifestyle vault as default
        if (vault == null) {
            cursor = db.query(Constants.TABLE_VAULTS,
                    null,
                    "user_id = ? AND vault_type = ? AND is_active = 1",
                    new String[]{String.valueOf(userId), Constants.VAULT_TYPE_LIFESTYLE},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                vault = extractVaultFromCursor(cursor);
                cursor.close();
            }
        }

        db.close();
        return vault;
    }

    /**
     * Update vault
     *
     * @param vault Vault object with updated information
     * @return number of rows affected
     */
    public int updateVault(Vault vault) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("vault_name", vault.getVaultName());
        values.put("vault_type", vault.getVaultType());
        values.put("custom_category_name", vault.getCustomCategoryName());
        values.put("vault_icon", vault.getVaultIcon());
        values.put("vault_color", vault.getVaultColor());
        values.put("monthly_limit", vault.getMonthlyLimit());
        values.put("current_spent", vault.getCurrentSpent());
        values.put("is_active", vault.isActive() ? 1 : 0);
        values.put("is_default_instant_pay", vault.isDefaultInstantPay() ? 1 : 0);
        values.put("reset_date", vault.getResetDate());

        int rowsAffected = db.update(Constants.TABLE_VAULTS,
                values,
                "vault_id = ?",
                new String[]{String.valueOf(vault.getVaultId())});

        db.close();
        return rowsAffected;
    }

    /**
     * Update vault spending
     *
     * @param vaultId Vault ID
     * @param newSpentAmount New spent amount
     * @return number of rows affected
     */
    public int updateVaultSpending(int vaultId, double newSpentAmount) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("current_spent", newSpentAmount);

        int rowsAffected = db.update(Constants.TABLE_VAULTS,
                values,
                "vault_id = ?",
                new String[]{String.valueOf(vaultId)});

        db.close();
        return rowsAffected;
    }

    /**
     * Set emergency PIN for vault
     *
     * @param vaultId Vault ID
     * @param emergencyPinHash Hashed emergency PIN
     * @return number of rows affected
     */
    public int setEmergencyPin(int vaultId, String emergencyPinHash) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("emergency_pin_hash", emergencyPinHash);

        int rowsAffected = db.update(Constants.TABLE_VAULTS,
                values,
                "vault_id = ?",
                new String[]{String.valueOf(vaultId)});

        db.close();
        return rowsAffected;
    }

    /**
     * Set vault as default for instant pay
     *
     * @param vaultId Vault ID
     * @param userId User ID
     * @return number of rows affected
     */
    public int setDefaultInstantPayVault(int vaultId, int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // First, unset all default instant pay vaults for this user
        ContentValues unsetValues = new ContentValues();
        unsetValues.put("is_default_instant_pay", 0);
        db.update(Constants.TABLE_VAULTS,
                unsetValues,
                "user_id = ?",
                new String[]{String.valueOf(userId)});

        // Then set the new default
        ContentValues setValues = new ContentValues();
        setValues.put("is_default_instant_pay", 1);

        int rowsAffected = db.update(Constants.TABLE_VAULTS,
                setValues,
                "vault_id = ?",
                new String[]{String.valueOf(vaultId)});

        db.close();
        return rowsAffected;
    }

    /**
     * Delete vault (soft delete)
     *
     * @param vaultId Vault ID
     * @return number of rows affected
     */
    public int deleteVault(int vaultId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_active", 0);

        int rowsAffected = db.update(Constants.TABLE_VAULTS,
                values,
                "vault_id = ?",
                new String[]{String.valueOf(vaultId)});

        db.close();
        return rowsAffected;
    }

    /**
     * Reset all vaults for a user (monthly reset)
     *
     * @param userId User ID
     * @param newResetDate Next reset date
     * @return number of rows affected
     */
    public int resetAllVaults(int userId, String newResetDate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("current_spent", 0);
        values.put("reset_date", newResetDate);

        int rowsAffected = db.update(Constants.TABLE_VAULTS,
                values,
                "user_id = ? AND is_active = 1",
                new String[]{String.valueOf(userId)});

        db.close();
        return rowsAffected;
    }

    /**
     * Get total available balance across all vaults
     *
     * @param userId User ID
     * @return Total available balance
     */
    public double getTotalAvailableBalance(int userId) {
        double totalBalance = 0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT SUM(monthly_limit - current_spent) as total FROM " +
                        Constants.TABLE_VAULTS + " WHERE user_id = ? AND is_active = 1",
                new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            totalBalance = cursor.getDouble(0);
            cursor.close();
        }

        db.close();
        return totalBalance;
    }

    /**
     * Get total vault limit (sum of all monthly limits)
     *
     * @param userId User ID
     * @return Total vault limit
     */
    public double getTotalVaultLimit(int userId) {
        double totalLimit = 0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT SUM(monthly_limit) as total FROM " +
                        Constants.TABLE_VAULTS + " WHERE user_id = ? AND is_active = 1",
                new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            totalLimit = cursor.getDouble(0);
            cursor.close();
        }

        db.close();
        return totalLimit;
    }

    /**
     * Check if vault type already exists for user (except Custom)
     *
     * @param userId User ID
     * @param vaultType Vault type
     * @return true if exists
     */
    public boolean vaultTypeExists(int userId, String vaultType) {
        // Custom vaults can have duplicates
        if (Constants.VAULT_TYPE_CUSTOM.equals(vaultType)) {
            return false;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(Constants.TABLE_VAULTS,
                new String[]{"vault_id"},
                "user_id = ? AND vault_type = ? AND is_active = 1",
                new String[]{String.valueOf(userId), vaultType},
                null, null, null);

        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        db.close();
        return exists;
    }

    /**
     * Get vaults with low balance (below 20% of limit)
     *
     * @param userId User ID
     * @return List of vaults with low balance
     */
    public List<Vault> getLowBalanceVaults(int userId) {
        List<Vault> lowBalanceVaults = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Get vaults where remaining balance is <= 20% of limit
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + Constants.TABLE_VAULTS +
                        " WHERE user_id = ? AND is_active = 1 " +
                        " AND (monthly_limit - current_spent) <= (monthly_limit * 0.2) " +
                        " AND (monthly_limit - current_spent) > 0",
                new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Vault vault = extractVaultFromCursor(cursor);
                lowBalanceVaults.add(vault);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return lowBalanceVaults;
    }

    /**
     * Get non-emergency vaults for user (for vault selection)
     *
     * @param userId User ID
     * @return List of non-emergency vaults
     */
    public List<Vault> getNonEmergencyVaults(int userId) {
        List<Vault> vaultList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(Constants.TABLE_VAULTS,
                null,
                "user_id = ? AND is_active = 1 AND is_emergency = 0",
                new String[]{String.valueOf(userId)},
                null, null,
                "vault_id ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Vault vault = extractVaultFromCursor(cursor);
                vaultList.add(vault);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return vaultList;
    }

    /**
     * Extract Vault object from cursor
     */
    private Vault extractVaultFromCursor(Cursor cursor) {
        Vault vault = new Vault();
        vault.setVaultId(cursor.getInt(cursor.getColumnIndexOrThrow("vault_id")));
        vault.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        vault.setVaultName(cursor.getString(cursor.getColumnIndexOrThrow("vault_name")));
        vault.setVaultType(cursor.getString(cursor.getColumnIndexOrThrow("vault_type")));
        vault.setCustomCategoryName(cursor.getString(cursor.getColumnIndexOrThrow("custom_category_name")));
        vault.setVaultIcon(cursor.getString(cursor.getColumnIndexOrThrow("vault_icon")));
        vault.setVaultColor(cursor.getString(cursor.getColumnIndexOrThrow("vault_color")));
        vault.setMonthlyLimit(cursor.getDouble(cursor.getColumnIndexOrThrow("monthly_limit")));
        vault.setCurrentSpent(cursor.getDouble(cursor.getColumnIndexOrThrow("current_spent")));
        vault.setEmergency(cursor.getInt(cursor.getColumnIndexOrThrow("is_emergency")) == 1);
        vault.setEmergencyPinHash(cursor.getString(cursor.getColumnIndexOrThrow("emergency_pin_hash")));
        vault.setActive(cursor.getInt(cursor.getColumnIndexOrThrow("is_active")) == 1);
        vault.setDefaultInstantPay(cursor.getInt(cursor.getColumnIndexOrThrow("is_default_instant_pay")) == 1);
        vault.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        vault.setResetDate(cursor.getString(cursor.getColumnIndexOrThrow("reset_date")));
        return vault;
    }
}