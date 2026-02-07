package com.example.paywise.receivers;

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
import com.example.paywise.services.VaultMonitorService;

/**
 * PaymentAlertReceiver - Broadcast receiver for scheduled alerts
 *
 * Receives broadcasts for:
 * - Daily vault balance checks
 * - Monthly reset reminders
 * - Payment limit alerts
 */
public class PaymentAlertReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "payment_alerts_channel";
    public static final String ACTION_DAILY_CHECK = "com.example.paywise.action.DAILY_CHECK";
    public static final String ACTION_MONTHLY_RESET = "com.example.paywise.action.MONTHLY_RESET";
    public static final String ACTION_LIMIT_ALERT = "com.example.paywise.action.LIMIT_ALERT";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action == null) {
            return;
        }

        switch (action) {
            case ACTION_DAILY_CHECK:
                handleDailyCheck(context);
                break;

            case ACTION_MONTHLY_RESET:
                handleMonthlyReset(context);
                break;

            case ACTION_LIMIT_ALERT:
                handleLimitAlert(context, intent);
                break;

            case Intent.ACTION_BOOT_COMPLETED:
                // Restart scheduled alarms after device boot
                handleBootCompleted(context);
                break;
        }
    }

    /**
     * Handle daily vault check
     */
    private void handleDailyCheck(Context context) {
        // Start VaultMonitorService to check vaults
        Intent serviceIntent = new Intent(context, VaultMonitorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    /**
     * Handle monthly reset reminder
     */
    private void handleMonthlyReset(Context context) {
        sendNotification(
                context,
                "Monthly Vault Reset",
                "Your vaults will reset at the start of next month",
                3001
        );
    }

    /**
     * Handle spending limit alert
     */
    private void handleLimitAlert(Context context, Intent intent) {
        String vaultName = intent.getStringExtra("vault_name");
        double remaining = intent.getDoubleExtra("remaining", 0);

        String message = String.format("%s is running low: ₹%.0f remaining", vaultName, remaining);

        sendNotification(
                context,
                "⚠️ Spending Limit Alert",
                message,
                3002
        );
    }

    /**
     * Handle device boot - reschedule alarms
     */
    private void handleBootCompleted(Context context) {
        // TODO: Reschedule daily and monthly alarms using AlarmManager
        // This would be implemented in a separate AlarmScheduler class
    }

    /**
     * Send notification
     */
    private void sendNotification(Context context, String title, String message, int notificationId) {
        createNotificationChannel(context);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_payment)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * Create notification channel
     */
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Payment Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Alerts for vault balances and payment limits");

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
}