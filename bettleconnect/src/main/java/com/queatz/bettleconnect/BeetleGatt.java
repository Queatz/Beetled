package com.queatz.bettleconnect;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;

import com.queatz.bettleconnect.util.WriteQueue;

import java.util.UUID;

/**
 * Created by jacob on 7/23/17.
 *
 * Manages the active bluetooth link to the beetle.
 */
public class BeetleGatt extends BluetoothGattCallback {

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

            item.characteristic.setValue(item.value);
        }
    };

    private BluetoothGatt gatt;
    private BluetoothGattService service;

    private BluetoothGattCharacteristic serialPortCharacteristic;
    private BluetoothGattCharacteristic commandCharacteristic;

    private void close() {
        if (gatt == null) {
            return;
        }

        Beetle.getBeetleListener().onDisconnected();

        gatt.close();
        gatt = null;
    }

    public void write(String string) {
        queue.add(new BeetledGattWrite(serialPortCharacteristic, string));
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        this.gatt = gatt;

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
        service = gatt.getService(Config.BLE_SERVICE_UUID);

        if (service == null) {
            close();
            return;
        }

        commandCharacteristic = service.getCharacteristic(Config.BLE_COMMAND_UUID);
        serialPortCharacteristic = service.getCharacteristic(Config.BLE_SERIAL_PORT_UUID);

        if (commandCharacteristic == null || serialPortCharacteristic == null) {
            close();
            return;
        }

        queue.add(new BeetledGattWrite(commandCharacteristic, Config.BLUNO_PASSWORD));
        queue.add(new BeetledGattWrite(commandCharacteristic, Config.BLUNO_BAUDRATE_BUFFER));
        gatt.setCharacteristicNotification(serialPortCharacteristic, true);
        Beetle.getBeetleListener().onConnected();
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        UUID uuid = characteristic.getUuid();
        if (Config.BLE_SERIAL_PORT_UUID.equals(uuid)) {
            Beetle.getBeetleListener().onRead(characteristic.getStringValue(0));
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        queue.next();
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        UUID uuid = characteristic.getUuid();
        if (Config.BLE_SERIAL_PORT_UUID.equals(uuid)) {
            Beetle.getBeetleListener().onRead(characteristic.getStringValue(0));
        }
    }

    public BluetoothGatt getGatt() {
        return gatt;
    }
}
