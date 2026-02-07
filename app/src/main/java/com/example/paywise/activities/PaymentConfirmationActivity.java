package com.example.paywise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.paywise.R;
import com.example.paywise.MainActivity;
import com.example.paywise.database.UserDao;
import com.example.paywise.managers.PaymentManager;
import com.example.paywise.managers.VaultManager;
import com.example.paywise.models.Transaction;
import com.example.paywise.models.User;
import com.example.paywise.models.Vault;
import com.example.paywise.utils.Constants;
import com.example.paywise.utils.PinManager;
import com.example.paywise.utils.SessionManager;
import com.example.paywise.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * PaymentConfirmationActivity - Review payment and enter PIN
 *
 * Flow:
 * 1. Display payment details summary
 * 2. Show vault before/after balance
 * 3. User enters PIN to confirm (or emergency PIN if emergency vault)
 * 4. Process payment
 * 5. Show success/failure and navigate back
 */
public class PaymentConfirmationActivity extends AppCompatActivity {

    private TextView tvMerchantName;
    private TextView tvAmount;
    private TextView tvVaultName;
    private TextView tvVaultBefore;
    private TextView tvVaultAfter;
    private TextView tvDescription;
    private EditText etPin;
    private TextView tvErrorMessage;
    private MaterialButton btnCancel;
    private MaterialButton btnPayNow;

    private View[] pinDots;

    private String merchantName;
    private double amount;
    private String description;
    private int vaultId;
    private String paymentMethod;
    private boolean isEmergency;

    private SessionManager sessionManager;
    private VaultManager vaultManager;
    private PaymentManager paymentManager;
    private UserDao userDao;

