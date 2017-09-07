package com.queatz.beetleconnect;

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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Beetle.initialize(getApplicationContext());

        if (intent == null) {
            Beetle.getBeetleManager().enable();
        } else {
            if (intent.hasExtra(EXTRA_ACTION)) {
                String action = intent.getStringExtra(EXTRA_ACTION);

                if (action != null) switch (action) {
                    case Intent.ACTION_USER_PRESENT:
                    case Intent.ACTION_SCREEN_ON:
                    case Intent.ACTION_BOOT_COMPLETED:
                    case Intent.ACTION_BATTERY_OKAY:
                        Beetle.getBeetleManager().setHighPower(false);
                        Beetle.getBeetleManager().enable();
                        break;
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                            case BluetoothAdapter.STATE_ON:
                            case BluetoothAdapter.STATE_CONNECTED:
                                Beetle.getBeetleManager().setHighPower(true);
                                Beetle.getBeetleManager().enable();
                                break;
                            case BluetoothAdapter.STATE_DISCONNECTED:
                            case BluetoothAdapter.STATE_DISCONNECTING:
                            case BluetoothAdapter.STATE_TURNING_OFF:
                            case BluetoothAdapter.STATE_OFF:
                                Beetle.getBeetleManager().disable();
                        }
                        break;
                    case Intent.ACTION_BATTERY_LOW:
                        Beetle.getBeetleManager().disable();
                        break;
                }
            } else {
                Beetle.getBeetleManager().enable();
            }
        }

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
