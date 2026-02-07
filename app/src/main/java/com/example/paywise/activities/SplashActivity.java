package com.example.paywise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.paywise.R;
import com.example.paywise.database.UserDao;
import com.example.paywise.utils.SessionManager;

/**
 * SplashActivity - App entry point with brand display
 *
 * Flow:
 * 1. Show splash screen for 2 seconds
 * 2. Check if user exists in database
 * 3. If user exists → Navigate to LoginActivity
 * 4. If no user → Navigate to AccountSetupActivity
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 seconds

    private SessionManager sessionManager;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize managers
        sessionManager = new SessionManager(this);
        userDao = new UserDao(this);

        // Delayed navigation after splash duration
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            navigateToNextScreen();
        }, SPLASH_DURATION);
    }

    /**
     * Determine which screen to navigate to based on user status
     */
    private void navigateToNextScreen() {
        Intent intent;

        // Check if any user exists in database
        if (userDao.isUserExists()) {
            // User exists → Go to login
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        } else {
            // No user → Go to account setup
            intent = new Intent(SplashActivity.this, AccountSetupActivity.class);
        }

        startActivity(intent);
        finish(); // Close splash so user can't go back to it
    }

    @Override
    public void onBackPressed() {
        // Disable back button on splash screen
        // Do nothing
    }
}