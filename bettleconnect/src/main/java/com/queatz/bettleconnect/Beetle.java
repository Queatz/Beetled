package com.queatz.bettleconnect;

/**
 * Created by jacob on 9/1/17.
 *
 * Easy access to the beetle.
 */
public class Beetle {

    private static BeetleListener beetleListener = null;

    public static BeetleManager getBeetleManager() {
        if (BeetleService.service == null) {
            return null;
        }

        return BeetleService.service.getBeetleManager();
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
}
