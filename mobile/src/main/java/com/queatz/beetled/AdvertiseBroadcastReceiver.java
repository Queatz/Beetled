package com.queatz.beetled;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by jacob on 7/23/17.
 */

public class AdvertiseBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, AdvertiseService.class);

        if (intent != null) {
            Bundle extras = new Bundle();
            extras.putString(AdvertiseService.EXTRA_ACTION, intent.getAction());
            serviceIntent.putExtras(extras);
        }

        context.startService(serviceIntent);
    }
}

