package com.queatz.beetled;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.queatz.bettleconnect.Beetle;

/**
 * Created by jacob on 9/1/17.
 */

public class ActionService extends Service {
    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_DATA = "data";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra(EXTRA_ACTION)) {
            switch (intent.getStringExtra(EXTRA_ACTION)) {
                case Environment.UNLOCK_ACTION:
                    if (intent.hasExtra(EXTRA_DATA)) {
                        Beetle.getBeetleManager().send(intent.getStringExtra(EXTRA_DATA));
                        ((App) getApplication()).updateLockStatus();
                    }
                    break;
            }
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
