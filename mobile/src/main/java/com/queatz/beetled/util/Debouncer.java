package com.queatz.beetled.util;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by jacob on 7/23/17.
 */

public class Debouncer {
    private Runnable runnable;
    private boolean triggered;
    private long delay;
    private Handler handler;

    public Debouncer(final Runnable runnable, long delay) {
        this.runnable = new Runnable() {
            @Override
            public void run() {
                runnable.run();
                triggered = false;
            }
        };
        this.delay = delay;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void run() {
        if (this.triggered) {
            handler.removeCallbacks(runnable);
        }

        handler.postDelayed(runnable, delay);
    }
}
