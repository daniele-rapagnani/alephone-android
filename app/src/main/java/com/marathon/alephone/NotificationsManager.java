package com.marathon.alephone;


import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationsManager {
    protected static String CHANNEL_ID = "AlephOne";

    public class ProgressNotification {
        private final NotificationCompat.Builder builder;
        private final Activity activity;
        private final NotificationManagerCompat nm;
        private final int id;

        protected ProgressNotification(
            Activity activity,
            int id,
            String title,
            String text,
            int icon
        ) {
            this.activity = activity;
            this.id = id;

            builder = new NotificationCompat.Builder(
                    this.activity,
                    NotificationsManager.CHANNEL_ID
            )
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setNotificationSilent()
            ;

            this.nm = NotificationManagerCompat.from(this.activity);
        }

        public void showWithProgress(int step, int total) {
            this.builder.setProgress(total, step, false);
            this.nm.notify(this.id, this.builder.build());
        }

        public void showIndeterminate() {
            this.builder.setProgress(0, 0, true);
            this.nm.notify(this.id, this.builder.build());
        }

        public void close() {
            this.nm.cancel(this.id);
        }
    }

    private final Activity activity;

    public NotificationsManager(Activity activity) {
        this.activity = activity;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NotificationsManager.CHANNEL_ID,
                    "AlephOne Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            channel.setDescription("AlephOne notification channel");

            NotificationManager notificationManager = activity.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public ProgressNotification createProgressNotification(
        int id,
        String title,
        String text,
        int icon
    )
    {
        return new ProgressNotification(
            this.activity,
            id,
            title,
            text,
            icon
        );
    }
}
