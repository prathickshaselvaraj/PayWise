package com.example.paywise.receivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.paywise.R;
import com.example.paywise.activities.MainActivity;
import com.example.paywise.database.TransactionDao;
import com.example.paywise.utils.Constants;
import com.example.paywise.utils.DateUtils;

/**
 * Broadcast receiver for payment alerts and low balance notifications
 */
public class PaymentAlertReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action == null) {
            return;
        }

        switch (action) {
            case "com.example.paywise.LOW_BALANCE":
                handleLowBalance(context, intent);
                break;

            case "com.example.paywise.VAULT_RESET":
                handleVaultReset(context);
                break;

            case "com.example.paywise.PAYMENT_FAILED":
                handlePaymentFailed(context, intent);
                break;
        }

        // Log broadcast action
        logBroadcastAction(context, action);
    }

    private void handleLowBalance(Context context, Intent intent) {
        String vaultName = intent.getStringExtra("vault_name");
        double remainingBalance = intent.getDoubleExtra("remaining_balance", 0.0);

        String message = String.format("Your %s vault balance is low: ₹%.2f remaining",
                vaultName, remainingBalance);

        showNotification(
                context,
                Constants.NOTIFICATION_ID_LOW_BALANCE,
                context.getString(R.string.notif_low_balance_title),
                message
        );
    }

    private void handleVaultReset(Context context) {
        showNotification(
                context,
                Constants.NOTIFICATION_ID_VAULT_RESET,
                "Monthly Reset",
                context.getString(R.string.notif_vault_reset)
        );
    }

    private void handlePaymentFailed(Context context, Intent intent) {
        String merchantName = intent.getStringExtra("merchant_name");
        String reason = intent.getStringExtra("reason");

        String message = "Payment to " + merchantName + " failed: " + reason;

        showNotification(
                context,
                Constants.NOTIFICATION_ID_PAYMENT,
                context.getString(R.string.payment_failed),
                message
        );
    }

    private void showNotification(Context context, int notificationId, String title, String message) {
        createNotificationChannel(context);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_vault_emergency)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    Constants.CHANNEL_ID,
                    Constants.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("PayWise payment and vault notifications");

            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void logBroadcastAction(Context context, String action) {
        TransactionDao transactionDao = new TransactionDao(context);
        String timestamp = DateUtils.getCurrentDateTime();
        transactionDao.insertServiceLog("PaymentAlertReceiver", "BROADCAST",
                "Received broadcast: " + action, timestamp);
    }
}