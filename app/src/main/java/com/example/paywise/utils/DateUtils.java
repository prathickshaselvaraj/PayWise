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
    // CURRENT DATE/TIME METHODS
    // ============================================================

    /**
     * Get current date and time in ISO format (yyyy-MM-dd HH:mm:ss)
     */
    public static String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Get current date only (yyyy-MM-dd)
     */
    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Get current time only (HH:mm:ss)
     */
    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // ============================================================
    // FORMATTING METHODS
    // ============================================================

    /**
     * Format date for display (MMM dd, yyyy)
     * Example: Jan 21, 2026
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
     * Format date and time for display (MMM dd, yyyy hh:mm a)
     * Example: Jan 21, 2026 02:45 PM
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
     * Format time only (hh:mm a)
     * Example: 02:45 PM
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
     * Get transaction date label (Today, Yesterday, or date)
     */
    public static String getTransactionDateLabel(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date transactionDate = inputFormat.parse(dateString);

            Calendar transactionCal = Calendar.getInstance();
            transactionCal.setTime(transactionDate);

            Calendar today = Calendar.getInstance();
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_YEAR, -1);

            if (isSameDay(transactionCal, today)) {
                return "Today";
            } else if (isSameDay(transactionCal, yesterday)) {
                return "Yesterday";
            } else {
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                return outputFormat.format(transactionDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return dateString;
        }
    }

    /**
     * Check if two calendar dates are on the same day
     */
    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    // ============================================================
    // VAULT RESET METHODS
    // ============================================================

    /**
     * Get next month's reset date (1st day of next month)
     */
    public static String getNextMonthResetDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

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
     * Get day of month (1-31)
     */
    public static int getDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Get current month name (January, February, etc.)
     */
    public static String getCurrentMonthName() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Get time elapsed since timestamp (e.g., "2 hours ago")
     */
    public static String getTimeElapsed(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date past = sdf.parse(dateString);
            Date now = new Date();

            long diff = now.getTime() - past.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) {
                return days + " day" + (days > 1 ? "s" : "") + " ago";
            } else if (hours > 0) {
                return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
            } else if (minutes > 0) {
                return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
            } else {
                return "Just now";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}