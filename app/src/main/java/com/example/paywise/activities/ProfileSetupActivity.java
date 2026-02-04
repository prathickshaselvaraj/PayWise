package com.example.paywise.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.paywise.R;
import com.example.paywise.database.UserDao;
import com.example.paywise.models.User;
import com.example.paywise.utils.Constants;
import com.example.paywise.utils.DateUtils;
import com.example.paywise.utils.ImageUtils;
import com.example.paywise.utils.PreferenceManager;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileSetupActivity extends AppCompatActivity {

    private ImageView ivProfileImage;
    private TextInputEditText etFullName, etEmail, etPhone;
    private Button btnUploadPhoto, btnContinue;

    private Uri selectedImageUri;
    private String savedImagePath;
    private PreferenceManager preferenceManager;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        initializeViews();
        preferenceManager = new PreferenceManager(this);
        userDao = new UserDao(this);

        setupClickListeners();
    }

    private void initializeViews() {
        ivProfileImage = findViewById(R.id.ivProfileImage);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        btnContinue = findViewById(R.id.btnContinue);
    }

    private void setupClickListeners() {
        btnUploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSaveProfile();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, Constants.REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_IMAGE_PICK
                && resultCode == RESULT_OK
                && data != null) {

            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), selectedImageUri);
                    ivProfileImage.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void validateAndSaveProfile() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Validate full name
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError(getString(R.string.error_empty_name));
            etFullName.requestFocus();
            return;
        }

        // Validate phone
        if (TextUtils.isEmpty(phone) || phone.length() != 10) {
            etPhone.setError(getString(R.string.error_invalid_phone));
            etPhone.requestFocus();
            return;
        }

        // Save profile image if selected
        if (selectedImageUri != null) {
            String fileName = "profile_" + System.currentTimeMillis() + ".jpg";
            savedImagePath = ImageUtils.saveImageToInternalStorage(
                    this, selectedImageUri, fileName);
        }

        // Create user object
        String currentDateTime = DateUtils.getCurrentDateTime();
        User user = new User(
                fullName,
                email,
                phone,
                savedImagePath,
                currentDateTime,
                currentDateTime
        );

        // Insert user into database
        long userId = userDao.insertUser(user);

        if (userId != -1) {
            preferenceManager.saveUserId((int) userId);
            preferenceManager.saveUserName(fullName);

            if (savedImagePath != null) {
                preferenceManager.saveProfileImage(savedImagePath);
            }

            preferenceManager.setLoggedIn(true);

            Toast.makeText(this, "Profile created successfully!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ProfileSetupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to create profile", Toast.LENGTH_SHORT).show();
        }
    }
}
