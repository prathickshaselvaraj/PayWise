package com.example.paywise.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.example.paywise.database.TransactionDao;
import com.example.paywise.managers.VaultManager;
import com.example.paywise.utils.DateUtils;
import com.example.paywise.utils.PreferenceManager;

/**
 * Background service that monitors vaults and performs monthly reset
 * This service runs periodically to check if vault reset is needed
 */
public class VaultMonitorService extends Service {

    private Handler handler;
    private Runnable monitorRunnable;
    private static final long MONITOR_INTERVAL = 24 * 60 * 60 * 1000; // 24 hours

    private VaultManager vaultManager;
    private TransactionDao transactionDao;
    private PreferenceManager preferenceManager;

    @Override
    public void onCreate() {
        super.onCreate();

        vaultManager = new VaultManager(this);
        transactionDao = new TransactionDao(this);
        preferenceManager = new PreferenceManager(this);

        handler = new Handler();

        monitorRunnable = new Runnable() {
            @Override
            public void run() {
                performMonitoring();
                // Schedule next run
                handler.postDelayed(this, MONITOR_INTERVAL);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start monitoring
        handler.post(monitorRunnable);

        // Log service start
        logServiceAction("VaultMonitorService", "START", "Vault monitoring started");

        return START_STICKY; // Service will be restarted if killed
    }

    private void performMonitoring() {
        int userId = preferenceManager.getUserId();

        if (userId != -1) {
            // Check if monthly reset is needed
            if (vaultManager.needsReset(userId)) {
                boolean resetSuccess = vaultManager.resetMonthlyVaults(userId);


                if (resetSuccess) {
                    logServiceAction("VaultMonitorService", "RESET",
                            "Monthly vault reset completed for user " + userId);

                    // Send broadcast for vault reset notification
                    Intent broadcastIntent = new Intent("com.example.paywise.VAULT_RESET");
                    sendBroadcast(broadcastIntent);
                }
            }
        }
    }

    private void logServiceAction(String serviceName, String actionType, String message) {
        String timestamp = DateUtils.getCurrentDateTime();
        transactionDao.insertServiceLog(serviceName, actionType, message, timestamp);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(monitorRunnable);
        logServiceAction("VaultMonitorService", "STOP", "Vault monitoring stopped");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // This is not a bound service
    }
}