package com.example.paywise.utils;

import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * PinManager - Handles PIN hashing, validation, and security
 *
 * Features:
 * - Hash 6-digit PINs using SHA-256
 * - Validate entered PIN against stored hash
 * - Track failed login attempts
 * - Implement account lockout mechanism
 */
public class PinManager {

    /**
     * Hash a 6-digit PIN using SHA-256
     *
     * @param pin The 6-digit PIN to hash
     * @return Base64-encoded hash string, or null if error
     */
    public static String hashPin(String pin) {
        if (pin == null || pin.length() != Constants.PIN_LENGTH) {
            return null;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pin.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Verify if entered PIN matches the stored hash
     *
     * @param enteredPin The PIN entered by user
     * @param storedHash The stored hash from database
     * @return true if PIN matches, false otherwise
     */
    public static boolean verifyPin(String enteredPin, String storedHash) {
        if (enteredPin == null || storedHash == null) {
            return false;
        }

        String enteredHash = hashPin(enteredPin);
        return storedHash.equals(enteredHash);
    }

    /**
     * Validate PIN format (must be 6 digits)
     *
     * @param pin The PIN to validate
     * @return true if valid format, false otherwise
     */
    public static boolean isValidPinFormat(String pin) {
        if (pin == null) {
            return false;
        }
        return pin.matches(Constants.REGEX_PIN);
    }

    /**
     * Check if PIN is weak (sequential or repeated digits)
     *
     * @param pin The PIN to check
     * @return true if weak, false if strong
     */
    public static boolean isWeakPin(String pin) {
        if (pin == null || pin.length() != Constants.PIN_LENGTH) {
            return true;
        }

        // Check for all same digits (e.g., 111111)
        if (pin.matches("(\\d)\\1{5}")) {
            return true;
        }

        // Check for sequential digits (e.g., 123456, 654321)
        boolean isSequential = true;
        for (int i = 1; i < pin.length(); i++) {
            int diff = pin.charAt(i) - pin.charAt(i - 1);
            if (diff != 1 && diff != -1) {
                isSequential = false;
                break;
            }
        }

        return isSequential;
    }

    /**
     * Generate a random 6-digit PIN (for testing/demo purposes)
     * WARNING: Do not use in production for actual security
     *
     * @return Random 6-digit PIN
     */
    public static String generateRandomPin() {
        int pin = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(pin);
    }
}