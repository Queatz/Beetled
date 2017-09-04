package com.queatz.beetleconnect;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jacob on 7/23/17.
 *
 * Manages the connection to the beetle from the library user's perspective.
 */
public class BeetleManager {

    private static final long REPORTING_DELAY = 500;

    private final Application app;

    private BluetoothAdapter bluetoothAdapter;
    private ScanCallback scanCallback;
    private BluetoothLeScanner bluetoothLeScanner;

    // For old BLE api
    private BluetoothAdapter.LeScanCallback leScanCallbackOld;

    private final LinkedList<BeetleGatt> gatts = new LinkedList<>();

    private boolean locked = false;
    private boolean setup = false;
    private Handler handler;
    private Handler background;

    private Runnable flush = new Runnable() {
        @Override
        public void run() {
            if (leScanCallbackOld == null && scanCallback != null && bluetoothLeScanner != null) {
                bluetoothLeScanner.flushPendingScanResults(scanCallback);
            }

            background.postDelayed(flush, 2000);
        }
    };

    public BeetleManager(Application app) {
        this.app = app;
        handler = new Handler(app.getMainLooper());
        background = new Handler();
    }

    /**
     * Enable scan and connect.
     */
    public boolean enable() {
        if (!app.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(app, "BLE not supported on this device", Toast.LENGTH_SHORT).show();
            return false;
        }

        disable();

        if (!setup) {
            BluetoothManager bluetoothManager = (BluetoothManager) app.getSystemService(Context.BLUETOOTH_SERVICE);

            if (bluetoothManager != null) {
                bluetoothAdapter = bluetoothManager.getAdapter();
            }

            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                Toast.makeText(app, "BLE not enabled", Toast.LENGTH_SHORT).show();
                return false;
            }

            scanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    foundDevice(result.getDevice());
                }

                @Override
                public void onScanFailed(int errorCode) {
                    if (errorCode == SCAN_FAILED_FEATURE_UNSUPPORTED) {
                        oldScan();
                    }
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    for (ScanResult result : results) {
                        foundDevice(result.getDevice());
                    }
                }
            };

            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            setup = true;
        }

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(REPORTING_DELAY)
                .build();

        if (bluetoothLeScanner != null) {
            bluetoothLeScanner.startScan(null, scanSettings, scanCallback);
        } else {
            return false;
        }

        background.postDelayed(flush, 1000);

        return true;
    }

    /**
     * Disable scan and disconnect.
     */
    public void disable() {
        locked = false;

        if (background != null) {
            background.removeCallbacks(flush);
        }

        if (scanCallback != null && bluetoothLeScanner != null) {
            bluetoothLeScanner.stopScan(scanCallback);
        }

        synchronized (gatts) {
            while (!gatts.isEmpty()) {
                gatts.removeFirst().close();
            }
        }

        oldScanDisable();
    }

    /**
     * Send data to the beetle.
     * @return if it was successful.
     */
    public boolean send(String data) {
        if (data == null) {
            return false;
        }

        if (gatts.isEmpty()) {
            return false;
        }

        synchronized (gatts) {
            for (BeetleGatt gatt : gatts) {
                gatt.write(data);
            }
        }

        return true;
    }

    /**
     * Called when a potential beetle is found
     */
    private void foundDevice(final BluetoothDevice device) {
        if (isLocked()) {
            return;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isLocked()) {
                    return;
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    device.connectGatt(app, true, newGatt(), BluetoothDevice.TRANSPORT_LE);
                } else {
                    device.connectGatt(app, true, newGatt());
                }
            }
        });
    }

    /**
     * Creates a new tracked gatt
     */
    private BeetleGatt newGatt() {
        synchronized (gatts) {
            BeetleGatt gatt = new BeetleGatt();

            gatts.add(gatt);
            return gatt;
        }
    }

    /*
     * Single connection locking
     */

    synchronized void unlock(BeetleGatt gatt) {
        locked = false;
    }

    synchronized void lock(BeetleGatt gatt) {
        locked = true;
    }

    boolean isLocked() {
        return locked;
    }

    /**
     * Implementation of old Android Scan API for pre Bluetooth 4.1 devices.
     */
    private void oldScan() {
        if (leScanCallbackOld != null) {
            bluetoothAdapter.stopLeScan(leScanCallbackOld);
        }

        leScanCallbackOld = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                foundDevice(device);
            }
        };

        bluetoothAdapter.startLeScan(
                new UUID[] { Config.BLE_SERVICE_UUID },
                leScanCallbackOld
        );
    }

    private void oldScanDisable() {
        if (leScanCallbackOld != null) {
            if (bluetoothAdapter != null) {
                bluetoothAdapter.stopLeScan(leScanCallbackOld);
            }
        }
    }
}