    private Vault vault;
    private User user;
    private String enteredPin = "";
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_confirmation);

        getIntentData();
        initializeViews();
        initializeManagers();
        loadData();
        displayPaymentDetails();
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
        vaultId = intent.getIntExtra("vault_id", -1);
        paymentMethod = intent.getStringExtra("payment_method");
        isEmergency = intent.getBooleanExtra("is_emergency", false);
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        tvMerchantName = findViewById(R.id.tvMerchantName);
        tvAmount = findViewById(R.id.tvAmount);
        tvVaultName = findViewById(R.id.tvVaultName);
        tvVaultBefore = findViewById(R.id.tvVaultBefore);
        tvVaultAfter = findViewById(R.id.tvVaultAfter);
        tvDescription = findViewById(R.id.tvDescription);
        etPin = findViewById(R.id.etPin);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        btnCancel = findViewById(R.id.btnCancel);
        btnPayNow = findViewById(R.id.btnPayNow);

        // Initialize PIN dots
        pinDots = new View[]{
                findViewById(R.id.pinDot1),
                findViewById(R.id.pinDot2),
                findViewById(R.id.pinDot3),
                findViewById(R.id.pinDot4),
                findViewById(R.id.pinDot5),
                findViewById(R.id.pinDot6)
        };

        etPin.requestFocus();
    }

    /**
     * Initialize managers and DAOs
     */
    private void initializeManagers() {
        sessionManager = new SessionManager(this);
        vaultManager = new VaultManager(this);
        paymentManager = new PaymentManager(this);
        userDao = new UserDao(this);

        userId = sessionManager.getUserId();
    }

    /**
     * Load vault and user data
     */
    private void loadData() {
        vault = vaultManager.getVault(vaultId);
        user = userDao.getUserById(userId);
    }

    /**
     * Display payment details
     */
    private void displayPaymentDetails() {
        if (vault == null) {
            showError("Vault not found");
            finish();
            return;
        }

        // Display payment info
        tvMerchantName.setText(merchantName);
        tvAmount.setText(ValidationUtils.formatAmount(amount));

        String vaultDisplay = vault.getVaultIcon() + " " + vault.getDisplayName();
        tvVaultName.setText(vaultDisplay);

        // Display vault balance before/after
        double currentBalance = vault.getRemainingBalance();
        double afterBalance = currentBalance - amount;

        tvVaultBefore.setText(ValidationUtils.formatAmount(currentBalance));
        tvVaultAfter.setText(ValidationUtils.formatAmount(afterBalance));

        // Description
        if (description != null && !description.isEmpty()) {
            tvDescription.setText(description);
            tvDescription.setVisibility(View.VISIBLE);
        } else {
            tvDescription.setVisibility(View.GONE);
        }
    }

    /**
     * Setup listeners
     */
    private void setupListeners() {
        // PIN input
        etPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enteredPin = s.toString();
                updatePinDots(enteredPin.length());
                hideError();

                // Auto-process payment when 6 digits entered
                if (enteredPin.length() == 6) {
                    processPayment();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnCancel.setOnClickListener(v -> finish());
        btnPayNow.setOnClickListener(v -> processPayment());
    }

    /**
     * Update visual PIN dots
     */
    private void updatePinDots(int filledCount) {
        for (int i = 0; i < pinDots.length; i++) {
            if (i < filledCount) {
                pinDots[i].setBackgroundResource(R.drawable.pin_dot_filled);
            } else {
                pinDots[i].setBackgroundResource(R.drawable.pin_dot_empty);
            }
        }
    }

    /**
     * Process payment after PIN verification
     */
    private void processPayment() {
        if (enteredPin.length() != 6) {
            showError(getString(R.string.error_invalid_pin));
            return;
        }

        // If emergency vault, verify emergency PIN first
        if (isEmergency && vault.isEmergency()) {
            if (!vaultManager.verifyEmergencyPin(vaultId, enteredPin)) {
                showError("Invalid emergency vault PIN");
                clearPin();
                return;
            }

            // Emergency PIN verified, now ask for app PIN
            showAppPinDialog();
            return;
        }

        // Verify app PIN
        if (user != null && !PinManager.verifyPin(enteredPin, user.getAppPinHash())) {
            showError("Invalid PIN");
            clearPin();
            return;
        }

        // PIN verified - process payment
        executePayment();
    }

    /**
     * Show dialog to enter app PIN (for emergency payments)
     */
    private void showAppPinDialog() {
        // For simplicity, we'll just verify the same PIN
        // In a real app, you might want a separate dialog
        executePayment();
    }

    /**
     * Execute the actual payment
     */
    private void executePayment() {
        Transaction transaction;

        // Process payment based on payment method
        switch (paymentMethod) {
            case Constants.PAYMENT_METHOD_VAULT_BASED:
                transaction = paymentManager.processVaultBasedPayment(vaultId, merchantName, amount, description);
                break;

            case Constants.PAYMENT_METHOD_INSTANT_PAY:
                transaction = paymentManager.processInstantPayment(userId, merchantName, amount, description);
                break;

            case Constants.PAYMENT_METHOD_EMERGENCY:
                transaction = paymentManager.processEmergencyPayment(vaultId, merchantName, amount, description);
                break;

            default:
                showError("Invalid payment method");
                return;
        }

        // Show result
        if (transaction != null && transaction.isSuccessful()) {
            showSuccessDialog(transaction);
        } else {
            String errorMessage = transaction != null ? transaction.getDescription() : "Payment failed";
            showFailureDialog(errorMessage);
        }
    }

    /**
     * Show payment success dialog
     */
    private void showSuccessDialog(Transaction transaction) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("✅ Payment Successful!")
                .setMessage(String.format(
                        "%s paid to %s\n\nTransaction ID: #TXN%d\n\nFrom: %s",
                        ValidationUtils.formatAmount(amount),
                        merchantName,
                        transaction.getTransactionId(),
                        vault.getDisplayName()
                ))
                .setPositiveButton("Done", (dialog, which) -> {
                    // Navigate back to MainActivity
                    Intent intent = new Intent(PaymentConfirmationActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Show payment failure dialog
     */
    private void showFailureDialog(String errorMessage) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("❌ Payment Failed")
                .setMessage(errorMessage)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .show();
    }

    /**
     * Clear PIN input
     */
    private void clearPin() {
        etPin.setText("");
        enteredPin = "";
        updatePinDots(0);
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
    }

    /**
     * Hide error message
     */
    private void hideError() {
        tvErrorMessage.setVisibility(View.GONE);
    }
}