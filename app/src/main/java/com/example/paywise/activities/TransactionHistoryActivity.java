package com.example.paywise.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.paywise.R;
import com.example.paywise.adapters.TransactionAdapter;
import com.example.paywise.database.TransactionDao;
import com.example.paywise.models.Transaction;
import com.example.paywise.utils.Constants;
import com.example.paywise.utils.PreferenceManager;
import java.util.ArrayList;
import java.util.List;

public class TransactionHistoryActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvTransactions;
    private TextView tvEmptyState;

    private TransactionDao transactionDao;
    private TransactionAdapter transactionAdapter;
    private PreferenceManager preferenceManager;

    private List<Transaction> transactionList;
    private int vaultId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        initializeViews();
        setupToolbar();

        preferenceManager = new PreferenceManager(this);
        transactionDao = new TransactionDao(this);

        // Check if specific vault ID was passed
        if (getIntent().hasExtra(Constants.EXTRA_VAULT_ID)) {
            vaultId = getIntent().getIntExtra(Constants.EXTRA_VAULT_ID, -1);
        }

        setupRecyclerView();
        loadTransactions();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        rvTransactions = findViewById(R.id.rvTransactions);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(this, transactionList);

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(transactionAdapter);
    }

    private void loadTransactions() {
        int userId = preferenceManager.getUserId();

        if (vaultId != -1) {
            // Load transactions for specific vault
            transactionList = transactionDao.getTransactionsByVault(vaultId);
        } else {
            // Load all transactions for user
            transactionList = transactionDao.getAllTransactionsByUser(userId);
        }

        if (transactionList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);
            transactionAdapter.updateTransactions(transactionList);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}