package com.queatz.beetled;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.queatz.bettleconnect.Beetle;
import com.queatz.bettleconnect.BeetleListener;
import com.queatz.bettleconnect.util.Debouncer;

/**
 * Created by jacob on 7/23/17.
 */

public class App extends Application {

    private Debouncer debouncer;

    public void updateLockStatus() {
        debouncer.run();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.debouncer = new Debouncer(new Runnable() {
            @Override
            public void run() {
                Beetle.getBeetleManager().send(Environment.LOCK_STATE_REQUEST);
            }
        }, 4000);

        Beetle.subscribe(new BeetleListener() {
            @Override
            public void onConnected() {
                Beetle.getBeetleManager().send(Environment.LOCK_STATE_REQUEST);
            }

            @Override
            public void onDisconnected() {
                new BeetledNotification(App.this, false).hide();
            }

            @Override
            public void onRead(String string) {
                if (Environment.LOCK_STATE_LOCK.equals(string)) {
                    new BeetledNotification(App.this, true).showNotification();
                } else if (Environment.LOCK_STATE_UNLOCK.equals(string)) {
                    new BeetledNotification(App.this, false).showNotification();
                }
            }
        });
    }

    public void run(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}