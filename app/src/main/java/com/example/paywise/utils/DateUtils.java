package com.example.paywise.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * DateUtils - Date and time formatting utilities
 */
public class DateUtils {

    // ============================================================
    // CURRENT DATE/TIME
    // ============================================================

    /**
     * Get current date and time in ISO format
     */
    public static String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Get current date
     */
    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Get current timestamp in milliseconds
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    // ============================================================
    // FORMATTING
    // ============================================================

    /**
     * Format date for display (e.g., "Feb 05, 2026")
     */
    public static String formatDateForDisplay(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return dateString;
        }
    }

    /**
     * Format date and time for display (e.g., "Feb 05, 2026 02:45 PM")
     */
    public static String formatDateTimeForDisplay(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return dateString;
        }
    }

    /**
     * Format time only (e.g., "02:45 PM")
     */
    public static String formatTimeForDisplay(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return dateString;
        }
    }

    /**
     * Get relative time (e.g., "Today", "Yesterday")
     */
    public static String getRelativeDate(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(dateString.substring(0, 10));
            Date today = new Date();

            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(date);
            cal2.setTime(today);

            boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

            if (sameDay) {
                return "Today";
            }

            cal2.add(Calendar.DAY_OF_YEAR, -1);
            boolean yesterday = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

            if (yesterday) {
                return "Yesterday";
            }

            return formatDateForDisplay(dateString);
        } catch (Exception e) {
            return dateString;
        }
    }

    // ============================================================
    // VAULT RESET
    // ============================================================

    /**
     * Get next month's reset date (1st day of next month)
     */
    public static String getNextMonthResetDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    /**
     * Check if reset date has passed
     */
    public static boolean isResetDatePassed(String resetDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date reset = sdf.parse(resetDate);
            Date current = new Date();
            return current.after(reset);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // UTILITY METHODS
    // ============================================================

    /**
     * Get day of month
     */
    public static int getDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Get current month name
     */
    public static String getCurrentMonthName() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Get month and year (e.g., "February 2026")
     */
    public static String getCurrentMonthYear() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Format milliseconds to readable time (e.g., "2m 30s")
     */
    public static String formatMillisToReadable(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;

        if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
}