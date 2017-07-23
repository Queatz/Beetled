package com.queatz.beetled;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

/**
 * Created by jacob on 7/23/17.
 */

public class BeetledNotification {

    private App app;
    private NotificationManager notificationManager;
    private final boolean status;

    public BeetledNotification(App app, boolean status) {
        this.app = app;
        notificationManager = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
        this.status = status;
    }

    public void show(Notification notification) {
        notificationManager.notify("", 0, notification);
    }

    public void hide() {
        notificationManager.cancel("", 0);
    }

    public void showNotification() {
        NotificationCompat.Builder builder;
        Intent resultIntent;

        builder = new NotificationCompat.Builder(app)
                .setDefaults(0)
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setSmallIcon(status ? R.drawable.icon_locked : R.drawable.icon)
                .setLargeIcon(BitmapFactory.decodeResource(app.getResources(), status ? R.drawable.icon_locked : R.drawable.icon))
                .setContentTitle("Front Door " + (status ? "Locked" : "Unlocked"))
                .setContentText("Tap to " + (status ? "unlock" : "lock"))
                .setOngoing(true)
                .setColor(app.getResources().getColor(android.R.color.background_dark))
                .setCategory(Notification.CATEGORY_SOCIAL);

        resultIntent = new Intent(app, AdvertiseService.class);
        resultIntent.putExtra(AdvertiseService.EXTRA_ACTION, Environment.UNLOCK_ACTION);
        resultIntent.putExtra(AdvertiseService.EXTRA_DATA, (status ? Environment.LOCK_STATE_UNLOCK : Environment.LOCK_STATE_LOCK));

        builder.setContentIntent(PendingIntent.getService(app, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        show(builder.build());
    }
}
