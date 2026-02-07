package com.example.paywise.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.paywise.R;
import com.example.paywise.database.BankAccountDao;
import com.example.paywise.database.UserDao;
import com.example.paywise.database.VaultDao;
import com.example.paywise.managers.VaultManager;
import com.example.paywise.models.BankAccount;
import com.example.paywise.models.User;
import com.example.paywise.utils.Constants;
import com.example.paywise.utils.DateUtils;
import com.example.paywise.utils.ImageUtils;
import com.example.paywise.utils.PinManager;
import com.example.paywise.utils.SessionManager;
import com.example.paywise.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;

/**
 * ProfileSetupActivity - Final setup step with profile info
 *
 * Flow:
 * 1. User uploads profile photo (optional)
 * 2. Enters full name and email
 * 3. Save all data to database (User, BankAccount, Emergency Vault)
 * 4. Create session and navigate to MainActivity
 */
public class ProfileSetupActivity extends AppCompatActivity {

    private ImageView ivProfilePhoto;
    private ImageView ivCameraIcon;
    private TextView tvUploadPhoto;
    private EditText etFullName;
    private EditText etEmail;
    private TextView tvErrorMessage;
    private MaterialButton btnCompleteSetup;

    private String mobileNumber;
    private String bankName;
    private String accountNumber;
    private String accountHolderName;
    private String ifscCode;
    private String appPin;

    private Uri selectedImageUri;
    private String savedImagePath;

    private UserDao userDao;
    private BankAccountDao bankAccountDao;
    private VaultManager vaultManager;
    private SessionManager sessionManager;

    // Image picker launcher
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        // Get data from previous screens
        Intent intent = getIntent();
        mobileNumber = intent.getStringExtra("mobile_number");
        bankName = intent.getStringExtra("bank_name");
        accountNumber = intent.getStringExtra("account_number");
        accountHolderName = intent.getStringExtra("account_holder_name");
        ifscCode = intent.getStringExtra("ifsc_code");
        appPin = intent.getStringExtra("app_pin");

        initializeViews();
        initializeDAOs();
        setupImagePicker();
        setupListeners();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        ivCameraIcon = findViewById(R.id.ivCameraIcon);
        tvUploadPhoto = findViewById(R.id.tvUploadPhoto);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        btnCompleteSetup = findViewById(R.id.btnCompleteSetup);
    }

    /**
     * Initialize DAOs and managers
     */
    private void initializeDAOs() {
        userDao = new UserDao(this);
        bankAccountDao = new BankAccountDao(this);
        vaultManager = new VaultManager(this);
        sessionManager = new SessionManager(this);
    }

    /**
     * Setup image picker launcher
     */
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            // Display selected image
                            ivProfilePhoto.setImageURI(selectedImageUri);
                        }
                    }
                }
        );
    }

    /**
     * Setup click listeners
     */
    private void setupListeners() {
        // Photo upload
        View.OnClickListener photoClickListener = v -> openImagePicker();
        ivProfilePhoto.setOnClickListener(photoClickListener);
        ivCameraIcon.setOnClickListener(photoClickListener);
        tvUploadPhoto.setOnClickListener(photoClickListener);

        // Complete setup button
        btnCompleteSetup.setOnClickListener(v -> validateAndCompleteSetup());
    }

    /**
     * Open image picker
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    /**
     * Validate input and complete setup
     */
    private void validateAndCompleteSetup() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        // Validate name
        if (!ValidationUtils.isValidName(fullName)) {
            showError(getString(R.string.error_empty_name));
            return;
        }

        // Validate email (optional, but if provided must be valid)
        if (!ValidationUtils.isValidEmail(email)) {
            showError("Please enter a valid email address");
            return;
        }

        // Save profile image if selected
        if (selectedImageUri != null) {
            String fileName = "profile_" + System.currentTimeMillis() + ".jpg";
            savedImagePath = ImageUtils.saveImageToInternalStorage(this, selectedImageUri, fileName);
        }

        // Save all data to database
        boolean success = saveUserData(fullName, email);

        if (success) {
            // Navigate to main activity
            Intent intent = new Intent(ProfileSetupActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            showError("Failed to complete setup. Please try again.");
        }
    }

    /**
     * Save user, bank account, and create emergency vault
     */
    private boolean saveUserData(String fullName, String email) {
        try {
            String currentDateTime = DateUtils.getCurrentDateTime();

            // 1. Create and save User
            String hashedPin = PinManager.hashPin(appPin);
            User user = new User(fullName, mobileNumber, email, hashedPin,
                    savedImagePath, currentDateTime, currentDateTime);
            long userId = userDao.insertUser(user);

            if (userId == -1) {
                return false;
            }

            // 2. Create and save Bank Account
            BankAccount bankAccount = new BankAccount((int) userId, bankName, accountNumber,
                    accountHolderName, ifscCode, currentDateTime);
            long accountId = bankAccountDao.insertBankAccount(bankAccount);

            if (accountId == -1) {
                return false;
            }

            // 3. Create Emergency Vault (auto-created during setup)
            // For now, use same PIN for emergency vault (can be changed later)
            long emergencyVaultId = vaultManager.createEmergencyVault((int) userId, appPin);

            if (emergencyVaultId == -1) {
                return false;
            }

            // 4. Create default Lifestyle vault for instant pay
            long lifestyleVaultId = vaultManager.createVault(
                    (int) userId,
                    "Lifestyle Vault",
                    Constants.VAULT_TYPE_LIFESTYLE,
                    Constants.ICON_LIFESTYLE,
                    5000.0, // Default limit
                    Constants.COLOR_LIFESTYLE
            );

            if (lifestyleVaultId > 0) {
                // Set as default for instant pay
                vaultManager.setDefaultInstantPayVault((int) lifestyleVaultId, (int) userId);
            }

            // 5. Create session
            sessionManager.createSession((int) userId, fullName, mobileNumber);
            sessionManager.setHasBankAccount(true);
            sessionManager.setEmergencyVaultId((int) emergencyVaultId);
            if (lifestyleVaultId > 0) {
                sessionManager.setDefaultInstantVaultId((int) lifestyleVaultId);
            }
            if (savedImagePath != null) {
                sessionManager.saveProfileImage(savedImagePath);
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        // Allow back navigation during profile setup
        super.onBackPressed();
    }
}