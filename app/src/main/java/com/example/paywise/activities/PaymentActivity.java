
package com.example.paywise.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import com.example.paywise.R;
import com.example.paywise.managers.PaymentManager;
import com.example.paywise.managers.VaultManager;
import com.example.paywise.models.Transaction;
import com.example.paywise.models.Vault;
import com.example.paywise.services.PaymentValidationService;
import com.example.paywise.utils.Constants;
import com.example.paywise.utils.PreferenceManager;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class PaymentActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputEditText etMerchantName, etAmount, etDescription;
    private Spinner spinnerVault;
    private CardView cvVaultInfo;
    private TextView tvVaultLimit, tvVaultSpent, tvVaultRemaining;
    private Button btnPayNow;

    private PreferenceManager preferenceManager;
    private VaultManager vaultManager;
    private PaymentManager paymentManager;

    private List<Vault> vaultList;
    private Vault selectedVault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initializeViews();
        setupToolbar();

        preferenceManager = new PreferenceManager(this);
        vaultManager = new VaultManager(this);
        paymentManager = new PaymentManager(this);

        loadVaults();
        setupSpinner();
        setupClickListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        etMerchantName = findViewById(R.id.etMerchantName);
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        spinnerVault = findViewById(R.id.spinnerVault);
        cvVaultInfo = findViewById(R.id.cvVaultInfo);
        tvVaultLimit = findViewById(R.id.tvVaultLimit);
        tvVaultSpent = findViewById(R.id.tvVaultSpent);
        tvVaultRemaining = findViewById(R.id.tvVaultRemaining);
        btnPayNow = findViewById(R.id.btnPayNow);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadVaults() {
        int userId = preferenceManager.getUserId();
        vaultList = vaultManager.getUserVaults(userId);
    }

    private void setupSpinner() {
        if (vaultList.isEmpty()) {
            Toast.makeText(this, "No vaults available. Please create a vault first.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        List<String> vaultNames = new ArrayList<>();
        for (Vault vault : vaultList) {
            vaultNames.add(vault.getVaultName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                vaultNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVault.setAdapter(adapter);

        spinnerVault.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedVault = vaultList.get(position);
                updateVaultInfo();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedVault = null;
                cvVaultInfo.setVisibility(View.GONE);
            }
        });
    }

    private void updateVaultInfo() {
        if (selectedVault != null) {
            cvVaultInfo.setVisibility(View.VISIBLE);

            tvVaultLimit.setText(String.format("Monthly Limit: ₹%.2f", selectedVault.getMonthlyLimit()));
            tvVaultSpent.setText(String.format("Spent: ₹%.2f", selectedVault.getCurrentSpent()));

            double remaining = selectedVault.getRemainingBalance();
            tvVaultRemaining.setText(String.format("Remaining: ₹%.2f", remaining));

            // Change color based on remaining balance
            if (remaining <= 0) {
                tvVaultRemaining.setTextColor(getResources().getColor(R.color.statusFailed));
            } else if (selectedVault.getSpendingPercentage() >= 70) {
                tvVaultRemaining.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                tvVaultRemaining.setTextColor(getResources().getColor(R.color.statusSuccess));
            }
        }
    }

    private void setupClickListeners() {
        btnPayNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processPayment();
            }
        });
    }

    private void processPayment() {
        String merchantName = etMerchantName.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // Validate merchant name
        if (TextUtils.isEmpty(merchantName)) {
            etMerchantName.setError("Merchant name is required");
            etMerchantName.requestFocus();
            return;
        }

        // Validate amount
        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError("Amount is required");
            etAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etAmount.setError("Invalid amount");
                etAmount.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
            etAmount.requestFocus();
            return;
        }

        if (selectedVault == null) {
            Toast.makeText(this, "Please select a vault", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if emergency vault - show confirmation dialog
        if (selectedVault.getVaultType().equals(Constants.VAULT_TYPE_EMERGENCY)) {
            showEmergencyConfirmationDialog(merchantName, amount, description);
        } else {
            performPayment(merchantName, amount, description);
        }
    }

    private void showEmergencyConfirmationDialog(final String merchantName, final double amount, final String description) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.emergency_warning_title)
                .setMessage(R.string.emergency_warning_message)
                .setPositiveButton(R.string.btn_proceed, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performPayment(merchantName, amount, description);
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .setIcon(R.drawable.ic_vault_emergency)
                .show();
    }

    private void performPayment(String merchantName, double amount, String description) {
        // Start foreground service for payment validation
        Intent serviceIntent = new Intent(this, PaymentValidationService.class);
        serviceIntent.putExtra("merchant_name", merchantName);
        serviceIntent.putExtra("amount", amount);
        startService(serviceIntent);

        // Process payment
        Transaction transaction = paymentManager.processPayment(
                selectedVault.getVaultId(),
                merchantName,
                amount,
                description
        );

        // Stop foreground service
        stopService(serviceIntent);

        // Show result
        if (transaction.getStatus().equals(Constants.TRANSACTION_STATUS_SUCCESS)) {
            Toast.makeText(this, getString(R.string.payment_success), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, getString(R.string.error_insufficient_balance), Toast.LENGTH_SHORT).show();
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
