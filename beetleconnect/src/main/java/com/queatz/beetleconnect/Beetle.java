package com.queatz.beetleconnect;

import android.app.Application;
import android.content.Context;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jacob on 9/1/17.
 *
 * Easy access to the beetle.
 */
public class Beetle {

    private static final Set<BeetleListener> beetleListeners = new HashSet<>();
    private static final BeetleListener beetleListener;
    private static BeetleManager beetleManager;
    private static BeetlePrefs beetlePrefs;
    private static boolean findFirst = false;

    static {
        beetleListener = new BeetleListener() {
            @Override
            public void onDisconnected() {
                synchronized (beetleListeners) {
                    for (BeetleListener b : beetleListeners) {
                        b.onDisconnected();
                    }
                }
            }

            @Override
            public void onRead(final String string) {
                synchronized (beetleListeners) {
                    for (BeetleListener b : beetleListeners) {
                        b.onRead(string);
                    }
                }
            }

            @Override
            public void onConnected() {
                synchronized (beetleListeners) {
                    for (BeetleListener b : beetleListeners) {
                        b.onConnected();
                    }
                }
            }
        };
    }

    /**
     * Must be called before using this class.
     */
    public static void initialize(Context context) {
        if (beetleManager == null) {
            beetleManager = new BeetleManager((Application) context.getApplicationContext());
        }

        if (beetlePrefs == null) {
            beetlePrefs = new BeetlePrefs(context);
        }
    }

    public static BeetleManager getBeetleManager() {
        if (beetleManager == null) {
            throw new IllegalStateException("Beetle was not initialized with Beetle.initialize(context)");
        }

        return beetleManager;
    }

    static BeetleListener getBeetleListener() {
        return beetleListener;
    }

    public static void subscribe(BeetleListener beetleListener) {
        Beetle.beetleListeners.add(beetleListener);
    }

    public static void unsubscribe(BeetleListener beetleListener) {
        beetleListeners.remove(beetleListener);
    }

    public static boolean stayConnected() {
        if (beetlePrefs == null) {
            throw new IllegalStateException("Beetle was not initialized with Beetle.initialize(context)");
        }

        return beetlePrefs.stayConnected();
    }

    /**
     * True - Automatically discover and connect at all times
     * False - Only connect when enable() is called
     */
    public static void stayConnected(boolean stayConnected) {
        if (beetlePrefs == null) {
            throw new IllegalStateException("Beetle was not initialized with Beetle.initialize(context)");
        }

        beetlePrefs.stayConnected(stayConnected);
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
