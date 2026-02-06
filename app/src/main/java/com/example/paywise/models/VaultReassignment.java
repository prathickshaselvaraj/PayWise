package com.example.paywise.models;

/**
 * VaultReassignment Model
 * Tracks when a transaction is moved from one vault to another
 */
public class VaultReassignment {
    private int reassignmentId;
    private int transactionId;
    private int fromVaultId;
    private int toVaultId;
    private int changedByUserId;
    private String changedAt;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public VaultReassignment() {}

    public VaultReassignment(int transactionId, int fromVaultId, int toVaultId,
                             int changedByUserId, String changedAt) {
        this.transactionId = transactionId;
        this.fromVaultId = fromVaultId;
        this.toVaultId = toVaultId;
        this.changedByUserId = changedByUserId;
        this.changedAt = changedAt;
    }

    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================

    public int getReassignmentId() {
        return reassignmentId;
    }

    public void setReassignmentId(int reassignmentId) {
        this.reassignmentId = reassignmentId;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getFromVaultId() {
        return fromVaultId;
    }

    public void setFromVaultId(int fromVaultId) {
        this.fromVaultId = fromVaultId;
    }

    public int getToVaultId() {
        return toVaultId;
    }

    public void setToVaultId(int toVaultId) {
        this.toVaultId = toVaultId;
    }

    public int getChangedByUserId() {
        return changedByUserId;
    }

    public void setChangedByUserId(int changedByUserId) {
        this.changedByUserId = changedByUserId;
    }

    public String getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(String changedAt) {
        this.changedAt = changedAt;
    }
}