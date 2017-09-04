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
    private static boolean findFirst = false;

    /**
     * Must be called before using this class.
     */
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

    static BeetleListener getBeetleListener() {
        if (beetleListener == null) {
            return BeetleListener.NOOP;
        }

        return beetleListener;
    }

    /**
     * @param beetleListener Callback
     */
    public static void subscribe(BeetleListener beetleListener) {
        Beetle.beetleListener = beetleListener;
    }

    public static boolean stayConnected() {
        return stayConnected;
    }

    /**
     * True - Automatically discover and connect at all times
     * False - Only connect when enable() is called
     */
    public static void stayConnected(boolean stayConnected) {
        Beetle.stayConnected = stayConnected;
    }

    public static boolean isFindFirst() {
        return findFirst;
    }

    /**
     * True - Only find the first Beetle
     * False - Find as many Beetles as possible
     */
    public static void setFindFirst(boolean findFirst) {
        Beetle.findFirst = findFirst;
    }
}
