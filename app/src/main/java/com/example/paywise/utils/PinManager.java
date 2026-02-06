package com.example.paywise.utils;

import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PinManager - Handles PIN hashing and validation
 * Uses SHA-256 for secure PIN storage
 */
public class PinManager {

    /**
     * Hash a PIN using SHA-256
     * @param pin 6-digit PIN
     * @return Base64 encoded hash
     */
    public static String hashPin(String pin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pin.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Verify if entered PIN matches stored hash
     * @param enteredPin PIN entered by user
     * @param storedHash Hashed PIN from database
     * @return true if match, false otherwise
     */
    public static boolean verifyPin(String enteredPin, String storedHash) {
        if (enteredPin == null || storedHash == null) {
            return false;
        }
        String enteredHash = hashPin(enteredPin);
        return storedHash.equals(enteredHash);
    }

    /**
     * Validate PIN format (6 digits)
     * @param pin PIN to validate
     * @return true if valid format
     */
    public static boolean isValidPinFormat(String pin) {
        if (pin == null) return false;
        return pin.matches(Constants.REGEX_PIN);
    }

    /**
     * Check if PIN is weak (e.g., 123456, 000000, etc.)
     * @param pin PIN to check
     * @return true if weak
     */
    public static boolean isWeakPin(String pin) {
        if (pin == null || pin.length() != 6) return true;

        // Check for all same digits
        if (pin.matches("(\\d)\\1{5}")) return true;

        // Check for sequential patterns
        String[] weakPatterns = {"123456", "654321", "000000", "111111", "222222",
                "333333", "444444", "555555", "666666", "777777",
                "888888", "999999", "112233", "121212"};
        for (String pattern : weakPatterns) {
            if (pin.equals(pattern)) return true;
        }

        return false;
    }

    /**
     * Generate a salt for additional security (optional enhancement)
     * @return Random salt string
     */
    public static String generateSalt() {
        return String.valueOf(System.currentTimeMillis());
    }
}