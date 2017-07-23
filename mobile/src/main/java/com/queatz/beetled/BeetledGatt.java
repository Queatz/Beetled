package com.queatz.beetled;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.UUID;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

/**
 * Created by jacob on 7/23/17.
 */

public abstract class BeetledGatt extends BluetoothGattCallback {

    private final App app;
    private BluetoothGatt gatt;
    private BluetoothGattService service;

    private BluetoothGattCharacteristic serialPortCharacteristic;
    private BluetoothGattCharacteristic commandCharacteristic;

    public BeetledGatt(App app) {
        this.app = app;
    }

    private void close() {
        if (gatt == null) {
            return;
        }

        removed();

        gatt.close();
    }

    protected abstract void removed();
    protected abstract void read(String string);

    public boolean write(String string) {
        if (serialPortCharacteristic == null || gatt == null) {
            return false;
        }

        serialPortCharacteristic.setValue(string.getBytes(Charset.forName("ISO-8859-1")));

        if (!gatt.writeCharacteristic(serialPortCharacteristic)) {
            err("gatt write failed");
            return false;
        }

        return true;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        this.gatt = gatt;

        Log.e(getClass().getSimpleName(), "advertise - onConnectionStateChange " + gatt.getDevice() + " - newState = " + newState);

        switch (newState) {
            case BluetoothProfile.STATE_DISCONNECTED:
                close();
                break;
            case BluetoothProfile.STATE_CONNECTED:
                gatt.discoverServices();
                break;
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        this.gatt = gatt;
        err("advertise - onServicesDiscovered " + gatt.getDevice() + " - getService = " + gatt.getService(Environment.BLE_SERVICE_UUID));
        service = gatt.getService(Environment.BLE_SERVICE_UUID);

        if (service == null) {
            close();
            return;
        }

        commandCharacteristic = service.getCharacteristic(Environment.BLE_COMMAND_UUID);
        serialPortCharacteristic = service.getCharacteristic(Environment.BLE_SERIAL_PORT_UUID);

        if (commandCharacteristic == null ||
                serialPortCharacteristic == null) {
            err("Wrong characteristics");
            close();
            return;
        }

        commandCharacteristic.setValue(Environment.BLUNO_PASSWORD);
        gatt.writeCharacteristic(commandCharacteristic);
        commandCharacteristic.setValue(Environment.BLUNO_BAUDRATE_BUFFER);
        gatt.writeCharacteristic(commandCharacteristic);
        gatt.setCharacteristicNotification(serialPortCharacteristic, true);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                requestLockStatus();
            }
        }, 100);
    }

    public void requestLockStatus() {
        write(Environment.LOCK_STATE_REQUEST);
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.e(getClass().getSimpleName(), "advertise - onCharacteristicRead " + gatt.getDevice() + " - characteristic = " + characteristic.getUuid());

        UUID uuid = characteristic.getUuid();
        if (Environment.BLE_SERIAL_PORT_UUID.equals(uuid)) {
            read(characteristic.getStringValue(0));
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        UUID uuid = characteristic.getUuid();
        if (Environment.BLE_SERIAL_PORT_UUID.equals(uuid)) {
            err("write: " + (status == GATT_SUCCESS));
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        UUID uuid = characteristic.getUuid();
        if (Environment.BLE_SERIAL_PORT_UUID.equals(uuid)) {
            read(characteristic.getStringValue(0));
        }
    }

    public BluetoothGatt getGatt() {
        return gatt;
    }

    private void err(final String string) {
        app.run(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(app, string, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
