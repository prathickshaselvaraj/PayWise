package com.example.paywise.utils;

import android.text.TextUtils;
import java.util.regex.Pattern;

/**
 * ValidationUtils - Input validation helper methods
 *
 * Validates:
 * - Mobile numbers (Indian format)
 * - Bank account numbers
 * - IFSC codes
 * - PINs
 * - Amounts
 */
public class ValidationUtils {

    /**
     * Validate Indian mobile number (10 digits, starts with 6-9)
     *
     * @param mobile Mobile number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidMobileNumber(String mobile) {
        if (TextUtils.isEmpty(mobile)) {
            return false;
        }
        return Pattern.matches(Constants.REGEX_MOBILE, mobile);
    }

    /**
     * Validate bank account number (9-18 digits)
     *
     * @param accountNumber Account number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidAccountNumber(String accountNumber) {
        if (TextUtils.isEmpty(accountNumber)) {
            return false;
        }
        return Pattern.matches(Constants.REGEX_ACCOUNT, accountNumber);
    }

    /**
     * Validate IFSC code (11 characters: XXXX0XXXXXX)
     *
     * @param ifsc IFSC code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidIfscCode(String ifsc) {
        if (TextUtils.isEmpty(ifsc)) {
            return false;
        }
        return Pattern.matches(Constants.REGEX_IFSC, ifsc.toUpperCase());
    }

    /**
     * Validate 6-digit PIN
     *
     * @param pin PIN to validate
     * @return true if valid format, false otherwise
     */
    public static boolean isValidPin(String pin) {
        if (TextUtils.isEmpty(pin)) {
            return false;
        }
        return Pattern.matches(Constants.REGEX_PIN, pin);
    }

    /**
     * Validate amount (must be positive number)
     *
     * @param amount Amount as string
     * @return true if valid, false otherwise
     */
    public static boolean isValidAmount(String amount) {
        if (TextUtils.isEmpty(amount)) {
            return false;
        }

        try {
            double value = Double.parseDouble(amount);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate email address (optional field)
     *
     * @param email Email to validate
     * @return true if valid or empty, false if invalid
     */
    public static boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return true; // Email is optional
        }

        String emailPattern = "^[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+$";
        return Pattern.matches(emailPattern, email);
    }

    /**
     * Validate name (alphabets and spaces only)
     *
     * @param name Name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidName(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }

        // Allow alphabets, spaces, and dots (for initials)
        String namePattern = "^[a-zA-Z .]+$";
        return Pattern.matches(namePattern, name.trim()) && name.trim().length() >= 2;
    }

    /**
     * Validate vault name
     *
     * @param vaultName Vault name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidVaultName(String vaultName) {
        if (TextUtils.isEmpty(vaultName)) {
            return false;
        }
        return vaultName.trim().length() >= 3 && vaultName.trim().length() <= 30;
    }

    /**
     * Format mobile number for display (+91 XXXXX XXXXX)
     *
     * @param mobile 10-digit mobile number
     * @return Formatted mobile number
     */
    public static String formatMobileNumber(String mobile) {
        if (mobile != null && mobile.length() == 10) {
            return "+91 " + mobile.substring(0, 5) + " " + mobile.substring(5);
        }
        return mobile;
    }

    /**
     * Format amount with currency symbol
     *
     * @param amount Amount value
     * @return Formatted amount string (₹X,XXX.XX)
     */
    public static String formatAmount(double amount) {
        return String.format("₹%.2f", amount);
    }

    /**
     * Format amount without decimals
     *
     * @param amount Amount value
     * @return Formatted amount string (₹X,XXX)
     */
    public static String formatAmountNoDecimals(double amount) {
        return String.format("₹%.0f", amount);
    }
}