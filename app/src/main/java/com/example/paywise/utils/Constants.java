package com.example.paywise.utils;

/**
 * Constants - Centralized configuration for ThinkPay AI
 * Contains all constant values used across the application
 */
public class Constants {

    // ============================================================
    // DATABASE CONFIGURATION
    // ============================================================
    public static final String DATABASE_NAME = "thinkpayai.db";
    public static final int DATABASE_VERSION = 2;  // Incremented for schema changes

    // ============================================================
    // TABLE NAMES
    // ============================================================
    public static final String TABLE_USERS = "users";
    public static final String TABLE_BANK_ACCOUNTS = "bank_accounts";  // NEW
    public static final String TABLE_VAULTS = "vaults";
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String TABLE_VAULT_REASSIGNMENTS = "vault_reassignments";  // NEW
    public static final String TABLE_SERVICE_LOGS = "service_logs";

    // ============================================================
    // VAULT TYPES
    // ============================================================
    public static final String VAULT_TYPE_FOOD = "Food";
    public static final String VAULT_TYPE_TRAVEL = "Travel";
    public static final String VAULT_TYPE_LIFESTYLE = "Lifestyle";
    public static final String VAULT_TYPE_BUSINESS = "Business";
    public static final String VAULT_TYPE_EMERGENCY = "Emergency";
    public static final String VAULT_TYPE_CUSTOM = "Custom";

    // ============================================================
    // TRANSACTION TYPES
    // ============================================================
    public static final String TRANSACTION_TYPE_DEBIT = "debit";
    public static final String TRANSACTION_TYPE_CREDIT = "credit";

    // ============================================================
    // PAYMENT METHODS (NEW)
    // ============================================================
    public static final String PAYMENT_METHOD_VAULT_BASED = "vault_based";
    public static final String PAYMENT_METHOD_INSTANT_PAY = "instant_pay";
    public static final String PAYMENT_METHOD_EMERGENCY = "emergency";

    // ============================================================
    // TRANSACTION STATUS
    // ============================================================
    public static final String TRANSACTION_STATUS_SUCCESS = "success";
    public static final String TRANSACTION_STATUS_FAILED = "failed";
    public static final String TRANSACTION_STATUS_PENDING = "pending";

    // ============================================================
    // SHARED PREFERENCES
    // ============================================================
    public static final String PREF_NAME = "ThinkPayAIPrefs";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_IS_LOGGED_IN = "is_logged_in";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_MOBILE_NUMBER = "mobile_number";
    public static final String PREF_PROFILE_IMAGE = "profile_image";
    public static final String PREF_HAS_BANK_ACCOUNT = "has_bank_account";  // NEW
    public static final String PREF_EMERGENCY_VAULT_ID = "emergency_vault_id";  // NEW
    public static final String PREF_DEFAULT_INSTANT_VAULT_ID = "default_instant_vault_id";  // NEW
    public static final String PREF_SESSION_ACTIVE = "session_active";  // NEW
    public static final String PREF_LAST_ACTIVITY_TIME = "last_activity_time";  // NEW

    // ============================================================
    // SECURITY & AUTHENTICATION (NEW)
    // ============================================================
    public static final int PIN_LENGTH = 6;
    public static final int MAX_LOGIN_ATTEMPTS = 3;
    public static final long LOCKOUT_DURATION_MS = 30000;  // 30 seconds
    public static final long SESSION_TIMEOUT_MS = 300000;  // 5 minutes

    // ============================================================
    // DEFAULT VALUES (NEW)
    // ============================================================
    public static final double DEFAULT_EMERGENCY_VAULT_LIMIT = 5000.0;
    public static final double DEFAULT_SIMULATED_BANK_BALANCE = 50000.0;
    public static final String DEFAULT_INSTANT_PAY_VAULT_TYPE = VAULT_TYPE_LIFESTYLE;

    // ============================================================
    // VAULT ICONS (NEW)
    // ============================================================
    public static final String ICON_FOOD = "🍔";
    public static final String ICON_TRAVEL = "✈️";
    public static final String ICON_LIFESTYLE = "🎨";
    public static final String ICON_BUSINESS = "💼";
    public static final String ICON_EMERGENCY = "🚨";

