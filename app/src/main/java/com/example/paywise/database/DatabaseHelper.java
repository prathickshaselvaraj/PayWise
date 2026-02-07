package com.example.paywise.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.paywise.utils.Constants;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_USERS_TABLE = "CREATE TABLE " + Constants.TABLE_USERS + " (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "full_name TEXT NOT NULL, " +
                "mobile_number TEXT UNIQUE NOT NULL, " +
                "email TEXT, " +
                "app_pin_hash TEXT NOT NULL, " +
                "profile_image_path TEXT, " +
                "is_locked INTEGER DEFAULT 0, " +
                "failed_attempts INTEGER DEFAULT 0, " +
                "lockout_until TEXT, " +
                "created_at TEXT NOT NULL, " +
                "updated_at TEXT NOT NULL)";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_BANK_ACCOUNTS_TABLE = "CREATE TABLE " + Constants.TABLE_BANK_ACCOUNTS + " (" +
                "account_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "bank_name TEXT NOT NULL, " +
                "account_number TEXT NOT NULL, " +
                "account_holder_name TEXT NOT NULL, " +
                "ifsc_code TEXT NOT NULL, " +
                "is_primary INTEGER DEFAULT 1, " +
                "simulated_balance REAL DEFAULT 50000.0, " +
                "created_at TEXT NOT NULL, " +
                "FOREIGN KEY(user_id) REFERENCES " + Constants.TABLE_USERS + "(user_id) ON DELETE CASCADE)";
        db.execSQL(CREATE_BANK_ACCOUNTS_TABLE);

        String CREATE_VAULTS_TABLE = "CREATE TABLE " + Constants.TABLE_VAULTS + " (" +
                "vault_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "vault_name TEXT NOT NULL, " +
                "vault_type TEXT NOT NULL, " +
                "custom_category_name TEXT, " +
                "vault_icon TEXT NOT NULL, " +
                "vault_color TEXT NOT NULL, " +
                "monthly_limit REAL NOT NULL, " +
                "current_spent REAL DEFAULT 0, " +
                "is_emergency INTEGER DEFAULT 0, " +
                "emergency_pin_hash TEXT, " +
                "is_active INTEGER DEFAULT 1, " +
                "is_default_instant_pay INTEGER DEFAULT 0, " +
                "created_at TEXT NOT NULL, " +
                "reset_date TEXT NOT NULL, " +
                "FOREIGN KEY(user_id) REFERENCES " + Constants.TABLE_USERS + "(user_id) ON DELETE CASCADE)";
        db.execSQL(CREATE_VAULTS_TABLE);

        String CREATE_TRANSACTIONS_TABLE = "CREATE TABLE " + Constants.TABLE_TRANSACTIONS + " (" +
                "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "vault_id INTEGER NOT NULL, " +
                "original_vault_id INTEGER, " +
                "merchant_name TEXT NOT NULL, " +
                "amount REAL NOT NULL, " +
                "transaction_type TEXT NOT NULL CHECK(transaction_type IN ('debit', 'credit')), " +
                "payment_method TEXT NOT NULL CHECK(payment_method IN ('vault_based', 'instant_pay', 'emergency')), " +
                "description TEXT, " +
                "transaction_date TEXT NOT NULL, " +
                "status TEXT NOT NULL CHECK(status IN ('success', 'failed', 'pending')), " +
                "vault_changed INTEGER DEFAULT 0, " +
                "vault_changed_at TEXT, " +
                "FOREIGN KEY(vault_id) REFERENCES " + Constants.TABLE_VAULTS + "(vault_id) ON DELETE CASCADE, " +
                "FOREIGN KEY(original_vault_id) REFERENCES " + Constants.TABLE_VAULTS + "(vault_id))";
        db.execSQL(CREATE_TRANSACTIONS_TABLE);

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

        String CREATE_SERVICE_LOGS_TABLE = "CREATE TABLE " + Constants.TABLE_SERVICE_LOGS + " (" +
                "log_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "service_name TEXT NOT NULL, " +
                "action_type TEXT NOT NULL, " +
                "message TEXT, " +
                "timestamp TEXT NOT NULL)";
        db.execSQL(CREATE_SERVICE_LOGS_TABLE);

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
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_SERVICE_LOGS);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_VAULT_REASSIGNMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_VAULTS);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_BANK_ACCOUNTS);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_USERS);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}
