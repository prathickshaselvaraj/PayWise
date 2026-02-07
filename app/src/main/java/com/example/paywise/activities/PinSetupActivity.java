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
import com.example.paywise.utils.PinManager;
import com.example.paywise.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;

/**
 * PinSetupActivity - Create 6-digit security PIN
 *
 * Flow:
 * 1. User enters 6-digit PIN
 * 2. User confirms PIN
 * 3. Validate both PINs match
 * 4. Navigate to ProfileSetupActivity
 */
public class PinSetupActivity extends AppCompatActivity {

    private EditText etPin;
    private EditText etConfirmPin;
    private TextView tvErrorMessage;
    private MaterialButton btnCreatePin;

    // PIN dots
    private View[] pinDots;
    private View[] confirmPinDots;

    private String mobileNumber;
    private String bankName;
    private String accountNumber;
    private String accountHolderName;
    private String ifscCode;

    private String enteredPin = "";
    private String confirmedPin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_setup);

        // Get data from previous screen
        Intent intent = getIntent();
        mobileNumber = intent.getStringExtra("mobile_number");
        bankName = intent.getStringExtra("bank_name");
        accountNumber = intent.getStringExtra("account_number");
        accountHolderName = intent.getStringExtra("account_holder_name");
        ifscCode = intent.getStringExtra("ifsc_code");

        initializeViews();
        setupListeners();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        etPin = findViewById(R.id.etPin);
        etConfirmPin = findViewById(R.id.etConfirmPin);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        btnCreatePin = findViewById(R.id.btnCreatePin);

        // Initialize PIN dots arrays
        pinDots = new View[]{
                findViewById(R.id.pinDot1),
                findViewById(R.id.pinDot2),
                findViewById(R.id.pinDot3),
                findViewById(R.id.pinDot4),
                findViewById(R.id.pinDot5),
                findViewById(R.id.pinDot6)
        };

        confirmPinDots = new View[]{
                findViewById(R.id.confirmPinDot1),
                findViewById(R.id.confirmPinDot2),
                findViewById(R.id.confirmPinDot3),
                findViewById(R.id.confirmPinDot4),
                findViewById(R.id.confirmPinDot5),
                findViewById(R.id.confirmPinDot6)
        };

        // Focus on first PIN input
        etPin.requestFocus();
    }

    /**
     * Setup text change and click listeners
     */
    private void setupListeners() {
        // PIN input listener
        etPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enteredPin = s.toString();
                updatePinDots(pinDots, enteredPin.length());
                hideError();

                // Auto-focus on confirm PIN when 6 digits entered
                if (enteredPin.length() == 6) {
                    etConfirmPin.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Confirm PIN input listener
        etConfirmPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                confirmedPin = s.toString();
                updatePinDots(confirmPinDots, confirmedPin.length());
                hideError();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Create PIN button
        btnCreatePin.setOnClickListener(v -> validateAndProceed());
    }

    /**
     * Update visual PIN dots based on entered digits
     */
    private void updatePinDots(View[] dots, int filledCount) {
        for (int i = 0; i < dots.length; i++) {
            if (i < filledCount) {
                dots[i].setBackgroundResource(R.drawable.pin_dot_filled);
            } else {
                dots[i].setBackgroundResource(R.drawable.pin_dot_empty);
            }
        }
    }

    /**
     * Validate PINs and proceed to profile setup
     */
    private void validateAndProceed() {
        // Validate PIN format
        if (!ValidationUtils.isValidPin(enteredPin)) {
            showError(getString(R.string.error_invalid_pin));
            return;
        }

        // Validate confirm PIN format
        if (!ValidationUtils.isValidPin(confirmedPin)) {
            showError(getString(R.string.error_invalid_pin));
            return;
        }

        // Check if PINs match
        if (!enteredPin.equals(confirmedPin)) {
            showError(getString(R.string.error_pin_mismatch));
            return;
        }

        // Check for weak PIN
        if (PinManager.isWeakPin(enteredPin)) {
            showError("PIN is too weak. Avoid sequential or repeated digits.");
            return;
        }

        // All validations passed - navigate to profile setup
        Intent intent = new Intent(PinSetupActivity.this, ProfileSetupActivity.class);
        intent.putExtra("mobile_number", mobileNumber);
        intent.putExtra("bank_name", bankName);
        intent.putExtra("account_number", accountNumber);
        intent.putExtra("account_holder_name", accountHolderName);
        intent.putExtra("ifsc_code", ifscCode);
        intent.putExtra("app_pin", enteredPin); // Pass unhashed PIN (will be hashed when saved)
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