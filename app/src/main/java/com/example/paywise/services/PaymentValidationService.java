package com.example.paywise.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.example.paywise.R;
import com.example.paywise.activities.MainActivity;
import com.example.paywise.database.TransactionDao;
import com.example.paywise.utils.Constants;
import com.example.paywise.utils.DateUtils;

/**
 * Foreground service that runs during payment processing
 * Shows notification to user that payment is being validated
 */
public class PaymentValidationService extends Service {

    private TransactionDao transactionDao;

    @Override
    public void onCreate() {
        super.onCreate();
        transactionDao = new TransactionDao(this);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String merchantName = intent.getStringExtra("merchant_name");
        double amount = intent.getDoubleExtra("amount", 0.0);

        // Start foreground service with notification
        startForeground(Constants.NOTIFICATION_ID_PAYMENT, createNotification(merchantName, amount));

        // Log service action
        logServiceAction("PaymentValidationService", "VALIDATE",
                "Validating payment to " + merchantName + " for ₹" + amount);

        // Simulate payment validation (in real app, this would do actual validation)
        try {
            Thread.sleep(2000); // 2 seconds validation
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Stop foreground service
        stopForeground(true);
        stopSelf();

        return START_NOT_STICKY;
    }

    private Notification createNotification(String merchantName, double amount) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constants.CHANNEL_ID)
                .setContentTitle(getString(R.string.notif_payment_validation))
                .setContentText("Processing payment to " + merchantName + " for ₹" + String.format("%.2f", amount))
                .setSmallIcon(R.drawable.ic_vault_business)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false);

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    Constants.CHANNEL_ID,
                    Constants.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("PayWise payment and vault notifications");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void logServiceAction(String serviceName, String actionType, String message) {
        String timestamp = DateUtils.getCurrentDateTime();
        transactionDao.insertServiceLog(serviceName, actionType, message, timestamp);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}