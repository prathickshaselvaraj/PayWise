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
import com.example.paywise.models.User;
import com.example.paywise.utils.PinManager;
import com.example.paywise.utils.SessionManager;

/**
 * LoginActivity - Returning user PIN authentication
 *
 * Flow:
 * 1. Display user name and masked mobile number
 * 2. User enters 6-digit PIN
 * 3. Validate PIN against stored hash
 * 4. Track failed attempts (max 3)
 * 5. Lock account for 30 seconds after 3 failed attempts
 * 6. Navigate to MainActivity on success
 */
public class LoginActivity extends AppCompatActivity {

    private TextView tvWelcomeBack;
    private TextView tvMobileNumber;
    private EditText etPin;
    private TextView tvErrorMessage;
    private TextView tvForgotPin;

    private View[] pinDots;

    private UserDao userDao;
    private SessionManager sessionManager;
    private User user;

    private String enteredPin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        initializeDAOs();
        loadUserData();
        setupListeners();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        tvWelcomeBack = findViewById(R.id.tvWelcomeBack);
        tvMobileNumber = findViewById(R.id.tvMobileNumber);
        etPin = findViewById(R.id.etPin);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        tvForgotPin = findViewById(R.id.tvForgotPin);

        // Initialize PIN dots
        pinDots = new View[]{
                findViewById(R.id.pinDot1),
                findViewById(R.id.pinDot2),
                findViewById(R.id.pinDot3),
                findViewById(R.id.pinDot4),
                findViewById(R.id.pinDot5),
                findViewById(R.id.pinDot6)
        };

        // Focus on PIN input
        etPin.requestFocus();
    }

    /**
     * Initialize DAOs
     */
    private void initializeDAOs() {
        userDao = new UserDao(this);
        sessionManager = new SessionManager(this);
    }

    /**
     * Load user data and display
     */
    private void loadUserData() {
        user = userDao.getFirstUser();

        if (user != null) {
            tvWelcomeBack.setText("Welcome back, " + user.getFullName().split(" ")[0] + "!");
            tvMobileNumber.setText(user.getFormattedMobileNumber());
        }
    }

    /**
     * Setup PIN input listener
     */
    private void setupListeners() {
        etPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enteredPin = s.toString();
                updatePinDots(enteredPin.length());
                hideError();

                // Auto-validate when 6 digits entered
                if (enteredPin.length() == 6) {
                    validatePin();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        tvForgotPin.setOnClickListener(v -> {
            // TODO: Implement forgot PIN flow
            showError("Forgot PIN feature coming soon");
        });
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
     * Validate entered PIN
     */
    private void validatePin() {
        if (user == null) {
            showError("User not found");
            return;
        }

        // Check if account is locked
        if (userDao.isAccountLocked(user.getUserId())) {
            long remainingSeconds = sessionManager.getLockoutTimeRemaining();
            showError(String.format(getString(R.string.error_account_locked), remainingSeconds));
            clearPin();
            return;
        }

        // Verify PIN
        if (PinManager.verifyPin(enteredPin, user.getAppPinHash())) {
            // PIN correct - reset failed attempts and login
            userDao.resetFailedAttempts(user.getUserId());

            // Create session
            sessionManager.createSession(user.getUserId(), user.getFullName(), user.getMobileNumber());
            if (user.getProfileImagePath() != null) {
                sessionManager.saveProfileImage(user.getProfileImagePath());
            }

            // Navigate to main activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } else {
            // PIN incorrect - increment failed attempts
            userDao.incrementFailedAttempts(user.getUserId());

            // Reload user to get updated failed attempts
            user = userDao.getUserById(user.getUserId());

            int remainingAttempts = 3 - user.getFailedAttempts();

            if (remainingAttempts > 0) {
                showError(String.format(getString(R.string.error_wrong_pin), remainingAttempts));
            } else {
                sessionManager.setLockout(30000); // 30 seconds
                showError("Account locked for 30 seconds");
            }

            clearPin();
        }
    }

    /**
     * Clear PIN input and dots
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

    @Override
    public void onBackPressed() {
        // Exit app on back press from login
        finishAffinity();
    }
}