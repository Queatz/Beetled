package com.queatz.beetleconnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by jacob on 7/23/17.
 *
 * This receiver listens to Android events that may help the beetle stay connected.
 *
 * See the AndroidManifest declaration.
 */
public class BeetleBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Beetle.stayConnected()) {
            return;
        }

        Intent serviceIntent = new Intent(context, BeetleService.class);

        if (intent != null) {
            Bundle extras = new Bundle();
            extras.putString(BeetleService.EXTRA_ACTION, intent.getAction());
            serviceIntent.putExtras(extras);
        }

        context.startService(serviceIntent);
    }
}
