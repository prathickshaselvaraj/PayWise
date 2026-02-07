package com.example.paywise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.paywise.R;
import com.example.paywise.utils.Constants;
import com.example.paywise.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;

/**
 * BankDetailsActivity - Link bank account during setup
 *
 * Flow:
 * 1. User selects bank from spinner
 * 2. Enters account number, holder name, IFSC code
 * 3. Validate all fields
 * 4. Navigate to PinSetupActivity
 */
public class BankDetailsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Spinner spinnerBankName;
    private EditText etAccountNumber;
    private EditText etAccountHolderName;
    private EditText etIfscCode;
    private TextView tvErrorMessage;
    private MaterialButton btnVerifyAccount;

    private String mobileNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_details);

        // Get mobile number from previous screen
        mobileNumber = getIntent().getStringExtra("mobile_number");

        initializeViews();
        setupBankSpinner();
        setupListeners();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        spinnerBankName = findViewById(R.id.spinnerBankName);
        etAccountNumber = findViewById(R.id.etAccountNumber);
        etAccountHolderName = findViewById(R.id.etAccountHolderName);
        etIfscCode = findViewById(R.id.etIfscCode);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        btnVerifyAccount = findViewById(R.id.btnVerifyAccount);
    }

    /**
     * Setup bank name spinner with list of banks
     */
    private void setupBankSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Constants.BANK_NAMES
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBankName.setAdapter(adapter);
    }

    /**
     * Setup click listeners
     */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
        btnVerifyAccount.setOnClickListener(v -> validateAndProceed());

        // Auto-capitalize IFSC code
        etIfscCode.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideError();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String input = s.toString();
                if (!input.equals(input.toUpperCase())) {
                    etIfscCode.removeTextChangedListener(this);
                    etIfscCode.setText(input.toUpperCase());
                    etIfscCode.setSelection(etIfscCode.getText().length());
                    etIfscCode.addTextChangedListener(this);
                }
            }
        });
    }

    /**
     * Validate all bank details and proceed
     */
    private void validateAndProceed() {
        String bankName = spinnerBankName.getSelectedItem().toString();
        String accountNumber = etAccountNumber.getText().toString().trim();
        String accountHolderName = etAccountHolderName.getText().toString().trim();
        String ifscCode = etIfscCode.getText().toString().trim();

        // Validate bank name
        if (bankName.isEmpty() || bankName.equals("Select Bank")) {
            showError(getString(R.string.error_empty_bank_name));
            return;
        }

        // Validate account number
        if (!ValidationUtils.isValidAccountNumber(accountNumber)) {
            showError(getString(R.string.error_invalid_account));
            return;
        }

        // Validate account holder name
        if (!ValidationUtils.isValidName(accountHolderName)) {
            showError("Please enter valid account holder name");
            return;
        }

        // Validate IFSC code
        if (!ValidationUtils.isValidIfscCode(ifscCode)) {
            showError(getString(R.string.error_invalid_ifsc));
            return;
        }

        // All validations passed - navigate to PIN setup
        Intent intent = new Intent(BankDetailsActivity.this, PinSetupActivity.class);
        intent.putExtra("mobile_number", mobileNumber);
        intent.putExtra("bank_name", bankName);
        intent.putExtra("account_number", accountNumber);
        intent.putExtra("account_holder_name", accountHolderName);
        intent.putExtra("ifsc_code", ifscCode);
        startActivity(intent);
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