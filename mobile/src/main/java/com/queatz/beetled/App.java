package com.queatz.beetled;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

/**
 * Created by jacob on 7/23/17.
 */

public class App extends Application {

    private Beetled beetled;

    @Override
    public void onCreate() {
        super.onCreate();
        this.beetled = new Beetled(this);
    }


    public Beetled getBeetled() {
        return beetled;
    }

    public void run(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
