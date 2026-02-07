package com.example.paywise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.paywise.R;
import com.example.paywise.adapters.VaultAdapter;
import com.example.paywise.managers.VaultManager;
import com.example.paywise.models.Vault;
import com.example.paywise.utils.SessionManager;
import com.example.paywise.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;

/**
 * VaultSelectionActivity - Select vault for payment
 *
 * Two modes:
 * 1. Payment mode: Select vault for new payment
 * 2. Reassignment mode: Change vault for existing transaction
 */
public class VaultSelectionActivity extends AppCompatActivity implements VaultAdapter.OnVaultClickListener {

    private Toolbar toolbar;
    private TextView tvMerchantName;
    private TextView tvAmount;
    private TextView tvInstruction;
    private RecyclerView recyclerViewVaults;
    private MaterialButton btnProceedPayment;

    private VaultManager vaultManager;
    private SessionManager sessionManager;
    private VaultAdapter vaultAdapter;

    private String merchantName;
    private double amount;
    private String description;
    private String paymentMethod;
    private int transactionId;
    private boolean isReassignment;

    private Vault selectedVault;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault_selection);

        getIntentData();
        initializeViews();
        initializeManagers();
        setupToolbar();
        displayPaymentSummary();
        loadVaults();
        setupListeners();
    }

    /**
     * Get data from intent
     */
    private void getIntentData() {
        Intent intent = getIntent();
        merchantName = intent.getStringExtra("merchant_name");
        amount = intent.getDoubleExtra("amount", 0);
        description = intent.getStringExtra("description");
        paymentMethod = intent.getStringExtra("payment_method");
        transactionId = intent.getIntExtra("transaction_id", -1);
        isReassignment = intent.getBooleanExtra("is_reassignment", false);
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvMerchantName = findViewById(R.id.tvMerchantName);
        tvAmount = findViewById(R.id.tvAmount);
        tvInstruction = findViewById(R.id.tvInstruction);
        recyclerViewVaults = findViewById(R.id.recyclerViewVaults);
        btnProceedPayment = findViewById(R.id.btnProceedPayment);
    }

    /**
     * Initialize managers
     */
    private void initializeManagers() {
        vaultManager = new VaultManager(this);
        sessionManager = new SessionManager(this);
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
     * Display payment summary
     */
    private void displayPaymentSummary() {
        if (!isReassignment) {
            tvMerchantName.setText(merchantName);
            tvAmount.setText(ValidationUtils.formatAmount(amount));
        } else {
            tvInstruction.setText("Select new vault for transaction:");
        }
    }

    /**
     * Load and display vaults (non-emergency vaults only)
     */
    private void loadVaults() {
        List<Vault> vaults = vaultManager.getNonEmergencyVaults(userId);

        recyclerViewVaults.setLayoutManager(new LinearLayoutManager(this));
        vaultAdapter = new VaultAdapter(this, vaults);
        vaultAdapter.setOnVaultClickListener(this);
        recyclerViewVaults.setAdapter(vaultAdapter);
    }

    /**
     * Setup listeners
     */
    private void setupListeners() {
        btnProceedPayment.setOnClickListener(v -> proceedWithSelectedVault());
    }

    /**
     * Vault selected
     */
    @Override
    public void onVaultClick(Vault vault, int position) {
        selectedVault = vault;
        btnProceedPayment.setEnabled(true);

        // Highlight selected vault (you can add visual feedback here)
    }

    @Override
    public void onVaultLongClick(Vault vault, int position) {
        // Not used in this screen
    }

    /**
     * Proceed with selected vault
     */
    private void proceedWithSelectedVault() {
        if (selectedVault == null) {
            showError("Please select a vault");
            return;
        }

        if (!isReassignment) {
            // Check if vault can afford payment
            if (!selectedVault.canAffordPayment(amount)) {
                showInsufficientBalanceDialog();
                return;
            }

            // Navigate to payment confirmation
            Intent intent = new Intent(VaultSelectionActivity.this, PaymentConfirmationActivity.class);
            intent.putExtra("merchant_name", merchantName);
            intent.putExtra("amount", amount);
            intent.putExtra("description", description);
            intent.putExtra("vault_id", selectedVault.getVaultId());
            intent.putExtra("payment_method", paymentMethod);
            startActivity(intent);
        } else {
            // TODO: Handle vault reassignment for transaction
            // This would call PaymentManager.reassignTransactionVault()
        }
    }

    /**
     * Show insufficient balance dialog
     */
    private void showInsufficientBalanceDialog() {
        double remaining = selectedVault.getRemainingBalance();
        String message = String.format(
                "Insufficient balance in %s.\nAvailable: %s\nRequired: %s",
                selectedVault.getDisplayName(),
                ValidationUtils.formatAmount(remaining),
                ValidationUtils.formatAmount(amount)
        );

        new MaterialAlertDialogBuilder(this)
                .setTitle("Insufficient Balance")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        new MaterialAlertDialogBuilder(this)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}