package com.example.paywise.utils;

public class Constants {
    // Database
    public static final String DATABASE_NAME = "paywise.db";
    public static final int DATABASE_VERSION = 1;

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_VAULTS = "vaults";
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String TABLE_SERVICE_LOGS = "service_logs";

    // Vault Types
    public static final String VAULT_TYPE_FOOD = "Food";
    public static final String VAULT_TYPE_TRAVEL = "Travel";
    public static final String VAULT_TYPE_LIFESTYLE = "Lifestyle";
    public static final String VAULT_TYPE_BUSINESS = "Business";
    public static final String VAULT_TYPE_EMERGENCY = "Emergency";

    // Transaction Types
    public static final String TRANSACTION_TYPE_DEBIT = "debit";
    public static final String TRANSACTION_TYPE_CREDIT = "credit";

    // Transaction Status
    public static final String TRANSACTION_STATUS_SUCCESS = "success";
    public static final String TRANSACTION_STATUS_FAILED = "failed";
    public static final String TRANSACTION_STATUS_PENDING = "pending";

    // SharedPreferences
    public static final String PREF_NAME = "PayWisePrefs";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_IS_LOGGED_IN = "is_logged_in";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_PROFILE_IMAGE = "profile_image";

    // Notifications
    public static final String CHANNEL_ID = "paywise_channel";
    public static final String CHANNEL_NAME = "PayWise Notifications";
    public static final int NOTIFICATION_ID_PAYMENT = 1001;
    public static final int NOTIFICATION_ID_LOW_BALANCE = 1002;
    public static final int NOTIFICATION_ID_VAULT_RESET = 1003;

    // Intent Extras
    public static final String EXTRA_VAULT_ID = "vault_id";
    public static final String EXTRA_TRANSACTION_ID = "transaction_id";

    // Image
    public static final int MAX_IMAGE_SIZE = 1024; // pixels
    public static final int IMAGE_QUALITY = 80; // compression quality

    // Request Codes
    public static final int REQUEST_IMAGE_PICK = 100;
    public static final int REQUEST_IMAGE_CAPTURE = 101;

    // Low Balance Threshold
    public static final double LOW_BALANCE_THRESHOLD = 0.2; // 20% of limit
}