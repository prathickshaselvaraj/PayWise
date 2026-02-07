package com.example.paywise.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.paywise.R;
import com.example.paywise.adapters.TransactionAdapter;
import com.example.paywise.database.TransactionDao;
import com.example.paywise.managers.PaymentManager;
import com.example.paywise.models.Transaction;
import com.example.paywise.utils.Constants;
import com.example.paywise.utils.SessionManager;
import com.google.android.material.chip.Chip;
import java.util.List;

/**
 * TransactionHistoryActivity - View all transactions with filtering
 *
 * Features:
 * - Display all transactions for user (or specific vault)
 * - Filter by status (All, Success, Failed)
 * - Support vault reassignment for instant pay transactions
 */
public class TransactionHistoryActivity extends AppCompatActivity implements
        TransactionAdapter.OnTransactionClickListener {

    private Toolbar toolbar;
    private Chip chipAll;
    private Chip chipSuccess;
    private Chip chipFailed;
    private RecyclerView recyclerViewTransactions;
    private LinearLayout layoutEmptyState;

    private SessionManager sessionManager;
    private PaymentManager paymentManager;
    private TransactionAdapter transactionAdapter;

    private int userId;
    private int vaultId = -1; // -1 means show all vaults
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        // Check if filtering by specific vault
        vaultId = getIntent().getIntExtra("vault_id", -1);

        initializeViews();
        initializeManagers();
        setupToolbar();
        setupFilterChips();
        loadTransactions();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        chipAll = findViewById(R.id.chipAll);
        chipSuccess = findViewById(R.id.chipSuccess);
        chipFailed = findViewById(R.id.chipFailed);
        recyclerViewTransactions = findViewById(R.id.recyclerViewTransactions);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
    }

    /**
     * Initialize managers
     */
    private void initializeManagers() {
        sessionManager = new SessionManager(this);
        paymentManager = new PaymentManager(this);
        userId = sessionManager.getUserId();
    }

    /**
     * Setup toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    /**
     * Setup filter chips
     */
    private void setupFilterChips() {
        chipAll.setOnClickListener(v -> {
            currentFilter = "all";
            loadTransactions();
        });

        chipSuccess.setOnClickListener(v -> {
            currentFilter = Constants.TRANSACTION_STATUS_SUCCESS;
            loadTransactions();
        });

        chipFailed.setOnClickListener(v -> {
            currentFilter = Constants.TRANSACTION_STATUS_FAILED;
            loadTransactions();
        });
    }

    /**
     * Load transactions based on current filter
     */
    private void loadTransactions() {
        List<Transaction> transactions;

        if (vaultId != -1) {
            // Load transactions for specific vault
            TransactionDao transactionDao = new TransactionDao(this);
            transactions = transactionDao.getTransactionsByVault(vaultId);
        } else {
            // Load all transactions for user
            transactions = paymentManager.getAllTransactions(userId);
        }

        // Apply filter
        if (!currentFilter.equals("all")) {
            transactions = filterTransactionsByStatus(transactions, currentFilter);
        }

        // Display transactions
        if (transactions.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            displayTransactions(transactions);
        }
    }

    /**
     * Filter transactions by status
     */
    private List<Transaction> filterTransactionsByStatus(List<Transaction> transactions, String status) {
        List<Transaction> filtered = new java.util.ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getStatus().equals(status)) {
                filtered.add(transaction);
            }
        }
        return filtered;
    }

    /**
     * Display transactions in RecyclerView
     */
    private void displayTransactions(List<Transaction> transactions) {
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(this, transactions);
        transactionAdapter.setOnTransactionClickListener(this);
        recyclerViewTransactions.setAdapter(transactionAdapter);
    }

    /**
     * Show empty state
     */
    private void showEmptyState() {
        recyclerViewTransactions.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
    }

    /**
     * Hide empty state
     */
    private void hideEmptyState() {
        recyclerViewTransactions.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
    }

    /**
     * Transaction clicked - show details
     */
    @Override
    public void onTransactionClick(Transaction transaction, int position) {
        // TODO: Show transaction details dialog
    }

    /**
     * Change vault clicked - navigate to vault selection for reassignment
     */
    @Override
    public void onChangeVaultClick(Transaction transaction, int position) {
        // TODO: Navigate to VaultSelectionActivity for reassignment
        // This would pass is_reassignment=true and transaction_id
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh transactions when returning to this screen
        loadTransactions();
    }
}