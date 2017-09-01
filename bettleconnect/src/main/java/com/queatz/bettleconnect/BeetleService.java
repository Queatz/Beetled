package com.queatz.bettleconnect;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by jacob on 7/23/17.
 *
 * This service scans and connects to the beetle.
 */
public class BeetleService extends Service {

    public static final String EXTRA_ACTION = "action";

    private BeetleManager beetleManager;

    protected static BeetleService service;

    protected BeetleManager getBeetleManager() {
        return beetleManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        service = this;
        beetleManager = new BeetleManager(getApplication());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            beetleManager.enable();
        } else {
            if (intent.hasExtra(EXTRA_ACTION)) {
                String action = intent.getStringExtra(EXTRA_ACTION);

                if (action != null) switch (action) {
                    case Intent.ACTION_USER_PRESENT:
                    case Intent.ACTION_SCREEN_ON:
                    case Intent.ACTION_BOOT_COMPLETED:
                    case Intent.ACTION_BATTERY_OKAY:
                        beetleManager.enable();
                        break;
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                            case BluetoothAdapter.STATE_ON:
                            case BluetoothAdapter.STATE_CONNECTED:
                                beetleManager.enable();
                                break;
                            case BluetoothAdapter.STATE_DISCONNECTED:
                            case BluetoothAdapter.STATE_DISCONNECTING:
                            case BluetoothAdapter.STATE_TURNING_OFF:
                            case BluetoothAdapter.STATE_OFF:
                                beetleManager.disable();
                        }
                        break;
                    case Intent.ACTION_BATTERY_LOW:
                        beetleManager.disable();
                        break;
                }
            } else {
                beetleManager.enable();
            }
        }

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
