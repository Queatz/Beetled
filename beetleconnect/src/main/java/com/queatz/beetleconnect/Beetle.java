package com.queatz.beetleconnect;

import android.app.Application;
import android.content.Context;

/**
 * Created by jacob on 9/1/17.
 *
 * Easy access to the beetle.
 */
public class Beetle {

    private static BeetleListener beetleListener = null;
    private static BeetleManager beetleManager;
    private static boolean stayConnected = false;

    public static void initialize(Context context) {
        if (beetleManager == null) {
            beetleManager = new BeetleManager((Application) context.getApplicationContext());
        }
    }

    public static BeetleManager getBeetleManager() {
        if (beetleManager == null) {
            throw new IllegalStateException("Beetle was not initialized with Beetle.initialize(context)");
        }

        return beetleManager;
    }

    public static BeetleListener getBeetleListener() {
        if (beetleListener == null) {
            return BeetleListener.NOOP;
        }

        return beetleListener;
    }

    public static void subscribe(BeetleListener beetleListener) {
        Beetle.beetleListener = beetleListener;
    }

    public static boolean stayConnected() {
        return stayConnected;
    }

    public static void stayConnected(boolean stayConnected) {
        Beetle.stayConnected = stayConnected;
    }
}
