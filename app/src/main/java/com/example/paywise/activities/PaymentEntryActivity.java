package com.example.paywise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import com.example.paywise.R;
import com.example.paywise.utils.SessionManager;
import com.example.paywise.utils.ValidationUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * PaymentEntryActivity - Enter payment details and select payment method
 *
 * Flow:
 * 1. User enters merchant name and amount
 * 2. User selects payment method:
 *    - Vault-Based Payment → Navigate to VaultSelectionActivity
 *    - Instant Pay → Navigate directly to PaymentConfirmationActivity with default vault
 *    - Emergency Payment → Show warning dialog, then navigate to PaymentConfirmationActivity
 */
public class PaymentEntryActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText etMerchantName;
    private EditText etAmount;
    private EditText etDescription;
    private CardView cardVaultBased;
    private CardView cardInstantPay;
    private CardView cardEmergencyPay;

    private SessionManager sessionManager;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_entry);

        initializeViews();
        setupToolbar();
        setupListeners();

        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        etMerchantName = findViewById(R.id.etMerchantName);
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        cardVaultBased = findViewById(R.id.cardVaultBased);
        cardInstantPay = findViewById(R.id.cardInstantPay);
        cardEmergencyPay = findViewById(R.id.cardEmergencyPay);
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
     * Setup click listeners for payment method cards
     */
    private void setupListeners() {
        cardVaultBased.setOnClickListener(v -> handleVaultBasedPayment());
        cardInstantPay.setOnClickListener(v -> handleInstantPayment());
        cardEmergencyPay.setOnClickListener(v -> handleEmergencyPayment());
    }

    /**
     * Validate payment details
     */
    private boolean validatePaymentDetails() {
        String merchantName = etMerchantName.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();

        if (merchantName.isEmpty()) {
            etMerchantName.setError(getString(R.string.error_empty_merchant));
            etMerchantName.requestFocus();
            return false;
        }

        if (!ValidationUtils.isValidAmount(amountStr)) {
            etAmount.setError(getString(R.string.error_invalid_amount));
            etAmount.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Handle vault-based payment selection
     */
    private void handleVaultBasedPayment() {
        if (!validatePaymentDetails()) {
            return;
        }

        // Navigate to vault selection
        Intent intent = new Intent(PaymentEntryActivity.this, VaultSelectionActivity.class);
        intent.putExtra("merchant_name", etMerchantName.getText().toString().trim());
        intent.putExtra("amount", Double.parseDouble(etAmount.getText().toString().trim()));
        intent.putExtra("description", etDescription.getText().toString().trim());
        intent.putExtra("payment_method", "vault_based");
        startActivity(intent);
    }

    /**
     * Handle instant payment selection
     */
    private void handleInstantPayment() {
        if (!validatePaymentDetails()) {
            return;
        }

        // Navigate directly to payment confirmation with default vault
        int defaultVaultId = sessionManager.getDefaultInstantVaultId();

        if (defaultVaultId == -1) {
            showError("No default vault set for instant pay. Please use vault-based payment.");
            return;
        }

        Intent intent = new Intent(PaymentEntryActivity.this, PaymentConfirmationActivity.class);
        intent.putExtra("merchant_name", etMerchantName.getText().toString().trim());
        intent.putExtra("amount", Double.parseDouble(etAmount.getText().toString().trim()));
        intent.putExtra("description", etDescription.getText().toString().trim());
        intent.putExtra("vault_id", defaultVaultId);
        intent.putExtra("payment_method", "instant_pay");
        startActivity(intent);
    }

    /**
     * Handle emergency payment selection
     */
    private void handleEmergencyPayment() {
        if (!validatePaymentDetails()) {
            return;
        }

        // Get emergency vault
        int emergencyVaultId = sessionManager.getEmergencyVaultId();

        if (emergencyVaultId == -1) {
            showError("Emergency vault not found. Please contact support.");
            return;
        }

        // Show warning dialog first
        showEmergencyWarningDialog(emergencyVaultId);
    }

    /**
     * Show emergency vault warning dialog
     */
    private void showEmergencyWarningDialog(int emergencyVaultId) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("⚠️ Emergency Vault Access")
                .setMessage(getString(R.string.emergency_warning_message))
                .setPositiveButton("Proceed", (dialog, which) -> {
                    // Navigate to payment confirmation with emergency vault
                    Intent intent = new Intent(PaymentEntryActivity.this, PaymentConfirmationActivity.class);
                    intent.putExtra("merchant_name", etMerchantName.getText().toString().trim());
                    intent.putExtra("amount", Double.parseDouble(etAmount.getText().toString().trim()));
                    intent.putExtra("description", etDescription.getText().toString().trim());
                    intent.putExtra("vault_id", emergencyVaultId);
                    intent.putExtra("payment_method", "emergency");
                    intent.putExtra("is_emergency", true);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}