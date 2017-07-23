package com.queatz.beetled;

import android.app.AlarmManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by jacob on 7/23/17.
 */

public class AdvertiseService extends Service {

    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_DATA = "data";
    private AlarmManager alarmManager;
    private App app;

    @Override
    public void onCreate() {
        super.onCreate();
        this.app = (App) getApplicationContext();
        this.alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            app.getBeetled().enable();
        } else {
            if (intent.hasExtra(EXTRA_ACTION)) {
                String action = intent.getStringExtra(EXTRA_ACTION);

                if (action != null) switch (action) {
                    case Environment.UNLOCK_ACTION:
                        if (intent.hasExtra(EXTRA_DATA)) {
                            app.getBeetled().send(intent.getStringExtra(EXTRA_DATA));
                        }
                        break;
                    case Intent.ACTION_USER_PRESENT:
                    case Intent.ACTION_SCREEN_ON:
                    case Intent.ACTION_BOOT_COMPLETED:
                    case Intent.ACTION_BATTERY_OKAY:
                        app.getBeetled().enable();
                        break;
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                            case BluetoothAdapter.STATE_ON:
                            case BluetoothAdapter.STATE_CONNECTED:
                                app.getBeetled().enable();
                                break;
                            case BluetoothAdapter.STATE_DISCONNECTED:
                            case BluetoothAdapter.STATE_DISCONNECTING:
                            case BluetoothAdapter.STATE_TURNING_OFF:
                            case BluetoothAdapter.STATE_OFF:
                                app.getBeetled().disable();
                        }
                        break;
                    case Intent.ACTION_BATTERY_LOW:
                        app.getBeetled().disable();
                        break;
                }
            } else {
                app.getBeetled().enable();
            }
        }

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
