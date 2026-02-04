package com.example.paywise.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.paywise.utils.Constants;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String CREATE_USERS_TABLE = "CREATE TABLE " + Constants.TABLE_USERS + " (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "full_name TEXT NOT NULL, " +
                "email TEXT, " +
                "phone TEXT NOT NULL, " +
                "profile_image_path TEXT, " +
                "created_at TEXT NOT NULL, " +
                "updated_at TEXT NOT NULL)";
        db.execSQL(CREATE_USERS_TABLE);

        // Create vaults table
        String CREATE_VAULTS_TABLE = "CREATE TABLE " + Constants.TABLE_VAULTS + " (" +
                "vault_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "vault_name TEXT NOT NULL, " +
                "vault_type TEXT NOT NULL CHECK(vault_type IN ('Food', 'Travel', 'Lifestyle', 'Business', 'Emergency')), " +
                "monthly_limit REAL NOT NULL, " +
                "current_spent REAL DEFAULT 0, " +
                "vault_color TEXT NOT NULL, " +
                "is_active INTEGER DEFAULT 1, " +
                "created_at TEXT NOT NULL, " +
                "reset_date TEXT NOT NULL, " +
                "FOREIGN KEY(user_id) REFERENCES " + Constants.TABLE_USERS + "(user_id) ON DELETE CASCADE)";
        db.execSQL(CREATE_VAULTS_TABLE);

        // Create transactions table
        String CREATE_TRANSACTIONS_TABLE = "CREATE TABLE " + Constants.TABLE_TRANSACTIONS + " (" +
                "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "vault_id INTEGER NOT NULL, " +
                "merchant_name TEXT NOT NULL, " +
                "amount REAL NOT NULL, " +
                "transaction_type TEXT NOT NULL CHECK(transaction_type IN ('debit', 'credit')), " +
                "description TEXT, " +
                "transaction_date TEXT NOT NULL, " +
                "status TEXT NOT NULL CHECK(status IN ('success', 'failed', 'pending')), " +
                "FOREIGN KEY(vault_id) REFERENCES " + Constants.TABLE_VAULTS + "(vault_id) ON DELETE CASCADE)";
        db.execSQL(CREATE_TRANSACTIONS_TABLE);

        // Create service_logs table
        String CREATE_SERVICE_LOGS_TABLE = "CREATE TABLE " + Constants.TABLE_SERVICE_LOGS + " (" +
                "log_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "service_name TEXT NOT NULL, " +
                "action_type TEXT NOT NULL, " +
                "message TEXT, " +
                "timestamp TEXT NOT NULL)";
        db.execSQL(CREATE_SERVICE_LOGS_TABLE);

        // Create indexes for performance
        db.execSQL("CREATE INDEX idx_vault_user ON " + Constants.TABLE_VAULTS + "(user_id)");
        db.execSQL("CREATE INDEX idx_transaction_vault ON " + Constants.TABLE_TRANSACTIONS + "(vault_id)");
        db.execSQL("CREATE INDEX idx_transaction_date ON " + Constants.TABLE_TRANSACTIONS + "(transaction_date)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if exist
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_SERVICE_LOGS);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_VAULTS);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_USERS);

        // Create tables again
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Enable foreign key constraints
        db.setForeignKeyConstraintsEnabled(true);
    }
}