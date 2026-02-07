package com.example.paywise.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.paywise.R;
import com.example.paywise.managers.VaultManager;
import com.example.paywise.utils.Constants;
import com.example.paywise.utils.SessionManager;
import com.example.paywise.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * VaultManagementActivity - Create or edit vault
 *
 * Features:
 * - Create standard vaults (Food, Travel, Lifestyle, Business)
 * - Create custom vaults with custom icon and color
 * - Set monthly spending limit
 */
public class VaultManagementActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etVaultName;
    private RadioGroup radioGroupVaultType;
    private RadioButton radioFood;
    private RadioButton radioTravel;
    private RadioButton radioLifestyle;
    private RadioButton radioBusiness;
    private RadioButton radioCustom;
    private LinearLayout layoutCustomCategory;
    private EditText etCustomCategory;
    private EditText etMonthlyLimit;
    private MaterialButton btnSaveVault;

    private SessionManager sessionManager;
    private VaultManager vaultManager;

    private String selectedIcon = "";
    private String selectedColor = "";
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault_management);

        initializeViews();
        initializeManagers();
        setupListeners();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        etVaultName = findViewById(R.id.etVaultName);
        radioGroupVaultType = findViewById(R.id.radioGroupVaultType);
        radioFood = findViewById(R.id.radioFood);
        radioTravel = findViewById(R.id.radioTravel);
        radioLifestyle = findViewById(R.id.radioLifestyle);
        radioBusiness = findViewById(R.id.radioBusiness);
        radioCustom = findViewById(R.id.radioCustom);
        layoutCustomCategory = findViewById(R.id.layoutCustomCategory);
        etCustomCategory = findViewById(R.id.etCustomCategory);
        etMonthlyLimit = findViewById(R.id.etMonthlyLimit);
        btnSaveVault = findViewById(R.id.btnSaveVault);
    }

    /**
     * Initialize managers
     */
    private void initializeManagers() {
        sessionManager = new SessionManager(this);
        vaultManager = new VaultManager(this);
        userId = sessionManager.getUserId();
    }

    /**
     * Setup listeners
     */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        // Radio group listener - show/hide custom category section
        radioGroupVaultType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioCustom) {
                layoutCustomCategory.setVisibility(View.VISIBLE);
            } else {
                layoutCustomCategory.setVisibility(View.GONE);
            }
        });

        // Icon selection listeners
        setupIconListeners();

        // Color selection listeners
        setupColorListeners();

        btnSaveVault.setOnClickListener(v -> validateAndSaveVault());
    }

    /**
     * Setup icon selection listeners
     */
    private void setupIconListeners() {
        TextView iconHealthcare = findViewById(R.id.iconHealthcare);
        TextView iconEducation = findViewById(R.id.iconEducation);
        TextView iconHome = findViewById(R.id.iconHome);
        TextView iconEntertainment = findViewById(R.id.iconEntertainment);
        TextView iconMedical = findViewById(R.id.iconMedical);
        TextView iconVehicle = findViewById(R.id.iconVehicle);

        View.OnClickListener iconClickListener = v -> {
            selectedIcon = ((TextView) v).getText().toString();
            // Highlight selected icon (add visual feedback)
            v.setSelected(true);
        };

        iconHealthcare.setOnClickListener(iconClickListener);
        iconEducation.setOnClickListener(iconClickListener);
        iconHome.setOnClickListener(iconClickListener);
        iconEntertainment.setOnClickListener(iconClickListener);
        iconMedical.setOnClickListener(iconClickListener);
        iconVehicle.setOnClickListener(iconClickListener);
    }

    /**
     * Setup color selection listeners
     */
    private void setupColorListeners() {
        View colorRed = findViewById(R.id.colorRed);
        View colorBlue = findViewById(R.id.colorBlue);
        View colorGreen = findViewById(R.id.colorGreen);
        View colorYellow = findViewById(R.id.colorYellow);
        View colorPurple = findViewById(R.id.colorPurple);
        View colorOrange = findViewById(R.id.colorOrange);

        View.OnClickListener colorClickListener = v -> {
            selectedColor = getColorFromView(v);
            // Highlight selected color
            v.setSelected(true);
        };

        colorRed.setOnClickListener(colorClickListener);
        colorBlue.setOnClickListener(colorClickListener);
        colorGreen.setOnClickListener(colorClickListener);
        colorYellow.setOnClickListener(colorClickListener);
        colorPurple.setOnClickListener(colorClickListener);
        colorOrange.setOnClickListener(colorClickListener);
    }

    /**
     * Get color hex from view ID
     */
    private String getColorFromView(View view) {
        int id = view.getId();
        if (id == R.id.colorRed) return Constants.COLOR_FOOD;
        else if (id == R.id.colorBlue) return Constants.COLOR_BLUE;
        else if (id == R.id.colorGreen) return Constants.COLOR_GREEN;
        else if (id == R.id.colorYellow) return Constants.COLOR_YELLOW;
        else if (id == R.id.colorPurple) return Constants.COLOR_PURPLE;
        else if (id == R.id.colorOrange) return Constants.COLOR_ORANGE;
        return Constants.COLOR_LIFESTYLE;
    }

    /**
     * Validate and save vault
     */
    private void validateAndSaveVault() {
        String vaultName = etVaultName.getText().toString().trim();
        String limitStr = etMonthlyLimit.getText().toString().trim();

        // Validate vault name
        if (!ValidationUtils.isValidVaultName(vaultName)) {
            etVaultName.setError(getString(R.string.error_empty_vault_name));
            return;
        }

        // Validate monthly limit
        if (!ValidationUtils.isValidAmount(limitStr)) {
            etMonthlyLimit.setError(getString(R.string.error_invalid_limit));
            return;
        }

        double monthlyLimit = Double.parseDouble(limitStr);

        // Get selected vault type
        int selectedId = radioGroupVaultType.getCheckedRadioButtonId();

        long vaultId;

        if (selectedId == R.id.radioCustom) {
            // Create custom vault
            String customCategory = etCustomCategory.getText().toString().trim();

            if (customCategory.isEmpty()) {
                etCustomCategory.setError("Custom category name is required");
                return;
            }

            if (selectedIcon.isEmpty()) {
                showError("Please select an icon");
                return;
            }

            if (selectedColor.isEmpty()) {
                showError("Please select a color");
                return;
            }

            vaultId = vaultManager.createCustomVault(userId, vaultName, customCategory,
                    selectedIcon, monthlyLimit, selectedColor);
        } else {
            // Create standard vault
            String vaultType = getVaultTypeFromRadio(selectedId);
            String icon = getIconForVaultType(vaultType);
            String color = getColorForVaultType(vaultType);

            // Check if vault type already exists
            if (vaultManager.vaultTypeExists(userId, vaultType)) {
                showError(getString(R.string.error_vault_type_exists));
                return;
            }

            vaultId = vaultManager.createVault(userId, vaultName, vaultType, icon, monthlyLimit, color);
        }

        if (vaultId > 0) {
            showSuccess("Vault created successfully!");
        } else {
            showError("Failed to create vault");
        }
    }

    /**
     * Get vault type from selected radio button
     */
    private String getVaultTypeFromRadio(int radioId) {
        if (radioId == R.id.radioFood) return Constants.VAULT_TYPE_FOOD;
        else if (radioId == R.id.radioTravel) return Constants.VAULT_TYPE_TRAVEL;
        else if (radioId == R.id.radioLifestyle) return Constants.VAULT_TYPE_LIFESTYLE;
        else if (radioId == R.id.radioBusiness) return Constants.VAULT_TYPE_BUSINESS;
        return Constants.VAULT_TYPE_CUSTOM;
    }

    /**
     * Get default icon for vault type
     */
    private String getIconForVaultType(String vaultType) {
        switch (vaultType) {
            case Constants.VAULT_TYPE_FOOD: return Constants.ICON_FOOD;
            case Constants.VAULT_TYPE_TRAVEL: return Constants.ICON_TRAVEL;
            case Constants.VAULT_TYPE_LIFESTYLE: return Constants.ICON_LIFESTYLE;
            case Constants.VAULT_TYPE_BUSINESS: return Constants.ICON_BUSINESS;
            default: return "📊";
        }
    }

    /**
     * Get default color for vault type
     */
    private String getColorForVaultType(String vaultType) {
        switch (vaultType) {
            case Constants.VAULT_TYPE_FOOD: return Constants.COLOR_FOOD;
            case Constants.VAULT_TYPE_TRAVEL: return Constants.COLOR_TRAVEL;
            case Constants.VAULT_TYPE_LIFESTYLE: return Constants.COLOR_LIFESTYLE;
            case Constants.VAULT_TYPE_BUSINESS: return Constants.COLOR_BUSINESS;
            default: return Constants.COLOR_LIFESTYLE;
        }
    }

    /**
     * Show success message and close
     */
    private void showSuccess(String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Success")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> finish())
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