package com.queatz.beetled;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;
import android.widget.Toast;

import com.queatz.beetled.util.WriteQueue;

import java.util.UUID;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

/**
 * Created by jacob on 7/23/17.
 */

public abstract class BeetledGatt extends BluetoothGattCallback {

    private class BeetledGattWrite {
        final BluetoothGattCharacteristic characteristic;
        final String value;

        public BeetledGattWrite(BluetoothGattCharacteristic characteristic, String value) {
            this.characteristic = characteristic;
            this.value = value;
        }
    }

    private WriteQueue<BeetledGattWrite> queue = new WriteQueue<BeetledGattWrite>() {
        @Override
        public void emit(BeetledGattWrite item) {
            if (item.characteristic == null || gatt == null) {
                return;
            }

            item.characteristic.setValue(item.value);//.getBytes(Charset.forName("ISO-8859-1"))

            if (!gatt.writeCharacteristic(item.characteristic)) {
                err("gatt write failed");
            }
        }
    };

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
        gatt = null;
    }

    protected abstract void removed();
    protected abstract void read(String string);

    public void write(String string) {
        queue.add(new BeetledGattWrite(serialPortCharacteristic, string));
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

        queue.add(new BeetledGattWrite(commandCharacteristic, Environment.BLUNO_PASSWORD));
        queue.add(new BeetledGattWrite(commandCharacteristic, Environment.BLUNO_BAUDRATE_BUFFER));
        gatt.setCharacteristicNotification(serialPortCharacteristic, true);
        requestLockStatus();
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

        queue.next();
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
        if (true)return;
        app.run(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(app, string, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
