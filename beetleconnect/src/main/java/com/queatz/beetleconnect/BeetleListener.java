package com.queatz.beetleconnect;

/**
 * Created by jacob on 9/1/17.
 */

public interface BeetleListener {
    BeetleListener NOOP = new BeetleListener() {
        @Override public void onDisconnected() {}
        @Override public void onRead(String string) {}
        @Override public void onConnected() {}
    };

    void onDisconnected();
    void onRead(String string);
    void onConnected();
}
