package com.example.paywise.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.paywise.models.VaultReassignment;
import com.example.paywise.utils.Constants;
import java.util.ArrayList;
import java.util.List;

/**
 * VaultReassignmentDao - Data Access Object for vault_reassignments table
 *
 * Tracks history of vault changes for transactions
 */
public class VaultReassignmentDao {

    private DatabaseHelper dbHelper;

    public VaultReassignmentDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Insert a new vault reassignment record
     *
     * @param reassignment VaultReassignment object
     * @return reassignment ID or -1 if failed
     */
    public long insertVaultReassignment(VaultReassignment reassignment) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("transaction_id", reassignment.getTransactionId());
        values.put("from_vault_id", reassignment.getFromVaultId());
        values.put("to_vault_id", reassignment.getToVaultId());
        values.put("changed_by_user_id", reassignment.getChangedByUserId());
        values.put("changed_at", reassignment.getChangedAt());

        long reassignmentId = db.insert(Constants.TABLE_VAULT_REASSIGNMENTS, null, values);
        db.close();
        return reassignmentId;
    }

    /**
     * Get reassignment history for a transaction
     *
     * @param transactionId Transaction ID
     * @return List of reassignments
     */
    public List<VaultReassignment> getReassignmentsByTransaction(int transactionId) {
        List<VaultReassignment> reassignments = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(Constants.TABLE_VAULT_REASSIGNMENTS,
                null,
                "transaction_id = ?",
                new String[]{String.valueOf(transactionId)},
                null, null,
                "changed_at DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                VaultReassignment reassignment = extractReassignmentFromCursor(cursor);
                reassignments.add(reassignment);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return reassignments;
    }

    /**
     * Get all reassignments by user
     *
     * @param userId User ID
     * @return List of reassignments
     */
    public List<VaultReassignment> getReassignmentsByUser(int userId) {
        List<VaultReassignment> reassignments = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(Constants.TABLE_VAULT_REASSIGNMENTS,
                null,
                "changed_by_user_id = ?",
                new String[]{String.valueOf(userId)},
                null, null,
                "changed_at DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                VaultReassignment reassignment = extractReassignmentFromCursor(cursor);
                reassignments.add(reassignment);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return reassignments;
    }

    /**
     * Extract VaultReassignment object from cursor
     */
    private VaultReassignment extractReassignmentFromCursor(Cursor cursor) {
        VaultReassignment reassignment = new VaultReassignment();
        reassignment.setReassignmentId(cursor.getInt(cursor.getColumnIndexOrThrow("reassignment_id")));
        reassignment.setTransactionId(cursor.getInt(cursor.getColumnIndexOrThrow("transaction_id")));
        reassignment.setFromVaultId(cursor.getInt(cursor.getColumnIndexOrThrow("from_vault_id")));
        reassignment.setToVaultId(cursor.getInt(cursor.getColumnIndexOrThrow("to_vault_id")));
        reassignment.setChangedByUserId(cursor.getInt(cursor.getColumnIndexOrThrow("changed_by_user_id")));
        reassignment.setChangedAt(cursor.getString(cursor.getColumnIndexOrThrow("changed_at")));
        return reassignment;
    }
}