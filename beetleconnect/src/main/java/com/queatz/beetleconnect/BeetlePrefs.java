package com.queatz.beetleconnect;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jacob on 9/6/17.
 */

public class BeetlePrefs {

    private static final String PREFS = "prefs";
    private static final String STAY_CONNECTED = "stay_connected";

    private SharedPreferences sharedPreferences;

    public BeetlePrefs(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean stayConnected() {
        return sharedPreferences.getBoolean(STAY_CONNECTED, false);
    }

    public void stayConnected(boolean value) {
        sharedPreferences.edit().putBoolean(STAY_CONNECTED, value).apply();
    }

}
