package com.example.paywise.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.paywise.models.Vault;
import com.example.paywise.utils.Constants;

public class VaultDao extends BaseDao {

    public VaultDao(Context context) {
        super(context);
    }

    public Vault getVaultById(int vaultId) {
        SQLiteDatabase db = getReadableDb();

        Cursor cursor = db.query(
                Constants.TABLE_VAULTS,
                null,
                "vault_id=?",
                new String[]{String.valueOf(vaultId)},
                null, null, null
        );

        Vault vault = null;
        if (cursor != null && cursor.moveToFirst()) {
            vault = new Vault();
            vault.setVaultId(cursor.getInt(cursor.getColumnIndexOrThrow("vault_id")));
            vault.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
            vault.setVaultName(cursor.getString(cursor.getColumnIndexOrThrow("vault_name")));
            vault.setBudgetLimit(cursor.getDouble(cursor.getColumnIndexOrThrow("budget_limit")));
            vault.setCurrentSpent(cursor.getDouble(cursor.getColumnIndexOrThrow("current_spent")));
            vault.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
            vault.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
        }

        if (cursor != null) cursor.close();
        return vault;
    }

    public boolean updateVaultSpending(int vaultId, double newSpent) {
        SQLiteDatabase db = getWritableDb();

        ContentValues values = new ContentValues();
        values.put("current_spent", newSpent);
        values.put("updated_at", String.valueOf(System.currentTimeMillis()));

        int rows = db.update(
                Constants.TABLE_VAULTS,
                values,
                "vault_id=?",
                new String[]{String.valueOf(vaultId)}
        );

        return rows > 0;
    }
}
