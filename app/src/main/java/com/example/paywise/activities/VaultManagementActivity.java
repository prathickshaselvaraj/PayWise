
package com.example.paywise.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.paywise.R;
import com.example.paywise.managers.VaultManager;
import com.example.paywise.utils.Constants;
import com.example.paywise.utils.PreferenceManager;
import com.google.android.material.textfield.TextInputEditText;

public class VaultManagementActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputEditText etVaultName, etMonthlyLimit;
    private Spinner spinnerVaultType;
    private Button btnSaveVault;

    private PreferenceManager preferenceManager;
    private VaultManager vaultManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault_management);

        initializeViews();
        setupToolbar();

        preferenceManager = new PreferenceManager(this);
        vaultManager = new VaultManager(this);

        setupSpinner();
        setupClickListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        etVaultName = findViewById(R.id.etVaultName);
        etMonthlyLimit = findViewById(R.id.etMonthlyLimit);
        spinnerVaultType = findViewById(R.id.spinnerVaultType);
        btnSaveVault = findViewById(R.id.btnSaveVault);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Vault");
        }
    }

    private void setupSpinner() {
        String[] vaultTypes = {
                Constants.VAULT_TYPE_FOOD,
                Constants.VAULT_TYPE_TRAVEL,
                Constants.VAULT_TYPE_LIFESTYLE,
                Constants.VAULT_TYPE_BUSINESS,
                Constants.VAULT_TYPE_EMERGENCY
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                vaultTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVaultType.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnSaveVault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSaveVault();
            }
        });
    }

    private void validateAndSaveVault() {
        String vaultName = etVaultName.getText().toString().trim();
        String monthlyLimitStr = etMonthlyLimit.getText().toString().trim();
        String vaultType = spinnerVaultType.getSelectedItem().toString();

        // Validate vault name
        if (TextUtils.isEmpty(vaultName)) {
            etVaultName.setError(getString(R.string.error_empty_vault_name));
            etVaultName.requestFocus();
            return;
        }

        // Validate monthly limit
        if (TextUtils.isEmpty(monthlyLimitStr)) {
            etMonthlyLimit.setError(getString(R.string.error_invalid_limit));
            etMonthlyLimit.requestFocus();
            return;
        }

        double monthlyLimit;
        try {
            monthlyLimit = Double.parseDouble(monthlyLimitStr);
            if (monthlyLimit <= 0) {
                etMonthlyLimit.setError(getString(R.string.error_invalid_limit));
                etMonthlyLimit.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etMonthlyLimit.setError(getString(R.string.error_invalid_limit));
            etMonthlyLimit.requestFocus();
            return;
        }

        // Get vault color based on type
        String vaultColor = getVaultColor(vaultType);

        // Save vault
        int userId = preferenceManager.getUserId();
        long vaultId = vaultManager.createVault(userId, vaultName, vaultType, monthlyLimit, vaultColor);

        if (vaultId == -2) {
            Toast.makeText(this, "Vault type already exists", Toast.LENGTH_SHORT).show();
        } else if (vaultId != -1) {
            Toast.makeText(this, "Vault created successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to create vault", Toast.LENGTH_SHORT).show();
        }
    }

    private String getVaultColor(String vaultType) {
        switch (vaultType) {
            case Constants.VAULT_TYPE_FOOD:
                return "#FF6B6B";
            case Constants.VAULT_TYPE_TRAVEL:
                return "#4ECDC4";
            case Constants.VAULT_TYPE_LIFESTYLE:
                return "#95E1D3";
            case Constants.VAULT_TYPE_BUSINESS:
                return "#F38181";
            case Constants.VAULT_TYPE_EMERGENCY:
                return "#FF8C42";
            default:
                return "#1976D2";
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
