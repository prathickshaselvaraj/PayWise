package com.example.paywise.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import com.example.paywise.R;
import com.example.paywise.MainActivity;
import com.example.paywise.database.TransactionDao;
import com.example.paywise.managers.VaultManager;
import com.example.paywise.models.Vault;
import com.example.paywise.utils.Constants;
import com.example.paywise.utils.DateUtils;
import com.example.paywise.utils.SessionManager;
import java.util.List;

/**
 * VaultMonitorService - Background service for vault monitoring
 *
 * Features:
 * - Monitor vault balances and send low balance alerts
 * - Check for monthly vault reset
 * - Log service activities
 */
public class VaultMonitorService extends Service {

    private static final String CHANNEL_ID = "vault_monitor_channel";
    private static final int NOTIFICATION_ID = 1001;

    private VaultManager vaultManager;
    private TransactionDao transactionDao;
    private SessionManager sessionManager;

    @Override
    public void onCreate() {
        super.onCreate();

        vaultManager = new VaultManager(this);
        transactionDao = new TransactionDao(this);
        sessionManager = new SessionManager(this);

        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start as foreground service
        startForeground(NOTIFICATION_ID, createForegroundNotification());

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            logServiceAction("Service started but no user logged in");
            stopSelf();
            return START_NOT_STICKY;
        }

        int userId = sessionManager.getUserId();

        // Monitor vaults
        monitorVaults(userId);

        // Check for monthly reset
        checkMonthlyReset(userId);

        logServiceAction("Vault monitoring completed");

        // Stop service after monitoring
        stopSelf();

        return START_NOT_STICKY;
    }

    /**
     * Monitor all vaults for low balance
     */
    private void monitorVaults(int userId) {
        List<Vault> lowBalanceVaults = vaultManager.getLowBalanceVaults(userId);

        if (!lowBalanceVaults.isEmpty()) {
            sendLowBalanceNotification(lowBalanceVaults);
            logServiceAction("Low balance detected in " + lowBalanceVaults.size() + " vault(s)");
        }
    }

    /**
     * Check if any vault needs monthly reset
     */
    private void checkMonthlyReset(int userId) {
        if (vaultManager.needsReset(userId)) {
            boolean resetSuccess = vaultManager.resetMonthlyVaults(userId);

            if (resetSuccess) {
                sendResetNotification();
                logServiceAction("Monthly vault reset completed");
            } else {
                logServiceAction("Monthly vault reset failed");
            }
        }
    }

    /**
     * Send low balance notification
     */
    private void sendLowBalanceNotification(List<Vault> lowBalanceVaults) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        String contentText;
        if (lowBalanceVaults.size() == 1) {
            Vault vault = lowBalanceVaults.get(0);
            contentText = String.format("%s has low balance: ₹%.0f remaining",
                    vault.getDisplayName(),
                    vault.getRemainingBalance());
        } else {
            contentText = String.format("%d vaults have low balance", lowBalanceVaults.size());
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_payment)
                .setContentTitle("⚠️ Low Vault Balance Alert")
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(2001, builder.build());
    }

    /**
     * Send monthly reset notification
     */
    private void sendResetNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_payment)
                .setContentTitle("🔄 Monthly Vault Reset")
                .setContentText("Your vaults have been reset for the new month!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(2002, builder.build());
    }

    /**
     * Create foreground service notification
     */
    private Notification createForegroundNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_payment)
                .setContentTitle("ThinkPay AI")
                .setContentText("Monitoring your vaults...")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .build();
    }

    /**
     * Create notification channel for Android O and above
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Vault Monitor",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Monitors vault balances and monthly resets");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Log service action to database
     */
    private void logServiceAction(String message) {
        String timestamp = DateUtils.getCurrentDateTime();
        transactionDao.insertServiceLog("VaultMonitorService", "MONITOR", message, timestamp);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logServiceAction("Service stopped");
    }
}