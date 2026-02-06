package com.example.paywise.managers;

import android.content.Context;
import com.example.paywise.database.VaultDao;
import com.example.paywise.models.Vault;
import com.example.paywise.utils.Constants;
import com.example.paywise.utils.DateUtils;
import com.example.paywise.utils.PinManager;
import java.util.List;

/**
 * VaultManager - Business logic for vault operations
 *
 * Handles:
 * - Vault creation (regular and emergency)
 * - Vault spending updates
 * - Monthly vault reset
 * - Low balance detection
 * - Emergency vault PIN management
 */
public class VaultManager {

    private VaultDao vaultDao;
    private Context context;

    public VaultManager(Context context) {
        this.context = context;
        this.vaultDao = new VaultDao(context);
    }

    // ============================================================
    // VAULT CREATION
    // ============================================================

    /**
     * Create a new vault
     *
     * @param userId User ID
     * @param vaultName Vault name
     * @param vaultType Vault type
     * @param vaultIcon Icon identifier or emoji
     * @param monthlyLimit Monthly spending limit
     * @param vaultColor Vault color
     * @return Vault ID or error code
     *         -1: Failed to insert
     *         -2: Duplicate vault type
     */
    public long createVault(int userId, String vaultName, String vaultType, String vaultIcon,
                            double monthlyLimit, String vaultColor) {
        // Check if vault type already exists (except Custom)
        if (!Constants.VAULT_TYPE_CUSTOM.equals(vaultType) &&
                vaultDao.vaultTypeExists(userId, vaultType)) {
            return -2; // Duplicate vault type
        }

        String currentDate = DateUtils.getCurrentDateTime();
        String resetDate = DateUtils.getNextMonthResetDate();

        Vault vault = new Vault(userId, vaultName, vaultType, vaultIcon, vaultColor,
                monthlyLimit, currentDate, resetDate);

        return vaultDao.insertVault(vault);
    }

    /**
     * Create custom vault with custom category name
     *
     * @param userId User ID
     * @param vaultName Vault name
     * @param customCategoryName Custom category (e.g., "Healthcare", "Education")
     * @param vaultIcon Icon identifier or emoji
     * @param monthlyLimit Monthly spending limit
     * @param vaultColor Vault color
     * @return Vault ID or -1 if failed
     */
    public long createCustomVault(int userId, String vaultName, String customCategoryName,
                                  String vaultIcon, double monthlyLimit, String vaultColor) {
        String currentDate = DateUtils.getCurrentDateTime();
        String resetDate = DateUtils.getNextMonthResetDate();

        Vault vault = new Vault(userId, vaultName, Constants.VAULT_TYPE_CUSTOM, vaultIcon,
                vaultColor, monthlyLimit, currentDate, resetDate);
        vault.setCustomCategoryName(customCategoryName);

        return vaultDao.insertVault(vault);
    }

    /**
     * Create emergency vault (auto-created during setup)
     *
     * @param userId User ID
     * @param emergencyPin 6-digit emergency PIN (different from app PIN)
     * @return Vault ID or -1 if failed
     */
    public long createEmergencyVault(int userId, String emergencyPin) {
        String currentDate = DateUtils.getCurrentDateTime();
        String resetDate = DateUtils.getNextMonthResetDate();

        Vault vault = new Vault(userId, "Emergency Vault", Constants.VAULT_TYPE_EMERGENCY,
                Constants.ICON_EMERGENCY, Constants.COLOR_EMERGENCY,
                Constants.DEFAULT_EMERGENCY_VAULT_LIMIT, currentDate, resetDate);

        // Set emergency vault specific properties
        vault.setEmergency(true);
        vault.setEmergencyPinHash(PinManager.hashPin(emergencyPin));

        return vaultDao.insertVault(vault);
    }

    /**
     * Set default vault for instant pay (usually Lifestyle)
     *
     * @param vaultId Vault ID to set as default
     * @param userId User ID
     * @return true if successful
     */
    public boolean setDefaultInstantPayVault(int vaultId, int userId) {
        return vaultDao.setDefaultInstantPayVault(vaultId, userId) > 0;
    }

    // ============================================================
    // VAULT RETRIEVAL
    // ============================================================

    /**
     * Get all vaults for a user
     */
    public List<Vault> getUserVaults(int userId) {
        return vaultDao.getAllVaultsByUser(userId);
    }

    /**
     * Get non-emergency vaults (for payment selection)
     */
    public List<Vault> getNonEmergencyVaults(int userId) {
        return vaultDao.getNonEmergencyVaults(userId);
    }