    // Custom vault icons
    public static final String ICON_HEALTHCARE = "🏥";
    public static final String ICON_EDUCATION = "📚";
    public static final String ICON_HOME = "🏠";
    public static final String ICON_ENTERTAINMENT = "🎮";
    public static final String ICON_MEDICAL = "💊";
    public static final String ICON_VEHICLE = "🚗";
    public static final String ICON_SPORTS = "⚽";
    public static final String ICON_MUSIC = "🎵";
    public static final String ICON_PET = "🐕";

    // ============================================================
    // VAULT COLORS (Vibrant Material Design)
    // ============================================================
    public static final String COLOR_FOOD = "#FF6B6B";
    public static final String COLOR_TRAVEL = "#4ECDC4";
    public static final String COLOR_LIFESTYLE = "#95E1D3";
    public static final String COLOR_BUSINESS = "#F38181";
    public static final String COLOR_EMERGENCY = "#FF8C42";

    // Additional colors for custom vaults
    public static final String COLOR_PURPLE = "#9B59B6";
    public static final String COLOR_BLUE = "#3498DB";
    public static final String COLOR_GREEN = "#2ECC71";
    public static final String COLOR_YELLOW = "#F1C40F";
    public static final String COLOR_PINK = "#E91E63";
    public static final String COLOR_BROWN = "#795548";
    public static final String COLOR_GREY = "#607D8B";

    // ============================================================
    // NOTIFICATIONS
    // ============================================================
    public static final String CHANNEL_ID = "thinkpayai_channel";
    public static final String CHANNEL_NAME = "ThinkPay AI Notifications";
    public static final int NOTIFICATION_ID_PAYMENT = 1001;
    public static final int NOTIFICATION_ID_LOW_BALANCE = 1002;
    public static final int NOTIFICATION_ID_VAULT_RESET = 1003;

    // ============================================================
    // INTENT EXTRAS
    // ============================================================
    public static final String EXTRA_VAULT_ID = "vault_id";
    public static final String EXTRA_TRANSACTION_ID = "transaction_id";
    public static final String EXTRA_MERCHANT_NAME = "merchant_name";
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_PAYMENT_METHOD = "payment_method";
    public static final String EXTRA_IS_EMERGENCY = "is_emergency";  // NEW
    public static final String EXTRA_DESCRIPTION = "description";  // NEW

    // ============================================================
    // IMAGE HANDLING
    // ============================================================
    public static final int MAX_IMAGE_SIZE = 1024;  // pixels
    public static final int IMAGE_QUALITY = 80;  // compression quality

    // ============================================================
    // REQUEST CODES
    // ============================================================
    public static final int REQUEST_IMAGE_PICK = 100;
    public static final int REQUEST_IMAGE_CAPTURE = 101;
    public static final int REQUEST_VAULT_SELECTION = 200;  // NEW
    public static final int REQUEST_PIN_ENTRY = 300;  // NEW
    public static final int REQUEST_EMERGENCY_PIN = 301;  // NEW

    // ============================================================
    // RESULT CODES (NEW)
    // ============================================================
    public static final int RESULT_VAULT_SELECTED = 201;
    public static final int RESULT_PIN_VERIFIED = 302;
    public static final int RESULT_PAYMENT_SUCCESS = 400;
    public static final int RESULT_PAYMENT_FAILED = 401;

    // ============================================================
    // LOW BALANCE THRESHOLD
    // ============================================================
    public static final double LOW_BALANCE_THRESHOLD = 0.2;  // 20% of limit

    // ============================================================
    // BANK NAMES (NEW)
    // ============================================================
    public static final String[] BANK_NAMES = {
            "State Bank of India",
            "HDFC Bank",
            "ICICI Bank",
            "Axis Bank",
            "Punjab National Bank",
            "Bank of Baroda",
            "Canara Bank",
            "Kotak Mahindra Bank",
            "Yes Bank",
            "Other Bank"
    };

    // ============================================================
    // VALIDATION PATTERNS (NEW)
    // ============================================================
    public static final String REGEX_MOBILE = "^[6-9]\\d{9}$";  // Indian mobile
    public static final String REGEX_IFSC = "^[A-Z]{4}0[A-Z0-9]{6}$";  // IFSC code
    public static final String REGEX_ACCOUNT = "^\\d{9,18}$";  // Account number
    public static final String REGEX_PIN = "^\\d{6}$";  // 6-digit PIN
}