package com.example.paywise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.example.paywise.R;
import com.example.paywise.utils.PreferenceManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        preferenceManager = new PreferenceManager(this);

        // Navigate after delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateToNextScreen();
            }
        }, SPLASH_DELAY);
    }

    private void navigateToNextScreen() {
        Intent intent;

        if (preferenceManager.isLoggedIn()) {
            // User already registered, go to MainActivity
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            // First time user, go to ProfileSetupActivity
            intent = new Intent(SplashActivity.this, ProfileSetupActivity.class);
        }

        startActivity(intent);
        finish();
    }
}