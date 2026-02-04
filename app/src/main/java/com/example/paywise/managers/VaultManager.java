package com.example.paywise.managers;

import android.content.Context;
import com.example.paywise.database.VaultDao;
import com.example.paywise.models.Vault;
import com.example.paywise.utils.DateUtils;
import java.util.List;

public class VaultManager {
    private VaultDao vaultDao;
    private Context context;

    public VaultManager(Context context) {
        this.context = context;
        this.vaultDao = new VaultDao(context);
    }

    /**
     * Create a new vault
     * @param userId User ID
     * @param vaultName Vault name
     * @param vaultType Vault type
     * @param monthlyLimit Monthly spending limit
     * @param vaultColor Vault color
     * @return Vault ID or -1 if failed
     */
    public long createVault(int userId, String vaultName, String vaultType, double monthlyLimit, String vaultColor) {
        // Check if vault type already exists
        if (vaultDao.vaultTypeExists(userId, vaultType)) {
            return -2; // Special code for duplicate vault type
        }

        String currentDate = DateUtils.getCurrentDateTime();
        String resetDate = DateUtils.getNextMonthResetDate();

        Vault vault = new Vault(userId, vaultName, vaultType, monthlyLimit, vaultColor, currentDate, resetDate);
        return vaultDao.insertVault(vault);
    }

    /**
     * Get all vaults for a user
     */
    public List<Vault> getUserVaults(int userId) {
        return vaultDao.getAllVaultsByUser(userId);
    }

    /**
     * Get vault by ID
     */
    public Vault getVault(int vaultId) {
        return vaultDao.getVaultById(vaultId);
    }

    /**
     * Update vault spending
     */
    public boolean updateVaultSpending(int vaultId, double amount) {
        Vault vault = vaultDao.getVaultById(vaultId);
        if (vault != null) {
            double newSpent = vault.getCurrentSpent() + amount;
            return vaultDao.updateVaultSpending(vaultId, newSpent) > 0;
        }
        return false;
    }

    /**
     * Check if payment can be made from vault
     */
    public boolean canMakePayment(int vaultId, double amount) {
        Vault vault = vaultDao.getVaultById(vaultId);
        if (vault != null && vault.isActive()) {
            double remainingBalance = vault.getRemainingBalance();
            return remainingBalance >= amount;
        }
        return false;
    }

    /**
     * Get total available balance
     */
    public double getTotalBalance(int userId) {
        return vaultDao.getTotalAvailableBalance(userId);
    }

    /**
     * Update vault details
     */
    public boolean updateVault(Vault vault) {
        return vaultDao.updateVault(vault) > 0;
    }

    /**
     * Delete vault
     */
    public boolean deleteVault(int vaultId) {
        return vaultDao.deleteVault(vaultId) > 0;
    }

    /**
     * Reset all vaults for monthly cycle
     */
    public boolean resetMonthlyVaults(int userId) {
        String nextResetDate = DateUtils.getNextMonthResetDate();
        return vaultDao.resetAllVaults(userId, nextResetDate) > 0;
    }

    /**
     * Check if any vault needs reset
     */
    public boolean needsReset(int userId) {
        List<Vault> vaults = vaultDao.getAllVaultsByUser(userId);
        for (Vault vault : vaults) {
            if (DateUtils.isResetDatePassed(vault.getResetDate())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if vault balance is low (below 20% of limit)
     */
    public boolean isLowBalance(int vaultId) {
        Vault vault = vaultDao.getVaultById(vaultId);
        if (vault != null) {
            double remainingBalance = vault.getRemainingBalance();
            double threshold = vault.getMonthlyLimit() * 0.2;
            return remainingBalance <= threshold && remainingBalance > 0;
        }
        return false;
    }
}