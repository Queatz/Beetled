package com.queatz.bettleconnect;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ParcelUuid;

import java.util.ArrayList;
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

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private ScanCallback scanCallback;
    private BluetoothLeScanner bluetoothLeScanner;

    private BluetoothAdapter.LeScanCallback leScanCallbackOld;

    private boolean setup = false;
    private Handler handler;
    private BeetleGatt beetleGatt;

    public BeetleManager(Application app) {
        this.app = app;
    }

    /**
     * Enable scan and connect.
     */
    public boolean enable() {
        if (!app.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }

        disable();

        if (!setup) {
            bluetoothManager = (BluetoothManager) app.getSystemService(Context.BLUETOOTH_SERVICE);

            if (bluetoothManager != null) {
                bluetoothAdapter = bluetoothManager.getAdapter();
            }

            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
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
            };

            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

            handler = new Handler(app.getMainLooper());

            setup = true;
        }

        ScanFilter scanFilter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(Config.BLE_SERVICE_UUID))
                .build();

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(REPORTING_DELAY)
                .build();

        List<ScanFilter> scanFilters = new ArrayList<>();
        scanFilters.add(scanFilter);

        if (bluetoothLeScanner != null) {
            bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
        } else {
            return false;
        }

        return true;
    }

    /**
     * Disable scan and disconnect.
     */
    public void disable() {
        if (scanCallback != null && bluetoothLeScanner != null) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.stopScan(scanCallback);
            }
        }

        if (beetleGatt != null) {
            beetleGatt.getGatt().close();
            beetleGatt = null;
        }

        if (leScanCallbackOld != null) {
            if (bluetoothAdapter != null) {
                bluetoothAdapter.stopLeScan(leScanCallbackOld);
            }
        }
    }

    /**
     * Send data to the beetle.
     * @return if it was successful.
     */
    public boolean send(String data) {
        if (data == null) {
            return false;
        }

        if (beetleGatt == null) {
            return false;
        }

        beetleGatt.write(data);
        return true;
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

    private void foundDevice(final BluetoothDevice device) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                doFoundDevice(device);
            }
        });
    }

    private void doFoundDevice(final BluetoothDevice device) {
        if (beetleGatt != null) {
            return;
        }

        beetleGatt = new BeetleGatt();

        // Prefer TRANSPORT_LE if possible
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            device.connectGatt(app, true, beetleGatt, BluetoothDevice.TRANSPORT_LE);
        } else {
            device.connectGatt(app, true, beetleGatt);
        }
    }
}
