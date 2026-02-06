package com.example.thinkpayai.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.thinkpayai.utils.Constants;

/**
 * DatabaseHelper - SQLite Database Manager for ThinkPay AI
 *
 * Manages 6 tables:
 * 1. users - User profile and authentication
 * 2. bank_accounts - Linked bank account details
 * 3. vaults - Budget vaults with spending limits
 * 4. transactions - All payment transactions
 * 5. vault_reassignments - History of vault changes
 * 6. service_logs - Background service activity logs
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // ============================================================
        // TABLE 1: USERS (Updated with PIN authentication)
        // ============================================================
        String CREATE_USERS_TABLE = "CREATE TABLE " + Constants.TABLE_USERS + " (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "full_name TEXT NOT NULL, " +
                "mobile_number TEXT UNIQUE NOT NULL, " +
                "email TEXT, " +
                "app_pin_hash TEXT NOT NULL, " +  // Hashed 6-digit PIN
                "profile_image_path TEXT, " +
                "is_locked INTEGER DEFAULT 0, " +  // Account lock status
                "failed_attempts INTEGER DEFAULT 0, " +  // Failed login count
                "lockout_until TEXT, " +  // Lockout expiry timestamp
                "created_at TEXT NOT NULL, " +
                "updated_at TEXT NOT NULL)";
        db.execSQL(CREATE_USERS_TABLE);

        // ============================================================
        // TABLE 2: BANK_ACCOUNTS (NEW - Bank account details)
        // ============================================================
        String CREATE_BANK_ACCOUNTS_TABLE = "CREATE TABLE " + Constants.TABLE_BANK_ACCOUNTS + " (" +
                "account_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "bank_name TEXT NOT NULL, " +
                "account_number TEXT NOT NULL, " +
                "account_holder_name TEXT NOT NULL, " +
                "ifsc_code TEXT NOT NULL, " +
                "is_primary INTEGER DEFAULT 1, " +  // Primary account flag
                "simulated_balance REAL DEFAULT 50000.0, " +  // Simulated bank balance
                "created_at TEXT NOT NULL, " +
                "FOREIGN KEY(user_id) REFERENCES " + Constants.TABLE_USERS + "(user_id) ON DELETE CASCADE)";
        db.execSQL(CREATE_BANK_ACCOUNTS_TABLE);

        // ============================================================
        // TABLE 3: VAULTS (Updated with emergency PIN and custom vaults)
        // ============================================================
        String CREATE_VAULTS_TABLE = "CREATE TABLE " + Constants.TABLE_VAULTS + " (" +
                "vault_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "vault_name TEXT NOT NULL, " +
                "vault_type TEXT NOT NULL, " +  // 'Food', 'Travel', 'Lifestyle', 'Business', 'Emergency', 'Custom'
                "custom_category_name TEXT, " +  // For custom vaults
                "vault_icon TEXT NOT NULL, " +  // Icon identifier or emoji
                "vault_color TEXT NOT NULL, " +
                "monthly_limit REAL NOT NULL, " +
                "current_spent REAL DEFAULT 0, " +
                "is_emergency INTEGER DEFAULT 0, " +  // 1 if emergency vault
                "emergency_pin_hash TEXT, " +  // Special PIN for emergency vault
                "is_active INTEGER DEFAULT 1, " +
                "is_default_instant_pay INTEGER DEFAULT 0, " +  // 1 if default for instant pay
                "created_at TEXT NOT NULL, " +
                "reset_date TEXT NOT NULL, " +
                "FOREIGN KEY(user_id) REFERENCES " + Constants.TABLE_USERS + "(user_id) ON DELETE CASCADE)";
        db.execSQL(CREATE_VAULTS_TABLE);

        // ============================================================
        // TABLE 4: TRANSACTIONS (Updated with payment methods and vault reassignment)
        // ============================================================
        String CREATE_TRANSACTIONS_TABLE = "CREATE TABLE " + Constants.TABLE_TRANSACTIONS + " (" +
                "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "vault_id INTEGER NOT NULL, " +
                "original_vault_id INTEGER, " +  // Track original vault if changed
                "merchant_name TEXT NOT NULL, " +
                "amount REAL NOT NULL, " +
                "transaction_type TEXT NOT NULL CHECK(transaction_type IN ('debit', 'credit')), " +
                "payment_method TEXT NOT NULL CHECK(payment_method IN ('vault_based', 'instant_pay', 'emergency')), " +
                "description TEXT, " +
                "transaction_date TEXT NOT NULL, " +
                "status TEXT NOT NULL CHECK(status IN ('success', 'failed', 'pending')), " +
                "vault_changed INTEGER DEFAULT 0, " +  // 1 if vault was reassigned
                "vault_changed_at TEXT, " +  // Timestamp of vault change
                "FOREIGN KEY(vault_id) REFERENCES " + Constants.TABLE_VAULTS + "(vault_id) ON DELETE CASCADE, " +
                "FOREIGN KEY(original_vault_id) REFERENCES " + Constants.TABLE_VAULTS + "(vault_id))";
        db.execSQL(CREATE_TRANSACTIONS_TABLE);

        // ============================================================
        // TABLE 5: VAULT_REASSIGNMENTS (NEW - Track vault changes)
        // ============================================================
        String CREATE_VAULT_REASSIGNMENTS_TABLE = "CREATE TABLE " + Constants.TABLE_VAULT_REASSIGNMENTS + " (" +
                "reassignment_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "transaction_id INTEGER NOT NULL, " +
                "from_vault_id INTEGER NOT NULL, " +
                "to_vault_id INTEGER NOT NULL, " +
                "changed_by_user_id INTEGER NOT NULL, " +
                "changed_at TEXT NOT NULL, " +
                "FOREIGN KEY(transaction_id) REFERENCES " + Constants.TABLE_TRANSACTIONS + "(transaction_id), " +
                "FOREIGN KEY(from_vault_id) REFERENCES " + Constants.TABLE_VAULTS + "(vault_id), " +
                "FOREIGN KEY(to_vault_id) REFERENCES " + Constants.TABLE_VAULTS + "(vault_id), " +
                "FOREIGN KEY(changed_by_user_id) REFERENCES " + Constants.TABLE_USERS + "(user_id))";
        db.execSQL(CREATE_VAULT_REASSIGNMENTS_TABLE);

        // ============================================================
        // TABLE 6: SERVICE_LOGS (Same as before)
        // ============================================================
        String CREATE_SERVICE_LOGS_TABLE = "CREATE TABLE " + Constants.TABLE_SERVICE_LOGS + " (" +
                "log_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "service_name TEXT NOT NULL, " +
                "action_type TEXT NOT NULL, " +
                "message TEXT, " +
                "timestamp TEXT NOT NULL)";
        db.execSQL(CREATE_SERVICE_LOGS_TABLE);

        // ============================================================
        // CREATE INDEXES for performance optimization
        // ============================================================
        db.execSQL("CREATE INDEX idx_user_mobile ON " + Constants.TABLE_USERS + "(mobile_number)");
        db.execSQL("CREATE INDEX idx_bank_user ON " + Constants.TABLE_BANK_ACCOUNTS + "(user_id)");
        db.execSQL("CREATE INDEX idx_vault_user ON " + Constants.TABLE_VAULTS + "(user_id)");
        db.execSQL("CREATE INDEX idx_vault_type ON " + Constants.TABLE_VAULTS + "(vault_type)");
        db.execSQL("CREATE INDEX idx_transaction_vault ON " + Constants.TABLE_TRANSACTIONS + "(vault_id)");
        db.execSQL("CREATE INDEX idx_transaction_date ON " + Constants.TABLE_TRANSACTIONS + "(transaction_date)");
        db.execSQL("CREATE INDEX idx_reassignment_transaction ON " + Constants.TABLE_VAULT_REASSIGNMENTS + "(transaction_id)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop all tables in reverse order of dependencies
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_SERVICE_LOGS);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_VAULT_REASSIGNMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_VAULTS);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_BANK_ACCOUNTS);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_USERS);

        // Recreate all tables
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Enable foreign key constraints
        db.setForeignKeyConstraintsEnabled(true);
    }
}