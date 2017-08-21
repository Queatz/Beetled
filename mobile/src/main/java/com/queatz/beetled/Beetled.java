package com.queatz.beetled;

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
import android.widget.Toast;

import com.queatz.beetled.util.Debouncer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jacob on 7/23/17.
 */

public class Beetled {

    private static final long REPORTING_DELAY = 500;

    private final App app;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private ScanCallback scanCallback;
    private BluetoothLeScanner bluetoothLeScanner;

    private BluetoothAdapter.LeScanCallback leScanCallbackOld;

    private boolean setup = false;
    private Handler handler;
    private BeetledGatt beetledGatt;
    private Debouncer debouncer;

    public Beetled(App app) {
        this.app = app;
        this.debouncer = new Debouncer(new Runnable() {
            @Override
            public void run() {
                beetledGatt.requestLockStatus();
            }
        }, 4000);
    }

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
                    err("advertise - scan success");
                    foundDevice(result.getDevice());
                }

                @Override
                public void onScanFailed(int errorCode) {
                    err("advertise - scan failed " + errorCode);

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
                .setServiceUuid(new ParcelUuid(Environment.BLE_SERVICE_UUID))
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
            err("advertise - scanner null");
            return false;
        }

        return true;
    }

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

        boolean success = bluetoothAdapter.startLeScan(
                new UUID[] { Environment.BLE_SERVICE_UUID },
                leScanCallbackOld
        );

        if (success) {
            err("advertise - scanner started using old method");
        } else {
            err("advertise - scanner failed completely");
        }
    }

    private void err(String string) {
        if (true)return;
        Toast.makeText(app, string, Toast.LENGTH_SHORT).show();
    }


    private void foundDevice(final BluetoothDevice device) {
        err("advertise - device found " + device);

        handler.post(new Runnable() {
            @Override
            public void run() {
                doFoundDevice(device);
            }
        });
    }

    private void doFoundDevice(final BluetoothDevice device) {
        if (beetledGatt != null) {
            return;
        }

        beetledGatt = new BeetledGatt(app) {
            @Override
            protected void removed() {
                beetledGatt = null;
                new BeetledNotification(app, false).hide();
            }

            @Override
            protected void read(String string) {
                if (Environment.LOCK_STATE_LOCK.equals(string)) {
                    new BeetledNotification(app, true).showNotification();
                } else if (Environment.LOCK_STATE_UNLOCK.equals(string)) {
                    new BeetledNotification(app, false).showNotification();
                }
            }
        };
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            device.connectGatt(app, true, beetledGatt, BluetoothDevice.TRANSPORT_LE);
        } else {
            device.connectGatt(app, true, beetledGatt);
        }
    }


    public void disable() {
        if (scanCallback != null && bluetoothLeScanner != null) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.stopScan(scanCallback);
            }
        }

        if (beetledGatt != null) {
            beetledGatt.getGatt().close();
            beetledGatt = null;
        }

        if (leScanCallbackOld != null) {
            if (bluetoothAdapter != null) {
                bluetoothAdapter.stopLeScan(leScanCallbackOld);
            }
        }
    }

    public void send(String data) {
        if (data == null) {
            return;
        }

        if (beetledGatt == null) {
            err("Not connected");
            return;
        }

        beetledGatt.write(data);

        debouncer.run();
    }
}