    /**
     * Get vault by ID
     */
    public Vault getVault(int vaultId) {
        return vaultDao.getVaultById(vaultId);
    }

    /**
     * Get emergency vault for user
     */
    public Vault getEmergencyVault(int userId) {
        return vaultDao.getEmergencyVault(userId);
    }

    /**
     * Get default instant pay vault
     */
    public Vault getDefaultInstantPayVault(int userId) {
        return vaultDao.getDefaultInstantPayVault(userId);
    }

    // ============================================================
    // VAULT SPENDING MANAGEMENT
    // ============================================================

    /**
     * Update vault spending (add or subtract amount)
     *
     * @param vaultId Vault ID
     * @param amount Amount to add (positive) or subtract (negative)
     * @return true if successful
     */
    public boolean updateVaultSpending(int vaultId, double amount) {
        Vault vault = vaultDao.getVaultById(vaultId);
        if (vault != null) {
            double newSpent = vault.getCurrentSpent() + amount;
            // Ensure spending doesn't go negative
            newSpent = Math.max(0, newSpent);
            return vaultDao.updateVaultSpending(vaultId, newSpent) > 0;
        }
        return false;
    }

    /**
     * Check if payment can be made from vault
     *
     * @param vaultId Vault ID
     * @param amount Payment amount
     * @return true if vault has sufficient balance
     */
    public boolean canMakePayment(int vaultId, double amount) {
        Vault vault = vaultDao.getVaultById(vaultId);
        if (vault != null && vault.isActive()) {
            return vault.canAffordPayment(amount);
        }
        return false;
    }

    /**
     * Get total available balance across all vaults
     */
    public double getTotalBalance(int userId) {
        return vaultDao.getTotalAvailableBalance(userId);
    }

    /**
     * Get total vault limit (sum of all monthly limits)
     */
    public double getTotalVaultLimit(int userId) {
        return vaultDao.getTotalVaultLimit(userId);
    }

    // ============================================================
    // EMERGENCY VAULT PIN MANAGEMENT
    // ============================================================

    /**
     * Verify emergency vault PIN
     *
     * @param vaultId Emergency vault ID
     * @param enteredPin Entered PIN
     * @return true if PIN is correct
     */
    public boolean verifyEmergencyPin(int vaultId, String enteredPin) {
        Vault vault = vaultDao.getVaultById(vaultId);
        if (vault != null && vault.isEmergency() && vault.getEmergencyPinHash() != null) {
            return PinManager.verifyPin(enteredPin, vault.getEmergencyPinHash());
        }
        return false;
    }

    /**
     * Update emergency vault PIN
     *
     * @param vaultId Emergency vault ID
     * @param newPin New PIN
     * @return true if successful
     */
    public boolean updateEmergencyPin(int vaultId, String newPin) {
        String hashedPin = PinManager.hashPin(newPin);
        return vaultDao.setEmergencyPin(vaultId, hashedPin) > 0;
    }

    // ============================================================
    // VAULT UPDATES & DELETION
    // ============================================================

    /**
     * Update vault details
     */
    public boolean updateVault(Vault vault) {
        return vaultDao.updateVault(vault) > 0;
    }

    /**
     * Delete vault (soft delete)
     * Emergency vault cannot be deleted
     */
    public boolean deleteVault(int vaultId) {
        Vault vault = vaultDao.getVaultById(vaultId);
        if (vault != null && !vault.isEmergency()) {
            return vaultDao.deleteVault(vaultId) > 0;
        }
        return false;
    }

    // ============================================================
    // MONTHLY RESET
    // ============================================================

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

    // ============================================================
    // LOW BALANCE DETECTION
    // ============================================================

    /**
     * Check if vault balance is low (below 20% of limit)
     */
    public boolean isLowBalance(int vaultId) {
        Vault vault = vaultDao.getVaultById(vaultId);
        return vault != null && vault.isLowBalance();
    }

    /**
     * Get all vaults with low balance
     */
    public List<Vault> getLowBalanceVaults(int userId) {
        return vaultDao.getLowBalanceVaults(userId);
    }

    // ============================================================
    // VAULT VALIDATION
    // ============================================================

    /**
     * Check if user can create more vaults
     * (Optional: implement vault limit if needed)
     */
    public boolean canCreateMoreVaults(int userId) {
        List<Vault> vaults = vaultDao.getAllVaultsByUser(userId);
        // Example: Allow max 10 vaults per user
        return vaults.size() < 10;
    }

    /**
     * Check if vault type exists
     */
    public boolean vaultTypeExists(int userId, String vaultType) {
        return vaultDao.vaultTypeExists(userId, vaultType);
    }
}